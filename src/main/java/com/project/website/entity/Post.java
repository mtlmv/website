package com.project.website.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
@Table(name = "posts")
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder

@FieldDefaults(level = AccessLevel.PRIVATE)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String title;

    @Column(columnDefinition = "TEXT")
    String content;

    LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User author;

    // Только признак наличия картинки, байты — в таблице post_images.
    // default false обязателен: без него ddl-auto=update не может добавить
    // NOT NULL колонку в таблицу, где уже есть строки, и молча её пропускает
    @Column(nullable = false, columnDefinition = "boolean default false")
    boolean hasImage;

    public Post(String title, String content, LocalDateTime createdAt, User author){
        this.author = author;
        this.content = content;
        this.createdAt = createdAt;
        this.title = title;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                ", autor=" + author +
                '}';
    }
}
