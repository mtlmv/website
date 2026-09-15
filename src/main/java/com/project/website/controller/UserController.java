package com.project.website.controller;

import com.project.website.DTO.User.UserRequest;
import com.project.website.DTO.User.UserResponse;
import com.project.website.DTO.User.UserResponseId;
import com.project.website.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor

public class UserController {

    private final UserService userService;

    @PostMapping
    public UserResponse create(@RequestBody @Valid UserRequest request) {
        return userService.create(request);
    }

    @GetMapping
    public List<UserResponse> getAll() {
        return userService.getAll();
    }

    @GetMapping("/{id}")
    public UserResponseId getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id,
                               @RequestBody @Valid UserRequest request) {
        return userService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        return userService.delete(id);
    }
}
