export async function importTimer(timerId, productName, location) {
  document.body.style.cursor = "wait";

  try {
    const response = await fetch(`/timers/${timerId}/categories`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Accept": "application/json"
      }
    });

    const data = await response.json();

    if (!response.ok) throw new Error("Error al cargar categorías");

    openImportModal(timerId, data.categorias, productName, location);

  } catch (error) {
    console.error(error);
    alert("Error al obtener las categorías disponibles.");
  } finally {
    document.body.style.cursor = "default";
  }
}

export function openImportModal(timerId, categories, productName, location) {
  const listContainer = document.getElementById("modal-categories-list");
  const modal = document.getElementById("import-modal");
  const nameDisplay = document.getElementById("modal-product-name");
  const locDisplay = document.getElementById("modal-product-location");
  const hiddenInput = document.getElementById("modal-timer-id");

  if (nameDisplay) nameDisplay.textContent = productName;
  if (locDisplay) locDisplay.textContent = location;
  if (hiddenInput) hiddenInput.value = timerId;

  listContainer.innerHTML = "";

  if (categories && categories.length > 0) {
    categories.forEach(cat => {
      const btn = document.createElement("button");
      const isDisabled = cat.estaPresente ?? false;

      btn.className = `btn-action w-full flex items-center justify-between p-4 border-2 rounded-xl transition-all group text-left mb-2 text-sm
        ${isDisabled
    ? "bg-gray-100 border-gray-100 text-gray-400 cursor-not-allowed"
    : "bg-white border-gray-200 hover:border-blue-500 hover:bg-blue-50 hover:text-blue-700"}`;

      btn.disabled = isDisabled;

      btn.innerHTML = `
        <span class="font-black uppercase ${isDisabled ? "" : "group-hover:text-blue-700"}">
          ${cat.nombre}
          ${isDisabled ? "<span class=\"text-[10px] ml-2 bg-gray-200 px-2 py-0.5 rounded text-gray-500\">YA EXISTE</span>" : ""}
        </span>
        ${!isDisabled
    ? "<svg class=\"w-5 h-5 text-gray-300 group-hover:text-blue-500\" fill=\"none\" stroke=\"currentColor\" viewBox=\"0 0 24 24\"><path stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-width=\"2\" d=\"M12 4v16m8-8H4\"></path></svg>"
    : ""}
      `;

      if (!isDisabled) {
        btn.dataset.action = "confirm-import";
        btn.dataset.timerId = timerId;
        btn.dataset.categoryId = cat.id;
      }

      listContainer.appendChild(btn);
    });
  } else {
    listContainer.innerHTML = `
      <div class="text-center py-8 text-gray-400">
        <p class="font-bold uppercase text-sm">Sin categorías disponibles</p>
      </div>`;
  }

  modal.classList.remove("hidden");
}

export async function executeImport(timerId, categoryId) {
  const listContainer = document.getElementById("modal-categories-list");
  listContainer.style.opacity = "0.5";
  listContainer.style.pointerEvents = "none";
  document.body.style.cursor = "wait";

  try {
    const response = await fetch(`/import-timer/${timerId}/${categoryId}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Accept": "application/json"
      }
    });

    const data = await response.json();

    if (data.success && data.message === "Timer importado correctamente") {
      window.location.href = "/dashboard";
    } else {
      throw new Error(data.message || "Error desconocido al importar");
    }

  } catch (error) {
    console.error(error);
    alert(error.message);
  } finally {
    listContainer.style.opacity = "1";
    listContainer.style.pointerEvents = "auto";
    document.body.style.cursor = "default";
  }
}

export function closeImportModal() {
  const modal = document.getElementById("import-modal");
  if (modal) modal.classList.add("hidden");
  document.body.style.cursor = "default";
}