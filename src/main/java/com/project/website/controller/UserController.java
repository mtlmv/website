package com.project.website.controller;

import com.project.website.DTO.User.PasswordChangeRequest;
import com.project.website.DTO.User.RegisterRequest;
import com.project.website.DTO.User.UserRequest;
import com.project.website.DTO.User.UserResponse;
import com.project.website.DTO.User.UserResponseId;
import com.project.website.entity.UserAvatar;
import com.project.website.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor

public class UserController {

    private final UserService userService;

    @PostMapping
    public UserResponse create(@RequestBody @Valid RegisterRequest request) {
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
                               @RequestBody @Valid UserRequest request,
                               Authentication authentication) {
        return userService.update(id, request, authentication);
    }

    /**
     * Форма, а не JSON: так файл едет в исходном виде, без раздувания
     * на треть, как было бы при base64.
     */
    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponse uploadAvatar(@PathVariable Long id,
                                     @RequestParam("image") MultipartFile image,
                                     Authentication authentication) {
        return userService.uploadAvatar(id, image, authentication);
    }

    @DeleteMapping("/{id}/avatar")
    public String deleteAvatar(@PathVariable Long id, Authentication authentication) {
        return userService.deleteAvatar(id, authentication);
    }

    /**
     * Отдаётся без токена: тег img не умеет слать заголовок Authorization.
     * Кэш короткий, в отличие от картинок постов: адрес аватара не меняется,
     * поэтому сутки кэша означали бы сутки старого фото после замены.
     */
    @GetMapping("/{id}/avatar")
    public ResponseEntity<byte[]> getAvatar(@PathVariable Long id) {
        UserAvatar avatar = userService.getAvatar(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(avatar.getContentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic())
                .body(avatar.getData());
    }

    @PutMapping("/{id}/password")
    public String changePassword(@PathVariable Long id,
                                 @RequestBody @Valid PasswordChangeRequest request,
                                 Authentication authentication) {
        return userService.changePassword(id, request, authentication);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        return userService.delete(id);
    }
}
