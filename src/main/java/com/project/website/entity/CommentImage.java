package com.project.website.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Картинка комментария в отдельной таблице: так байты не попадают в выборку
 * при загрузке ленты и читаются только когда браузер запрашивает саму картинку.
 */
@Entity
@Table(name = "comment_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentImage {

    @Id
    Long commentId;

    @Column(nullable = false, length = 100)
    String contentType;

    @Column(nullable = false, columnDefinition = "bytea")
    byte[] data;
}
