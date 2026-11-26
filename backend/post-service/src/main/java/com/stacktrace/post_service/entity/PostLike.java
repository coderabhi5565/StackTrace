package com.stacktrace.post_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "post_likes")
@Getter
@Setter
@NoArgsConstructor
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long postId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Instant createdAt;

    public PostLike(Long postId, Long userId) {
        this.postId = postId;
        this.userId = userId;
        this.createdAt = Instant.now();
    }
}
