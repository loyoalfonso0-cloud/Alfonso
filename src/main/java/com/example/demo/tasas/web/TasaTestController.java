package com.example.demo.tasas.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador de prueba para Tasas Municipales
 */
@Controller
@RequestMapping("/tasas-test")
@Slf4j
public class TasaTestController {

    @GetMapping
    public String index(Model model) {
        log.info("=== ACCEDIENDO A TASAS TEST ===");
        
        model.addAttribute("mensaje", "Controlador de prueba funcionando");
        model.addAttribute("totalTasas", 0);
        
        log.info("=== RENDERIZANDO TEMPLATE ===");
        return "tasas/test";
    }
}
