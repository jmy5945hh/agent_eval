# agent_eval

Agent Evaluation Platform — a full-stack application for evaluating AI agents.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 2.7, JPA, Redis, MySQL, Lombok, MapStruct |
| Frontend | Vite, React 19, Ant Design 6 |
| Build | Maven (backend), npm (frontend) |

## Project Structure

```
agent_eval/
├── backend/          # Spring Boot service (DDD architecture)
│   └── src/
│       ├── main/java/com/example/agenteval/
│       │   ├── adaptor/rest/       # REST controllers
│       │   ├── application/        # Commands, queries, DTOs
│       │   ├── domain/             # Entities, repositories, services
│       │   └── infrastructure/     # Config (Redis, etc.)
│       └── test/                   # Unit tests
└── frontend/         # Vite + React SPA
    └── src/
        ├── components/   # UI components
        ├── mock/         # Mock data
        └── assets/       # Static assets
```

## Getting Started

### Backend

```bash
cd backend
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```
