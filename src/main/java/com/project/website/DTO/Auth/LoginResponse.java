package com.project.website.DTO.Auth;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor

@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginResponse {
    Long id;
    String name;
    String email;
    String role;
    String token;


}
