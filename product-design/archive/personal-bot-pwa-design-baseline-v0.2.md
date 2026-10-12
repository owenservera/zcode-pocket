---
artifact: personal-bot-pwa-design-baseline
version: 0.2.0
as_of: 2026-10-12
status: exploratory
implementation_authority: none
owner: user
purpose: Portable requirements and research starting point for local ZCode coding agents
---

# Personal Bot PWA — Requirements & Research Baseline v0.2

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
- The Android PWA is **also the universal intake and continuation surface** for work initiated anywhere on the phone: share/capture content into the local app, then synchronize, delegate to bots, and view outcomes on the phone or computer.
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
| INTAKE-00 | CONFIRMED | Treat phone-to-PWA-to-ZCode work handoff as a **primary product pillar**, not a secondary chat attachment feature. |


## Primary product pillar: Universal Phone Intake & Work Continuity

### Product intent — CONFIRMED

The PWA is the entry point for **anything the user does on Android** that they choose to pass to their personal AI system. This includes collecting content, retaining it locally, routing it to existing ZCode bots, and continuing work across Android and desktop. A useful goal is low-friction *explicit handoff*, not silent monitoring of all phone activity. "Anything" is an experience target; actual source-app interoperability must be tested.

### User flows — PROPOSED

**Flow A: Capture instantly**
1. In Chrome, another app, Photos, Files, or a supported Android share surface, choose Share → Personal PWA.
2. PWA opens to a share intake handler, stores original content and capture metadata durably **before** reporting "Saved on phone".
3. Show a fast receipt with minimal choices: **Save to Inbox** (safe default), **Ask a bot**, **Create a task**, **Add to a workspace**.
4. If online, upload a copy in the background under idempotent sync; show separate states: Local saved → Syncing → Synced → Bot received → Processing → Result available.
5. If offline, retain the captured material and pending *transfer*. Do not silently execute an old action on reconnect.

**Flow B: Continue from phone to desktop**
- Anything captured becomes a durable, searchable inbox item with user-approved server synchronization, provenance, attachments, and optional workspace/conversation associations.
- A computer bot can pick up the corresponding item using the existing backend's event/API contracts. The result is accessible from the PWA; no assumption that remote agent execution can be mirrored on Android.

**Flow C: Continue from computer to phone**
- Open a synchronized task, conversation, artifact, or pending approval in the PWA and continue it. Offer share/export to another Android app via browser-supported Web Share API where appropriate.

**Flow D: Manual and quick-capture fallback**
- In-app Paste, Write, Upload, optional voice capture/dictation, and deep links; do not rely solely on system share targets or require permissions to read arbitrary device content.

### Requirement matrix

| ID | Status | Requirement / research directive |
|---|---|---|
| INTAKE-01 | PROPOSED | Installed Android PWA registers an OS share target for text, URLs, and selected document/image/audio formats using `manifest.share_target`; confirm WebAPK/Chrome behavior on the real device. |
| INTAKE-02 | PROPOSED | Support multiple content types with one canonical intake landing surface, not separate OS share targets per bot. |
| INTAKE-03 | PROPOSED | Offline-capable share handling via service worker and IndexedDB/blob storage, subject to actual device and browser tests. |
| INTAKE-04 | PROPOSED | Receipt must distinguish **saved locally**, **transferred to server**, **accepted by bot**, **executed**; never claim success prematurely. |
| INTAKE-05 | PROPOSED | Do not transform, summarize or discard the original captured input unless policy allows it; derived interpretations remain linked to original. |
| INTAKE-06 | PROPOSED | Manual capture tools: paste, text note, file picker, camera/screenshot attachment where exposed, and potential voice dictation/recording. |
| INTAKE-07 | PROPOSED | Capture destination model: inbox by default, optional bot, existing session, task or workspace; quick routes can be customized later. |
| INTAKE-08 | PROPOSED | Content can travel phone → private server → agent → result → phone, with stable item IDs, acknowledgments, visibility and deduplication. |
| INTAKE-09 | PROPOSED | Shared material is **untrusted data**; an external article or document never silently gains instructions/permissions to run local tools. |
| INTAKE-10 | PROPOSED | Add explicit privacy and sensitivity controls; content need not automatically sync if marked local-only. User-approved execution is separate from capture. |
| INTAKE-11 | TO_VALIDATE | Whether source app identity, selected text vs URL vs attachment bytes, multiple files and metadata are actually available varies by source app and OS; do not invent missing provenance. |
| INTAKE-12 | TO_VALIDATE | Test share target registration with private HTTPS Tailscale origin, browser installation, PWA updates, foreground/background app states, offline handoff and large attachments. |
| INTAKE-13 | UNDECIDED | Native Android enhancements (Trusted Web Activity / Capacitor + share-intent handling) only if actual PWA capability gaps block core use cases. |
| INTAKE-14 | PROPOSED | Deep-link to captured items, bot results, sessions, workspaces and tasks using stable route IDs; avoid placing private payload data in URLs. |
| INTAKE-15 | PROPOSED | UX objective: *capture from another app and safely save in as few gestures as feasible*, without forcing a bot selection before saving. |

### Canonical intake envelope — illustrative, not adopted server schema

```json
{
  "capture_id": "device-generated-stable-uuid",
  "schema_version": 1,
  "created_at": "ISO-8601",
  "device_id": "opaque-device-id",
  "source": {
    "method": "android_share_target | paste | file_picker | note | voice",
    "app_hint": null,
    "url": null
  },
  "content": {
    "title": null,
    "text": null,
    "attachments": [
      { "blob_key": "local-durable-reference", "mime_type": "image/png", "sha256": "optional-content-hash" }
    ]
  },
  "routing": { "intent": "capture_only", "target_id": null },
  "sync": { "status": "local_saved", "server_id": null, "server_revision": null },
  "permissions": { "local_only": false, "execute": false }
}
```

- Treat content bytes separately from record metadata, validate MIME/type/size, and enforce storage quotas.
- The envelope is meant to seed a mapping/adapter, **not** force changes to the existing server schema.
- Avoid placing captured private text, access tokens, or files in GET query parameters where they can leak into logs/history. Prefer POST for payload-bearing shares.

### UI research experiments — same payload, different intents

- **Minimum sheet:** icon + recognizable preview + "Saved locally" receipt + three compact options: Ask bot / Create task / Organize. Default path must work with no further decision.
- **Universal Inbox:** recent captures, unsent queue, status/error states, workspace filters, route to bot after capture.
- **Quick Command:** optional typed/voice instruction attached to shared material ("Have my research bot compare this with the existing design") with visible destination and approval semantics.
- **Cross-device continuity:** last active work on phone and PC, result notifications, and open/resume same item.

### What PWA cannot be assumed to do

- A browser PWA cannot automatically read every Android app's data, notifications, microphone, clipboard or screen in the background; sources must offer sharing, explicit file selection, paste or permitted API access. Clipboard read has user-gesture/permission requirements.
- The Web Share Target API has **limited cross-browser support** and requires installation; verify Android Chrome/WebAPK support on the actual setup.
- Some apps share only a link, snippet, restricted content URI, or an exported file, not the full document/messages. The UX should explain what was captured and avoid pretending it has unseen content.
- Browser-managed local storage can be cleared/evicted; irreplaceable captures need durability/recovery policies.
- Native wrapper/companion is a later tested fallback, not required up front.

### Intake acceptance tests — REQUIRED for design signoff

1. Install the PWA on the target Android device; verify it appears in Share from Chrome, Photos and Files for claimed MIME types.
2. From Chrome share a URL+title; from Photos share an image; from Files share a PDF; inspect actually received payloads and source metadata.
3. With Tailscale/server unavailable, share a supported item and verify capture survives PWA/background termination, relaunch and reconnect.
4. Send the same capture twice after a network timeout: server gets no duplicate job, while history records what happened.
5. Share with another bot streaming in the PWA; both conversations and the captured item keep correct attribution and local state.
6. Share malformed/large/unsupported file and confirm validation, honest error messages, privacy and no data loss.
7. Accept receipt as "local only" and confirm nothing reaches server. Later opt in and confirm transfer.
8. Revoke permission/device access and confirm old capture links cannot grant fresh bot-execution authority.
9. Verify phone→server→bot→phone result handoff, including an explicit approval step where work has side effects.
10. Test against the actual app shortlist (Chrome, WhatsApp, Gmail, Photos, Files, Drive, YouTube, native screenshot, voice recorder); document exactly what each sends; do not claim all will share complete content.

### Intake research sources checked 2026-10-12

- MDN Web Share Target: https://developer.mozilla.org/en-US/docs/Web/Progressive_web_apps/Manifest/Reference/share_target
- Chrome Web Share Target: https://developer.chrome.com/docs/capabilities/web-apis/web-share-target
- MDN receiving shared data: https://developer.mozilla.org/en-US/docs/Web/Progressive_web_apps/How_to/Share_data_between_apps
- Web.dev OS integration: https://web.dev/learn/pwa/os-integration
- MDN Clipboard API: https://developer.mozilla.org/en-US/docs/Web/API/Clipboard_API
- Chrome Trusted Web Activity share target option: https://developer.chrome.com/docs/android/trusted-web-activity/web-share-target
- Optional Capacitor inbound-share plugin (only if needed): https://github.com/Cap-go/capacitor-share-target

## Logical entity candidates — TO_VALIDATE against live server

`capture`, `capture_attachment`, `capture_receipt`, `bot`, `agent_session`, `conversation`, `message`, `task`, `job`, `execution_event`, `capability`, `approval_request`, `workspace`, `artifact`, `device`, `snapshot`, `sync_cursor`, `pending_operation`.

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
- **R1 — Usable phone shell + universal intake**: installed PWA, HTTPS Tailscale origin, Android share target, offline capture receipt/inbox, bot list, one chat, live output, reconnection feedback, local draft/cache.
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

- What Android phone/browser/version and PWA install route is used? Does WebAPK install/register Share Target via the private Tailscale origin?
- Which Android sources are highest priority for capture (browser, messaging, screenshots, mail, PDF, recording)?
- For user-shared content, is the desired fast default "save" or "immediately ask a bot"? (Until resolved: safe local save.)
- What are acceptable maximum files/recording sizes and which captured material should remain phone-only?

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
- v0.2.0 (2026-10-12): confirmed universal Android-to-PWA intake and cross-device continuity as a product pillar. Added capture flows, proposed intake contracts, security, UX experiments, device validations and acceptance tests. No code or external repositories changed.