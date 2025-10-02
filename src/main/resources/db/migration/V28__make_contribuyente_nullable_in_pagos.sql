-- Hacer que contribuyente_id sea nullable en la tabla pagos
-- para permitir pagos que solo tengan multa_id

ALTER TABLE pagos 
MODIFY COLUMN contribuyente_id BIGINT NULL;
