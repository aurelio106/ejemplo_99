package com.example.ejemplo_99.controllers;

import com.example.ejemplo_99.services.DoctorService;
import com.example.ejemplo_99.services.ServicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private ServicioService servicioService;

    @GetMapping("/inicio")
    public String mostrarInicio(Model model) {
        model.addAttribute("doctores", doctorService.findAll());
        model.addAttribute("servicios", servicioService.findAll());
        // Mostrar solo 3 doctores destacados
        model.addAttribute("doctoresDestacados", doctorService.findAll().stream().limit(3).toList());
        // Establecer enlace activo para la navegación
        model.addAttribute("activeLink", "inicio");
        return "inicio";
    }

    @GetMapping("/")
    public String principal(Model model) {
        model.addAttribute("doctores", doctorService.findAll());
        model.addAttribute("servicios", servicioService.findAll());
        // Mostrar solo 3 doctores destacados
        model.addAttribute("doctoresDestacados", doctorService.findAll().stream().limit(3).toList());
        // Establecer enlace activo para la navegación
        model.addAttribute("activeLink", "inicio");
        return "inicio";
    }

    // Eliminar este método que está causando el conflicto
    // @GetMapping("/login")
    // public String mostrarLogin() {
    //     return "login";
    // }
}