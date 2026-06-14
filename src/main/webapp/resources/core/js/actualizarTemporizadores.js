class DashboardTimer {
  constructor() {
    this.UMBRAL_CRITICO = 15;
    this.UMBRAL_ADVERTENCIA = 45;

    this.notificados = new Set(JSON.parse(sessionStorage.getItem("notificados") || "[]"));
    this.alertaSonido = new Audio("/sounds/alert.mp3");

    this.modal = document.getElementById("notificacion-modal");
    this.notifNombre = document.getElementById("notif-producto-nombre");
    this.notifLoc = document.getElementById("notif-producto-ubicacion");

    this.init();
    this.setearFechasElaboracionYVencimiento();
  }


  init() {
    setInterval(() => this.actualizarTodos(), 1000);
    this.actualizarTodos();

    //Modal notificación
    const closeBtn = document.getElementById("cerrarNotificacion");
    if (closeBtn) {
      closeBtn.addEventListener("click", () => this.modal.classList.add("hidden"));
    }
  }


  actualizarTodos() {
    const cards = document.querySelectorAll(".timer");
    const now = Date.now();

    cards.forEach(card => {
      const id = card.id.replace("timer-", "");
      const vence = new Date(card.getAttribute("data-vencimiento")).getTime();
      const distancia = vence - now;

      this.renderizarCard(card, id, distancia);
    });
  }


  renderizarCard(card, id, distance) {
    const display = document.getElementById(`display-${id}`);
    if (!display) return;

    if (isNaN(distance) || distance < 0) {
      this.aplicarEstilo(card, display, "vencido");
      display.innerText = "VENCIDO";
      return;
    }

    const minutosQueFaltan = distance / 1000 / 60;
    display.innerText = this.formatearFecha(distance);

    if (minutosQueFaltan <= this.UMBRAL_CRITICO) {
      this.aplicarEstilo(card, display, "critico");
      this.enviarNotificacion(id, card);
    } else if (minutosQueFaltan <= this.UMBRAL_ADVERTENCIA) {
      this.aplicarEstilo(card, display, "advertencia");
    } else {
      this.aplicarEstilo(card, display, "normal");
    }
  }

  setearFechasElaboracionYVencimiento(){
    const cards = document.querySelectorAll(".timer");
    cards.forEach(card => {
      const id = card.id.replace("timer-", "");
      const vence = new Date(card.getAttribute("data-vencimiento"));
      const elaborado = new Date(card.getAttribute("data-elaboracion"));

      const elabTextEl = document.getElementById(`elab-${id}`);
      const venceTextEl = document.getElementById(`vence-${id}`);
      console.log(elabTextEl, venceTextEl);

      const opciones = { day: "2-digit", month: "2-digit", hour: "2-digit", minute: "2-digit", hour12: false };

      if (elabTextEl && venceTextEl) {
        elabTextEl.textContent = elaborado.toLocaleString("es-AR", opciones).replace(",", "");
        venceTextEl.textContent = vence.toLocaleString("es-AR", opciones).replace(",", "");
      }
    });

  }

  formatearFecha(ms) {
    const tiempo = Math.max(0, ms);
    const MS_POR_DIA = 24 * 60 * 60 * 1000;

    if (tiempo >= MS_POR_DIA) {
      const dias = Math.floor(tiempo / MS_POR_DIA);
      const horasRestantes = Math.floor((tiempo % MS_POR_DIA) / 3600000);

      return `${dias}D ${horasRestantes}h`;
    }

    const horas = Math.floor(tiempo / 3600000);
    const minutos = Math.floor((tiempo % 3600000) / 60000);
    const segundos = Math.floor((tiempo % 60000) / 1000);

    return `${horas.toString().padStart(2, "0")}:${minutos.toString().padStart(2, "0")}:${segundos.toString().padStart(2, "0")}`;
  }

  aplicarEstilo(card, display, estado) {
    const container = display.parentElement;
    card.classList.remove("border-red-600", "border-yellow-500", "border-green-500", "border-puesto-header");
    container.classList.remove(
      "bg-gray-50", "border-gray-200",
      "bg-red-100", "bg-yellow-100", "bg-green-100",
      "border-red-600", "border-yellow-500", "border-green-500"
    );

    display.classList.remove("text-red-600", "text-yellow-700", "text-green-700", "text-gray-700");

    const styles = {
      vencido:     { border: "border-red-600",    bg: "bg-red-100",    text: "text-red-600",    borderCont: "border-red-600" },
      critico:     { border: "border-red-600",    bg: "bg-red-100",    text: "text-red-600",    borderCont: "border-red-600" },
      advertencia: { border: "border-yellow-500", bg: "bg-yellow-100", text: "text-yellow-700", borderCont: "border-yellow-500" },
      normal:      { border: "border-green-500",  bg: "bg-green-100",  text: "text-green-700",  borderCont: "border-green-500" }
    };

    const s = styles[estado];

    card.classList.add(s.border);
    container.classList.add(s.bg, s.borderCont);
    display.classList.add(s.text);
  }

  enviarNotificacion(id, card) {
    if (this.notificados.has(id)) return;

    const nombre = card.querySelector(`#nombre-${id}`)?.textContent || "Producto";
    const ubicacion = card.querySelector(`#ubicacion-${id}`)?.textContent || "General";

    this.alertaSonido.play().catch(() => console.log("Interacción requerida para audio"));

    this.notifNombre.textContent = nombre;
    this.notifLoc.textContent = ubicacion;
    this.modal.classList.remove("hidden");

    sessionStorage.setItem("notificados", JSON.stringify([...this.notificados]));
    this.notificados.add(id);
  }
}

let timersInstancia;
document.addEventListener("DOMContentLoaded", () => {
  timersInstancia = new DashboardTimer();
});
export function getTimers(){
  return timersInstancia;
}