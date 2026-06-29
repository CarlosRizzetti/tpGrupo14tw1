import { deleteTimer } from "./deleteTimer.js";
import { importTimer, executeImport, closeImportModal } from "./importarTimer.js";
import { closeRenewModal, openRenewModal } from "./renovarTimer.js";

document.addEventListener("click", (e) => {
  const btn = e.target.closest("[data-action]");
  if (!btn) return;

  e.preventDefault();

  const { action, timerId, categoryId, productName, location } = btn.dataset;

  switch (action) {
  case "delete":
    deleteTimer(timerId);
    break;

  case "renew": {
    const cantidadRenovar = btn.dataset.cantidad;
    openRenewModal(timerId, productName, location, cantidadRenovar);
    break;
  }

  case "import": {
    const cantidad = btn.dataset.cantidad;
    importTimer(timerId, productName, location, cantidad);
    break;
  }

  case "confirm-import":
    executeImport(timerId, categoryId);
    break;

  case "closeImportModal":
    closeImportModal();
    break;

  case "closeRenewModal":
    closeRenewModal();
    break;

  default:
    console.warn("Acción no reconocida:", action);
  }
});