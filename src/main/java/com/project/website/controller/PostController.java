package com.project.website.controller;

import com.project.website.DTO.Post.PostRequest;
import com.project.website.DTO.Post.PostResponse;
import com.project.website.DTO.Post.PostResponseId;
import com.project.website.entity.PostImage;
import com.project.website.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;


@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor

public class PostController {
    private final PostService postService;

    /**
     * Форма, а не JSON: так файл едет в исходном виде, без раздувания
     * на треть, как было бы при base64.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PostResponse create(@ModelAttribute @Valid PostRequest request,
                               @RequestParam(value = "image", required = false) MultipartFile image,
                               Authentication authentication
    ){
        return postService.create(request, image, authentication);
    }

    /**
     * Отдаётся без токена: тег img не умеет слать заголовок Authorization.
     * Кэш на сутки — картинка по этому адресу уже не меняется.
     */
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id){
        PostImage image = postService.getImage(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePublic())
                .body(image.getData());
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
