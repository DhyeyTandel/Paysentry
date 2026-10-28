# PaySentry

Minimal payment fraud scoring API. Call `POST /v1/score` with a payment attempt and receive `APPROVE`, `REVIEW`, or `DECLINE`, a risk score from 0 to 1, and reason codes from four Redis-backed rules: `VELOCITY_1H`, `HIGH_AMOUNT`, `GEO_MISMATCH`, and `NEW_DEVICE`. Requests require an `Idempotency-Key` header; repeats return the stored response without updating Redis. Every scored request is append-only audited in PostgreSQL.

## Run

```sh
docker compose up --build
```

The API listens on port 8080. Compose starts PostgreSQL 16, Redis 7, and the app (multi-stage Docker build). Flyway runs on startup.

## Example

```sh
curl -s http://localhost:8080/v1/score \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: demo-001' \
  -d '{
    "cardId": "card-001",
    "deviceId": "device-001",
    "merchantId": "merchant-001",
    "amount": 120.00,
    "country": "US",
    "timestamp": "2024-06-01T12:00:00Z"
  }'
```

Example response on a fresh card and device:

```json
{
  "decision": "APPROVE",
  "riskScore": 0.15,
  "reasonCodes": ["NEW_DEVICE"]
}
```
