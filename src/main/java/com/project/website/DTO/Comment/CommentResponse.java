package com.project.website.DTO.Comment;


import lombok.*;
import lombok.experimental.FieldDefaults;
import com.project.website.DTO.Comment.CommentResponse;
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
    // Байты картинки здесь не отдаются — фронтенд забирает её
    // отдельным запросом GET /comments/{id}/image
    boolean hasImage;
    LocalDateTime createdAt;
}
