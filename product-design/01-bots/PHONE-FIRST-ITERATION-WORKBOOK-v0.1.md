---
artifact: phone-first-iteration-workbook
version: 0.1
date: 2026-10-13
status: active-design-workbook
execution_authority: none
owner: human
workbench: ChatGPT conversation + published evidence from the existing local agent project
---

# ZCode Pocket — Phone-First Iteration Workbook

**Goal:** The owner opens one stable, installable PWA on their Android phone and has a **real, two-way, streaming conversation with a local ZCode agent**. From that working experience, grow the interface **one personally tested change at a time**. The PWA is an evolving product, not a frozen dashboard derived from a huge initial specification.

**Owner decision (2026-10-13):** PWA forward; fast updates; use the actual phone to judge appearance and interaction; begin with one agent and only then consider a workspace, additional agents, group conversation and deeper organizational behavior. This updates the old open question of starting from an inbox versus a first conversation: start with **one conversation**. All aesthetic decisions stay open until observed on the phone.

**Design mandate:** This is a *frontend UX/product* workbook. Do not redesign ZCode, agents, models, backend security, tools or orchestration here. A small transport-readiness handoff may be sent separately to the existing local system custodian. Saved Views and universal Android intake stay parked, not forgotten. The product remains Android-first and primarily over the owner's private Tailscale network.

## 1. Grounded starting state (remote repository snapshot, NOT live machine check)

- The standalone public repository currently contains a **Kotlin/Compose reference client** and this design lab; it is not evidence that an installable PWA has been published.
- The canonical greenfield web preview lives in the private agent-system project's `playbook/toolkit/zcode-pocket-web/`. It has a simulated scenario player, no live hub connection, and a disabled Send button. It has **no PWA manifest or service-worker install/update flow in the inspected tree**.
- There is a design twin in `playbook/toolkit/zcode-pocket/twin/`. Preserve it as a fast visual experiment surface, but the **installed phone PWA** becomes the final arbiter.
- Prior local evidence shows the adopted ACP hub can execute, stream, cancel and resume a real conversation; this does **not** prove phone access.
- The adopted ACP hub does not offer directly selecting a named roster agent over the current ACP session API. The first client must never falsely label an undifferentiated session as `Chief of Staff` until identity/routing is actually established.
- Public repo decision log confirms PWA forward, design twin retained, and UI constrained to bot interaction. No claim is made about which processes are *running now*.

## 2. Workbook rules

1. **Exactly one user-facing product behavior per iteration.** Connection/security scaffolding may be packaged with a release if required for that behavior.
2. **See it on the phone, then choose the next step.** Desktop previews and screenshots are support, not release acceptance.
3. **A real event or nothing.** No simulated typing, fake bot presence, synthetic tool work, misleading `sent` or `done` states in the live build. Simulation is allowed only in an unmistakably labeled design-lab preview.
4. **Do not throw away the prior working build.** Stable link, tagged/commit-addressable releases, tested rollback. Distinguish proposed, built, published, installed, observed and owner-accepted.
5. **The front page stays conversation-first.** Add navigation only once a real use case demands it. Warm, recognizable, expressive *without* an obligatory corporate console.
6. **Privacy-first by default.** No real conversations or device identifiers in this public repo. Never commit hub tokens or screenshots showing private material. Don't reuse a full-control bearer token in browser JS.
7. **The one-agent proof is essential.** A named bot roster and group-room appearance should not hide an inability to direct and receive an actual local response.
8. **One consolidated main.** Quick experiments can branch briefly, but only accepted, verified surfaces ship on the stable phone URL.

## 3. Working release ladder (only the next rung is executable scope)

| Cut | One new experience | Phone-visible acceptance | Readiness |
| --- | --- | --- | --- |
| **P0 — Reach & install** | Open the private HTTPS URL; install the tiny shell; see `Connected/Offline` accurately; see real build ID and `Update available` when changed | Open from Android Chrome; install on home screen; relaunch; publish a text/color change and apply it without reinstallation; preserve draft on update | **FIRST GATE; needs local validation** |
| **P1 — One real agent** | Type one message to one known real local session; receive incremental, attributable response | User sends from installed PWA, machine records prompt, streamed output arrives, cancel/refresh/reconnect behave honestly; no mock data in live mode | **FIRST USEFUL RELEASE; blocked on P0 connection/security** |
| P2 — Enjoyable conversation | Refine only what annoys the owner after using P1: composer, scroll, message spacing, status or character | Owner picks one visible before/after improvement; live chat still works | Proposed |
| P3 — Conversation continuity | Return to the same chat later; draft and location survive interruption | Reopen app/reconnect and see exact conversation or an honest unavailable state; no duplicate send | Proposed |
| P4 — First workspace | Enter or switch **one** actual workspace/context | User knows which project they are talking about; change is explicit and sourced | Proposed |
| P5 — Second agent | Switch between two verified, distinct agent identities/sessions | Messages are correctly attributed and the first session is unchanged | Proposed; named routing must be proved |
| P6 — Multi-agent visibility | Watch a **real** handoff or observed parallel contribution | Show who did what from actual events, not imitation | Proposed |
| P7 — Conversation room | Distinct multi-bot conversation if the local system actually supports group addressing and attribution | User can observe multiple legitimate participants, not a cosmetic group | Proposed |
| P8 — Deeper organization | Optional activity, approvals, useful status and backstage | Drill down without cluttering direct conversation | Proposed |
| Later | Voice, attachments, notifications, share intake, richer local snapshots, Saved Views | Admit only when user wants the *next* specific behavior; prior parking remains | Parked/optional |

**P0 and P1 are one minimum viable journey**, not an excuse to spend multiple cycles on a disconnected shell. P0 is a prerequisite, not a product release to celebrate if actual chat remains inaccessible. The fastest test is a plain real message, even if the UI is aesthetically crude.

## 4. P0 experiment card — Stable installed phone playground

**One question:** Can I reliably open, install and update the very same private phone surface while away from the computer?

- Only one screen: name/identity (or honest generic ZCode label), conversation area, composer, tiny connection state, build ID.
- Private HTTPS origin accessible from phone on the tailnet, not public exposure of privileged agent endpoints.
- PWA manifest, icons and an appropriate service-worker update strategy; don't interrupt an active turn to force reload.
- Deploy/restart a newer build; indicate and apply update on user action or safe idle transition; keep composer draft.
- Authentic offline/error state (not "agent is typing" while disconnected).
- Local team's handoff supplies exact working URL, build identifier, install instruction, and tested rollback.

**Pass:** owner can install and see a new visual change in the installed app with no APK, no new URL and no manual data reset.  
**Fail:** works only on localhost, requires copying privileged secrets into the browser, or a public URL that silently cannot contact the private machine.

## 5. P1 experiment card — A real single-agent conversation

**One question:** Can I do from this app the essential thing I currently do in ChatGPT—send natural language and receive a real, live answer—except from my own local ZCode system?

- Exactly one authenticated, named session whose actual identity the backend confirms; if that is generic, label it generic.
- Show user text, server acknowledged receipt, streaming assistant text, completed/failed/cancelled; distinguish each.
- A very small set of controls: type, send, stop; new conversation/recover only if required by the real workflow.
- No broad bot directory, no fake multiple agents, no routine scheduler, no machine dashboard, no settings marketplace.
- Phone acceptance includes: one normal turn; one deliberate cancellation; one reopen and resume; one unplugged/Tailscale-off scenario showing truthful recovery.
- Check that server-side prompt and response can be matched to same session, without exposing transcript contents in public build logs.

**Pass:** user independently sends a sentence while away, sees a real stream, and later resumes the same context with accurate status.  
**Fail:** merely loads chat history, simulates a response, writes `accepted` with no model result, or cannot select a real session.

## 6. Design experimentation on the actual phone

After P1, change **one variable** per run. Candidate dimensions, not selected requirements:

- Header: bot portrait + name vs minimalist identity pill.
- Composer: ChatGPT-like one-handed input vs compact messenger bubble.
- Streaming: live text vs an unobtrusive `Working` label while waiting for the first chunk.
- Activity: single compact status line vs a collapsible true event card.
- Character: subtle avatar/color/style (personality without faux emotion).
- Navigation: keep single-chat surface until actual second conversation or second bot exists.

The current design twin may preview two variants quickly. Only the favorite goes to the next PWA cut. **Never redesign five screens before receiving actual phone feedback.**

### Micro-feedback record (fill together in ChatGPT)

```yaml
trial_id: phone-P1-001
phone_build: null
actual_phone_tested: false
task: "Talk to one real ZCode session"
worked: []
friction: []
unexpected: []
owner_preference: null
change_next: null   # one behavior, not a wish list
decision: proposed  # proposed | accepted | rejected | parked
evidence: []         # tested URL/commit/log pointers, no private text
```

The decision log stores explicit owner-selected choices, not the assistant's favorite design.

## 7. Evidence and state for every cut

A cut is not shipped because files were committed. Report **separately**:

`designed → implemented → tests_passed → hosted_reachable → installed_phone → observed_working → owner_accepted`.

The local team should return: exact commit/rollback, URL location (private), build/version stamp, machine/client tests, any unverified endpoints, screenshots *only in private storage*, and failures. Recheck from ChatGPT where the connected repo permits; I cannot infer phone runtime from Git alone.

Baseline: the old preview uses simulated data; the private hub/adapter must be proven via the **real phone**. Automated CI never substitutes for that acceptance.

## 8. Working-session ritual (no scheduler implied)

**Start:** Read current release card and last accepted owner feedback. Identify **one** next behavior.

**Explore:** Brainstorm quickly here. Sketch or compare only enough to decide what to test; retain alternatives as `proposed`.

**Handoff:** Issue one bounded implementation prompt to the existing ZCode custodian; don't appoint new agents or rewrite infrastructure.

**Observe:** Open the installed app on the Android phone, test that behavior and give natural-language feedback. A visual or interaction annoyance becomes the next experiment, not a mass feature backlog.

**Reconcile:** Record acceptance/rejection, bug evidence and a single next change. Separate changes to the product from required corrections to the connection layer.

**Stop condition:** If phone connection or live-agent truth is broken, fix that before decorating or expanding the interface.

## 9. Open questions — intentionally not pre-decided

- Which actual local bot/session can first be addressed and verified? Generic headless ZCode session is an honest fallback; `Chief` is a user-experience goal, not a transport fact.
- Do we want an almost blank chat or a subtle character identity for the first design experiment?
- Which workspaces should become selectable **after** one-chat continuity works?
- Should multiple bots show separate chats, group rooms or handoff events? Test with real traffic first.
- How should voice and share-sheet intake appear after the owner asks to promote them from parked?
- Can the installed app receive incremental UI bundles without interrupting a live stream, and can the same build be rolled back if it fails?

## 10. Linked source materials

- `product-design/00-scope/NORTH-STAR.md` and `product-design/04-decisions/DECISION-LOG.md` — owner decisions.
- `product-design/01-bots/BOT-INTERACTION-v0.1.md` — UX features to selectively test, not build all at once.
- `product-design/02-references/GROK-BOT-MOBILE-INVENTORY-v0.1.md` — reference only.
- `product-design/archive/personal-bot-pwa-design-baseline-v0.1..v0.3.md` — historical advanced data/sync/intake hypotheses, not immediate build scope.
- Private source-of-truth for the live system, as of 2026-10-13: `playbook/toolkit/zcode-pocket-web/`, `playbook/toolkit/zcode-pocket/twin/`, `playbook/toolkit/pocket-chain/`, `playbook/toolkit/pocket-link/`, and `research/briefs/050..052` in `zcode-setup`. This public workbook contains no private network details.

**Current selected experiment:** **P0 → P1**; get a stable installed private PWA and one genuine agent conversation. Design everything after that by actual usage, not by prediction.
