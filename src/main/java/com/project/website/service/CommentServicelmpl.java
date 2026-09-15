package com.project.website.service;

import com.project.website.DTO.Comment.CommentCreateRequest;
import com.project.website.DTO.Comment.CommentUpdateRequest;
import com.project.website.enums.Role;
import org.springframework.http.HttpStatus;
import com.project.website.DTO.Comment.CommentRequest;
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

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor

public class CommentServicelmpl implements CommentService{
    private final CommentRepo commentRepo;
    private final UserRepo userRepo;
    private final PostRepo postRepo;

    private @NotNull CommentResponse convertToResponse(@NotNull Comment comment){
        CommentResponse response = new CommentResponse();

        response.setId(comment.getId());
        response.setText(comment.getText());
        response.setCreatedAt(comment.getCreatedAt());
        response.setAuthorId(comment.getAuthor().getId());
        response.setAuthorName(comment.getAuthor().getName());
        return response;
    }


    @Override
    public CommentResponse created(CommentCreateRequest request, Authentication authentication) {
        String email = authentication.getName();

        User author = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = postRepo.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = Comment.builder()
                .text(request.getText())
                .createdAt(LocalDateTime.now())
                .author(author)
                .post(post)
                .build();
        Comment savedComment = commentRepo.save(comment);
        return convertToResponse(savedComment);
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
        commentRepo.delete(comment);
        return "Comment delete";
    }

}
