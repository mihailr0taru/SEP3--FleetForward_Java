# FleetForward – Java Server

This repository contains the **Java backend server** for the FleetForward system.  
It handles **data management, business logic, and communication with the .NET/Blazor client through gRPC**.

The server is responsible for:

- Managing fleet-related data
- Handling driver, dispatcher, and company operations
- Processing job assignments
- Communicating with the client application
- Interacting with the PostgreSQL database

---

# Architecture Overview

The project follows a **layered architecture** to separate responsibilities and keep the system maintainable.

```
Client (Blazor / .NET)
        │
        │ gRPC
        ▼
Networking Layer
Handlers
        │
        ▼
Service Layer (Business Logic)
        │
        ▼
Repository Layer (Database Access)
        │
        ▼
PostgreSQL Database
```

## Layers

### Networking Layer

Handles incoming **gRPC requests** and routes them to the correct handlers.

Key classes:

- `FleetServer`
- `FleetMainHandler`

---

### Handlers

Handlers receive network requests and call the appropriate services.

Examples:

- `AuthentificationHandler`
- `CompanyHandler`
- `DispatcherHandler`
- `DriverHandler`
- `JobHandler`
- `RecruitHandler`

---

### Service Layer

Contains the **business logic** of the application.

Examples:

- `CompanyService`
- `JobService`
- `DriverService`
- `DispatcherService`
- `RecruitDriverService`
- `AuthentificationService`

Each service may have a **database implementation**, such as:

```
CompanyServiceDatabase
JobServiceDatabase
DriverServiceDatabase
```

---

### Repository Layer

Responsible for **direct database communication**.

Examples:

- `CompanyRepository`
- `DriverRepository`
- `DispatcherRepository`
- `JobRepository`
- `UserRepository`

These repositories interact with **PostgreSQL**.

---

# Technologies Used

- Java
- gRPC
- Protocol Buffers
- PostgreSQL
- Maven

---

# Project Structure

```
src
 ├── main
 │   ├── java/dk/via/fleetforward
 │   │   ├── config
 │   │   ├── model
 │   │   ├── networking
 │   │   ├── repositories
 │   │   ├── services
 │   │   └── utility
 │   │
 │   ├── proto
 │   │   └── fleetforward.proto
 │   │
 │   └── resources
 │       └── application.properties
 │
 └── test
```

---

# Database Setup

SQL scripts are located in:

```
sql/
```

Important files:

```
fleetforward.sql
setup.sql
addresses_insert.sql
```

## Setup Steps

1. Create a PostgreSQL database
2. Run the SQL setup scripts
3. Configure the database connection in:

```
src/main/resources/application.properties
```

---

# Running the Server

## 1. Clone the repository

```
git clone <repository-url>
```

## 2. Build the project

Using Maven:

```
mvn clean install
```

or using the Maven wrapper:

```
./mvnw clean install
```

---

## 3. Start the server

Run:

```
StartServer.java
```

The server will start and begin listening for **gRPC client connections**.

---

# gRPC Communication

The communication contract between the **Java server and the .NET client** is defined in:

```
src/main/proto/fleetforward.proto
```

Protocol Buffers are used to:

- define request/response models
- generate gRPC client and server code
- ensure type-safe communication

---

# Documentation

Additional documentation can be found in the `docs` folder:

```
docs/
 ├── EntitiesRepoDocs.md
 ├── HandlerServiceDocs.md
 └── ServerDocs.md
```

These documents explain:

- entity and repository relationships
- handler-service communication
- server responsibilities

---

# Role in the FleetForward System

FleetForward consists of multiple components:

| Component | Technology |
|----------|------------|
| Client Application | Blazor (.NET) |
| Business Layer | .NET |
| Data Server | Java |
| Communication | gRPC |
| Database | PostgreSQL |

This repository represents the **Java data server** responsible for managing persistent data and handling fleet-related operations.

---

# Authors

FleetForward was developed as part of a **Software Engineering project at VIA University College**.
