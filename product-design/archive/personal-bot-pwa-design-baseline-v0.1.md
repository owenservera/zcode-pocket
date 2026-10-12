---
artifact: personal-bot-pwa-design-baseline
version: 0.1.0
as_of: 2026-10-12
status: exploratory
implementation_authority: none
owner: user
purpose: Portable requirements and research starting point for local ZCode coding agents
---

# Personal Bot PWA — Requirements & Research Baseline v0.1

## Reading rules for implementation agents

- `CONFIRMED` = user stated fact or goal. Do not challenge or replace it without a documented reason.
- `PROPOSED` = architectural suggestion, **not an approved implementation decision**.
- `TO_VALIDATE` = empirical research or inspection required before selecting dependencies or modifying the backend.
- `UNDECIDED` = intentional open design space. Do not silently decide.
- This file is a baseline for design discussion, **not** an instruction to rebuild a working server.

## Context

**CONFIRMED**
- A custom local ZCode bot system already exists; server/backend is substantially complete.
- User is building a personal Android Progressive Web App (PWA) to access/control bots through Tailscale.
- App should retain its own local data, including versioned snapshots, and support fast PWA iterations.
- Local data and server should be closely aligned conceptually; concurrency and version compatibility matter.
- Future work includes frontend, features, UX, roadmap, research, and reusable tools/skills.
- Specific backend language, database, transports, APIs, and current app code have not been supplied here. Never assume them.

## Foundational requirements

| ID | Status | Requirement / position |
|---|---|---|
| ARCH-01 | CONFIRMED | Android-first installable PWA with access to existing ZCode bots through Tailscale. |
| ARCH-02 | CONFIRMED | Local storage with historical/versioned snapshots and good concurrency. |
| ARCH-03 | PROPOSED | Match server-client **logical schemas, IDs and protocols**, not necessarily physical database/runtime. |
| ARCH-04 | PROPOSED | Server remains authoritative for agent execution and operational state; client is local-first for human interaction, drafts, layouts, and cached views. |
| ARCH-05 | PROPOSED | Independently version app bundle, local schema, sync protocol, records/events, and snapshots. |
| DATA-01 | PROPOSED | Classify local data as working state, replicated views, immutable snapshots, or pending operations. |
| DATA-02 | PROPOSED | Local writes are durable before UI confirmation; use transactional updates and migrations. |
| DATA-03 | PROPOSED | Sync deltas with revision/checkpoints rather than whole-state copying where feasible. |
| DATA-04 | PROPOSED | Snapshot retention is configurable; no invented retention intervals or quotas. |
| SYNC-01 | PROPOSED | Explicit online/offline/degraded, fresh/stale, pending/acknowledged/rejected UI states. |
| SYNC-02 | PROPOSED | Each operation has stable ID, idempotency key, base revision, and server acknowledgement. |
| SYNC-03 | PROPOSED | Preserve conflicting edits and provide deterministic policies per object type (drafts vs layout vs bot configs vs jobs). |
| SYNC-04 | PROPOSED | Offline executable bot commands do not auto-run on reconnect by default; require policy/expiry/reconfirmation. |
| COMPAT-01 | PROPOSED | App/server exchange supported protocol versions and feature capabilities; unsupported features degrade gracefully. |
| COMPAT-02 | PROPOSED | Schema migration tested against genuine old DB copies; on failure preserve/backup and show recovery, never silently wipe. |
| COMPAT-03 | PROPOSED | Service worker and data/schema upgrades coordinate to prevent half-upgraded active sessions; do not assume downgrading IndexedDB is easy. |
| SEC-01 | PROPOSED | HTTPS secure origin, in addition to Tailscale transport; Tailscale admission alone is not authorization to run destructive agent actions. |
| SEC-02 | PROPOSED | Limit permissions, validate commands server-side, avoid browser storage of long-lived privileged secrets. |
| SEC-03 | PROPOSED | Define phone-loss, session revocation, local sensitive-content retention, and backup policy before storing private histories broadly. |
| UX-01 | PROPOSED | Launch into one useful surface: bot selector + chat + runtime status + concise actions. |
| UX-02 | PROPOSED | Mobile keyboard, streamed output, interrupted streams, retry, navigation restoration and one-handed controls must work. |
| UX-03 | PROPOSED | Preserve user draft and navigation/scroll state across reloads and app updates. |
| UX-04 | UNDECIDED | Final information architecture: conversation-first, bot control room, task inbox, or composite. |
| UX-05 | UNDECIDED | Long-term workspace/canvas/map-of-bots capability; keep optional and separately test. |

## Logical entity candidates — TO_VALIDATE against live server

`bot`, `agent_session`, `conversation`, `message`, `task`, `job`, `execution_event`, `capability`, `approval_request`, `workspace`, `artifact`, `device`, `snapshot`, `sync_cursor`, `pending_operation`.

Avoid creating new backend tables until current API/entity inventory is complete. Use stable IDs, provenance, timestamps, and revision metadata. An execution event stream may be useful, but event sourcing is not yet selected.

## Sync behavior sketch — PROPOSED

1. On launch, render last known local data immediately and label it with freshness.
2. Check connection to server. Negotiate protocol and capabilities.
3. Download changed server records/events since last acknowledged cursor.
4. Apply validated changes transactionally; update cursor only after commit.
5. Upload permitted local changes with idempotency keys and base revisions.
6. Report/reconcile conflicts by data type; **never** treat bot command execution like harmless preference syncing.
7. Keep an explicit recoverability path for failed schema migrations, partial downloads and storage quota errors.

## UI tracks to prototype — not approved final design

### A. Conversation-first (recommended initial experiment)
Full-screen streaming chat; compact bot/workspace switcher; slide-out history; persistent composer; visible connection/processing state; tap through to approvals and execution traces.

### B. Bot control room
Bot roster and live status first, a single selected conversation second, jobs/alerts as a side or bottom sheet. Useful if many bots are active simultaneously.

### C. Task inbox
Pending decisions, failures, approvals, completed results and mentions as the home screen; chat opens in a drill-in. Strong for supervising asynchronous work.

Evaluate each on Android using actual workflows: direct a bot, switch bots while one streams, approve task, inspect output, resume after disconnect.

## Research/evaluation backlog

| Track | Candidates | Decision test |
|---|---|---|
| Local database lite | IndexedDB + Dexie | Low complexity; migrations, transactions, reactive views; build sync protocol intentionally. |
| Local database with sync | RxDB | Compare conflict handling, reactive records, multi-tab coordination and replication integration burden. |
| Other local-first tooling | TanStack DB + adapter (e.g. RxDB or PowerSync) | Only if matching existing server source/database; avoid adopting a second backend. |
| App offline/update | Vite PWA plugin + Workbox | Prompted updates, predictable service worker lifecycle, restore sessions safely. |
| Chat UI | assistant-ui | Check custom backend adapter, streaming, tool cards, approval UI, accessibility. |
| Mobile component shell | shadcn/ui | Check drawers, bot selector, dialogs, theming, touch targets; no framework rewrite solely to use components. |
| Long chat/logs | TanStack Virtual | Preserve scroll position while prepending history and streaming. |
| Visual bot canvas | React Flow | Exploratory later; evaluate touch behavior and performance before adoption. |
| Private network | Tailscale Serve + HTTPS | Verify stable `*.ts.net` origin, certs, auth/ACL, browser installability and socket support. |

## Roadmap hypotheses

- **R0 — Audit without changes**: inspect existing server implementation, API/events, auth, actual data models, current PWA structure; capture test fixtures; make architecture decision record.
- **R1 — Usable phone shell**: installed PWA, HTTPS Tailscale origin, bot list, one chat, live output, reconnection feedback, local draft/cache.
- **R2 — Local durability**: versioned database, migrations, snapshots, export/recovery, schema compatibility, storage persistence/quotas tests.
- **R3 — Multi-bot operations**: task inbox, approvals, streaming job events, switch/parallel conversations, idempotent operations, selective replication and conflict policy.
- **R4 — Advanced mobile workspace**: visual orchestration, custom dashboards, offline planning, richer artifacts and experiments if validated by usage.

## Minimum falsifiable acceptance tests

1. Install PWA on an Android device over stable HTTPS Tailscale address; reopen after killing browser.
2. Open with Tailscale disconnected; last-known conversation renders clearly marked stale; local draft survives.
3. Reconnect after server advanced two conversations; sync converges without duplicate messages or reordered history.
4. Interleave multiple chat/bot streams without losing last-message position or response attribution.
5. Update UI bundle while older data schema exists; app remains usable or presents explicit repair, never wipes silently.
6. On two simulated writers editing same record, conflict is detected; deterministic policy applies and preserves recoverable data.
7. Replaying the same operation ID does not create a duplicate job on server.
8. Offline high-risk operation cannot execute unexpectedly when connection returns.
9. Browser refuses persistent storage / quota fills / device clears origin data; recovery instructions and server copies prevent loss of irreplaceable content.
10. Phone is revoked from tailnet/app session; access is denied even with stale installed PWA.

## Open questions requiring facts, not assumptions

- What language/framework/database does the existing ZCode backend really use?
- What API shape, streaming transport and auth are available today?
- Which entities actually need edit access on both phone and server?
- How many bots/sessions are routinely active and how long are their output logs?
- Is phone storage intended for text only or also files/images/large artifacts?
- How important is offline command *submission* versus offline *drafting*?
- Which UI track best matches actual phone use?

## Evidence references checked 2026-10-12

- Browser origin/quota/eviction: https://developer.mozilla.org/en-US/docs/Web/API/Storage_API/Storage_quotas_and_eviction_criteria
- PWA updates: https://web.dev/learn/pwa/update
- PWA offline data: https://web.dev/learn/pwa/offline-data
- Dexie migrations: https://dexie.org/docs/Version/Version
- RxDB replication: https://rxdb.info/replication.html
- RxDB conflict handling: https://rxdb.info/transactions-conflicts-revisions.html
- TanStack DB adapters: https://tanstack.com/db/latest/docs/installation
- Workbox background sync: https://developer.chrome.com/docs/workbox/modules/workbox-background-sync
- Tailscale HTTPS: https://tailscale.com/docs/how-to/set-up-https-certificates
- Tailscale Serve: https://tailscale.com/docs/features/tailscale-serve
- assistant-ui: https://github.com/assistant-ui/assistant-ui
- shadcn mobile navigation: https://ui.shadcn.com/docs/components/base/sidebar
- TanStack Virtual chat: https://tanstack.com/virtual/latest/docs/chat
- React Flow touch: https://reactflow.dev/examples/interaction/touch-device

## Change record

- v0.1.0 (2026-10-12): first portable conceptual baseline compiled from conversation and public documentation. No code or external repositories changed.