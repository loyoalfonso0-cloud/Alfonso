-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 16-09-2025 a las 14:52:03
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `sistema_tributario`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `comprobantes`
--

CREATE TABLE `comprobantes` (
  `id` bigint(20) NOT NULL,
  `contribuyente_id` bigint(20) NOT NULL,
  `tipo` varchar(40) NOT NULL,
  `serie` varchar(10) NOT NULL,
  `numero` varchar(20) NOT NULL,
  `fecha_emision` date NOT NULL,
  `subtotal` decimal(18,2) NOT NULL,
  `impuesto` decimal(18,2) NOT NULL,
  `total` decimal(18,2) NOT NULL,
  `estado` varchar(20) NOT NULL DEFAULT 'EMITIDO',
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp(),
  `actualizado_en` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `contribuyentes`
--

CREATE TABLE `contribuyentes` (
  `id` bigint(20) NOT NULL,
  `rif` varchar(20) NOT NULL,
  `razon_social` varchar(200) NOT NULL,
  `direccion` varchar(255) NOT NULL,
  `telefono` varchar(40) DEFAULT NULL,
  `email` varchar(150) NOT NULL,
  `tipo_contribuyente` varchar(20) NOT NULL DEFAULT 'PERSONA_NATURAL',
  `representante_legal` varchar(200) DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT 1,
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp(),
  `actualizado_en` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `contribuyentes`
--

INSERT INTO `contribuyentes` (`id`, `rif`, `razon_social`, `direccion`, `telefono`, `email`, `tipo_contribuyente`, `representante_legal`, `activo`, `creado_en`, `actualizado_en`) VALUES
(1, 'V-10123456-1', 'Juan Carlos Pérez López', 'Av. Libertador 123, Caracas', '04229876543', 'juan.perez@email.com', 'PERSONA_NATURAL', '', 1, '2025-09-12 18:05:44', NULL),
(2, 'J-20987654-3', 'Constructora del Norte C.A.', 'Av. Industrial 789, Maracay', '0212-678901', 'gerencia@constructoranorte.ve', 'PERSONA_JURIDICA', 'Carlos Alberto Mendoza Silva', 1, '2025-09-12 18:07:41', NULL),
(3, 'V-12345678-9', 'Sirumatek Soluciones', 'Antímano, Caracas', '0414-1789876', 'loyoa@gmail.com', 'PERSONA_NATURAL', '', 1, '2025-09-12 23:35:22', NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `declaraciones`
--

CREATE TABLE `declaraciones` (
  `id` bigint(20) NOT NULL,
  `contribuyente_id` bigint(20) NOT NULL,
  `impuesto_id` bigint(20) NOT NULL,
  `periodo` varchar(20) NOT NULL,
  `base_imponible` decimal(18,2) NOT NULL DEFAULT 0.00,
  `monto` decimal(18,2) NOT NULL DEFAULT 0.00,
  `estado` varchar(20) NOT NULL DEFAULT 'PENDIENTE',
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `declaraciones`
--

INSERT INTO `declaraciones` (`id`, `contribuyente_id`, `impuesto_id`, `periodo`, `base_imponible`, `monto`, `estado`, `creado_en`) VALUES
(1, 1, 1, '2025-09', 30.00, 70.00, 'PAGADA', '2025-09-12 15:04:03');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `flyway_schema_history`
--

CREATE TABLE `flyway_schema_history` (
  `installed_rank` int(11) NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int(11) DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT current_timestamp(),
  `execution_time` int(11) NOT NULL,
  `success` tinyint(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `flyway_schema_history`
--

INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES
(1, '1', '<< Flyway Baseline >>', 'BASELINE', '<< Flyway Baseline >>', NULL, 'root', '2025-09-12 13:29:00', 0, 1),
(2, '3', 'modulo tributario', 'SQL', 'V3__modulo_tributario.sql', -830210134, 'root', '2025-09-12 13:29:00', 6, 1),
(3, '4', 'seed superadmin', 'SQL', 'V4__seed_superadmin.sql', 880296611, 'root', '2025-09-12 13:29:00', 9, 1),
(4, '5', 'nuevos modulos tributarios', 'SQL', 'V5__nuevos_modulos_tributarios.sql', 289595870, 'root', '2025-09-12 13:29:00', 3, 1),
(5, '6', 'actualizar contribuyentes', 'SQL', 'V6__actualizar_contribuyentes.sql', -867642180, 'root', '2025-09-12 13:31:50', 31, 1),
(6, '7', 'retenciones estructura', 'SQL', 'V7__actualizar_retenciones.sql', 820097233, 'root', '2025-09-12 14:30:43', 20, 1),
(7, '8', 'retenciones datos', 'SQL', 'V8__retenciones_completas.sql', -1039934397, 'root', '2025-09-12 16:04:58', 42, 1),
(8, '7', 'actualizar retenciones', 'DELETE', 'V7__actualizar_retenciones.sql', -1778007852, 'root', '2025-09-12 16:25:41', 0, 1),
(9, '7', 'retenciones estructura', 'SQL', 'V7__retenciones_estructura.sql', 820097233, 'root', '2025-09-12 17:03:58', 50, 1),
(10, '10', 'comprobantes tabla', 'SQL', 'V10__comprobantes_tabla.sql', 1802602366, 'root', '2025-09-12 17:17:35', 26, 1),
(11, '11', 'comprobantes datos', 'SQL', 'V11__comprobantes_datos.sql', -39832600, 'root', '2025-09-12 17:20:13', 18, 1),
(12, '12', 'fix retenciones table', 'SQL', 'V12__fix_retenciones_table.sql', 2052545726, 'root', '2025-09-12 19:10:29', 34, 1),
(13, '13', 'insert permisos basicos', 'SQL', 'V13__insert_permisos_basicos.sql', 1989955694, 'root', '2025-09-12 19:53:05', 15, 1),
(14, '14', 'actualizar personal campos', 'SQL', 'V14__actualizar_personal_campos.sql', 1745245407, 'root', '2025-09-12 20:39:05', 48, 1),
(15, '15', 'verificar y agregar campos personal', 'SQL', 'V15__verificar_y_agregar_campos_personal.sql', 298522321, 'root', '2025-09-15 13:20:43', 194, 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `impuestos`
--

CREATE TABLE `impuestos` (
  `id` bigint(20) NOT NULL,
  `codigo` varchar(30) NOT NULL,
  `nombre` varchar(200) NOT NULL,
  `tasa` decimal(10,4) NOT NULL DEFAULT 0.0000
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `impuestos`
--

INSERT INTO `impuestos` (`id`, `codigo`, `nombre`, `tasa`) VALUES
(1, 'IGV0101', 'Impuesto General a las Ventas de Super Mercado', 19.0000);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `pagos`
--

CREATE TABLE `pagos` (
  `id` bigint(20) NOT NULL,
  `declaracion_id` bigint(20) NOT NULL,
  `fecha_pago` date NOT NULL,
  `monto` decimal(18,2) NOT NULL,
  `metodo` varchar(40) NOT NULL,
  `referencia` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `permisos`
--

CREATE TABLE `permisos` (
  `id` bigint(20) NOT NULL,
  `clave` varchar(100) NOT NULL,
  `nombre` varchar(150) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `permisos`
--

INSERT INTO `permisos` (`id`, `clave`, `nombre`) VALUES
(1, 'USUARIOS_VER', 'Ver usuarios'),
(2, 'USUARIOS_CREAR', 'Crear usuarios'),
(3, 'USUARIOS_EDITAR', 'Editar usuarios'),
(4, 'USUARIOS_ELIMINAR', 'Eliminar usuarios'),
(5, 'ROLES_VER', 'Ver roles'),
(6, 'ROLES_GESTIONAR', 'Gestionar roles y permisos'),
(7, 'PERSONAL_VER', 'Ver personal'),
(8, 'PERSONAL_GESTIONAR', 'Gestionar personal'),
(17, 'DECLARACIONES_READ', 'Ver Declaraciones'),
(18, 'DECLARACIONES_WRITE', 'Crear/Editar Declaraciones'),
(19, 'DECLARACIONES_DELETE', 'Eliminar Declaraciones'),
(20, 'CONTRIBUYENTES_READ', 'Ver Contribuyentes'),
(21, 'CONTRIBUYENTES_WRITE', 'Crear/Editar Contribuyentes'),
(22, 'CONTRIBUYENTES_DELETE', 'Eliminar Contribuyentes'),
(23, 'IMPUESTOS_READ', 'Ver Impuestos'),
(24, 'IMPUESTOS_WRITE', 'Crear/Editar Impuestos'),
(25, 'IMPUESTOS_DELETE', 'Eliminar Impuestos'),
(26, 'RETENCIONES_READ', 'Ver Retenciones'),
(27, 'RETENCIONES_WRITE', 'Crear/Editar Retenciones'),
(28, 'RETENCIONES_DELETE', 'Eliminar Retenciones'),
(29, 'COMPROBANTES_READ', 'Ver Comprobantes'),
(30, 'COMPROBANTES_WRITE', 'Crear/Editar Comprobantes'),
(31, 'COMPROBANTES_DELETE', 'Eliminar Comprobantes'),
(32, 'USUARIOS_READ', 'Ver Usuarios'),
(33, 'USUARIOS_WRITE', 'Crear/Editar Usuarios'),
(34, 'USUARIOS_DELETE', 'Eliminar Usuarios'),
(35, 'ROLES_READ', 'Ver Roles'),
(36, 'ROLES_WRITE', 'Crear/Editar Roles'),
(37, 'ROLES_DELETE', 'Eliminar Roles'),
(38, 'REPORTES_READ', 'Ver Reportes'),
(39, 'REPORTES_EXPORT', 'Exportar Reportes'),
(40, 'CONFIGURACION_WRITE', 'Configurar Sistema');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `personal`
--

CREATE TABLE `personal` (
  `id` bigint(20) NOT NULL,
  `usuario_id` bigint(20) DEFAULT NULL,
  `nombres` varchar(120) NOT NULL,
  `apellidos` varchar(120) NOT NULL,
  `documento` varchar(50) NOT NULL,
  `tipo_documento` varchar(10) DEFAULT NULL,
  `email` varchar(150) DEFAULT NULL,
  `direccion` varchar(200) DEFAULT NULL,
  `telefono` varchar(30) DEFAULT NULL,
  `cargo` varchar(120) DEFAULT NULL,
  `departamento` varchar(50) DEFAULT NULL,
  `fecha_ingreso` date DEFAULT NULL,
  `salario` decimal(12,2) DEFAULT NULL,
  `observaciones` varchar(500) DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `personal`
--

INSERT INTO `personal` (`id`, `usuario_id`, `nombres`, `apellidos`, `documento`, `tipo_documento`, `email`, `direccion`, `telefono`, `cargo`, `departamento`, `fecha_ingreso`, `salario`, `observaciones`, `activo`) VALUES
(7, 4, 'Alfonso David', 'Lloyo Ramos', '31760908', 'CC', 'Alfonso@gmail.com', 'Antimano', '04142159285', 'Empleado', 'TRIBUTARIO', '2025-09-15', 12.00, '', 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `retenciones`
--

CREATE TABLE `retenciones` (
  `id` bigint(20) NOT NULL,
  `contribuyente_id` bigint(20) NOT NULL,
  `fecha` date NOT NULL,
  `concepto` varchar(200) NOT NULL,
  `porcentaje` decimal(10,4) NOT NULL,
  `monto_base` decimal(18,2) NOT NULL,
  `monto_retenido` decimal(18,2) NOT NULL,
  `estado` varchar(20) NOT NULL DEFAULT 'PENDIENTE',
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp(),
  `actualizado_en` timestamp NULL DEFAULT NULL ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `roles`
--

CREATE TABLE `roles` (
  `id` bigint(20) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `roles`
--

INSERT INTO `roles` (`id`, `nombre`, `descripcion`) VALUES
(6, 'supervisor', 'ver usuario'),
(7, 'ADMIN_TRIBUTARIO', 'Administrador del sistema tributario con acceso completo'),
(8, 'USUARIO_BASICO', 'Usuario básico con permisos limitados');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `roles_permisos`
--

CREATE TABLE `roles_permisos` (
  `rol_id` bigint(20) NOT NULL,
  `permiso_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `roles_permisos`
--

INSERT INTO `roles_permisos` (`rol_id`, `permiso_id`) VALUES
(6, 1),
-- ADMIN_TRIBUTARIO - Acceso completo a todos los módulos
(7, 5), (7, 6), -- Roles
(7, 7), (7, 8), -- Personal
(7, 17), (7, 18), (7, 19), -- Declaraciones
(7, 20), (7, 21), (7, 22), -- Contribuyentes
(7, 23), (7, 24), (7, 25), -- Impuestos
(7, 26), (7, 27), (7, 28), -- Retenciones
(7, 29), (7, 30), (7, 31), -- Comprobantes
(7, 32), (7, 33), (7, 34), -- Usuarios
(7, 35), (7, 36), (7, 37), -- Roles adicionales
-- Reportes y configuración
(7, 38), (7, 39), (7, 40),
-- USUARIO_BASICO - Solo lectura en módulos principales
(8, 17), -- Ver Declaraciones
(8, 20), -- Ver Contribuyentes
(8, 23), -- Ver Impuestos
(8, 26), -- Ver Retenciones
(8, 29); -- Ver Comprobantes

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

CREATE TABLE `usuarios` (
  `id` bigint(20) NOT NULL,
  `username` varchar(100) NOT NULL,
  `email` varchar(180) NOT NULL,
  `password` varchar(255) NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT 1,
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`id`, `username`, `email`, `password`, `activo`, `creado_en`) VALUES
(1, 'superadmin', 'superadmin@tributario.local', '$2a$10$nBIIsOCv7KOmZg1llHylG.yB9hHiA4fh18G..O0ymxBrA062I0uNG', 1, '2025-09-12 13:28:19'),
(4, 'Alfonso@gmail.com', 'Alfonso@gmail.com', '$2a$10$mf1uvC5DWecULZl9gN.uwORAq/6kmRZ3rvuq4CDvPjR9RaFGmIlfi', 1, '2025-09-15 13:40:19');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios_roles`
--

CREATE TABLE `usuarios_roles` (
  `usuario_id` bigint(20) NOT NULL,
  `rol_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `usuarios_roles`
--

INSERT INTO `usuarios_roles` (`usuario_id`, `rol_id`) VALUES
(1, 7), -- superadmin -> ADMIN_TRIBUTARIO (acceso total)
(4, 6);

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `comprobantes`
--
ALTER TABLE `comprobantes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_comp` (`tipo`,`serie`,`numero`),
  ADD KEY `fk_comp_contribuyente` (`contribuyente_id`),
  ADD KEY `idx_comprobantes_estado` (`estado`),
  ADD KEY `idx_comprobantes_creado_en` (`creado_en`);

--
-- Indices de la tabla `contribuyentes`
--
ALTER TABLE `contribuyentes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `rif` (`rif`);

--
-- Indices de la tabla `declaraciones`
--
ALTER TABLE `declaraciones`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_dec_contribuyente` (`contribuyente_id`),
  ADD KEY `fk_dec_impuesto` (`impuesto_id`);

--
-- Indices de la tabla `flyway_schema_history`
--
ALTER TABLE `flyway_schema_history`
  ADD PRIMARY KEY (`installed_rank`),
  ADD KEY `flyway_schema_history_s_idx` (`success`);

--
-- Indices de la tabla `impuestos`
--
ALTER TABLE `impuestos`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `codigo` (`codigo`);

--
-- Indices de la tabla `pagos`
--
ALTER TABLE `pagos`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_pago_declaracion` (`declaracion_id`);

--
-- Indices de la tabla `permisos`
--
ALTER TABLE `permisos`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `clave` (`clave`);

--
-- Indices de la tabla `personal`
--
ALTER TABLE `personal`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `documento` (`documento`),
  ADD KEY `fk_personal_usuario` (`usuario_id`);

--
-- Indices de la tabla `retenciones`
--
ALTER TABLE `retenciones`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_ret_contribuyente` (`contribuyente_id`),
  ADD KEY `idx_ret_fecha` (`fecha`),
  ADD KEY `idx_ret_estado` (`estado`),
  ADD KEY `idx_ret_concepto` (`concepto`);

--
-- Indices de la tabla `roles`
--
ALTER TABLE `roles`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `nombre` (`nombre`);

--
-- Indices de la tabla `roles_permisos`
--
ALTER TABLE `roles_permisos`
  ADD PRIMARY KEY (`rol_id`,`permiso_id`),
  ADD KEY `fk_rp_permiso` (`permiso_id`);

--
-- Indices de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indices de la tabla `usuarios_roles`
--
ALTER TABLE `usuarios_roles`
  ADD PRIMARY KEY (`usuario_id`,`rol_id`),
  ADD KEY `fk_ur_rol` (`rol_id`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `comprobantes`
--
ALTER TABLE `comprobantes`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de la tabla `contribuyentes`
--
ALTER TABLE `contribuyentes`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT de la tabla `declaraciones`
--
ALTER TABLE `declaraciones`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `impuestos`
--
ALTER TABLE `impuestos`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `pagos`
--
ALTER TABLE `pagos`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `permisos`
--
ALTER TABLE `permisos`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=41;

--
-- AUTO_INCREMENT de la tabla `personal`
--
ALTER TABLE `personal`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT de la tabla `retenciones`
--
ALTER TABLE `retenciones`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=39;

--
-- AUTO_INCREMENT de la tabla `roles`
--
ALTER TABLE `roles`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `comprobantes`
--
ALTER TABLE `comprobantes`
  ADD CONSTRAINT `fk_comp_contribuyente` FOREIGN KEY (`contribuyente_id`) REFERENCES `contribuyentes` (`id`);

--
-- Filtros para la tabla `declaraciones`
--
ALTER TABLE `declaraciones`
  ADD CONSTRAINT `fk_dec_contribuyente` FOREIGN KEY (`contribuyente_id`) REFERENCES `contribuyentes` (`id`),
  ADD CONSTRAINT `fk_dec_impuesto` FOREIGN KEY (`impuesto_id`) REFERENCES `impuestos` (`id`);

--
-- Filtros para la tabla `pagos`
--
ALTER TABLE `pagos`
  ADD CONSTRAINT `fk_pago_declaracion` FOREIGN KEY (`declaracion_id`) REFERENCES `declaraciones` (`id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `personal`
--
ALTER TABLE `personal`
  ADD CONSTRAINT `fk_personal_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL;

--
-- Filtros para la tabla `retenciones`
--
ALTER TABLE `retenciones`
  ADD CONSTRAINT `fk_ret_temp_contribuyente` FOREIGN KEY (`contribuyente_id`) REFERENCES `contribuyentes` (`id`);

--
-- Filtros para la tabla `roles_permisos`
--
ALTER TABLE `roles_permisos`
  ADD CONSTRAINT `fk_rp_permiso` FOREIGN KEY (`permiso_id`) REFERENCES `permisos` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_rp_rol` FOREIGN KEY (`rol_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `usuarios_roles`
--
ALTER TABLE `usuarios_roles`
  ADD CONSTRAINT `fk_ur_rol` FOREIGN KEY (`rol_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_ur_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
