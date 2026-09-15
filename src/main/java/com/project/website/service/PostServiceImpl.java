package com.project.website.service;

import com.project.website.DTO.Comment.CommentResponse;
import com.project.website.DTO.Post.PostRequest;
import com.project.website.DTO.Post.PostResponse;
import com.project.website.DTO.Post.PostResponseId;
import com.project.website.entity.Post;
import com.project.website.entity.User;
import com.project.website.enums.Role;
import com.project.website.exeption.ForbiddenException;
import com.project.website.repository.CommentRepo;
import com.project.website.repository.LikeRepo;
import com.project.website.repository.PostRepo;
import com.project.website.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.project.website.entity.Comment;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor

public class PostServiceImpl implements PostService {
    private final PostRepo postRepo;
    private final UserRepo userRepo;
    private final LikeRepo likeRepo;
    private final CommentRepo commentRepo;


    private CommentResponse convertCommentToResponse(Comment comment) {

        CommentResponse response = new CommentResponse();

        response.setId(comment.getId());
        response.setText(comment.getText());
        response.setAuthorId(comment.getAuthor().getId());
        response.setAuthorName(comment.getAuthor().getName());
        response.setCreatedAt(comment.getCreatedAt());

        return response;
    }
    private @NotNull PostResponse convertToResponse(
            @NotNull Post post,
            Authentication authentication) {
        PostResponse response = new PostResponse();

        response.setId(post.getId());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setAuthorId(post.getAuthor().getId());
        response.setAuthorName(post.getAuthor().getName());
        response.setCreatedAt(post.getCreatedAt());

        //КОЛИЧЕСТВО ЛАЙКОВ
        long likesCount = likeRepo.countByPostId(post.getId());
        response.setLikesCount(likesCount);

        //ТЕКУЩИЙ ПОЛЬЗОВАТЕЛЬ
        String email = authentication.getName();

        User user = userRepo.findByEmail(email)
                .orElseThrow(()->
                        new RuntimeException("User not found"));

        //ПРОВЕРЯЕМ ЛАЙКИ
        boolean liked = likeRepo
                .findByUserIdAndPostId(
                        user.getId(),
                        post.getId()
                )
                .isPresent();
        response.setLiked(liked);

        //КОМЕНТАРИИ
        List<CommentResponse> comments = commentRepo
                .findByPostId(post.getId())
                .stream()
                .map(this::convertCommentToResponse)
                .toList();
        response.setComments(comments);
        return response;
    }

    private @NotNull PostResponseId convertToResponseId(@NotNull Post postId) {
        PostResponseId responseId = new PostResponseId();

        responseId.setTitle(postId.getTitle());
        responseId.setContent(postId.getContent());
        responseId.setAuthorName(postId.getAuthor().getName());
        responseId.setCreatedAt(postId.getCreatedAt());
        return responseId;
    }

//CREATE
    @Override
    public PostResponse create(
            @NotNull PostRequest request,
            Authentication authentication)
    {
        String email = authentication.getName();
        User author = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(author)
                .createdAt(LocalDateTime.now())
                .build();

        Post savedPost = postRepo.save(post);
        return convertToResponse(savedPost, authentication);
    }

//GET BY ID
@Override
public PostResponse getPostById(Long id, Authentication authentication) {
    Post post = postRepo.findById(id)
            .orElseThrow(()->
                    new RuntimeException("Post not found"));
    return convertToResponse(post, authentication);
}
//GET ALL
    @Override
    public List<PostResponse> getAllPosts (
            Authentication authentication
    ) {
        List<Post> posts = postRepo.findAll();

        return posts.stream()
                .map(post -> convertToResponse(post, authentication))
                .toList();
    }

    //GET ALL ID
    @Override
    public List<PostResponseId> getAllPostsId() {
        List<Post> posts = postRepo.findAll();
        return posts.stream()
                .map(this::convertToResponseId)
                .toList();
    }

    //UPDATE
    @Override
    public PostResponseId updatePost(
            @NotNull PostRequest request,
            Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Post post = postRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Post not found"));

        //ПРОВЕРКА ВЛАДЕЛЬЦА
        boolean isOwner =
                post.getAuthor().getId().equals(currentUser.getId());

        //ПРОВЕРЯЕМ ADMIN
        boolean isAdmin =
                currentUser.getRole() == Role.ADMIN;

        //ЕСЛИ НЕ ВЛАДЕЛЕЦ ИЛИ НЕ ADMIN
        if (!isOwner && !isAdmin) {
            throw new ForbiddenException(
                    HttpStatus.FORBIDDEN, "You can update only your own posts"
            );
        }
        //ОБНОВЛЯЕМ TITLE
        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
        }
        //ОБНОВЛЯЕМ CONTENT
        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }
        Post updated = postRepo.save(post);
        return convertToResponseId(updated);
    }
    //DELETE
    @Override
    @Transactional
    public PostResponseId deletePost (Long id, Authentication authentication){
        String email = authentication.getName();

        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Post post = postRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Post not found"));

        //ПРОВЕРКА ВЛАДЕЛЬЦА
        boolean isOwner =
                post.getAuthor().getId()
                        .equals(currentUser.getId());

        //ПРОВЕРКА ADMIN
        boolean isAdmin =
                currentUser.getRole() == Role.ADMIN;

        //ЕСЛИ НЕ ВЛАДЕЛЕЦ И НЕ ADMIN
        if (!isOwner && !isAdmin) {
            throw new ForbiddenException(
                    HttpStatus.FORBIDDEN, "You can delete only your own posts"
            );
        }
        PostResponseId response = convertToResponseId(post);

        commentRepo.deleteByPostId(id);
        likeRepo.deleteByPostId(id);
        postRepo.delete(post);

        return response;
    }
}
