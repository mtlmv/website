package com.project.website.service;

import com.project.website.DTO.User.UserRequest;
import com.project.website.DTO.User.UserResponse;
import com.project.website.entity.User;
import com.project.website.mapper.UserMapper;
import com.project.website.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import com.project.website.DTO.User.UserResponseId;
import com.project.website.enums.Role;

@Service
@RequiredArgsConstructor

public class UserServiceImpl implements UserService{

    private final UserRepo userRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse create(UserRequest request) {

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        User saved = userRepo.save(user);

        return userMapper.toResponse(saved);
    }

    @Override
    public List<UserResponse> getAll() {

        return userRepo.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public UserResponseId getById(Long id) {

        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return userMapper.toResponseId(user);
    }

    @Override
    public UserResponse update(Long id, UserRequest request) {

        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        if(request.getPassword() != null && !request.getPassword().isBlank()){
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updated = userRepo.save(user);

        return userMapper.toResponse(updated);
    }

    @Override
    public String delete(Long id) {

        userRepo.deleteById(id);

        return "User deleted";
    }
}
