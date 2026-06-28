/* global ApexCharts */

/**
 * Estadísticas: consume /estadisticas/datos y renderiza los gráficos con ApexCharts
 * (motor utilizado por el plugin de charts de Flowbite: https://flowbite.com/docs/plugins/charts/).
 */

const COLOR_PRIMARIO = "#1A56DB";
const COLOR_SECUNDARIO = "#7E3AF2";

const graficos = {};

const obtenerEtiquetas = (serie) => serie.map((p) => p.etiqueta);
const obtenerValores = (serie) => serie.map((p) => p.valor);

const opcionesBase = (categorias, nombreSerie, datos, color, tipo) => ({
  chart: {
    type: tipo,
    height: 300,
    fontFamily: "Figtree, sans-serif",
    toolbar: { show: false },
  },
  series: [{ name: nombreSerie, data: datos, color: color }],
  xaxis: {
    categories: categorias,
    labels: { style: { fontSize: "11px" } },
  },
  yaxis: {
    labels: { formatter: (valor) => Math.round(valor) },
  },
  plotOptions: {
    bar: { borderRadius: 4, columnWidth: "55%" },
  },
  dataLabels: { enabled: false },
  stroke: { curve: "smooth", width: tipo === "line" ? 3 : 0 },
  grid: { borderColor: "#E5E7EB", strokeDashArray: 4 },
  tooltip: { theme: "light" },
});

const PALETA_TORTA = [
  "#1A56DB",
  "#7E3AF2",
  "#16BDCA",
  "#FDBA8C",
  "#E74694",
  "#9061F9",
  "#31C48D",
  "#F05252",
];

const opcionesTorta = (etiquetas, valores) => ({
  chart: {
    type: "donut",
    height: 320,
    fontFamily: "Figtree, sans-serif",
  },
  series: valores,
  labels: etiquetas,
  colors: PALETA_TORTA,
  stroke: { colors: ["#ffffff"] },
  dataLabels: { enabled: true },
  legend: { position: "bottom" },
  plotOptions: {
    pie: { donut: { size: "55%" } },
  },
  tooltip: { theme: "light" },
});

const COLORES_ESTADO = ["#F05252", "#1A56DB", "#31C48D"];

// radialBar espera porcentajes: convertimos los conteos a % sobre el total
// y mostramos el conteo real en la leyenda.
const opcionesRadial = (etiquetas, valores) => {
  const total = valores.reduce((acc, valor) => acc + valor, 0);
  const porcentajes = valores.map((valor) =>
    total > 0 ? Math.round((valor / total) * 100) : 0
  );

  return {
    chart: {
      type: "radialBar",
      height: 320,
      fontFamily: "Figtree, sans-serif",
    },
    series: porcentajes,
    labels: etiquetas,
    colors: COLORES_ESTADO,
    plotOptions: {
      radialBar: {
        hollow: { size: "35%" },
        dataLabels: {
          name: { fontSize: "13px" },
          value: { formatter: (val) => `${val}%` },
        },
      },
    },
    legend: {
      show: true,
      position: "bottom",
      formatter: (nombre, opts) => `${nombre}: ${valores[opts.seriesIndex]}`,
    },
    tooltip: { enabled: true },
  };
};

const dibujar = (idContenedor, opciones) => {
  if (graficos[idContenedor]) {
    graficos[idContenedor].updateOptions(opciones);
    return;
  }
  const contenedor = document.getElementById(idContenedor);
  if (!contenedor || typeof ApexCharts === "undefined") {
    return;
  }
  graficos[idContenedor] = new ApexCharts(contenedor, opciones);
  graficos[idContenedor].render();
};

const renderizar = (datos) => {
  dibujar(
    "grafico-vencimientos",
    opcionesBase(
      obtenerEtiquetas(datos.vencimientosPorDia),
      "Vencimientos",
      obtenerValores(datos.vencimientosPorDia),
      COLOR_PRIMARIO,
      "line"
    )
  );

  dibujar(
    "grafico-stock",
    opcionesBase(
      obtenerEtiquetas(datos.modificacionesStockPorDia),
      "Modificaciones",
      obtenerValores(datos.modificacionesStockPorDia),
      COLOR_SECUNDARIO,
      "line"
    )
  );

  dibujar(
    "grafico-dia-semana",
    opcionesBase(
      obtenerEtiquetas(datos.demandaPorDiaSemana),
      "Egresos",
      obtenerValores(datos.demandaPorDiaSemana),
      COLOR_PRIMARIO,
      "bar"
    )
  );

  dibujar(
    "grafico-hora",
    opcionesBase(
      obtenerEtiquetas(datos.demandaPorHora),
      "Egresos",
      obtenerValores(datos.demandaPorHora),
      COLOR_SECUNDARIO,
      "bar"
    )
  );

  dibujar(
    "grafico-productos",
    opcionesTorta(
      obtenerEtiquetas(datos.productosMasUtilizados),
      obtenerValores(datos.productosMasUtilizados)
    )
  );

  dibujar(
    "grafico-estados",
    opcionesRadial(
      obtenerEtiquetas(datos.vencimientosPorEstado),
      obtenerValores(datos.vencimientosPorEstado)
    )
  );
};

const mostrarError = (mostrar) => {
  const error = document.getElementById("estadisticas-error");
  if (error) {
    error.classList.toggle("hidden", !mostrar);
  }
};

const cargar = async (dias) => {
  try {
    const respuesta = await fetch(`/admin/estadisticas/datos?dias=${dias}`, {
      headers: { Accept: "application/json" },
    });
    if (!respuesta.ok) {
      throw new Error(`Respuesta ${respuesta.status}`);
    }
    const datos = await respuesta.json();
    mostrarError(false);
    renderizar(datos);
  } catch {
    mostrarError(true);
  }
};

document.addEventListener("DOMContentLoaded", () => {
  const filtro = document.getElementById("filtro-dias");
  const diasIniciales = filtro ? filtro.value : "30";

  cargar(diasIniciales);

  if (filtro) {
    filtro.addEventListener("change", () => cargar(filtro.value));
  }
});
