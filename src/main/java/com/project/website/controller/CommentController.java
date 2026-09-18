package com.project.website.controller;

import com.project.website.DTO.Comment.CommentCreateRequest;
import com.project.website.DTO.Comment.CommentResponse;
import com.project.website.DTO.Comment.CommentUpdateRequest;
import com.project.website.entity.CommentImage;
import com.project.website.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    /**
     * Форма, а не JSON: так файл едет в исходном виде, без раздувания
     * на треть, как было бы при base64.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommentResponse create(
            @ModelAttribute @Valid CommentCreateRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Authentication authentication){
        return commentService.created(request, image, authentication);
    }

    @GetMapping("/post/{postId}")
    public List<CommentResponse> getCommentsByPost(@PathVariable Long postId){
        return commentService.getCommentsByPost(postId);
    }

    /**
     * Отдаётся без токена: тег img не умеет слать заголовок Authorization.
     * Кэш на сутки — картинка по этому адресу уже не меняется.
     */
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id){
        CommentImage image = commentService.getImage(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePublic())
                .body(image.getData());
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
