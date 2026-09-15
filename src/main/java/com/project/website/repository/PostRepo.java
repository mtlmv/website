package com.project.website.repository;

import com.project.website.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface PostRepo extends JpaRepository<Post, Long> {
}
