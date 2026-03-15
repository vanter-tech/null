package com.example.demo.auth;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Controlador REST que gestiona el acceso y la identidad en la aplicación.
 * <p>
 * Expone los endpoints públicos (no requieren token previo) para el onboarding
 * de usuarios: Registro, Activación de Cuenta por correo y Autenticación (Login).
 * </p>
 */
@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@Tag(name = "authentication", description = "Endpoints para registro, login y validación de cuentas")
public class AuthenticationController {

    private final AuthenticationService service;

    /**
     * Paso 1 del Onboarding: Registra a un nuevo usuario en el sistema.
     * <p>
     * El usuario se crea por defecto en estado inactivo (enable = false).
     * Inmediatamente después de guardarlo, el servicio dispara un correo electrónico
     * con el código de activación de 6 dígitos.
     * </p>
     *
     * @param request Objeto que contiene los datos del formulario de registro (email, password, etc.).
     * La anotación @Valid asegura que cumpla con las restricciones antes de entrar al método.
     * @return ResponseEntity con código 202 (Accepted), indicando que la solicitud fue
     * aceptada, pero el proceso completo (activación) aún está pendiente.
     * @throws MessagingException Si ocurre un error al intentar enviar el correo de activación.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> register(
            @RequestBody @Valid RegistrationRequest request
    ) throws MessagingException {
        service.register(request);
        return ResponseEntity.accepted().build();
    }

    /**
     * Autenticación de usuarios (Login).
     * <p>
     * Recibe las credenciales, las valida contra la base de datos y, si son correctas,
     * genera un JSON Web Token (JWT).
     * NOTA DE ARQUITECTURA: El JWT generado aquí incluye el 'userId' en sus claims,
     * lo cual es vital para el funcionamiento del chat y los WebSockets en el frontend.
     * </p>
     *
     * @param request Credenciales del usuario (email y contraseña).
     * @return ResponseEntity con código 200 (OK) y el token JWT junto con datos básicos del usuario.
     */
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody @Valid AuthenticationRequest request
    ){
        return ResponseEntity.ok(service.authenticate(request));
    }

    /**
     * Paso 2 del Onboarding: Activa la cuenta del usuario.
     * <p>
     * Este endpoint suele ser consumido cuando el usuario hace clic en el enlace de su correo
     * o introduce manualmente el código numérico recibido. Valida que el código exista
     * y no haya expirado.
     * </p>
     *
     * @param token El código de activación (ej. "849201") pasado como parámetro en la URL (?token=...).
     * @throws MessagingException Si el token expiró y el sistema decide enviar un nuevo correo automáticamente.
     */
    @GetMapping("/activate-account")
    public void confirm(
            @RequestParam String token
    ) throws MessagingException {
        service.activateAccount(token);
    }

}
