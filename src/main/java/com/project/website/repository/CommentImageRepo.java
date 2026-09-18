package com.project.website.repository;

import com.project.website.entity.CommentImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentImageRepo extends JpaRepository<CommentImage, Long> {
}
