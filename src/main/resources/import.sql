INSERT INTO Categoria (icono, estaActiva, nombre, tema) VALUES ('/resources/core/img/cocina.png', 1, 'Cocina', 'Cocina');
INSERT INTO Categoria (icono, estaActiva, nombre, tema) VALUES ('/resources/core/img/servicio.png', 1, 'Servicio', 'Servicio');
INSERT INTO Categoria (icono, estaActiva, nombre, tema) VALUES ('/resources/core/img/mccafe.png', 1, 'McCafe', 'McCafe');
INSERT INTO Categoria (icono, estaActiva, nombre, tema) VALUES ('/resources/core/img/isla.png', 1, 'Isla', 'Isla');

INSERT INTO Producto (estaActivo, nombre) VALUES (1, 'Hamburguesa');
INSERT INTO Producto (estaActivo, nombre) VALUES (1, 'Papas Fritas');
INSERT INTO Producto (estaActivo, nombre) VALUES (1, 'Helado');
INSERT INTO Producto (estaActivo, nombre) VALUES (1, 'Cafe');

INSERT INTO productosCategoria (idProducto, idCategoria) VALUES (1, 1); -- Hamburguesa -> Cocina
INSERT INTO productosCategoria (idProducto, idCategoria) VALUES (2, 1); -- Papas Fritas -> Cocina
INSERT INTO productosCategoria (idProducto, idCategoria) VALUES (2, 2); -- Papas Fritas -> Servicio
INSERT INTO productosCategoria (idProducto, idCategoria) VALUES (3, 4); -- Helado -> Isla
INSERT INTO productosCategoria (idProducto, idCategoria) VALUES (4, 3); -- Cafe -> McCafe

INSERT INTO reglaVencimiento (descongelamientoMinutos, duracionMinutos, tieneDescongelamiento, ubicacion, idProducto) VALUES (120, 4000, 1, 'mesa', 1);

INSERT INTO TimerActivo(estaActivo, estado, fechaCreacion, fechaVencimiento, groupId, idCategoria, idProducto, idReglaVencimiento) VALUES (1, 'activo', NOW(6), '2026-05-31 23:59:59', 'GRP-100', 3, 1, 1);
