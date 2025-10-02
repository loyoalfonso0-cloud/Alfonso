-- Migración para crear tabla de Servicios Municipales
-- Versión: V31
-- Descripción: Crear tabla servicios con todos los campos necesarios

-- Crear tabla servicios
CREATE TABLE IF NOT EXISTS servicios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_servicio VARCHAR(20) NOT NULL UNIQUE,
    contribuyente_id BIGINT NOT NULL,
    tipo_servicio ENUM(
        'AGUA_POTABLE',
        'ALCANTARILLADO', 
        'ASEO_URBANO',
        'ALUMBRADO_PUBLICO',
        'GAS_DOMESTICO',
        'TELEFONIA',
        'INTERNET',
        'TELEVISION_CABLE',
        'MANTENIMIENTO_VIAL',
        'SEGURIDAD_CIUDADANA',
        'BOMBEROS',
        'CEMENTERIO',
        'MERCADO_MUNICIPAL',
        'TRANSPORTE_PUBLICO',
        'PARQUES_JARDINES',
        'OTROS'
    ) NOT NULL,
    estado ENUM(
        'ACTIVO',
        'SUSPENDIDO',
        'CORTADO',
        'INACTIVO',
        'MANTENIMIENTO',
        'PENDIENTE_INSTALACION'
    ) NOT NULL DEFAULT 'ACTIVO',
    tarifa_base DECIMAL(10,2) NOT NULL,
    consumo_actual DECIMAL(10,2) DEFAULT 0.00,
    consumo_anterior DECIMAL(10,2) DEFAULT 0.00,
    monto_facturado DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    monto_pagado DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    fecha_instalacion DATE,
    fecha_ultima_lectura DATE,
    fecha_proxima_lectura DATE,
    fecha_corte DATE,
    direccion_servicio VARCHAR(500),
    medidor VARCHAR(50),
    observaciones TEXT,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME,
    usuario_registro VARCHAR(100),
    usuario_modificacion VARCHAR(100),
    
    -- Índices
    INDEX idx_servicios_numero (numero_servicio),
    INDEX idx_servicios_contribuyente (contribuyente_id),
    INDEX idx_servicios_tipo (tipo_servicio),
    INDEX idx_servicios_estado (estado),
    INDEX idx_servicios_fecha_registro (fecha_registro),
    INDEX idx_servicios_fecha_corte (fecha_corte),
    INDEX idx_servicios_deuda (monto_facturado, monto_pagado),
    
    -- Clave foránea
    CONSTRAINT fk_servicios_contribuyente 
        FOREIGN KEY (contribuyente_id) 
        REFERENCES contribuyentes(id) 
        ON DELETE RESTRICT ON UPDATE CASCADE
);

-- Comentarios de la tabla
ALTER TABLE servicios COMMENT = 'Tabla para gestionar servicios municipales';

-- Comentarios de columnas importantes
ALTER TABLE servicios 
MODIFY COLUMN numero_servicio VARCHAR(20) NOT NULL UNIQUE COMMENT 'Número único del servicio formato SRV-YYYY-NNNN',
MODIFY COLUMN tipo_servicio ENUM(
    'AGUA_POTABLE',
    'ALCANTARILLADO', 
    'ASEO_URBANO',
    'ALUMBRADO_PUBLICO',
    'GAS_DOMESTICO',
    'TELEFONIA',
    'INTERNET',
    'TELEVISION_CABLE',
    'MANTENIMIENTO_VIAL',
    'SEGURIDAD_CIUDADANA',
    'BOMBEROS',
    'CEMENTERIO',
    'MERCADO_MUNICIPAL',
    'TRANSPORTE_PUBLICO',
    'PARQUES_JARDINES',
    'OTROS'
) NOT NULL COMMENT 'Tipo de servicio municipal',
MODIFY COLUMN estado ENUM(
    'ACTIVO',
    'SUSPENDIDO',
    'CORTADO',
    'INACTIVO',
    'MANTENIMIENTO',
    'PENDIENTE_INSTALACION'
) NOT NULL DEFAULT 'ACTIVO' COMMENT 'Estado actual del servicio',
MODIFY COLUMN tarifa_base DECIMAL(10,2) NOT NULL COMMENT 'Tarifa base del servicio',
MODIFY COLUMN consumo_actual DECIMAL(10,2) DEFAULT 0.00 COMMENT 'Lectura actual del medidor',
MODIFY COLUMN consumo_anterior DECIMAL(10,2) DEFAULT 0.00 COMMENT 'Lectura anterior del medidor',
MODIFY COLUMN monto_facturado DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'Monto total facturado',
MODIFY COLUMN monto_pagado DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'Monto total pagado',
MODIFY COLUMN medidor VARCHAR(50) COMMENT 'Número del medidor (si aplica)';

-- Insertar algunos datos de ejemplo para testing (solo si no existen)
INSERT IGNORE INTO servicios (
    numero_servicio, contribuyente_id, tipo_servicio, estado, tarifa_base, 
    monto_facturado, direccion_servicio, usuario_registro
) VALUES 
('SRV-2025-0001', 1, 'AGUA_POTABLE', 'ACTIVO', 15.00, 15.00, 'Av. Principal #123', 'admin'),
('SRV-2025-0002', 2, 'ASEO_URBANO', 'ACTIVO', 8.00, 8.00, 'Calle Secundaria #456', 'admin'),
('SRV-2025-0003', 1, 'ALUMBRADO_PUBLICO', 'ACTIVO', 12.00, 12.00, 'Av. Principal #123', 'admin'),
('SRV-2025-0004', 3, 'GAS_DOMESTICO', 'SUSPENDIDO', 25.00, 50.00, 'Urbanización Los Pinos #789', 'admin'),
('SRV-2025-0005', 2, 'INTERNET', 'ACTIVO', 35.00, 35.00, 'Calle Secundaria #456', 'admin'),
('SRV-2025-0006', 1, 'TELEFONIA', 'ACTIVO', 20.00, 20.00, 'Av. Principal #123', 'admin'),
('SRV-2025-0007', 2, 'TELEVISION_CABLE', 'ACTIVO', 30.00, 30.00, 'Calle Secundaria #456', 'admin'),
('SRV-2025-0008', 3, 'ALCANTARILLADO', 'MANTENIMIENTO', 18.00, 18.00, 'Urbanización Los Pinos #789', 'admin');

-- Verificar que la tabla se creó correctamente
SELECT 'Tabla servicios creada exitosamente' as resultado;
