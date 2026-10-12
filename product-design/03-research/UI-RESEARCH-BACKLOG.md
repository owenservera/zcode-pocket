# UI Research & Reuse Backlog

**Research / proposal only. No dependency approval.** Never change the bot/server runtime here.

## Highest priority

- Mobile messenger composition: text/voice/attachment, interruption-safe draft behavior, safe keyboard insets, accessibility.
- Distinct bot avatars/persona grammar; status-driven (rather than fabricated) micro-animations.
- Groups and multi-bot conversations, including attributions and compact handoff progress.
- Scalable directory: hundreds of specialists with fast one-tap access to familiar bots.
- Progressive disclosure from inline activity to detailed observable trace.
- Android-first navigation and menu tests; tall phone screens, small phones, gesture/nav safe areas, reduced motion.
- Contrast and comparison against current native Jetpack Compose app rather than assuming PWA implementation.

## UI component/reference candidates (PROPOSED)

- https://www.assistant-ui.com/ — chat interaction patterns; assess whether web-only ideas translate to Compose.
- https://docs.copilotkit.ai/ — agent progress/interaction patterns; study UX, not server integration.
- https://ui.shadcn.com/ — component/animation reference for potential web implementation.
- https://tanstack.com/virtual/latest — large transcript rendering reference if web is chosen.
- Android Jetpack Compose Material 3 documentation — **current implementation stack**: https://developer.android.com/develop/ui/compose/designsystems/material3

Compare: UX quality, usability tests, customization, accessibility, licenses, maturity, performance, dependency cost, and fit to *current actual platform*. Do not adopt without a written decision.

## First research output requested

Three low-fidelity chat-screen alternatives with clear assessment of direct bot switch, voice/attachments, actual bot status, approvals, group participation, Backstage discoverability, keyboard safety. Follow with a user-facing decision, not an automatic code rewrite.
