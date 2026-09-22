package com.project.website.DTO.User;

import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)

public class UserResponseId {
    Long id;
    String name;
    String username;
    LocalDate birthDate;
    String bio;
    String email;
    boolean hasAvatar;
}

