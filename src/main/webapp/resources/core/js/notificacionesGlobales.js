/*
 * Script de Monitoreo Global de Notificaciones de Vencimiento..
 */

const UMBRAL_CRITICO_GLOBAL = 15; // Umbral en minutos para considerar un vencimiento crítico

/**
 * Función principal que inicializa el monitoreo global cuando el DOM está listo.
 * Solicita permisos al navegador y programa la verificación periódica cada 10 segundos.
 */
function inicializarMonitoreoGlobal() {
  solicitarPermisosNotificacion();
  
  // Realizar una primera verificación inmediatamente y luego repetir cada 10 segundos
  monitorearTimersGlobales();
  setInterval(monitorearTimersGlobales, 10000);
}

/**
 * Solicita permiso al sistema para desplegar notificaciones nativas de escritorio.
 */
function solicitarPermisosNotificacion() {
  if ("Notification" in window && Notification.permission !== "granted" && Notification.permission !== "denied") {
    Notification.requestPermission();
  }
}

/**
 * Parsea la fecha de vencimiento recibida del backend y calcula los minutos restantes respecto al tiempo actual.
 * @param {string} fechaVencimientoStr 
 * @returns {number}
 */
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

/**
 * Consulta en segundo plano al endpoint /api/timers/activos para obtener todos los temporizadores del sistema.
 * Analiza el tiempo restante de cada uno y dispara la alerta si se encuentran en el umbral crítico.
 */
async function monitorearTimersGlobales() {
  try {
    const respuesta = await fetch("/api/timers/activos");
    if (!respuesta.ok) return;
    
    const timers = await respuesta.json();
    const notificados = new Set(JSON.parse(sessionStorage.getItem("notificados") || "[]"));
    
    timers.forEach(timer => {
      const minutosFaltantes = calcularMinutosRestantes(timer.fechaVencimiento);
      
      // Verificamos que esté dentro de los 15 minutos (pero que sea mayor o igual a 0, para no alertar si ya venció)
      if (!isNaN(minutosFaltantes) && minutosFaltantes <= UMBRAL_CRITICO_GLOBAL && minutosFaltantes >= 0) {
        const idStr = timer.id.toString();
        if (!notificados.has(idStr)) {
          dispararNotificacionGlobal(timer, notificados);
        }
      }
    });
  } catch (error) {
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
  
  // 2. Intentar reproducir sonido de alerta
  try {
    const audio = new Audio("/sounds/alert.mp3");
    audio.play().catch(() => {}); // Si falla (ej. por falta de archivo 404 o política de autoplay), se ignora
  } catch (e) {
    // Ignorado intencionalmente si el audio no puede reproducirse
  }
  
  // 3. Mostrar el modal visual en pantalla si la página actual cuenta con el fragmento HTML del modal
  const modal = document.getElementById("notificacion-modal");
  const notifNombre = document.getElementById("notif-producto-nombre");
  const notifLoc = document.getElementById("notif-producto-ubicacion");
  
  if (modal && notifNombre && notifLoc) {
    notifNombre.textContent = nombre;
    notifLoc.textContent = ubicacion;
    modal.classList.remove("hidden");
  }
  
  // 4. Guardar en memoria de sesión que ya se alertó sobre este ID
  notificados.add(idStr);
  sessionStorage.setItem("notificados", JSON.stringify([...notificados]));
}

// Iniciar el monitoreo cuando la página cargue
document.addEventListener("DOMContentLoaded", inicializarMonitoreoGlobal);
