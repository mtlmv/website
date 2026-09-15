package com.project.website.DTO.Comment;


import lombok.*;
import lombok.experimental.FieldDefaults;
import com.project.website.DTO.Comment.CommentResponse;
import java.util.List;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentResponse {
    Long id;
    Long authorId;
    String authorName;
    String text;
    LocalDateTime createdAt;
}
