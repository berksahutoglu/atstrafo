package com.example.talepyonetimbackend.talepyonetim.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Kullanıcı adı gerekli")
    private String username;

    @NotBlank(message = "Şifre gerekli")
    private String password;
}
