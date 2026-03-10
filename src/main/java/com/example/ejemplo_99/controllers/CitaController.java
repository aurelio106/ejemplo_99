package com.example.ejemplo_99.controllers;

import com.example.ejemplo_99.models.mongo.Cita;
import com.example.ejemplo_99.models.mongo.Servicio;
import com.example.ejemplo_99.models.mysql.Usuario;
import com.example.ejemplo_99.services.CitaService;
import com.example.ejemplo_99.services.DoctorService;
import com.example.ejemplo_99.services.ServicioService;
import com.example.ejemplo_99.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class CitaController {

    @Autowired
    private CitaService citaService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private ServicioService servicioService;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Vista de citas del usuario autenticado
     */
    @GetMapping("/mis-citas")
    public String misCitas(Model model) {
        try {
            // Obtener el usuario autenticado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            // Buscar el usuario en la base de datos
            Optional<Usuario> usuarioOpt = usuarioService.findByUsername(username);

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                // Obtener las citas del usuario
                List<Cita> citas = citaService.findByUsuarioId(usuario.getId());
                model.addAttribute("citas", citas);
            } else {
                model.addAttribute("citas", new ArrayList<>());
            }

            // Establecer enlace activo para la navegación
            model.addAttribute("activeLink", "mis-citas");

            return "mis-citas";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar las citas: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Vista del formulario de reserva de citas
     */
    @GetMapping("/reservar")
    public String mostrarFormularioReserva(Model model) {
        try {
            model.addAttribute("doctores", doctorService.findAll());
            model.addAttribute("servicios", servicioService.findAll());

            // Generar lista de horarios disponibles para hoy
            model.addAttribute("horariosDisponibles", citaService.obtenerHorariosDisponiblesString(LocalDate.now()));

            // Establecer fecha mínima como hoy
            LocalDate hoy = LocalDate.now();
            model.addAttribute("fechaMinima", hoy);

            // Establecer enlace activo para la navegación
            model.addAttribute("activeLink", "reservar");

            return "reservar";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar el formulario de reserva: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Obtener los horarios disponibles según la fecha y el servicio seleccionado
     */
    @GetMapping("/horarios-disponibles")
    @ResponseBody
    public List<String> obtenerHorariosDisponibles(
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        try {
            return (List<String>) citaService.obtenerHorariosDisponiblesString(fecha);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Obtener información de un servicio específico
     */
    @GetMapping("/servicio-info/{id}")
    @ResponseBody
    public ResponseEntity<?> obtenerInfoServicio(@PathVariable String id) {
        try {
            Optional<Servicio> servicioOpt = servicioService.findById(id);
            if (servicioOpt.isPresent()) {
                Servicio servicio = servicioOpt.get();
                Map<String, Object> response = Map.of(
                        "duracionMinutos", servicio.getDuracionMinutos(),
                        "nombre", servicio.getNombre(),
                        "descripcion", servicio.getDescripcion()
                );
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Reservar una nueva cita
     */
    @PostMapping("/reservarCita")
    public String reservarCita(
            @RequestParam("nombre") String nombre,
            @RequestParam("apellido") String apellido,
            @RequestParam("direccion") String direccion,
            @RequestParam("telefono") String telefono,
            @RequestParam("correo") String correo,
            @RequestParam("cedula") String cedula,
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam("hora") String horaStr,
            @RequestParam(value = "doctorId", required = false) String doctorId,
            @RequestParam(value = "servicioId", required = false) String servicioId,
            RedirectAttributes redirectAttributes) {

        try {
            // Convertir la hora de String a LocalTime
            LocalTime hora = LocalTime.parse(horaStr);

            // Verificar si la hora está disponible
            boolean horaDisponible = true;
            List<Map<String, Object>> horarios = citaService.obtenerHorariosDisponibles(fecha, servicioId);
            for (Map<String, Object> horario : horarios) {
                if (horario.get("hora").equals(hora) && !(Boolean)horario.get("disponible")) {
                    horaDisponible = false;
                    break;
                }
            }

            if (!horaDisponible) {
                redirectAttributes.addFlashAttribute("error", "La hora seleccionada ya no está disponible. Por favor, elige otra hora.");
                return "redirect:/reservar";
            }

            // Obtener el usuario autenticado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            // Crear la cita
            citaService.crearCita(nombre, apellido, direccion, telefono, correo, cedula,
                    fecha, hora, doctorId, servicioId, username);

            redirectAttributes.addFlashAttribute("mensaje", "Cita reservada con éxito");
            return "redirect:/mis-citas";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al reservar la cita: " + e.getMessage());
            return "redirect:/reservar";
        }
    }

    /**
     * Eliminar una cita
     */
    @PostMapping("/citas/eliminar/{id}")
    public String eliminarCita(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            citaService.deleteById(id);
            redirectAttributes.addFlashAttribute("mensaje", "Cita eliminada con éxito");
            return "redirect:/mis-citas";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la cita: " + e.getMessage());
            return "redirect:/mis-citas";
        }
    }

    /**
     * Cancelar una cita (cambiar estado a CANCELADA sin eliminarla)
     */
    @PostMapping("/citas/{id}/cancelar")
    @ResponseBody
    public ResponseEntity<?> cancelarCita(@PathVariable String id) {
        try {
            // Buscar la cita por ID
            Optional<Cita> citaOpt = citaService.findById(id);

            if (!citaOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Cita cita = citaOpt.get();

            // Obtener el usuario autenticado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            // Verificar que la cita pertenezca al usuario actual
            Optional<Usuario> usuarioOpt = usuarioService.findByUsername(username);
            if (!usuarioOpt.isPresent() || !cita.getUsuarioId().equals(usuarioOpt.get().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "success", false,
                        "message", "No tienes permiso para cancelar esta cita"
                ));
            }

            // Verificar que la cita esté en estado PROGRAMADA
            if (cita.getEstado() != null && !"PROGRAMADA".equals(cita.getEstado())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Solo se pueden cancelar citas programadas"
                ));
            }

            // Cambiar estado a CANCELADA
            cita.setEstado("CANCELADA");
            citaService.save(cita);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cita cancelada correctamente");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Error al cancelar la cita: " + e.getMessage()
            ));
        }
    }
}