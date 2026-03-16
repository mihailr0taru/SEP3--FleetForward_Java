# FleetForward – Java Server

This repository contains the **Java backend server** for the FleetForward system.  
It manages **fleet data, business logic, and communication with the .NET client using gRPC**.

---

## System Architecture

```
Blazor Client (.NET)
        │
        │ gRPC
        ▼
Java Server
  ├── Handlers
  ├── Services (Business Logic)
  └── Repositories (Database Access)
        │
        ▼
PostgreSQL
```

---

## Tech Stack

- **Java**
- **gRPC / Protocol Buffers**
- **PostgreSQL**
- **Maven**

---

## Project Structure

```
src/main/java/dk/via/fleetforward
 ├── networking     → gRPC server & handlers
 ├── services       → business logic
 ├── repositories   → database access
 ├── model          → domain models
 └── config         → configuration

proto/              → gRPC contract
resources/          → application configuration
sql/                → database setup scripts
```

---

## Running the Server

Build the project:

```
mvn clean install
```

Start the server:

```
StartServer.java
```

The server will start and listen for **gRPC client connections**.

---

## gRPC Contract

Communication between the **Java server** and the **.NET client** is defined in:

```
src/main/proto/fleetforward.proto
```

---

## Role in FleetForward

| Component | Technology |
|----------|------------|
| Client | Blazor (.NET) |
| Business Layer | .NET |
| Data Server | Java |
| Communication | gRPC |
| Database | PostgreSQL |
