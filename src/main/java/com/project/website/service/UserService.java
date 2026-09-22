package com.project.website.service;

import com.project.website.DTO.User.PasswordChangeRequest;
import com.project.website.DTO.User.RegisterRequest;
import com.project.website.DTO.User.UserRequest;
import com.project.website.DTO.User.UserResponse;
import com.project.website.DTO.User.UserResponseId;
import com.project.website.entity.UserAvatar;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service

public interface UserService {
    UserResponse create(RegisterRequest request);

    List<UserResponse> getAll();

    UserResponseId getById(Long id);

    UserResponse update(Long id, UserRequest request, Authentication authentication);

    UserResponse uploadAvatar(Long id, MultipartFile image, Authentication authentication);

    String deleteAvatar(Long id, Authentication authentication);

    UserAvatar getAvatar(Long id);

    String changePassword(Long id, PasswordChangeRequest request, Authentication authentication);

    String delete(Long id);

}
