package com.project.website.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.website.entity.UserAvatar;

public interface UserAvatarRepo extends JpaRepository<UserAvatar, Long> {
    
}
