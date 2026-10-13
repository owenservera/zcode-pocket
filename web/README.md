# P0 — Installable phone shell
The source is ready; private HTTPS deployment, Android installation, and real ZCode messages have **NOT** been verified. No API calls, privileged secrets, or simulated agent activity are present. Send is intentionally disabled until P1.

Canonical playbook source: private `zcode-setup/playbook/toolkit/zcode-pocket-web/web/app/`. This public `zcode-pocket/web/` is a deliberate reviewed source mirror, not a separate backend. Never add tokens, private device names, IP addresses, or transcripts.

Run in the public repo: `python -m http.server 8080 --directory web`, open `http://localhost:8080`. For the canonical private repo, existing `node playbook/toolkit/zcode-pocket-web/server.mjs preview` serves the new shell at `http://127.0.0.1:8391/app/index.html` beside the original simulated preview. Both are development previews, not Android deployment.

Test public files: `node web/tests/p0-selftest.mjs`. Test canonical files: `node playbook/toolkit/zcode-pocket-web/web/app/tests/p0-selftest.mjs`.

To release a new build: bump BUILD in `main.js` and CACHE in `sw.js`; publish over the **same private HTTPS origin**, test that the existing installed app offers `Update app`, then confirm the draft survives. Never force mid-message reload. Local draft storage can still be cleared by the browser.

Next: authenticated private-origin admission and an actual one-agent ACP turn, per the phone-first workbook. Do not confuse this front-end scaffold with a working local-agent chat.
