
A desktop Point-of-Sale application built with **Spring Boot 3** and **JavaFX 17**, backed by a local **SQLite** database. Designed for single-register retail environments — no server or internet connection required.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Module Structure](#module-structure)
- [Prerequisites](#prerequisites)
- [Build & Run](#build--run)
- [Default Credentials](#default-credentials)
- [Configuration](#configuration)
- [Role Permissions](#role-permissions)
- [Project Structure](#project-structure)
- [Running Tests](#running-tests)

---

## Features

### Cashier Screen
- Scan products by **barcode SKU** or search by name/SKU fragment
- Live **cart** with quantity management and real-time totals
- **Percentage discounts** applied per transaction
- **Cash** and **Card** payment methods
- Change calculation for cash payments
- **Operator display** showing the logged-in cashier's name

<img width="1916" height="994" alt="image" src="https://github.com/user-attachments/assets/5809ecb1-687f-4f32-b30a-6a59359e4c28" />

### Checkout & Receipt
- Stock is deducted automatically on checkout
- Formatted receipt printed to a configured thermal printer
- Receipt includes: shop header, itemised list, subtotal, discount, total, payment details

<img width="655" height="734" alt="image" src="https://github.com/user-attachments/assets/6bbfff53-9d6b-4f8c-bd46-1a0563451f67" />

<img width="669" height="757" alt="image" src="https://github.com/user-attachments/assets/63c657b8-5d58-40f0-8f3d-94f96f45f7c9" />

### Product Manager *(Admin / Manager)*
- Full **CRUD** for products — name, price, stock, description
- **Auto-generated SKU** (`P-XXXXXX` format, guaranteed unique)
- **CODE128 barcode label** preview in-app (name + SKU)
- **Print barcode label** directly to any system printer
- Search by name or SKU

<img width="1919" height="986" alt="image" src="https://github.com/user-attachments/assets/508dd4e5-33ab-450e-8f8c-e8c49d36b288" />

### User Management *(Admin only)*
- Create, edit, and soft-delete user accounts
- Roles: **Admin**, **Manager**, **Cashier**, **Inventory Clerk**
- Password update with confirmation
- **Staff badge barcode** (CODE128 of the employee's unique PIN)
- Print staff badges directly from the app

<img width="1916" height="988" alt="image" src="https://github.com/user-attachments/assets/51fd3dbb-9052-4d0e-9eae-fad104394316" />

### Reports & Analytics *(Admin / Manager)*
- Date-range picker (defaults to current month)
- **5 KPI cards**: Revenue · Transactions · Avg Order · Items Sold · Discounts Given
- **Transaction history** with Cashier, Subtotal, Discount, Total, Payment method columns
- **Line-item drill-down**: click any transaction to see every product, quantity, unit price, and subtotal
- **Top 10 products by revenue** (scoped to the selected date range)
- **Payment breakdown**: Cash vs Card — count and total for each

  <img width="1919" height="986" alt="image" src="https://github.com/user-attachments/assets/8bfe8288-d72a-44d1-844d-0d41511625f3" />
  
  <img width="1894" height="878" alt="image" src="https://github.com/user-attachments/assets/38d86952-9857-4c9b-b3c4-ee0c653951f0" />

### Authentication
- Username + BCrypt password login
- **Barcode PIN login** — scan the staff badge to log in instantly
- Account lockout after **5 failed attempts** (unlocks after 15 minutes)
- Session cleared on logout
  
  <img width="617" height="795" alt="image" src="https://github.com/user-attachments/assets/9c361a80-353f-4c11-b822-0eeefb587dcd" />

### Navigation
- Smooth **fade transitions** (120 ms out → 180 ms in) between all screens
- All main screens open **maximised**; login is a fixed centred window (500 × 640)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.11 |
| UI | JavaFX 17.0.6 + FXML |
| DI in FXML | `FXMLLoader.setControllerFactory(springContext::getBean)` |
| Database | SQLite 3.45 (file: `~/.cashier-pos/cashier.db`) |
| ORM | Spring Data JPA + Hibernate Community SQLite Dialect 6.4 |
| Security | Spring Security 6 (BCrypt, strength 12) |
| Barcode | ZXing 3.5.3 (CODE128, EAN-13) |
| Printing | `javax.print` API (receipts), JavaFX `PrinterJob` (labels/badges) |
| Build | Maven 3 (multi-module) |
| Testing | JUnit 5, Mockito 5.11, Spring Boot Starter Test, H2 (in-memory) |
| Utilities | Lombok 1.18, SLF4J + Logback |

---

## Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                         pos-app                              │
│  Spring Boot entry point · DataInitializer · app config      │
└────────────────────────────┬─────────────────────────────────┘
                             │ depends on all modules
         ┌───────────────────┼─────────────────────┐
         ▼                   ▼                     ▼
  ┌─────────────┐   ┌──────────────────┐   ┌─────────────┐
  │   pos-ui    │   │  pos-services    │   │ pos-reports │
  │  JavaFX     │──▶│  Business Logic  │◀──│  ZXing      │
  │  FXML       │   │  Service impls   │   │  Barcode    │
  │  Controllers│   │  DiscountStrat.  │   │  engine     │
  └─────────────┘   └────────┬─────────┘   └─────────────┘
                             │
                    ┌────────▼─────────┐
                    │    pos-data      │
                    │  JPA Repos       │
                    │  SQLite via JDBC │
                    └────────┬─────────┘
                             │
                    ┌────────▼─────────┐
                    │    pos-core      │
                    │  Entities        │
                    │  Enums           │
                    │  Discount strat. │
                    │  Exceptions      │
                    └──────────────────┘
```

### Design Principles Applied

- **Single Responsibility** — each service class has one reason to change; controllers delegate all business logic to services
- **Open/Closed** — discount calculation is open for extension via the `DiscountStrategy` interface (`NoDiscount`, `PercentageDiscount`) without modifying `CartService`
- **Liskov Substitution** — all service interfaces are substitutable; `CashierProductService` and `CashierReportService` are fully substitutable for `ProductService` / `ReportService`
- **Interface Segregation** — 7 focused service interfaces; controllers inject only what they need
- **Dependency Inversion** — controllers and inter-service dependencies all inject interfaces, never concrete classes

---

## Module Structure

```
pos-parent/                 ← Root POM, dependency management
├── pos-core/               ← Domain layer: entities, enums, exceptions, discount strategies
├── pos-data/               ← Persistence layer: Spring Data JPA repositories
├── pos-services/           ← Business layer: service interfaces + implementations
│   └── impl/               ← Concrete implementations (AuthServiceImpl, CartServiceImpl, …)
├── pos-reports/            ← Reporting infrastructure: ZXing barcode engine
├── pos-ui/                 ← Presentation layer: JavaFX controllers, FXML, utilities
└── pos-app/                ← Application entry point: Spring Boot main class, config, seeding
```

### Module Dependencies

```
pos-core   ←─── pos-data   ←─── pos-services   ←─── pos-ui   ←─── pos-app
                                      ↑                              ↑
                           pos-reports ─────────────────────────────┘
```

---

## Prerequisites

| Requirement | Version |
|---|---|
| JDK | 17 or later |
| Maven | 3.8 or later |
| OS | Windows 10/11, macOS 12+, or Linux (with JavaFX runtime) |

> The SQLite database file is created automatically at `~/.cashier-pos/cashier.db` on first launch.

---

## Build & Run

```bash
# Clone the repository
git clone <repo-url>
cd Cashier

# Build all modules (skipping tests for a quick first build)
mvn clean package -DskipTests

# Run the application
java -jar pos-app/target/pos-app-*.jar
```

Or run directly with Maven:

```bash
mvn -pl pos-app spring-boot:run
```

### Development (IDE)

1. Import the root `pom.xml` as a Maven project
2. Set the JDK to **Java 17**
3. Run `org.example.cashier.app.Launcher` as the main class
4. If using IntelliJ IDEA, add `--add-opens=java.base/java.lang=ALL-UNNAMED` to VM options

---

## Default Credentials

These accounts are seeded automatically on the **first launch** (when the database is empty):

| Username | Password | Role |
|---|---|---|
| `admin` | `admin` | ADMIN |
| `cashier` | `cashier123` | CASHIER |

> The barcode PINs for both accounts are printed to the console on first launch. Use the **User Management** screen to print staff badges after logging in as admin.

**Change default passwords immediately in a production environment.**

---

## Configuration

All settings live in `pos-app/src/main/resources/application.properties`:

```properties
# Shop details (printed on receipts)
pos.shop.name=My Shop
pos.shop.address=123 Main Street
pos.receipt.footer=Thank you for your purchase!

# Currency symbol prepended to all prices
pos.currency.symbol= EGY

# Printer name (exact system name, empty = system default)
pos.printer.name=

# SQLite database location
spring.datasource.url=jdbc:sqlite:${user.home}/.cashier-pos/cashier.db

# Schema management (update = safe for production, never drops data)
spring.jpa.hibernate.ddl-auto=update
```

### Changing the Currency Symbol

1. Edit `pos.currency.symbol` in `application.properties`
2. The change propagates to all price displays and receipt formatting automatically

### Connecting a Thermal Printer

Set `pos.printer.name` to the exact name shown in your OS printer settings (e.g., `EPSON TM-T20III`). Leave blank to use the system default printer.

---

## Role Permissions

| Feature | Admin | Manager | Cashier | Inventory Clerk |
|---|:---:|:---:|:---:|:---:|
| Cashier screen (POS) | ✓ | ✓ | ✓ | ✓ |
| Product Manager | ✓ | ✓ | — | — |
| Reports & Analytics | ✓ | ✓ | — | — |
| User Management | ✓ | — | — | — |
| Add / delete users | ✓ | — | — | — |
| Change any password | ✓ | — | — | — |

---

## Project Structure

```
pos-core/src/main/java/org/example/cashier/core/
├── constants/
│   └── BarcodeConstants.java
├── discount/
│   ├── DiscountStrategy.java       ← Strategy interface
│   ├── NoDiscount.java
│   └── PercentageDiscount.java
├── entity/
│   ├── Barcode.java
│   ├── Category.java
│   ├── PrintJob.java
│   ├── Product.java
│   ├── Transaction.java
│   ├── TransactionItem.java
│   └── User.java
├── enums/
│   ├── BarcodeType.java
│   ├── LabelTemplate.java
│   └── UserRole.java
├── exception/
│   ├── BarcodeGenerationException.java
│   ├── PrintJobException.java
│   └── ProductNotFoundException.java
└── model/
    └── CartItem.java

pos-data/src/main/java/org/example/cashier/data/repository/
├── ProductRepository.java
├── TransactionRepository.java
└── UserRepository.java

pos-services/src/main/java/org/example/cashier/services/
├── AuthService.java                ← interface
├── CartService.java                ← interface
├── CashierProductService.java      ← implements ProductService
├── CashierReportService.java       ← implements ReportService
├── CheckoutService.java            ← interface
├── ProductService.java             ← interface
├── ReceiptFormatter.java           ← @Component: text layout for receipts
├── ReceiptService.java             ← interface
├── ReportService.java              ← interface (+ RangeSummary record)
├── UserManagementService.java      ← interface
└── impl/
    ├── AuthServiceImpl.java
    ├── BarcodeGenerationServiceImpl.java
    ├── CartServiceImpl.java
    ├── CheckoutServiceImpl.java
    ├── LabelPrintingServiceImpl.java
    ├── ReceiptServiceImpl.java
    └── UserManagementServiceImpl.java

pos-reports/src/main/java/org/example/cashier/reports/barcode/
└── ZXingBarcodeEngine.java         ← CODE128 / EAN-13 PNG generation

pos-ui/src/main/java/org/example/cashier/ui/
├── AlertHelper.java
├── SessionState.java
├── StageManager.java               ← screen navigation + fade transitions
├── controller/
│   ├── CheckoutController.java
│   ├── LoginController.java
│   ├── MainCashierController.java
│   ├── ProductManagerController.java
│   ├── ReceiptController.java
│   ├── ReportsController.java
│   └── UserManagerController.java
└── util/
    ├── CurrencyFormatter.java
    └── DateUtil.java

pos-ui/src/main/resources/fxml/
├── checkout.fxml
├── login.fxml
├── main_cashier.fxml
├── product_manager.fxml
├── receipt.fxml
├── reports.fxml
└── user_manager.fxml

pos-app/src/main/java/org/example/cashier/app/
├── Launcher.java                   ← main() entry point
├── PosApplication.java             ← Spring Boot @SpringBootApplication
├── config/
│   └── DataInitializer.java        ← Seeds default admin + cashier on first run
└── StageReadyListener.java         ← Bridges Spring events to JavaFX Stage
```

---

## Running Tests

```bash
# All modules
mvn test

# Specific module
mvn test -pl pos-services
mvn test -pl pos-core

# With coverage report (if JaCoCo is configured)
mvn verify
```

### Test Scope

| Test Class | Module | What it covers |
|---|---|---|
| `AuthServiceImplTest` | pos-services | Password login, barcode login, lockout after 5 failures, unlock on success |
| `CartServiceImplTest` | pos-services | Add/remove items, stock enforcement, discount strategies, totals |
| `CheckoutServiceImplTest` | pos-services | Checkout flow, cash validation, stock deduction, empty cart guard |
| `CashierProductServiceTest` | pos-services | SKU lookup, search, SKU generation uniqueness |
| `DiscountStrategyTest` | pos-core | NoDiscount always zero, PercentageDiscount calculation and rounding |
| `CurrencyFormatterTest` | pos-ui | Null handling, formatting with configured symbol |
| `DateUtilTest` | pos-ui | LocalDate and LocalDateTime formatting, null safety |

---

## Database

The SQLite database is stored at:

| OS | Path |
|---|---|
| Windows | `C:\Users\<username>\.cashier-pos\cashier.db` |
| macOS / Linux | `~/.cashier-pos/cashier.db` |

The schema is managed by Hibernate (`ddl-auto=update`). Tables are created on first launch and altered (never dropped) on subsequent launches. To reset to a clean state, delete the `.db` file — it will be recreated with fresh default users on the next run.

---

## License

This project is for internal / educational use.
