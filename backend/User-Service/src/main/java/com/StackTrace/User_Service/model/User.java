package com.StackTrace.User_Service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false, length = 30)
    private String username;
    @Column(length = 100)
    private String name;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(length = 500)
    private String bio;
    private String avatarUrl;
    @Column(length = 100)
    private String location;
    @Builder.Default
    @Column(nullable = false)
    private int points = 0;
    private LocalDateTime createdAt;
    private String password;

    @PrePersist
    protected  void onCreate(){
        createdAt = LocalDateTime.now();
    }
}
