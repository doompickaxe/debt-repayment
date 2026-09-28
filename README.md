# Debt Repayment

## Approach

3 hour time investment.

* Hour 1: Problem understanding, planning solution, creating logic-less repository
* Hour 2: Focus on backend implementation
* Hour 3: Focus on frontend implementation

As the usage of LLMs is allowed, it will be used where useful.
A prompt history will be part of this project.

# Technical Documentation

## Project structure

| Path | Content |
|------|---------|
| `openapi/debt-repayment-api.yaml` | OpenAPI 3.1 spec, the single source of truth for the API |
| `backend/` | Quarkus 3 (Java 25, Gradle). JAX-RS interfaces and models are generated from the spec into `build/generated/openapi`, and resources implement them. |
| `frontend/` | Angular 22 (Yarn 4). The `typescript-angular` client is generated from the spec into `src/app/api` (git-ignored). |

The backend serves the Angular app via [Quinoa](https://docs.quarkiverse.io/quarkus-quinoa/dev/). During the Gradle build it runs `yarn install` and `yarn build` in `frontend/` and bundles the output. It also handles SPA routing: every path except `/api` and `/q` falls back to `index.html`.

## Prerequisites

* Java 25
* Node.js 24+ (Yarn is bundled in `frontend/.yarn/releases`, so you don't need a global install)

## Development

```bash
./gradlew :backend:quarkusDev
```

This starts Quarkus dev mode at http://localhost:8080 with live reload for both Java and Angular (Quinoa starts `ng serve` and proxies to it).

You can also run the frontend on its own (it proxies `/api` to `localhost:8080`):

```bash
cd frontend && yarn start
```

## Changing the API

1. Edit `openapi/debt-repayment-api.yaml`.
2. Regenerate the code: `./gradlew :backend:openApiGenerate` for the backend (also runs automatically before compilation) and `cd frontend && yarn generate:api` for the frontend (also runs automatically before `start`, `build` and `test`).
3. Implement the new interface methods in the backend and use the generated services in Angular.

## Endpoints

* `/`: Angular app
* `/api/*`: REST API
* `/q/openapi`: OpenAPI spec
* `/q/swagger-ui`: Swagger UI

## Build & test

```bash
./gradlew build
```

This builds the frontend, runs the backend tests and produces `backend/build/quarkus-app/quarkus-run.jar`. To run the frontend tests: `cd frontend && yarn test --watch=false`.
