
const API = "/api/cajero";

const $ = (selector) => document.querySelector(selector);

const totalEl = $("[data-total-pedido]");
const total = Number(totalEl.dataset.monto);

const inputEfectivo = $("[data-input-efectivo]");
const cambioEl = $("[data-cambio]");
const estadoPagoEl = $("[data-estado-pago]");
const btnCobrar = $("[data-btn-cobrar-efectivo]");
const inputDoc = $("[data-input-documento]");
const infoClienteEl = $("[data-info-cliente]");

const formatearPrecio = (n) => `$${Number(n).toFixed(2)}`;


const debounce = (fn, delay) => {
  let timer;
  return (...args) => {
    clearTimeout(timer);
    timer = setTimeout(() => fn(...args), delay);
  };
};

// -------- Calculadora de cambio --------
const parsearMonto = (valor) => {
  if (!valor) return NaN;
  const limpio = String(valor).replace(/\./g, "").replace(",", ".").replace(/[^\d.]/g, "");
  return Number(limpio);
};

const actualizarCambio = () => {
  const recibido = parsearMonto(inputEfectivo.value);

  if (isNaN(recibido) || recibido <= 0) {
    cambioEl.textContent = formatearPrecio(0);
    cambioEl.className = "text-2xl font-bold px-4 py-3 rounded-md bg-slate-50 border-2 border-slate-100";
    estadoPagoEl.innerHTML = "<span class=\"text-slate-400\">Ingresá el efectivo recibido.</span>";
    btnCobrar.disabled = true;
    return;
  }

  const cambio = recibido - total;

  if (cambio < 0) {
    const falta = Math.abs(cambio);
    cambioEl.textContent = `- ${formatearPrecio(falta)}`;
    cambioEl.className = "text-2xl font-bold px-4 py-3 rounded-md bg-red-50 border-2 border-red-200 text-red-700";
    estadoPagoEl.innerHTML = `<span class="text-red-600 font-semibold">Falta ${formatearPrecio(falta)} para cubrir el total.</span>`;
    btnCobrar.disabled = true;
    return;
  }

  cambioEl.textContent = formatearPrecio(cambio);
  cambioEl.className = "text-2xl font-bold px-4 py-3 rounded-md bg-emerald-50 border-2 border-emerald-200 text-emerald-700";

  if (cambio === 0) {
    estadoPagoEl.innerHTML = "<span class=\"text-emerald-600 font-semibold\">Pago exacto. Listo para cobrar.</span>";
  } else {
    estadoPagoEl.innerHTML = `<span class="text-slate-600">Devolver <strong>${formatearPrecio(cambio)}</strong> de vuelto.</span>`;
  }
  btnCobrar.disabled = false;
};

const initEfectivo = () => {
  inputEfectivo.addEventListener("input", actualizarCambio);

  // Atajos: sumar montos redondos al valor actual
  document.querySelectorAll("[data-monto]").forEach((btn) => {
    btn.addEventListener("click", () => {
      const actual = parsearMonto(inputEfectivo.value) || 0;
      const sumar = Number(btn.dataset.monto);
      inputEfectivo.value = String(actual + sumar);
      actualizarCambio();
    });
  });

  $("[data-monto-exacto]").addEventListener("click", () => {
    inputEfectivo.value = String(total);
    actualizarCambio();
  });

  actualizarCambio();
};

// -------- Búsqueda de cliente por DNI --------
const buscarCliente = async (documento) => {
  if (!documento || documento.trim().length < 3) {
    infoClienteEl.innerHTML = "";
    return;
  }
  try {
    const respuesta = await fetch(`${API}/buscar-cliente`, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: `documento=${encodeURIComponent(documento.trim())}`
    });
    if (!respuesta.ok) throw new Error("error");
    const data = await respuesta.json();
    if (data.cliente) {
      infoClienteEl.innerHTML = `<span class="text-emerald-600 font-semibold">Cliente: ${data.cliente.nombre}</span>`;
    } else {
      infoClienteEl.innerHTML = "<span class=\"text-amber-600\">Sin cliente registrado. Se cobra como anónimo.</span>";
    }
  } catch (err) {
    infoClienteEl.innerHTML = "<span class=\"text-slate-400\">No se pudo verificar el cliente.</span>";
  }
};

const initDocumento = () => {
  inputDoc.addEventListener("input", debounce((e) => buscarCliente(e.target.value), 400));
};

// -------- Confirmar cobro --------
const confirmarCobro = async () => {
  btnCobrar.disabled = true;
  btnCobrar.textContent = "Cobrando…";

  const documento = inputDoc.value.trim();
  const url = `${API}/cobrar${documento ? `?documento=${encodeURIComponent(documento)}` : ""}`;

  try {
    const respuesta = await fetch(url, { method: "POST" });
    if (!respuesta.ok) {
      const err = await respuesta.json().catch(() => ({}));
      alert(err.error || "No se pudo cobrar el pedido.");
      btnCobrar.disabled = false;
      btnCobrar.textContent = "Confirmar cobro";
      return;
    }
    // Volvemos a la caja para el próximo pedido
    window.location.href = "/cajero";
  } catch (err) {
    alert("Error de red al cobrar. Reintentá.");
    btnCobrar.disabled = false;
    btnCobrar.textContent = "Confirmar cobro";
  }
};

const initBotonCobrar = () => {
  btnCobrar.addEventListener("click", confirmarCobro);
};

document.addEventListener("DOMContentLoaded", () => {
  initEfectivo();
  initDocumento();
  initBotonCobrar();
});
