const form = document.getElementById('filtros-historial');
const contenedor = document.getElementById('resultados-pedidos');

const construirQueryParams = () => {
    const datos = new FormData(form);
    const params = new URLSearchParams();
    for (const [clave, valor] of datos.entries()) {
        if (valor) {
            params.append(clave, valor);
        }
    }
    return params.toString();
};

const formatearFecha = (fechaIso) => {
    if (!fechaIso) return '-';
    const fecha = new Date(fechaIso);
    return fecha.toLocaleString('es-AR', { dateStyle: 'short', timeStyle: 'short' });
};

const escaparHtml = (texto = '') =>
    texto.replace(/[&<>"']/g, (caracter) => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;',
    }[caracter]));

const renderTimers = (timers = []) =>
    timers
        .map(
            (timerId) => `
        <a href="/admin/timers/${timerId}" class="text-emerald-600 hover:underline text-xs ml-1">
          (Timer #${timerId})
        </a>
      `
        )
        .join('');

const renderIngredientes = (ingredientes = []) => `
  <ul class="ml-4 mt-1 space-y-1 text-xs text-gray-600 list-disc">
    ${ingredientes
    .map(
        ({ nombreProducto, cantidad, timers }) => `
          <li>${cantidad}x ${escaparHtml(nombreProducto)} ${renderTimers(timers)}</li>
        `
    )
    .join('')}
  </ul>
`;

const renderItems = (items = []) =>
    items
        .map(
            ({ nombreProducto, ingredientes }) => `
        <div class="border-t pt-2 mt-2 first:border-t-0 first:mt-0 first:pt-0">
          <p class="font-semibold text-sm text-gray-800">${escaparHtml(nombreProducto)}</p>
          ${renderIngredientes(ingredientes)}
        </div>
      `
        )
        .join('');

const renderPedido = (pedido) => {
    const { id, clienteId, clienteNombre, estado, horaCobro, horaSalida, items } = pedido;
    const rangoHoras = horaSalida
        ? `${formatearFecha(horaCobro)} &rarr; ${formatearFecha(horaSalida)}`
        : formatearFecha(horaCobro);

    return `
    <div class="bg-white rounded-lg shadow p-6">
      <div class="flex items-center justify-between mb-2">
        <h3 class="font-bold text-gray-800">Pedido #${id}</h3>
        <span class="text-xs px-2 py-1 rounded-full bg-gray-100 text-gray-700">${estado ?? '-'}</span>
      </div>
      <div class="flex items-center justify-between text-sm text-gray-500 mb-3">
        <a href="/admin/clientes/${clienteId ?? ''}" class="text-blue-600 hover:underline font-medium">
          ${escaparHtml(clienteNombre ?? 'Sin cliente')}
        </a>
        <span>${rangoHoras}</span>
      </div>
      ${renderItems(items)}
    </div>
  `;
};

const renderResultados = (pedidos) => {
    contenedor.innerHTML = pedidos.length
        ? pedidos.map(renderPedido).join('')
        : '<p class="text-gray-500 text-sm">No se encontraron pedidos con esos filtros.</p>';
};

const buscarHistorial = async () => {
    contenedor.innerHTML = '<p class="text-gray-400 text-sm">Cargando...</p>';
    try {
        const query = construirQueryParams();
        const respuesta = await fetch(`/admin/historial-pedidos/buscar?${query}`);
        if (!respuesta.ok) {
            throw new Error(`Error ${respuesta.status}`);
        }
        const pedidos = await respuesta.json();
        renderResultados(pedidos);
    } catch (error) {
        contenedor.innerHTML = `<p class="text-red-600 text-sm">Ocurrió un error al buscar los pedidos: ${error.message}</p>`;
    }
};

form.addEventListener('submit', (evento) => {
    evento.preventDefault();
    buscarHistorial();
});

form.addEventListener('reset', () => {
    setTimeout(buscarHistorial, 0);
});

// El script se carga como type="module" (defer implícito), así que el DOM ya
// existe cuando llegamos acá: no hace falta esperar DOMContentLoaded.
buscarHistorial();