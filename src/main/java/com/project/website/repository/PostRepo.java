package com.project.website.repository;

import com.project.website.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface PostRepo extends JpaRepository<Post, Long> {
    List<Post> findByAuthorId(Long authorId);
}
