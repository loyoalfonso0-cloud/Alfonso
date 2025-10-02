    -- Agregar permisos para el módulo de Multas

    -- Insertar permisos de Multas
    INSERT IGNORE INTO permisos (clave, nombre) VALUES
    ('MULTAS_READ', 'Ver multas'),
    ('MULTAS_WRITE', 'Crear y editar multas'),
    ('MULTAS_GESTIONAR', 'Gestión completa de multas (crear, editar, anular, procesar pagos)');

    -- Asignar permisos al rol ADMIN_TRIBUTARIO
    INSERT IGNORE INTO roles_permisos (rol_id, permiso_id)
    SELECT r.id, p.id
    FROM roles r, permisos p
    WHERE r.nombre = 'ADMIN_TRIBUTARIO'
    AND p.clave IN ('MULTAS_READ', 'MULTAS_WRITE', 'MULTAS_GESTIONAR');

    -- Asignar permisos al rol SUPER_ADMIN si existe
    INSERT IGNORE INTO roles_permisos (rol_id, permiso_id)
    SELECT r.id, p.id
    FROM roles r, permisos p
    WHERE r.nombre = 'SUPER_ADMIN'
    AND p.clave IN ('MULTAS_READ', 'MULTAS_WRITE', 'MULTAS_GESTIONAR');

    -- Asignar permisos básicos de lectura al rol USUARIO_TRIBUTARIO si existe
    INSERT IGNORE INTO roles_permisos (rol_id, permiso_id)
    SELECT r.id, p.id
    FROM roles r, permisos p
    WHERE r.nombre = 'USUARIO_TRIBUTARIO'
    AND p.clave = 'MULTAS_READ';
