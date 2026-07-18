# Student Employment Law Assistant Backend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans or implement task-by-task with verification. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create a Java 17 + Spring Boot Maven backend project for the大学生就业法律助手 with a working health check endpoint.

**Architecture:** The backend is a single Spring Boot service under `backend/student-employment-law-assistant`. It exposes REST APIs under `/api`, starts with a health endpoint, and reserves layered packages for future RAG法律知识库、合同风险识别、法律问答等能力.

**Tech Stack:** Java 17, Spring Boot, Maven, Spring Web, Validation, MyBatis-Plus, MySQL Driver, Spring AI OpenAI Starter, Elasticsearch Java Client, Lombok, JUnit/Spring Boot Test.

## Global Constraints

- Create `backend` in current project root.
- Project name: `student-employment-law-assistant`.
- Maven dependency management.
- Java version: 17.
- Reserved packages: controller, service, mapper, entity, dto, vo, config, common, exception.
- Health endpoint: `GET /api/health`.
- Health response: `{ "status": "ok" }`.
- Do not implement business features yet.
- Final verification: `mvn test` or `mvn package -DskipTests`.

---

### Task 1: Maven Spring Boot skeleton

**Files:**
- Create: `backend/student-employment-law-assistant/pom.xml`
- Create: `backend/student-employment-law-assistant/src/main/java/com/example/ragkbdemo/RagKbDemoApplication.java`
- Create: `backend/student-employment-law-assistant/src/main/resources/application.yml`

- [ ] Create Maven project directories.
- [ ] Add Java 17 and required dependencies.
- [ ] Add Spring Boot application entrypoint.

### Task 2: Health endpoint with TDD

**Files:**
- Create: `backend/student-employment-law-assistant/src/test/java/com/example/ragkbdemo/controller/HealthControllerTest.java`
- Create: `backend/student-employment-law-assistant/src/main/java/com/example/ragkbdemo/controller/HealthController.java`

- [ ] Write failing MockMvc test for `GET /api/health` returning `{status:ok}`.
- [ ] Run test and confirm failure because controller does not exist.
- [ ] Implement minimal controller.
- [ ] Run tests again and confirm pass.

### Task 3: Reserved package structure

**Files:**
- Create placeholder package marker files in service, mapper, entity, dto, vo, config, common, exception.

- [ ] Create package directories with `package-info.java` so structure is kept in source control/filesystem.
- [ ] Run final Maven verification.
