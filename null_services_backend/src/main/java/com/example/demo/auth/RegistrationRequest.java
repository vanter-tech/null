package com.example.demo.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO (Data Transfer Object) que encapsula la
 * carga útil (payload) enviada por el frontend
 * durante el proceso de registro de un nuevo usuario.
 * <p>
 * Utiliza Jakarta Bean Validation para garantizar
 * la integridad de los datos antes de
 * que la petición alcance la capa de servicio. Si alguna
 * validación falla, el framework
 * intercepta la petición y devuelve un error
 * 400 (Bad Request) automático con los mensajes
 * definidos aquí, los cuales pueden ser leídos
 * directamente por Angular para mostrar
 * alertas en el formulario.
 * </p>
 */
@Getter
@Setter
@Builder
public class RegistrationRequest {

    /**
     * Correo electrónico del usuario, que servirá
     * como su identificador único (username).
     */
    @NotEmpty(message = "Email is required")
    @NotBlank(message = "Blank spaces not accepted")
    private String email;

    /**
     * Nombre completo del usuario para propósitos
     * formales o de facturación.
     */
    @NotEmpty(message = "This form is required")
    @NotBlank(message = "Blank spaces not accepted")
    private String fullname;

    /**
     * El alias o apodo que verán los demás usuarios
     * en la lista de amigos y en las salas de chat.
     */
    @NotEmpty(message = "This form is required")
    @NotBlank(message = "Blank spaces not accepted")
    private String nickName;

    /**
     * Contraseña elegida por el usuario.
     * <p>
     * NOTA DE ARQUITECTURA: Esta contraseña viaja
     * en texto plano desde el frontend
     * hasta este DTO. Es responsabilidad estricta
     * del AuthenticationService hashearla
     * (ej. usando BCrypt) inmediatamente antes de guardarla
     * en la base de datos.
     * </p>
     */
    @NotEmpty(message = "This form is required")
    @NotBlank(message = "Blank spaces not accepted")
    private String password;

    /**
     * Fecha de nacimiento del usuario.
     * <p>
     * Se valida con @NotNull porque, a diferencia de los String,
     * las fechas son objetos
     * y no pueden estar "en blanco", solo pueden ser nulas.
     * Útil para futuras validaciones de mayoría de edad
     * en el sistema de chat.
     * </p>
     */
    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

}
