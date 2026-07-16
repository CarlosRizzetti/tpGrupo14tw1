

const filtroEstado = document.getElementById("filtro-estado");
const filtroCategoria = document.getElementById("filtro-categoria");
const contenedorTimers = document.getElementById("timers-container");

const formatearFecha = (fechaString) => {
  if (!fechaString || fechaString === "null" || fechaString === "--") return "--";

  try {
    const fecha = new Date(fechaString);
    if (Number.isNaN(fecha.getTime())) return fechaString;

    const dia = String(fecha.getDate()).padStart(2, "0");
    const mes = String(fecha.getMonth() + 1).padStart(2, "0");
    const horas = String(fecha.getHours()).padStart(2, "0");
    const minutos = String(fecha.getMinutes()).padStart(2, "0");

    return `${dia}/${mes} ${horas}:${minutos}`;
  } catch {
    return "--";
  }
};

document.querySelectorAll(".js-formatear-fecha").forEach((span) => {
  span.textContent = formatearFecha(span.textContent.trim());
  span.classList.remove("js-formatear-fecha");
});

const renderizarLotes = (lotes = []) =>
  lotes.length
    ? lotes
      .map(
        ({ id, numeroDeLote, cantidadConsumida }) => `
            <a href="/admin/lotes/${id}" class="lote-link text-[10px] font-bold text-blue-600 hover:underline bg-blue-50 px-2 py-0.5 rounded">
              Lote #${numeroDeLote} (x${cantidadConsumida})
            </a>
          `
      )
      .join("")
    : "<span class=\"text-[10px] text-gray-400\">Sin lotes</span>";

const renderizarTimer = (timer) => {
  const {
    id,
    nombre,
    cantidad,
    estado,
    ubicacion,
    usuario,
    categoria,
    lotesUtilizados,
  } = timer;
  const { fechaCreacion, fechaVencimiento } = timer.cicloVida ?? {}

  const temaClase = categoria?.tema ?? "tema-servicio";
  const nombreUsuario = usuario ?? "Sistema";
  const ubicacionTexto = ubicacion ?? "General";
  const estadoTexto = estado ?? "--";
  const elaboracionFormateada = formatearFecha(fechaCreacion);
  const vencimientoFormateado = formatearFecha(fechaVencimiento);
  const lotesHtml = renderizarLotes(lotesUtilizados);

  return `
    <div class="timer-card cursor-pointer hover:shadow-md ${temaClase} bg-white rounded-2xl shadow-sm border border-gray-100 flex flex-col md:flex-row overflow-hidden transform transition-all" data-timer-id="${id}">

      <div class="bg-puesto-header p-5 md:w-1/3 flex flex-col justify-center border-b-[6px] md:border-b-0 md:border-l-[6px] border-puesto-btn">
        <span class="text-[10px] font-black text-white/80 uppercase tracking-widest mb-1">${ubicacionTexto}</span>
        <h2 class="text-xl font-black text-puesto-header-text uppercase leading-tight line-clamp-2 break-words">${nombre}</h2>
      </div>

      <div class="p-5 md:w-1/3 flex flex-col justify-center gap-3 border-b md:border-b-0 md:border-r border-gray-100 bg-white">
        <div>
          <span class="text-[9px] font-bold text-gray-400 uppercase tracking-widest block mb-0.5">Estado</span>
          <span class="text-sm font-black text-gray-800 uppercase">${estadoTexto}</span>
        </div>
        <div>
          <span class="text-[9px] font-bold text-gray-400 uppercase tracking-widest block mb-0.5">Elaboración</span>
          <span class="text-sm font-black text-gray-800">${elaboracionFormateada}</span>
        </div>
        <div>
          <span class="text-[9px] font-bold text-gray-400 uppercase tracking-widest block mb-0.5">Vencimiento</span>
          <span class="text-sm font-black text-puesto-header">${vencimientoFormateado}</span>
        </div>
        <div>
          <span class="text-[9px] font-bold text-gray-400 uppercase tracking-widest block mb-0.5">Lotes utilizados</span>
          <div class="flex flex-wrap gap-1">${lotesHtml}</div>
        </div>
      </div>

      <div class="p-5 md:w-1/3 flex flex-row md:flex-col justify-between items-center md:items-end bg-white">
        <div class="text-left md:text-right">
          <span class="text-[9px] font-bold text-gray-400 uppercase tracking-widest block mb-0.5">Cantidad</span>
          <span class="text-3xl font-black text-gray-800 tabular-nums">${cantidad}</span>
        </div>

        <div class="flex items-center gap-1.5 bg-gray-50 border border-gray-100 px-3 py-1.5 rounded-lg">
          <svg class="w-4 h-4 text-gray-400" fill="currentColor" viewBox="0 0 24 24">
            <path fill-rule="evenodd" d="M12 12a5 5 0 100-10 5 5 0 000 10zm-7 8a7 7 0 1114 0H5z" clip-rule="evenodd"/>
          </svg>
          <span class="text-[10px] font-bold text-gray-500 uppercase tracking-wider max-w-[100px] truncate">${nombreUsuario}</span>
        </div>
      </div>

    </div>
  `;
};

const renderizarTimers = (timers = []) => {
  if (!timers.length) {
    contenedorTimers.innerHTML = `
      <div class="text-center py-16 bg-white rounded-2xl border border-dashed border-gray-300">
        <p class="text-gray-400 font-bold uppercase tracking-widest">No se encontraron resultados</p>
      </div>
    `;
    return;
  }
  contenedorTimers.innerHTML = timers.map(renderizarTimer).join("");
};

const aplicarFiltros = async () => {
  const params = new URLSearchParams();
  if (filtroEstado.value) params.append("estado", filtroEstado.value);
  if (filtroCategoria.value) params.append("categoriaId", filtroCategoria.value);

  try {
    const respuesta = await fetch(`/timers/obtener/?${params.toString()}`);
    if (!respuesta.ok) {
      throw new Error("Error de red");
    }
    const { timers } = await respuesta.json();
    renderizarTimers(timers);
  } catch (error) {
    console.error("Error cargando los timers:", error);
  }
};


contenedorTimers.addEventListener("click", (evento) => {
  if (evento.target.closest(".lote-link")) {
    return;
  }
  const card = evento.target.closest(".timer-card");
  if (card?.dataset.timerId) {
    window.location.href = `/admin/timers/${card.dataset.timerId}`;
  }
});

filtroEstado.addEventListener("change", aplicarFiltros);
filtroCategoria.addEventListener("change", aplicarFiltros);