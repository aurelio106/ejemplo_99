package com.example.ejemplo_99.repositories.mongo;

import com.example.ejemplo_99.models.mongo.Cita;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CitaRepository extends MongoRepository<Cita, String> {

    // Buscar por usuario
    List<Cita> findByUsuarioId(Long usuarioId);

    // Buscar por fecha
    List<Cita> findByFechaOrderByHora(LocalDate fecha);

    // Buscar por fecha y estado
    List<Cita> findByFechaAndEstado(LocalDate fecha, String estado);

    // Buscar por rango de fechas
    List<Cita> findByFechaBetweenOrderByFechaAscHoraAsc(LocalDate fechaInicio, LocalDate fechaFin);

    // Buscar por estado
    List<Cita> findByEstado(String estado);

    // Contar por estado
    long countByEstado(String estado);

    // Contar por fecha
    long countByFecha(LocalDate fecha);

    // Contar por rango de fechas
    long countByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin);

    // Contar por servicio
    long countByServicioId(String servicioId);

    // Contar por doctor
    long countByDoctorId(String doctorId);
}