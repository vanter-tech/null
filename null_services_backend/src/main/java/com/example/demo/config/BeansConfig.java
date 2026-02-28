package com.example.demo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

/**
 * Clase de configuración central que provee los Beans necesarios para el motor de Spring Security
 * y la política global de CORS (Cross-Origin Resource Sharing).
 * <p>
 * Aquí se definen los componentes que Spring utilizará por debajo para codificar contraseñas,
 * buscar usuarios en la base de datos y permitir que el frontend (Angular) se comunique
 * sin ser bloqueado por el navegador.
 * </p>
 */
@Configuration
@RequiredArgsConstructor
public class BeansConfig {

    private final UserDetailsService userDetailsService;

    /**
     * Proveedor de autenticación (Data Access Object - DAO).
     * <p>
     * Le enseña a Spring Security cómo encontrar al usuario (usando nuestro UserDetailsService)
     * y cómo verificar su contraseña (usando nuestro PasswordEncoder).
     * </p>
     *
     * @return DaoAuthenticationProvider configurado con nuestra lógica de base de datos.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    /**
     * El director de orquesta de la autenticación.
     * <p>
     * Es el encargado de recibir la petición de login desde el controlador, pasarla al
     * AuthenticationProvider y devolver el objeto Authentication validado si todo es correcto.
     * </p>
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Define el algoritmo criptográfico para las contraseñas.
     * <p>
     * BCrypt es el estándar de la industria. Aplica un "salt" aleatorio a cada contraseña,
     * lo que significa que dos usuarios con la misma clave "12345678" tendrán hashes
     * completamente distintos en la base de datos, previniendo ataques de diccionario.
     * </p>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configuración global de CORS para permitir peticiones desde el cliente Angular.
     * <p>
     * NOTA DE ARQUITECTURA: Esta configuración está específicamente afinada para soportar
     * WebSockets y SockJS. SockJS realiza peticiones HTTP complejas (handshakes) que
     * el navegador intercepta lanzando peticiones preflight (OPTIONS).
     * </p>
     *
     * @return Fuente de configuración CORS basada en URL.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();

        // Permite el envío de credenciales (cookies, headers de autorización como nuestro JWT)
        config.setAllowCredentials(true);

        // Define el origen exacto de nuestro frontend Angular
        config.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));

        // Se permiten todos los headers ("*") porque los protocolos como SockJS inyectan
        // headers dinámicos durante el establecimiento del túnel en tiempo real.
        config.setAllowedHeaders(Collections.singletonList("*"));

        // Se definen los métodos permitidos. Es CRÍTICO mantener "OPTIONS" aquí, ya que
        // los navegadores lo usan automáticamente antes de peticiones POST/PUT complejas
        // para verificar si el servidor los acepta.
        config.setAllowedMethods(Arrays.asList("GET", "POST", "DELETE", "PUT", "PATCH", "OPTIONS"));

        // Aplica esta política a todas las rutas ("/**") del servidor
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}