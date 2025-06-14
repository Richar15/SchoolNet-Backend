package com.RichardDev.SchoolNet.presentation.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private String username;
    private String rol;
    private Long userId;

    private boolean error;
    private String mensaje;
    private int status;
    private LocalDateTime timestamp;

}
