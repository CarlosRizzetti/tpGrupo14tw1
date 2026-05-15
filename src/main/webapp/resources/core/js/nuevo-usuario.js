const btnRegistrarme = document.getElementById("btn-registrarme");
const inputEmail = document.getElementById("email");
const inputPassword = document.getElementById("password");

// Hint elements
const hintLen = document.getElementById("hint-len");
const hintUpper = document.getElementById("hint-upper");
const hintNum = document.getElementById("hint-num");

function validarPassword(password) {
    const isLenValid = password.length >= 6 && password.length <= 10;
    const hasUpper = /[A-Z]/.test(password);
    const hasNum = /\d/.test(password);

    // Update hints immediately
    updateHintState(hintLen, isLenValid);
    updateHintState(hintUpper, hasUpper);
    updateHintState(hintNum, hasNum);

    return isLenValid && hasUpper && hasNum;
}

function updateHintState(element, isMet) {
    if (isMet) {
        element.classList.add("met");
    } else {
        element.classList.remove("met");
    }
}

function updateSubmitButton() {
    const emailValid = inputEmail.checkValidity() && inputEmail.value.length > 0;
    const passwordValid = validarPassword(inputPassword.value);
    
    // Enable button only if both are valid
    btnRegistrarme.disabled = !(emailValid && passwordValid);
}

// Attach events
inputEmail.addEventListener("input", updateSubmitButton);
inputPassword.addEventListener("input", updateSubmitButton);

// Initial check on page load
updateSubmitButton();
