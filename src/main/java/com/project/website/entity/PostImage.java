package com.project.website.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Картинка поста в отдельной таблице: так байты не попадают в выборку
 * при загрузке ленты и читаются только когда браузер запрашивает саму картинку.
 */
@Entity
@Table(name = "post_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostImage {

    @Id
    Long postId;

    @Column(nullable = false, length = 100)
    String contentType;

    @Column(nullable = false, columnDefinition = "bytea")
    byte[] data;
}
