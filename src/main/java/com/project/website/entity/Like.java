package com.project.website.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "likes",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "post_id"})
        }
)
@FieldDefaults(level = AccessLevel.PRIVATE)

public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "post_id")
    Post post;
}
