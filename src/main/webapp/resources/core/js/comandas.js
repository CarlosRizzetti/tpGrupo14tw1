

const API = "/api/cocina/comandas";
const INTERVALO_POLLING_MS = 6000;

const $ = (selector) => document.querySelector(selector);

const root = $("[data-comandas-root]");
const idCategoria = Number(root.dataset.idCategoria);

const grillaEl = $("[data-grilla-comandas]");
const vacioEl = $("[data-estado-vacio]");
const modalEl = $("[data-modal-faltantes]");
const listaFaltantesEl = $("[data-lista-faltantes]");

let comandasEnPantalla = new Map(); // idComanda -> hash (para detectar cambios)

const formatearHora = (isoString) => {
  if (!isoString) return "";
  const d = new Date(isoString);
  return `${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
};

// -------- Modal --------
const abrirModalFaltantes = (productos) => {
  listaFaltantesEl.innerHTML = "";
  productos.forEach((p) => {
    const li = document.createElement("li");
    li.textContent = p.nombre;
    listaFaltantesEl.appendChild(li);
  });
  modalEl.classList.remove("hidden");
};

const cerrarModal = () => modalEl.classList.add("hidden");

$("[data-cerrar-modal]").addEventListener("click", cerrarModal);
modalEl.addEventListener("click", (e) => {
  if (e.target === modalEl) cerrarModal();
});

// -------- Sacar comanda --------
const sacarComanda = async (idComanda, btn) => {
  btn.disabled = true;
  btn.textContent = "Enviando…";

  try {
    const respuesta = await fetch(`${API}/${idComanda}/sacar`, { method: "POST" });

    if (respuesta.status === 409) {
      const data = await respuesta.json();
      abrirModalFaltantes(data.productos || []);
      btn.disabled = false;
      btn.textContent = "Servido";
      return;
    }

    if (!respuesta.ok) {
      btn.disabled = false;
      btn.textContent = "Servido";
      alert("No se pudo sacar la comanda. Reintentá.");
      return;
    }


    await refrescarComandas();

  } catch (err) {
    console.error(err);
    btn.disabled = false;
    btn.textContent = "Servido";
    alert("Error de red al sacar la comanda.");
  }
};

// -------- Render --------
const construirHashComanda = (comanda) => {
  const totalIngredientes = comanda.lineas.reduce((acc, l) => acc + l.ingredientes.length, 0);
  return `${comanda.id}-${comanda.lineas.length}-${totalIngredientes}`;
};

const renderComanda = (comanda) => {
  const card = document.createElement("article");
  card.className = "bg-white rounded-2xl shadow-lg border-t-8 border-puesto-btn-border flex flex-col overflow-hidden";
  card.dataset.idComanda = comanda.id;

  // Header
  const header = document.createElement("div");
  header.className = "flex items-center justify-between px-5 py-3 bg-slate-50 border-b border-slate-200";
  header.innerHTML = `
        <div>
            <p class="text-xs uppercase tracking-widest text-slate-400">Comanda</p>
            <p class="text-2xl font-black text-slate-900">#${comanda.idPedido}</p>
        </div>
        <div class="text-right">
            <p class="text-xs uppercase tracking-widest text-slate-400">Cobrado</p>
            <p class="text-sm font-bold text-slate-700">${formatearHora(comanda.horaCobro)}</p>
        </div>
    `;
  card.appendChild(header);

  // Cuerpo: líneas del pedido
  const body = document.createElement("div");
  body.className = "flex-1 px-5 py-4 space-y-4";

  comanda.lineas.forEach((linea) => {
    const bloque = document.createElement("div");
    bloque.innerHTML = `
            <p class="font-black uppercase tracking-wide text-slate-900">${linea.nombre}</p>
            ${linea.tieneIngredientes
    ? `<ul class="mt-1 pl-4 space-y-0.5">
                     ${linea.ingredientes.map((ing) => `
                         <li class="text-sm text-slate-700 flex items-center gap-2">
                             <span class="w-1 h-1 rounded-full bg-slate-400"></span>
                             ${ing.nombre}${ing.cantidad > 1 ? ` <span class="font-bold">×${ing.cantidad}</span>` : ""}
                         </li>
                     `).join("")}
                   </ul>`
    : "<p class=\"text-xs text-slate-400 italic mt-1\">Sin ingredientes personalizables</p>"
}
        `;
    body.appendChild(bloque);
  });
  card.appendChild(body);

  // Botón "Servido"
  const footer = document.createElement("div");
  footer.className = "p-4 border-t border-slate-100";
  const btn = document.createElement("button");
  btn.type = "button";
  btn.textContent = "Servido";
  btn.className = "w-full bg-puesto-btn text-puesto-btn-text border-2 border-puesto-btn-border rounded-md py-3 font-black uppercase tracking-widest hover:bg-puesto-btn-hover-bg transition";
  btn.addEventListener("click", () => sacarComanda(comanda.id, btn));
  footer.appendChild(btn);
  card.appendChild(footer);

  return card;
};

const renderTodas = (comandas) => {
  if (comandas.length === 0) {
    grillaEl.classList.add("hidden");
    vacioEl.classList.remove("hidden");
    grillaEl.innerHTML = "";
    comandasEnPantalla.clear();
    return;
  }

  vacioEl.classList.add("hidden");
  grillaEl.classList.remove("hidden");

  const nuevasHashes = new Map(comandas.map((c) => [c.id, construirHashComanda(c)]));

  const cambio =
        nuevasHashes.size !== comandasEnPantalla.size ||
        [...nuevasHashes.entries()].some(([id, hash]) => comandasEnPantalla.get(id) !== hash);

  if (!cambio) return;

  grillaEl.innerHTML = "";
  comandas.forEach((c) => grillaEl.appendChild(renderComanda(c)));
  comandasEnPantalla = nuevasHashes;
};

// -------- Polling --------
const refrescarComandas = async () => {
  try {
    const respuesta = await fetch(`${API}?idCategoria=${idCategoria}`);
    if (!respuesta.ok) return;
    const comandas = await respuesta.json();
    renderTodas(comandas);
  } catch (err) {
    console.error("No se pudieron cargar las comandas", err);
  }
};

document.addEventListener("DOMContentLoaded", () => {
  refrescarComandas();
  setInterval(refrescarComandas, INTERVALO_POLLING_MS);


  document.addEventListener("visibilitychange", () => {
    if (document.visibilityState === "visible") refrescarComandas();
  });
});
