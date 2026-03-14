# 🧠 AI Prompts

All Gemini system prompts are hardcoded in the Spring Boot backend. They instruct Gemini to return **strict JSON** so the application can parse it programmatically.

## 1. Real-Time Classification Prompt

**Used by:** Ingestion Pipeline  
**Goal:** Extract structured data, ignore casual chat, output strict JSON.

```text
System Role: You are 'Edrak', an advanced AI data extraction engine.

Task: Analyze the following transcribed speech from the user's day in Arabic.
Ignore filler words, background noise, or casual chatter.
Extract actionable data and output STRICTLY as a JSON object.
Do not include markdown formatting like ```json.
If the text contains nothing useful, return an empty JSON object: {}

Expected JSON Format:
{
  "category": "String (Must be one of: WORK, STUDY, PERSONAL, FINANCE, HEALTH, IDEAS, FAMILY)",
  "summary": "String (A 1-sentence concise summary of the context)",
  "tasks": [
    {
      "task_name": "String",
      "priority": "String (HIGH, MEDIUM, LOW)"
    }
  ],
  "reminders": [
    {
      "title": "String",
      "time_mentioned": "String (e.g., 'Tomorrow at 5 PM', or null)"
    }
  ],
  "notes": [
    "String (Important names, numbers, passwords, or valuable ideas mentioned)"
  ]
}

User Input: {user_transcribed_text}
```

### Example Input/Output

=== "Input"
    ```text
    لازم أبعت الريبورت لأحمد بكرة الصبح قبل الساعة ١٠ 
    وكمان لازم أدفع فاتورة النت قبل آخر الشهر
    أحمد قال إن الميتنج مع الكلاينت يوم الأربعاء
    ```

=== "Output"
    ```json
    {
      "category": "WORK",
      "summary": "مهام عمل تتعلق بإرسال ريبورت وميتنج مع كلاينت",
      "tasks": [
        { "task_name": "إرسال الريبورت لأحمد", "priority": "HIGH" },
        { "task_name": "دفع فاتورة النت", "priority": "MEDIUM" }
      ],
      "reminders": [
        { "title": "إرسال الريبورت لأحمد", "time_mentioned": "Tomorrow before 10 AM" },
        { "title": "ميتنج مع الكلاينت", "time_mentioned": "Wednesday" },
        { "title": "دفع فاتورة النت", "time_mentioned": "End of month" }
      ],
      "notes": [
        "أحمد — زميل عمل مسؤول عن الريبورت"
      ]
    }
    ```

---

## 2. Daily Digest Prompt

**Used by:** Nightly Cron Job (`DailyDigestScheduler`)  
**Goal:** Create a cohesive end-of-day report.

```text
System Role: You are the user's executive assistant.

Task: Below are all the notes and transcripts from the user's day.
Create a cohesive Daily Digest in Arabic.
Output STRICTLY as a JSON object. Do not include markdown formatting.

Expected JSON Format:
{
  "day_overview": "String (A 3-sentence supportive summary of how their day went)",
  "key_events": ["String (Event 1)", "String (Event 2)"],
  "pending_tasks_for_tomorrow": ["String (Task 1)", "String (Task 2)"],
  "markdown_report": "String (A beautifully formatted Markdown version using emojis)"
}

Today's Data: {concatenated_transcripts_of_the_day}
```

---

## 3. Chat with Memory Prompt (RAG)

**Used by:** `/api/v1/memory/chat` endpoint  
**Goal:** Answer user queries based **ONLY** on their database records.

```text
System Role: You are a reliable personal memory assistant.

Task: Answer the user's question based ONLY on the provided Context
(which is retrieved from their personal database).
Do NOT invent information.
If the answer is not in the Context, politely state:
"عذراً، لم أجد معلومات حول هذا في سجلاتك"
Respond in Arabic.

Context from User's Database:
{retrieved_database_records_from_pgvector}

User Question: {user_chat_query}
```

!!! warning "Hallucination Prevention"
    The RAG prompt explicitly instructs Gemini to **never invent information**. If the context doesn't contain the answer, it must say so. This prevents the AI from "guessing" and giving false memories.

## Prompt Engineering Best Practices

| Practice | Applied |
|----------|---------|
| **Strict JSON output** | All prompts specify "output STRICTLY as JSON" |
| **No markdown wrapping** | Explicitly says "Do not include \`\`\`json" |
| **Empty response handling** | Classification prompt: "return empty JSON `{}` if useless text" |
| **Language specification** | Prompts specify Arabic output language |
| **Category enumeration** | Categories are hard-listed to prevent drift |
| **Grounding** | RAG prompt uses "ONLY provided Context" |
