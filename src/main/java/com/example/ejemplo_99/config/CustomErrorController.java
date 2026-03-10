package com.example.ejemplo_99.config;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Obtener el código de estado
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        // Añadir timestamp
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        model.addAttribute("timestamp", now.format(formatter));

        // Añadir path si está disponible
        if (path != null) {
            model.addAttribute("path", path.toString());
        }

        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            model.addAttribute("statusCode", statusCode);

            // Personalizar mensajes según el código de estado
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("error", "La página que buscas no existe");
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("error", "No tienes permiso para acceder a esta página");
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                model.addAttribute("error", "Error interno del servidor" +
                        (message != null ? ": " + message : ""));
            } else {
                model.addAttribute("error", "Ha ocurrido un error" +
                        (message != null ? ": " + message : ""));
            }
        } else {
            model.addAttribute("error", "Ha ocurrido un error inesperado");
        }

        return "error";
    }
}