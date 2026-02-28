package com.example.demo.email;

import lombok.Getter;

/**
 * Registro centralizado (Enum) de las plantillas de correo electrónico del sistema.
 * <p>
 * Mapea acciones lógicas del negocio (ej. Activar Cuenta, Recuperar Contraseña)
 * a los nombres exactos de los archivos HTML ubicados en la carpeta 'resources/templates'.
 * El uso de este enum previene errores tipográficos (typos) y facilita el
 * mantenimiento si los nombres de los archivos cambian en el futuro.
 * </p>
 */
@Getter
public enum EmailTemplateName {

    /**
     * Plantilla utilizada para el paso 1 del Onboarding.
     * <p>
     * NOTA DE ARQUITECTURA: El valor "null_activation_account" corresponde exactamente
     * al archivo "null_activation_account.html" en la carpeta de templates.
     * Thymeleaf se encarga de añadirle la extensión '.html' automáticamente por debajo.
     * </p>
     */
    ACTIVATE_ACCOUNT("null_activation_account");

    /**
     * El nombre base del archivo HTML de la plantilla (sin la extensión .html).
     */
    private final String name;

    EmailTemplateName(String name) {
        this.name = name;
    }
}