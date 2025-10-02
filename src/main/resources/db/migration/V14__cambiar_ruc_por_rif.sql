-- Migración para cambiar RUC por RIF (adaptación a Venezuela)
-- V14__cambiar_ruc_por_rif.sql

-- Cambiar nombre de columna de 'ruc' a 'rif' en tabla contribuyentes
ALTER TABLE contribuyentes 
CHANGE COLUMN ruc rif VARCHAR(20) NOT NULL;

-- Actualizar el índice único
ALTER TABLE contribuyentes 
DROP INDEX ruc;

ALTER TABLE contribuyentes 
ADD UNIQUE KEY rif (rif);

-- Actualizar datos existentes para usar formato RIF venezolano
-- Convertir RUCs existentes a formato RIF básico (agregando V- al inicio)
UPDATE contribuyentes 
SET rif = CONCAT('V-', SUBSTRING(rif, 1, 8), '-', SUBSTRING(rif, 9, 1))
WHERE rif REGEXP '^[0-9]{11}$';

-- Para RUCs de 10 dígitos, agregar un 0 al final
UPDATE contribuyentes 
SET rif = CONCAT('V-', SUBSTRING(rif, 1, 8), '-', SUBSTRING(rif, 9, 2))
WHERE rif REGEXP '^[0-9]{10}$';

-- Comentario: Los datos existentes se han convertido automáticamente al formato RIF venezolano
-- Formato: V-12345678-9 (V para venezolanos)
