package com.project.website.service;

import com.project.website.DTO.User.PasswordChangeRequest;
import com.project.website.DTO.User.RegisterRequest;
import com.project.website.DTO.User.UserRequest;
import com.project.website.DTO.User.UserResponse;
import com.project.website.entity.Comment;
import com.project.website.entity.Post;
import com.project.website.entity.User;
import com.project.website.entity.UserAvatar;
import com.project.website.exeption.ForbiddenException;
import com.project.website.mapper.UserMapper;
import com.project.website.repository.CommentImageRepo;
import com.project.website.repository.CommentRepo;
import com.project.website.repository.LikeRepo;
import com.project.website.repository.PostImageRepo;
import com.project.website.repository.PostRepo;
import com.project.website.repository.UserAvatarRepo;
import com.project.website.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import com.project.website.DTO.User.UserResponseId;
import com.project.website.enums.Role;

@Service
@RequiredArgsConstructor

public class UserServiceImpl implements UserService{

    private final UserRepo userRepo;
    private final UserAvatarRepo userAvatarRepo;
    private final PostRepo postRepo;
    private final PostImageRepo postImageRepo;
    private final CommentRepo commentRepo;
    private final CommentImageRepo commentImageRepo;
    private final LikeRepo likeRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ImageValidator imageValidator;

    /**
     * Профиль правит только его владелец (админ — любой): без этой проверки
     * любой вошедший мог переписать чужие данные или подменить фото.
     */
    private User requireOwner(Long id, Authentication authentication) {

        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User currentUser = userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isOwner = user.getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new ForbiddenException(
                    HttpStatus.FORBIDDEN, "Можно редактировать только свой профиль"
            );
        }

        return user;
    }

    @Override
    public UserResponse create(RegisterRequest request) {

        if (userRepo.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Этот email уже занят");
        }
        if (userRepo.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Это имя пользователя уже занято");
        }

        User user = new User();

        user.setName(request.getName());
        user.setUsername(request.getUsername());
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
    public UserResponse update(Long id, UserRequest request, Authentication authentication) {

        User user = requireOwner(id, authentication);

        // Почта — логин, поэтому занятую чужим аккаунтом ставить нельзя
        if (!user.getEmail().equals(request.getEmail())
                && userRepo.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Этот email уже занят");
        }

        // Имя пользователя тоже должно быть уникальным: по нему на профиль ссылаются
        if (!Objects.equals(user.getUsername(), request.getUsername())
                && userRepo.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Это имя пользователя уже занято");
        }

        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setBirthDate(request.getBirthDate());
        user.setBio(request.getBio());

        User updated = userRepo.save(user);
        return userMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public UserResponse uploadAvatar(Long id, MultipartFile image, Authentication authentication) {

        User user = requireOwner(id, authentication);

        // Без этой проверки getBytes() вернул бы пустой массив и получился бы
        // «аватар есть», который не открывается
        if (image == null || image.isEmpty()) {
            throw new RuntimeException("Файл не выбран");
        }

        imageValidator.validate(image);

        try {
            // userId — первичный ключ, поэтому save перезаписывает прежний
            // аватар сам, удалять его отдельно не нужно
            userAvatarRepo.save(UserAvatar.builder()
                    .userId(user.getId())
                    .contentType(image.getContentType())
                    .data(image.getBytes())
                    .build());
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать файл");
        }

        user.setHasAvatar(true);

        return userMapper.toResponse(userRepo.save(user));
    }

    @Override
    @Transactional
    public String deleteAvatar(Long id, Authentication authentication) {

        User user = requireOwner(id, authentication);

        if (!user.isHasAvatar()) {
            throw new RuntimeException("Фото профиля не установлено");
        }

        userAvatarRepo.deleteById(user.getId());
        user.setHasAvatar(false);
        userRepo.save(user);

        return "Avatar deleted";
    }

    @Override
    public UserAvatar getAvatar(Long id) {

        return userAvatarRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Avatar not found"));
    }

    @Override
    public String changePassword(Long id, PasswordChangeRequest request, Authentication authentication) {

        User currentUser = userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Пароль меняет только сам владелец: даже админу нельзя, иначе он
        // получил бы вход в чужой аккаунт, не зная старого пароля
        if (!currentUser.getId().equals(id)) {
            throw new ForbiddenException(
                    HttpStatus.FORBIDDEN, "Можно менять только свой пароль"
            );
        }

        // Подтверждение старым паролём: иначе открытой вкладки хватало,
        // чтобы отобрать аккаунт насовсем
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new ForbiddenException(
                    HttpStatus.FORBIDDEN, "Текущий пароль неверный"
            );
        }

        if (passwordEncoder.matches(request.getNewPassword(), currentUser.getPassword())) {
            throw new RuntimeException("Новый пароль совпадает с текущим");
        }

        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepo.save(currentUser);

        return "Password changed";
    }

    @Override
    @Transactional
    public String delete(Long id) {

        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Порядок важен: сначала всё, что ссылается на пользователя и его посты,
        // иначе удаление упрётся во внешние ключи
        likeRepo.deleteByUserId(id);
        commentImageRepo.deleteAllById(
                commentRepo.findByAuthorId(id).stream().map(Comment::getId).toList());
        commentRepo.deleteByAuthorId(id);

        List<Post> ownPosts = postRepo.findByAuthorId(id);
        for (Post post : ownPosts) {
            // на его постах могли остаться лайки и комментарии других людей
            likeRepo.deleteByPostId(post.getId());
            commentImageRepo.deleteAllById(
                    commentRepo.findByPostId(post.getId()).stream().map(Comment::getId).toList());
            commentRepo.deleteByPostId(post.getId());
            if (post.isHasImage()) {
                postImageRepo.deleteById(post.getId());
            }
        }
        postRepo.deleteAll(ownPosts);

        if (user.isHasAvatar()) {
            userAvatarRepo.deleteById(id);
        }

        userRepo.delete(user);

        return "User deleted";
    }
}
