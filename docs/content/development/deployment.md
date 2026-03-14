# 🚀 Deployment

## Production: Google Cloud Run

### Service Details

| Property | Value |
|----------|-------|
| **Service URL** | `https://edrak-backend-386734725162.us-central1.run.app` |
| **Service Name** | `edrak-backend` |
| **Region** | `us-central1` |
| **GCP Project** | `edrak-second-brain-ai` |
| **Memory** | 1 GiB |
| **Timeout** | 300s |
| **Port** | 8080 |
| **Auth** | Unauthenticated (public — API handles its own JWT auth) |

### Cloud SQL (PostgreSQL)

| Property | Value |
|----------|-------|
| **Instance** | `edrak-db` |
| **Version** | PostgreSQL 15 |
| **Tier** | `db-f1-micro` |
| **Region** | `us-central1` |
| **IP** | `34.63.3.88` |
| **Database** | `edrak_db` |
| **Connection** | Unix socket via Cloud SQL Auth Proxy |

### Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `SPRING_PROFILES_ACTIVE` | Set to `prod` | ✅ |
| `DB_URL` | Cloud SQL JDBC URL with socket factory | ✅ |
| `DB_USERNAME` | Database user | ✅ |
| `DB_PASSWORD` | Database password | ✅ |
| `JWT_SECRET` | HMAC signing key for backend JWTs | ✅ |
| `GOOGLE_GENAI_API_KEY` | Gemini API key for AI features | ✅ |
| `FIRESTORE_PROVIDER` | `firebase` or `console` | ✅ |

### Deploy Commands

```bash
# Full deployment (from backend directory)
gcloud run deploy edrak-backend \
  --source . \
  --region us-central1 \
  --platform managed \
  --port 8080 \
  --memory 1Gi \
  --timeout 300 \
  --allow-unauthenticated \
  --add-cloudsql-instances edrak-second-brain-ai:us-central1:edrak-db \
  --project edrak-second-brain-ai

# Verify deployment
curl https://edrak-backend-386734725162.us-central1.run.app/actuator/health
# Expected: {"status":"UP"}
```

### Firebase on Cloud Run

Firebase Admin SDK uses **Application Default Credentials (ADC)** on Cloud Run — no explicit credentials file needed. The Cloud Run service account inherits the project's Firebase permissions.

For local development, set `app.firebase.credentials-path` in `application-local.yml` to your service account JSON.

### Cloud SQL Connection

Connection uses the **Cloud SQL Auth Proxy** injected by `--add-cloudsql-instances`:

```
DB_URL=jdbc:postgresql:///edrak_db?cloudSqlInstance=edrak-second-brain-ai:us-central1:edrak-db&socketFactory=com.google.cloud.sql.postgres.SocketFactory
```

Requires `com.google.cloud.sql:postgres-socket-factory` dependency in `build.gradle.kts`.

## Local Development

See [Setup Guide](setup.md) for local development instructions.
