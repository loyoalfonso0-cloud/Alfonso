-- =====================================================
-- MIGRACIÓN V33: MÓDULO DE TRANSACCIONES COMPLETO
-- =====================================================
-- Eliminar tabla si existe para recrearla completamente
DROP TABLE IF EXISTS transacciones;

-- =====================================================
-- CREAR TABLA TRANSACCIONES
-- =====================================================
CREATE TABLE transacciones (
    -- Campos principales
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_transaccion VARCHAR(100) NOT NULL UNIQUE COMMENT 'Número único de identificación de la transacción',
    
    -- Clasificación de la transacción
    tipo_transaccion VARCHAR(50) NOT NULL COMMENT 'Tipo de transacción según enum TipoTransaccion',
    estado VARCHAR(50) NOT NULL DEFAULT 'PENDIENTE' COMMENT 'Estado actual de la transacción',
    
    -- Información financiera
    monto DECIMAL(15,2) NOT NULL COMMENT 'Monto de la transacción en la moneda base del sistema',
    concepto VARCHAR(500) NOT NULL COMMENT 'Descripción o concepto de la transacción',
    
    -- Referencias externas
    referencia_externa VARCHAR(100) COMMENT 'Referencia del sistema externo o banco',
    numero_comprobante VARCHAR(100) COMMENT 'Número del comprobante o recibo',
    
    -- Relaciones
    contribuyente_id BIGINT COMMENT 'ID del contribuyente asociado (opcional)',
    entidad_relacionada_tipo VARCHAR(50) COMMENT 'Tipo de entidad relacionada (SERVICIO, IMPUESTO, TASA, etc.)',
    entidad_relacionada_id BIGINT COMMENT 'ID de la entidad relacionada',
    
    -- Fechas del flujo de transacción
    fecha_transaccion DATETIME NOT NULL COMMENT 'Fecha y hora de la transacción',
    fecha_procesamiento DATETIME COMMENT 'Fecha y hora cuando se procesó',
    fecha_confirmacion DATETIME COMMENT 'Fecha y hora cuando se confirmó',
    
    -- Información adicional
    observaciones TEXT COMMENT 'Observaciones adicionales de la transacción',
    
    -- Auditoría
    usuario_registro VARCHAR(100) COMMENT 'Usuario que registró la transacción',
    usuario_procesamiento VARCHAR(100) COMMENT 'Usuario que procesó la transacción',
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de registro en el sistema',
    fecha_modificacion DATETIME COMMENT 'Fecha de última modificación'
) COMMENT = 'Tabla para gestionar todas las transacciones financieras del sistema tributario';

-- =====================================================
-- CREAR ÍNDICES PARA OPTIMIZACIÓN
-- =====================================================
-- Índice único para número de transacción
CREATE UNIQUE INDEX idx_transacciones_numero ON transacciones (numero_transaccion);

-- Índices para consultas frecuentes
CREATE INDEX idx_transacciones_tipo ON transacciones (tipo_transaccion);
CREATE INDEX idx_transacciones_estado ON transacciones (estado);
CREATE INDEX idx_transacciones_contribuyente ON transacciones (contribuyente_id);
CREATE INDEX idx_transacciones_fecha ON transacciones (fecha_transaccion);
CREATE INDEX idx_transacciones_monto ON transacciones (monto);

-- Índices compuestos para consultas complejas
CREATE INDEX idx_transacciones_tipo_estado ON transacciones (tipo_transaccion, estado);
CREATE INDEX idx_transacciones_fecha_estado ON transacciones (fecha_transaccion, estado);
CREATE INDEX idx_transacciones_entidad ON transacciones (entidad_relacionada_tipo, entidad_relacionada_id);

-- Índices para referencias externas
CREATE INDEX idx_transacciones_referencia ON transacciones (referencia_externa);
CREATE INDEX idx_transacciones_comprobante ON transacciones (numero_comprobante);

-- Índices para auditoría
CREATE INDEX idx_transacciones_usuario_registro ON transacciones (usuario_registro);
CREATE INDEX idx_transacciones_fecha_registro ON transacciones (fecha_registro);

-- =====================================================
-- CREAR FOREIGN KEYS
-- =====================================================
-- Relación con contribuyentes (opcional)
ALTER TABLE transacciones 
ADD CONSTRAINT fk_transacciones_contribuyente 
FOREIGN KEY (contribuyente_id) 
REFERENCES contribuyentes(id) 
ON DELETE SET NULL 
ON UPDATE CASCADE;

-- =====================================================
-- CREAR PERMISOS DEL MÓDULO
-- =====================================================
-- Insertar permisos (usando IGNORE para evitar duplicados)
INSERT IGNORE INTO permisos (clave, nombre) VALUES
('TRANSACCIONES_READ', 'Ver transacciones'),
('TRANSACCIONES_WRITE', 'Crear y editar transacciones'),
('TRANSACCIONES_GESTIONAR', 'Gestión completa de transacciones (crear, editar, procesar, confirmar, anular)');

-- =====================================================
-- ASIGNAR PERMISOS A ROLES
-- =====================================================
-- Asignar todos los permisos al rol ADMIN_TRIBUTARIO
INSERT IGNORE INTO roles_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.nombre = 'ADMIN_TRIBUTARIO'
AND p.clave IN ('TRANSACCIONES_READ', 'TRANSACCIONES_WRITE', 'TRANSACCIONES_GESTIONAR');

-- Asignar permisos al rol SUPER_ADMIN si existe
INSERT IGNORE INTO roles_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.nombre = 'SUPER_ADMIN'
AND p.clave IN ('TRANSACCIONES_READ', 'TRANSACCIONES_WRITE', 'TRANSACCIONES_GESTIONAR');

-- Asignar permisos básicos de lectura al rol USUARIO_TRIBUTARIO si existe
INSERT IGNORE INTO roles_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.nombre = 'USUARIO_TRIBUTARIO'
AND p.clave = 'TRANSACCIONES_READ';

-- =====================================================
-- INSERTAR DATOS DE EJEMPLO
-- =====================================================
INSERT INTO transacciones (
    numero_transaccion, tipo_transaccion, estado, monto, concepto,
    contribuyente_id, entidad_relacionada_tipo, entidad_relacionada_id,
    fecha_transaccion, usuario_registro, referencia_externa, observaciones
) VALUES
-- Transacciones de pagos de servicios
('TXN-2025-12345001', 'PAGO_SERVICIO', 'CONFIRMADA', 150.00, 'Pago de servicio de agua potable', 
 1, 'SERVICIO', 1, '2025-01-01 10:30:00', 'admin', 'REF-001', 'Pago realizado por transferencia bancaria'),

('TXN-2025-12345002', 'PAGO_SERVICIO', 'CONFIRMADA', 85.00, 'Pago de servicio de alumbrado público', 
 1, 'SERVICIO', 2, '2025-01-01 14:15:00', 'admin', 'REF-002', 'Pago en efectivo'),

-- Transacciones de impuestos
('TXN-2025-12345003', 'PAGO_IMPUESTO', 'CONFIRMADA', 2500.00, 'Pago de impuesto sobre inmuebles urbanos', 
 2, 'IMPUESTO', 1, '2025-01-02 09:45:00', 'admin', 'REF-003', 'Pago anual completo'),

('TXN-2025-12345004', 'PAGO_IMPUESTO', 'PROCESADA', 1200.00, 'Pago de impuesto sobre actividades económicas', 
 2, 'IMPUESTO', 2, '2025-01-02 16:20:00', 'admin', 'REF-004', 'Pago trimestral'),

-- Transacciones de tasas
('TXN-2025-12345005', 'PAGO_TASA', 'CONFIRMADA', 75.50, 'Pago de tasa de aseo urbano', 
 1, 'TASA', 1, '2025-01-03 11:10:00', 'admin', 'REF-005', 'Pago mensual'),

('TXN-2025-12345006', 'PAGO_TASA', 'PROCESADA', 120.00, 'Pago de tasa de protección civil', 
 3, 'TASA', 2, '2025-01-03 13:30:00', 'admin', 'REF-006', 'Pago semestral'),

-- Transacciones de multas
('TXN-2025-12345007', 'PAGO_MULTA', 'CONFIRMADA', 300.00, 'Pago de multa de tránsito', 
 2, 'MULTA', 1, '2025-01-04 08:45:00', 'admin', 'REF-007', 'Multa por exceso de velocidad'),

-- Ingresos diversos
('TXN-2025-12345008', 'INGRESO', 'PENDIENTE', 500.00, 'Ingreso por concepto de licencia comercial', 
 3, 'LICENCIA', 1, '2025-01-04 15:00:00', 'admin', 'REF-008', 'Licencia nueva'),

-- Transferencias y ajustes
('TXN-2025-12345009', 'TRANSFERENCIA', 'CONFIRMADA', 1000.00, 'Transferencia entre cuentas municipales', 
 NULL, NULL, NULL, '2025-01-05 10:00:00', 'admin', 'REF-009', 'Movimiento interno de fondos'),

('TXN-2025-12345010', 'AJUSTE', 'PROCESADA', 50.00, 'Ajuste por diferencia cambiaria', 
 NULL, NULL, NULL, '2025-01-05 17:30:00', 'admin', 'REF-010', 'Ajuste contable'),

-- Devoluciones
('TXN-2025-12345011', 'DEVOLUCION', 'CONFIRMADA', 125.00, 'Devolución por pago duplicado', 
 1, 'SERVICIO', 1, '2025-01-06 12:00:00', 'admin', 'REF-011', 'Pago duplicado detectado'),

-- Comisiones
('TXN-2025-12345012', 'COMISION', 'CONFIRMADA', 15.00, 'Comisión bancaria por transferencia', 
 NULL, NULL, NULL, '2025-01-06 14:30:00', 'admin', 'REF-012', 'Comisión del banco');

-- =====================================================
-- COMENTARIOS FINALES Y DOCUMENTACIÓN
-- =====================================================
-- Actualizar comentarios de tabla y columnas para documentación completa
ALTER TABLE transacciones MODIFY COLUMN tipo_transaccion VARCHAR(50) 
COMMENT 'Valores: INGRESO, EGRESO, PAGO_SERVICIO, PAGO_IMPUESTO, PAGO_TASA, PAGO_MULTA, TRANSFERENCIA, DEVOLUCION, AJUSTE, COMISION, INTERES, DESCUENTO';

ALTER TABLE transacciones MODIFY COLUMN estado VARCHAR(50) 
COMMENT 'Valores: PENDIENTE, PROCESANDO, PROCESADA, CONFIRMADA, ANULADA, RECHAZADA, REVERTIDA';
