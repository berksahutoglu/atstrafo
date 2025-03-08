package com.example.talepyonetimbackend.talepyonetim.controller;

import com.example.talepyonetimbackend.talepyonetim.dto.LoginRequest;
import com.example.talepyonetimbackend.talepyonetim.dto.LoginResponse;
import com.example.talepyonetimbackend.talepyonetim.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }
}
