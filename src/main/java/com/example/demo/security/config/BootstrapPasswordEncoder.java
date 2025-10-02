package com.example.demo.security.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BootstrapPasswordEncoder {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void encodeSuperAdminPasswordIfNeeded() {
        String raw = "SuperAdmin#2025";
        String username = "superadmin";
        try {
            String current = jdbcTemplate.queryForObject("SELECT password FROM usuarios WHERE username=?", String.class, username);
            if (current != null && current.startsWith("{noop}")) {
                String encoded = passwordEncoder.encode(raw);
                jdbcTemplate.update("UPDATE usuarios SET password=? WHERE username=?", encoded, username);
            }
        } catch (Exception ignored) {
            // Tabla o usuario aún no existen en arranque temprano
        }
    }
}


