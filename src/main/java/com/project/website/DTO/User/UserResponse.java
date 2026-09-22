package com.project.website.DTO.User;

import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.Data;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)

public class UserResponse {
    Long id;
    String name;
    String username;
    String bio;
    LocalDate birthDate;
    String email;
    boolean hasAvatar;
}

