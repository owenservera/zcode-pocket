# ZCode Pocket — Product & UI Design Lab

**This folder is the shared working area for the owner's ChatGPT-led design conversations and local frontend agents.** It coexists with the existing Android project; it is NOT a new application and must not overwrite source code in `app/`.

## Read first

1. [00-scope/NORTH-STAR.md](00-scope/NORTH-STAR.md) — what we are designing and what we are not.
2. [00-scope/REPO-REALITY.md](00-scope/REPO-REALITY.md) — what currently exists in this repo, including the Kotlin vs. PWA distinction.
3. [01-bots/BOT-INTERACTION-v0.1.md](01-bots/BOT-INTERACTION-v0.1.md) — first active UI workstream.
4. [01-bots/FEATURE-MATRIX.md](01-bots/FEATURE-MATRIX.md) — visible Grok reference vs current native app vs proposed UI.
5. [02-references/GROK-BOT-MOBILE-INVENTORY-v0.1.md](02-references/GROK-BOT-MOBILE-INVENTORY-v0.1.md) — observed screenshot menus/features (text only).
6. [04-decisions/DECISION-LOG.md](04-decisions/DECISION-LOG.md) — explicit decisions and unresolved choices.
7. [AGENTS.md](AGENTS.md) — guardrails for contributors.

## Working areas

- `00-scope/`: product boundaries and actual repo facts.
- `01-bots/`: **ACTIVE** bot-only mobile UX, screens, interaction ideas, prototypes later.
- `02-references/`: summarized UI evidence; no private screenshots in this public repository.
- `03-research/`: component/library candidates and research questions, no automatic adoption.
- `04-decisions/`: dated decision log with approval status.
- `05-parked/`: wider personal-app requirements deliberately deferred.
- `archive/`: historical exploratory requirement versions; they do not override current scope.

## Design discipline

Use `USER-CONFIRMED`, `REPO-OBSERVED`, `REFERENCE-OBSERVED`, `PROPOSED`, and `OPEN`. A screenshot showing an affordance does not establish working backend support. A proposed UI does not authorize code changes.

This is a **public repository**. Do not add user screenshots, raw bot transcripts, email addresses, private URLs, tokens, or personal captured content. Ask for approval before publishing sanitized screenshots.

## Repository policy

One integrated `main` branch. Favor small verifiable changes. Do not redesign Oracle, ZCode, local tooling, bots, server, models, permission enforcement, or execution infrastructure. UI contracts may be mapped from the existing code without inventing a replacement backend.
