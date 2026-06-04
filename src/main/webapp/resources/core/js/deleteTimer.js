export function deleteTimer(timerId, categoryId) {
  if (!confirm("¿Estás seguro de que deseas eliminar este timer?")) return;

  const card = document.getElementById(`timer-${timerId}`);

  if (card) {
    card.style.opacity = "0.5";
    card.style.pointerEvents = "none";
  }

  fetch(`active-timers/eliminarTimer/${timerId}`, {
    method: "DELETE",
    headers: {
      "Content-Type": "application/json",
      "X-CSRF-TOKEN": document.querySelector("meta[name=\"csrf-token\"]")?.getAttribute("content")
    }
  })
    .then(response => {
      if (response.ok) {
        if (card) card.remove();
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