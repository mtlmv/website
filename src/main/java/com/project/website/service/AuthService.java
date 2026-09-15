package com.project.website.service;

import com.project.website.DTO.Auth.LoginRequest;
import com.project.website.DTO.Auth.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}
