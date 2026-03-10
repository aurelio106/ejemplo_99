package com.example.ejemplo_99.services;

import com.example.ejemplo_99.models.mongo.Cita;
import com.example.ejemplo_99.models.mongo.Servicio;
import com.example.ejemplo_99.models.mysql.Usuario;
import com.example.ejemplo_99.repositories.mongo.CitaRepository;
import com.example.ejemplo_99.repositories.mongo.DoctorRepository;
import com.example.ejemplo_99.repositories.mongo.ServicioRepository;
import com.example.ejemplo_99.repositories.mysql.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Obtiene estadísticas generales para el dashboard
     */
    public Map<String, Object> obtenerEstadisticasGenerales() {
        Map<String, Object> estadisticas = new HashMap<>();

        // Contar citas por estado
        long totalCitas = citaRepository.count();
        long citasProgramadas = citaRepository.countByEstado("PROGRAMADA");
        long citasCompletadas = citaRepository.countByEstado("COMPLETADA");
        long citasCanceladas = citaRepository.countByEstado("CANCELADA");

        // Contar doctores y servicios
        long totalDoctores = doctorRepository.count();
        long totalServicios = servicioRepository.count();
        long totalUsuarios = usuarioRepository.count();

        // Añadir estadísticas al mapa
        estadisticas.put("totalCitas", totalCitas);
        estadisticas.put("citasProgramadas", citasProgramadas);
        estadisticas.put("citasCompletadas", citasCompletadas);
        estadisticas.put("citasCanceladas", citasCanceladas);
        estadisticas.put("totalDoctores", totalDoctores);
        estadisticas.put("totalServicios", totalServicios);
        estadisticas.put("totalUsuarios", totalUsuarios);

        return estadisticas;
    }

    /**
     * Obtiene las citas de hoy
     */
    public List<Cita> obtenerCitasHoy() {
        LocalDate hoy = LocalDate.now();
        return citaRepository.findByFechaOrderByHora(hoy);
    }

    /**
     * Obtiene las citas de la semana actual
     */
    public List<Cita> obtenerCitasSemana() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioSemana = hoy.minusDays(hoy.getDayOfWeek().getValue() - 1);
        LocalDate finSemana = inicioSemana.plusDays(6);
        return citaRepository.findByFechaBetweenOrderByFechaAscHoraAsc(inicioSemana, finSemana);
    }

    /**
     * Obtiene las citas del mes actual
     */
    public List<Cita> obtenerCitasMes() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());
        return citaRepository.findByFechaBetweenOrderByFechaAscHoraAsc(inicioMes, finMes);
    }

    /**
     * Obtiene estadísticas de citas por día para los últimos 7 días
     */
    public Map<String, Long> obtenerEstadisticasCitasPorDia() {
        Map<String, Long> citasPorDia = new LinkedHashMap<>();
        LocalDate hoy = LocalDate.now();

        // Obtener datos para los últimos 7 días
        for (int i = 6; i >= 0; i--) {
            LocalDate fecha = hoy.minusDays(i);
            long cantidadCitas = citaRepository.countByFecha(fecha);
            citasPorDia.put(fecha.toString(), cantidadCitas);
        }

        return citasPorDia;
    }

    /**
     * Obtiene los servicios más populares basados en la cantidad de citas
     */
    public List<Map<String, Object>> obtenerServiciosPopulares() {
        List<Map<String, Object>> serviciosPopulares = new ArrayList<>();

        // Obtener todos los servicios
        List<Servicio> servicios = servicioRepository.findAll();

        // Contar citas por servicio
        for (Servicio servicio : servicios) {
            long cantidadCitas = citaRepository.countByServicioId(servicio.getId());

            Map<String, Object> servicioData = new HashMap<>();
            servicioData.put("id", servicio.getId());
            servicioData.put("nombre", servicio.getNombre());
            servicioData.put("cantidad", cantidadCitas);

            serviciosPopulares.add(servicioData);
        }

        // Ordenar por cantidad de citas (descendente)
        serviciosPopulares.sort((s1, s2) ->
                Long.compare((Long) s2.get("cantidad"), (Long) s1.get("cantidad")));

        // Limitar a los 5 más populares
        return serviciosPopulares.stream().limit(5).collect(Collectors.toList());
    }

    /**
     * Busca citas por término en varios campos
     */
    public List<Cita> buscarCitas(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return citaRepository.findAll();
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

    /**
     * Busca usuarios por término en varios campos
     */
    public List<Usuario> buscarUsuarios(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return usuarioRepository.findAll();
        }

        String terminoLower = termino.toLowerCase();

        return usuarioRepository.findAll().stream()
                .filter(usuario ->
                        (usuario.getUsername() != null && usuario.getUsername().toLowerCase().contains(terminoLower)) ||
                                (usuario.getNombre() != null && usuario.getNombre().toLowerCase().contains(terminoLower)) ||
                                (usuario.getApellido() != null && usuario.getApellido().toLowerCase().contains(terminoLower)) ||
                                (usuario.getEmail() != null && usuario.getEmail().toLowerCase().contains(terminoLower))
                )
                .collect(Collectors.toList());
    }

    /**
     * Filtra citas por rango de fechas
     */
    public List<Cita> filtrarCitasPorFecha(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            return citaRepository.findAll();
        }

        return citaRepository.findByFechaBetweenOrderByFechaAscHoraAsc(fechaInicio, fechaFin);
    }

    /**
     * Cambia el estado de un usuario (activo/inactivo)
     */
    public boolean cambiarEstadoUsuario(Long id, boolean activo) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.setActivo(activo);
            usuarioRepository.save(usuario);
            return true;
        }
        return false;
    }
}