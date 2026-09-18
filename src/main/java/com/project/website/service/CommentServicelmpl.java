package com.project.website.service;

import com.project.website.DTO.Comment.CommentCreateRequest;
import com.project.website.DTO.Comment.CommentUpdateRequest;
import com.project.website.entity.CommentImage;
import com.project.website.enums.Role;
import com.project.website.repository.CommentImageRepo;
import org.springframework.http.HttpStatus;
import com.project.website.DTO.Comment.CommentResponse;
import com.project.website.entity.Comment;
import com.project.website.entity.Post;
import com.project.website.entity.User;
import com.project.website.exeption.ForbiddenException;
import com.project.website.repository.CommentRepo;
import com.project.website.repository.PostRepo;
import com.project.website.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor

public class CommentServicelmpl implements CommentService{
    private final CommentRepo commentRepo;
    private final CommentImageRepo commentImageRepo;
    private final UserRepo userRepo;
    private final PostRepo postRepo;
    private final ImageValidator imageValidator;

    private @NotNull CommentResponse convertToResponse(@NotNull Comment comment){
        CommentResponse response = new CommentResponse();

        response.setId(comment.getId());
        response.setText(comment.getText());
        response.setCreatedAt(comment.getCreatedAt());
        response.setAuthorId(comment.getAuthor().getId());
        response.setAuthorName(comment.getAuthor().getName());
        response.setHasImage(comment.isHasImage());
        return response;
    }


    @Override
    @Transactional
    public CommentResponse created(CommentCreateRequest request, MultipartFile image, Authentication authentication) {
        String email = authentication.getName();

        User author = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = postRepo.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));

        boolean withImage = image != null && !image.isEmpty();
        if (withImage) {
            imageValidator.validate(image);
        }

        Comment comment = Comment.builder()
                .text(request.getText())
                .createdAt(LocalDateTime.now())
                .author(author)
                .post(post)
                .hasImage(withImage)
                .build();
        Comment savedComment = commentRepo.save(comment);

        if (withImage) {
            try {
                commentImageRepo.save(CommentImage.builder()
                        .commentId(savedComment.getId())
                        .contentType(image.getContentType())
                        .data(image.getBytes())
                        .build());
            } catch (IOException e) {
                throw new RuntimeException("Не удалось прочитать файл изображения");
            }
        }

        return convertToResponse(savedComment);
    }

    @Override
    public CommentImage getImage(Long commentId) {
        return commentImageRepo.findById(commentId)
                .orElseThrow(() -> new RuntimeException("У этого комментария нет изображения"));
    }

    @Override
    public List<CommentResponse> getCommentsByPost(Long postId){
        postRepo.findById(postId)
                .orElseThrow(()->
                        new RuntimeException("Post not found"));

        return commentRepo.findByPostId(postId)
                .stream()
                .map(this :: convertToResponse)
                .toList();
    }

    @Override
    public CommentResponse updateComments(
            Long id,
            CommentUpdateRequest request,
            Authentication authentication)
    {
        String email = authentication.getName();
        User correntUser = userRepo.findByEmail(email)
                .orElseThrow(()->
                        new RuntimeException("User not found")
                );
        Comment comment = commentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        boolean isOwner =
            comment.getAuthor().getId()
                    .equals(correntUser.getId());

        boolean isAdmin =
                correntUser.getRole() == Role.ADMIN;

        if(!isOwner && !isAdmin){
            throw new ForbiddenException(
                    HttpStatus.FORBIDDEN,
                    "You can update only your own comments"
            );
        }
        if (request.getText() != null){
            comment.setText(request.getText());
        }
        Comment update = commentRepo.save(comment);
        return convertToResponse(update);
    }

    @Override
    @Transactional
    public String delete(Long id, Authentication authentication) {
        String email = authentication.getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(()->
                        new RuntimeException("User not found")
                );
        Comment comment = commentRepo.findById(id)
                        .orElseThrow(() -> new RuntimeException("Comment not found"));

        boolean isOwner =
                comment.getAuthor().getId()
                                .equals(currentUser.getId());
        boolean isAdmin =
                currentUser.getRole() == Role.ADMIN;

        if(!isOwner && !isAdmin){
            throw new ForbiddenException(
                    HttpStatus.FORBIDDEN,
                    "You can delete only your own comments"
            );
        }
        // Иначе картинка осталась бы в базе навсегда, ни на что не ссылаясь
        if (comment.isHasImage()) {
            commentImageRepo.deleteById(comment.getId());
        }
        commentRepo.delete(comment);
        return "Comment delete";
    }

}
