package com.project.website.DTO.Post;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class PostResponseId{
    String authorName;
    String title;
    String content;
    LocalDateTime createdAt;
}
