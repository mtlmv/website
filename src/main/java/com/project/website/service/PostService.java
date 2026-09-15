package com.project.website.service;

import com.project.website.DTO.Post.PostRequest;
import com.project.website.DTO.Post.PostResponse;
import com.project.website.DTO.Post.PostResponseId;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface PostService {
    PostResponse create(PostRequest request,
                        Authentication authentication);
    List<PostResponse> getAllPosts(Authentication authentication);
    PostResponse getPostById(
            Long id,
            Authentication authentication
    );
    List<PostResponseId> getAllPostsId();
    PostResponseId updatePost(PostRequest request, Long id, Authentication authentication);
    PostResponseId deletePost(Long id, Authentication authentication);

}
