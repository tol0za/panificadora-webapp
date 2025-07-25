-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 25-07-2025 a las 05:12:18
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

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `catalogo_pan`
--

CREATE TABLE `catalogo_pan` (
  `id_pan` int(11) NOT NULL,
  `nombre_pan` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

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
  `merma` int(11) DEFAULT 0,
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

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `notas_venta`
--

CREATE TABLE `notas_venta` (
  `id_nota` int(11) NOT NULL,
  `folio` int(10) UNSIGNED NOT NULL,
  `id_repartidor` int(11) NOT NULL,
  `id_tienda` int(11) NOT NULL,
  `fecha_nota` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

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

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `tiendas`
--

CREATE TABLE `tiendas` (
  `id_tienda` int(11) NOT NULL,
  `nombre_tienda` varchar(100) NOT NULL,
  `direccion` varchar(255) NOT NULL,
  `telefono` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

CREATE TABLE `usuarios` (
  `id_usuario` int(11) NOT NULL,
  `nombre_usuario` varchar(100) NOT NULL,
  `apellido_usuario` varchar(100) NOT NULL,
  `usuario` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

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
  ADD KEY `id_nota` (`id_nota`),
  ADD KEY `id_empaque` (`id_empaque`),
  ADD KEY `id_distribucion` (`id_distribucion`);

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
  ADD KEY `id_repartidor` (`id_repartidor`);

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
  ADD KEY `id_repartidor` (`id_repartidor`),
  ADD KEY `id_tienda` (`id_tienda`);

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
-- Indices de la tabla `tiendas`
--
ALTER TABLE `tiendas`
  ADD PRIMARY KEY (`id_tienda`),
  ADD UNIQUE KEY `nombre_tienda` (`nombre_tienda`);

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
  MODIFY `id_empaque` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `catalogo_pan`
--
ALTER TABLE `catalogo_pan`
  MODIFY `id_pan` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `detalle_nota_venta`
--
ALTER TABLE `detalle_nota_venta`
  MODIFY `id_detalle` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `distribucion`
--
ALTER TABLE `distribucion`
  MODIFY `id_distribucion` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `empaquetado`
--
ALTER TABLE `empaquetado`
  MODIFY `id_empaquetado` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `inventario_empaquetado`
--
ALTER TABLE `inventario_empaquetado`
  MODIFY `id_inventario` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `notas_venta`
--
ALTER TABLE `notas_venta`
  MODIFY `id_nota` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `produccion`
--
ALTER TABLE `produccion`
  MODIFY `id_produccion` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `repartidores`
--
ALTER TABLE `repartidores`
  MODIFY `id_repartidor` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `tiendas`
--
ALTER TABLE `tiendas`
  MODIFY `id_tienda` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id_usuario` int(11) NOT NULL AUTO_INCREMENT;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `detalle_nota_venta`
--
ALTER TABLE `detalle_nota_venta`
  ADD CONSTRAINT `detalle_nota_venta_ibfk_1` FOREIGN KEY (`id_nota`) REFERENCES `notas_venta` (`id_nota`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `detalle_nota_venta_ibfk_2` FOREIGN KEY (`id_empaque`) REFERENCES `catalogo_empaque` (`id_empaque`) ON UPDATE CASCADE,
  ADD CONSTRAINT `detalle_nota_venta_ibfk_3` FOREIGN KEY (`id_distribucion`) REFERENCES `distribucion` (`id_distribucion`) ON UPDATE CASCADE;

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
  ADD CONSTRAINT `notas_venta_ibfk_1` FOREIGN KEY (`id_repartidor`) REFERENCES `repartidores` (`id_repartidor`) ON UPDATE CASCADE,
  ADD CONSTRAINT `notas_venta_ibfk_2` FOREIGN KEY (`id_tienda`) REFERENCES `tiendas` (`id_tienda`) ON UPDATE CASCADE;

--
-- Filtros para la tabla `produccion`
--
ALTER TABLE `produccion`
  ADD CONSTRAINT `produccion_ibfk_1` FOREIGN KEY (`id_pan`) REFERENCES `catalogo_pan` (`id_pan`) ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
