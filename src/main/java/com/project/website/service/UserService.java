package com.project.website.service;

import com.project.website.DTO.User.UserRequest;
import com.project.website.DTO.User.UserResponse;
import com.project.website.DTO.User.UserResponseId;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public interface UserService {
    UserResponse create(UserRequest request);

    List<UserResponse> getAll();

    UserResponseId getById(Long id);

    UserResponse update(Long id, UserRequest request);

    String delete(Long id);

}
