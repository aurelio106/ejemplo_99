package com.example.ejemplo_99.repositories.mysql;

import com.example.ejemplo_99.models.mysql.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar por username
    Optional<Usuario> findByUsername(String username);

    // Buscar por email
    Optional<Usuario> findByEmail(String email);

    // Buscar por nombre o apellido
    List<Usuario> findByNombreContainingOrApellidoContaining(String nombre, String apellido);

    // Verificar si existe por username
    boolean existsByUsername(String username);

    // Verificar si existe por email
    boolean existsByEmail(String email);

    // Buscar usuarios activos
    List<Usuario> findByActivoTrue();

    // Buscar por rol - MODIFICAR ESTE MÉTODO
    List<Usuario> findByRolesContaining(String rol);
}