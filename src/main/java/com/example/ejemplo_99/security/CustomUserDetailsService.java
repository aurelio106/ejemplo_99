package com.example.ejemplo_99.security;

import com.example.ejemplo_99.models.mysql.Usuario;
import com.example.ejemplo_99.repositories.mysql.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // Verificar si el usuario está activo
        if (!usuario.isActivo()) {
            throw new UsernameNotFoundException("Usuario desactivado: " + username);
        }

        // Añadir logs para depuración
        System.out.println("Usuario encontrado: " + username);

        // Obtener roles y manejar posible nulo
        Set<String> roles = usuario.getRoles();
        System.out.println("Roles del usuario: " + (roles != null ? roles : "Sin roles"));

        // Crear lista de autoridades
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // Verificar que roles no sea nulo antes de iterar
        if (roles != null) {
            for (String rol : roles) {
                authorities.add(new SimpleGrantedAuthority(rol));
            }
        }

        // Imprimir las autoridades para verificar
        System.out.println("Autoridades asignadas: " + authorities);

        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                authorities
        );
    }
}