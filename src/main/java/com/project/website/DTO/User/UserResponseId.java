package com.project.website.DTO.User;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)

public class UserResponseId {
    String name;
    String email;
}

