package com.project.website.DTO.Post;


import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostUpdateRequest {
    String title;
    String content;

}
