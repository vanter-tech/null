package com.example.demo.server;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberResponse {
    private Integer id; // O el tipo de dato que uses para el ID de User
    private String username; // O email, o nombre a mostrar
    private String imageUrl; // Para su avatar
    private String status;
}