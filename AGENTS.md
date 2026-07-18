# Project Agent Rules

## Project Context

This project is a frontend internship project for a legal-domain knowledge base. Favor clear, trustworthy, source-aware UI decisions for legal research and document workflows.

## Context Handoff Rule

Maintain `docs/codex-project-memory.md` as durable memory for this project.

Update it when:

- A task becomes long or multi-step.
- Important tools, skills, files, commands, decisions, or blockers change.
- Work may continue in another Codex window.
- The user asks to preserve context or mentions forgetting previous content.

Do not record passwords, tokens, private keys, raw credentials, or other secrets. For Feishu/Lark, record only setup status and next authorization steps.

## Frontend Design Rule

For legal knowledge-base UI work, use the `legal-kb-frontend-design` skill. Prioritize source visibility, legal metadata, long-text resilience, search/filter quality, citation actions, and careful AI-answer grounding.
