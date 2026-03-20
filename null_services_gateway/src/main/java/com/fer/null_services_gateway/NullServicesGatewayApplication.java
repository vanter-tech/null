package com.fer.null_services_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal que inicia la aplicación de Spring Boot.
 * Esta clase actúa como el punto de entrada
 * (entry point) para el microservicio
 * de Gateway, configurando el contexto de
 * la aplicación y los beans necesarios.
 */
@SpringBootApplication
public class NullServicesGatewayApplication {

    /**
     * Constructor privado para ocultar
     * el constructor público implícito.
     * Según las reglas de diseño de Checkstyle,
     * las clases de utilidad o de entrada
     * no deben ser instanciadas.
     */
    protected NullServicesGatewayApplication() {
        // Constructor protegido para
        // cumplir con las reglas de diseño.
    }

    /**
     * Método principal que lanza la ejecución de la aplicación.
     *
     * @param args Argumentos de la línea
     *                de comandos pasados al iniciar.
     */
    public static void main(final String[] args) {
        SpringApplication.run(NullServicesGatewayApplication.class, args);
    }

}
