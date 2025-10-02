package com.example.demo.config;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.exception.FlywayValidateException;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            try {
                // Configurar para permitir migraciones fuera de orden
                Flyway.configure()
                    .dataSource(flyway.getConfiguration().getDataSource())
                    .outOfOrder(true)
                    .load()
                    .migrate();
            } catch (FlywayValidateException ex) {
                // Repara historial y vuelve a migrar si hay una validación fallida previa
                flyway.repair();
                Flyway.configure()
                    .dataSource(flyway.getConfiguration().getDataSource())
                    .outOfOrder(true)
                    .load()
                    .migrate();
            }
        };
    }
}


