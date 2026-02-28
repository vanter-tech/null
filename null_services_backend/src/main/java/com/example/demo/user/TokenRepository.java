package com.example.demo.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad {@link Token}.
 * <p>
 * Gestiona las operaciones de base de datos para los códigos de activación
 * generados durante el registro de nuevos usuarios.
 * </p>
 */
public interface TokenRepository extends JpaRepository<Token, Integer> {

    /**
     * Busca un registro de token específico utilizando su valor en texto (el código).
     * <p>
     * Este método se utiliza en el flujo de activación de cuenta cuando el usuario
     * ingresa el código que recibió por correo electrónico.
     * </p>
     *
     * @param token El código de activación exacto (ej. "849201") a buscar.
     * @return Un {@link Optional} que contiene la entidad Token si el código existe.
     * Si el usuario ingresa un código inventado o incorrecto, devuelve un Optional vacío,
     * lo que permite al servicio lanzar una excepción de "Token Inválido" de forma segura
     * sin causar un NullPointerException.
     */
    Optional<Token> findByToken(String token);
}