package com.example.ejemplo_99.config;

import com.example.ejemplo_99.models.mongo.Doctor;
import com.example.ejemplo_99.models.mongo.Servicio;
import com.example.ejemplo_99.models.mysql.Usuario;
import com.example.ejemplo_99.repositories.mongo.DoctorRepository;
import com.example.ejemplo_99.repositories.mongo.ServicioRepository;
import com.example.ejemplo_99.repositories.mysql.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Crear usuario administrador si no existe
        if (!usuarioRepository.existsByUsername("admin")) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin"));
            Set<String> roles = new HashSet<>();
            roles.add("ROLE_ADMIN");
            admin.setRoles(roles);
            usuarioRepository.save(admin);
            System.out.println("Usuario administrador creado");
        }

        // Crear usuario normal si no existe
        if (!usuarioRepository.existsByUsername("user")) {
            Usuario user = new Usuario();
            user.setUsername("user");
            user.setEmail("user@example.com");
            user.setPassword(passwordEncoder.encode("user"));
            Set<String> roles = new HashSet<>();
            roles.add("ROLE_USER");
            user.setRoles(roles);
            usuarioRepository.save(user);
            System.out.println("Usuario normal creado");
        }

        // Crear doctores de ejemplo si no hay doctores
        if (doctorRepository.count() == 0) {
            Doctor doctor1 = new Doctor();
            doctor1.setNombre("Carlos");
            doctor1.setApellido("Rodríguez");
            doctor1.setEspecialidad("Medicina General");
            doctor1.setCorreo("carlos.rodriguez@example.com");
            doctor1.setTelefono("555-123-4567");
            doctor1.setImagenUrl("/image/doctor-1.jpg");
            doctorRepository.save(doctor1);

            Doctor doctor2 = new Doctor();
            doctor2.setNombre("Ana");
            doctor2.setApellido("Martínez");
            doctor2.setEspecialidad("Odontología");
            doctor2.setCorreo("ana.martinez@example.com");
            doctor2.setTelefono("555-234-5678");
            doctor2.setImagenUrl("/image/doctor-2.jpg");
            doctorRepository.save(doctor2);

            Doctor doctor3 = new Doctor();
            doctor3.setNombre("Luis");
            doctor3.setApellido("Morales");
            doctor3.setEspecialidad("Pediatría");
            doctor3.setCorreo("luis.morales@example.com");
            doctor3.setTelefono("555-345-6789");
            doctor3.setImagenUrl("/image/doctor-3.jpg");
            doctorRepository.save(doctor3);

            System.out.println("Doctores de ejemplo creados");
        }

        // Crear servicios de ejemplo si no hay servicios
        if (servicioRepository.count() == 0) {
            Servicio servicio1 = new Servicio();
            servicio1.setNombre("Consulta General");
            servicio1.setDescripcion("Evaluación médica general para diagnóstico y tratamiento de enfermedades comunes.");
            servicio1.setIcono("ri-stethoscope-line");
            servicio1.setDuracionMinutos(30);
            servicioRepository.save(servicio1);

            Servicio servicio2 = new Servicio();
            servicio2.setNombre("Odontología");
            servicio2.setDescripcion("Servicios dentales que incluyen limpieza, extracciones y tratamientos de conducto.");
            servicio2.setIcono("ri-mental-health-line");
            servicio2.setDuracionMinutos(60);
            servicioRepository.save(servicio2);

            Servicio servicio3 = new Servicio();
            servicio3.setNombre("Pediatría");
            servicio3.setDescripcion("Atención médica especializada para niños y adolescentes.");
            servicio3.setIcono("ri-heart-pulse-line");
            servicio3.setDuracionMinutos(45);
            servicioRepository.save(servicio3);

            System.out.println("Servicios de ejemplo creados");
        }
    }
}