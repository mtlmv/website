package com.project.website.service;

import com.project.website.DTO.Like.LikeRequest;
import com.project.website.DTO.Like.LikeResponse;
import com.project.website.DTO.Like.LikeStatusResponse;
import org.springframework.security.core.Authentication;

public interface LikeService {
    LikeResponse likeOrUnlike(LikeRequest request,
                              Authentication authentication
    );
    long getLikesCount(Long postId);
    LikeStatusResponse getLikeStatus(
            Long postId,
            Authentication authentication
    );
}
