package com.project.website.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;


/**
 * Аватар в отдельной таблице: байты не тянутся при каждой выборке пользователя
 * и читаются только когда браузер запрашивает саму картинку.
 */
@Entity
@Table(name = "user_avatars")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserAvatar {

    /** Тот же id, что у пользователя: у одного человека одна аватарка. */
    @Id
    Long userId;

    @Column(nullable = false, length = 100)
    String contentType;

    @Column(nullable = false, columnDefinition = "bytea")
    byte[] data;
}
