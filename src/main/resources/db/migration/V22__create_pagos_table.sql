-- Eliminar tabla si existe para recrearla
DROP TABLE IF EXISTS pagos;

-- Crear tabla de pagos
CREATE TABLE pagos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contribuyente_id BIGINT NOT NULL,
    declaracion_id BIGINT NULL,
    monto DECIMAL(15,2) NOT NULL,
    metodo_pago VARCHAR(50) NOT NULL,
    referencia VARCHAR(100) NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_pago DATETIME NOT NULL,
    concepto VARCHAR(200) NOT NULL,
    comprobante_path VARCHAR(500) NULL,
    creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_registro VARCHAR(100) NULL,
    actualizado_en DATETIME NULL
);

-- Agregar constraints de foreign key
ALTER TABLE pagos 
ADD CONSTRAINT fk_pagos_contribuyente 
FOREIGN KEY (contribuyente_id) REFERENCES contribuyentes(id) ON DELETE RESTRICT;

ALTER TABLE pagos 
ADD CONSTRAINT fk_pagos_declaracion 
FOREIGN KEY (declaracion_id) REFERENCES declaraciones(id) ON DELETE SET NULL;

-- Índices para optimizar consultas
CREATE INDEX idx_pagos_contribuyente ON pagos(contribuyente_id);
CREATE INDEX idx_pagos_declaracion ON pagos(declaracion_id);
CREATE INDEX idx_pagos_fecha ON pagos(fecha_pago);
CREATE INDEX idx_pagos_estado ON pagos(estado);
CREATE INDEX idx_pagos_metodo ON pagos(metodo_pago);
CREATE INDEX idx_pagos_referencia ON pagos(referencia);

-- Comentarios para documentación
ALTER TABLE pagos COMMENT = 'Tabla para registrar todos los pagos realizados por contribuyentes';
