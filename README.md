# Fintech Ledger Monolith

A production-inspired Java 21 Spring Boot backend for wallet payments, double-entry ledger accounting, fraud scoring, audit logging, JWT security, RBAC, idempotency, optimistic locking, and admin review workflows.

The application intentionally uses a modular monolith architecture focused on transactional consistency and simplicity.

## Why This Project Exists

Most portfolio payment projects stop at CRUD. This one focuses on the problems backend engineers actually discuss in fintech interviews:

- money movement correctness
- immutable ledger thinking
- idempotent payment APIs
- transaction boundaries
- concurrency protection
- fraud/risk workflow design
- auditability
- RBAC-secured operations
- practical monolith architecture

## Tech Stack

- Java 21
- Spring Boot 3
- Maven
- Spring Web
- Spring Security
- JWT authentication
- RBAC authorization
- Spring Data JPA / Hibernate
- Native SQL queries for reconciliation projections
- H2 file database for local persistence
- Swagger / OpenAPI
- Spring Application Events instead of Kafka
- ConcurrentHashMap-backed velocity cache instead of Redis
- Local AI-style fraud explanation service instead of external AI infrastructure

## Architecture

```text
com.portfolio.fintech
├── auth        JWT login, filter, user details service
├── user        onboarding and customer identity
├── wallet      wallet aggregate with optimistic locking
├── payment     idempotent transfer workflow
├── ledger      double-entry append-only ledger model
├── fraud       velocity scoring and local AI explanation boundary
├── audit       after-commit audit logging
├── admin       funding, fraud review, reconciliation APIs
├── events      Spring domain events
├── config      security, OpenAPI, seed data
└── common      shared errors, roles, responses
```

The application is a monolith by design. In a real fintech company, this is often the right starting point: strong transactional guarantees, simple local debugging, low operational overhead, and fewer distributed failure modes. Boundaries are still explicit so the fraud, ledger, audit, and payment modules could later be extracted if scale justified it.

## Key Engineering Decisions

### H2 Instead Of PostgreSQL

H2 is used in file mode so the app persists data between restarts without requiring a local database installation. The JDBC URL enables PostgreSQL compatibility mode to keep SQL habits realistic while staying lightweight.

### Spring Events Instead Of Kafka

Payment completion and fraud-review notifications are published as Spring events after the database transaction commits. This demonstrates event-driven thinking without forcing a local broker onto a laptop.

### ConcurrentHashMap Instead Of Redis

Fraud velocity checks use an in-memory cache. The cache is deliberately simple and local because the project is a monolith. The code comments explain where Redis would fit in a horizontally scaled deployment.

### Local AI Fraud Explanation Layer

The fraud explanation service is a local deterministic service that behaves like the application boundary you would place around Spring AI or another model provider. This keeps the project free of API keys and paid services while demonstrating how AI explanations belong behind a service abstraction.

### Double-Entry Ledger

Every completed transfer writes one debit and one credit ledger entry. Wallet balances are updated for fast reads, but the ledger is the source of audit truth. The reconciliation endpoint compares wallet balance against a native SQL projection from ledger entries.

### Idempotency

`POST /api/payments/transfers` requires an `Idempotency-Key` header. Retrying the same request with the same key returns the original transaction instead of moving money twice.

### Concurrency Protection

Wallets use JPA optimistic locking with `@Version`. Transfer processing loads wallets with `OPTIMISTIC_FORCE_INCREMENT`, so concurrent requests touching the same wallet cannot silently overwrite one another.

## Local Setup In IntelliJ

1. Open IntelliJ IDEA.
2. Choose **File > Open**.
3. Select the `fintech-ledger-monolith` folder.
4. Let IntelliJ import the Maven project.
5. Use JDK 21.
6. Run `FintechLedgerApplication`.

The app starts on:

```text
http://localhost:8081
```

## Maven Run

```bash
mvn spring-boot:run
```

Package:

```bash
mvn clean package
```

## Swagger

Open:

```text
http://localhost:8081/swagger-ui.html
```

Click **Authorize** and paste:

```text
<jwt-token>
```

Swagger UI adds the `Bearer` prefix automatically. Do not paste `Bearer <token>`
there, or the request will become `Bearer Bearer <token>`.

## H2 Console

Open:

```text
http://localhost:8081/h2-console
```

Use:

```text
JDBC URL: jdbc:h2:file:./data/fintech-ledger;MODE=PostgreSQL;DATABASE_TO_UPPER=false;AUTO_SERVER=TRUE
User: sa
Password: <empty>
```

If you previously ran the app with a different H2 naming setting and see both
`APP_USERS` and `app_users`, stop the application and delete the local `data/`
folder. It is only the local file database; the app will recreate clean demo
tables on the next startup.

## Demo Users

All seeded users use this password:

```text
Password@123
```

| Email | Role |
| --- | --- |
| alice@demo.local | CUSTOMER |
| bob@demo.local | CUSTOMER |
| analyst@demo.local | ANALYST |
| admin@demo.local | ADMIN |

## Postman Testing Flow

### 1. Login As Admin

`POST http://localhost:8081/api/auth/login`

```json
{
  "email": "admin@demo.local",
  "password": "Password@123"
}
```

Copy the token.

### 2. Login As Alice

`POST http://localhost:8081/api/auth/login`

```json
{
  "email": "alice@demo.local",
  "password": "Password@123"
}
```

### 3. Get Alice Wallet

`GET http://localhost:8081/api/wallets/me`

Use Alice token. Note Alice wallet id.

### 4. Get Bob Wallet

Login as Bob, call `GET /api/wallets/me`, and note Bob wallet id.

### 5. Fund Alice Wallet

`POST http://localhost:8081/api/admin/wallets/fund`

Use Admin token.

```json
{
  "walletId": 1,
  "amount": 5000.00
}
```

### 6. Transfer Money From Alice To Bob

`POST http://localhost:8081/api/payments/transfers`

Headers:

```text
Authorization: Bearer <alice-token>
Idempotency-Key: demo-transfer-001
```

Body:

```json
{
  "toWalletId": 2,
  "amount": 125.50,
  "memo": "Dinner settlement"
}
```

Send the same request again with the same idempotency key. The response should return the same transaction reference and should not debit Alice twice.

### 7. Trigger Fraud Review

Use a higher amount, such as:

```json
{
  "toWalletId": 2,
  "amount": 2500.00,
  "memo": "Large payout"
}
```

Depending on velocity and amount, the transaction may become `PENDING_REVIEW`.

### 8. Review Pending Payments

`GET http://localhost:8081/api/admin/reviews`

Use Admin or Analyst token.

### 9. Approve A Payment

`POST http://localhost:8081/api/admin/reviews/{reference}/approve`

### 10. Reconcile Ledger

`GET http://localhost:8081/api/admin/reconciliation`

This compares wallet balances with native SQL ledger projections.

