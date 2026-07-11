const API = "/api/cajero";

// -------- Estado en cliente (mínimo indispensable) --------
const estado = {
  categoriaSeleccionada: null,
  productoEnPanel: null,       // ProductoFinalDto que está en el panel superior
  ingredientesMarcados: new Set(), // productoIds tildados en el panel superior
  carrito: { items: [], total: 0 }
};

// -------- Helpers --------
const formatearPrecio = (n) => `$${Number(n).toFixed(2)}`;

const $ = (selector) => document.querySelector(selector);
const $$ = (selector) => document.querySelectorAll(selector);

const fetchJson = async (url, opciones = {}) => {
  const respuesta = await fetch(url, {
    headers: { "Content-Type": "application/json" },
    ...opciones
  });
  if (!respuesta.ok) throw new Error(`${respuesta.status}`);
  return respuesta.json();
};

// -------- Categorías (botones fijos, se pintan desde el HTML server-rendered) --------
const initCategorias = async () => {
  const botones = $$("[data-btn-categoria]");
  if (botones.length === 0) return;

  botones.forEach((btn) => {
    btn.addEventListener("click", () => seleccionarCategoria(btn));
  });

  // Arranco con la primera categoría (ya viene con la clase de "seleccionada" desde el server)
  await seleccionarCategoria(botones[0]);
};

const seleccionarCategoria = async (btn) => {
  const idCategoria = Number(btn.dataset.idCategoria);
  if (estado.categoriaSeleccionada === idCategoria) return;

  estado.categoriaSeleccionada = idCategoria;

  // Estado visual de las pestañas de categoría
  $$("[data-btn-categoria]").forEach((b) => {
    b.classList.remove("ring-2", "ring-offset-1", "ring-puesto-accent");
  });
  btn.classList.add("ring-2", "ring-offset-1", "ring-puesto-accent");

  await cargarProductosDeCategoria(idCategoria);
};

// -------- Grilla de productos --------
const cargarProductosDeCategoria = async (idCategoria) => {
  const grilla = $("[data-grilla-productos]");
  const msg = $("[data-mensaje-productos]");

  try {
    const productos = await fetchJson(`${API}/productos?idCategoria=${idCategoria}`);
    renderProductos(productos);
  } catch (err) {
    grilla.innerHTML = "";
    msg.textContent = "No se pudieron cargar los productos.";
    msg.classList.remove("hidden");
    grilla.appendChild(msg);
  }
};

const renderProductos = (productos) => {
  const grilla = $("[data-grilla-productos]");
  grilla.innerHTML = "";

  if (productos.length === 0) {
    grilla.innerHTML = `
            <p class="col-span-full text-slate-400 text-sm text-center py-8">
                No hay productos en esta categoría.
            </p>`;
    return;
  }

  productos.forEach((pf) => {
    const boton = document.createElement("button");
    boton.type = "button";
    boton.className = "bg-puesto-btn text-puesto-btn-text border border-puesto-btn-border rounded-md p-3 hover:bg-puesto-btn-hover-bg transition text-left flex flex-col justify-between";
    boton.innerHTML = `
            <span class="font-semibold">${pf.nombre}</span>
            <span class="text-xs opacity-80 mt-1">${formatearPrecio(pf.precio)}</span>
        `;
    boton.addEventListener("click", () => seleccionarProducto(pf));
    grilla.appendChild(boton);
  });
};

// -------- Panel superior: producto seleccionado --------
const seleccionarProducto = (pf) => {
  // Si NO tiene ingredientes, agregado directo, sin pasar por el panel
  if (!pf.tieneIngredientes) {
    agregarAlCarrito(pf.id, []);
    return;
  }

  estado.productoEnPanel = pf;
  // Por defecto TODOS los ingredientes están marcados (van al pedido)
  estado.ingredientesMarcados = new Set(pf.ingredientes.map((i) => i.productoId));

  renderPanelSeleccion();
};

const renderPanelSeleccion = () => {
  const vacio = $("[data-estado-vacio]");
  const conProducto = $("[data-estado-con-producto]");

  if (!estado.productoEnPanel) {
    vacio.classList.remove("hidden");
    conProducto.classList.add("hidden");
    return;
  }

  vacio.classList.add("hidden");
  conProducto.classList.remove("hidden");

  $("[data-nombre-producto]").textContent = estado.productoEnPanel.nombre;
  $("[data-precio-producto]").textContent = formatearPrecio(estado.productoEnPanel.precio);

  const lista = $("[data-lista-ingredientes]");
  lista.innerHTML = "";

  estado.productoEnPanel.ingredientes.forEach((ing) => {
    const marcado = estado.ingredientesMarcados.has(ing.productoId);
    const chip = document.createElement("label");
    chip.className = `flex items-center gap-2 px-3 py-1.5 rounded-full border cursor-pointer text-sm transition ${
      marcado
        ? "bg-puesto-btn text-puesto-btn-text border-puesto-btn-border"
        : "bg-slate-100 text-slate-500 border-slate-200 line-through"
    }`;
    chip.innerHTML = `
            <input type="checkbox" class="sr-only" ${marcado ? "checked" : ""}/>
            <span>${ing.nombre}</span>
        `;
    chip.querySelector("input").addEventListener("change", (e) => {
      if (e.target.checked) {
        estado.ingredientesMarcados.add(ing.productoId);
      } else {
        estado.ingredientesMarcados.delete(ing.productoId);
      }
      renderPanelSeleccion();
    });
    lista.appendChild(chip);
  });
};

const initBotonConfirmar = () => {
  $("[data-btn-confirmar]").addEventListener("click", async () => {
    if (!estado.productoEnPanel) return;

    // Ingredientes retirados = los que están en el original pero NO en el Set de marcados
    const retirados = estado.productoEnPanel.ingredientes
      .filter((ing) => !estado.ingredientesMarcados.has(ing.productoId))
      .map((ing) => ing.productoId);

    await agregarAlCarrito(estado.productoEnPanel.id, retirados);

    // Limpiar el panel superior después de agregar
    estado.productoEnPanel = null;
    estado.ingredientesMarcados.clear();
    renderPanelSeleccion();
  });
};

// -------- Carrito --------
const agregarAlCarrito = async (idProductoFinal, ingredientesRetiradosIds) => {
  try {
    estado.carrito = await fetchJson(`${API}/carrito/items`, {
      method: "POST",
      body: JSON.stringify({ idProductoFinal, ingredientesRetiradosIds })
    });
    renderCarrito();
  } catch (err) {
    console.error("Error al agregar item", err);
  }
};

const eliminarDelCarrito = async (idLinea) => {
  try {
    estado.carrito = await fetchJson(`${API}/carrito/items/${idLinea}`, {
      method: "DELETE"
    });
    renderCarrito();
  } catch (err) {
    console.error("Error al eliminar item", err);
  }
};

const renderCarrito = () => {
  const contenedor = $("[data-lista-carrito]");
  const cantidad = $("[data-cantidad-items]");
  const total = $("[data-total]");
  const btnCobrar = $("[data-btn-cobrar]");

  total.textContent = formatearPrecio(estado.carrito.total);
  cantidad.textContent = `${estado.carrito.items.length} item${estado.carrito.items.length === 1 ? "" : "s"}`;

  if (estado.carrito.items.length === 0) {
    contenedor.innerHTML = "<p class=\"text-slate-400 text-sm text-center py-8\">El carrito está vacío.</p>";
    btnCobrar.disabled = true;
    return;
  }

  contenedor.innerHTML = "";
  estado.carrito.items.forEach((item) => {
    const card = document.createElement("div");
    card.className = "py-3 border-b border-slate-100 last:border-b-0 flex items-start justify-between gap-2";
    const ingredientesTexto = item.ingredientes
      .filter((i) => i.cantidad > 0)
      .map((i) => `${i.nombre}${i.cantidad > 1 ? ` x${i.cantidad}` : ""}`)
      .join(", ") || "Sin ingredientes";
    card.innerHTML = `
            <div class="flex-1 min-w-0">
                <p class="font-semibold text-slate-900 truncate">${item.nombre}</p>
                <p class="text-xs text-slate-500 truncate">${ingredientesTexto}</p>
                <p class="text-sm font-semibold text-puesto-accent mt-1">${formatearPrecio(item.precio)}</p>
            </div>
            <button type="button" class="text-slate-400 hover:text-red-600 transition p-1" aria-label="Quitar" data-btn-eliminar>
                <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M6 18 18 6M6 6l12 12"/>
                </svg>
            </button>
        `;
    card.querySelector("[data-btn-eliminar]").addEventListener("click", () => {
      eliminarDelCarrito(item.idLinea);
    });
    contenedor.appendChild(card);
  });

  btnCobrar.disabled = false;
};

const initBotonCobrar = () => {
  $("[data-btn-cobrar]").addEventListener("click", () => {
    // Redirect a la pantalla de cobro; el carrito ya está en sesión
    window.location.href = "/cajero/cobro";
  });
};

// -------- Bootstrap --------
const cargarCarritoInicial = async () => {
  try {
    estado.carrito = await fetchJson(`${API}/carrito`);
    renderCarrito();
  } catch (err) {
    console.error("No se pudo cargar el carrito", err);
  }
};

document.addEventListener("DOMContentLoaded", async () => {
  renderPanelSeleccion();
  initBotonConfirmar();
  initBotonCobrar();
  await cargarCarritoInicial();
  await initCategorias();
});