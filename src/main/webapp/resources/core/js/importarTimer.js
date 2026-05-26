//import {c} from "caniuse-lite/data/browserVersions.js";

export async function importTimer(timerId, productName, location) {

    document.body.style.cursor = 'wait';
    console.log(timerId);
    try {
        const response = await fetch(`timers/${timerId}/categories`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });

        console.log(response);


        const data = await response.json();
        const categories = data.options;
        console.log(categories);
        if (!response.ok) throw new Error('Error al cargar sectores');
        console.log(categories);
        openImportModal(timerId, categories, productName, location);

    } catch (error) {
        console.error(error);
        alert('Error al obtener los sectores disponibles.');
    } finally {
        document.body.style.cursor = 'default';
    }
}

function openImportModal(timerId, categories, productName, location) {
    const listContainer = document.getElementById('modal-categories-list');
    const modal = document.getElementById('import-modal');
    const nameDisplay = document.getElementById('modal-product-name');
    const locDisplay = document.getElementById('modal-product-location');
    const emptyState = document.getElementById('modal-empty-state');
    const hiddenInput = document.getElementById('modal-timer-id');


    if (nameDisplay) nameDisplay.textContent = productName;
    if (locDisplay) locDisplay.textContent = location;
    if (hiddenInput) hiddenInput.value = timerId;

    listContainer.innerHTML = '';


    if (categories && categories.length > 0) {

        emptyState.classList.add('hidden');
        listContainer.classList.remove('hidden');

        categories.forEach(cat => {
            const btn = document.createElement('button');
            const isDisabled = cat.is_present;

            btn.className = `btn-action w-full flex items-center justify-between p-4 border-2 rounded-xl transition-all group text-left mb-2 text-sm
                ${isDisabled
                    ? 'bg-gray-100 border-gray-100 text-gray-400 cursor-not-allowed'
                    : 'bg-white border-gray-200 hover:border-blue-500 hover:bg-blue-50 hover:text-blue-700'}`;

            btn.disabled = isDisabled;

            btn.innerHTML = `
                <span class="font-black uppercase ${isDisabled ? '' : 'group-hover:text-blue-700'}">
                    ${cat.name} ${isDisabled ? '<span class="text-[10px] ml-2 bg-gray-200 px-2 py-0.5 rounded text-gray-500">YA EXISTE</span>' : ''}
                </span>
                ${!isDisabled ?
                    '<svg class="w-5 h-5 text-gray-300 group-hover:text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>'
                    : ''}
            `;

            if (!isDisabled) {

                btn.dataset.action = 'confirm-import';
                btn.dataset.timerId = timerId;
                btn.dataset.categoryId = cat.id;
            }


            listContainer.appendChild(btn);
        });
    } else {

        listContainer.classList.add('hidden');
        emptyState.classList.remove('hidden');
    }


    modal.classList.remove('hidden');
}

export async function executeImport(timerId, categoryId) {
    const listContainer = document.getElementById('modal-categories-list');
    const originalOpacity = listContainer.style.opacity;
    listContainer.style.opacity = '0.5';
    listContainer.style.pointerEvents = 'none';
    document.body.style.cursor = 'wait';

    try {

        const response = await fetch(`import-timer/${timerId}/${categoryId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });

        console.log('Respuesta recibida', response);


        const data = await response.json();

        if (response.ok && data.success) {
            window.location.href = '/dashboard';
        } else {
            throw new Error(data.message || 'Error desconocido al importar');
        }

    } catch (error) {
        console.error(error);
        alert(error.message);
    } finally {
        listContainer.style.opacity = originalOpacity;
        listContainer.style.pointerEvents = 'auto';
        document.body.style.cursor = 'default';
    }
}

export function closeImportModal() {
    const modal = document.getElementById('import-modal');
    if (modal) {
        modal.classList.add('hidden');
    }

    document.body.style.cursor = 'default';
}

// Listener for the confirm-import buttons that appear in the modal
// It captures the click event, extracts the timerId and categoryId from the
// button's data attributes and calls executeImport.
document.addEventListener('click', function (e) {
    const btn = e.target.closest('button[data-action="confirm-import"]');
    if (btn) {
        const timerId = btn.dataset.timerId;
        const categoryId = btn.dataset.categoryId;
        // Call the import function defined above
        executeImport(timerId, categoryId);
    }
});