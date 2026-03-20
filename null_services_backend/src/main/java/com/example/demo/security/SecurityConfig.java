package com.example.demo.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Configuración global de Seguridad HTTP (El Muro de Fuego de la aplicación).
 * <p>
 * Define la cadena de filtros de seguridad (Security Filter Chain), estableciendo
 * qué rutas son de acceso público y cuáles requieren un token JWT válido.
 * </p>
 */
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true) // Permite usar @Secured o @PreAuthorize a nivel de método
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;
    private final JwtFilter jwtFilter; // (Asegúrate de que la clase JwtFilter empiece con mayúscula)

    /**
     * Construye y configura el filtro de seguridad principal de Spring.
     * <p>
     * NOTA DE ARQUITECTURA: Dado que el application.yml define un context-path de '/api/v1',
     * todos los requestMatchers aquí definidos son relativos a esa ruta base.
     * Ej: "/auth/**" intercepta "http://dominio/api/v1/auth/..."
     * </p>
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // 1. CORS y CSRF
                // Habilitamos CORS usando la configuración que definimos en BeansConfig.
                // Deshabilitamos CSRF (Cross-Site Request Forgery) porque al usar JWT (Stateless),
                // no dependemos de cookies de sesión, que es donde ataca el CSRF.
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)

                // 2. REGLAS DE AUTORIZACIÓN (El semáforo)
                .authorizeHttpRequests(req ->
                        req.requestMatchers(
                                        // Rutas de Onboarding / Autenticación
                                        "/auth/**",

                                        // Rutas de Swagger y Documentación de API
                                        "/v2/api-docs",
                                        "/v3/api-docs",
                                        "/v3/api-docs/**",
                                        "/swagger-resources",
                                        "/swagger-resources/**",
                                        "/configuration/ui",
                                        "/configuration/security",
                                        "/swagger-ui/**",
                                        "/webjars/**",
                                        "/swagger-ui.html",

                                        // Rutas de WebSockets (Handshake inicial y tráfico STOMP)
                                        "/ws",
                                        "/ws/**" // <- CORREGIDO: Eliminamos el "/api/v1" redundante
                                ).permitAll() // Todo lo listado arriba pasa sin pedir JWT

                                // Cualquier otra petición (ej. chats, mensajes, amigos) REQUIERE estar autenticado
                                .anyRequest().authenticated()
                )

                // 3. GESTIÓN DE SESIÓN
                // Política STATELESS: Spring Security no creará HttpSession. Cada petición HTTP
                // debe traer su propio token JWT para identificarse (excepto WebSockets una vez conectados).
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. PROVEEDOR Y FILTROS
                // Inyectamos nuestro proveedor (que usa BCrypt y UserDetailsService)
                .authenticationProvider(authenticationProvider)

                // ⚠️ CRÍTICO: Colocamos nuestro filtro JWT ANTES del filtro estándar de usuario/contraseña.
                // Así nos aseguramos de que el JWT se evalúe primero.
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
