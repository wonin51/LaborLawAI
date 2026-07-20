# Internship Frontend Collaboration Playbook

## Skill Toolkit

- Visual design: `frontend-design`, `brand-guidelines`, `figma`, `figma-use`, `figma-implement-design`, `figma-create-design-system-rules`.
- Frontend verification: `playwright`, `playwright-interactive`, `screenshot`, `webapp-testing`.
- Git collaboration: `using-git-worktrees`, `finishing-a-development-branch`, `requesting-code-review`, `receiving-code-review`, `gh-address-comments`, `gh-fix-ci`.
- Planning and delivery: `writing-plans`, `executing-plans`, `verification-before-completion`, `doc-coauthoring`, `internal-comms`.
- Feishu/Lark delivery: `codex-lark-deliver`.

## Frontend Design Workflow

1. Define the screen's job, target user, and one concrete usage scenario.
2. Draft visual direction with palette, typography, layout, and one memorable interaction or signature element.
3. Implement the smallest complete version of the screen.
4. Verify responsive layout, keyboard focus, loading/empty/error states, and reduced-motion behavior.
5. Capture screenshots before asking teammates for review.

## Git Context Handoff

When handing work to a teammate, include:

- Branch name.
- What changed and why.
- Files or components they should read first.
- How to run locally.
- What was tested.
- Known issues or unfinished decisions.

Suggested message:

```text
Branch: feature/<name>
Context: <what this change is for>
Start here: <important files>
Run: <install/start/test commands>
Verified: <screenshots/tests/manual checks>
Needs review: <specific questions>
```

## Feishu/Lark Setup Notes

Installed tools:

- `lark-cli`
- `lark-channel-bridge`

Next manual steps:

1. Open a fresh terminal so User PATH changes are loaded.
2. Run `lark-cli auth login` and complete Feishu/Lark authorization.
3. Run `lark-cli auth status` to verify login.
4. Run `lark-channel-bridge run` if you want Feishu to communicate with local coding agents.
5. Keep tokens, secrets, and open_id values out of Git.

## Pull Request Checklist

- UI matches design direction and real project content.
- Mobile and desktop layouts were checked.
- No unrelated files are included.
- Commit messages explain intent, not just file names.
- PR description includes screenshots or screen recordings for visual changes.
- Reviewer questions are explicit.
