package com.project.website.DTO.Comment;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentRequest {
    @NotBlank
    String text;

    @NotNull
    Long postId;


}
