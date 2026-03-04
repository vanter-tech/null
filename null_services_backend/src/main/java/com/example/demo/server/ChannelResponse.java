package com.example.demo.server;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChannelResponse {
    private Long id;
    private String name;
    private String type;
    private Boolean isPrivate;
}