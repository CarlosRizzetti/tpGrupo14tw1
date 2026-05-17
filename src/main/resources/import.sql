INSERT INTO Categoria (icono, estaActiva, nombre) VALUES ('/resources/core/img/cocina.png', 1, 'Cocina');
INSERT INTO Categoria (icono, estaActiva, nombre) VALUES ('/resources/core/img/servicio.png', 1, 'Servicio');
INSERT INTO Categoria (icono, estaActiva, nombre) VALUES ('/resources/core/img/mccafe.png', 1, 'McCafe');
INSERT INTO Categoria (icono, estaActiva, nombre) VALUES ('/resources/core/img/isla.png', 1, 'Isla');

INSERT INTO Producto (estaActivo, nombre) VALUES (1, 'Hamburguesa');
INSERT INTO Producto (estaActivo, nombre) VALUES (1, 'Papas Fritas');
INSERT INTO Producto (estaActivo, nombre) VALUES (1, 'Helado');
INSERT INTO Producto (estaActivo, nombre) VALUES (1, 'Cafe');

INSERT INTO productos_categoria (id_producto, id_categoria) VALUES (1, 1); -- Hamburguesa -> Cocina
INSERT INTO productos_categoria (id_producto, id_categoria) VALUES (2, 1); -- Papas Fritas -> Cocina
INSERT INTO productos_categoria (id_producto, id_categoria) VALUES (2, 2); -- Papas Fritas -> Servicio
INSERT INTO productos_categoria (id_producto, id_categoria) VALUES (3, 4); -- Helado -> Isla
INSERT INTO productos_categoria (id_producto, id_categoria) VALUES (4, 3); -- Cafe -> McCafe

INSERT INTO regla_vencimiento (descongelamientoMinutos, duracionMinutos, tieneDescongelamiento, ubicacion, producto_id) VALUES (120, 4000, 1, 'mesa', 1);

INSERT INTO Timer(estaActivo, estado, fechaCreacion, fechaVencimiento, groupId, id_categoria, id_producto, id_regla_vencimiento) VALUES (1, 'activo', NOW(6), '2026-05-31 23:59:59', 'GRP-100', 3, 1, 1);

