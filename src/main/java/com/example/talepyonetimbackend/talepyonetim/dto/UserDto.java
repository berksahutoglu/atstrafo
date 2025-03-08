package com.example.talepyonetimbackend.talepyonetim.dto;

import com.example.talepyonetimbackend.talepyonetim.model.Role;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
}
