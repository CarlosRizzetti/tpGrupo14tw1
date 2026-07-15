

const UMBRAL_CRITICO_GLOBAL = 15;

function inicializarMonitoreoGlobal() {
  solicitarPermisosNotificacion();
  monitorearTimersGlobales();
  setInterval(monitorearTimersGlobales, 10000);
}


function solicitarPermisosNotificacion() {
  if ("Notification" in window && Notification.permission !== "granted" && Notification.permission !== "denied") {
    Notification.requestPermission();
  }
}


function calcularMinutosRestantes(fechaVencimientoStr) {
  if (!fechaVencimientoStr) return NaN;
  
  let formatoIso = fechaVencimientoStr.trim();
  if (formatoIso.indexOf("T") === -1 && formatoIso.indexOf(" ") !== -1) {
    formatoIso = formatoIso.replace(" ", "T");
  }
  
  const venceMs = new Date(formatoIso).getTime();
  const ahoraMs = Date.now();
  return (venceMs - ahoraMs) / (1000 * 60);
}


async function monitorearTimersGlobales() {
  try {
    const respuesta = await fetch("/api/timers/activos");
    if (!respuesta.ok) return;
    
    const timers = await respuesta.json();
    const notificados = new Set(JSON.parse(sessionStorage.getItem("notificados") || "[]"));
    
    timers.forEach(timer => {
      const minutosFaltantes = calcularMinutosRestantes(timer.fechaVencimiento);
      

      if (!isNaN(minutosFaltantes) && minutosFaltantes <= UMBRAL_CRITICO_GLOBAL && minutosFaltantes >= 0) {
        const idStr = timer.id.toString();
        if (!notificados.has(idStr)) {
          dispararNotificacionGlobal(timer, notificados);
        }
      }
    });
  } catch (error) {
    console.error("Error silenciado:", error);
    // Si hay un error de red momentáneo, se ignora silenciosamente para no interrumpir al usuario
  }
}

/**
 * Dispara la alerta visual y sonora cuando se detecta un temporizador crítico.
 * Registra el ID en sessionStorage para garantizar que solo notifique una vez por sesión.
 * @param {Object} timer
 * @param {Set} notificados
 */
function dispararNotificacionGlobal(timer, notificados) {
  const nombre = timer.nombre || "Producto";
  const ubicacion = timer.ubicacion || "General";
  const idStr = timer.id.toString();
  
  // 1. Disparar notificación nativa del sistema operativo/escritorio
  if ("Notification" in window && Notification.permission === "granted") {
    new Notification("¡Timer a punto de vencer!", {
      body: `El producto "${nombre}" en "${ubicacion}" requiere tu atención inmediata.`,
    });
  }

  try {
    const audio = new Audio("/sounds/alert.mp3");
    audio.play().catch(() => {});
  } catch (e) {
    console.error("Error silenciado al reproducir audio:", e);

  }
  

  const modal = document.getElementById("notificacion-modal");
  const notifNombre = document.getElementById("notif-producto-nombre");
  const notifLoc = document.getElementById("notif-producto-ubicacion");
  
  if (modal && notifNombre && notifLoc) {
    notifNombre.textContent = nombre;
    notifLoc.textContent = ubicacion;
    modal.classList.remove("hidden");
  }

  notificados.add(idStr);
  sessionStorage.setItem("notificados", JSON.stringify([...notificados]));
}

document.addEventListener("DOMContentLoaded", inicializarMonitoreoGlobal);
