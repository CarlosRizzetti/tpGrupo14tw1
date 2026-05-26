document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll(".btn-eliminar").forEach(btn => {
    btn.addEventListener("click", () => {
      const timerId = btn.getAttribute("data-timer-id");
      const categoryId = btn.getAttribute("data-category-id");
      const card = document.getElementById(`timer-${timerId}`);
      eliminarTimer(timerId, categoryId, card);
    });
  });
});

function eliminarTimer(timerId, categoryId, card) {
  if (!confirm("¿Estás seguro de que deseas eliminar este timer?")) return;

  if (card) {
    card.style.opacity = "0.5";
    card.style.pointerEvents = "none";
  }

  fetch(`active-timers/${timerId}/${categoryId}`, {
    method: "DELETE",
    headers: {
      "Content-Type": "application/json",
      "X-CSRF-TOKEN": document.querySelector("meta[name=\"csrf-token\"]")?.getAttribute("content")
    }
  })
    .then(response => {
      if (response.ok) {
        card.remove();
        if (document.querySelectorAll(".timer").length === 0) {
          location.reload();
        }
      } else {
        alert("Hubo un error al eliminar el timer.");
        if (card) {
          card.style.opacity = "1";
          card.style.pointerEvents = "auto";
        }
      }
    })
    .catch(error => {
      console.error("Error:", error);
      alert("Hubo un error al eliminar el timer.");
      if (card) {
        card.style.opacity = "1";
        card.style.pointerEvents = "auto";
      }
    });
}
