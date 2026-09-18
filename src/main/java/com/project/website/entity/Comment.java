package com.project.website.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.builder.ToStringExclude;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "comments")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(columnDefinition = "TEXT")
    String text;

    LocalDateTime createdAt;

    @ToStringExclude
    @ManyToOne
    @JoinColumn(name = "user_id")
    User author;

    @ToStringExclude
    @ManyToOne
    @JoinColumn(name = "post_id")
    Post post;

    // Только признак наличия картинки. Сами байты лежат в отдельной таблице
    // (CommentImage), иначе Hibernate тянул бы их при каждой загрузке ленты.
    // default false обязателен: без него ddl-auto=update не может добавить
    // NOT NULL колонку в таблицу, где уже есть строки, и молча её пропускает
    @Column(nullable = false, columnDefinition = "boolean default false")
    boolean hasImage;

    public Comment(Long id, String text, LocalDateTime createdAt, Post post){
        this.id = id;
        this.text = text;
        this.createdAt = createdAt;
        this.post = post;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", createdAt=" + createdAt +
                ", post=" + (post != null ? post.getId() : null)+
                '}';
    }
}
