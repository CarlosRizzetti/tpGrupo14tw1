const $r = (id) => document.getElementById(id);

const RENEW = {
  modal:         () => $r("renew-modal"),
  step2:         () => $r("renew-step-2"),
  productName:   () => $r("renew-product-name"),
  productLoc:    () => $r("renew-product-location"),
  timerId:       () => $r("renew-timer-id"),
  cantidadTimer: () => $r("renew-cantidad-timer"),
  cantidadMain:  () => $r("renew-cantidad-main"),
  cantidadBadge: () => $r("modal-renew-cantidad-badge"),
  cantidadInput: () => $r("modal-renew-cantidad-input"),
  cantidadError: () => $r("modal-renew-cantidad-error"),
};

// ─────────────────────────────────────────────
//  Navegación entre pasos
// ─────────────────────────────────────────────

const renewGoToStep2 = () => {
  RENEW.step2().style.transform = "translateX(0)";
};

const renewGoToStep1 = () => {
  RENEW.step2().style.transform = "translateX(100%)";
};

// ─────────────────────────────────────────────
//  Helpers UI
// ─────────────────────────────────────────────

const showRenewError = (show, msg = null) => {
  const el = RENEW.cantidadError();
  if (!el) return;
  if (msg) el.textContent = msg;
  el.classList.toggle("hidden", !show);
};

const resetRenewModal = () => {
  renewGoToStep1();
  showRenewError(false);
  const input = RENEW.cantidadInput();
  if (input) {
    input.value = "";
    input.max   = "";
  }
};

// ─────────────────────────────────────────────
//  Abrir modal de renovación
// ─────────────────────────────────────────────

export const openRenewModal = (timerId, productName, location, cantidadTimer) => {
  const cantidad = cantidadTimer ?? 0;
  console.log(cantidadTimer);
  RENEW.productName().textContent = productName;
  RENEW.productLoc().textContent  = location;
  RENEW.timerId().value           = timerId;
  RENEW.cantidadTimer().value     = cantidad;
  RENEW.cantidadMain().textContent  = cantidad;
  RENEW.cantidadBadge().textContent = cantidad;
  RENEW.cantidadInput().max         = cantidad;

  resetRenewModal();
  RENEW.modal().classList.remove("hidden");
  initRenewListeners();
};

// ─────────────────────────────────────────────
//  Listeners (se registran una sola vez)
// ─────────────────────────────────────────────

let renewListenersInit = false;

const initRenewListeners = () => {
  if (renewListenersInit) return;
  renewListenersInit = true;

  // Volver al paso 1
  $r("btn-renew-back-step1")?.addEventListener("click", () => {
    renewGoToStep1();
    showRenewError(false);
  });

  // Validación en tiempo real
  $r("modal-renew-cantidad-input")?.addEventListener("input", ({ target }) => {
    const disponible = parseInt(RENEW.cantidadTimer()?.value, 10);
    const valor      = parseInt(target.value, 10);

    if (isNaN(valor) || valor <= 0) {
      showRenewError(true, "Ingresá una cantidad válida");
    } else if (valor > disponible) {
      target.value = disponible;
      showRenewError(true, `Máximo disponible: ${disponible}`);
    } else {
      showRenewError(false);
    }
  });

  // Renovar misma cantidad
  $r("btn-renew-total")?.addEventListener("click", () => {
    const timerId  = RENEW.timerId().value;
    const cantidad = parseInt(RENEW.cantidadTimer().value, 10);
    executeRenew(timerId, cantidad);
  });

  // Ir al paso 2
  $r("btn-renew-custom")?.addEventListener("click", () => {
    renewGoToStep2();
  });

  // Confirmar renovación personalizada
  $r("btn-renew-confirm-custom-import")?.addEventListener("click", () => {
    const timerId    = RENEW.timerId().value;
    const disponible = parseInt(RENEW.cantidadTimer().value, 10);
    const cantidad   = parseInt(RENEW.cantidadInput().value, 10);

    if (isNaN(cantidad) || cantidad <= 0) {
      showRenewError(true, "Ingresá una cantidad válida");
      return;
    }
    if (cantidad > disponible) {
      showRenewError(true, `No podés renovar más de ${disponible} unidades`);
      return;
    }

    showRenewError(false);
    executeRenew(timerId, cantidad);
  });
};

// ─────────────────────────────────────────────
//  Ejecutar renovación contra el backend
// ─────────────────────────────────────────────

const executeRenew = async (timerId, cantidad) => {
  const card = $r(`timer-${timerId}`);
  if (card) {
    card.style.opacity      = "0.5";
    card.style.pointerEvents = "none";
  }

  closeRenewModal();
  document.body.style.cursor = "wait";

  try {
    const response = await fetch(`/active-timers/renovarTimer/${timerId}/${cantidad}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
    });

    const data = await response.json();

    if (data.status === "ok") {
      updateCardDOM(card, timerId, data);

      card.classList.add("animate-success", "ring-4", "ring-green-500/50");
      setTimeout(() => {
        card.classList.remove("animate-success", "ring-4", "ring-green-500/50");
      }, 800);

      let notificados = JSON.parse(sessionStorage.getItem("notificados_mcd")) || [];
      notificados = notificados.filter((item) => item !== timerId.toString());
      sessionStorage.setItem("notificados_mcd", JSON.stringify(notificados));

    } else {
      throw new Error(data.mensaje || "Hubo un error al renovar el timer.");
    }

  } catch (error) {
    console.error("[executeRenew]", error);
    alert(error.message);
  } finally {
    if (card) {
      card.style.opacity      = "1";
      card.style.pointerEvents = "auto";
    }
    document.body.style.cursor = "default";
  }
};

// ─────────────────────────────────────────────
//  Actualizar DOM de la card
// ─────────────────────────────────────────────

const updateCardDOM = (card, oldId, data) => {
  const newId = data.nuevoTimerId;

  card.id = `timer-${newId}`;
  card.setAttribute("data-vencimiento", data.fechaVencimiento);

  const subIds = {
    [`display-${oldId}`]:   `display-${newId}`,
    [`elab-${oldId}`]:      `elab-${newId}`,
    [`vence-${oldId}`]:     `vence-${newId}`,
    [`nombre-${oldId}`]:    `nombre-${newId}`,
    [`ubicacion-${oldId}`]: `ubicacion-${newId}`,
  };

  Object.entries(subIds).forEach(([oldSubId, newSubId]) => {
    const el = $r(oldSubId);
    if (el) el.id = newSubId;
  });

  const elabSpan  = $r(`elab-${newId}`);
  const venceSpan = $r(`vence-${newId}`);
  if (elabSpan)  elabSpan.innerText  = formatearFecha(data.fechaElaboracion);
  if (venceSpan) venceSpan.innerText = formatearFecha(data.fechaVencimiento);

  ["btn-eliminar", "btn-importar", "btn-renovar"].forEach((btnClass) => {
    card.querySelector(`.${btnClass}`)?.setAttribute("data-timer-id", `${newId}`);
  });

  const btnImportar = card.querySelector(".btn-importar");
  if (btnImportar) {
    const nombre = data.nombre || $r(`nombre-${newId}`)?.textContent || "";
    btnImportar.setAttribute("data-product-name", nombre);
  }

  // Actualizar cantidad en la card si cambió
  if (data.cantidad !== undefined) {
    card.querySelector("[data-cantidad]")?.setAttribute("data-cantidad", data.cantidad);
    const cantSpan = card.querySelector(".timer-cantidad");
    if (cantSpan) cantSpan.textContent = data.cantidad;
  }
};

// ─────────────────────────────────────────────
//  Formatear fecha
// ─────────────────────────────────────────────

const formatearFecha = (isoString) =>
  new Date(isoString)
    .toLocaleDateString("es-AR", {
      day:    "2-digit",
      month:  "2-digit",
      hour:   "2-digit",
      minute: "2-digit",
      hour12: false,
    })
    .replace(",", "");

// ─────────────────────────────────────────────
//  Cerrar modal
// ─────────────────────────────────────────────

export const closeRenewModal = () => {
  RENEW.modal()?.classList.add("hidden");
  document.body.style.cursor = "default";
};