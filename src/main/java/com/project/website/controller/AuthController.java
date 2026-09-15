package com.project.website.controller;


import com.project.website.DTO.Auth.LoginRequest;
import com.project.website.DTO.Auth.LoginResponse;
import com.project.website.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody @Valid LoginRequest request){
        return authService.login(request);
    }
}
