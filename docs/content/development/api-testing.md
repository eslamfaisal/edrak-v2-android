# 🧪 API Testing & Documentation

## Swagger / OpenAPI (Auto-Generated)

Every endpoint is **automatically documented** by SpringDoc OpenAPI. No extra work needed — just annotate controllers with `@Tag`, `@Operation`, and use `@Valid` DTOs.

### Access URLs

| URL | Description |
|-----|-------------|
| `https://edrak-backend-386734725162.us-central1.run.app/swagger-ui.html` | Interactive Swagger UI |
| `https://edrak-backend-386734725162.us-central1.run.app/v3/api-docs` | OpenAPI 3.0 JSON spec |
| `https://edrak-backend-386734725162.us-central1.run.app/v3/api-docs.yaml` | OpenAPI 3.0 YAML spec |

!!! tip "JWT in Swagger"
    1. Call `/api/v1/auth/login` to get an access token
    2. Click the 🔒 **Authorize** button in Swagger UI
    3. Paste: `eyJhbG...` (just the token, no "Bearer" prefix)
    4. All subsequent requests will include the token

## Postman (Auto-Import from OpenAPI)

### Setup (One-Time)

1. Open Postman → **Import** → paste URL:
   ```
   https://edrak-backend-386734725162.us-central1.run.app/v3/api-docs
   ```
2. Postman auto-generates all endpoints as a collection
3. Set up a Postman **Environment** with variables:
   - `baseUrl` = `https://edrak-backend-386734725162.us-central1.run.app`
   - `accessToken` = _(empty, auto-filled by login script)_

### Auto-Token Script

Add this to the **Login** request's **Tests** tab in Postman:

```javascript
if (pm.response.code === 200) {
    var json = pm.response.json();
    pm.environment.set("accessToken", json.data.accessToken);
    pm.environment.set("refreshToken", json.data.refreshToken);
}
```

Then set the collection's **Authorization** → Type: **Bearer Token** → Token: `{{accessToken}}`.

### Keeping Postman in Sync

When new endpoints are added:

1. Re-import from `https://edrak-backend-386734725162.us-central1.run.app/v3/api-docs`
2. Choose **Replace existing collection** to merge changes

!!! info "Automatic Documentation Rule"
    Every new endpoint **must** include:

    - `@Tag(name = "Feature")` on the controller
    - `@Operation(summary = "...")` on each method
    - `@Valid` request DTOs with Bean Validation annotations
    - Response wrapped in `ApiResponse<T>`

    This ensures Swagger and Postman always have complete, accurate docs.
