package com.example.demo.handler;

import jakarta.mail.MessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashSet;
import java.util.Set;

/**
 * Controlador de excepciones global para toda
 * la aplicación.
 * Intercepta las excepciones lanzadas por
 * cualquier controlador y las formatea
 * en un objeto estructurado (ExceptionResponse)
 * antes de enviarlas al cliente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja la excepción cuando se intenta acceder
     * con una cuenta que ha sido bloqueada.
     *
     * @param exp La excepción de cuenta bloqueada capturada.
     * @return Una respuesta HTTP 401 (No autorizado)
     * con los detalles del error de negocio.
     */
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ExceptionResponse> handleException(
            final LockedException exp) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(
                        ExceptionResponse
                                .builder()
                                .businessErrorCode(
                                        BusinessErrorCode
                                                .ACCOUNT_LOCKED
                                                .getCode())
                                .businessErrorDescription(
                                        BusinessErrorCode
                                                .ACCOUNT_LOCKED
                                                .getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    /**
     * Maneja la excepción cuando se intenta acceder
     * con una cuenta que ha sido deshabilitada.
     *
     * @param exp La excepción de cuenta deshabilitada capturada.
     * @return Una respuesta HTTP 401 (No autorizado)
     * con los detalles del error de negocio.
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ExceptionResponse> handleException(
            final DisabledException exp) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(
                        ExceptionResponse
                                .builder()
                                .businessErrorCode(
                                        BusinessErrorCode
                                                .ACCOUNT_DISABLED
                                                .getCode())
                                .businessErrorDescription(
                                        BusinessErrorCode
                                                .ACCOUNT_DISABLED
                                                .getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    /**
     * Maneja la excepción cuando el usuario introduce
     * credenciales (contraseña) incorrectas.
     *
     * @param exp La excepción de credenciales inválidas capturada.
     * @return Una respuesta HTTP 401 (No autorizado)
     * con los detalles del error.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleException(
            final BadCredentialsException exp) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(
                        ExceptionResponse
                                .builder()
                                .businessErrorCode(
                                        BusinessErrorCode
                                                .BAD_CREDENTIALS
                                                .getCode())
                                .businessErrorDescription(
                                        BusinessErrorCode
                                                .BAD_CREDENTIALS
                                                .getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    /**
     * Maneja la excepción cuando ocurre un problema
     * al intentar enviar un correo electrónico.
     *
     * @param exp La excepción de mensajería capturada.
     * @return Una respuesta HTTP 401 (No autorizado)
     * con el mensaje de error.
     */
    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ExceptionResponse> handleException(
            final MessagingException exp) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(
                        ExceptionResponse
                                .builder()
                                .error(exp.getMessage())
                                .build()
                );
    }

    /**
     * Maneja las excepciones de validación de datos
     * de entrada (por ejemplo, cuando fallan
     * las anotaciones en los DTOs al crear un usuario o enviar
     * un mensaje).
     *
     * @param exp La excepción de validación capturada
     * que contiene los campos con errores.
     * @return Una respuesta HTTP 401 (No autorizado)
     * con la lista de errores de validación.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleException(
            final MethodArgumentNotValidException exp) {

        Set<String> errors = new HashSet<>();
        exp.getBindingResult().getAllErrors()
                .forEach(err -> {
                    var errorMessage = err.getDefaultMessage();
                    errors.add(errorMessage);
                });

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(
                        ExceptionResponse
                                .builder()
                                .validationError(errors)
                                .build()
                );
    }

    /**
     * Maneja cualquier otra excepción inesperada que
     * no haya sido capturada por los métodos anteriores.
     * Actúa como una red de seguridad final para evitar que
     * la aplicación falle silenciosamente.
     *
     * @param exp La excepción genérica capturada.
     * @return Una respuesta HTTP 500 (Error interno del servidor)
     * con los detalles del fallo.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(
            final Exception exp) {
        exp.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorDescription(
                                        "Internal Server Error")
                                .error(exp.getMessage())
                                .build()
                );
    }

}
