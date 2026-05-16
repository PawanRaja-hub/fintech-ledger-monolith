# Fintech Ledger Monolith

Fintech Ledger is a small full-stack banking-style demo application. It lets you open a browser, sign in as demo users, fund wallets, transfer money, review risky payments, and check that wallet balances match the ledger.

The project is built as one Spring Boot application: the backend APIs, database access, security, and frontend webpage all run together.

![Fintech Ledger app preview](docs/screenshots/ledger-console-preview.svg)

## What You Can Do

- Sign in from the webpage as Admin, Analyst, Alice, or Bob.
- See wallet balances and wallet IDs.
- Fund a customer wallet as Admin.
- Transfer money from one customer wallet to another.
- Send large transfers into a fraud-review queue.
- Approve or reject pending payments as Admin or Analyst.
- Run reconciliation to compare wallet balances with ledger entries.
- Open the H2 database console and see the stored data.
- Run backend tests and browser automation tests.

## Tech Stack

- Java 21
- Spring Boot 3
- Maven
- Spring Security with JWT login
- Spring Data JPA / Hibernate
- H2 file database
- HTML, CSS, and JavaScript frontend served by Spring Boot
- Swagger / OpenAPI
- JUnit 5 and MockMvc for fast backend tests
- Selenium, Cucumber, and TestNG for automation testing

## Requirements

Install these before running the project:

- Java 21
- Maven
- Google Chrome, for Selenium UI automation

No separate frontend setup is needed. There is no `npm install` step.

## Start The Application

From the project folder, run:

```bash
mvn spring-boot:run
```

Then open:

```text
http://localhost:8081
```

Yes, this is now a full-stack application. You interact with the frontend webpage, and the frontend calls the backend APIs. The backend saves data in the local H2 database, so wallet funding, transfers, reviews, ledger entries, and audit logs are stored there.

## Demo Logins

Every demo user has the same password:

```text
Password@123
```

| Person | Email | Role | What they can do |
| --- | --- | --- | --- |
| Alice | `alice@demo.local` | CUSTOMER | View wallet and transfer money |
| Bob | `bob@demo.local` | CUSTOMER | View wallet and receive transfers |
| Fraud Analyst | `analyst@demo.local` | ANALYST | Review risky payments and run reconciliation |
| Platform Admin | `admin@demo.local` | ADMIN | Fund wallets, review payments, run reconciliation |

The webpage has a demo-user dropdown, so you do not need to type these emails manually in the UI.

## Simple Manual Testing Flow

1. Open `http://localhost:8081`.
2. Select `Platform Admin` and click `Sign in`.
3. Click the `Admin` tab.
4. Pick Alice's wallet and fund it with an amount like `5000.00`.
5. Select `Alice Customer` from the login dropdown and click `Sign in`.
6. In the `Transfer` tab, choose Bob's wallet.
7. Enter an amount like `125.50`.
8. Click `Submit transfer`.
9. Select `Platform Admin` again and sign in.
10. Click `Reconcile` and run the check.

If everything is healthy, the reconciliation rows should show `MATCHED`.

## View The H2 Database

While the app is running, open:

```text
http://localhost:8081/h2-console
```

Use these values:

```text
JDBC URL: jdbc:h2:file:./data/fintech-ledger;MODE=PostgreSQL;DATABASE_TO_UPPER=false;AUTO_SERVER=TRUE
User: sa
Password: leave empty
```

Useful tables to inspect:

- `app_users` contains demo users.
- `wallets` contains current wallet balances.
- `payment_transactions` contains transfers and review status.
- `ledger_entries` contains debit and credit records.
- `audit_logs` contains business activity history.

If you want a clean database, stop the app and delete the local `data` folder. The app recreates demo users and wallets when it starts again.

## Automation Testing

There are three levels of testing in this project.

### Fast Backend Tests

Run:

```bash
mvn test
```

This runs JUnit and MockMvc tests without opening a browser.

### Backend Cucumber + TestNG Automation

Run:

```bash
mvn verify -Pbackend-automation
```

This starts a temporary Spring Boot app on `http://127.0.0.1:18081`, runs Cucumber scenarios through TestNG, calls the real backend APIs, and writes reports to:

```text
target/cucumber-reports/backend.html
```

### Selenium UI + Cucumber + TestNG Automation

Run:

```bash
mvn verify -Pui-automation
```

This starts a temporary Spring Boot app on `http://127.0.0.1:18081`, opens Chrome through Selenium, uses the real webpage, and writes reports to:

```text
target/cucumber-reports/ui.html
```

Screenshots from Selenium runs are saved to:

```text
target/selenium-screenshots
```

By default Selenium runs in headless mode. To watch Chrome while the tests run:

```bash
mvn verify -Pui-automation -Dselenium.headless=false
```

To run both backend and UI Cucumber automation:

```bash
mvn verify -Pautomation
```

The automation profiles intentionally do not use the normal app port `8081`.
That means you can keep the manual application open at `http://localhost:8081`
while tests run on `18081`.

Automation also uses its own H2 database file:

```text
./data/fintech-ledger-automation
```

The regular manual app still uses:

```text
./data/fintech-ledger
```

If port `18081` is busy on your machine, choose another test port:

```bash
mvn verify -Pautomation -Dautomation.server.port=18082
```

## Cucumber Feature Files

The readable automation scenarios are here:

```text
src/test/resources/features/backend/ledger_api.feature
src/test/resources/features/ui/ledger_console.feature
```

These files describe the behavior in plain English before the Java step definitions automate it.

## Swagger API Page

Open:

```text
http://localhost:8081/swagger-ui.html
```

You can test APIs from Swagger. For secured APIs:

1. Call `POST /api/auth/login`.
2. Copy the token from the response.
3. Click `Authorize`.
4. Paste only the token value.

Swagger adds `Bearer` automatically.

## Important API Endpoints

| Action | Endpoint |
| --- | --- |
| Login | `POST /api/auth/login` |
| My wallet | `GET /api/wallets/me` |
| Transfer recipients | `GET /api/wallets/recipients` |
| Fund wallet | `POST /api/admin/wallets/fund` |
| Transfer money | `POST /api/payments/transfers` |
| Pending reviews | `GET /api/admin/reviews` |
| Approve payment | `POST /api/admin/reviews/{reference}/approve` |
| Reject payment | `POST /api/admin/reviews/{reference}/reject` |
| Reconciliation | `GET /api/admin/reconciliation` |

## Why This Project Is Useful

This is not just a CRUD demo. It shows fintech backend ideas in a simple full-stack package:

- Wallet balance updates
- Double-entry ledger records
- Idempotent transfer requests
- Fraud scoring and manual review
- Role-based access control
- Audit logging
- Reconciliation between wallet balances and ledger balances
- Browser automation using Selenium, Cucumber, and TestNG
