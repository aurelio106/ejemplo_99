package com.example.ejemplo_99.repositories.mongo;

import com.example.ejemplo_99.models.mongo.Doctor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends MongoRepository<Doctor, String> {

    // Buscar por especialidad
    List<Doctor> findByEspecialidad(String especialidad);

    // Buscar por nombre o apellido
    List<Doctor> findByNombreContainingOrApellidoContaining(String nombre, String apellido);

    // Verificar si existe por correo
    boolean existsByCorreo(String correo);

    // Verificar si existe por teléfono
    boolean existsByTelefono(String telefono);
}