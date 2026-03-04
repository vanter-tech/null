package com.example.demo.server;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChannelRequest {
    @NotBlank(message = "El nombre del canal es obligatorio")
    private String name;

    // Opcional, por si en el futuro quieres crear canales de voz desde el frontend
    @Builder.Default
    private String type = "TEXT";

    private Boolean isPrivate;
}