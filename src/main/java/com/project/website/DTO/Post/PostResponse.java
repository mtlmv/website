package com.project.website.DTO.Post;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import com.project.website.DTO.Comment.CommentResponse;
import lombok.Data;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostResponse {
    Long id;
    Long authorId;
    String authorName;
    String title;
    String content;
    LocalDateTime createdAt;

    Long likesCount;
    boolean liked;

    List<CommentResponse> comments;
}
