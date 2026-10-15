package co.edu.fet.pokedex.config;

import co.edu.fet.pokedex.dto.ErrorDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Seguridad de la API (HU-13, T-13.2).
 *
 * - Las consultas (GET) son públicas, como en el Sprint 1.
 * - Registrar, editar y eliminar (POST, PUT, DELETE) exige el rol ADMIN.
 * - El administrador se autentica con HTTP Basic; el usuario y la clave salen de
 *   pokedex.admin.usuario y pokedex.admin.clave (variables POKEDEX_ADMIN_USUARIO y POKEDEX_ADMIN_CLAVE).
 * - Los errores 401 y 403 tienen la misma forma que el resto: {"estado": 401, "mensaje": "..."}.
 *   No se envía la cabecera WWW-Authenticate para que el navegador no muestre su propia ventana de inicio de sesión.
 */
@Configuration
public class SeguridadConfig {

    public static final String ROL_ADMIN = "ADMIN";
    static final String MENSAJE_SIN_SESION = "Debes iniciar sesión como administrador";
    static final String MENSAJE_SIN_PERMISO = "Solo un administrador puede hacer esto";

    @Bean
    SecurityFilterChain filtroSeguridad(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        AuthenticationEntryPoint sinSesion = (peticion, respuesta, excepcion) ->
                escribirError(respuesta, objectMapper, HttpStatus.UNAUTHORIZED, MENSAJE_SIN_SESION);
        AccessDeniedHandler sinPermiso = (peticion, respuesta, excepcion) ->
                escribirError(respuesta, objectMapper, HttpStatus.FORBIDDEN, MENSAJE_SIN_PERMISO);

        return http
                // API sin sesiones ni formularios del servidor: no aplica CSRF
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(reglas -> reglas
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole(ROL_ADMIN)
                        .requestMatchers(HttpMethod.GET, "/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().hasRole(ROL_ADMIN))
                .httpBasic(basic -> basic.authenticationEntryPoint(sinSesion))
                .exceptionHandling(errores -> errores
                        .authenticationEntryPoint(sinSesion)
                        .accessDeniedHandler(sinPermiso))
                .build();
    }

    @Bean
    PasswordEncoder codificadorClaves() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    UserDetailsService usuarios(@Value("${pokedex.admin.usuario}") String usuario,
                                @Value("${pokedex.admin.clave}") String clave,
                                PasswordEncoder codificador) {
        return new InMemoryUserDetailsManager(User.withUsername(usuario)
                .password(codificador.encode(clave))
                .roles(ROL_ADMIN)
                .build());
    }

    private static void escribirError(HttpServletResponse respuesta, ObjectMapper objectMapper,
                                      HttpStatus estado, String mensaje) throws IOException {
        respuesta.setStatus(estado.value());
        respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(respuesta.getOutputStream(), new ErrorDto(estado.value(), mensaje));
    }
}
