
INSERT INTO Categoria (id, icono, estaActiva, nombre) VALUES (1, '/resources/core/img/cocina.png', 1, 'Cocina');
INSERT INTO Categoria (id, icono, estaActiva, nombre) VALUES (2, '/resources/core/img/servicio.png', 1, 'Servicio');
INSERT INTO Categoria (id, icono, estaActiva, nombre) VALUES (3, '/resources/core/img/mccafe.png', 1, 'McCafe');
INSERT INTO Categoria (id, icono, estaActiva, nombre) VALUES (4, '/resources/core/img/isla.png', 1, 'Isla');

INSERT INTO Producto (id, estaActivo, nombre) VALUES (1, 1, 'Hamburguesa');
INSERT INTO Producto (id, estaActivo, nombre) VALUES (2, 1, 'Papas Fritas');
INSERT INTO Producto (id, estaActivo, nombre) VALUES (3, 1, 'Helado');
INSERT INTO Producto (id, estaActivo, nombre) VALUES (4, 1, 'Cafe');

INSERT INTO productos_categoria (id_producto, id_categoria) VALUES (1, 1);
INSERT INTO productos_categoria (id_producto, id_categoria) VALUES (2, 1);
INSERT INTO productos_categoria (id_producto, id_categoria) VALUES (4, 3);

INSERT INTO ReglaVencimiento (id, descongelamientoMinutos, duracionMinutos, tieneDescongelamiento, ubicacion, id_producto) VALUES (1, 120, 4000, 1, 'mesa', 1);


INSERT INTO Timer (estaActivo, estado, fechaCreacion, fechaVencimiento, groupId, id_categoria, id_producto, id_regla_vencimiento) VALUES (1, 'activo', NOW(6), '2026-05-31 23:59:59', 'GRP-100', 3, 1, 1);