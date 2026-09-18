package com.project.website.service;

import com.project.website.DTO.User.UserRequest;
import com.project.website.DTO.User.UserResponse;
import com.project.website.entity.Comment;
import com.project.website.entity.Post;
import com.project.website.entity.User;
import com.project.website.exeption.ForbiddenException;
import com.project.website.mapper.UserMapper;
import com.project.website.repository.CommentImageRepo;
import com.project.website.repository.CommentRepo;
import com.project.website.repository.LikeRepo;
import com.project.website.repository.PostImageRepo;
import com.project.website.repository.PostRepo;
import com.project.website.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.project.website.DTO.User.UserResponseId;
import com.project.website.enums.Role;

@Service
@RequiredArgsConstructor

public class UserServiceImpl implements UserService{

    private final UserRepo userRepo;
    private final PostRepo postRepo;
    private final PostImageRepo postImageRepo;
    private final CommentRepo commentRepo;
    private final CommentImageRepo commentImageRepo;
    private final LikeRepo likeRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse create(UserRequest request) {

        // @Size(min=6) пропускает null, поэтому обязательность проверяем здесь:
        // тот же DTO используется при обновлении профиля, где пустой пароль
        // означает «не менять»
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new RuntimeException("Пароль обязателен при регистрации");
        }
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Этот email уже занят");
        }

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
    public UserResponse update(Long id, UserRequest request, Authentication authentication) {

        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User currentUser = userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Без этой проверки любой вошедший мог сменить пароль чужому аккаунту
        // и войти под ним
        boolean isOwner = user.getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new ForbiddenException(
                    HttpStatus.FORBIDDEN, "Можно редактировать только свой профиль"
            );
        }

        // Почта — логин, поэтому занятую чужим аккаунтом ставить нельзя
        if (!user.getEmail().equals(request.getEmail())
                && userRepo.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Этот email уже занят");
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        if(request.getPassword() != null && !request.getPassword().isBlank()){
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updated = userRepo.save(user);

        return userMapper.toResponse(updated);
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

        userRepo.delete(user);

        return "User deleted";
    }
}
