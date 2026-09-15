package com.project.website.controller;

import com.project.website.DTO.Comment.CommentCreateRequest;
import com.project.website.DTO.Comment.CommentRequest;
import com.project.website.DTO.Comment.CommentResponse;
import com.project.website.DTO.Comment.CommentUpdateRequest;
import com.project.website.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public CommentResponse create(@RequestBody @Valid CommentCreateRequest request, Authentication authentication){
        return commentService.created(request, authentication);
    }

    @GetMapping("/post/{postId}")
    public List<CommentResponse> getCommentsByPost(@PathVariable Long postId){
        return commentService.getCommentsByPost(postId);
    }

    @PutMapping("/{id}")
    public CommentResponse updateComments(
            @PathVariable Long id,
            @RequestBody @Valid CommentUpdateRequest request,
            Authentication authentication){
        return commentService.updateComments(id, request, authentication);
    }

    @DeleteMapping("/{id}")
    public String delete(
            @PathVariable Long id,
            Authentication authentication){
        return commentService.delete(
                id,
                authentication);
    }
}
