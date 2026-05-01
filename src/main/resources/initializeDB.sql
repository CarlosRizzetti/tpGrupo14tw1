-- 1. Creación de la tabla Usuario (Agregada)
CREATE TABLE usuario (
                         id SERIAL PRIMARY KEY,
                         nombre VARCHAR(150) NOT NULL,
                         email VARCHAR(150) UNIQUE NOT NULL,
                         fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Creación de la tabla Categoria
CREATE TABLE categoria (
                           id SERIAL PRIMARY KEY,
                           nombre VARCHAR(100) NOT NULL
);

-- 3. Creación de la tabla Producto
CREATE TABLE producto (
                          id SERIAL PRIMARY KEY,
                          nombre VARCHAR(150) NOT NULL,
                          esta_activo SMALLINT DEFAULT 1
);

-- 4. Tabla intermedia para resolver la relación N:N entre Categoria y Producto
CREATE TABLE producto_categoria (
                                    producto_id INT REFERENCES producto(id) ON DELETE CASCADE,
                                    categoria_id INT REFERENCES categoria(id) ON DELETE CASCADE,
                                    PRIMARY KEY (producto_id, categoria_id)
);

-- 5. Creación de la tabla Regla_vencimiento
CREATE TABLE regla_vencimiento (
                                   id SERIAL PRIMARY KEY,
                                   producto_id INT NOT NULL REFERENCES producto(id) ON DELETE CASCADE,
                                   ubicacion VARCHAR(150),
                                   duracion_minutos INT NOT NULL,
                                   tiene_descongelamiento SMALLINT DEFAULT 0,
                                   descongelamiento_minutos INT
);

-- 6. Creación de la tabla TimerActivo
CREATE TABLE timer_activo (
                              id SERIAL PRIMARY KEY,
                              id_grupo CHAR(36), -- Ideal si vas a guardar un UUID
                              producto_id INT NOT NULL REFERENCES producto(id) ON DELETE CASCADE,
                              categoria_id INT NOT NULL REFERENCES categoria(id) ON DELETE CASCADE,
                              regla_vencimiento_id INT NOT NULL REFERENCES regla_vencimiento(id) ON DELETE CASCADE,
                              usuario_id INT REFERENCES usuario(id) ON DELETE SET NULL, -- Vinculación con el Usuario
                              fecha_inicio TIMESTAMP, -- Recomendado sobre DATE para precisión en timers
                              fecha_fin TIMESTAMP,
                              esta_activo SMALLINT DEFAULT 1,
                              estado VARCHAR(50)
);