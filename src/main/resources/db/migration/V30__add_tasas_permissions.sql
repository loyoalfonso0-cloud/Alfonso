-- Agregar permisos para el módulo de Tasas Municipales

-- Insertar permisos para tasas (solo si no existen)
INSERT IGNORE INTO permisos (clave, nombre) VALUES
('TASAS_READ', 'Leer tasas municipales'),
('TASAS_WRITE', 'Crear y editar tasas municipales'),
('TASAS_GESTIONAR', 'Gestión completa de tasas municipales');

-- Asignar permisos al rol ADMIN (ID = 1)
INSERT IGNORE INTO roles_permisos (rol_id, permiso_id)
SELECT 1, p.id FROM permisos p WHERE p.clave IN ('TASAS_READ', 'TASAS_WRITE', 'TASAS_GESTIONAR');

-- Asignar permisos de lectura al rol USER (ID = 2) si existe
INSERT IGNORE INTO roles_permisos (rol_id, permiso_id)
SELECT 2, p.id FROM permisos p WHERE p.clave = 'TASAS_READ' AND EXISTS (SELECT 1 FROM roles WHERE id = 2);

-- Asignar permisos de escritura al rol GESTOR (ID = 3) si existe
INSERT IGNORE INTO roles_permisos (rol_id, permiso_id)
SELECT 3, p.id FROM permisos p WHERE p.clave IN ('TASAS_READ', 'TASAS_WRITE') AND EXISTS (SELECT 1 FROM roles WHERE id = 3);
