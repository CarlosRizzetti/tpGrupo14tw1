import { deleteTimer } from "./deleteTimer.js";
import { renovarTimer } from "./renovarTimer.js";
import { importTimer, executeImport, closeImportModal } from "./importarTimer.js";

document.addEventListener("click", (e) => {
  const btn = e.target.closest("[data-action]");
  if (!btn) return;

  e.preventDefault();

  const { action, timerId, categoryId, productName, location } = btn.dataset;

  switch (action) {
  case "delete":
    deleteTimer(timerId);
    break;

  case "renew":
    renovarTimer(timerId, categoryId);
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