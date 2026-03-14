---
description: Start working on the Edrak project — launch docs server and consult documentation
---

## Step 1: Start Documentation Preview Server

// turbo
```bash
cd /Users/eslamfaisal/Desktop/work/second_brain/docs && python3 -m mkdocs serve -f mkdocs.yml &
```

The documentation site will be available at `http://127.0.0.1:8000`.

## Step 2: Consult Documentation Before Any Work

Before writing any code or making changes, **always** consult the Edrak documentation using the `edrak-docs` MCP server tools:

1. Use `list_edrak_docs` to see all available documentation pages
2. Use `read_edrak_doc` to read the relevant doc page for the feature/area you're working on
3. Use `search_edrak_docs` to search for specific patterns, conventions, or architecture decisions

### Key docs to consult by area:

| Working on... | Read first |
|--------------|-----------|
| Backend API | `backend/api-design.md`, `backend/overview.md` |
| Authentication | `backend/authentication.md`, `features/auth.md` |
| Database changes | `backend/database-schema.md` |
| AI/Gemini | `backend/ai-pipeline.md`, `backend/ai-prompts.md` |
| Flutter feature | `mobile/clean-architecture.md`, `mobile/design-system.md` |
| Background service | `mobile/background-service.md`, `mobile/battery-optimization.md` |
| Any new feature | `architecture/folder-structure.md`, `development/coding-standards.md` |

## Step 3: Update Documentation After Work

After implementing any feature or making significant changes:

1. Update the relevant documentation page in `docs/content/`
2. Rebuild: `python3 -m mkdocs build --strict -f docs/mkdocs.yml`

## Step 4: Endpoint Auto-Documentation (Mandatory)

Every new backend endpoint **must** include these Swagger annotations for automatic Postman/Swagger sync:

- `@Tag(name = "Feature")` on the controller class
- `@Operation(summary = "...")` on each endpoint method
- `@Valid` on request DTOs with Bean Validation annotations
- Response wrapped in `ApiResponse<T>`

After adding endpoints, verify at:
- Swagger UI: `http://https://edrak-backend-386734725162.us-central1.run.app/swagger-ui.html`
- Re-import in Postman from: `https://edrak-backend-386734725162.us-central1.run.app/v3/api-docs`