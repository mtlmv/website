package com.project.website.service;

import com.project.website.DTO.Auth.LoginRequest;
import com.project.website.DTO.Auth.LoginResponse;
import com.project.website.entity.User;
import com.project.website.repository.UserRepo;
import com.project.website.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean passworMatches =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPassword()
                );
        if(!passworMatches){
            throw new RuntimeException("Wrong Password!");
        }
        String token = jwtService.generateToken(user.getEmail());

        // Через builder, а не позиционный конструктор: name, username и email
        // идут подряд тремя String, и их перестановка компилятором не ловится
        return LoginResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .hasAvatar(user.isHasAvatar())
                .token(token)
                .build();
    }
}
