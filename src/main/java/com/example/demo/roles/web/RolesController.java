package com.example.demo.roles.web;

import com.example.demo.security.model.Permiso;
import com.example.demo.security.model.Rol;
import com.example.demo.security.repository.PermisoRepository;
import com.example.demo.security.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class RolesController {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;

    @GetMapping("/roles")
    @PreAuthorize("hasAnyAuthority('ROLES_READ','ROLES_VER','ROLES_GESTIONAR') or hasRole('ADMIN_TRIBUTARIO')")
    public String index(Model model) {
        List<Rol> roles = rolRepository.findAll();
        List<Permiso> permisos = permisoRepository.findAll();
        
        // Estadísticas
        long totalRoles = roles.size();
        long rolesActivos = roles.stream().filter(r -> !r.getPermisos().isEmpty()).count();
        long permisosUnicos = permisos.size();
        
        model.addAttribute("roles", roles);
        model.addAttribute("permisos", permisos);
        model.addAttribute("totalRoles", totalRoles);
        model.addAttribute("rolesActivos", rolesActivos);
        model.addAttribute("permisosUnicos", permisosUnicos);
        
        return "roles/index";
    }

    @PostMapping("/roles")
    @PreAuthorize("hasAnyAuthority('ROLES_WRITE','ROLES_GESTIONAR') or hasRole('ADMIN_TRIBUTARIO')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> crear(@RequestBody Map<String, Object> datos) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String nombre = (String) datos.get("nombre");
            String descripcion = (String) datos.get("descripcion");
            @SuppressWarnings("unchecked")
            List<Long> permisoIds = (List<Long>) datos.get("permisos");
            
            if (nombre == null || nombre.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "El nombre del rol es obligatorio");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Verificar si ya existe un rol con ese nombre
            if (rolRepository.findByNombre(nombre).isPresent()) {
                response.put("success", false);
                response.put("message", "Ya existe un rol con ese nombre");
                return ResponseEntity.badRequest().body(response);
            }
            
            Rol rol = new Rol();
            rol.setNombre(nombre.trim());
            rol.setDescripcion(descripcion != null ? descripcion.trim() : "");
            
            // Asignar permisos si se proporcionaron
            if (permisoIds != null && !permisoIds.isEmpty()) {
                Set<Permiso> permisos = Set.copyOf(permisoRepository.findAllById(permisoIds));
                rol.setPermisos(permisos);
            }
            
            Rol rolGuardado = rolRepository.save(rol);
            
            response.put("success", true);
            response.put("message", "Rol creado exitosamente");
            response.put("rol", Map.of(
                "id", rolGuardado.getId(),
                "nombre", rolGuardado.getNombre(),
                "descripcion", rolGuardado.getDescripcion(),
                "permisos", rolGuardado.getPermisos().size()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al crear el rol: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/roles/{id}")
    @PreAuthorize("hasAnyAuthority('ROLES_WRITE','ROLES_GESTIONAR') or hasRole('ADMIN_TRIBUTARIO')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> actualizar(@PathVariable Long id, @RequestBody Map<String, Object> datos) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Rol rol = rolRepository.findById(id).orElse(null);
            if (rol == null) {
                response.put("success", false);
                response.put("message", "Rol no encontrado");
                return ResponseEntity.notFound().build();
            }
            
            String nombre = (String) datos.get("nombre");
            String descripcion = (String) datos.get("descripcion");
            @SuppressWarnings("unchecked")
            List<Long> permisoIds = (List<Long>) datos.get("permisos");
            
            if (nombre == null || nombre.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "El nombre del rol es obligatorio");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Verificar si ya existe otro rol con ese nombre
            rolRepository.findByNombre(nombre).ifPresent(existente -> {
                if (!existente.getId().equals(id)) {
                    throw new RuntimeException("Ya existe otro rol con ese nombre");
                }
            });
            
            rol.setNombre(nombre.trim());
            rol.setDescripcion(descripcion != null ? descripcion.trim() : "");
            
            // Actualizar permisos
            if (permisoIds != null) {
                Set<Permiso> permisos = Set.copyOf(permisoRepository.findAllById(permisoIds));
                rol.setPermisos(permisos);
            }
            
            Rol rolActualizado = rolRepository.save(rol);
            
            response.put("success", true);
            response.put("message", "Rol actualizado exitosamente");
            response.put("rol", Map.of(
                "id", rolActualizado.getId(),
                "nombre", rolActualizado.getNombre(),
                "descripcion", rolActualizado.getDescripcion(),
                "permisos", rolActualizado.getPermisos().size()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al actualizar el rol: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @DeleteMapping("/roles/{id}")
    @PreAuthorize("hasAnyAuthority('ROLES_DELETE','ROLES_GESTIONAR') or hasRole('ADMIN_TRIBUTARIO')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Rol rol = rolRepository.findById(id).orElse(null);
            if (rol == null) {
                response.put("success", false);
                response.put("message", "Rol no encontrado");
                return ResponseEntity.notFound().build();
            }
            
            rolRepository.delete(rol);
            
            response.put("success", true);
            response.put("message", "Rol eliminado exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al eliminar el rol: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/roles/{id}")
    @PreAuthorize("hasAnyAuthority('ROLES_READ','ROLES_VER','ROLES_GESTIONAR') or hasRole('ADMIN_TRIBUTARIO')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtener(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Rol rol = rolRepository.findById(id).orElse(null);
            if (rol == null) {
                response.put("success", false);
                response.put("message", "Rol no encontrado");
                return ResponseEntity.notFound().build();
            }
            
            response.put("success", true);
            response.put("rol", Map.of(
                "id", rol.getId(),
                "nombre", rol.getNombre(),
                "descripcion", rol.getDescripcion(),
                "permisos", rol.getPermisos().stream()
                    .map(p -> Map.of("id", p.getId(), "nombre", p.getNombre()))
                    .toList()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al obtener el rol: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/roles/{id}/permisos")
    @PreAuthorize("hasAuthority('ROLES_GESTIONAR') or hasRole('ADMIN_TRIBUTARIO')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> actualizarPermisos(@PathVariable Long id, @RequestBody Map<String, Object> datos) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Rol rol = rolRepository.findById(id).orElse(null);
            if (rol == null) {
                response.put("success", false);
                response.put("message", "Rol no encontrado");
                return ResponseEntity.notFound().build();
            }
            
            @SuppressWarnings("unchecked")
            List<Long> permisoIds = (List<Long>) datos.get("permisos");
            
            // Actualizar permisos
            if (permisoIds != null) {
                Set<Permiso> permisos = Set.copyOf(permisoRepository.findAllById(permisoIds));
                rol.setPermisos(permisos);
            } else {
                rol.getPermisos().clear();
            }
            
            Rol rolActualizado = rolRepository.save(rol);
            
            response.put("success", true);
            response.put("message", "Permisos actualizados exitosamente");
            response.put("rol", Map.of(
                "id", rolActualizado.getId(),
                "nombre", rolActualizado.getNombre(),
                "permisos", rolActualizado.getPermisos().size()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al actualizar permisos: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}


