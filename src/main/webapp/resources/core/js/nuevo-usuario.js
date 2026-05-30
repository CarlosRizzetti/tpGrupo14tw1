const btnRegistrarme = document.getElementById("btn-registrarme");
const inputEmail = document.getElementById("email");
const inputPassword = document.getElementById("password");
const inputConfirmPassword = document.getElementById("confirm-password");

const hintLen = document.getElementById("hint-len");
const hintUpper = document.getElementById("hint-upper");
const hintNum = document.getElementById("hint-num");
const hintMatch = document.getElementById("hint-match");

function validarPassword(password) {
  const isLenValid = password.length >= 6 && password.length <= 10;
  const hasUpper = /[A-Z]/.test(password);
  const hasNum = /\d/.test(password);

  updateHintState(hintLen, isLenValid);
  updateHintState(hintUpper, hasUpper);
  updateHintState(hintNum, hasNum);

  return isLenValid && hasUpper && hasNum;
}

function validarCoincidencia() {
  const password = inputPassword.value;
  const confirmPassword = inputConfirmPassword.value;
  const match = password === confirmPassword && password.length > 0;

  updateHintState(hintMatch, match);
  return match;
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
  const matchValid = validarCoincidencia();

  btnRegistrarme.disabled = !(emailValid && passwordValid && matchValid);
}

inputEmail.addEventListener("input", updateSubmitButton);
inputPassword.addEventListener("input", updateSubmitButton);
inputConfirmPassword.addEventListener("input", updateSubmitButton);

updateSubmitButton();