package com.example.demo.personal.service;

import com.example.demo.personal.model.Personal;
import com.example.demo.personal.repository.PersonalRepository;
import com.example.demo.security.model.Rol;
import com.example.demo.security.model.Usuario;
import com.example.demo.security.repository.RolRepository;
import com.example.demo.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PersonalService {

    private final PersonalRepository personalRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    // Obtener todos los empleados
    public List<Personal> obtenerTodos() {
        return personalRepository.findAll();
    }

    // Obtener empleado por ID
    public Optional<Personal> obtenerPorId(Long id) {
        return personalRepository.findById(id);
    }

    // Obtener empleados activos
    public List<Personal> obtenerActivos() {
        return personalRepository.findByActivoTrue();
    }

    // Buscar empleados por término
    public List<Personal> buscarPorTermino(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return obtenerTodos();
        }
        return personalRepository.buscarPorTermino(termino.trim());
    }

    // Crear nuevo empleado
    public Personal crear(Personal personal, String emailUsuario, String password) {
        return crear(personal, emailUsuario, password, null);
    }

    // Crear nuevo empleado con rol
    public Personal crear(Personal personal, String emailUsuario, String password, Long rolId) {
        // Validar que no exista el documento
        if (personalRepository.existeDocumento(personal.getDocumento(), null)) {
            throw new RuntimeException("Ya existe un empleado con el documento: " + personal.getDocumento());
        }

        // Guardar primero el empleado
        personal = personalRepository.save(personal);

        // Si se asigna un rol, crear automáticamente el usuario del sistema
        if (rolId != null) {
            Rol rol = rolRepository.findById(rolId)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + rolId));

            // Generar email automático si no se proporciona
            if (emailUsuario == null || emailUsuario.trim().isEmpty()) {
                emailUsuario = generarEmailAutomatico(personal);
            }

            // Generar contraseña automática si no se proporciona
            if (password == null || password.trim().isEmpty()) {
                password = generarPasswordAutomatico();
            }

            // Verificar que no exista el email
            if (usuarioRepository.findByEmail(emailUsuario).isPresent()) {
                throw new RuntimeException("Ya existe un usuario con el email: " + emailUsuario);
            }

            Usuario usuario = Usuario.builder()
                    .username(emailUsuario)
                    .email(emailUsuario)
                    .password(passwordEncoder.encode(password))
                    .activo(true)
                    .build();
            
            // Asignar rol al usuario antes de guardarlo
            usuario.getRoles().add(rol);
            usuario = usuarioRepository.save(usuario);
            
            // Asociar usuario al empleado
            personal.setUsuario(usuario);
            personal = personalRepository.save(personal);
            
        } else if (emailUsuario != null && !emailUsuario.trim().isEmpty() && password != null && !password.trim().isEmpty()) {
            // Crear usuario sin rol si se proporcionan credenciales manualmente
            if (usuarioRepository.findByEmail(emailUsuario).isPresent()) {
                throw new RuntimeException("Ya existe un usuario con el email: " + emailUsuario);
            }

            Usuario usuario = Usuario.builder()
                    .username(emailUsuario)
                    .email(emailUsuario)
                    .password(passwordEncoder.encode(password))
                    .activo(true)
                    .build();
            
            usuario = usuarioRepository.save(usuario);
            personal.setUsuario(usuario);
            personal = personalRepository.save(personal);
        }

        return personal;
    }

    // Generar email automático basado en el nombre del empleado
    private String generarEmailAutomatico(Personal personal) {
        String nombres = personal.getNombres().toLowerCase().replaceAll("[^a-z]", "");
        String apellidos = personal.getApellidos().toLowerCase().replaceAll("[^a-z]", "");
        
        // Tomar primera parte del nombre y primer apellido
        String primerNombre = nombres.split("\\s+")[0];
        String primerApellido = apellidos.split("\\s+")[0];
        
        String baseEmail = primerNombre + "." + primerApellido + "@empresa.com";
        
        // Verificar si ya existe, si es así agregar número
        String emailFinal = baseEmail;
        int contador = 1;
        while (usuarioRepository.findByEmail(emailFinal).isPresent()) {
            emailFinal = primerNombre + "." + primerApellido + contador + "@empresa.com";
            contador++;
        }
        
        return emailFinal;
    }

    // Generar contraseña automática
    private String generarPasswordAutomatico() {
        // Generar contraseña con documento + año actual
        return "Empresa2025!";
    }

    // Actualizar empleado
    public Personal actualizar(Long id, Personal personalActualizado) {
        Personal personalExistente = personalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + id));

        // Validar documento único (excluyendo el actual)
        if (!personalExistente.getDocumento().equals(personalActualizado.getDocumento()) &&
            personalRepository.existeDocumento(personalActualizado.getDocumento(), id)) {
            throw new RuntimeException("Ya existe un empleado con el documento: " + personalActualizado.getDocumento());
        }

        // Actualizar campos
        personalExistente.setNombres(personalActualizado.getNombres());
        personalExistente.setApellidos(personalActualizado.getApellidos());
        personalExistente.setDocumento(personalActualizado.getDocumento());
        personalExistente.setTipoDocumento(personalActualizado.getTipoDocumento());
        personalExistente.setEmail(personalActualizado.getEmail());
        personalExistente.setTelefono(personalActualizado.getTelefono());
        personalExistente.setDireccion(personalActualizado.getDireccion());
        personalExistente.setCargo(personalActualizado.getCargo());
        personalExistente.setDepartamento(personalActualizado.getDepartamento());
        personalExistente.setFechaIngreso(personalActualizado.getFechaIngreso());
        personalExistente.setSalario(personalActualizado.getSalario());
        personalExistente.setObservaciones(personalActualizado.getObservaciones());
        personalExistente.setActivo(personalActualizado.isActivo());

        return personalRepository.save(personalExistente);
    }

    // Cambiar estado del empleado
    public Personal cambiarEstado(Long id, boolean activo) {
        Personal personal = personalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + id));

        personal.setActivo(activo);
        
        // Si tiene usuario asociado, también cambiar su estado
        if (personal.getUsuario() != null) {
            personal.getUsuario().setActivo(activo);
            usuarioRepository.save(personal.getUsuario());
        }

        return personalRepository.save(personal);
    }

    // Eliminar empleado
    public void eliminar(Long id) {
        Personal personal = personalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + id));

        // Si tiene usuario asociado, también eliminarlo
        if (personal.getUsuario() != null) {
            usuarioRepository.delete(personal.getUsuario());
        }

        personalRepository.delete(personal);
    }

    // Obtener estadísticas
    public Map<String, Object> obtenerEstadisticas() {
        long totalEmpleados = personalRepository.count();
        long empleadosActivos = personalRepository.countByActivoTrue();
        long empleadosInactivos = totalEmpleados - empleadosActivos;

        return Map.of(
                "total", totalEmpleados,
                "activos", empleadosActivos,
                "inactivos", empleadosInactivos
        );
    }

    // Verificar si existe documento
    public boolean existeDocumento(String documento, Long excludeId) {
        return personalRepository.existeDocumento(documento, excludeId);
    }

    // Obtener roles disponibles
    public List<Rol> obtenerRoles() {
        return rolRepository.findAll();
    }

    // Asignar rol a empleado existente
    public Personal asignarRol(Long personalId, Long rolId) {
        Personal personal = personalRepository.findById(personalId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + personalId));

        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + rolId));

        // Si el empleado ya tiene usuario, actualizar sus roles
        if (personal.getUsuario() != null) {
            Usuario usuario = personal.getUsuario();
            // Limpiar roles existentes y agregar el nuevo
            usuario.getRoles().clear();
            usuario.getRoles().add(rol);
            usuarioRepository.save(usuario);
        } else {
            // Si no tiene usuario, crear uno automáticamente
            String emailUsuario = generarEmailAutomatico(personal);
            String password = generarPasswordAutomatico();

            // Verificar que no exista el email
            if (usuarioRepository.findByEmail(emailUsuario).isPresent()) {
                throw new RuntimeException("Ya existe un usuario con el email: " + emailUsuario);
            }

            Usuario usuario = Usuario.builder()
                    .username(emailUsuario)
                    .email(emailUsuario)
                    .password(passwordEncoder.encode(password))
                    .activo(true)
                    .build();
            
            // Asignar rol al usuario
            usuario.getRoles().add(rol);
            usuario = usuarioRepository.save(usuario);
            
            // Asociar usuario al empleado
            personal.setUsuario(usuario);
            personal = personalRepository.save(personal);
        }

        return personal;
    }

    // Remover rol de empleado
    public Personal removerRol(Long personalId) {
        Personal personal = personalRepository.findById(personalId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + personalId));

        if (personal.getUsuario() != null) {
            Usuario usuario = personal.getUsuario();
            usuario.getRoles().clear();
            usuarioRepository.save(usuario);
        }

        return personal;
    }
}
