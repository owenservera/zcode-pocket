// Bump cache name and BUILD for each new version.
const CACHE="zcode-pocket-p0.1.0";
const ASSETS=["./index.html","./main.js","./styles.css","./manifest.webmanifest","./icon.svg","./icon-192.png","./icon-512.png"];
self.addEventListener("install",event=>event.waitUntil(caches.open(CACHE).then(cache=>cache.addAll(ASSETS))));
self.addEventListener("activate",event=>event.waitUntil(Promise.all([caches.keys().then(keys=>Promise.all(keys.filter(k=>k.startsWith("zcode-pocket-")&&k!==CACHE).map(k=>caches.delete(k)))),self.clients.claim()])));
self.addEventListener("message",event=>{if(event.data?.type==="SKIP_WAITING")self.skipWaiting();});
self.addEventListener("fetch",event=>{
  const req=event.request;if(req.method!=="GET")return;
  const url=new URL(req.url),root=new URL(self.registration.scope);
  if(url.origin!==root.origin||!url.pathname.startsWith(root.pathname))return;
  if(url.pathname.includes("/api/")||url.pathname.includes("/acp"))return;
  if(req.mode==="navigate"){event.respondWith(fetch(req).then(res=>{
    if(res.ok){const copy=res.clone();caches.open(CACHE).then(c=>c.put(req,copy)).catch(()=>{});}return res;
  }).catch(()=>caches.match("./index.html")));return;}
  event.respondWith(caches.match(req).then(hit=>hit||fetch(req)));
});
