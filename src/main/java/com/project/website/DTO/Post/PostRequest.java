package com.project.website.DTO.Post;


import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class PostRequest {
    @NotBlank
    String title;
    
    @NotBlank
    String content;

}
