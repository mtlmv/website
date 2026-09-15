package com.project.website.service;


import com.project.website.DTO.Like.LikeRequest;
import com.project.website.DTO.Like.LikeResponse;
import com.project.website.DTO.Like.LikeStatusResponse;
import com.project.website.entity.Like;
import com.project.website.entity.Post;
import com.project.website.entity.User;
import com.project.website.repository.LikeRepo;
import com.project.website.repository.PostRepo;
import com.project.website.repository.UserRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LikeServiceImpl implements LikeService{
    final LikeRepo likeRepo;
    final UserRepo userRepo;
    final PostRepo postRepo;


    @Override
    public LikeResponse likeOrUnlike(LikeRequest request, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(()->
                        new RuntimeException("User not found"));
        Post post = postRepo.findById(request.getPostId())
                .orElseThrow(()->
                        new RuntimeException("Post not found"));
        Optional<Like> existingLike =
                likeRepo.findByUserIdAndPostId(
                        user.getId(),
                        post.getId()
                );
        if(existingLike.isPresent()) {
            //UNLIKE
            likeRepo.delete(existingLike.get());

            return LikeResponse.builder()
                    .liked(false)
                    .massage("Post unliked")
                    .build();
        }
        //LIKE
        Like like = new Like();
        like.setUser(user);
        like.setPost(post);

        likeRepo.save(like);
        return LikeResponse.builder()
                .liked(true)
                .massage("Post liked")
                .build();
    }

    @Override
    public long getLikesCount(Long postId) {
        postRepo.findById(postId)
                .orElseThrow(()->
                        new RuntimeException("Post not found"));
        return likeRepo.countByPostId(postId);

    }

    @Override
    public LikeStatusResponse getLikeStatus(
            Long postId,
            Authentication authentication) {
        String email = authentication.getName();

        User user = userRepo.findByEmail(email)
                .orElseThrow(()->
                        new RuntimeException("User not found"));

        postRepo.findById(postId)
                .orElseThrow(()->
                        new RuntimeException("Post not found"));

        boolean liked = likeRepo
                .findByUserIdAndPostId(
                        user.getId(),
                        postId
                )
                .isPresent();
        return new LikeStatusResponse(liked);
    }
}
