package com.example.ejemplo_99.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final AuthenticationSuccessHandler roleBasedAuthenticationSuccessHandler;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(AuthenticationSuccessHandler roleBasedAuthenticationSuccessHandler,
                          CustomUserDetailsService userDetailsService) {
        this.roleBasedAuthenticationSuccessHandler = roleBasedAuthenticationSuccessHandler;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(List.of(authenticationProvider()));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/inicio", "/login", "/register", "/servicios", "/doctores").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/image/**", "/webjars/**").permitAll()
                        .requestMatchers("/reservar", "/horarios-disponibles", "/servicio-info/**").permitAll()
                        .requestMatchers("/admin", "/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/mis-citas", "/mis-citas/**", "/reservarCita").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        .requestMatchers("/debug/**").permitAll() // Para depuración
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(roleBasedAuthenticationSuccessHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .expiredUrl("/login?expired")
                )
                // Configuración CSRF - Permitir solicitudes POST para eliminar
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                new AntPathRequestMatcher("/admin/doctores/eliminar/**"),
                                new AntPathRequestMatcher("/admin/servicios/eliminar/**"),
                                new AntPathRequestMatcher("/admin/citas/eliminar/**"),
                                new AntPathRequestMatcher("/admin/citas/*/estado")
                        )
                );

        return http.build();
    }
}
