package com.project.website.controller;

import com.project.website.DTO.Like.LikeRequest;
import com.project.website.DTO.Like.LikeResponse;
import com.project.website.DTO.Like.LikeStatusResponse;
import com.project.website.service.LikeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;

    @PostMapping
    public LikeResponse likeOrUnlike(
            @RequestBody @Valid LikeRequest request,
            Authentication authentication
    ) {
        return likeService.likeOrUnlike(
                request,
                authentication
        );
    }
    @GetMapping("/post/{postId}/count")
    public long getLikesCount(
            @PathVariable Long postId
    ){
        return likeService.getLikesCount(postId);
    }
    @GetMapping("/post/{postId}/status")
    public LikeStatusResponse getLikeStatus(
            @PathVariable Long postId,
            Authentication authentication
    ){
        return likeService.getLikeStatus(
                postId,
                authentication
        );

    }
}
