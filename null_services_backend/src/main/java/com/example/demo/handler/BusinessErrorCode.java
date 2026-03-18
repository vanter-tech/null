package com.example.demo.handler;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_IMPLEMENTED;


/**
 * Enum que define los códigos de error de negocio
 * personalizados de la aplicación.
 * Cada código incluye un número identificador
 * interno, un estado HTTP apropiado
 * y una descripción del error para retornar al cliente.
 */
public enum BusinessErrorCode {

    /** Error por defecto cuando
     * no hay un código específico implementado.
     */
    NO_CODE(0, NOT_IMPLEMENTED,
            "NO CODE"),

    /** Error cuando la contraseña actual
     * proporcionada por el usuario es incorrecta.
     */
    INCORRECT_CURRENT_PASSWORD(300, BAD_REQUEST,
            "Password Incorrect"),

    /** Error cuando la nueva contraseña y su
     * confirmación no coinciden en un cambio.
     */
    NEW_PASSWORD_DOES_NOT_MATCH(301, BAD_REQUEST,
            "New Password Does Not Match"),

    /** Error cuando se intenta acceder con
     * una cuenta que ha sido deshabilitada.
     */
    ACCOUNT_DISABLED(303, FORBIDDEN,
            "Current Account Disabled"),

    /** Error cuando la cuenta del usuario se
     * encuentra bloqueada por seguridad.
     */
    ACCOUNT_LOCKED(302, FORBIDDEN,
            "Current Account Locked"),

    /** Error genérico cuando las
     * credenciales (usuario o contraseña) son inválidas.
     */
    BAD_CREDENTIALS(304, FORBIDDEN,
            "Bad credentials");

    /** Código numérico interno del error de negocio. */
    @Getter
    private final int code;

    /** Descripción legible por humanos sobre el motivo del error. */
    @Getter
    private final String description;

    /** Estado HTTP de Spring correspondiente que se enviará en la respuesta. */
    @Getter
    private final HttpStatus httpStatus;

    /**
     * Constructor para inicializar una constante de error de negocio.
     *
     * @param codes El código numérico interno del error.
     * @param httpStatuS El estado HTTP asociado (ej. 400, 403).
     * @param descriptions La descripción detallada del mensaje de error.
     */
    BusinessErrorCode(
            final int codes,
            final HttpStatus httpStatuS,
            final String descriptions) {
        this.code = codes;
        this.description = descriptions;
        this.httpStatus = httpStatuS;
    }
}
