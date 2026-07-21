# 🍔 MCD Vencimientos — Sistema Integral de Gestión Gastronómica

Sistema web full-stack que digitaliza la operación diaria de un local gastronómico (inspirado en la operatoria real de McDonald's): **control de vencimientos**, **punto de venta (POS)**, **comandas de cocina en tiempo real** y **trazabilidad de stock por lotes** con enfoque en auditoría sanitaria.

El proyecto nació de una necesidad real: como empleado del local, detecté que el control de vida útil de los alimentos se hacía con etiquetas manuales, lo que generaba errores operativos. Este sistema reemplaza ese proceso de punta a punta.

> 🎓 Desarrollado en el marco de la materia **Taller Web I** (Tecnicatura en Desarrollo Web, UNLaM).

---

## ✨ Funcionalidades principales

### ⏱️ Gestión de vencimientos (Timers)
- Cada producto que sale a la línea de producción genera un **timer** con fecha de vencimiento calculada automáticamente según reglas configurables (duración, descongelamiento, ubicación).
- Los timers se agrupan por lote, se pueden **renovar** o **importar entre categorías** (Cocina, Servicio, McCafé, Isla), y cambian de estado automáticamente al vencer.
- Impresión de etiquetas de vencimiento en **impresoras térmicas (ESC/POS)**.

### 💰 Punto de venta (Caja)
- Dashboard reactivo de una sola pantalla: el cajero navega el menú por categoría, arma cada producto (puede **retirar ingredientes** a pedido del cliente) y gestiona un carrito vivo, todo **sin recargas de página** (API REST + Fetch).
- Pantalla de cobro con **calculadora de cambio**, búsqueda de cliente por DNI (opcional — se puede cobrar como anónimo) e integración con Mercado Pago prevista.

### 👨‍🍳 Comandas de cocina
- Al cobrar un pedido se genera automáticamente una **comanda** que la cocina ve en tiempo real (polling automático), filtrada según las categorías habilitadas para cada usuario.
- Al marcar una comanda como servida, el sistema **valida y descuenta stock de los timers activos** con lógica FIFO por vencimiento. Si falta stock, alerta al cocinero indicando exactamente qué productos necesitan un timer nuevo.
- La validación se hace en **dos fases (planificación + ejecución)** dentro de una transacción, evitando condiciones de carrera y stock a medio descontar.

### 📦 Trazabilidad de stock por lotes
- El stock primario se gestiona por **lotes** con proveedor, marca, número de lote y fechas de ingreso/vencimiento.
- Regla FIFO automática: solo un lote está `EN_USO` por producto (el más próximo a vencer).
- **Cadena de trazabilidad completa**: cada pedido vendido es rastreable hasta el lote y proveedor de origen.

```
Proveedor → Lote → Timer → Comanda → Pedido → Cliente
```

Ante un retiro sanitario (ej: "el lote 8892 está contaminado"), el sistema puede responder qué pedidos y clientes recibieron productos de ese lote.

### 🔐 Seguridad y usuarios
- Autenticación con **Spring Security**: login tradicional y **OAuth2 con Google**.
- Protección de registro con **Google reCAPTCHA**.
- Roles y permisos por categoría: cada usuario ve solo las secciones que atiende (los administradores ven todo).

---

## 🛠️ Stack tecnológico

| Capa | Tecnologías |
|---|---|
| **Backend** | Java, Spring Framework (MVC, Security, OAuth2), Hibernate / JPA |
| **Frontend** | Thymeleaf, JavaScript (ES6+, Fetch API), Tailwind CSS |
| **Base de datos** | MySQL (modelo relacional de 15+ entidades) |
| **Infraestructura** | Docker, Maven, Jetty |
| **Calidad de código** | PMD (análisis estático), Lombok, principios SOLID |
| **Hardware** | Impresión térmica ESC/POS |

**Arquitectura**: MVC en capas estrictas — los controladores solo hablan con servicios, los servicios con repositorios, y el frontend consume DTOs (nunca entidades JPA). Los endpoints JSON viven en controladores REST separados de los de vistas.

---

## 🚀 Cómo iniciar el proyecto

### Requisitos previos
- [Docker](https://www.docker.com/) y Docker Compose
- JDK 11+
- Maven 3.6+

### Pasos

```bash
# 1. Clonar el repositorio
git clone https://github.com/Tahiel-Recchia/<nombre-del-repo>.git
cd <nombre-del-repo>

# 2. Levantar la base de datos MySQL con Docker
docker compose up -d

# 3. Configurar credenciales (ver sección siguiente)
#    src/main/resources/application.properties

# 4. Compilar y correr la aplicación
mvn clean install
mvn jetty:run

# 5. Abrir en el navegador
#    http://localhost:8080
```

> 💡 El esquema de base de datos se genera automáticamente vía Hibernate (`hbm2ddl`). Los datos iniciales (categorías, productos y usuarios de prueba) se cargan desde el script SQL incluido en el proyecto.

---

## ⚙️ Configuración

Las credenciales externas se configuran en `src/main/resources/application.properties`.

### Google OAuth2 (login con Google)

1. Crear un proyecto en [Google Cloud Console](https://console.cloud.google.com/).
2. En **APIs y servicios → Credenciales**, crear un **ID de cliente de OAuth 2.0** de tipo *Aplicación web*.
3. Agregar como URI de redirección autorizada:
   `http://localhost:8080/login/oauth2/code/google`
4. Copiar las credenciales en `application.properties`:

```properties
spring.security.oauth2.client.registration.google.client-id=TU_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=TU_CLIENT_SECRET
```

### Google reCAPTCHA

1. Registrar el sitio en [Google reCAPTCHA Admin](https://www.google.com/recaptcha/admin) (reCAPTCHA v2).
2. Agregar `localhost` como dominio permitido para desarrollo.
3. Copiar las claves en `application.properties`:

```properties
recaptcha.site-key=TU_SITE_KEY
recaptcha.secret-key=TU_SECRET_KEY
```

> ⚠️ **Nunca subas credenciales reales al repositorio.** El `application.properties` del repo incluye valores de ejemplo; usá un archivo local ignorado por Git o variables de entorno para las claves reales.

---

## 🗂️ Estructura del proyecto

```
src/main/java/com/tallerwebi/
├── dominio/
│   ├── entity/          # Entidades JPA (Producto, Timer, Pedido, Lote, Comanda...)
│   ├── interfaces/      # Interfaces de servicios
│   ├── services/        # Lógica de negocio (@Transactional)
│   ├── utils/           # Modelo en memoria (carrito de sesión)
│   └── exception/       # Excepciones de dominio
├── presentacion/
│   ├── controller/      # Controladores de vistas (MVC) y de API (REST/JSON)
│   └── dto/             # DTOs: contrato entre backend y frontend
└── repositorio/         # Acceso a datos (Hibernate / HQL)

src/main/resources/
├── templates/           # Vistas Thymeleaf (caja, cocina, admin...)
└── static/              # JS vanilla (ES6) y CSS con temas por categoría
```

---

## 📸 Capturas

<!-- Agregar capturas de: dashboard de caja, pantalla de cobro, comandas de cocina, gestión de timers -->
*(Próximamente)*

---

## 👥 Autores

Proyecto desarrollado en equipo para Taller Web I (UNLaM).

- **Tahiel Recchia** — [LinkedIn](https://www.linkedin.com/in/tahiel-recchia/) · [GitHub](https://github.com/Tahiel-Recchia)
Proyecto desarrollado en equipo para Taller Web I (UNLaM).

- **Tahiel Recchia** — [LinkedIn](https://www.linkedin.com/in/tahiel-recchia/) · [GitHub](https://github.com/Tahiel-Recchia)
