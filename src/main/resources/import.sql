SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO Categoria (id, icono, estaActiva, nombre) VALUES (1, '/resources/core/img/cocina.png', 1, 'Cocina');
INSERT INTO Categoria (id, icono, estaActiva, nombre) VALUES (2, '/resources/core/img/servicio.png', 1, 'Servicio');
INSERT INTO Categoria (id, icono, estaActiva, nombre) VALUES (3, '/resources/core/img/mccafe.png', 1, 'McCafe');
INSERT INTO Categoria (id, icono, estaActiva, nombre) VALUES (4, '/resources/core/img/isla.png', 1, 'Isla');

INSERT INTO Lote (id, idProducto, fechaDeIngreso, fechaDeVencimiento, proveedor, marca, numeroDeLote, cantidadInicial, cantidadDisponible, estado) VALUES (1, 5, '2026-06-01 09:00:00', '2026-07-20 00:00:00', 'Lácteos del Sur', 'La Serenísima', 1001, 20, 20, 'EN_USO');
INSERT INTO Lote (id, idProducto, fechaDeIngreso, fechaDeVencimiento, proveedor, marca, numeroDeLote, cantidadInicial, cantidadDisponible, estado) VALUES (2, 5, '2026-06-15 09:00:00', '2026-08-15 00:00:00', 'Lácteos del Sur', 'La Serenísima', 1002, 25, 25, 'DISPONIBLE');
INSERT INTO Lote (id, idProducto, fechaDeIngreso, fechaDeVencimiento, proveedor, marca, numeroDeLote, cantidadInicial, cantidadDisponible, estado) VALUES (3, 6, '2026-05-20 09:00:00', '2026-07-10 00:00:00', 'Verduleria Don Pepe', 'Sin marca', 2001, 30, 12, 'EN_USO');
INSERT INTO Lote (id, idProducto, fechaDeIngreso, fechaDeVencimiento, proveedor, marca, numeroDeLote, cantidadInicial, cantidadDisponible, estado) VALUES (4, 7, '2026-05-25 09:00:00', '2026-06-30 00:00:00', 'Verduleria Don Pepe', 'Sin marca', 3001, 15, 4, 'VENCIDO');
INSERT INTO Lote (id, idProducto, fechaDeIngreso, fechaDeVencimiento, proveedor, marca, numeroDeLote, cantidadInicial, cantidadDisponible, estado) VALUES (5, 1, '2026-06-01 09:00:00', '2026-12-01 00:00:00', 'Frigorífico Central', 'Paty', 4001, 50, 50, 'DISPONIBLE');
INSERT INTO Lote (id, idProducto, fechaDeIngreso, fechaDeVencimiento, proveedor, marca, numeroDeLote, cantidadInicial, cantidadDisponible, estado) VALUES (6, 1, '2026-05-01 09:00:00', '2026-08-01 00:00:00', 'Frigorífico Central', 'Paty', 4000, 40, 0, 'CONSUMIDO');
INSERT INTO Lote (id, idProducto, fechaDeIngreso, fechaDeVencimiento, proveedor, marca, numeroDeLote, cantidadInicial, cantidadDisponible, estado) VALUES (7, 2, '2026-06-10 09:00:00', '2027-01-10 00:00:00', 'McCain Argentina', 'McCain', 5001, 60, 60, 'EN_USO');
INSERT INTO Lote (id, idProducto, fechaDeIngreso, fechaDeVencimiento, proveedor, marca, numeroDeLote, cantidadInicial, cantidadDisponible, estado) VALUES (8, 3, '2026-06-05 09:00:00', '2026-09-05 00:00:00', 'Distribuidora Fría', 'Freddo', 6001, 10, 3, 'DESCARTADO');
INSERT INTO Lote (id, idProducto, fechaDeIngreso, fechaDeVencimiento, proveedor, marca, numeroDeLote, cantidadInicial, cantidadDisponible, estado) VALUES (9, 4, '2026-04-01 09:00:00', '2027-04-01 00:00:00', 'Cafetería Mayorista', 'La Virginia', 7001, 100, 100, 'DISPONIBLE');
INSERT INTO Lote (id, idProducto, fechaDeIngreso, fechaDeVencimiento, proveedor, marca, numeroDeLote, cantidadInicial, cantidadDisponible, estado) VALUES (10, 6, '2026-06-25 09:00:00', '2026-11-25 00:00:00', 'Verduleria Don Pepe', 'Sin marca', 2002, 35, 35, 'DISPONIBLE');


INSERT INTO Producto (id, unidadDeMedida, estaActivo, tipoProducto, nombre) VALUES (5, 'KILOS', 1, 'SECO', 'Queso');
INSERT INTO Producto (id, unidadDeMedida, estaActivo, tipoProducto, nombre) VALUES (6, 'KILOS', 1, 'SECO', 'Cebolla');
INSERT INTO Producto (id, unidadDeMedida, estaActivo, tipoProducto, nombre) VALUES (7, 'KILOS', 1, 'SECO', 'Tomate');
INSERT INTO Producto (id, unidadDeMedida, estaActivo, tipoProducto, nombre) VALUES (1, 'KILOS', 1, 'SECO', 'Hamburguesa');
INSERT INTO Producto (id, unidadDeMedida, estaActivo, tipoProducto, nombre) VALUES (2, 'KILOS', 1, 'SECO', 'Papas Fritas');
INSERT INTO Producto (id, unidadDeMedida, estaActivo, tipoProducto, nombre) VALUES (3, 'KILOS', 1, 'SECO', 'Helado');
INSERT INTO Producto (id, unidadDeMedida, estaActivo, tipoProducto, nombre) VALUES (4, 'KILOS', 1, 'SECO', 'Cafe');


INSERT INTO ProductosCategoria (idProducto, idCategoria) VALUES (5, 1);
INSERT INTO ProductosCategoria (idProducto, idCategoria) VALUES (6, 1);
INSERT INTO ProductosCategoria (idProducto, idCategoria) VALUES (7, 1);



INSERT INTO Usuario(id, nombre, email, password, rol, estado) VALUES(3, 'Tatiel','tahielrecchia05@gmail.com', '$2a$10$pKxNbg5qcYqoRCxKvU0t8e8dMAWZ8QBl5clkQD9KHcIsgBFcnz9Ei', 'ADMIN', 'ACTIVO');
INSERT INTO Usuario(id, nombre, email, password, rol, estado) VALUES(6, 'Tatiel','santi2@mail.com', '$2a$10$aHZnx5KAVlbHi52kmBYWDO0eD23FBux7ucIUeTR9rUOFaBlDKLz7C', 'ADMIN', 'ACTIVO');
INSERT INTO Usuario(id, nombre, email, password, rol, estado) VALUES(7, 'Carlos','cr10@mail.com', '$2a$10$RRF0.atOTSi88b01MgHpxeMi9gZ7XpayDlHQKwUfa.oVpuPIB4FE2', 'ADMIN', 'ACTIVO');


INSERT INTO usuarioCategorias(idUsuario, idCategoria) VALUES (3, 1);
INSERT INTO usuarioCategorias(idUsuario, idCategoria) VALUES (3, 2);
INSERT INTO usuarioCategorias(idUsuario, idCategoria) VALUES (3, 3);
INSERT INTO usuarioCategorias(idUsuario, idCategoria) VALUES (3, 4);



INSERT INTO ReglaVencimiento (descongelamientoMinutos, duracionMinutos, tieneDescongelamiento, ubicacion, idProducto) VALUES (0, 240, 0, 'Mesa de Producción', 5);
INSERT INTO ReglaVencimiento (descongelamientoMinutos, duracionMinutos, tieneDescongelamiento, ubicacion, idProducto) VALUES (0, 240, 0, 'Mesa de Producción', 6);
INSERT INTO ReglaVencimiento (descongelamientoMinutos, duracionMinutos, tieneDescongelamiento, ubicacion, idProducto) VALUES (0, 240, 0, 'Mesa de Producción', 7);
INSERT INTO ReglaVencimiento (descongelamientoMinutos, duracionMinutos, tieneDescongelamiento, ubicacion, idProducto) VALUES (120, 4000, 1, 'Mesa de Producción', 1 );
INSERT INTO ReglaVencimiento (descongelamientoMinutos, duracionMinutos, tieneDescongelamiento, ubicacion, idProducto) VALUES (0, 120, 0, 'Línea de Servicio', 1);
INSERT INTO ReglaVencimiento (descongelamientoMinutos, duracionMinutos, tieneDescongelamiento, ubicacion, idProducto) VALUES (120, 4000, 1, 'Mesa de Producción', 2);
INSERT INTO ReglaVencimiento (descongelamientoMinutos, duracionMinutos, tieneDescongelamiento, ubicacion, idProducto) VALUES (0, 120, 0, 'Línea de Servicio', 2);
-- Regla de vencimiento para Café (Producto ID 4)
INSERT INTO ReglaVencimiento (descongelamientoMinutos, duracionMinutos, tieneDescongelamiento, ubicacion, idProducto) VALUES (0, 30, 0, 'Mostrador McCafe', 4);

-- Regla de vencimiento para Helado (Producto ID 3)
INSERT INTO ReglaVencimiento (descongelamientoMinutos, duracionMinutos, tieneDescongelamiento, ubicacion, idProducto) VALUES (0, 60, 0, 'Freezer Isla', 3);

INSERT INTO ProductosCategoria (idProducto, idCategoria) VALUES (1, 1); -- Hamburguesa -> Cocina
INSERT INTO ProductosCategoria (idProducto, idCategoria) VALUES (2, 1); -- Papas Fritas -> Cocina
INSERT INTO ProductosCategoria (idProducto, idCategoria) VALUES (2, 2); -- Papas Fritas -> Servicio
INSERT INTO ProductosCategoria (idProducto, idCategoria) VALUES (3, 4); -- Helado -> Isla
INSERT INTO ProductosCategoria (idProducto, idCategoria) VALUES (4, 3); -- Cafe -> McCafe


INSERT INTO Timer (estado, fechaCreacion, fechaVencimiento, groupId, cantidadProducto ,idCategoria, idProducto, idUsuario , idReglaVencimiento) VALUES ('VENCIDO', NOW(6), '2026-05-24 20:00:59', 'GRP-100', 2,3, 1, 3, 1);
INSERT INTO Timer (estado, fechaCreacion, fechaVencimiento, groupId, cantidadProducto ,idCategoria, idProducto, idUsuario , idReglaVencimiento) VALUES ('RENOVADO', NOW(6), '2026-05-23 20:00:59', 'GRP-100', 10,3, 1, 3, 1);
INSERT INTO Timer (estado, fechaCreacion, fechaVencimiento, groupId, cantidadProducto ,idCategoria, idProducto, idUsuario , idReglaVencimiento) VALUES ('IMPORTADO', NOW(6), '2026-05-23 19:00:59', 'GRP-100', 5,3, 1, 3, 1);
INSERT INTO Timer (estado, fechaCreacion, fechaVencimiento, groupId, cantidadProducto, idCategoria, idProducto, idUsuario, idReglaVencimiento) VALUES ('ACTIVO', NOW(6), DATE_ADD(NOW(6), INTERVAL 4 HOUR), 'GRP-BURG-1', 10, 1, 1, 3, 2);
INSERT INTO Timer (estado, fechaCreacion, fechaVencimiento, groupId, cantidadProducto, idCategoria, idProducto, idUsuario, idReglaVencimiento) VALUES ('ACTIVO', NOW(6), DATE_ADD(NOW(6), INTERVAL 4 HOUR), 'GRP-QUESO-1', 20, 1, 5, 3, 5);
INSERT INTO Timer (estado, fechaCreacion, fechaVencimiento, groupId, cantidadProducto, idCategoria, idProducto, idUsuario, idReglaVencimiento) VALUES ('ACTIVO', NOW(6), DATE_ADD(NOW(6), INTERVAL 4 HOUR), 'GRP-CEB-1', 20, 1, 6, 3, 6);
INSERT INTO Timer (estado, fechaCreacion, fechaVencimiento, groupId, cantidadProducto, idCategoria, idProducto, idUsuario, idReglaVencimiento) VALUES ('ACTIVO', NOW(6), DATE_ADD(NOW(6), INTERVAL 4 HOUR), 'GRP-TOM-1', 20, 1, 7, 3, 7);

INSERT INTO ProductoFinal (id, nombre, precio) VALUES (1, 'Hamburguesa Completa', 3500.00);

INSERT INTO ProductoFinalCategoria (idProductoFinal, idCategoria) VALUES (1, 1);

INSERT INTO ProductoFinalIngrediente (id, productoFinalId, productoId, cantidad) VALUES (1, 1, 1, 1); -- 1 hamburguesa (carne)
INSERT INTO ProductoFinalIngrediente (id, productoFinalId, productoId, cantidad) VALUES (2, 1, 5, 1); -- 1 queso
INSERT INTO ProductoFinalIngrediente (id, productoFinalId, productoId, cantidad) VALUES (3, 1, 6, 1); -- 1 cebolla
INSERT INTO ProductoFinalIngrediente (id, productoFinalId, productoId, cantidad) VALUES (4, 1, 7, 1); -- 1 tomate


-- ---------------------------------------------------------
-- RECETAS / COMPOSICIONES DE PRODUCTOS
-- ---------------------------------------------------------

SET FOREIGN_KEY_CHECKS = 1;

