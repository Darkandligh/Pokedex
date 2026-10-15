package co.edu.fet.pokedex.controller;

import co.edu.fet.pokedex.dto.SesionDto;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Comprobación del inicio de sesión del administrador (HU-13). El frontend llama aquí con
 * las credenciales: 200 si son de un administrador, 401 si no.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/sesion")
    public SesionDto sesion(Authentication autenticacion) {
        return new SesionDto(autenticacion.getName());
    }
}
