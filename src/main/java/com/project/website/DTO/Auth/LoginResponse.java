package com.project.website.DTO.Auth;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginResponse {
    Long id;
    String name;
    String username;
    boolean hasAvatar;
    String email;
    String role;
    String token;


}
