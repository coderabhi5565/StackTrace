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
    @Column(unique = true)
    private String username;
    private String name;
    @Column(unique = true)
    private String email;
    private String bio;
    private String avatarUrl;
    private String location;
    private int points = 0;
    private LocalDateTime createdAt;
    private String password;

    @PrePersist
    protected  void onCreate(){
        createdAt = LocalDateTime.now();
    }
}
