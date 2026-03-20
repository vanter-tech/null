package com.example.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Servicio encargado de toda la criptografía y g
 * estión de los JSON Web Tokens (JWT).
 *
 * Su responsabilidad incluye generar nuevos
 * tokens al iniciar sesión, extraer
 * información (claims) de tokens existentes y validar
 * que no hayan sido alterados
 * ni hayan expirado.
 */
@Service
public class JwtService {

    /**
     * Tiempo de expiración del token en
     * milisegundos, inyectado desde la configuración.
     */
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    /**
     * Clave secreta en Base64 utilizada para
     * firmar digitalmente los tokens.
     */
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    /**
     * Extrae el nombre de usuario
     * (en este sistema, el email) del token.
     * El "Subject" es el estándar en JWT para
     * guardar el identificador principal.
     *
     * @param token El token JWT en formato
     * String enviado por el cliente.
     * @return El nombre de usuario (email)
     * extraído del token.
     */
    public String extractUsername(
            final String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Método genérico para extraer
     * cualquier "Claim" (pieza de información) del token.
     * Utiliza funciones de orden superior
     * (Function) para retornar el tipo de dato correcto.
     *
     * @param <T> El tipo de dato esperado del claim a extraer.
     * @param token El token JWT en formato String.
     * @param claimsTResolver Función que define
     * cómo extraer el claim específico.
     * @return El valor del claim extraído.
     */
    public <T> T extractClaim(
            final String token,
            final Function<Claims, T> claimsTResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsTResolver.apply(claims);
    }

    /**
     * Desencripta y parsea el token completo
     * para obtener su cuerpo (Payload).
     * Si el token fue alterado o la firma no
     * coincide con nuestra secretKey,
     * este método lanzará una excepción
     * automáticamente (SignatureException).
     *
     * @param token El token JWT en formato String.
     * @return El objeto Claims que contiene
     * toda la información del token.
     */
    private Claims extractAllClaims(
            final String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Genera un token estándar solo con
     * los datos básicos del usuario.
     *
     * @param userDetails Los datos del usuario autenticado.
     * @return El token JWT generado en formato String.
     */
    public String generateToken(
            final UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Genera un token inyectando claims
     * adicionales (ejemplo: el userId para los WebSockets).
     *
     * @param claims Mapa con información extra a
     * guardar dentro del token.
     * @param userDetails Los datos del usuario autenticado.
     * @return El token JWT en formato String.
     */
    public String generateToken(
            final Map<String, Object> claims,
            final UserDetails userDetails) {
        return buildToken(claims, userDetails, jwtExpiration);
    }

    /**
     * Constructor real del JWT.
     * Ensambla el Header (algoritmo), el
     * Payload (claims, subject, fechas, roles)
     * y la Signature (firma matemática).
     *
     * @param extraClaims Mapa con información
     * adicional a incluir en el cuerpo del token.
     * @param userDetails Los datos del usuario autenticado.
     * @param jwtExpirations El tiempo de validez del
     * token a partir de su creación.
     * @return El token JWT firmado, compactado y listo
     * para ser enviado al cliente.
     */
    private String buildToken(
            final Map<String, Object> extraClaims,
            final UserDetails userDetails,
            final long jwtExpirations) {

        // Extraemos los roles de Spring Security
        // para guardarlos dentro del token
        var authorities = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                // La fecha de expiración se calcula
                // sumando el tiempo configurado a la fecha actual
                .setExpiration(new Date(
                        System.currentTimeMillis() + jwtExpirations))
                // Inyectamos los roles para que el frontend
                // pueda leerlos si lo necesita
                .claim("authorities", authorities)
                // Firmamos el token con nuestro secreto
                // usando el algoritmo HMAC SHA-256
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Valida si un token es legítimo para un
     * usuario específico y si aún está vigente.
     *
     * @param token El JWT recibido en la petición HTTP.
     * @param userDetails El usuario cargado desde la base de datos.
     * @return true si el token pertenece al usuario y no
     * ha expirado; false en caso contrario.
     */
    public boolean isTokenValid(
            final String token,
            final UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username
                .equals(userDetails.getUsername()))
                &&
                !isTokenExpired(token);
    }

    /**
     * Comprueba si la fecha de expiración del token
     * ya pasó respecto a la fecha actual.
     *
     * @param token El token JWT a evaluar.
     * @return true si el token ya expiró, false si aún es válido.
     */
    private boolean isTokenExpired(
            final String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrae específicamente la fecha de
     * expiración configurada dentro del token.
     *
     * @param token El token JWT del cual extraer la fecha.
     * @return Objeto Date con la fecha y hora
     * exacta en la que expira el token.
     */
    private Date extractExpiration(
            final String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Decodifica la clave secreta configurada (que debe estar en Base64)
     * y genera el objeto Key criptográfico
     * necesario para firmar y validar los tokens.
     *
     * @return La clave criptográfica para operaciones HMAC-SHA.
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
