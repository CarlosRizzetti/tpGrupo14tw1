const btnRegistrarme = document.getElementById("btn-registrarme");
const formRegistrarme = document.getElementById("form-registrarme");
const inputEmail = document.getElementById("email");
const inputPassword = document.getElementById("password");
const inputConfirmPassword = document.getElementById("confirm-password");

const hintLen = document.getElementById("hint-len");
const hintUpper = document.getElementById("hint-upper");
const hintNum = document.getElementById("hint-num");
const hintMatch = document.getElementById("hint-match");
const matchError = document.getElementById("password-match-error");

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
  const showError = confirmPassword.length > 0 && !match;

  updateHintState(hintMatch, match);
  matchError.classList.toggle("hidden", !showError);
  inputConfirmPassword.setCustomValidity(
    showError ? "Las contraseñas no coinciden" : ""
  );
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

formRegistrarme.addEventListener("submit", (event) => {
  const matchValid = validarCoincidencia();
  if (!matchValid || !formRegistrarme.checkValidity()) {
    matchError.classList.toggle("hidden", matchValid);
    inputConfirmPassword.reportValidity();
    event.preventDefault();
  }
});

updateSubmitButton();
