package com.example.demo.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador de prueba independiente
 */
@Controller
@RequestMapping("/test-tasas")
@Slf4j
public class TestController {

    @GetMapping
    public String testPage(Model model) {
        log.info("=== CONTROLADOR TEST INDEPENDIENTE ===");
        
        model.addAttribute("mensaje", "Controlador independiente funcionando");
        model.addAttribute("timestamp", System.currentTimeMillis());
        
        log.info("=== RETORNANDO TEMPLATE TEST ===");
        return "test-page";
    }
}
