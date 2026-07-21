# Codex Project Memory

Last updated: 2026-07-18

## Project

- Workspace: `C:\Users\Teddy\Documents\proj1`
- Project theme: legal-domain knowledge base.
- Current focus: build the labor-contract legal assistant backend/frontend foundation and preserve project context for team collaboration.
- Likely product needs: legal research search UI, source-heavy result cards, document reader, filters, citation actions, saved workspace, and careful AI/source transparency.

## User Preferences

- Preferred language: Chinese.
- Collaboration style: practical setup help, concrete tooling, project-specific frontend design support.
- Sensitive information rule: do not write passwords, tokens, private keys, or raw credentials into files. Use interactive login/authorization for Feishu/Lark.

## Installed Skills

- Frontend/design: `frontend-design`, `brand-guidelines`, `figma`, `figma-use`, `figma-implement-design`, `figma-create-design-system-rules`.
- Legal project custom skill: `legal-kb-frontend-design` created locally and intended for global install.
- Context continuity custom skill: `project-context-handoff` created locally and intended for global install.
- Frontend verification: `playwright`, `playwright-interactive`, `screenshot`, `webapp-testing`.
- Git/collaboration: `using-git-worktrees`, `requesting-code-review`, `receiving-code-review`, `gh-address-comments`, `gh-fix-ci`.
- Planning and delivery: `writing-plans`, `executing-plans`, `verification-before-completion`, `doc-coauthoring`, `internal-comms`.
- Feishu/Lark: `codex-lark-deliver`.
- Research paper reading: installed `pdf`, `jupyter-notebook`, `notion-research-documentation`, and third-party `academic-research-suite` from `Imbad0202/academic-research-skills-codex` for PDF reading, technical novelty extraction, reviewer-style critique, reproducibility checks, and interview-style questioning.

## Installed Tools

- Git works: `git version 2.53.0.windows.1`.
- Java available: Oracle JDK `23.0.1`; backend Maven project targets Java 17 via compiler release.
- Maven available: Apache Maven `3.9.16`.
- MySQL available as project-local portable install: `.local/mysql/mysql-8.4.10-winx64`; server verified alive on port `3306`.
- npm works via `npm.cmd`: version `10.9.4`.
- Feishu/Lark tools installed:
  - `lark-cli version 1.0.70`
  - `lark-channel-bridge`
- `gh` was not found in PATH during setup.
- `bash` was not found in the current PowerShell PATH, although the user says Git Bash is installed.

## Feishu/Lark Status

- `lark-cli auth status` returned `not_configured`.
- Next required step is user-driven authorization:
  - Run `C:\Users\Teddy\AppData\Roaming\npm\lark-cli.cmd config init --new`
  - Open the verification URL it prints and complete authorization.
  - Then run `C:\Users\Teddy\AppData\Roaming\npm\lark-cli.cmd auth status`.
- Do not use or store the user's Feishu password.

## Project Files Added

- `backend/`: Spring Boot backend skeleton for `legal-contract-assistant`.
- `frontend/`: Vue 3 + Vite + Element Plus frontend skeleton and legal consultation workstation prototype.
- `backend/src/main/resources/db/init.sql`: initial MySQL database and table skeleton for legal documents, chunks, QA sessions, and feedback.
- `docs/mysql-setup.md`: MySQL setup and database initialization notes.
- `scripts/start-local-mysql.ps1`: starts project-local portable MySQL on port `3306`.
- `README.md`: current project startup, frontend/backend verification, CORS, and troubleshooting instructions.
- `.gitignore`: excludes Maven, frontend, logs, IDE, and local secret artifacts.
- `docs/internship-collab-playbook.md`: frontend/Git/Feishu team workflow playbook.
- `docs/codex-project-memory.md`: this durable context handoff file.
- `docs/project-plan.md`: project plan based on lesson 01 bootstrap prompt, with architecture, workflow, schedule, risks, and acceptance charts.
- `docs/stitch-ui-prompt.md`: Stitch prompt for UI design of the labor-contract legal assistant, based on the requirement analysis document.
- `docs/ai-project-resume-worklog.md`: updateable AI project work record and resume material for postgraduate recommendation/resume writing.
- `local-skills/legal-kb-frontend-design/`: project-specific legal knowledge-base frontend design skill.
- `local-skills/project-context-handoff/`: skill for maintaining context handoff docs.

## Open Tasks

- Await project data from the user/team for import into MySQL.
- Later implement legal RAG modules: knowledge ingestion, document chunking, Elasticsearch retrieval, structured answer API, legal source citation, feedback/history.
- Keep `docs/ai-project-resume-worklog.md` updated whenever AI project scope, implementation progress, UI artifacts, RAG backend work, or measurable results change.
- Complete Feishu authorization interactively if the user wants live Feishu messaging.
- Consider installing GitHub CLI later if PR workflow needs `gh`.
- When the user provides a paper PDF, title, DOI, arXiv link, or asks Codex to search for a paper, use the research paper reading skills to produce expert-reviewer and postgraduate-interview style analysis: problem setting, method pipeline, technical innovation, assumptions, experiments, weaknesses, reproducibility risks, and targeted oral-defense questions.

## Backend Status

- Created backend project at `C:\Users\Teddy\Documents\proj1\backend`.
- Project name/artifact: `legal-contract-assistant`.
- Package root: `com.laborlaw.ragkbdemo`.
- Reserved packages: `controller`, `service`, `mapper`, `entity`, `dto`, `vo`, `config`, `client`.
- Health endpoint: `GET /api/health`.
- Health response verified by running the jar locally: `{"status":"ok"}`.
- Build verification:
  - `mvn test`: passed.
  - `mvn package -DskipTests`: passed.
- Spring AI version adjusted from `2.0.0` to `1.1.8` because `2.0.0` brought Spring Boot 4 auto-config classes that conflicted with Spring Boot 3.5.3.
- `OPENAI_API_KEY` defaults to non-secret `demo-key` so the skeleton can start without a real key; real model calls must override it with environment variables.
- Added `WebMvcConfig` CORS handling for `/api/**`, allowing local Vite origins `localhost:5173`, `127.0.0.1:5173`, `localhost:4173`, and `127.0.0.1:4173`.
- Added initial MySQL schema for `legal_contract_assistant`; it is ready for later data import once MySQL is installed and running on port `3306`.
- Project-local MySQL 8.4.10 was downloaded from official MySQL CDN, initialized with root empty password for local development, started on port `3306`, and verified with database/tables:
  - `kb_document`
  - `kb_chunk`
  - `qa_session`
  - `qa_feedback`
- Backend was temporarily started on port `8081` while MySQL was running; `GET /api/health` returned `{"status":"ok"}`.

## Frontend Status

- Created frontend project at `C:\Users\Teddy\Documents\proj1\frontend`.
- Project name: `legal-assistant-web`.
- Tech stack: Vue 3, Vite, Element Plus, Axios, Element Plus icons.
- Main UI: legal consultation workstation with left navigation, central structured answer area, right citation/source panel, evidence checklist, process steps, contract review preview, knowledge-base management table, and placeholder modules.
- Frontend request wrapper: `frontend/src/api/http.js`.
- Vite proxy: `/api` -> `http://localhost:8080`.
- `npm.cmd install`: passed with 0 vulnerabilities.
- `npm.cmd run build`: passed. Vite reported a large chunk warning from Element Plus/Vue bundle size; this does not block running.
- Compatibility verification:
  - Existing backend at `localhost:8080` returned `{"status":"ok"}`.
  - Vite proxy at `127.0.0.1:5173/api/health` returned `{"status":"ok"}`.
  - CORS preflight against a fresh backend instance on port 8081 returned `Access-Control-Allow-Origin: http://localhost:5173`.

## AI Coding Issues

- Issue: Initial Vite build under restricted sandbox failed with `Cannot read directory "../../..": Access is denied`.
  - Cause: esbuild/Vite config resolution needed filesystem access outside the restricted sandbox boundary.
  - Fix: reran the build with approved elevated permissions.
- Issue: Backend compatibility script failed because port `8080` was already in use.
  - Cause: another backend process was already listening on `8080`.
  - Fix: verified that existing `8080` responded to `/api/health`; used temporary port `8081` to validate the newly added CORS config without stopping the user's process.
- Issue: CORS can fail when frontend and backend run on different local ports.
  - Solution: frontend dev proxy handles `/api` requests during Vite development; backend `WebMvcConfig` also allows local frontend origins for direct API calls.
## 2026-07-20 P0 处理记录

- 已将 `backend/src/main/resources/application-local.yml` 中的本地敏感值改为环境变量占位，并新增 `application-local.example.yml`。
- 已将知识库文档管理页面接入真实接口，入口在 `frontend/src/App.vue`，页面为 `frontend/src/views/KnowledgeDocsView.vue`。
- 已新增 `frontend/src/api/knowledgeDocuments.js`、Vitest 最小配置和 `KnowledgeDocsView.test.js`。
- `/api/admin/**` 已增加 `X-Admin-Token` 基础访问控制，令牌由 `ADMIN_API_TOKEN` 注入，前端使用 `VITE_ADMIN_API_TOKEN`。
- 关键验证：`frontend npm test`、`frontend npm run build`、`backend mvn test` 均已验证通过；前端构建仍有 chunk 体积警告。
- 当前后续重点：正式认证/RBAC、异常统一处理、包名和构建产物治理。
## 2026-07-20 P1 优化记录

- 全局异常处理已覆盖 JSON 解析错误、请求参数错误、数据库异常和未预期异常；客户端统一收到结构化错误。
- 删除接口已移除局部异常吞掉逻辑，统一交由全局异常处理器处理。
- 删除旧的 `com.teddy.legal` 源码、配置和测试入口，正式包名统一为 `com.laborlaw.ragkbdemo`，Maven groupId 统一为 `com.laborlaw`。
- `backend/target` 与 `frontend/dist` 已从 Git 索引移除，`.gitignore` 补充 `.vite/` 和 `coverage/`。
- 知识库页面测试扩展到 6 个场景，覆盖详情接口、失败提示和新增后刷新。
- 关键验证：`backend mvn test`、`frontend npm test`、`frontend npm run build` 均通过；构建仍保留 chunk 体积警告。
## 2026-07-20 配置型 RBAC 记录

- 管理员鉴权从单 token 升级为 `ADMIN_ACCOUNTS` 配置型 RBAC。
- 账户格式：`username|token|ROLE_1,ROLE_2`。
- 当前角色：`ADMIN`、`KNOWLEDGE_READ`、`KNOWLEDGE_WRITE`。
- GET/HEAD 需要 `KNOWLEDGE_READ`，POST/PUT/PATCH/DELETE 需要 `KNOWLEDGE_WRITE`。
- `ADMIN_API_TOKEN` 保留为旧环境兼容回退，默认不配置。
- 审计日志增加 actor 和 roles，不记录 token。
- 构建产物去跟踪提交：`1346c59 chore: stop tracking generated build artifacts`。
- 用户要求暂不继续处理前端体积问题。
## 2026-07-20 知识库文档 CRUD 记录

- `frontend/src/views/KnowledgeDocsView.vue` 已支持新增、查询/详情、编辑、删除和状态更新。
- 编辑会先调用详情接口加载完整元数据，再调用 PUT 更新。
- 删除使用确认弹窗，成功后自动刷新当前分页。
- 状态下拉使用 PATCH 状态接口，失败时不直接修改列表数据。
- 前端测试扩展到编辑、删除、状态更新，当前 10 个测试通过。

## 2026-07-21 Risk Review Update

- User asked to inspect `G:\LaborLawAI` and write potential project risks into `项目风险问题分析.md`.
- Updated `项目风险问题分析.md` with a fresh current-state review covering Git-tracked generated artifacts, local env/auth files, backend test failure, public QA/knowledge endpoints, divergent DB scripts, RBAC limitations, validation gaps, delete/index lifecycle risks, missing CI, frontend size, and demo-only UI risks.
- Verification evidence collected:
  - `git ls-files` still tracks `frontend/node_modules` (11712 files), `backend/target` (63 files), `frontend/dist` (1 file), `.npm-global`, `.npm-cache`, `.lark-auth`, `.idea`, `.vscode`, and local env/config files.
  - Running validation initially modified tracked `backend/target` and `frontend/dist`, confirming generated artifact tracking risk; those generated changes were restored with `git restore -- backend/target frontend/dist` before editing docs.
  - `frontend`: `npm audit --omit=dev` and `npm audit` both reported 0 vulnerabilities; `npm test` passed with 2 test files and 10 tests; `npm run build` succeeded but output large CSS/main JS sizes and Rollup PURE-comment warnings.
  - `backend`: `mvn test` failed with ApplicationContext errors because WebMvc slice tests load scanned MyBatis mappers without `sqlSessionFactory/sqlSessionTemplate`.
- Do not record raw local passwords, tokens, QR auth values, or secrets when continuing this risk work.

## 2026-07-21 Read-only backend query APIs

- User requested paginated read-only endpoints for `kb_document`, `kb_chunk_ref`, `qa_answer`, and `qa_answer_citation` using MyBatis-Plus, DTO/Entity/VO separation, `ApiResponse`, `page_no`/`page_size`, id-desc ordering, and QA record question assembly via `qa_message.content_redacted`.
- Existing backend source already had the four public read-only controllers/services/DTOs/VOs: `/api/knowledge/documents`, `/api/knowledge/chunk-refs`, `/api/qa/records`, `/api/qa/citations`.
- Added `backend/src/test/java/com/laborlaw/ragkbdemo/controller/ReadOnlyQueryControllerTest.java` covering all four endpoints, unified `code=0` responses, JSON field names, pagination params, filters, and `question` field in QA record VO.
- Fixed WebMvc slice test pollution by moving `@MapperScan("com.laborlaw.ragkbdemo.mapper")` from `RagKbDemoApplication` to `MybatisPlusConfig`; full app still loads mapper scan via normal configuration, while MVC slice tests no longer instantiate mapper factory beans without MyBatis session factory.
- Verification: `cd backend && mvn test` passed with 35 tests, 0 failures, 0 errors, 0 skipped. Maven still emits existing `@MockBean` deprecation warnings.
- Running Maven modifies tracked `backend/target` artifacts due existing repository hygiene issue; those generated changes were restored after verification.

## 2026-07-21 Frontend knowledge query pages

- User requested wiring four read-only query APIs into frontend: `kb_document`, `kb_chunk_ref`, `qa_answer`, `qa_answer_citation`.
- Existing files already present: `frontend/src/api/knowledgeSources.js`, `frontend/src/api/qaHistory.js`, `frontend/src/views/KnowledgeSourcesView.vue`, `frontend/src/views/QaHistoryView.vue`, and `App.vue` imports/routes for `sources` and `history`.
- Added row-click detail behavior to all four Element Plus tables: documents, chunks, QA records, citations. Kept existing Chinese detail buttons.
- Renamed chunk detail dialog title from `分片详情` to `知识分片详情` for clearer Chinese UI.
- Added tests:
  - `KnowledgeSourcesView.test.js`: tabs, Chinese labels, list data, filters/pagination API params, row-click detail dialogs, no `标题(title)` display.
  - `QaHistoryView.test.js`: tabs, Chinese labels, list data, filters/pagination API params, row-click detail dialogs, no `标题(title)` display.
  - Updated `App.test.js` to verify `sources` and `history` render real pages instead of placeholders.
- Verification: `cd frontend && npm test` passed with 4 test files and 20 tests. `npm run build` passed; existing Rollup PURE-comment warnings remain. Restored generated `frontend/dist` changes after build because dist is still tracked in this repository.

## 2026-07-21 README startup documentation

- User requested creating `Changes.md` under `G:\LaborLawAI` and updating `README.md` with concrete backend/frontend startup steps.
- Created `Changes.md` with a 2026-07-21 documentation change entry.
- Updated README backend startup section with prerequisites, database initialization options, environment variables, `mvn spring-boot:run`, health-check curl, tests, package, and JAR run commands.
- Updated README frontend startup section with Node/npm prerequisites, `.env` setup, `npm.cmd install`, `npm.cmd run dev`, default `http://localhost:5173` URL, tests, build, and preview commands.
