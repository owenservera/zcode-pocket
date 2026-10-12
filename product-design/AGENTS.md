# Agent Instructions — Product Design Folder

Scope: frontend/product UX only, particularly bot interaction. Assume existing Oracle/ZCode local bot infrastructure works.

1. Read README.md, 00-scope/NORTH-STAR.md, 00-scope/REPO-REALITY.md and 04-decisions/DECISION-LOG.md before acting.
2. Never bootstrap/rebuild bot orchestration, runtime, backend API, models, prompts, MCPs, memory, authorization, or machine control on the strength of these design docs.
3. First active workstream is the **bots-only mobile interface**; Saved Views are parked. Other future app domains remain recorded, not implementation-authorized.
4. Source of truth for implementation behavior is current application code; don't assume older PWA design proposals describe this Kotlin Android codebase.
5. Distinguish OWNER-CONFIRMED, REPO-OBSERVED, REFERENCE-OBSERVED, PROPOSED and OPEN. Never turn proposed UX into requirements silently.
6. Prototype UI alternatives separately; do not replace working Android screens unless specifically requested. Keep designs accessible, mobile-first, responsive and usable one-handed.
7. Retain readable history and update the decision log after confirmed choices. One integrated main; short-lived branches if needed.
8. This repository is PUBLIC. Never commit screenshots showing private chats/account identifiers, tokens, keys, private network details or real conversation logs. The Grok reference screenshots were reviewed but are intentionally NOT included here.
9. Never invent bot activity or claim presence/typing/tool states that the app doesn't actually receive; reveal advanced activity only where data exists.
10. For uncertain integration details, write a frontend UI input contract or REPO-OBSERVED audit. Ask the local implementer to map to existing interfaces; don't expand backend scope.
