-- Crear tabla de multas (solo si no existe)
CREATE TABLE IF NOT EXISTS multas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contribuyente_id BIGINT NOT NULL,
    numero_multa VARCHAR(50) UNIQUE NOT NULL,
    tipo_infraccion VARCHAR(50) NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    monto DECIMAL(15,2) NOT NULL,
    monto_pagado DECIMAL(15,2) DEFAULT 0.00,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    fecha_infraccion DATETIME NOT NULL,
    fecha_vencimiento DATETIME NOT NULL,
    fecha_pago DATETIME NULL,
    observaciones TEXT,
    usuario_registro VARCHAR(100) NOT NULL,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_modificacion VARCHAR(100),
    fecha_modificacion DATETIME,
    
    CONSTRAINT fk_multas_contribuyente 
        FOREIGN KEY (contribuyente_id) REFERENCES contribuyentes(id),
    
    CONSTRAINT chk_multas_monto 
        CHECK (monto > 0),
    
    CONSTRAINT chk_multas_monto_pagado 
        CHECK (monto_pagado >= 0),
    
    CONSTRAINT chk_multas_fechas 
        CHECK (fecha_vencimiento > fecha_infraccion)
);

-- Índices para optimizar consultas (solo si no existen)
CREATE INDEX IF NOT EXISTS idx_multas_contribuyente ON multas(contribuyente_id);
CREATE INDEX IF NOT EXISTS idx_multas_estado ON multas(estado);
CREATE INDEX IF NOT EXISTS idx_multas_tipo_infraccion ON multas(tipo_infraccion);
CREATE INDEX IF NOT EXISTS idx_multas_fecha_vencimiento ON multas(fecha_vencimiento);
CREATE INDEX IF NOT EXISTS idx_multas_fecha_registro ON multas(fecha_registro);
CREATE INDEX IF NOT EXISTS idx_multas_numero ON multas(numero_multa);

-- Comentarios para documentación (removidos para compatibilidad)
