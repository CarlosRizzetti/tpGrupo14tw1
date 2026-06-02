import { deleteTimer } from "./deleteTimer.js";
// import { renewTimer } from "./renewTimer.js";
import { importTimer, executeImport, closeImportModal } from "./importarTimer.js";

document.addEventListener("click", (e) => {
  const btn = e.target.closest("[data-action]");
  console.log(btn);
  if (!btn) return;

  e.preventDefault();

  const { action, timerId, categoryId, productName, location } = btn.dataset;

  switch (action) {
  case "delete":
    deleteTimer(timerId, categoryId);
    break;

  case "renew":
    renewTimer(timerId, categoryId);
    break;

  case "import":
    importTimer(timerId, productName, location);
    break;

  case "confirm-import":
    executeImport(timerId, categoryId);
    break;

  case "closeImportModal":
    closeImportModal();
    break;

  default:
    console.warn("Acción no reconocida:", action);
  }
});