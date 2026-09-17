package com.project.website.controller;

import com.project.website.DTO.Post.PostRequest;
import com.project.website.DTO.Post.PostResponse;
import com.project.website.DTO.Post.PostResponseId;
import com.project.website.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor

public class PostController {
    private final PostService postService;


    @PostMapping
    public PostResponse create(@RequestBody @Valid PostRequest request,
                               Authentication authentication
    ){
        return postService.create(request, authentication);
    }

    //Все посты всех пользователей
    @GetMapping("/all")
    public List <PostResponse> getAllPosts(
            Authentication authentication
    ){
        return postService.getAllPosts(authentication);
    }

    //Все посты одного пользователя
    @GetMapping("/all/{id}")
    public List <PostResponseId> getAllPostsId(){
        return postService.getAllPostsId();
    }

    //Пост пользователя по id
    @GetMapping("/{id}")
    public PostResponse getPostById(@PathVariable Long id, Authentication authentication){
        return postService.getPostById(id, authentication);
    }

    @PutMapping("/{id}")
    public PostResponseId updatePost(
            @PathVariable Long id,
            @RequestBody @Valid PostRequest request,
            Authentication authentication){
        return postService.updatePost(request, id,authentication);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable Long id, Authentication authentication){
        postService.deletePost(id, authentication);
    }
}
