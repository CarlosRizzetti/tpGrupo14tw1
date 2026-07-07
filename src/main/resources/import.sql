SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO Categoria (id, icono, estaActiva, nombre) VALUES (1, '/resources/core/img/cocina.png', 1, 'Cocina');
INSERT INTO Categoria (id, icono, estaActiva, nombre) VALUES (2, '/resources/core/img/servicio.png', 1, 'Servicio');
INSERT INTO Categoria (id, icono, estaActiva, nombre) VALUES (3, '/resources/core/img/mccafe.png', 1, 'McCafe');
INSERT INTO Categoria (id, icono, estaActiva, nombre) VALUES (4, '/resources/core/img/isla.png', 1, 'Isla');

INSERT INTO Articulos (id, codigo, nombre, marca, proveedor, numeroDeLote, fechaDeIngreso, fechaDeVencimiento, cantidad, unidadDeMedida, tipoArticulo) VALUES (1, 001, 'Hamburguesa', 'McCain', 'Distribuidora Central', 1001, NOW(6), '2027-12-31 23:59:59', 10, 'UNIDAD', 'CONGELADO');
INSERT INTO Articulos (id, codigo, nombre, marca, proveedor, numeroDeLote, fechaDeIngreso, fechaDeVencimiento, cantidad, unidadDeMedida, tipoArticulo) VALUES (2, 002, 'Vasos Grandes', 'Papelera SA', 'Distribuidora Sur', 1002, NOW(6), '2027-12-31 23:59:59', 500, 'UNIDAD', 'SECO');
INSERT INTO Articulos (id, codigo, nombre, marca, proveedor, numeroDeLote, fechaDeIngreso, fechaDeVencimiento, cantidad, unidadDeMedida, tipoArticulo) VALUES (3, 003, 'Hamburguesa', 'Paty', 'Frigorífico del Sur', 2013, '2026-06-10 09:15:00', '2027-05-20 23:59:59', 5, 'UNIDAD', 'CONGELADO');
INSERT INTO Articulos (id, codigo, nombre, marca, proveedor, numeroDeLote, fechaDeIngreso, fechaDeVencimiento, cantidad, unidadDeMedida, tipoArticulo) VALUES (4, 004, 'Queso Cheddar', 'La Serenísima', 'Lácteos del Centro', 1014, '2026-06-12 10:30:00', '2026-09-15 23:59:59', 80, 'KILOS', 'LACTEOS');
INSERT INTO Articulos (id, codigo, nombre, marca, proveedor, numeroDeLote, fechaDeIngreso, fechaDeVencimiento, cantidad, unidadDeMedida, tipoArticulo) VALUES (5, 005, 'Mayonesa', 'Hellmann''s', 'Distribuidora Sur', 1015, '2026-06-15 14:20:00', '2027-12-10 23:59:59', 120, 'LITROS', 'SECO');
INSERT INTO Articulos (id, codigo, nombre, marca, proveedor, numeroDeLote, fechaDeIngreso, fechaDeVencimiento, cantidad, unidadDeMedida, tipoArticulo) VALUES (6, 006, 'Café', 'La Virginia', 'Mayorista Uno', 1016, '2026-06-18 08:45:00', '2028-06-30 23:59:59', 60, 'KILOS', 'SECO');
INSERT INTO Articulos (id, codigo, nombre, marca, proveedor, numeroDeLote, fechaDeIngreso, fechaDeVencimiento, cantidad, unidadDeMedida, tipoArticulo) VALUES (7, 007, 'Pan de Hamburguesa', 'Bimbo', 'Panificados SRL', 1017, '2026-06-20 07:50:00', '2026-07-25 23:59:59', 500, 'UNIDAD', 'LACTEOS');
INSERT INTO Articulos (id, codigo, nombre, marca, proveedor, numeroDeLote, fechaDeIngreso, fechaDeVencimiento, cantidad, unidadDeMedida, tipoArticulo) VALUES (9, 009, 'Papas Congeladas', 'McCain', 'Congelados Express', 1019, '2026-06-25 11:25:00', '2027-08-30 23:59:59', 350, 'KILOS', 'CONGELADO');
INSERT INTO Articulos (id, codigo, nombre, marca, proveedor, numeroDeLote, fechaDeIngreso, fechaDeVencimiento, cantidad, unidadDeMedida, tipoArticulo) VALUES (10, 010, 'Jugo de Naranja', 'Cepita', 'Bebidas Argentinas', 1020, '2026-06-27 16:40:00', '2027-02-28 23:59:59', 180, 'LITROS', 'SECO');
INSERT INTO Articulos (id, codigo, nombre, marca, proveedor, numeroDeLote, fechaDeIngreso, fechaDeVencimiento, cantidad, unidadDeMedida, tipoArticulo) VALUES (11, 011, 'Azúcar', 'Ledesma', 'Distribuidora Norte', 1021, '2026-06-29 09:00:00', '2029-01-15 23:59:59', 500, 'KILOS', 'SECO');
INSERT INTO Articulos (id, codigo, nombre, marca, proveedor, numeroDeLote, fechaDeIngreso, fechaDeVencimiento, cantidad, unidadDeMedida, tipoArticulo) VALUES (12, 012, 'Lechuga', 'Huerta Verde', 'Verdulería Central', 1022, '2026-06-30 06:30:00', '2026-07-08 23:59:59', 120, 'UNIDAD', 'SECO');
INSERT INTO Articulos (id, codigo, nombre, marca, proveedor, numeroDeLote, fechaDeIngreso, fechaDeVencimiento, cantidad, unidadDeMedida, tipoArticulo) VALUES (13, 013, 'Tomate', 'Huerta Verde', 'Verdulería Central', 1023, '2026-06-30 06:45:00', '2026-07-09 23:59:59', 150, 'KILOS', 'SECO');
INSERT INTO Articulos (id, codigo, nombre, marca, proveedor, numeroDeLote, fechaDeIngreso, fechaDeVencimiento, cantidad, unidadDeMedida, tipoArticulo) VALUES (14, 003, 'Hamburguesa', 'Paty', 'Frigorífico del Sur', 3013, '2026-06-20 09:15:00', '2027-05-20 23:59:59', 10, 'UNIDAD', 'CONGELADO');


INSERT INTO Producto (id, cantidad, estaActivo, nombre) VALUES (1, 100, 1, 'Hamburguesa');
INSERT INTO Producto (id, cantidad, estaActivo, nombre) VALUES (2, 100, 1, 'Papas Fritas');
INSERT INTO Producto (id, cantidad, estaActivo, nombre) VALUES (3, 100, 1, 'Helado');
INSERT INTO Producto (id, cantidad, estaActivo, nombre) VALUES (4, 100, 1, 'Cafe');

INSERT INTO Usuario(id, nombre, email, password, rol, estado) VALUES(3, 'Tatiel','tahielrecchia05@gmail.com', '$2a$10$pKxNbg5qcYqoRCxKvU0t8e8dMAWZ8QBl5clkQD9KHcIsgBFcnz9Ei', 'ADMIN', 'ACTIVO');
INSERT INTO Usuario(id, nombre, email, password, rol, estado) VALUES(6, 'Tatiel','santi2@mail.com', '$2a$10$aHZnx5KAVlbHi52kmBYWDO0eD23FBux7ucIUeTR9rUOFaBlDKLz7C', 'ADMIN', 'ACTIVO');
INSERT INTO Usuario(id, nombre, email, password, rol, estado) VALUES(7, 'Carlos','cr10@mail.com', '$2a$10$RRF0.atOTSi88b01MgHpxeMi9gZ7XpayDlHQKwUfa.oVpuPIB4FE2', 'ADMIN', 'ACTIVO');


INSERT INTO usuarioCategorias(idUsuario, idCategoria) VALUES (3, 1);
INSERT INTO usuarioCategorias(idUsuario, idCategoria) VALUES (3, 2);
INSERT INTO usuarioCategorias(idUsuario, idCategoria) VALUES (3, 3);
INSERT INTO usuarioCategorias(idUsuario, idCategoria) VALUES (3, 4);
INSERT INTO Usuario(id, email, password, rol, estado) VALUES(7, 'cr10@mail.com', '$2a$10$vpXbkVGioeJq7AxQp/nWFeOwN1pQXL5dbJijT83BVv2LuJTcWy1pG', 'ADMIN', 'ACTIVO');
INSERT INTO Usuario(id, nombre, email, password, rol, estado) VALUES(3, 'Tatiel','tahielrecchia05@gmail.com', '$2a$10$pKxNbg5qcYqoRCxKvU0t8e8dMAWZ8QBl5clkQD9KHcIsgBFcnz9Ei', 'ADMIN', 'ACTIVO');
INSERT INTO Usuario(id, nombre, email, password, rol, estado) VALUES(6, 'Tatiel','santi2@mail.com', '$2a$10$aHZnx5KAVlbHi52kmBYWDO0eD23FBux7ucIUeTR9rUOFaBlDKLz7C', 'ADMIN', 'ACTIVO');

INSERT INTO usuarioCategorias(idUsuario, idCategoria) VALUES (3, 1);
INSERT INTO usuarioCategorias(idUsuario, idCategoria) VALUES (3, 2);
INSERT INTO usuarioCategorias(idUsuario, idCategoria) VALUES (3, 3);
INSERT INTO usuarioCategorias(idUsuario, idCategoria) VALUES (3, 4);

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


INSERT INTO Timer (estado, fechaCreacion, fechaVencimiento, groupId, cantidad ,idCategoria, idProducto, idUsuario , idReglaVencimiento) VALUES ('VENCIDO', NOW(6), '2026-05-24 20:00:59', 'GRP-100', 2,3, 1, 3, 1);
INSERT INTO Timer (estado, fechaCreacion, fechaVencimiento, groupId, cantidad ,idCategoria, idProducto, idUsuario , idReglaVencimiento) VALUES ('RENOVADO', NOW(6), '2026-05-23 20:00:59', 'GRP-100', 10,3, 1, 3, 1);
INSERT INTO Timer (estado, fechaCreacion, fechaVencimiento, groupId, cantidad ,idCategoria, idProducto, idUsuario , idReglaVencimiento) VALUES ('IMPORTADO', NOW(6), '2026-05-23 19:00:59', 'GRP-100', 5,3, 1, 3, 1);

-- ---------------------------------------------------------
-- RECETAS / COMPOSICIONES DE PRODUCTOS
-- ---------------------------------------------------------

-- 1. Receta de Hamburguesa (Producto ID 1)
INSERT INTO Receta (id, idProducto) VALUES (1, 1);
INSERT INTO RecetaDetalle (id, cantidad, idArticulo, idReceta) VALUES (1, 1.0, 7, 1);  -- Pan de Hamburguesa (1 unidad)
INSERT INTO RecetaDetalle (id, cantidad, idArticulo, idReceta) VALUES (2, 1.0, 3, 1);  -- Carne (1 unidad)
INSERT INTO RecetaDetalle (id, cantidad, idArticulo, idReceta) VALUES (3, 1.0, 12, 1); -- Lechuga (1 unidad)
INSERT INTO RecetaDetalle (id, cantidad, idArticulo, idReceta) VALUES (4, 0.1, 13, 1); -- Tomate (0.1 kg)
INSERT INTO RecetaDetalle (id, cantidad, idArticulo, idReceta) VALUES (5, 0.05, 4, 1); -- Queso Cheddar (0.05 kg)
INSERT INTO RecetaDetalle (id, cantidad, idArticulo, idReceta) VALUES (6, 0.02, 5, 1); -- Mayonesa (0.02 litros)

-- 2. Receta de Papas Fritas (Producto ID 2)
INSERT INTO Receta (id, idProducto) VALUES (2, 2);
INSERT INTO RecetaDetalle (id, cantidad, idArticulo, idReceta) VALUES (7, 0.25, 9, 2); -- Papas Congeladas (0.25 kg = 250g)

-- 3. Receta de Café (Producto ID 4)
INSERT INTO Receta (id, idProducto) VALUES (3, 4);
INSERT INTO RecetaDetalle (id, cantidad, idArticulo, idReceta) VALUES (8, 0.02, 6, 3); -- Café (0.02 kg = 20g)

-- 4. Artículo Helado (El artículo no existía en stock, lo creamos)
INSERT INTO Articulos (id, codigo, nombre, marca, proveedor, numeroDeLote, fechaDeIngreso, fechaDeVencimiento, cantidad, unidadDeMedida, tipoArticulo) VALUES (14, 014, 'Helado Vainilla', 'La Serenísima', 'Lácteos del Centro', 1024, NOW(6), '2027-07-09 23:59:59', 50, 'KILOS', 'LACTEOS');

-- 5. Receta de Helado (Producto ID 3)
INSERT INTO Receta (id, idProducto) VALUES (4, 3);
INSERT INTO RecetaDetalle (id, cantidad, idArticulo, idReceta) VALUES (9, 0.15, 14, 4); -- Helado Vainilla (0.15 kg = 150g)

SET FOREIGN_KEY_CHECKS = 1;

