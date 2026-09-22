package com.project.website.entity;

import com.project.website.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDate;


@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class User {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;

    @Column(unique = true)
    String username;

    @Column(unique = true)
    String email;

    String password;

    LocalDate birthDate;

    @Column(length = 500)
    String bio;

    @Column(nullable = false, columnDefinition = "boolean default false")
    boolean hasAvatar;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    Role role;
    public User(String name, String email, String password){
        this.name = name;
        this.email = email;
        this.password = password;

    }
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
