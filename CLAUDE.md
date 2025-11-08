# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **311 Citizen Issue Tracker** - a full-stack civic issue reporting system using a three-tier microservices architecture:

- **Backend (be-citizen-tracker/)**: PostgreSQL 16 with PostGIS 3.4 for geospatial data
- **Middleware (mw-citizen-tracker/)**: Spring Boot 3.5.7 REST API (Java 17)
- **Frontend (fe-citizen-tracker/angular/)**: Angular 20 standalone components

The project is currently in Phase 1 Foundation - scaffolding is complete but minimal business logic exists.

## Development Commands

### Backend (Database)

```bash
cd be-citizen-tracker
docker-compose up -d          # Start PostgreSQL/PostGIS
docker-compose down           # Stop database
docker-compose logs -f        # View logs
```

**Connection Details:**
- Host: localhost:5432
- Database: city_issues
- Credentials: See .env file (currently committed - security concern)

### Middleware (Spring Boot API)

```bash
cd mw-citizen-tracker
./mvnw spring-boot:run        # Run development server (hot reload enabled)
./mvnw test                   # Run all tests
./mvnw test -Dtest=ClassName  # Run single test class
./mvnw clean install          # Build JAR with tests
./mvnw clean package          # Build JAR without tests
```

**Main Entry Point:** `src/main/java/gov/lby/mw_citizen_tracker/MwCitizenTrackerApplication.java`

### Frontend (Angular)

```bash
cd fe-citizen-tracker/angular
npm install                   # Install dependencies
npm start                     # Dev server at http://localhost:4200
npm test                      # Run Jasmine/Karma tests
npm run build                 # Production build
npm run watch                 # Auto-rebuild on changes
```

**Code Formatting:** Prettier is configured (100 char width, single quotes). Run formatting via your editor or add as a script if needed.

## Architecture Notes

### Monorepo Structure

All three tiers exist in a single repository. When working across tiers:
- Database schema changes should be reflected in middleware JPA entities
- API changes in middleware should be reflected in frontend services
- No inter-service communication is implemented yet

### PostGIS Integration

The database uses PostGIS extension for geospatial features. This suggests:
- Issue locations will be stored as geographic coordinates
- Map-based visualizations are likely needed in the frontend
- Spatial queries (nearest issues, issues in boundary) will be used

### Angular Standalone Components

This project uses Angular's modern standalone API (no NgModules):
- Components declare their dependencies directly via `imports: []`
- Routes are configured in `app.routes.ts`
- No `app.module.ts` file exists

Entry points:
- `src/main.ts` - bootstraps the application
- `src/app/app.ts` - root component
- `src/app/app.config.ts` - application-level providers

### Java Package Structure

Package: `gov.lby.mw_citizen_tracker`
- The `gov.lby` domain suggests Libya government project
- Follow this package convention for new classes

## Testing

### Middleware Tests
- Framework: JUnit 5 + Spring Boot Test
- Location: `src/test/java/gov/lby/mw_citizen_tracker/`
- Run: `./mvnw test`
- Currently only has context load test - integration tests needed

### Frontend Tests
- Framework: Jasmine + Karma (Chrome)
- Location: `*.spec.ts` files alongside components
- Run: `npm test`
- Coverage configured via karma-coverage

## Code Quality

### Frontend
- **TypeScript**: Strict mode enabled (`tsconfig.json`)
- **Prettier**: Configured in `package.json` (100 char, single quotes)
- **EditorConfig**: 2-space indentation, LF line endings
- **No ESLint**: Consider adding for additional linting

### Middleware
- **No linting tools configured**: Consider adding Checkstyle or SpotBugs
- **Spring Boot DevTools**: Hot reload enabled for development

## Known Issues

1. **Security**: `.env` file with database credentials is committed to git. Should be removed from git history and added to `.gitignore`.

2. **No CI/CD**: No automated testing or deployment pipelines exist yet.

3. **Incomplete Configuration**: Middleware's `application.properties` is minimal - needs database connection configuration when connecting to backend.

## Development Workflow

The project uses feature branches. Check current branch before starting work:
```bash
git status
git branch -a
```

Phase 1 development has been on `phase1/foundation` branch.
