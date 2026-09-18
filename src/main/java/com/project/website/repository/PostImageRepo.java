package com.project.website.repository;

import com.project.website.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostImageRepo extends JpaRepository<PostImage, Long> {
}
