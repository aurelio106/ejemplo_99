package com.example.ejemplo_99.controllers;

import com.example.ejemplo_99.models.mongo.Cita;
import com.example.ejemplo_99.models.mongo.Doctor;
import com.example.ejemplo_99.models.mongo.Servicio;
import com.example.ejemplo_99.models.mysql.Usuario;
import com.example.ejemplo_99.repositories.mysql.UsuarioRepository;
import com.example.ejemplo_99.services.CitaService;
import com.example.ejemplo_99.services.DoctorService;
import com.example.ejemplo_99.services.ServicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CitaService citaService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private ServicioService servicioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Método para verificar si el usuario tiene un rol específico
    private boolean hasRole(Principal principal, String role) {
        if (principal == null) {
            return false;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(r -> r.equals(role));
    }

    // Página principal del panel de administración
    @GetMapping("")
    public String adminPanel(Model model, Principal principal) {
        // Verificar si el usuario tiene rol de administrador
        if (!hasRole(principal, "ROLE_ADMIN")) {
            return "redirect:/acceso-denegado";
        }

        // Obtener todas las citas
        List<Cita> citas = citaService.findAll();
        model.addAttribute("citas", citas);

        // Obtener todos los doctores
        List<Doctor> doctores = doctorService.findAll();
        model.addAttribute("doctores", doctores);

        // Obtener todos los servicios
        List<Servicio> servicios = servicioService.findAll();
        model.addAttribute("servicios", servicios);

        // Estadísticas generales
        Map<String, Long> estadisticasGenerales = new HashMap<>();
        estadisticasGenerales.put("totalCitas", (long) citas.size());
        estadisticasGenerales.put("citasProgramadas", citaService.countCitasByEstado("PROGRAMADA"));
        estadisticasGenerales.put("citasCompletadas", citaService.countCitasByEstado("COMPLETADA"));
        estadisticasGenerales.put("citasCanceladas", citaService.countCitasByEstado("CANCELADA"));
        model.addAttribute("estadisticasGenerales", estadisticasGenerales);

        // Obtener doctores más populares para el dashboard
        List<Map<String, Object>> doctoresPopulares = getDoctoresPopulares();
        model.addAttribute("doctoresPopulares", doctoresPopulares);

        // Obtener estadísticas mensuales para la nueva gráfica
        Map<String, Object> estadisticasMensuales = getEstadisticasMensuales();
        model.addAttribute("estadisticasMensuales", estadisticasMensuales);

        model.addAttribute("activeLink", "admin");
        return "admin";
    }

    // ==================== GESTIÓN DE CITAS ====================

    // Obtener todas las citas
    @GetMapping("/citas")
    @ResponseBody
    public List<Cita> getAllCitas() {
        return citaService.findAll();
    }

    // Obtener cita por ID
    @GetMapping("/citas/{id}")
    @ResponseBody
    public ResponseEntity<Cita> getCitaById(@PathVariable String id) {
        Optional<Cita> cita = citaService.findById(id);
        return cita.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Cambiar estado de una cita
    @PostMapping("/citas/{id}/estado")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cambiarEstadoCita(
            @PathVariable String id,
            @RequestParam String estado) {

        boolean resultado = citaService.cambiarEstadoCita(id, estado);
        Map<String, Object> response = new HashMap<>();

        if (resultado) {
            response.put("success", true);
            response.put("message", "Estado de la cita actualizado correctamente");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "No se pudo actualizar el estado de la cita");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Eliminar cita (Método POST para compatibilidad con formularios HTML)
    @PostMapping("/citas/eliminar/{id}")
    public String eliminarCitaPost(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            citaService.deleteById(id);
            redirectAttributes.addFlashAttribute("mensaje", "Cita eliminada correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la cita: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    // Eliminar cita (Método DELETE para API REST)
    @DeleteMapping("/citas/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteCita(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();

        try {
            citaService.deleteById(id);
            response.put("success", true);
            response.put("message", "Cita eliminada correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al eliminar la cita: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Buscar citas
    @GetMapping("/citas/buscar")
    @ResponseBody
    public List<Cita> buscarCitas(@RequestParam String termino) {
        return citaService.buscarCitas(termino);
    }

    // ==================== GESTIÓN DE DOCTORES ====================

    // Obtener todos los doctores
    @GetMapping("/doctores")
    @ResponseBody
    public List<Doctor> getAllDoctores() {
        return doctorService.findAll();
    }

    // Obtener doctor por ID
    @GetMapping("/doctores/{id}")
    @ResponseBody
    public ResponseEntity<Doctor> getDoctorById(@PathVariable String id) {
        Optional<Doctor> doctor = doctorService.findById(id);
        return doctor.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Crear nuevo doctor
    @PostMapping("/doctores")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createDoctor(@RequestBody Doctor doctor) {
        Map<String, Object> response = new HashMap<>();

        try {
            Doctor nuevoDoctor = doctorService.save(doctor);
            response.put("success", true);
            response.put("message", "Doctor creado correctamente");
            response.put("doctor", nuevoDoctor);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al crear el doctor: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Actualizar doctor
    @PutMapping("/doctores/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateDoctor(
            @PathVariable String id,
            @RequestBody Doctor doctor) {

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Doctor> doctorExistente = doctorService.findById(id);
            if (!doctorExistente.isPresent()) {
                response.put("success", false);
                response.put("message", "Doctor no encontrado");
                return ResponseEntity.notFound().build();
            }

            doctor.setId(id);
            Doctor doctorActualizado = doctorService.save(doctor);

            response.put("success", true);
            response.put("message", "Doctor actualizado correctamente");
            response.put("doctor", doctorActualizado);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al actualizar el doctor: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Eliminar doctor (Método POST para compatibilidad con formularios HTML)
    @PostMapping("/doctores/eliminar/{id}")
    public String eliminarDoctorPost(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            doctorService.deleteById(id);
            redirectAttributes.addFlashAttribute("mensaje", "Doctor eliminado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el doctor: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    // Eliminar doctor (Método DELETE para API REST)
    @DeleteMapping("/doctores/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteDoctor(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();

        try {
            doctorService.deleteById(id);
            response.put("success", true);
            response.put("message", "Doctor eliminado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al eliminar el doctor: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ==================== GESTIÓN DE SERVICIOS ====================

    // Obtener todos los servicios
    @GetMapping("/servicios")
    @ResponseBody
    public List<Servicio> getAllServicios() {
        return servicioService.findAll();
    }

    // Obtener servicio por ID
    @GetMapping("/servicios/{id}")
    @ResponseBody
    public ResponseEntity<Servicio> getServicioById(@PathVariable String id) {
        Optional<Servicio> servicio = servicioService.findById(id);
        return servicio.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Crear nuevo servicio
    @PostMapping("/servicios")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createServicio(@RequestBody Servicio servicio) {
        Map<String, Object> response = new HashMap<>();

        try {
            Servicio nuevoServicio = servicioService.save(servicio);
            response.put("success", true);
            response.put("message", "Servicio creado correctamente");
            response.put("servicio", nuevoServicio);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al crear el servicio: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Actualizar servicio
    @PutMapping("/servicios/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateServicio(
            @PathVariable String id,
            @RequestBody Servicio servicio) {

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Servicio> servicioExistente = servicioService.findById(id);
            if (!servicioExistente.isPresent()) {
                response.put("success", false);
                response.put("message", "Servicio no encontrado");
                return ResponseEntity.notFound().build();
            }

            servicio.setId(id);
            Servicio servicioActualizado = servicioService.save(servicio);

            response.put("success", true);
            response.put("message", "Servicio actualizado correctamente");
            response.put("servicio", servicioActualizado);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al actualizar el servicio: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Eliminar servicio (Método POST para compatibilidad con formularios HTML)
    @PostMapping("/servicios/eliminar/{id}")
    public String eliminarServicioPost(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            servicioService.deleteById(id);
            redirectAttributes.addFlashAttribute("mensaje", "Servicio eliminado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el servicio: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    // Eliminar servicio (Método DELETE para API REST)
    @DeleteMapping("/servicios/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteServicio(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();

        try {
            servicioService.deleteById(id);
            response.put("success", true);
            response.put("message", "Servicio eliminado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al eliminar el servicio: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ==================== DASHBOARD Y ESTADÍSTICAS ====================

    // Obtener datos para el dashboard
    @GetMapping("/dashboard/datos")
    @ResponseBody
    public Map<String, Object> getDatosDashboard() {
        Map<String, Object> datos = new HashMap<>();

        // Estadísticas generales
        long totalCitas = citaService.findAll().size();
        long citasProgramadas = citaService.countCitasByEstado("PROGRAMADA");
        long citasCompletadas = citaService.countCitasByEstado("COMPLETADA");
        long citasCanceladas = citaService.countCitasByEstado("CANCELADA");

        Map<String, Long> estadisticas = new HashMap<>();
        estadisticas.put("totalCitas", totalCitas);
        estadisticas.put("citasProgramadas", citasProgramadas);
        estadisticas.put("citasCompletadas", citasCompletadas);
        estadisticas.put("citasCanceladas", citasCanceladas);

        datos.put("estadisticas", estadisticas);

        // Doctores más populares
        List<Map<String, Object>> doctoresPopulares = getDoctoresPopulares();
        datos.put("doctoresPopulares", doctoresPopulares);

        // Estadísticas mensuales para la nueva gráfica
        Map<String, Object> estadisticasMensuales = getEstadisticasMensuales();
        datos.put("estadisticasMensuales", estadisticasMensuales);

        return datos;
    }

    // Método para obtener doctores más populares
    private List<Map<String, Object>> getDoctoresPopulares() {
        // Obtener todas las citas
        List<Cita> citas = citaService.findAll();

        // Contar citas por doctor
        Map<String, Map<String, Object>> conteoDoctores = new HashMap<>();

        // Colores predefinidos para los doctores
        String[] colores = {
                "#4CAF50", "#2196F3", "#FFC107", "#E91E63", "#9C27B0",
                "#FF5722", "#607D8B", "#795548", "#3F51B5", "#009688"
        };

        int colorIndex = 0;

        // Primero, obtener todos los doctores para asignarles colores
        List<Doctor> doctores = doctorService.findAll();
        for (Doctor doctor : doctores) {
            String nombreDoctor = "Dr. " + doctor.getNombre() + " " + doctor.getApellido();
            Map<String, Object> infoDoctor = new HashMap<>();
            infoDoctor.put("nombre", nombreDoctor);
            infoDoctor.put("cantidad", 0L);
            infoDoctor.put("color", colores[colorIndex % colores.length]);
            colorIndex++;
            conteoDoctores.put(doctor.getId(), infoDoctor);
        }

        // Ahora contar las citas para cada doctor
        for (Cita cita : citas) {
            if (cita.getDoctorId() != null && conteoDoctores.containsKey(cita.getDoctorId())) {
                Map<String, Object> infoDoctor = conteoDoctores.get(cita.getDoctorId());
                long cantidadActual = (long) infoDoctor.get("cantidad");
                infoDoctor.put("cantidad", cantidadActual + 1);
            }
        }

        // Convertir a lista de mapas para la respuesta
        List<Map<String, Object>> resultado = new ArrayList<>(conteoDoctores.values());

        // Calcular porcentajes
        if (!citas.isEmpty()) {
            for (Map<String, Object> doctor : resultado) {
                long cantidad = (long) doctor.get("cantidad");
                double porcentaje = (double) cantidad / citas.size() * 100;
                doctor.put("porcentaje", Math.round(porcentaje));
            }
        }

        // Ordenar por cantidad (descendente)
        resultado.sort((a, b) -> Long.compare(
                (Long) b.get("cantidad"),
                (Long) a.get("cantidad")
        ));

        return resultado;
    }

    // Método para obtener estadísticas mensuales (nueva gráfica)
    private Map<String, Object> getEstadisticasMensuales() {
        Map<String, Object> resultado = new HashMap<>();

        // Obtener todas las citas
        List<Cita> citas = citaService.findAll();

        // Obtener el año actual
        int añoActual = LocalDate.now().getYear();

        // Crear mapa para almacenar citas por mes
        Map<Month, Integer> citasPorMes = new HashMap<>();
        for (Month mes : Month.values()) {
            citasPorMes.put(mes, 0);
        }

        // Contar citas por mes para el año actual
        for (Cita cita : citas) {
            if (cita.getFecha() != null && cita.getFecha().getYear() == añoActual) {
                Month mes = cita.getFecha().getMonth();
                citasPorMes.put(mes, citasPorMes.get(mes) + 1);
            }
        }

        // Convertir a formato para la gráfica
        List<String> meses = new ArrayList<>();
        List<Integer> valores = new ArrayList<>();

        for (Month mes : Month.values()) {
            // Obtener nombre del mes en español
            String nombreMes = mes.toString();
            switch (mes) {
                case JANUARY: nombreMes = "Enero"; break;
                case FEBRUARY: nombreMes = "Febrero"; break;
                case MARCH: nombreMes = "Marzo"; break;
                case APRIL: nombreMes = "Abril"; break;
                case MAY: nombreMes = "Mayo"; break;
                case JUNE: nombreMes = "Junio"; break;
                case JULY: nombreMes = "Julio"; break;
                case AUGUST: nombreMes = "Agosto"; break;
                case SEPTEMBER: nombreMes = "Septiembre"; break;
                case OCTOBER: nombreMes = "Octubre"; break;
                case NOVEMBER: nombreMes = "Noviembre"; break;
                case DECEMBER: nombreMes = "Diciembre"; break;
            }

            meses.add(nombreMes);
            valores.add(citasPorMes.get(mes));
        }

        resultado.put("meses", meses);
        resultado.put("valores", valores);
        resultado.put("año", añoActual);

        return resultado;
    }

    // ==================== USUARIOS ====================

    // Obtener todos los usuarios
    @GetMapping("/usuarios")
    @ResponseBody
    public List<Map<String, Object>> getAllUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();

        return usuarios.stream().map(usuario -> {
            Map<String, Object> usuarioMap = new HashMap<>();
            usuarioMap.put("id", usuario.getId());
            usuarioMap.put("username", usuario.getUsername());
            usuarioMap.put("email", usuario.getEmail());
            usuarioMap.put("enabled", usuario.isEnabled());
            usuarioMap.put("roles", usuario.getRoles());
            return usuarioMap;
        }).collect(Collectors.toList());
    }

    // Cambiar estado de un usuario (activar/desactivar)
    @PostMapping("/usuarios/{id}/cambiar-estado")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cambiarEstadoUsuario(
            @PathVariable Long id,
            @RequestParam boolean enabled) {

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
            if (!usuarioOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Usuario no encontrado");
                return ResponseEntity.notFound().build();
            }

            Usuario usuario = usuarioOpt.get();
            usuario.setEnabled(enabled);
            usuarioRepository.save(usuario);

            response.put("success", true);
            response.put("message", "Estado del usuario actualizado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al actualizar el estado del usuario: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
