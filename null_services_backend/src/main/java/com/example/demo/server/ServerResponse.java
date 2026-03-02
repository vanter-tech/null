package com.example.demo.server;

import lombok.*;

import java.util.List;

/**
 * DTO (Data Transfer Object) optimizado para el envío de información de servidores al cliente.
 * <p>
 * Su objetivo principal es la proyección de datos: transforma la entidad interna {@link Server}
 * en un objeto ligero y seguro para ser consumido por el Frontend (Angular).
 * Al filtrar los campos, evitamos problemas de recursividad infinita (Circular References)
 * y protegemos la privacidad de los usuarios al no enviar la lista de miembros completa
 * en consultas generales.
 * </p>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServerResponse {

    /**
     * Identificador único del servidor.
     * Necesario para que el Frontend pueda realizar peticiones específicas (como unirse
     * o cargar los canales) de este servidor en particular.
     */
    private Long id;

    /**
     * Nombre público del servidor que se mostrará en las listas de descubrimiento
     * y en los tooltips de la barra lateral.
     */
    private String name;

    /**
     * URL de la imagen del servidor.
     * Se utiliza en Angular para renderizar los iconos circulares de la barra lateral.
     */
    private String imageUrl;

    /**
     * Lista de canales pertenecientes al servidor.
     * Mapeados a DTO para evitar recursividad y exponer solo lo necesario.
     */
    private List<ChannelResponse> channels;
}