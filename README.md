# Expense Management API

A secure and production-oriented REST API for managing personal finances, built using **Spring Boot, Spring Security, JWT, Spring Data JPA, and MySQL**.

The application allows users to securely track expenses and income, organize transactions using categories, define monthly budgets, and view financial analytics. Each user's financial data is isolated using authenticated user ownership.

The project also includes automated integration testing, Swagger/OpenAPI documentation, Docker support, and a GitHub Actions CI pipeline.

---

## Features

### Authentication & Security
- User registration and login
- BCrypt password hashing
- JWT-based stateless authentication
- Spring Security integration
- Protected API endpoints
- User-level data isolation
- Cross-user resource access protection

### Expense Management
- Create, retrieve, update, and delete expenses
- Category-based expense organization
- Pagination and sorting
- Date-range filtering
- Amount-range filtering
- Category filtering
- Search by title and description

### Income Management
- Create, retrieve, update, and delete income records
- Income source tracking
- User-specific income history

### Category Management
- Create and manage expense categories
- Case-insensitive duplicate prevention
- Protection against deleting categories currently in use

### Budget Management
- Create monthly budgets for individual categories
- Prevent duplicate category budgets for the same month
- Automatically calculate actual spending
- Calculate remaining budget
- Calculate budget utilization percentage
- Detect exceeded budgets

### Financial Analytics
- Monthly financial summary
- Total income and expenses
- Net balance
- Savings rate
- Transaction count
- Expense breakdown by category
- Category spending percentages
- Monthly income vs expense trends
- Budget performance analytics

---

## Tech Stack

| Area | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4 |
| REST API | Spring Web MVC |
| Security | Spring Security |
| Authentication | JWT |
| Password Security | BCrypt |
| Persistence | Spring Data JPA / Hibernate |
| Database | MySQL 8 |
| Validation | Jakarta Bean Validation |
| API Documentation | Swagger / OpenAPI |
| Testing | JUnit, Spring Boot Test, MockMvc |
| Build Tool | Maven |
| Containerization | Docker, Docker Compose |
| CI | GitHub Actions |

---

## Architecture

The project follows a layered backend architecture:

```text
Client / Swagger / Postman
          |
          v
+-----------------------+
|   Spring Security     |
|   JWT Authentication  |
+-----------------------+
          |
          v
+-----------------------+
|      Controllers      |
+-----------------------+
          |
          v
+-----------------------+
|       Services        |
|   Business Logic      |
+-----------------------+
          |
          v
+-----------------------+
|     Repositories      |
|   Spring Data JPA     |
+-----------------------+
          |
          v
+-----------------------+
|        MySQL          |
+-----------------------+
```

The application separates HTTP handling, business logic, persistence, security, validation, and DTOs to keep the codebase maintainable and extensible.

---

## Authentication Flow

```text
Register
   |
   v
Password
   |
   v
BCrypt Hash
   |
   v
MySQL


Login
   |
   v
Verify Email + Password
   |
   v
Generate JWT
   |
   v
Return Token


Protected Request
   |
   v
Authorization: Bearer <JWT>
   |
   v
JwtAuthenticationFilter
   |
   v
Validate Token
   |
   v
SecurityContext
   |
   v
Protected API
```

The server remains stateless and does not maintain authentication sessions.

---

## User Data Isolation

Financial records belong to the authenticated user.

For example:

```text
User A
 ├── Expenses
 ├── Income
 └── Budgets

User B
 ├── Expenses
 ├── Income
 └── Budgets
```

The client does not supply a `userId` when creating financial records. The authenticated user's identity is extracted from the Spring Security context.

Resource queries combine the requested resource with the authenticated user's ID, preventing users from retrieving or modifying another user's financial information.

---

## Core Domain Model

```text
User
 |
 +----------------+
 |                |
 v                v
Expense         Income
 |
 v
Category
 ^
 |
Budget
 |
 +------> User
```

An expense belongs to both a user and a category.

An income record belongs to a user.

A budget belongs to a user and category and represents the allowed spending for a particular month and year.

---

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a user |
| POST | `/api/auth/login` | Login and receive JWT |

### Expenses

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/expenses` | Create expense |
| GET | `/api/expenses` | Retrieve expenses |
| GET | `/api/expenses/{id}` | Retrieve expense |
| PUT | `/api/expenses/{id}` | Update expense |
| DELETE | `/api/expenses/{id}` | Delete expense |

Expense listing supports pagination, sorting, searching, and filtering.

Example:

```text
GET /api/expenses?page=0&size=10&sortBy=expenseDate&direction=desc
```

Available filters include:

```text
category
startDate
endDate
minAmount
maxAmount
search
```

### Categories

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/categories` | Create category |
| GET | `/api/categories` | Retrieve categories |
| GET | `/api/categories/{id}` | Retrieve category |
| PUT | `/api/categories/{id}` | Update category |
| DELETE | `/api/categories/{id}` | Delete category |

### Income

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/incomes` | Create income |
| GET | `/api/incomes` | Retrieve user's income |
| GET | `/api/incomes/{id}` | Retrieve income |
| PUT | `/api/incomes/{id}` | Update income |
| DELETE | `/api/incomes/{id}` | Delete income |

### Budgets

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/budgets` | Create monthly budget |
| GET | `/api/budgets` | Retrieve budgets by month/year |
| GET | `/api/budgets/{id}` | Retrieve budget |
| PUT | `/api/budgets/{id}` | Update budget |
| DELETE | `/api/budgets/{id}` | Delete budget |

Example:

```text
GET /api/budgets?month=10&year=2026
```

### Analytics

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/analytics/summary` | Monthly financial summary |
| GET | `/api/analytics/expenses-by-category` | Category spending breakdown |
| GET | `/api/analytics/monthly-trend` | Annual monthly trend |
| GET | `/api/analytics/budget-performance` | Budget performance |

Example:

```text
GET /api/analytics/summary?month=10&year=2026
```

---

## Financial Analytics

The analytics layer performs database-level aggregation rather than loading every transaction into memory.

### Monthly Summary

```json
{
  "totalIncome": 60000.00,
  "totalExpenses": 15000.00,
  "netBalance": 45000.00,
  "savingsRate": 75.00,
  "transactionCount": 5,
  "month": 10,
  "year": 2026
}
```

Net balance:

```text
Net Balance = Total Income - Total Expenses
```

Savings rate:

```text
Savings Rate = (Net Balance / Total Income) × 100
```

### Budget Performance

Budget usage is calculated dynamically from actual expense records.

```text
Configured Budget
       |
       v
Find expenses for:
User + Category + Month
       |
       v
SUM(expense.amount)
       |
       v
Spent Amount
       |
       +----> Remaining Amount
       |
       +----> Usage Percentage
       |
       +----> WITHIN_BUDGET / EXCEEDED
```

This avoids storing duplicate spending totals that could become inconsistent with expense data.

---

## Validation & Error Handling

Request DTOs use Jakarta Bean Validation for input validation.

Examples include:

- Required fields
- Positive monetary amounts
- Valid email addresses
- Password length requirements
- Valid month ranges
- Date and amount filter validation
- Category existence validation

A global exception handler provides consistent API error responses.

Example:

```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "amount": "Amount must be greater than 0"
  },
  "timestamp": "2026-10-08T12:00:00"
}
```

---

## Swagger / OpenAPI

Interactive API documentation is provided using Swagger UI.

After starting the application:

```text
http://localhost:8080/swagger-ui.html
```

For protected endpoints:

1. Register or login.
2. Copy the generated JWT.
3. Click **Authorize** in Swagger.
4. Enter the JWT.
5. Call protected APIs directly from Swagger.

---

## Running Locally

### Prerequisites

Install:

- Java 21
- MySQL 8
- Git

Clone the repository:

```bash
git clone https://github.com/saumil1501/expense-management-api.git
cd expense-management-api
```

Create the database:

```sql
CREATE DATABASE expense_management;
```

Configure environment variables or your local application configuration:

```text
DB_URL=jdbc:mysql://localhost:3306/expense_management
DB_USERNAME=root
DB_PASSWORD=your_password
JWT_SECRET=your_secure_jwt_secret
JWT_EXPIRATION=86400000
```

Run:

### Windows

```powershell
.\mvnw.cmd spring-boot:run
```

### Linux/macOS

```bash
./mvnw spring-boot:run
```

The API runs at:

```text
http://localhost:8080
```

---

## Docker

The project includes Docker support for both the Spring Boot API and MySQL.

Start the complete application stack:

```bash
docker compose up --build
```

The services communicate internally through the Docker Compose network.

```text
Host
 |
 | :8080
 v
Spring Boot API
 |
 | mysql:3306
 v
MySQL Container
```

The MySQL container is exposed separately on port `3307` to avoid conflicts with a locally installed MySQL server.

Stop the containers:

```bash
docker compose down
```

---

## Testing

The project contains automated integration tests for authentication, security, user isolation, and expense functionality.

Run the complete test suite:

### Windows

```powershell
.\mvnw.cmd clean verify
```

### Linux/macOS

```bash
./mvnw clean verify
```

Tests run against a dedicated test database rather than the development database.

The test suite verifies scenarios including:

- Successful registration and login
- Duplicate registration
- Invalid authentication
- JWT authorization
- Invalid JWT rejection
- Expense CRUD
- Validation
- Category handling
- Filtering and pagination
- User-level resource isolation
- Cross-user access protection

---

## Continuous Integration

GitHub Actions automatically builds and tests the application for pushes and pull requests to `main`.

The CI pipeline:

```text
Push / Pull Request
        |
        v
GitHub Actions
        |
        +--> Start MySQL 8 service
        |
        +--> Configure Java 21
        |
        +--> Restore Maven dependencies
        |
        +--> Maven clean verify
        |
        v
   Build + Tests
```

This ensures changes are automatically validated before integration.

---

## Security Considerations

The application includes several security practices:

- Passwords are never stored as plain text.
- BCrypt is used for password hashing.
- Authentication is stateless.
- JWT signatures and expiration are validated.
- Financial resources are scoped to the authenticated user.
- Invalid credentials do not reveal whether an email exists.
- Cross-user resource requests do not disclose resource ownership.
- API requests are validated before business logic execution.
- Secrets can be supplied using environment variables.

---

## Project Structure

```text
src/main/java/com/example/expense_management_api/
│
├── config/
│   └── OpenApiConfig
│
├── controller/
│   ├── AnalyticsController
│   ├── AuthController
│   ├── BudgetController
│   ├── CategoryController
│   ├── ExpenseController
│   └── IncomeController
│
├── dto/
│   ├── request/response models
│   └── analytics response models
│
├── exception/
│   ├── custom exceptions
│   └── GlobalExceptionHandler
│
├── model/
│   ├── Budget
│   ├── Category
│   ├── Expense
│   ├── Income
│   └── User
│
├── repository/
│   ├── BudgetRepository
│   ├── CategoryRepository
│   ├── ExpenseRepository
│   ├── IncomeRepository
│   └── UserRepository
│
├── security/
│   ├── JwtAuthenticationFilter
│   ├── JwtService
│   ├── SecurityConfig
│   └── SecurityUtils
│
└── service/
    ├── AnalyticsService
    ├── BudgetService
    ├── CategoryService
    ├── ExpenseService
    ├── IncomeService
    └── UserService
```

---

## Engineering Highlights

This project demonstrates:

- RESTful API design
- Layered Spring Boot architecture
- JWT authentication and authorization
- Secure user-level resource ownership
- Relational database modeling
- JPA relationships and specifications
- Dynamic filtering and pagination
- Aggregate financial queries
- Transaction management
- DTO-based API boundaries
- Centralized exception handling
- Bean Validation
- Integration testing
- Dockerized development
- Automated CI using GitHub Actions
- OpenAPI documentation

---

## Future Enhancements

Potential extensions include:

- Recurring transactions
- CSV/Excel financial report export
- Refresh tokens
- Role-based administration
- Budget threshold notifications
- Database migrations using Flyway
- Frontend dashboard
- Cloud deployment

---

## Author

**Saumil Dixit**

B.Tech Computer Science & Engineering