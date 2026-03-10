package com.example.ejemplo_99.controllers;

import com.example.ejemplo_99.models.mysql.Usuario;
import com.example.ejemplo_99.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/admin/usuarios/cambiar-estado/{id}")
    @ResponseBody
    public Map<String, Object> cambiarEstadoUsuario(
            @PathVariable Long id,
            @RequestParam("activo") boolean activo) {

        Map<String, Object> response = new HashMap<>();
        boolean resultado = usuarioService.cambiarEstadoUsuario(id, activo);

        response.put("success", resultado);
        return response;
    }

    @PostMapping("/admin/usuarios/actualizar/{id}")
    @ResponseBody
    public Map<String, Object> actualizarUsuario(
            @PathVariable Long id,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("rol") String rol) {

        Map<String, Object> response = new HashMap<>();

        Optional<Usuario> usuarioOpt = usuarioService.findById(id);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.setUsername(username);
            usuario.setEmail(email);
            usuario.addRol(rol);

            usuarioService.save(usuario);
            response.put("success", true);
        } else {
            response.put("success", false);
            response.put("message", "Usuario no encontrado");
        }

        return response;
    }
}