-- Eliminar logs de navegación y accesos a módulos existentes
DELETE FROM auditoria_logs WHERE accion IN ('VIEW', 'LOGIN', 'LOGOUT');

-- Eliminar logs con descripciones de acceso a módulos
DELETE FROM auditoria_logs WHERE descripcion LIKE '%accedió al módulo%';
DELETE FROM auditoria_logs WHERE descripcion LIKE '%ingresó al sistema%';
DELETE FROM auditoria_logs WHERE descripcion LIKE '%cerró sesión%';
