import assert from "node:assert/strict";
import fs from "node:fs";import path from "node:path";import {fileURLToPath} from "node:url";
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),"..");
const get=name=>fs.readFileSync(path.join(root,name));
const html=get("index.html").toString(),js=get("main.js").toString(),sw=get("sw.js").toString();
const manifest=JSON.parse(get("manifest.webmanifest").toString());
assert.equal(manifest.display,"standalone");assert.equal(manifest.scope,"./");
assert.deepEqual(manifest.icons.map(i=>i.sizes),["192x192","512x512"]);
for(const name of ["index.html","main.js","styles.css","sw.js","manifest.webmanifest","icon.svg","icon-192.png","icon-512.png"])assert.ok(fs.existsSync(path.join(root,name)),name);
for(const size of [192,512]){const png=get(`icon-${size}.png`);assert.equal(png.subarray(0,8).toString("hex"),"89504e470d0a1a0a");assert.equal(png.readUInt32BE(16),size);assert.equal(png.readUInt32BE(20),size);}
assert.match(js,/localStorage\.setItem/);assert.match(js,/serviceWorker\.register/);assert.match(js,/SKIP_WAITING/);assert.match(sw,/caches\.open/);
assert.match(html,/id="send" disabled/);assert.match(html,/not a working chat yet/i);
assert.doesNotMatch(js,/fetch\(|new WebSocket|Authorization|Bearer/);
assert.ok(js.includes("p0.1.0")&&sw.includes("p0.1.0"));
console.log("P0 SELFTEST PASS — assets, PNG icons, manifest, draft, update, truth labels");
