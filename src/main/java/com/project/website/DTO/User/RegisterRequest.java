package com.project.website.DTO.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * Регистрация отделена от редактирования профиля: здесь пароль обязателен,
 * а в UserRequest его нет вовсе — он меняется через отдельный эндпоинт
 * с подтверждением старого пароля.
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest {

    @NotBlank(message = "Имя не может быть пустым")
    String name;

    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$")
    String username;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный email")
    String email;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 6, message = "Пароль должен быть не короче 6 символов")
    String password;
}
