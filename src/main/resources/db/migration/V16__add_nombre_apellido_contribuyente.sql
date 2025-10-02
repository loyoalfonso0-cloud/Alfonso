-- Agregar campos nombre y apellido para personas naturales si no existen
ALTER TABLE contribuyentes 
ADD COLUMN IF NOT EXISTS nombre VARCHAR(100),
ADD COLUMN IF NOT EXISTS apellido VARCHAR(100);

-- Hacer razón social opcional (remover NOT NULL)
ALTER TABLE contribuyentes 
MODIFY COLUMN razon_social VARCHAR(200) NULL;
