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

    @NotBlank(message = "Имя не может быть пустым")
    String name;

    // @Email без @NotBlank пропускает пустую строку — нужны оба
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный email")
    String email;

    // Пустой пароль допустим только при обновлении профиля («не менять»).
    // При регистрации его обязательность проверяется в UserServiceImpl.create
    @Size(min = 6, message = "Пароль должен быть не короче 6 символов")
    String password;
}
