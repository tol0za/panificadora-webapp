-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 31-07-2025 a las 18:24:35
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `panificadora_webapp`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `catalogo_empaque`
--

CREATE TABLE `catalogo_empaque` (
  `id_empaque` int(11) NOT NULL,
  `nombre_empaque` varchar(100) NOT NULL,
  `precio_unitario` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

--
-- Volcado de datos para la tabla `catalogo_empaque`
--

INSERT INTO `catalogo_empaque` (`id_empaque`, `nombre_empaque`, `precio_unitario`) VALUES
(1, 'Pan Dulce Familiar', 30.00),
(2, 'Pan Dulce Individual', 6.00),
(3, 'Pan Jumbo', 20.00),
(4, 'Mantecadas', 9.00),
(5, 'Besos de Fresa', 8.00),
(6, 'Conchas Dúo', 12.00),
(7, 'Elote', 8.00),
(8, 'Girasol', 8.00),
(9, 'Cochitos (Caja 25 Pzs)', 75.00),
(10, 'Sevillana (Caja 35 Pzs)', 85.00),
(11, 'Cocadas (Caja 50 Pzs)', 125.00),
(12, 'Payasitos (Caja 30 Pzs)', 90.00),
(13, 'Galletas (Caja 35 Pzs)', 85.00),
(14, 'Bolsa Bollo Sal (6 Pzs)', 15.00),
(15, 'Bolillo / Telera Duo', 25.00);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `catalogo_pan`
--

CREATE TABLE `catalogo_pan` (
  `id_pan` int(11) NOT NULL,
  `nombre_pan` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

--
-- Volcado de datos para la tabla `catalogo_pan`
--

INSERT INTO `catalogo_pan` (`id_pan`, `nombre_pan`) VALUES
(1, 'Besos de Fresa'),
(2, 'Cevillana Mini'),
(3, 'Chino sabor Vainilla'),
(4, 'Cocadas'),
(5, 'Cochito Mini'),
(6, 'Elotes'),
(7, 'Galleta Mediana Chocochips'),
(8, 'Galleta Mediana Grajea'),
(9, 'Girasol'),
(10, 'Mantecada Individual'),
(11, 'Mantecadas'),
(12, 'Mini Galleta Grajea'),
(13, 'Mini Galleta Punto Fresa'),
(14, 'Pan Dulce Levadura'),
(15, 'Pan Jumbo'),
(16, 'Payasito Mini');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `detalle_nota_venta`
--

CREATE TABLE `detalle_nota_venta` (
  `id_detalle` int(11) NOT NULL,
  `id_nota` int(11) NOT NULL,
  `id_distribucion` int(11) DEFAULT NULL,
  `id_empaque` int(11) NOT NULL,
  `cantidad_vendida` int(11) NOT NULL,
  `merma` int(11) NOT NULL DEFAULT 0,
  `precio_unitario` decimal(10,2) NOT NULL,
  `total_linea` decimal(12,2) GENERATED ALWAYS AS (`cantidad_vendida` * `precio_unitario`) STORED
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `distribucion`
--

CREATE TABLE `distribucion` (
  `id_distribucion` int(11) NOT NULL,
  `id_repartidor` int(11) NOT NULL,
  `id_empaque` int(11) NOT NULL,
  `cantidad` int(11) NOT NULL,
  `fecha_distribucion` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

--
-- Volcado de datos para la tabla `distribucion`
--

INSERT INTO `distribucion` (`id_distribucion`, `id_repartidor`, `id_empaque`, `cantidad`, `fecha_distribucion`) VALUES
(51, 33, 1, 18, '2025-07-30 22:00:33'),
(52, 33, 9, 25, '2025-07-30 22:00:33'),
(65, 1, 1, 100, '2025-07-30 22:57:58'),
(72, 33, 1, 120, '2025-07-30 23:04:30'),
(75, 30, 2, 20, '2025-07-30 23:05:37'),
(77, 33, 2, 2, '2025-07-30 23:07:22'),
(81, 33, 1, 1500, '2025-07-31 00:05:59'),
(82, 1, 1, 10, '2025-07-31 07:38:10'),
(83, 1, 3, 10, '2025-07-31 07:38:10'),
(84, 1, 11, 2, '2025-07-31 07:38:10');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `empaquetado`
--

CREATE TABLE `empaquetado` (
  `id_empaquetado` int(11) NOT NULL,
  `id_empaque` int(11) NOT NULL,
  `cantidad` int(11) NOT NULL,
  `fecha_empaquetado` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

--
-- Disparadores `empaquetado`
--
DELIMITER $$
CREATE TRIGGER `trg_after_empaquetado_insert` AFTER INSERT ON `empaquetado` FOR EACH ROW BEGIN
  INSERT INTO inventario_empaquetado (id_empaque, cantidad, fecha, motivo, cantidad_actual)
  VALUES (NEW.id_empaque, NEW.cantidad, NOW(), 'Ingreso automático', NEW.cantidad)
  ON DUPLICATE KEY UPDATE cantidad_actual = cantidad_actual + NEW.cantidad;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `inventario_empaquetado`
--

CREATE TABLE `inventario_empaquetado` (
  `id_inventario` int(11) NOT NULL,
  `id_empaque` int(11) NOT NULL,
  `cantidad` int(11) NOT NULL,
  `fecha` datetime NOT NULL,
  `motivo` varchar(255) DEFAULT NULL,
  `id_repartidor` int(11) DEFAULT NULL,
  `cantidad_actual` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

--
-- Volcado de datos para la tabla `inventario_empaquetado`
--

INSERT INTO `inventario_empaquetado` (`id_inventario`, `id_empaque`, `cantidad`, `fecha`, `motivo`, `id_repartidor`, `cantidad_actual`) VALUES
(84, 1, 1000, '2025-07-29 18:07:05', 'Ingreso de Mercancia', NULL, 700),
(85, 2, 180, '2025-07-29 18:31:55', 'Ingreso de Mercancia', NULL, 180),
(86, 3, 120, '2025-07-29 18:32:03', 'Ingreso de Mercancia', NULL, 120),
(87, 9, 10, '2025-07-29 18:32:10', 'Ingreso de Mercancia', NULL, 10),
(88, 10, 10, '2025-07-29 18:32:17', 'Ingreso de Mercancia', NULL, 10),
(89, 11, 25, '2025-07-29 18:32:26', 'Ingreso de Mercancia', NULL, 25),
(90, 12, 32, '2025-07-29 18:32:32', 'Ingreso de Mercancia', NULL, 32),
(91, 1, 300, '2025-07-30 01:03:43', 'Ingreso de Mercancia', NULL, 1300),
(92, 1, 300, '2025-07-30 15:25:47', 'Ingreso de Mercancia', NULL, 1600);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `inventario_repartidor`
--

CREATE TABLE `inventario_repartidor` (
  `id_repartidor` int(11) NOT NULL,
  `id_empaque` int(11) NOT NULL,
  `cantidad_distribuida` int(11) DEFAULT 0,
  `cantidad_vendida` int(11) DEFAULT 0,
  `cantidad_mermada` int(11) DEFAULT 0,
  `cantidad_restante` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

--
-- Volcado de datos para la tabla `inventario_repartidor`
--

INSERT INTO `inventario_repartidor` (`id_repartidor`, `id_empaque`, `cantidad_distribuida`, `cantidad_vendida`, `cantidad_mermada`, `cantidad_restante`) VALUES
(3, 1, 50, 0, 0, 50),
(30, 1, 2600, 0, 0, 2600);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `notas_venta`
--

CREATE TABLE `notas_venta` (
  `id_nota` int(11) NOT NULL,
  `folio` int(10) UNSIGNED NOT NULL,
  `id_repartidor` int(11) NOT NULL,
  `id_tienda` int(11) NOT NULL,
  `fecha_nota` datetime NOT NULL DEFAULT current_timestamp(),
  `total` decimal(12,2) DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

--
-- Volcado de datos para la tabla `notas_venta`
--

INSERT INTO `notas_venta` (`id_nota`, `folio`, `id_repartidor`, `id_tienda`, `fecha_nota`, `total`) VALUES
(1, 1234, 2, 165, '2025-07-31 00:38:32', 0.00);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `produccion`
--

CREATE TABLE `produccion` (
  `id_produccion` int(11) NOT NULL,
  `id_pan` int(11) NOT NULL,
  `tipo_produccion` varchar(50) NOT NULL,
  `cant_articulo` int(11) NOT NULL,
  `fecha_produccion` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `repartidores`
--

CREATE TABLE `repartidores` (
  `id_repartidor` int(11) NOT NULL,
  `nombre_repartidor` varchar(100) NOT NULL,
  `apellido_repartidor` varchar(100) NOT NULL,
  `telefono` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

--
-- Volcado de datos para la tabla `repartidores`
--

INSERT INTO `repartidores` (`id_repartidor`, `nombre_repartidor`, `apellido_repartidor`, `telefono`) VALUES
(1, 'Felipe Roberto', 'Lomeli Rodriguez', '5580551532'),
(2, 'Eldriche Emanuel', 'Arreola Galvez', '6673329484'),
(3, 'Wuilmer Jeovany', 'Pena Cruz', '6161093492'),
(30, 'LUIS ANGEL', 'TOLOZA HERNANDEZ', '6161093492'),
(33, 'Ivonne Elissa', 'Camacho Cajeme', '6161342828');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `secuencias`
--

CREATE TABLE `secuencias` (
  `nombre` varchar(50) NOT NULL,
  `valor` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `tiendas`
--

CREATE TABLE `tiendas` (
  `id_tienda` int(11) NOT NULL,
  `nombre_tienda` varchar(100) NOT NULL,
  `direccion` varchar(255) NOT NULL,
  `telefono` varchar(20) NOT NULL,
  `zona` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

--
-- Volcado de datos para la tabla `tiendas`
--

INSERT INTO `tiendas` (`id_tienda`, `nombre_tienda`, `direccion`, `telefono`, `zona`) VALUES
(119, 'Mercadito Popular', 'L.San Ramon', '', 'Norte'),
(120, 'Castro', 'L.San Ramon', '', 'Norte'),
(121, 'Sofi', 'L.San Ramon', '', 'Norte'),
(122, 'Cuates', 'L.San Ramon', '', 'Norte'),
(123, 'Martinez', 'L.San Ramon', '', 'Norte'),
(124, 'Diconsa Leo', 'L.San Ramon', '', 'Norte'),
(125, 'Mixtequita', 'L.San Ramon', '', 'Norte'),
(126, 'Omayra', 'L.San Ramon', '', 'Norte'),
(127, 'Loncheria Gaby', 'L.San Ramon', '', 'Norte'),
(128, 'Yarely', 'L.San Ramon', '', 'Norte'),
(129, 'Diconsa Alejandra', 'L.San Ramon', '', 'Norte'),
(130, 'Martinez', '13 de Mayo', '', 'Norte'),
(131, 'Pablos', '13 de Mayo', '', 'Norte'),
(132, 'Santiago', '13 de Mayo', '', 'Norte'),
(133, 'Brayant', '13 de Mayo', '', 'Norte'),
(134, 'Vicky', '13 de Mayo', '', 'Norte'),
(135, 'Comercial Hernandez', '13 de Mayo', '', 'Norte'),
(136, 'Carlitos', '13 de Mayo', '', 'Norte'),
(137, 'Emilio', '13 de Mayo', '', 'Norte'),
(138, 'Loncheria Paty', '13 de Mayo', '', 'Norte'),
(139, 'Yaretzi', '13 de Mayo', '', 'Norte'),
(140, 'Isai', 'Triki', '', 'Norte'),
(141, 'El Girasol', 'Triki', '', 'Norte'),
(142, 'Merino', 'Triki', '', 'Norte'),
(143, 'Ojendis', 'Triki', '', 'Norte'),
(144, 'Diconsa Alejandra', 'Triki', '', 'Norte'),
(145, 'Chavez', 'Triki', '', 'Norte'),
(146, 'Rosita', 'Triki', '', 'Norte'),
(147, 'Caute', 'Triki', '', 'Norte'),
(148, 'Martinez', 'Triki', '', 'Norte'),
(149, 'La Pasadita', 'Triki', '', 'Norte'),
(150, 'El Caminante', 'Triki', '', 'Norte'),
(151, 'Comercial Hernandez I', 'Triki', '', 'Norte'),
(152, 'Comercial Hernandez II', 'Triki', '', 'Norte'),
(153, 'Isai', 'Triki', '', 'Norte'),
(154, 'Lopez', 'Misiones', '', 'Norte'),
(155, 'Rodriguez', 'Misiones', '', 'Norte'),
(156, 'Cruz', 'Misiones', '', 'Norte'),
(157, 'Cortez', 'Misiones', '', 'Norte'),
(158, 'Mi Pueblito', 'Misiones', '', 'Norte'),
(159, 'Super Plus', 'Misiones', '', 'Norte'),
(160, 'P. Rosales', 'Misiones', '', 'Norte'),
(161, 'Ahorro', 'Misiones', '', 'Norte'),
(162, 'Boni', 'Misiones', '', 'Norte'),
(163, 'Flores de Leon I', 'V.G.', '', 'Norte'),
(164, 'Flores de Leon II', 'V.G.', '', 'Norte'),
(165, '3 Hermanos', 'V.G.', '', 'Norte'),
(166, 'Paulina', 'V.G.', '', 'Norte'),
(167, 'Pablos', 'V.G.', '', 'Norte');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

CREATE TABLE `usuarios` (
  `id_usuario` int(11) NOT NULL,
  `nombre_usuario` varchar(100) NOT NULL,
  `apellido_usuario` varchar(100) NOT NULL,
  `usuario` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `rol` enum('administrador','empleado') NOT NULL DEFAULT 'empleado'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`id_usuario`, `nombre_usuario`, `apellido_usuario`, `usuario`, `password`, `rol`) VALUES
(1, 'Luis Angel', 'Toloza Hernandez', 'admin', 'admin', 'administrador'),
(2, 'Ivette Melissa', 'Camacho Cajeme', 'melissa', 'melissa', 'empleado');

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `catalogo_empaque`
--
ALTER TABLE `catalogo_empaque`
  ADD PRIMARY KEY (`id_empaque`),
  ADD UNIQUE KEY `nombre_empaque` (`nombre_empaque`);

--
-- Indices de la tabla `catalogo_pan`
--
ALTER TABLE `catalogo_pan`
  ADD PRIMARY KEY (`id_pan`),
  ADD UNIQUE KEY `nombre_pan` (`nombre_pan`);

--
-- Indices de la tabla `detalle_nota_venta`
--
ALTER TABLE `detalle_nota_venta`
  ADD PRIMARY KEY (`id_detalle`),
  ADD KEY `idx_detalle_nota` (`id_nota`),
  ADD KEY `idx_detalle_empaque` (`id_empaque`),
  ADD KEY `idx_detalle_distribucion` (`id_distribucion`);

--
-- Indices de la tabla `distribucion`
--
ALTER TABLE `distribucion`
  ADD PRIMARY KEY (`id_distribucion`),
  ADD KEY `id_repartidor` (`id_repartidor`),
  ADD KEY `id_empaque` (`id_empaque`);

--
-- Indices de la tabla `empaquetado`
--
ALTER TABLE `empaquetado`
  ADD PRIMARY KEY (`id_empaquetado`),
  ADD KEY `id_empaque` (`id_empaque`);

--
-- Indices de la tabla `inventario_empaquetado`
--
ALTER TABLE `inventario_empaquetado`
  ADD PRIMARY KEY (`id_inventario`),
  ADD KEY `id_empaque` (`id_empaque`),
  ADD KEY `id_repartidor` (`id_repartidor`),
  ADD KEY `id_inventario` (`id_inventario`,`id_empaque`,`cantidad`,`fecha`,`motivo`,`id_repartidor`,`cantidad_actual`);

--
-- Indices de la tabla `inventario_repartidor`
--
ALTER TABLE `inventario_repartidor`
  ADD PRIMARY KEY (`id_repartidor`,`id_empaque`),
  ADD KEY `id_empaque` (`id_empaque`);

--
-- Indices de la tabla `notas_venta`
--
ALTER TABLE `notas_venta`
  ADD PRIMARY KEY (`id_nota`),
  ADD UNIQUE KEY `folio` (`folio`),
  ADD KEY `idx_notas_repartidor` (`id_repartidor`),
  ADD KEY `idx_notas_tienda` (`id_tienda`);

--
-- Indices de la tabla `produccion`
--
ALTER TABLE `produccion`
  ADD PRIMARY KEY (`id_produccion`),
  ADD KEY `id_pan` (`id_pan`);

--
-- Indices de la tabla `repartidores`
--
ALTER TABLE `repartidores`
  ADD PRIMARY KEY (`id_repartidor`);

--
-- Indices de la tabla `secuencias`
--
ALTER TABLE `secuencias`
  ADD PRIMARY KEY (`nombre`);

--
-- Indices de la tabla `tiendas`
--
ALTER TABLE `tiendas`
  ADD PRIMARY KEY (`id_tienda`);

--
-- Indices de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD PRIMARY KEY (`id_usuario`),
  ADD UNIQUE KEY `usuario` (`usuario`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `catalogo_empaque`
--
ALTER TABLE `catalogo_empaque`
  MODIFY `id_empaque` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;

--
-- AUTO_INCREMENT de la tabla `catalogo_pan`
--
ALTER TABLE `catalogo_pan`
  MODIFY `id_pan` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=28;

--
-- AUTO_INCREMENT de la tabla `detalle_nota_venta`
--
ALTER TABLE `detalle_nota_venta`
  MODIFY `id_detalle` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `distribucion`
--
ALTER TABLE `distribucion`
  MODIFY `id_distribucion` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=85;

--
-- AUTO_INCREMENT de la tabla `empaquetado`
--
ALTER TABLE `empaquetado`
  MODIFY `id_empaquetado` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `inventario_empaquetado`
--
ALTER TABLE `inventario_empaquetado`
  MODIFY `id_inventario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=93;

--
-- AUTO_INCREMENT de la tabla `notas_venta`
--
ALTER TABLE `notas_venta`
  MODIFY `id_nota` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `produccion`
--
ALTER TABLE `produccion`
  MODIFY `id_produccion` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `repartidores`
--
ALTER TABLE `repartidores`
  MODIFY `id_repartidor` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=34;

--
-- AUTO_INCREMENT de la tabla `tiendas`
--
ALTER TABLE `tiendas`
  MODIFY `id_tienda` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=168;

--
-- AUTO_INCREMENT de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id_usuario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `detalle_nota_venta`
--
ALTER TABLE `detalle_nota_venta`
  ADD CONSTRAINT `fk_detalle_distrib` FOREIGN KEY (`id_distribucion`) REFERENCES `distribucion` (`id_distribucion`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_detalle_empaque` FOREIGN KEY (`id_empaque`) REFERENCES `catalogo_empaque` (`id_empaque`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_detalle_nota` FOREIGN KEY (`id_nota`) REFERENCES `notas_venta` (`id_nota`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Filtros para la tabla `distribucion`
--
ALTER TABLE `distribucion`
  ADD CONSTRAINT `distribucion_ibfk_1` FOREIGN KEY (`id_repartidor`) REFERENCES `repartidores` (`id_repartidor`) ON UPDATE CASCADE,
  ADD CONSTRAINT `distribucion_ibfk_2` FOREIGN KEY (`id_empaque`) REFERENCES `catalogo_empaque` (`id_empaque`) ON UPDATE CASCADE;

--
-- Filtros para la tabla `empaquetado`
--
ALTER TABLE `empaquetado`
  ADD CONSTRAINT `empaquetado_ibfk_1` FOREIGN KEY (`id_empaque`) REFERENCES `catalogo_empaque` (`id_empaque`) ON UPDATE CASCADE;

--
-- Filtros para la tabla `inventario_empaquetado`
--
ALTER TABLE `inventario_empaquetado`
  ADD CONSTRAINT `inventario_empaquetado_ibfk_1` FOREIGN KEY (`id_empaque`) REFERENCES `catalogo_empaque` (`id_empaque`) ON UPDATE CASCADE,
  ADD CONSTRAINT `inventario_empaquetado_ibfk_2` FOREIGN KEY (`id_repartidor`) REFERENCES `repartidores` (`id_repartidor`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Filtros para la tabla `inventario_repartidor`
--
ALTER TABLE `inventario_repartidor`
  ADD CONSTRAINT `inventario_repartidor_ibfk_1` FOREIGN KEY (`id_repartidor`) REFERENCES `repartidores` (`id_repartidor`) ON DELETE CASCADE,
  ADD CONSTRAINT `inventario_repartidor_ibfk_2` FOREIGN KEY (`id_empaque`) REFERENCES `catalogo_empaque` (`id_empaque`) ON DELETE CASCADE;

--
-- Filtros para la tabla `notas_venta`
--
ALTER TABLE `notas_venta`
  ADD CONSTRAINT `fk_notas_repartidor` FOREIGN KEY (`id_repartidor`) REFERENCES `repartidores` (`id_repartidor`),
  ADD CONSTRAINT `fk_notas_tienda` FOREIGN KEY (`id_tienda`) REFERENCES `tiendas` (`id_tienda`);

--
-- Filtros para la tabla `produccion`
--
ALTER TABLE `produccion`
  ADD CONSTRAINT `produccion_ibfk_1` FOREIGN KEY (`id_pan`) REFERENCES `catalogo_pan` (`id_pan`) ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
