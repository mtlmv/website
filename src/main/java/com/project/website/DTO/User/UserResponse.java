package com.project.website.DTO.User;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.Data;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)

public class UserResponse {
    Long id;
    String name;
    String email;
}

