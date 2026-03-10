package com.example.ejemplo_99.controllers;

import com.example.ejemplo_99.models.mongo.Doctor;
import com.example.ejemplo_99.services.DoctorService;
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
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/doctores")
    public String mostrarDoctores(Model model) {
        model.addAttribute("doctores", doctorService.findAll());
        // Establecer enlace activo para la navegación
        model.addAttribute("activeLink", "doctores");
        return "doctores";
    }

    @PostMapping("/admin/doctores/guardar")
    public String guardarDoctor(
            @RequestParam("nombre") String nombre,
            @RequestParam("apellido") String apellido,
            @RequestParam("especialidad") String especialidad,
            @RequestParam("correo") String correo,
            @RequestParam("telefono") String telefono,
            @RequestParam("imagenUrl") String imagenUrl,
            RedirectAttributes redirectAttributes) {

        Doctor doctor = new Doctor();
        doctor.setNombre(nombre);
        doctor.setApellido(apellido);
        doctor.setEspecialidad(especialidad);
        doctor.setCorreo(correo);
        doctor.setTelefono(telefono);
        doctor.setImagenUrl(imagenUrl);

        doctorService.save(doctor);

        redirectAttributes.addFlashAttribute("mensaje", "Doctor guardado con éxito");
        return "redirect:/admin";
    }

    @PostMapping("/admin/doctores/actualizar/{id}")
    public String actualizarDoctor(
            @PathVariable String id,
            @RequestParam("nombre") String nombre,
            @RequestParam("apellido") String apellido,
            @RequestParam("especialidad") String especialidad,
            @RequestParam("correo") String correo,
            @RequestParam("telefono") String telefono,
            @RequestParam("imagenUrl") String imagenUrl,
            RedirectAttributes redirectAttributes) {

        Optional<Doctor> doctorOpt = doctorService.findById(id);

        if (doctorOpt.isPresent()) {
            Doctor doctor = doctorOpt.get();
            doctor.setNombre(nombre);
            doctor.setApellido(apellido);
            doctor.setEspecialidad(especialidad);
            doctor.setCorreo(correo);
            doctor.setTelefono(telefono);
            doctor.setImagenUrl(imagenUrl);

            doctorService.save(doctor);
            redirectAttributes.addFlashAttribute("mensaje", "Doctor actualizado con éxito");
        } else {
            redirectAttributes.addFlashAttribute("error", "Doctor no encontrado");
        }

        return "redirect:/admin";
    }


}