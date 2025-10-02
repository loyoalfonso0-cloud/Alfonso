-- Crear tabla de tasas municipales

-- Crear tabla de tasas (solo si no existe)
CREATE TABLE IF NOT EXISTS tasas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contribuyente_id BIGINT NOT NULL,
    numero_tasa VARCHAR(50) UNIQUE NOT NULL,
    tipo_tasa VARCHAR(50) NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    monto_base DECIMAL(15,2) NOT NULL,
    monto_pagado DECIMAL(15,2) DEFAULT 0.00,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVA',
    periodo_facturacion VARCHAR(20) NOT NULL,
    fecha_inicio DATETIME NOT NULL,
    fecha_vencimiento DATETIME NULL,
    fecha_pago DATETIME NULL,
    direccion VARCHAR(200),
    zona_municipal VARCHAR(100),
    area_inmueble DECIMAL(10,2),
    valor_catastral DECIMAL(15,2),
    observaciones TEXT,
    usuario_registro VARCHAR(100) NOT NULL,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_modificacion VARCHAR(100),
    fecha_modificacion DATETIME,
    
    CONSTRAINT fk_tasas_contribuyente 
        FOREIGN KEY (contribuyente_id) REFERENCES contribuyentes(id),
    
    CONSTRAINT chk_tasas_monto_base 
        CHECK (monto_base > 0),
    
    CONSTRAINT chk_tasas_monto_pagado 
        CHECK (monto_pagado >= 0),
    
    CONSTRAINT chk_tasas_fechas 
        CHECK (fecha_vencimiento IS NULL OR fecha_vencimiento > fecha_inicio)
);

-- Índices para optimizar consultas (solo si no existen)
CREATE INDEX IF NOT EXISTS idx_tasas_contribuyente ON tasas(contribuyente_id);
CREATE INDEX IF NOT EXISTS idx_tasas_estado ON tasas(estado);
CREATE INDEX IF NOT EXISTS idx_tasas_tipo ON tasas(tipo_tasa);
CREATE INDEX IF NOT EXISTS idx_tasas_fecha_vencimiento ON tasas(fecha_vencimiento);
CREATE INDEX IF NOT EXISTS idx_tasas_fecha_registro ON tasas(fecha_registro);
CREATE INDEX IF NOT EXISTS idx_tasas_numero ON tasas(numero_tasa);
CREATE INDEX IF NOT EXISTS idx_tasas_zona ON tasas(zona_municipal);

-- Comentarios para documentación
ALTER TABLE tasas COMMENT = 'Tabla de tasas municipales del sistema tributario';

-- Insertar algunos datos de ejemplo para pruebas
INSERT IGNORE INTO tasas (
    contribuyente_id, numero_tasa, tipo_tasa, descripcion, monto_base, 
    periodo_facturacion, fecha_inicio, fecha_vencimiento, direccion, 
    zona_municipal, usuario_registro
) VALUES 
(1, 'TAS-202501-0001', 'ASEO_URBANO', 'Tasa de aseo urbano - Enero 2025', 25.00, 
 'MENSUAL', '2025-01-01 00:00:00', '2025-01-31 23:59:59', 'Av. Principal #123', 
 'Centro', 'admin'),
(1, 'TAS-202501-0002', 'ALUMBRADO_PUBLICO', 'Tasa de alumbrado público - Enero 2025', 15.00, 
 'MENSUAL', '2025-01-01 00:00:00', '2025-01-31 23:59:59', 'Av. Principal #123', 
 'Centro', 'admin'),
(2, 'TAS-202501-0003', 'ASEO_URBANO', 'Tasa de aseo urbano - Enero 2025', 30.00, 
 'MENSUAL', '2025-01-01 00:00:00', '2025-01-31 23:59:59', 'Calle Comercio #456', 
 'Comercial', 'admin');
