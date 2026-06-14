# Guía de Configuración Local: Entorno de Correos con Mailtrap

Para probar el envío de correos (como el registro y validación de usuarios) de forma segura y sin que Gmail o Outlook nos bloqueen, utilizamos **Mailtrap**.

⚠️ **Regla de Oro del Equipo:** Cada desarrollador debe usar su **propia cuenta de Mailtrap** y configurar sus credenciales localmente. De esta forma, los correos de prueba de un compañero no aparecerán en la bandeja del otro, evitando confusiones durante el desarrollo. NO COMITEAR CONFIGURACIONES DE MAILTRAP EN EL REPOSITORIO.

---

## Paso 1: Crear tu cuenta en Mailtrap
1. Ingresa a [mailtrap.io](https://mailtrap.io/) y regístrate gratis (puedes usar tu cuenta de Google o GitHub).
2. Una vez dentro del panel principal, ve al menú lateral izquierdo y haz clic en **Email Testing** > **Inboxes**.
3. Haz clic en el buzón llamado **"My Sandbox"** (o "My Inbox").

## Paso 2: Obtener tus credenciales SMTP
Dentro de "My Sandbox", busca la sección central llamada **"SMTP Settings"** o **"Show Credentials"**.
Ahí vas a encontrar dos datos clave que son únicos para tu cuenta:
* **Username** (ej: `13b6c5f52b7e9e`)
* **Password** (haz clic en los asteriscos para copiarla completa)

> *Nota: Mantén esta pestaña del navegador abierta, porque aquí es donde van a llegar todos los correos que envíe tu aplicación local.*

---

## Paso 3: Configurar tu entorno local (`.env`)
Las credenciales del correo no se suben al repositorio por seguridad. El proyecto está preparado para leerlas desde tu archivo `.env` local y pasarlas a los contenedores de Docker.

1. En la raíz del proyecto, busca el archivo llamado `.env_mailtrap`.
2. Haz una copia exacta de ese archivo y renómbrala a **`.env`**.
3. Abre tu nuevo archivo `.env` y pega tus credenciales en las variables correspondientes de Mailtrap:

```env
# ... (Otras configuraciones de BD) ...

# Configuración de Mailtrap (Tus credenciales privadas)
MAILTRAP_HOST=sandbox.smtp.mailtrap.io
MAILTRAP_PORT=2525
MAILTRAP_USER=pega_aqui_tu_username
MAILTRAP_PASSWORD=pega_aqui_tu_password