package com.example.demo.server;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO (Data Transfer Object) para la creación y actualización de Servidores.
 * <p>
 * Define los campos necesarios que el cliente (Frontend) debe enviar al servidor
 * para dar de alta una nueva comunidad. Incluye validaciones básicas para asegurar
 * que la información mínima requerida esté presente.
 * </p>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServerRequest {

    /**
     * El nombre identificativo del servidor.
     * <p>
     * Se utiliza @NotBlank para asegurar que el nombre no sea nulo, no esté vacío
     * y no contenga únicamente espacios en blanco, garantizando que cada servidor
     * sea visible y seleccionable en la UI.
     * </p>
     */
    @NotBlank(message = "Server's name is required")
    private String name;

    /**
     * Ruta o URL de la imagen de perfil del servidor.
     * <p>
     * Este campo es opcional en el envío; si no se proporciona, el sistema
     * podría asignar una imagen por defecto o un placeholder con las iniciales
     * del nombre del servidor en el Frontend.
     * </p>
     */
    private String imageUrl;

}