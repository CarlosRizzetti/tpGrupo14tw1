
const filterDescripcion = document.getElementById("filterDescripcion");
const filterEstado = document.getElementById("filterEstado");
const filterNumeroLote = document.getElementById("filterNumeroLote");
const filterFechaExacta = document.getElementById("filterFechaExacta");
const sortFecha = document.getElementById("sortFecha");
const cantidadTotalSpan = document.getElementById("cantidadTotal");
const tbody = document.getElementById("articulosTableBody");
const rows = [...document.querySelectorAll(".articulo-row")];

const coincideTexto = (valor = "", filtro = "") => valor.toLowerCase().includes(filtro.toLowerCase().trim());

const coincideFila = (row) => {
  const nombre = row.querySelector(".articulo-nombre")?.textContent ?? "";
  const { estado = "", numeroLote = "", fechaIngreso = "" } = row.dataset;

  const textoFiltro = filterDescripcion?.value ?? "";
  const estadoFiltro = filterEstado?.value ?? "";
  const numeroLoteFiltro = filterNumeroLote?.value.trim() ?? "";
  const fechaFiltro = filterFechaExacta?.value ?? "";

  return (
    coincideTexto(nombre, textoFiltro) &&
        (!estadoFiltro || estado === estadoFiltro) &&
        (!numeroLoteFiltro || numeroLote.includes(numeroLoteFiltro)) &&
        (!fechaFiltro || fechaIngreso === fechaFiltro)
  );
};

const obtenerCantidad = (row) => Number(row.querySelector(".articulo-cantidad")?.dataset.cantidad ?? 0);

const formatearCantidad = (cantidad) => (Number.isInteger(cantidad) ? cantidad : cantidad.toFixed(2));

const filtrarTabla = () => {
  const filasVisibles = rows.filter((row) => {
    const visible = coincideFila(row);
    row.style.display = visible ? "" : "none";
    return visible;
  });

  const cantidadFiltrada = filasVisibles.reduce((total, row) => total + obtenerCantidad(row), 0);

  if (cantidadTotalSpan) {
    cantidadTotalSpan.textContent = formatearCantidad(cantidadFiltrada);
  }
};

const ordenarPorFecha = () => {
  const orden = sortFecha?.value;
  if (!orden || !tbody) return;

  const filasOrdenadas = [...rows].sort((a, b) => {
    const tsA = Number(a.dataset.fechaIngresoTs);
    const tsB = Number(b.dataset.fechaIngresoTs);
    return orden === "desc" ? tsB - tsA : tsA - tsB;
  });

  filasOrdenadas.forEach((fila) => tbody.appendChild(fila));
};

const irADetalleLote = ({ currentTarget }) => {
  const { id } = currentTarget.dataset;
  if (id) {
    window.location.href = `/admin/lotes/${id}`;
  }
};

rows.forEach((row) => row.addEventListener("click", irADetalleLote));

[filterDescripcion, filterNumeroLote, filterFechaExacta].forEach((input) => {
  input?.addEventListener("input", filtrarTabla);
});
filterEstado?.addEventListener("change", filtrarTabla);
sortFecha?.addEventListener("change", ordenarPorFecha);

filtrarTabla();