package com.project.website.repository;

import com.project.website.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepo extends JpaRepository<Like, Long> {
    Optional<Like> findByUserIdAndPostId(Long userId, Long postId);
    long countByPostId(Long postId);
    void deleteByPostId(Long postId);
}
