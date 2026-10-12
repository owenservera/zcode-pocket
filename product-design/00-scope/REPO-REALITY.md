# Current Repository Reality — 2026-10-12

Source: checked `owenservera/zcode-pocket` at commit `79946869aca89a882e0cb8087f44102aef75baa6`. This note is **REPO-OBSERVED**, not an implementation request.

- Root `README.md` identifies the project as **ZCode Pocket**, Android client for ZCode ACP hub.
- `app/build.gradle.kts`, `app/src/main/java/app/zcode/pocket/` and `app/src/main/AndroidManifest.xml` indicate a **native Kotlin / Jetpack Compose Android APK** project, not a browser-installed PWA in the source currently published.
- `ui/PocketApp.kt` defines Chats, Projects, Agents, Machine, Settings and Chat destinations, with four bottom navigation tabs (Chats, Projects, Agents, Machine). Settings uses a separate screen.
- `ui/ChatScreen.kt` renders a streaming conversational timeline, activity/tool cards, plans, usage bar, slash-command hints, text composer, stop/send control and a permission sheet.
- `ui/AgentsScreen.kt` lists agents and metadata.
- `ui/SettingsScreen.kt` displays hub URL, token and connection test controls.
- This examination did NOT verify runtime behaviors or Android usability.

## Platform mismatch — OPEN

Our earlier conversation described a personal **PWA**; the repository the owner just published is a **native Android Compose project**. Both facts are recorded. We do not assume a migration, dual client, wrapper, or platform switch. **Keep designing platform-neutral frontend interactions and ask the owner/local Android team to decide implementation alignment separately.**

## Boundary

This design lab may map screen concepts to existing UI sources, but must NOT alter the Android code or server while authoring design/research documents. Existing app functionality takes precedence over exploratory design archives.
