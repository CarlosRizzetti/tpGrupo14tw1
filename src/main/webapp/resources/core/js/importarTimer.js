const $ = (id) => document.getElementById(id);

const ELEMENTS = {
  modal:           () => $("import-modal"),
  step2:           () => $("modal-step-2"),
  productName:     () => $("modal-product-name"),
  productLocation: () => $("modal-product-location"),
  timerId:         () => $("modal-timer-id"),
  cantidadDisp:    () => $("modal-cantidad-disponible"),
  cantidadBadge:   () => $("modal-cantidad-badge"),
  cantidadInput:   () => $("modal-cantidad-input"),
  categorySelect:  () => $("modal-category-select"),
  selectError:     () => $("modal-select-error"),
  cantidadError:   () => $("modal-cantidad-error"),
  selectedCatId:   () => $("modal-selected-category-id"),
};

// ─────────────────────────────────────────────
//  Helpers de navegación entre pasos
// ─────────────────────────────────────────────

const goToStep2 = () => {
  ELEMENTS.step2().style.transform = "translateX(0)";
};

const goToStep1 = () => {
  ELEMENTS.step2().style.transform = "translateX(100%)";
};

// ─────────────────────────────────────────────
//  Helpers de validación y UI
// ─────────────────────────────────────────────

const getSelectedCategoryId = () => ELEMENTS.categorySelect()?.value || null;

const showError = (element, show, msg = null) => {
  if (!element) return;
  if (msg) element.textContent = msg;
  element.classList.toggle("hidden", !show);
};

const showSelectError   = (show, msg = null) => showError(ELEMENTS.selectError(),   show, msg);
const showCantidadError = (show, msg = null) => showError(ELEMENTS.cantidadError(), show, msg);

const resetModal = () => {
  goToStep1();
  showSelectError(false);
  showCantidadError(false);

  const cantInput = ELEMENTS.cantidadInput();
  if (cantInput) {
    cantInput.value = "";
    cantInput.max   = "";
  }
};

const buildCategoryOptions = (categories) => {
  const select = ELEMENTS.categorySelect();
  select.innerHTML = `<option value="">— Elegí una categoría —</option>`;

  if (!categories?.length) {
    const empty = new Option("Sin categorías disponibles");
    empty.disabled = true;
    select.appendChild(empty);
    return;
  }

  categories.forEach(({ id, nombre, estaPresente }) => {
    const option    = new Option(estaPresente ? `${nombre} (ya existe)` : nombre, id);
    option.disabled = estaPresente ?? false;
    select.appendChild(option);
  });
};

// ─────────────────────────────────────────────
//  Cargar categorías desde el backend
// ─────────────────────────────────────────────

export const importTimer = async (timerId, productName, location, cantidad) => {
  document.body.style.cursor = "wait";

  try {
    const response = await fetch(`/timers/${timerId}/categories`, {
      method: "GET",
      headers: { "Content-Type": "application/json", Accept: "application/json" },
    });

    if (!response.ok) throw new Error("Error al cargar categorías");

    const { categorias } = await response.json();
    openImportModal(timerId, categorias, productName, location, cantidad);

  } catch (error) {
    console.error("[importTimer]", error);
    alert("Error al obtener las categorías disponibles.");
  } finally {
    document.body.style.cursor = "default";
  }
};

// ─────────────────────────────────────────────
//  Abrir modal y poblar datos
// ─────────────────────────────────────────────

export const openImportModal = (timerId, categories, productName, location, cantidadDisponible) => {
  const cantidad = cantidadDisponible ?? 0;

  ELEMENTS.productName().textContent     = productName;
  ELEMENTS.productLocation().textContent = location;
  ELEMENTS.timerId().value               = timerId;
  ELEMENTS.cantidadDisp().value          = cantidad;
  ELEMENTS.cantidadBadge().textContent   = cantidad;
  ELEMENTS.cantidadInput().max           = cantidad;

  resetModal();
  buildCategoryOptions(categories);

  ELEMENTS.modal().classList.remove("hidden");
  initModalListeners();
};

// ─────────────────────────────────────────────
//  Listeners del modal (se registran una sola vez)
// ─────────────────────────────────────────────

let listenersInit = false;

const initModalListeners = () => {
  if (listenersInit) return;
  listenersInit = true;

  // Volver al paso 1
  $("btn-back-step1")?.addEventListener("click", () => {
    goToStep1();
    showCantidadError(false);
  });

  // Validación en tiempo real de la cantidad
  $("modal-cantidad-input")?.addEventListener("input", ({ target }) => {
    const disponible = parseInt(ELEMENTS.cantidadDisp()?.value, 10);
    const valor      = parseInt(target.value, 10);

    if (isNaN(valor) || valor <= 0) {
      showCantidadError(true, "Ingresá una cantidad válida");
    } else if (valor > disponible) {
      target.value = disponible;
      showCantidadError(true, `Máximo disponible: ${disponible}`);
    } else {
      showCantidadError(false);
    }
  });

  // Importar totalidad
  $("btn-import-total")?.addEventListener("click", () => {
    const categoryId = getSelectedCategoryId();
    if (!categoryId) { showSelectError(true); return; }

    showSelectError(false);
    const timerId  = ELEMENTS.timerId().value;
    const cantidad = parseInt(ELEMENTS.cantidadDisp().value, 10);
    executeImport(timerId, categoryId, cantidad);
  });

  // Ir al paso 2 (cantidad personalizada)
  $("btn-import-custom")?.addEventListener("click", () => {
    const categoryId = getSelectedCategoryId();
    if (!categoryId) { showSelectError(true); return; }

    showSelectError(false);
    ELEMENTS.selectedCatId().value = categoryId;
    goToStep2();
  });

  // Confirmar importación personalizada
  $("btn-confirm-custom-import")?.addEventListener("click", () => {
    const timerId    = ELEMENTS.timerId().value;
    const categoryId = ELEMENTS.selectedCatId().value;
    const disponible = parseInt(ELEMENTS.cantidadDisp().value, 10);
    const cantidad   = parseInt(ELEMENTS.cantidadInput().value, 10);

    if (isNaN(cantidad) || cantidad <= 0) {
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
};

// ─────────────────────────────────────────────
//  Ejecutar el import contra el backend
// ─────────────────────────────────────────────

export const executeImport = async (timerId, categoryId, cantidad) => {
  document.body.style.cursor = "wait";

  try {
    const response = await fetch(`/import-timer/${timerId}/${categoryId}/${cantidad}`, {
      method: "POST",
      headers: { "Content-Type": "application/json", Accept: "application/json" },
    });

    const data = await response.json();

    if (!data.success) throw new Error(data.message || "Error desconocido al importar");

    window.location.href = "/dashboard";

  } catch (error) {
    console.error("[executeImport]", error);
    alert(error.message);
  } finally {
    document.body.style.cursor = "default";
  }
};

// ─────────────────────────────────────────────
//  Cerrar modal
// ─────────────────────────────────────────────

export const closeImportModal = () => {
  ELEMENTS.modal()?.classList.add("hidden");
  document.body.style.cursor = "default";
};