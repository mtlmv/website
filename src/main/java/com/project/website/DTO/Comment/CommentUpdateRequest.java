package com.project.website.DTO.Comment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentUpdateRequest {
    @NotBlank
    String text;
}
