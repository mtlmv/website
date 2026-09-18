package com.project.website.service;

import com.project.website.DTO.Comment.CommentCreateRequest;
import com.project.website.DTO.Comment.CommentResponse;
import com.project.website.DTO.Comment.CommentUpdateRequest;
import com.project.website.entity.CommentImage;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CommentService {
    CommentResponse created(CommentCreateRequest request, MultipartFile image, Authentication authentication);

   List<CommentResponse> getCommentsByPost(Long postId);

    CommentResponse updateComments(Long id, CommentUpdateRequest request, Authentication authentication);

    String delete(Long id, Authentication authentication);

    CommentImage getImage(Long commentId);
}
