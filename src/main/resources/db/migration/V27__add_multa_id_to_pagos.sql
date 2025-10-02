-- Agregar campo multa_id a la tabla pagos para vincular pagos con multas

-- Verificar si la columna ya existe antes de agregarla
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'pagos' 
     AND COLUMN_NAME = 'multa_id') = 0,
    'ALTER TABLE pagos ADD COLUMN multa_id BIGINT NULL',
    'SELECT "La columna multa_id ya existe" AS mensaje'
));

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Crear índice si no existe
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'pagos' 
     AND INDEX_NAME = 'idx_pagos_multa_id') = 0,
    'CREATE INDEX idx_pagos_multa_id ON pagos(multa_id)',
    'SELECT "El índice idx_pagos_multa_id ya existe" AS mensaje'
));

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
