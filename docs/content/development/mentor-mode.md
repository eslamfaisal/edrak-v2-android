# 🧑‍🏫 Mentor Mode

> **Stop using AI as autocomplete. Start using it as a Senior Mentor available 24/7.**

Most engineers use tools like Cursor, Antigravity, or Claude Code at **30% capacity** — just copy-pasting solutions. This page documents how to unlock the remaining **70%** by transforming your AI tool into a strict, Socratic programming mentor.

---

## The Mindset Shift

| ❌ Autocomplete Mode | ✅ Mentor Mode |
|----------------------|----------------|
| "Fix this bug for me" | "I'm getting a NullPointerException here — what concept should I review?" |
| "Write me a caching layer" | "What are the trade-offs between write-through and write-behind caching?" |
| "Give me the code" | "Generate an assignment, then code-review my solution" |

**The goal is not speed — it's understanding.**

---

## The Mentor Rule

Add this rule to your AI workspace (`.gemini/settings.json`, Cursor rules, or Claude project instructions) when you want to **learn**, not just ship:

```markdown
# Role
You are an expert Senior Software Engineer and my dedicated programming mentor.

# Primary Goal
My goal is to deeply learn, understand concepts, and improve my problem-solving skills.
I am NOT looking for quick copy-paste solutions.

# Strict Rules for Interaction
1. NO DIRECT ANSWERS: Never give me the complete code solution or fix my bugs
   directly unless I explicitly use the override phrase: "GIVE_ME_THE_ANSWER".
2. THE SOCRATIC METHOD: When I ask how to do something or present an error, guide
   me. Ask leading questions, give me hints, or point me to the specific
   concept/documentation I need to review. Make me think.
3. CODE REVIEWS: When I write code, review it like a strict but helpful Senior.
   Point out anti-patterns, bad naming conventions, or performance bottlenecks
   (Time/Space complexity). Explain the *WHY* behind your feedback, not just
   the *HOW*.
4. EXPLANATIONS: If I ask you to explain a concept from a book or documentation,
   use real-world, practical analogies. Relate it to actual production systems
   (e.g., e-commerce, banking).
5. WHAT-IF SCENARIOS: Always push me further. After I successfully implement a
   concept, ask me how the system would behave under heavy load, or what edge
   cases I might have missed.

# Tone
Professional, encouraging, but strict about not doing the work for me.
```

!!! tip "Override Escape Hatch"
    When you're truly stuck and need the direct answer, use the phrase **`GIVE_ME_THE_ANSWER`** to temporarily bypass the Socratic rules.

---

## Phase-Based Learning Workflow

This workflow works for studying **any new technology, protocol, or framework** (e.g., A2A, gRPC, CRDT, Raft Consensus).

### Step 1 — Generate the Learning Roadmap

Ask the AI to break the topic into phases:

```
Create a folder named "phases" and put inside it each topic I need to learn
as headline markdown files, ordered from fundamentals to advanced.
```

The AI generates a structured set of files like:

```
phases/
├── 01-core-concepts.md
├── 02-protocol-design.md
├── 03-message-format.md
├── 04-agent-discovery.md
├── 05-security-model.md
└── 06-production-deployment.md
```

### Step 2 — Study Phase by Phase

Open a phase file and study it interactively:

```
Let's start with Phase 1. Explain each concept with real-world analogies.
```

### Step 3 — Get Assignments

After studying a phase:

```
Generate an assignment for Phase 1 that tests my understanding.
```

### Step 4 — Submit for Code Review

Write your solution, then:

```
Review my code like a strict Senior Engineer. Point out anti-patterns,
naming issues, and complexity problems.
```

### Step 5 — Level Up to Production Grade

After passing the review:

```
In a production-grade system, how would you re-engineer this code
for high availability and scale?
```

### Step 6 — Move to Next Phase

```
Let's move to Phase 2.
```

Repeat until all phases are complete.

---

## Learning from Books

When studying a technical book (e.g., an LLM / Deep Learning textbook):

### Step 1 — Extract Chapter Content

Use [Datalab](https://github.com/VikParuchuri/datalab) or any PDF-to-Markdown tool to extract a chapter as Markdown.

### Step 2 — Generate Assessment

Feed the chapter to the AI:

```
Here is Chapter 3 of [Book Name]. Generate an assessment for me
covering the main ideas, key formulas, and critical concepts.
```

### Step 3 — Deep Comparisons

```
Compare the Attention mechanism described in this chapter
with the implementation in LLaMA-7B. What changed and why?
```

### Step 4 — Hands-On Implementation

```
How can I write code to tokenize data using BPE, customized
for this specific architecture?
```

### Step 5 — Cross-Reference Latest Research

```
How did DeepSeek solve [specific gap] in their latest paper?
What can I apply from their approach?
```

---

## Learning from Repositories

When studying an open-source repo or reference implementation:

### Step 1 — Clone & Analyze

```
Clone this repo: [URL]
Read the documentation and give me a high-level architecture overview.
```

### Step 2 — Guided Exploration

```
Walk me through the request lifecycle in this codebase.
Don't explain the code — ask me questions about what I think each
layer does, and correct me.
```

### Step 3 — Reproduce the Patterns

```
Generate an assignment: I need to build a simplified version
of the [specific component] using the same patterns.
```

---

## When to Use Each Mode

| Scenario | Mode |
|----------|------|
| Shipping a feature under deadline | Normal mode (autocomplete) |
| Studying a new technology or framework | **Mentor mode** |
| Reading a technical book | **Mentor mode** |
| Understanding an open-source codebase | **Mentor mode** |
| Preparing for system design interviews | **Mentor mode** |
| Debugging a production issue at 2 AM | Normal mode 😅 |

!!! note "Switching Modes"
    You can activate Mentor Mode using the `/mentor` workflow command, or by adding the Mentor Rule to your workspace settings.

---

## Key Principles

1. **You learn by struggling, not by reading answers** — The discomfort of not knowing is where growth happens.
2. **Assignments > Tutorials** — Doing beats watching. Always ask for assignments.
3. **Code Reviews build muscle memory** — Having your code torn apart by a "Senior" builds real engineering instincts.
4. **Analogies cement understanding** — If you can't explain it with a real-world analogy, you don't truly understand it.
5. **What-If scenarios prepare you for production** — Edge cases, load, and failure modes are what separate juniors from seniors.
