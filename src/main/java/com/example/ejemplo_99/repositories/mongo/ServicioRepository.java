package com.example.ejemplo_99.repositories.mongo;

import com.example.ejemplo_99.models.mongo.Servicio;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicioRepository extends MongoRepository<Servicio, String> {

    // Buscar por nombre
    List<Servicio> findByNombreContaining(String nombre);

    // Buscar por categoría
    List<Servicio> findByCategoria(String categoria);

    // Buscar por duración menor o igual
    List<Servicio> findByDuracionMinutosLessThanEqual(int duracionMinutos);

    // Verificar si existe por nombre
    boolean existsByNombre(String nombre);
}