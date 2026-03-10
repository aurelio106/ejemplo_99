package com.example.ejemplo_99.controllers;

import com.example.ejemplo_99.models.mongo.Servicio;
import com.example.ejemplo_99.services.ServicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class ServicioController {

    @Autowired
    private ServicioService servicioService;

    @GetMapping("/servicios")
    public String mostrarServicios(Model model) {
        model.addAttribute("servicios", servicioService.findAll());
        // Establecer enlace activo para la navegación
        model.addAttribute("activeLink", "servicios");
        return "servicios";
    }

    @PostMapping("/admin/servicios/guardar")
    public String guardarServicio(
            @RequestParam("nombre") String nombre,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("icono") String icono,
            @RequestParam(value = "duracionMinutos", defaultValue = "30") Integer duracionMinutos,
            RedirectAttributes redirectAttributes) {

        Servicio servicio = new Servicio();
        servicio.setNombre(nombre);
        servicio.setDescripcion(descripcion);
        servicio.setIcono(icono);
        servicio.setDuracionMinutos(duracionMinutos);

        servicioService.save(servicio);

        redirectAttributes.addFlashAttribute("mensaje", "Servicio guardado con éxito");
        return "redirect:/admin";
    }

    @PostMapping("/admin/servicios/actualizar/{id}")
    public String actualizarServicio(
            @PathVariable String id,
            @RequestParam("nombre") String nombre,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("icono") String icono,
            @RequestParam(value = "duracionMinutos", defaultValue = "30") Integer duracionMinutos,
            RedirectAttributes redirectAttributes) {

        Optional<Servicio> servicioOpt = servicioService.findById(id);

        if (servicioOpt.isPresent()) {
            Servicio servicio = servicioOpt.get();
            servicio.setNombre(nombre);
            servicio.setDescripcion(descripcion);
            servicio.setIcono(icono);
            servicio.setDuracionMinutos(duracionMinutos);

            servicioService.save(servicio);
            redirectAttributes.addFlashAttribute("mensaje", "Servicio actualizado con éxito");
        } else {
            redirectAttributes.addFlashAttribute("error", "Servicio no encontrado");
        }

        return "redirect:/admin";
    }


}