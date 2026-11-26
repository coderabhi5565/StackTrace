package com.stacktrace.post_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "bookmarks")
@Getter
@Setter
@NoArgsConstructor
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long postId;

    @Column(nullable = false)
    private Instant createdAt;

    public Bookmark(Long userId, Long postId) {
        this.userId = userId;
        this.postId = postId;
        this.createdAt = Instant.now();
    }
}
