-- Agregar campo cedula_personal a la tabla auditoria_logs si no existe
ALTER TABLE auditoria_logs ADD COLUMN IF NOT EXISTS cedula_personal VARCHAR(20) AFTER usuario;
