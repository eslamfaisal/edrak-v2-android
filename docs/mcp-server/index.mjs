#!/usr/bin/env node

/**
 * Edrak Documentation MCP Server
 * 
 * A lightweight MCP server that serves Edrak project documentation
 * from the docs/ directory, making it accessible to AI assistants.
 */

import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import {
  CallToolRequestSchema,
  ListResourcesRequestSchema,
  ReadResourceRequestSchema,
  ListToolsRequestSchema,
} from "@modelcontextprotocol/sdk/types.js";
import * as fs from "fs";
import * as path from "path";

const DOCS_DIR = path.resolve(
  process.env.EDRAK_DOCS_DIR ||
    path.join(path.dirname(new URL(import.meta.url).pathname), "..", "content")
);
const MKDOCS_PATH = path.join(path.dirname(DOCS_DIR), "mkdocs.yml");

// ── Path safety ───────────────────────────────────────────────
function validateDocPath(relPath) {
  if (!relPath || typeof relPath !== "string") {
    return "Path is required and must be a non-empty string.";
  }
  if (relPath.includes("..") || path.isAbsolute(relPath)) {
    return "Path must be relative and cannot contain '..'.";
  }
  if (!relPath.endsWith(".md")) {
    return "Path must end with '.md'.";
  }
  return null;
}

// ── Section mapping (first path segment → nav section keyword) ─
const SECTION_MAP = {
  architecture: "Architecture",
  backend: "Backend",
  mobile: "Mobile",
  security: "Security",
  features: "Features",
  development: "Development",
};

// ── Mkdocs.yml navigation sync ────────────────────────────────
function syncMkdocsNav(action, docPath, navTitle) {
  if (!fs.existsSync(MKDOCS_PATH)) {
    return "mkdocs.yml not found — nav not updated.";
  }

  const lines = fs.readFileSync(MKDOCS_PATH, "utf-8").split("\n");
  const firstSegment = docPath.split("/")[0];
  const sectionKeyword = SECTION_MAP[firstSegment];

  if (action === "add" && navTitle && sectionKeyword) {
    // Find the section header line, then insert after the last entry in that section
    let sectionStart = -1;
    let insertIdx = -1;
    let sectionIndent = "";

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i];
      // Match section header like "  - 🧩 Features:" or "  - ⚙️ Backend:"
      if (line.includes(sectionKeyword + ":") && line.trimStart().startsWith("-")) {
        sectionStart = i;
        // Determine the indent of child entries (section indent + 6 spaces typically)
        const leadingSpaces = line.match(/^(\s*)/)[1];
        sectionIndent = leadingSpaces + "      ";
        continue;
      }

      if (sectionStart >= 0) {
        // We are inside the section — look for child entries
        const trimmed = line.trimStart();
        if (trimmed.startsWith("- ") && line.startsWith(sectionIndent)) {
          insertIdx = i; // track last child in this section
        } else if (trimmed.startsWith("- ") && !line.startsWith(sectionIndent) && trimmed !== "") {
          // We've exited the section (hit next top-level section)
          break;
        } else if (trimmed === "" || trimmed.startsWith("#")) {
          // Skip blanks and comments within a section
          continue;
        } else if (insertIdx >= 0 && !line.startsWith(sectionIndent) && trimmed !== "") {
          break;
        }
      }
    }

    if (insertIdx >= 0) {
      const newLine = `${sectionIndent}- ${navTitle}: ${docPath}`;
      lines.splice(insertIdx + 1, 0, newLine);
      fs.writeFileSync(MKDOCS_PATH, lines.join("\n"), "utf-8");
      return `Added nav entry '${navTitle}' in ${sectionKeyword} section.`;
    } else if (sectionStart >= 0) {
      // Section found but no children yet — insert right after section header
      const newLine = `${sectionIndent}- ${navTitle}: ${docPath}`;
      lines.splice(sectionStart + 1, 0, newLine);
      fs.writeFileSync(MKDOCS_PATH, lines.join("\n"), "utf-8");
      return `Added nav entry '${navTitle}' in ${sectionKeyword} section.`;
    }
    return `Section '${sectionKeyword}' not found in mkdocs.yml — nav not updated.`;
  }

  if (action === "remove") {
    let removed = false;
    const filtered = lines.filter((line) => {
      if (line.includes(docPath)) {
        removed = true;
        return false;
      }
      return true;
    });
    if (removed) {
      fs.writeFileSync(MKDOCS_PATH, filtered.join("\n"), "utf-8");
      return `Removed nav entry for '${docPath}'.`;
    }
    return `No nav entry found for '${docPath}'.`;
  }

  return "No nav action taken.";
}

const server = new Server(
  { name: "edrak-docs", version: "1.0.0" },
  { capabilities: { resources: {}, tools: {} } }
);

// ── Gather all .md files recursively ──────────────────────────
function getMarkdownFiles(dir, base = "") {
  let results = [];
  if (!fs.existsSync(dir)) return results;
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const rel = path.join(base, entry.name);
    if (entry.isDirectory()) {
      results = results.concat(getMarkdownFiles(path.join(dir, entry.name), rel));
    } else if (entry.name.endsWith(".md")) {
      results.push(rel);
    }
  }
  return results;
}

// ── List Resources ────────────────────────────────────────────
server.setRequestHandler(ListResourcesRequestSchema, async () => {
  const files = getMarkdownFiles(DOCS_DIR);
  return {
    resources: files.map((f) => ({
      uri: `edrak-docs:///${f}`,
      name: f.replace(/\.md$/, "").replace(/\//g, " › "),
      description: `Documentation: ${f}`,
      mimeType: "text/markdown",
    })),
  };
});

// ── Read Resource ─────────────────────────────────────────────
server.setRequestHandler(ReadResourceRequestSchema, async (request) => {
  const filePath = request.params.uri.replace("edrak-docs:///", "");
  const fullPath = path.join(DOCS_DIR, filePath);

  if (!fs.existsSync(fullPath)) {
    throw new Error(`Document not found: ${filePath}`);
  }

  return {
    contents: [
      {
        uri: request.params.uri,
        mimeType: "text/markdown",
        text: fs.readFileSync(fullPath, "utf-8"),
      },
    ],
  };
});

// ── List Tools ────────────────────────────────────────────────
server.setRequestHandler(ListToolsRequestSchema, async () => ({
  tools: [
    {
      name: "search_edrak_docs",
      description:
        "Search Edrak project documentation by keyword. Returns matching sections from all docs.",
      inputSchema: {
        type: "object",
        properties: {
          query: {
            type: "string",
            description: "Search term to find in the documentation",
          },
        },
        required: ["query"],
      },
    },
    {
      name: "read_edrak_doc",
      description:
        "Read a specific Edrak documentation page by path (e.g. 'backend/api-design.md', 'mobile/clean-architecture.md').",
      inputSchema: {
        type: "object",
        properties: {
          path: {
            type: "string",
            description:
              "Relative path to the doc file, e.g. 'backend/api-design.md'",
          },
        },
        required: ["path"],
      },
    },
    {
      name: "list_edrak_docs",
      description:
        "List all available Edrak documentation pages grouped by section.",
      inputSchema: { type: "object", properties: {} },
    },
    {
      name: "write_edrak_doc",
      description:
        "Create a new Edrak documentation page. Optionally adds it to the mkdocs.yml navigation. Errors if the file already exists.",
      inputSchema: {
        type: "object",
        properties: {
          path: {
            type: "string",
            description:
              "Relative path under docs/content/, e.g. 'features/notifications.md'",
          },
          content: {
            type: "string",
            description: "Full markdown content for the new page",
          },
          nav_title: {
            type: "string",
            description:
              "Display title for mkdocs.yml navigation, e.g. '🔔 Notifications'. If provided, auto-inserts into the matching nav section.",
          },
        },
        required: ["path", "content"],
      },
    },
    {
      name: "update_edrak_doc",
      description:
        "Update (overwrite) an existing Edrak documentation page. Errors if the file does not exist.",
      inputSchema: {
        type: "object",
        properties: {
          path: {
            type: "string",
            description:
              "Relative path to the existing doc, e.g. 'backend/api-design.md'",
          },
          content: {
            type: "string",
            description:
              "New full markdown content (replaces the entire file)",
          },
        },
        required: ["path", "content"],
      },
    },
    {
      name: "delete_edrak_doc",
      description:
        "Remove an Edrak documentation page and optionally remove its mkdocs.yml navigation entry.",
      inputSchema: {
        type: "object",
        properties: {
          path: {
            type: "string",
            description:
              "Relative path to the doc to delete, e.g. 'features/old-feature.md'",
          },
          remove_from_nav: {
            type: "boolean",
            description:
              "If true (default), also removes the entry from mkdocs.yml navigation.",
            default: true,
          },
        },
        required: ["path"],
      },
    },
  ],
}));

// ── Handle Tool Calls ─────────────────────────────────────────
server.setRequestHandler(CallToolRequestSchema, async (request) => {
  const { name, arguments: args } = request.params;

  if (name === "list_edrak_docs") {
    const files = getMarkdownFiles(DOCS_DIR);
    const grouped = {};
    for (const f of files) {
      const section = path.dirname(f) || "root";
      if (!grouped[section]) grouped[section] = [];
      grouped[section].push(f);
    }
    let text = "# Edrak Documentation\n\n";
    for (const [section, docs] of Object.entries(grouped).sort()) {
      text += `## ${section === "." ? "Home" : section}\n`;
      for (const d of docs) text += `- ${d}\n`;
      text += "\n";
    }
    return { content: [{ type: "text", text }] };
  }

  if (name === "read_edrak_doc") {
    const filePath = args.path;
    const fullPath = path.join(DOCS_DIR, filePath);
    if (!fs.existsSync(fullPath)) {
      return {
        content: [{ type: "text", text: `Document not found: ${filePath}` }],
        isError: true,
      };
    }
    return {
      content: [{ type: "text", text: fs.readFileSync(fullPath, "utf-8") }],
    };
  }

  if (name === "search_edrak_docs") {
    const queryRaw = (args.query || "").toLowerCase().trim();
    const queryWords = queryRaw.split(/\s+/).filter(Boolean);
    if (queryWords.length === 0) {
      return { content: [{ type: "text", text: "Empty search query." }] };
    }

    const files = getMarkdownFiles(DOCS_DIR);
    const results = [];

    for (const f of files) {
      const content = fs.readFileSync(path.join(DOCS_DIR, f), "utf-8");
      const lines = content.split("\n");
      const matches = [];

      for (let i = 0; i < lines.length; i++) {
        const lineLower = lines[i].toLowerCase();
        // Match if ANY query word appears on this line
        const wordHits = queryWords.filter((w) => lineLower.includes(w));
        if (wordHits.length > 0) {
          const start = Math.max(0, i - 1);
          const end = Math.min(lines.length - 1, i + 2);
          matches.push(
            lines
              .slice(start, end + 1)
              .map((l) => l.trim())
              .join("\n")
          );
        }
      }

      if (matches.length > 0) {
        // Score: files matching more unique query words rank higher
        const contentLower = content.toLowerCase();
        const uniqueWordHits = queryWords.filter((w) => contentLower.includes(w)).length;
        results.push({
          file: f,
          matchCount: matches.length,
          score: uniqueWordHits,
          excerpts: matches.slice(0, 3),
        });
      }
    }

    // Sort by score (most word hits first), then by match count
    results.sort((a, b) => b.score - a.score || b.matchCount - a.matchCount);

    if (results.length === 0) {
      return {
        content: [{ type: "text", text: `No results found for: "${queryRaw}"` }],
      };
    }

    let text = `# Search Results: "${queryRaw}"\n\n`;
    for (const r of results.slice(0, 10)) {
      text += `## 📄 ${r.file} (${r.matchCount} matches, ${r.score}/${queryWords.length} words)\n`;
      for (const e of r.excerpts) text += `\`\`\`\n${e}\n\`\`\`\n`;
      text += "\n";
    }
    return { content: [{ type: "text", text }] };
  }

  // ── write_edrak_doc ────────────────────────────────────────
  if (name === "write_edrak_doc") {
    const pathErr = validateDocPath(args.path);
    if (pathErr) return { content: [{ type: "text", text: pathErr }], isError: true };

    const fullPath = path.join(DOCS_DIR, args.path);
    if (fs.existsSync(fullPath)) {
      return {
        content: [{ type: "text", text: `File already exists: ${args.path}. Use 'update_edrak_doc' to overwrite.` }],
        isError: true,
      };
    }

    // Ensure parent directories exist
    fs.mkdirSync(path.dirname(fullPath), { recursive: true });
    fs.writeFileSync(fullPath, args.content, "utf-8");

    let navMsg = "";
    if (args.nav_title) {
      navMsg = " " + syncMkdocsNav("add", args.path, args.nav_title);
    }

    return {
      content: [{ type: "text", text: `✅ Created: ${args.path}${navMsg}` }],
    };
  }

  // ── update_edrak_doc ───────────────────────────────────────
  if (name === "update_edrak_doc") {
    const pathErr = validateDocPath(args.path);
    if (pathErr) return { content: [{ type: "text", text: pathErr }], isError: true };

    const fullPath = path.join(DOCS_DIR, args.path);
    if (!fs.existsSync(fullPath)) {
      return {
        content: [{ type: "text", text: `File not found: ${args.path}. Use 'write_edrak_doc' to create a new page.` }],
        isError: true,
      };
    }

    fs.writeFileSync(fullPath, args.content, "utf-8");
    return {
      content: [{ type: "text", text: `✅ Updated: ${args.path}` }],
    };
  }

  // ── delete_edrak_doc ───────────────────────────────────────
  if (name === "delete_edrak_doc") {
    const pathErr = validateDocPath(args.path);
    if (pathErr) return { content: [{ type: "text", text: pathErr }], isError: true };

    const fullPath = path.join(DOCS_DIR, args.path);
    if (!fs.existsSync(fullPath)) {
      return {
        content: [{ type: "text", text: `File not found: ${args.path}` }],
        isError: true,
      };
    }

    fs.unlinkSync(fullPath);

    let navMsg = "";
    const removeFromNav = args.remove_from_nav !== false; // default true
    if (removeFromNav) {
      navMsg = " " + syncMkdocsNav("remove", args.path);
    }

    return {
      content: [{ type: "text", text: `✅ Deleted: ${args.path}${navMsg}` }],
    };
  }

  return { content: [{ type: "text", text: `Unknown tool: ${name}` }], isError: true };
});

// ── Start Server ──────────────────────────────────────────────
async function main() {
  const transport = new StdioServerTransport();
  await server.connect(transport);
  console.error("Edrak Docs MCP Server running on stdio");
}

main().catch(console.error);
