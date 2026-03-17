package com.example.demo.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Utiliza JavaMailSender para la transmisión y Thymeleaf
 * como motor de plantillas
 * para renderizar correos HTML dinámicos y atractivos,
 * incrustando imágenes directamente
 * en el cuerpo del mensaje.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    /**
     * Componente principal de Spring encargado de la conexión y el envío físico
     * de los correos electrónicos.
     */
    private final JavaMailSender mailSender;
    /**
     * Motor de plantillas (Thymeleaf) utilizado para procesar y generar
     * el contenido HTML de los correos de forma dinámica.
     */
    private final SpringTemplateEngine templateEngine;

    /**
     * Envía un correo electrónico con formato
     * HTML de manera asíncrona.
     * <p>
     * NOTA DE ARQUITECTURA (@Async): Esta anotación
     * delega la ejecución de este método
     * a un hilo (Thread) separado. Esto evita bloquear
     * el hilo principal de la petición HTTP
     * (ej. durante el registro), mejorando drásticamente
     * el tiempo de respuesta y la
     * experiencia de usuario (UX).
     * </p>
     *
     * @param to Correo electrónico del destinatario.
     * @param username Nombre del usuario para personalizar el saludo.
     * @param emailTemplate Enum que define qué plantilla
     * HTML de Thymeleaf usar.
     * @param confirmationUrl Enlace (URL) que el usuario
     * puede clicar para activar su cuenta.
     * @param activationCode Código OTP de 6 dígitos enviado
     * para validación manual.
     * @param subject Asunto del correo electrónico.
     * @throws MessagingException Si ocurre un error al construir el
     * mensaje multipart o al conectar con el servidor SMTP.
     */
    @Async
    public void sendEmail(
            final String to,
            final String username,
            final EmailTemplateName emailTemplate,
            final String confirmationUrl,
            final String activationCode,
            final String subject
    ) throws MessagingException {

        // 1. Selección de Plantilla
        String templateName;
        if (emailTemplate == null) {
            templateName = "confirm-url"; // Plantilla por defecto de seguridad
        } else {
            templateName = emailTemplate.getName();
        }

        // 2. Configuración del Mensaje Multipart
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        // MULTIPART_MODE_MIXED es crucial para permitir
        // tanto contenido HTML como imágenes incrustadas (inline)
        MimeMessageHelper helper = new MimeMessageHelper(
                mimeMessage,
                MimeMessageHelper.MULTIPART_MODE_MIXED,
                StandardCharsets.UTF_8.name()
        );

        // 3. Preparación de Variables para Thymeleaf
        Map<String, Object> properties = new HashMap<>();
        properties.put("username", username);
        properties.put("confirmationUrl", confirmationUrl);
        properties.put("activationCode", activationCode);

        // El Context es el puente que pasa nuestras
        // variables de Java al archivo HTML de Thymeleaf
        Context context = new Context();
        context.setVariables(properties);

        // 4. Construcción del Correo
        // El remitente oficial del sistema
        helper.setFrom("noreply@nullservices.com");
        helper.setTo(to);
        helper.setSubject(subject);

        // Renderizamos el HTML inyectando las variables
        String template = templateEngine.process(templateName, context);
        // El 'true' indica que el contenido es HTML
        // y no texto plano
        helper.setText(template, true);

        // 5. Inyección de Imagen Inline (Incrustada)
        // Buscamos el logo en la carpeta resources/static/images
        ClassPathResource logoImage = new ClassPathResource(
                "static/images/null_imagotipo_activation_html.png"
        );
        // 'logoNull' es el CID (Content-ID) que debe coincidir
        // con el 'src="cid:logoNull"' en el archivo HTML
        helper.addInline("logoNull", logoImage);

        // 6. Envío final al servidor SMTP (ej. Gmail, Mailtrap, AWS SES)
        mailSender.send(mimeMessage);
    }
}
