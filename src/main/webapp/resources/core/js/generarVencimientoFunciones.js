
function cambiarInputs (e){
    const select = e.target.closest(".vencimiento-select");
    if (!select) return;
    const id = select.dataset.id;
    const numericContainer = document.getElementById(`numeric-container-${id}`);
    const dateContainer = document.getElementById(`date-container-${id}`);
    if (select.value === 'custom') {
        numericContainer.classList.add('hidden');
        dateContainer.classList.remove('hidden');
    } else {
        numericContainer.classList.remove('hidden');
        dateContainer.classList.add('hidden');
        document.getElementById(`date-${id}`).value = '';
    }
};


function enviarFormulario(e) {
    const form = e.target.closest("form[id^='form-']");
    if(!form) return;
    const select = form.querySelector(".vencimiento-select");
    if (!select) return;
    const id = select.dataset.id;
    const valInput = document.getElementById(`val-${id}`);
    const dateInput = document.getElementById(`date-${id}`).value;
    const finalInput = document.getElementById(`final-${id}`);


    if (select.value === 'custom' && dateInput) {
        const selectedDate = new Date(dateInput);
        const now = new Date();
        const diffMs = now - selectedDate;
        finalInput.value = diffMs > 0 ? Math.floor(diffMs / 60000) : 0;
    } else {
        const value = parseFloat(valInput.value) || 0;
        const unit = parseFloat(select.value) || 0;
        finalInput.value = Math.floor(value * unit);
    }

    console.log("Minutos a restar calculados: " + finalInput.value);
    form.submit;
}

document.addEventListener("submit", enviarFormulario);
document.addEventListener("change", cambiarInputs);
