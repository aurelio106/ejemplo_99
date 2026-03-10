package com.example.ejemplo_99.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class RoleBasedAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        // Añadir logs para depuración
        System.out.println("Usuario autenticado: " + authentication.getName());
        System.out.println("Roles del usuario: " + roles);

        if (roles.contains("ROLE_ADMIN")) {
            System.out.println("Redirigiendo a /admin");
            response.sendRedirect("/admin");
        } else {
            System.out.println("Redirigiendo a /inicio");
            response.sendRedirect("/inicio");
        }
    }
}