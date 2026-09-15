package com.project.website.DTO.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)


public class UserRequest {

    @NotBlank
    String name;

    @Email
    String email;

    @Size(min = 6)
    String password;
}