function goToStep2() {
  const step1 = document.getElementById("modal-step-1");
  const step2 = document.getElementById("modal-step-2");
  if (!step1 || !step2) return;
  step2.style.transform = "translateX(0)";
}

function goToStep1() {
  const step2 = document.getElementById("modal-step-2");
  if (!step2) return;
  step2.style.transform = "translateX(100%)";
}

function getSelectedCategoryId() {
  const select = document.getElementById("modal-category-select");
  return select?.value || null;
}

function showSelectError(show) {
  const el = document.getElementById("modal-select-error");
  if (!el) return;
  el.classList.toggle("hidden", !show);
}

function showCantidadError(show, msg = null) {
  const el = document.getElementById("modal-cantidad-error");
  if (!el) return;
  if (msg) el.textContent = msg;
  el.classList.toggle("hidden", !show);
}

// ─────────────────────────────────────────────
//  Cargar categorías al abrir el modal
// ─────────────────────────────────────────────

export async function importTimer(timerId, productName, location) {
  document.body.style.cursor = "wait";

  try {
    const response = await fetch(`/timers/${timerId}/categories`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
      },
    });

    const data = await response.json();
    if (!response.ok) throw new Error("Error al cargar categorías");

    openImportModal(timerId, data.categorias, productName, location, data.cantidad);
  } catch (error) {
    console.error(error);
    alert("Error al obtener las categorías disponibles.");
  } finally {
    document.body.style.cursor = "default";
  }
}

// ─────────────────────────────────────────────
//  Abrir modal y poblar select
// ─────────────────────────────────────────────

export function openImportModal(timerId, categories, productName, location, cantidadDisponible) {
  const select       = document.getElementById("modal-category-select");
  const modal        = document.getElementById("import-modal");
  const nameDisplay  = document.getElementById("modal-product-name");
  const locDisplay   = document.getElementById("modal-product-location");
  const hiddenId     = document.getElementById("modal-timer-id");
  const hiddenCant   = document.getElementById("modal-cantidad-disponible");
  const cantBadge    = document.getElementById("modal-cantidad-badge");

  if (nameDisplay) nameDisplay.textContent = productName;
  if (locDisplay)  locDisplay.textContent  = location;
  if (hiddenId)    hiddenId.value          = timerId;
  if (hiddenCant)  hiddenCant.value        = cantidadDisponible ?? 0;
  if (cantBadge)   cantBadge.textContent   = cantidadDisponible ?? 0;

  // Resetear al paso 1 al abrir
  goToStep1();
  showSelectError(false);
  showCantidadError(false);

  const cantInput = document.getElementById("modal-cantidad-input");
  if (cantInput) cantInput.value = "";

  // Limpiar y poblar el select
  select.innerHTML = `<option value="">— Elegí una categoría —</option>`;

  if (categories && categories.length > 0) {
    categories.forEach((cat) => {
      const option = document.createElement("option");
      option.value = cat.id;
      option.textContent = cat.nombre;
      if (cat.estaPresente) {
        option.disabled = true;
        option.textContent += " (ya existe)";
      }
      select.appendChild(option);
    });
  } else {
    const option = document.createElement("option");
    option.disabled = true;
    option.textContent = "Sin categorías disponibles";
    select.appendChild(option);
  }

  modal.classList.remove("hidden");
  initModalListeners();
}

// ─────────────────────────────────────────────
//  Listeners internos del modal (se registran
//  una sola vez gracias al flag)
// ─────────────────────────────────────────────

let listenersInit = false;

function initModalListeners() {
  if (listenersInit) return;
  listenersInit = true;

  // Volver al paso 1
  document.getElementById("btn-back-step1")?.addEventListener("click", () => {
    goToStep1();
    showCantidadError(false);
  });

  // Importar TOTALIDAD
  document.getElementById("btn-import-total")?.addEventListener("click", () => {
    const categoryId = getSelectedCategoryId();
    if (!categoryId) { showSelectError(true); return; }
    showSelectError(false);

    const timerId  = document.getElementById("modal-timer-id")?.value;
    const cantidad = parseInt(document.getElementById("modal-cantidad-disponible")?.value, 10);
    executeImport(timerId, categoryId, cantidad);
  });

  // Ir al paso 2 (cantidad personalizada)
  document.getElementById("btn-import-custom")?.addEventListener("click", () => {
    const categoryId = getSelectedCategoryId();
    if (!categoryId) { showSelectError(true); return; }
    showSelectError(false);
    goToStep2();
  });

  // Confirmar importación con cantidad personalizada
  document.getElementById("btn-confirm-custom-import")?.addEventListener("click", () => {
    const timerId    = document.getElementById("modal-timer-id")?.value;
    const categoryId = getSelectedCategoryId();
    const disponible = parseInt(document.getElementById("modal-cantidad-disponible")?.value, 10);
    const cantidad   = parseInt(document.getElementById("modal-cantidad-input")?.value, 10);

    if (!cantidad || cantidad <= 0) {
      showCantidadError(true, "Ingresá una cantidad válida");
      return;
    }
    if (cantidad > disponible) {
      showCantidadError(true, `No podés importar más de ${disponible} unidades`);
      return;
    }
    showCantidadError(false);
    executeImport(timerId, categoryId, cantidad);
  });
}

// ─────────────────────────────────────────────
//  Ejecutar el import contra el backend
// ─────────────────────────────────────────────

export async function executeImport(timerId, categoryId, cantidad) {
  document.body.style.cursor = "wait";

  try {
    const response = await fetch(`/import-timer/${timerId}/${categoryId}/${cantidad}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
      },
    });

    const data = await response.json();

    if (data.success && data.message === "Timer importado correctamente") {
      window.location.href = "/dashboard";
    } else {
      throw new Error(data.message || "Error desconocido al importar");
    }
  } catch (error) {
    console.error(error);
    alert(error.message);
  } finally {
    document.body.style.cursor = "default";
  }
}

// ─────────────────────────────────────────────
//  Cerrar modal
// ─────────────────────────────────────────────

export function closeImportModal() {
  const modal = document.getElementById("import-modal");
  if (modal) modal.classList.add("hidden");
  document.body.style.cursor = "default";
}