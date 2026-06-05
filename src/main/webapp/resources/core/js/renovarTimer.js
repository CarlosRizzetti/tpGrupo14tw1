export function renovarTimer(id, categoryId) {
  if (!confirm("¿Estás seguro de que quieres renovar este vencimiento?")) return;

  const card = document.getElementById(`timer-${id}`);
  if (card) {
    card.style.opacity = "0.5";
    card.style.pointerEvents = "none";
  }

  fetch(`/active-timers/renovarTimer/${id}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json"
    }
  })
    .then(response => response.json())
    .then(data => {
      if (data.status === "ok") {
        updateCardDOM(card, id, categoryId, data);

        card.classList.add("animate-success", "ring-4", "ring-green-500/50");
        setTimeout(() => {
          card.classList.remove("animate-success", "ring-4", "ring-green-500/50");
        }, 800);

        let notificados = JSON.parse(sessionStorage.getItem("notificados_mcd")) || [];
        notificados = notificados.filter(item => item !== id.toString());
        sessionStorage.setItem("notificados_mcd", JSON.stringify(notificados));
      } else {
        alert(data.message || "Hubo un error al renovar el timer.");
      }
    })
    .catch(error => {
      console.error("Error:", error);
      alert("Error de conexión con el servidor.");
    })
    .finally(() => {
      if (card) {
        card.style.opacity = "1";
        card.style.pointerEvents = "auto";
      }
    });
}

function updateCardDOM(card, id, categoryId, data) {
  const newId = data.nuevoTimerId;

  card.id = `timer-${newId}`;
  card.setAttribute("data-expires", data.fechaVencimiento);

  const spans = {
    [`elab-${id}`]: { text: formatearFecha(data.fechaElaboracion), newId: `elab-${newId}` },
    [`vence-${id}`]: { text: formatearFecha(data.fechaVencimiento), newId: `vence-${newId}` }
  };

  Object.entries(spans).forEach(([oldId, { text, newId: spanNewId }]) => {
    const span = document.getElementById(oldId);
    if (span) {
      span.innerText = text;
      span.id = spanNewId;
    }
  });

  ["btn-eliminar", "btn-importar", "btn-renovar"].forEach(btnClass => {
    card.querySelector(`.${btnClass}`)?.setAttribute("data-timer-id", `${newId}`);
  });


}

function formatearFecha(isoString) {
  const fecha = new Date(isoString);
  return fecha.toLocaleDateString("es-AR", {
    day: "2-digit",
    month: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    hour12: false
  }).replace(",", "");
}