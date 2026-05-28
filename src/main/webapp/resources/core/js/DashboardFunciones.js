import { importTimer, executeImport, closeImportModal } from './importarTimer.js';

document.addEventListener('click', (e) => {
    const btnImportar = e.target.closest('.btn-importar');
    if (btnImportar) {
        const { timerId, productName, location } = btnImportar.dataset;
        importTimer(timerId, productName, location);
        return;
    }

    const btn = e.target.closest('.btn-action[data-action]');
    if (!btn) return;

    e.preventDefault();

    const { action, timerId, categoryId } = btn.dataset;

    console.log(`Acción: ${action}, ID: ${timerId}`);

    switch (action) {
        case 'delete':
            deleteTimer(timerId, categoryId);
            break;

        case 'renew':
            updateTimer(timerId, categoryId);
            break;

        case 'import':
            const { productName, location } = btn.dataset;
            importTimer(timerId, productName, location);
            break;

        case 'confirm-import':
            executeImport(timerId, categoryId);
            break;
        case 'closeImportModal':
            closeImportModal();
            break;
        default:
            console.warn('Acción no reconocida:', action);
    }
});