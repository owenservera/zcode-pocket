# Bot UI Feature Matrix — First Pass

Labels: REFERENCE-OBSERVED = seen in Grok mobile screenshots. REPO-OBSERVED = found in current Android source. PROPOSED = not an approved feature. UNKNOWN = no conclusion.

| User-facing feature | Grok screenshot baseline | ZCode Pocket current repository | Design direction |
|---|---|---|---|
| Conversation list/inbox | REFERENCE-OBSERVED | REPO-OBSERVED: Chats/Sessions screen exists | Keep easy access; refine UX |
| Search / new conversation | REFERENCE-OBSERVED | UNKNOWN pending deeper UI audit | Explore |
| Direct bot conversation | REFERENCE-OBSERVED | REPO-OBSERVED: ChatScreen exists | First active screen |
| Group conversations / overlapping avatars | REFERENCE-OBSERVED | UNKNOWN | Prototype separately |
| Named member attribution / bot-to-bot summaries | REFERENCE-OBSERVED | REPO-OBSERVED: transcript/tool/plan items; exact peer attribution UNKNOWN | Explore event-aware design |
| Chat text composer | REFERENCE-OBSERVED | REPO-OBSERVED | Improve one-handed ergonomics |
| Microphone/voice entry | REFERENCE-OBSERVED | Not observed in current ChatScreen source | Interface concept only |
| Choose file / take photo / attach image | REFERENCE-OBSERVED | Not observed in current ChatScreen source | Interface concept only |
| Conversation overflow/profile | REFERENCE-OBSERVED | UNKNOWN in current chat screen | Study menu affordances |
| Group info: members, document/link/media/file tabs, routines | REFERENCE-OBSERVED | UNKNOWN | Later UI design candidate |
| Unread markers/timestamps | REFERENCE-OBSERVED | UNKNOWN in current chat list source | Baseline navigation |
| Approval action | REFERENCE-OBSERVED | REPO-OBSERVED: PermissionSheet exists | Show clearly, no new permission logic |
| Tool/status/plan progress display | REFERENCE-OBSERVED for collaboration labels | REPO-OBSERVED: tool/plan and status in timeline | Progressive disclosure |
| Bot identity/personality/animation | REFERENCE-OBSERVED: colorful avatar language | UNKNOWN | PROPOSED, no fake activity |
| Plugin marketplace / account/settings | REFERENCE-OBSERVED | REPO-OBSERVED: settings exists; marketplace UNKNOWN | Out of bot-ui v1 core |

This matrix is a **screen and design inventory**, not a promise that reference controls or backend capabilities work.
