---
description: Post-implementation audit — run automatically after completing any backend feature or fix
---

# Post-Task Gap Audit

> **This is NOT optional.** After completing ANY implementation task — backend, mobile (Android/iOS), or frontend — you MUST run this audit before marking the task as done. Do NOT wait for the user to ask.

---

## 1. Cross-Layer Contract Alignment

After any code change, check ALL affected layers:

### Backend ↔ Mobile/Frontend
- [ ] If a backend DTO changed, verify Android DTOs (`*Dto.kt`) match field names and types
- [ ] If a backend DTO changed, verify iOS DTOs (`*DTO.swift`) match field names and types
- [ ] If a backend DTO changed, verify Flutter DTOs (`*_model.dart`) match if applicable
- [ ] If an endpoint was added/removed/modified, verify mobile `ApiService`/`APIClient` matches
- [ ] If response shape changed, verify mobile mappers map all new/renamed fields

### Mobile ↔ Backend
- [ ] If mobile expects a new field, verify backend entity and DTO expose it
- [ ] If mobile calls a new endpoint, verify it exists in the controller
- [ ] If mobile sends a new request body, verify backend DTO accepts those field names

---

## 2. Documentation Alignment

### API Documentation (`docs/content/backend/api-design.md`)
- [ ] Endpoint registry table lists all implemented endpoints
- [ ] Every endpoint has a request/response example
- [ ] DTO field names in examples match actual code
- [ ] Auth column (Public/Required) matches `SecurityConfig.java`

### Feature Documentation (`docs/content/features/*.md`)
- [ ] Feature doc references all related endpoints
- [ ] Data flow diagrams reflect actual implementation
- [ ] Business rules table is current

### Database Schema (`docs/content/backend/database-schema.md`)
- [ ] Entity fields match SQL table definitions
- [ ] ER diagram relationships are accurate
- [ ] Enum values match code

---

## 3. Security Alignment

- [ ] `SecurityConfig.java` public/authenticated rules match docs
- [ ] No wildcard rules accidentally expose protected endpoints
- [ ] New endpoints have correct auth requirements

---

## 4. Swagger/OpenAPI

- [ ] All endpoints have `@Operation` / `@Tag` annotations
- [ ] Request/response schemas match actual DTOs
- [ ] Bearer auth scheme covers protected endpoints

---

## 5. Postman Collection

- [ ] Every endpoint has a request in `postman/Edrak-API.postman_collection.json`
- [ ] Test scripts assert correct status codes and response shape
- [ ] Auto-token scripts save ALL returned tokens
- [ ] Environment file has variables for new placeholders

---

## 6. Build Verification

// turbo
- [ ] Run `./gradlew classes` (backend) to verify compilation
- [ ] If mobile changed, verify it builds (Android: `./gradlew assembleDebug`, iOS: `xcodebuild`)

---

## How to Run

**Automated**: After every task, run through sections 1-6 for the layers you touched.
**Shortcut**: If only backend changed → focus on sections 2-5 + backend side of section 1.
If only mobile changed → focus on mobile side of section 1 + section 2 features.
