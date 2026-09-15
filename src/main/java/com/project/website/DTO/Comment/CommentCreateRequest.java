package com.project.website.DTO.Comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class CommentCreateRequest {
    @NotBlank
    String text;

    @NotNull
    Long postId;
}
