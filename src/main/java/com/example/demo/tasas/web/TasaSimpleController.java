package com.example.demo.tasas.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador simple de Tasas sin dependencias
 */
@Controller
@RequestMapping("/tasas-simple")
@Slf4j
public class TasaSimpleController {

    @GetMapping
    public String index(Model model) {
        log.info("=== ACCEDIENDO A TASAS SIMPLE ===");
        
        try {
            // Datos básicos sin dependencias
            model.addAttribute("totalTasas", 0);
            model.addAttribute("activasTasas", 0);
            model.addAttribute("pagadasTasas", 0);
            model.addAttribute("vencidasTasas", 0);
            model.addAttribute("mensaje", "Controlador simple funcionando");
            
            log.info("=== DATOS AGREGADOS AL MODELO ===");
            log.info("=== RENDERIZANDO TEMPLATE TASAS-SIMPLE ===");
            
            return "tasas-simple";
        } catch (Exception e) {
            log.error("=== ERROR EN CONTROLADOR SIMPLE ===", e);
            return "error";
        }
    }
}
