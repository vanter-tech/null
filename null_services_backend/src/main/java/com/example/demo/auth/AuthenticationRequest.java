package com.example.demo.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO (Data Transfer Object) que encapsula la carga útil (payload) de
 * la solicitud de inicio de sesión.
 * <p>
 * Implementa validación de entrada (Input Validation)
 * mediante Jakarta Bean Validation.
 * Esto asegura el principio "Fail-Fast": si el cliente
 * envía credenciales mal formadas,
 * el controlador lanzará un MethodArgumentNotValidException
 * automáticamente, devolviendo
 * un error 400 (Bad Request) sin llegar a sobrecargar
 * la capa de servicio o la base de datos.
 * </p>
 */
@Getter
@Setter
@Builder
public class AuthenticationRequest {
    /**
     * Caracteres minimo para la contraseña.
     */
    private static final int MIN_PASSWORD_LENGTH = 8;

    /**
     * El nombre de usuario utilizado para el login.
     * En esta arquitectura, se utiliza
     * estrictamente el correo electrónico.
     * <p>
     * Se valida estructuralmente (@Email) y se previene la inyección de cadenas
     * compuestas únicamente por espacios en blanco (@NotBlank).
     * </p>
     */
    @Email(message = "Bad format to email address")
    @NotEmpty(message = "It must no be empty")
    @NotBlank(message = "Learn how to type , DONT BLANK SPACES")
    private String username;

    /**
     * La contraseña en texto plano recibida desde el frontend (Angular).
     * <p>
     * Se aplica una validación de longitud mínima (@Size) para garantizar que,
     * al menos a nivel estructural, cumpla con la política básica de seguridad
     * antes de intentar compararla con el hash de la base de datos.
     * </p>
     */
    @NotEmpty(message = "It must no be empty")
    @NotBlank(message = "Learn how to type , DONT BLANK SPACES")
    @Size(min = MIN_PASSWORD_LENGTH,
            message = "Generic password will not accepted")
    private String password;

}
