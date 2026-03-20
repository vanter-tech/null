package com.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * Filtro de seguridad personalizado que
 * intercepta todas las peticiones HTTP entrantes.
 * <p>
 * Extiende {@link OncePerRequestFilter} para
 * garantizar que este filtro se ejecute
 * una y solo una vez por cada petición del cliente.
 * Su responsabilidad es extraer
 * el token JWT del header, validarlo y configurar
 * el contexto de seguridad de Spring.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    /**
     * Primeros 7 caracteres (Bearer ) para eliminar y
     * obtener el código jwt limpio.
     */
    private final int bearer = 7;

    /**
     * Servicio encargado de la gestión de tokens JWT.
     * Contiene la lógica para generar
     * nuevos tokens, validarlos y extraer
     * información (como el correo del usuario)
     * desde el token cifrado.
     */
    private final JwtService jwtService;

    /**
     * Servicio principal de Spring Security
     * utilizado para cargar los detalles del usuario.
     * Se encarga de buscar al usuario en la
     * base de datos durante el proceso
     * de autenticación para verificar que realmente
     * exista y esté activo.
     */
    private final UserDetailsService userDetailsService;

    /**
     * Lógica central de filtrado de peticiones.
     * <p>
     * Evalúa si la ruta requiere validación, extrae
     * el token 'Bearer', verifica la
     * firma y expiración, y finalmente inyecta al usuario
     * autenticado en el hilo actual.
     * </p>
     *
     * @param request  La petición HTTP entrante.
     * @param response La respuesta HTTP saliente.
     * @param filterChain La cadena de filtros de Spring Security.
     */
    @Override
    protected void doFilterInternal(
            final @NonNull HttpServletRequest request,
            final @NonNull HttpServletResponse response,
            final @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // BYPASS (Rutas Públicas y WebSockets)
        // Si es la ruta de autenticación
        // (login/registro) o el handshake de WebSockets ("/ws"),
        // dejamos pasar la petición sin exigir un token JWT
        // en el header HTTP.
        // NOTA DE ARQUITECTURA: Los navegadores
        // no soportan headers personalizados
        // en el handshake de WebSockets, por eso
        // DEBE excluirse aquí.
        if (request.getServletPath().contains("/api/v1/auth")
                ||
                request.getServletPath().startsWith("/ws")) {
            filterChain.doFilter(request, response);
            return;
        }

        // EXTRACCIÓN DEL HEADER
        final String authHeader = request.getHeader(AUTHORIZATION);
        final String jwt;
        final String userEmail;

        // Si no hay header de Autorización o
        // no tiene el formato "Bearer [token]",
        // pasamos al siguiente filtro (que probablement
        // rechazará la petición con un 403 Forbidden).
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // EXTRACCIÓN DEL TOKEN Y USUARIO
        // Cortamos los primeros 7 caracteres
        // ("Bearer ") para aislar el token real
        jwt = authHeader.substring(bearer);
        userEmail = jwtService.extractUsername(jwt);

        // VALIDACIÓN Y AUTENTICACIÓN
        // Si extrajimos un email válido y el
        // contexto de seguridad aún no tiene a nadie autenticado...
        if (userEmail != null
                &&
                SecurityContextHolder.getContext()
                        .getAuthentication() == null) {

            // Buscamos al usuario en la Base de Datos
            UserDetails userDetails = userDetailsService
                    .loadUserByUsername(userEmail);

            // Verificamos matemáticamente si el token
            // pertenece a este usuario y si no ha expirado
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // Creamos un objeto de
                // autenticación válido para Spring Security
                UsernamePasswordAuthenticationToken authToken
                        =
                        new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                // Le agregamos detalles extra de
                // la petición (como la IP o el ID de sesión)
                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                //  PASO CRÍTICO: Guardamos la
                //  autenticación en el Contexto de Spring.
                // Esto permite que el resto de la aplicación
                // (Controladores, Servicios) sepa
                // exactamente quién está haciendo
                // la petición usando @AuthenticationPrincipal.
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // CONTINUAR LA CADENA
        // Ya sea que se haya autenticado o no,
        // dejamos que la petición continúe su camino.
        filterChain.doFilter(request, response);
    }
}
