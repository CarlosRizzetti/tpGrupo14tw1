document.addEventListener("DOMContentLoaded", () => {
    const loginForm = document.querySelector("form");

    if (loginForm) {
        loginForm.addEventListener("submit", (e) => {
            const recaptchaResponse = grecaptcha.getResponse();

            if (recaptchaResponse.length === 0) {
                e.preventDefault(); // Detenemos el envío

                // Buscamos el contenedor por su ID
                let errorContainer = document.getElementById("error-container");

                if (errorContainer) {
                    // 1. Si el contenedor YA EXISTE (Ej: Falló una validación del backend antes o ya hicimos clic)
                    errorContainer.removeAttribute("hidden");
                    const errorText = errorContainer.querySelector("span");
                    if (errorText) {
                        errorText.textContent = "Por favor, marcá la casilla de verificación para demostrar que no sos un robot.";
                    }
                } else {
                    // 2. Si el contenedor NO EXISTE, lo creamos dinámicamente
                    errorContainer = document.createElement("div");
                    errorContainer.id = "error-container";
                    // Le pegamos exactamente tus mismas clases de Tailwind
                    errorContainer.className = "mb-6 p-4 bg-red-100/90 border-l-4 border-red-500 text-red-800 rounded shadow-inner";

                    const errorText = document.createElement("span");
                    errorText.className = "font-bold text-sm";
                    errorText.textContent = "Por favor, marcá la casilla de verificación para demostrar que no sos un robot.";

                    // Armamos la estructura: metemos el span adentro del div
                    errorContainer.appendChild(errorText);

                    // Lo inyectamos en el DOM, justo antes de que empiece el formulario
                    loginForm.parentNode.insertBefore(errorContainer, loginForm);
                }
            }
        });
    }
});