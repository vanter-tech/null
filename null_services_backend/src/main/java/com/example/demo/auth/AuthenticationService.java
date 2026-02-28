package com.example.demo.auth;

import com.example.demo.email.EmailService;
import com.example.demo.email.EmailTemplateName;
import com.example.demo.role.RoleRepository;
import com.example.demo.security.JwtService;
import com.example.demo.user.*;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

/**
 * Servicio central de Gestión de Identidad y Acceso (IAM).
 * <p>
 * Orquesta la lógica de negocio para el registro de nuevos usuarios, la validación
 * mediante correos electrónicos (OTP - One Time Passwords) y la emisión de tokens JWT
 * para el inicio de sesión.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;

    /**
     * Registra a un nuevo usuario en la base de datos y dispara el proceso de validación.
     * <p>
     * NOTA DE ARQUITECTURA: El usuario se crea bloqueado por defecto (enable = false).
     * No podrá hacer login ni usar el sistema de WebSockets hasta que valide su correo.
     * </p>
     *
     * @param request DTO con los datos del formulario de registro.
     * @throws MessagingException Si falla el envío del correo electrónico.
     */
    public void register(RegistrationRequest request) throws MessagingException {
        var userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("User Role Not Found"));

        var user = User.builder()
                .email(request.getEmail())
                .fullname(request.getFullname())
                .nickName(request.getNickName())
                // Hasheamos la contraseña antes de guardarla (NUNCA texto plano)
                .password(passwordEncoder.encode(request.getPassword()))
                .dateOfBirth(request.getDateOfBirth())
                .accountLocked(false)
                .enable(false) // Requiere activación por email
                .roles(List.of(userRole))
                .build();

        userRepository.save(user);
        sendValidationEmail(user);
    }

    /**
     * Helper method: Coordina la creación del token de 6 dígitos y el envío del correo.
     */
    private void sendValidationEmail(User user) throws MessagingException {
        var newToken = generateAndSaveActivationToken(user);
        emailService.sendEmail(
                user.getEmail(),
                user.getFullname(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl,
                newToken,
                "Account Activation"
        );
    }

    /**
     * Genera un token de activación con una vida útil de 15 minutos y lo asocia al usuario.
     */
    private String generateAndSaveActivationToken(User user) {
        String generateToken = generateActivationCode(6);
        var token = Token.builder()
                .token(generateToken)
                .createdAt(LocalDateTime.now())
                // El token expira estrictamente en 15 minutos por seguridad
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();
        tokenRepository.save(token);
        return generateToken;
    }

    /**
     * Genera un código numérico aleatorio criptográficamente seguro.
     *
     * @param length La longitud del código (ej. 6 dígitos).
     * @return El código en formato String.
     */
    private String generateActivationCode(int length) {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        // Usamos SecureRandom en lugar de Math.random() para evitar vulnerabilidades de predicción
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int randomIndex =  random.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }

        return codeBuilder.toString();
    }

    /**
     * Autentica al usuario comprobando sus credenciales y genera su token de sesión (JWT).
     *
     * @param request DTO con el email y la contraseña.
     * @return DTO con el JWT generado y datos básicos del usuario para la UI.
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request){
        // Esto delega la validación de la contraseña al UserDetailsService y PasswordEncoder
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        var claims = new HashMap<String, Object>();
        var user = ((User) auth.getPrincipal());

        // ⚠️ IMPORTANTE PARA EL CHAT: Inyectamos el ID del usuario en el token.
        // El frontend extraerá este 'userId' para usarlo en el motor de WebSockets.
        claims.put("username", user.getUsername());
        claims.put("userId", user.getId());

        var jwtToken = jwtService.generateToken(claims, user);

        // Cambiamos el estado a ONLINE al hacer login
        user.setStatus(UserStatus.ONLINE);
        userRepository.save(user); // Guardamos el cambio en la base de datos

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .nickname(user.getNickName())
                .email(user.getEmail())
                .status(user.getStatus())
                .build();
    }

    /**
     * Valida el código OTP ingresado por el usuario y habilita su cuenta si es correcto.
     *
     * @param token El código numérico de 6 dígitos.
     * @throws MessagingException Si el token expiró (reenvía un correo nuevo automáticamente).
     */
    public void activateAccount(String token) throws MessagingException {
        Token savedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid Token"));

        // Lógica UX: Si se le pasó el tiempo, le enviamos otro código automáticamente
        // en lugar de obligarlo a hacer clic en un botón de "Reenviar correo".
        if(LocalDateTime.now().isAfter(savedToken.getExpiresAt())){
            sendValidationEmail(savedToken.getUser());
            throw new RuntimeException("Activation Token Expired. A new one has been sent.");
        }

        var user = userRepository.findById(savedToken.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        // Habilitamos al usuario para que pueda hacer login
        user.setEnable(true);
        userRepository.save(user);

        // Quemamos el token marcando su fecha de uso (Auditoría / Prevención de reúso)
        savedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(savedToken);
    }
}