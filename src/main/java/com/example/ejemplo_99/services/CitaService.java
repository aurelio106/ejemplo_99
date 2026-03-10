package com.example.ejemplo_99.services;

import com.example.ejemplo_99.models.mongo.Cita;
import com.example.ejemplo_99.models.mongo.Doctor;
import com.example.ejemplo_99.models.mongo.Servicio;
import com.example.ejemplo_99.models.mysql.Usuario;
import com.example.ejemplo_99.repositories.mongo.CitaRepository;
import com.example.ejemplo_99.repositories.mongo.DoctorRepository;
import com.example.ejemplo_99.repositories.mongo.ServicioRepository;
import com.example.ejemplo_99.repositories.mysql.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Métodos básicos CRUD
    public List<Cita> findAll() {
        return citaRepository.findAll();
    }

    public Optional<Cita> findById(String id) {
        return citaRepository.findById(id);
    }

    public List<Cita> findByUsuarioId(Long usuarioId) {
        return citaRepository.findByUsuarioId(usuarioId);
    }

    public List<Cita> findByFecha(LocalDate fecha) {
        return citaRepository.findByFechaOrderByHora(fecha);
    }

    public List<Cita> findByFechaAndEstado(LocalDate fecha, String estado) {
        return citaRepository.findByFechaAndEstado(fecha, estado);
    }

    public List<Cita> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin) {
        return citaRepository.findByFechaBetweenOrderByFechaAscHoraAsc(fechaInicio, fechaFin);
    }

    public List<Cita> findByEstado(String estado) {
        return citaRepository.findByEstado(estado);
    }

    public Cita save(Cita cita) {
        // Verificar si se especificó un doctor
        if (cita.getDoctorId() != null && !cita.getDoctorId().isEmpty()) {
            Optional<Doctor> doctor = doctorRepository.findById(cita.getDoctorId());
            if (doctor.isPresent()) {
                cita.setDoctorNombre(doctor.get().getNombre());
                cita.setDoctorApellido(doctor.get().getApellido());
            }
        }

        // Verificar si se especificó un servicio
        if (cita.getServicioId() != null && !cita.getServicioId().isEmpty()) {
            Optional<Servicio> servicio = servicioRepository.findById(cita.getServicioId());
            if (servicio.isPresent()) {
                cita.setServicioNombre(servicio.get().getNombre());
            }
        } else {
            // Si no se especificó un servicio, asignar "Consulta por Medicina General" por defecto
            cita.setServicioNombre("Consulta por Medicina General");
        }

        // Verificar si se especificó un usuario
        if (cita.getUsuarioId() != null) {
            Optional<Usuario> usuario = usuarioRepository.findById(cita.getUsuarioId());
            if (usuario.isPresent()) {
                cita.setUsuarioUsername(usuario.get().getUsername());
            }
        }

        // Establecer estado por defecto si no se especificó
        if (cita.getEstado() == null || cita.getEstado().isEmpty()) {
            cita.setEstado("PROGRAMADA");
        }

        return citaRepository.save(cita);
    }

    public void deleteById(String id) {
        citaRepository.deleteById(id);
    }

    // Método para cambiar el estado de una cita
    public boolean cambiarEstadoCita(String id, String estado) {
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isPresent()) {
            Cita cita = citaOpt.get();
            cita.setEstado(estado);
            citaRepository.save(cita);
            return true;
        }
        return false;
    }

    // Método para obtener el usuario (corregido)
    public String getUsuario(Cita cita) {
        if (cita == null) {
            return "No disponible";
        }

        if (cita.getUsuarioUsername() != null && !cita.getUsuarioUsername().isEmpty()) {
            return cita.getUsuarioUsername();
        }

        if (cita.getUsuarioId() != null) {
            Optional<Usuario> usuario = usuarioRepository.findById(cita.getUsuarioId());
            if (usuario.isPresent()) {
                return usuario.get().getUsername();
            }
        }

        return "No asignado";
    }

    // Métodos para estadísticas
    public long countCitasByEstado(String estado) {
        return citaRepository.countByEstado(estado);
    }

    public long countCitasByFecha(LocalDate fecha) {
        return citaRepository.countByFecha(fecha);
    }

    public long countCitasByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin) {
        return citaRepository.countByFechaBetween(fechaInicio, fechaFin);
    }

    // Método para obtener todas las citas de hoy para todos los usuarios
    public List<Cita> findCitasHoy() {
        LocalDate hoy = LocalDate.now();
        return citaRepository.findByFechaOrderByHora(hoy);
    }

    // Método para obtener todas las citas de la semana para todos los usuarios
    public List<Cita> findCitasSemana() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioSemana = hoy.minusDays(hoy.getDayOfWeek().getValue() - 1);
        LocalDate finSemana = inicioSemana.plusDays(6);
        return citaRepository.findByFechaBetweenOrderByFechaAscHoraAsc(inicioSemana, finSemana);
    }

    // Método para obtener todas las citas del mes para todos los usuarios
    public List<Cita> findCitasMes() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());
        return citaRepository.findByFechaBetweenOrderByFechaAscHoraAsc(inicioMes, finMes);
    }

    // Método para obtener citas agrupadas por día de la semana
    public Map<String, Object> getCitasPorDiaSemana() {
        List<Cita> todasLasCitas = citaRepository.findAll();

        // Crear mapa para agrupar citas por día de la semana
        Map<String, List<Cita>> citasPorDia = new HashMap<>();
        citasPorDia.put("Lunes", new ArrayList<>());
        citasPorDia.put("Martes", new ArrayList<>());
        citasPorDia.put("Miércoles", new ArrayList<>());
        citasPorDia.put("Jueves", new ArrayList<>());
        citasPorDia.put("Viernes", new ArrayList<>());
        citasPorDia.put("Sábado", new ArrayList<>());
        citasPorDia.put("Domingo", new ArrayList<>());

        // Agrupar citas por día de la semana
        for (Cita cita : todasLasCitas) {
            if (cita.getFecha() != null) {
                DayOfWeek diaSemana = cita.getFecha().getDayOfWeek();
                String nombreDia = diaSemana.getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
                nombreDia = nombreDia.substring(0, 1).toUpperCase() + nombreDia.substring(1);

                List<Cita> citasDelDia = citasPorDia.get(nombreDia);
                if (citasDelDia != null) {
                    citasDelDia.add(cita);
                }
            }
        }

        // Crear resultado con conteo y citas por día
        Map<String, Object> resultado = new HashMap<>();
        Map<String, Integer> conteo = new HashMap<>();
        Map<String, List<Map<String, String>>> citasDetalle = new HashMap<>();

        for (Map.Entry<String, List<Cita>> entry : citasPorDia.entrySet()) {
            String dia = entry.getKey();
            List<Cita> citas = entry.getValue();

            // Agregar conteo
            conteo.put(dia, citas.size());

            // Agregar detalles de citas
            List<Map<String, String>> detalles = new ArrayList<>();
            for (Cita cita : citas) {
                Map<String, String> detalle = new HashMap<>();
                detalle.put("id", cita.getId());
                detalle.put("paciente", cita.getNombre() + " " + cita.getApellido());
                detalle.put("hora", cita.getHora() != null ? cita.getHora().toString() : "");
                detalle.put("doctor", cita.getDoctorNombre() != null ? "Dr. " + cita.getDoctorNombre() + " " + cita.getDoctorApellido() : "No asignado");
                detalle.put("servicio", cita.getServicioNombre() != null ? cita.getServicioNombre() : "Consulta General");

                detalles.add(detalle);
            }
            citasDetalle.put(dia, detalles);
        }

        resultado.put("conteo", conteo);
        resultado.put("citas", citasDetalle);

        return resultado;
    }

    // Método para buscar citas por término
    public List<Cita> buscarCitas(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return findAll();
        }

        String terminoLower = termino.toLowerCase();

        return citaRepository.findAll().stream()
                .filter(cita ->
                        (cita.getNombre() != null && cita.getNombre().toLowerCase().contains(terminoLower)) ||
                                (cita.getApellido() != null && cita.getApellido().toLowerCase().contains(terminoLower)) ||
                                (cita.getCedula() != null && cita.getCedula().toLowerCase().contains(terminoLower)) ||
                                (cita.getCorreo() != null && cita.getCorreo().toLowerCase().contains(terminoLower)) ||
                                (cita.getTelefono() != null && cita.getTelefono().toLowerCase().contains(terminoLower)) ||
                                (cita.getDoctorNombre() != null && cita.getDoctorNombre().toLowerCase().contains(terminoLower)) ||
                                (cita.getServicioNombre() != null && cita.getServicioNombre().toLowerCase().contains(terminoLower))
                )
                .collect(Collectors.toList());
    }

    // Método para obtener horarios disponibles como strings
    public List<String> obtenerHorariosDisponiblesString(LocalDate fecha) {
        // Definir horarios de atención (de 7:00 AM a 7:00 PM, cada 30 minutos)
        List<String> todosLosHorarios = new ArrayList<>();
        LocalTime horaInicio = LocalTime.of(7, 0);
        LocalTime horaFin = LocalTime.of(19, 0);

        LocalTime horaActual = horaInicio;
        while (!horaActual.isAfter(horaFin)) {
            todosLosHorarios.add(horaActual.toString());
            horaActual = horaActual.plusMinutes(30);
        }

        // Si la fecha es hoy, filtrar horarios pasados
        if (fecha.equals(LocalDate.now())) {
            LocalTime ahora = LocalTime.now();
            todosLosHorarios = todosLosHorarios.stream()
                    .filter(hora -> LocalTime.parse(hora).isAfter(ahora))
                    .collect(Collectors.toList());
        }

        // Obtener citas existentes para la fecha
        List<Cita> citasExistentes = citaRepository.findByFechaOrderByHora(fecha);

        // Filtrar horarios ocupados
        List<String> horariosOcupados = citasExistentes.stream()
                .map(cita -> cita.getHora().toString())
                .collect(Collectors.toList());

        // Devolver solo horarios disponibles
        return todosLosHorarios.stream()
                .filter(hora -> !horariosOcupados.contains(hora))
                .collect(Collectors.toList());
    }

    // Método para obtener horarios disponibles como objetos
    public List<Map<String, Object>> obtenerHorariosDisponibles(LocalDate fecha, String servicioId) {
        // Obtener todos los horarios disponibles como strings
        List<String> horariosDisponiblesStr = obtenerHorariosDisponiblesString(fecha);

        // Convertir a formato de objeto para más información
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (String horaStr : horariosDisponiblesStr) {
            Map<String, Object> horario = new HashMap<>();
            LocalTime hora = LocalTime.parse(horaStr);
            horario.put("hora", hora);
            horario.put("horaFormateada", hora.format(DateTimeFormatter.ofPattern("HH:mm")));
            horario.put("disponible", true);

            resultado.add(horario);
        }

        return resultado;
    }

    // Método para crear una cita
    public void crearCita(String nombre, String apellido, String direccion, String telefono, String correo, String cedula,
                          LocalDate fecha, LocalTime hora, String doctorId, String servicioId, String username) {

        // Verificar si la hora está disponible
        List<String> horariosDisponibles = obtenerHorariosDisponiblesString(fecha);
        if (!horariosDisponibles.contains(hora.toString())) {
            throw new RuntimeException("La hora seleccionada no está disponible");
        }

        // Crear nueva cita
        Cita cita = new Cita();
        cita.setNombre(nombre);
        cita.setApellido(apellido);
        cita.setDireccion(direccion);
        cita.setTelefono(telefono);
        cita.setCorreo(correo);
        cita.setCedula(cedula);
        cita.setFecha(fecha);
        cita.setHora(hora);
        cita.setEstado("PROGRAMADA");

        // Buscar usuario por username
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            cita.setUsuarioId(usuario.getId());
            cita.setUsuarioUsername(usuario.getUsername());
        }

        // Asignar doctor si se especificó
        if (doctorId != null && !doctorId.isEmpty()) {
            Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
            if (doctorOpt.isPresent()) {
                Doctor doctor = doctorOpt.get();
                cita.setDoctorId(doctor.getId());
                cita.setDoctorNombre(doctor.getNombre());
                cita.setDoctorApellido(doctor.getApellido());
            }
        }

        // Asignar servicio por defecto como "Consulta por Medicina General"
        cita.setServicioNombre("Consulta por Medicina General");

        // Guardar la cita en la base de datos
        citaRepository.save(cita);
    }
}