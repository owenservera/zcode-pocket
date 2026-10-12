# Grok Bot Mobile App — Observed UI Reference Inventory v0.1

Date: 2026-10-12
Source: ten screenshots provided by user. This document records UI surfaces and visible affordances only. It does **not** prescribe or investigate bot infrastructure, models, agent execution, orchestration, backend APIs, permissions implementation, or plugin wiring. Assume the user's local ZCode/Oracle bot system is already operational.

## Scope of the design thread

- Design a personal Android PWA's front-end bot interaction experience: navigation, layout, identity and personality, conversations, group chats, visible status, user controls, and progressive disclosure of advanced activity.
- The screenshots are a feature/menu reference to help identify useful user-facing affordances, **not** a visual style mandate or a request to rebuild Grok Bot's backend.
- Distinguish **visible UI** from **confirmed working behavior**. Screenshots establish presence of controls, not their technical functionality.
- Park Saved Views; design the bots-first PWA surface for now.

## Screen-by-screen observed inventory

1. **Push notification onboarding:** Full-screen push notification request, illustrated bot/app mark with unread badge, “Later” and “Enable Notifications” buttons, overflow icon.
2. **Conversation list / home:** Profile/app icon; search and new (+) buttons; list of individual bots and named group chats; distinctive colorful bot avatars; latest message previews; timestamps; unread dots; floating “10 unread” navigation affordance.
3. **Direct bot conversation:** Back button; compact bot identity/title pill and overflow menu; conversational message bubbles; system/context labels (“Message from …”, “Messaged … 2 Bots”); an approval-required notice/action; outgoing user bubble; bottom composer with add (+), microphone and prominent voice interaction control.
4. **Direct bot chat overflow menu:** “View Profile”, “Share as Template”, and “Delete”.
5. **Composer attachment menu:** “Choose File”, “Take Photo”, and “Attach Image”.
6. **Group conversation:** Back button; overlapping member-avatar title pill; message author label; received bubbles; “NEW” marker/unread separator; composer and microphone.
7. **Group profile/details:** Large group avatars and name; horizontal category tabs — Info, Documents, Links, Media, Files (some horizontally clipped); participant/member list with drill-down chevrons; “Add member”; Routines section with empty state and “Add routine”.
8. **Group overflow menu:** “Copy ID” and “Delete Group Chat”.
9. **Account/settings:** Account identity; usage indicator; “Connect apps”; Auto-review toggle with explanatory text; Auto-review Rules; automatic time zone toggle; Time Zone value; Bot Computer; Notifications toggle (lower portion may continue offscreen).
10. **Plugin marketplace:** Back button; marketplace heading; installed-count chip; search field; filter button; “Featured” listings and “View all”; item logos/descriptions; “Add” and “Authorize” controls; category heading for Agent Orchestration.

## UI features represented across the screenshots

- Navigation: chats list, direct conversation, group conversation, profiles/details, settings, and plugin browsing.
- Identity: single bot avatars, composite group avatars, identifiable sender names, chat titles and profile destinations.
- Communication: text bubbles, unread state, voice entry affordances, photo/image/file attachment affordances, and bot-to-bot activity indicators in the transcript.
- Intervention: approval prompt/allow action visible in a conversation; settings UI exposes approval-related controls. This inventory makes **no assumption** about the enforcement backend.
- Content organization: group members, document/link/media/file tabs and routines exposed via group detail UI.
- Extensibility visibility: integrations marketplace and installed count, as **frontend reference only**.

## Initial UX observations (proposals, not approved requirements)

- **Keep:** Avatar-led bot/group recognition; easy switching from inbox to conversation; persistent composer; one-tap media and voice entry; sender attribution in multibot conversations; small unread indicators.
- **Consider:** Clearer, compact progress/handoff cards and an optional Backstage drill-down to inspect bot collaboration without surfacing verbose orchestration by default.
- **Avoid:** Making the everyday chat UI feel like a control console, hiding high-frequency interactions under multiple menus, or requiring users to inspect technical configuration.
- **Check in future UI testing:** Legibility of long group names; discoverability of side tabs and menus; handling of long bot outputs; visibility and distinction of approval states; one-handed voice use; accessibility and reduced-motion support.

## Open UI-only questions

- Default entry: direct Chief of Staff conversation vs inbox.
- How to visually distinguish bot personality, presence, and actual working state without implying unsupported activity.
- Whether multiple active bots should appear as stacked avatars, a participant strip, or inline handoff messages.
- Which advanced surfaces are essential in release one: bot profile, group detail, activity drawer, or all three.

## Explicit non-goals

No bot creation, bot roles/prompts, tools/MCP setup, memory engine, model selection, infrastructure, Oracle/ZCode design, database/sync design, or backend permissions design in this UI review. UI may display statuses and controls supplied by the existing backend, with interface contracts to be mapped later by the local implementation agent.