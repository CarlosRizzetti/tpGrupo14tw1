SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO Categoria (id, icono, estaActiva, nombre) VALUES (1, '/resources/core/img/cocina.png', 1, 'Cocina');
INSERT INTO Categoria (id, icono, estaActiva, nombre) VALUES (2, '/resources/core/img/servicio.png', 1, 'Servicio');
INSERT INTO Categoria (id, icono, estaActiva, nombre) VALUES (3, '/resources/core/img/mccafe.png', 1, 'McCafe');
INSERT INTO Categoria (id, icono, estaActiva, nombre) VALUES (4, '/resources/core/img/isla.png', 1, 'Isla');

INSERT INTO Producto (id, cantidad, estaActivo, nombre) VALUES (1, 100, 1, 'Hamburguesa');
INSERT INTO Producto (id, cantidad, estaActivo, nombre) VALUES (2, 100, 1, 'Papas Fritas');
INSERT INTO Producto (id, cantidad, estaActivo, nombre) VALUES (3, 100, 1, 'Helado');
INSERT INTO Producto (id, cantidad, estaActivo, nombre) VALUES (4, 100, 1, 'Cafe');

INSERT INTO Usuario(id, nombre, email, password, rol, activo) VALUES(3, 'Tatiel', 'tahielrecchia05@gmail.com', '$2a$10$pKxNbg5qcYqoRCxKvU0t8e8dMAWZ8QBl5clkQD9KHcIsgBFcnz9Ei', 'ADMIN', true);

INSERT INTO ReglaVencimiento (descongelamientoMinutos, duracionMinutos, tieneDescongelamiento, ubicacion, idProducto) VALUES (120, 4000, 1, 'Mesa de Producción', 1 );
INSERT INTO ReglaVencimiento (descongelamientoMinutos, duracionMinutos, tieneDescongelamiento, ubicacion, idProducto) VALUES (0, 120, 0, 'Línea de Servicio', 1);
INSERT INTO ReglaVencimiento (descongelamientoMinutos, duracionMinutos, tieneDescongelamiento, ubicacion, idProducto) VALUES (120, 4000, 1, 'Mesa de Producción', 2);
INSERT INTO ReglaVencimiento (descongelamientoMinutos, duracionMinutos, tieneDescongelamiento, ubicacion, idProducto) VALUES (0, 120, 0, 'Línea de Servicio', 2);
INSERT INTO ProductosCategoria (idProducto, idCategoria) VALUES (1, 1); -- Hamburguesa -> Cocina
INSERT INTO ProductosCategoria (idProducto, idCategoria) VALUES (2, 1); -- Papas Fritas -> Cocina
INSERT INTO ProductosCategoria (idProducto, idCategoria) VALUES (2, 2); -- Papas Fritas -> Servicio
INSERT INTO ProductosCategoria (idProducto, idCategoria) VALUES (3, 4); -- Helado -> Isla
INSERT INTO ProductosCategoria (idProducto, idCategoria) VALUES (4, 3); -- Cafe -> McCafe


INSERT INTO Timer (estado, fechaCreacion, fechaVencimiento, groupId, cantidad ,idCategoria, idProducto, idUsuario , idReglaVencimiento) VALUES ('VENCIDO', NOW(6), '2026-05-24 20:00:59', 'GRP-100', 2,3, 1, 3, 1);
INSERT INTO Timer (estado, fechaCreacion, fechaVencimiento, groupId, cantidad ,idCategoria, idProducto, idUsuario , idReglaVencimiento) VALUES ('RENOVADO', NOW(6), '2026-05-23 20:00:59', 'GRP-100', 10,3, 1, 3, 1);
INSERT INTO Timer (estado, fechaCreacion, fechaVencimiento, groupId, cantidad ,idCategoria, idProducto, idUsuario , idReglaVencimiento) VALUES ('IMPORTADO', NOW(6), '2026-05-23 19:00:59', 'GRP-100', 5,3, 1, 3, 1);

SET FOREIGN_KEY_CHECKS = 1;