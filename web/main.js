// P0 only: no hub transport, secret, send, or simulated conversation.
const BUILD = "p0.1.0";
const STORAGE = "zcode-pocket:draft:p0";
const $ = s => document.querySelector(s);
const draft = $("#draft"), status = $("#draft-status"), connection = $("#connection");
$("#build").textContent = BUILD;
function readDraft(){try{return localStorage.getItem(STORAGE)||""}catch{return ""}}
function saveDraft(){try{localStorage.setItem(STORAGE,draft.value);return true}catch{return false}}
draft.value=readDraft();
draft.addEventListener("input",()=>{status.textContent=saveDraft()?"Draft saved on this device":"Storage unavailable; copy before leaving";});
function networkHint(){connection.textContent=navigator.onLine?"Agent not connected yet":"Offline · draft available";}
addEventListener("online",networkHint);addEventListener("offline",networkHint);networkHint();
let waiting=null;
function showUpdate(worker){if(!worker)return;waiting=worker;$("#update").hidden=false;}
$("#apply").addEventListener("click",()=>{if(!waiting)return;if(!saveDraft()){status.textContent="Copy draft before updating";return;}waiting.postMessage({type:"SKIP_WAITING"});});
if("serviceWorker" in navigator && isSecureContext){
  navigator.serviceWorker.addEventListener("controllerchange",()=>location.reload());
  addEventListener("load",async()=>{
    try{
      const reg=await navigator.serviceWorker.register("./sw.js",{scope:"./",updateViaCache:"none"});
      if(reg.waiting&&navigator.serviceWorker.controller)showUpdate(reg.waiting);
      reg.addEventListener("updatefound",()=>{
        const worker=reg.installing;if(!worker)return;
        worker.addEventListener("statechange",()=>{if(worker.state==="installed"&&navigator.serviceWorker.controller)showUpdate(worker);});
      });
      reg.update().catch(()=>{});
    }catch(e){console.warn("PWA install/cache unavailable",e);}
  });
}
