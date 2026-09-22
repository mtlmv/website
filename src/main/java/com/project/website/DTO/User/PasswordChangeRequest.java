package com.project.website.DTO.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PasswordChangeRequest {

    // Старый пароль по длине не проверяем: он уже существует каким есть,
    // нам нужно лишь убедиться, что его прислали
    @NotBlank(message = "Введите текущий пароль")
    String currentPassword;

    @NotBlank(message = "Введите новый пароль")
    @Size(min = 6, message = "Пароль должен быть не короче 6 символов")
    String newPassword;
}
