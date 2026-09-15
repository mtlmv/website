package com.project.website.mapper;

import com.project.website.DTO.User.UserResponse;
import com.project.website.DTO.User.UserResponseId;
import com.project.website.entity.User;
import org.springframework.stereotype.Component;


@Component

public class UserMapper {
    public UserResponse toResponse(User user){

        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());

        return response;
    }

    public UserResponseId toResponseId(User user){

        UserResponseId response = new UserResponseId();

        response.setName(user.getName());
        response.setEmail(user.getEmail());

        return response;
    }
}
