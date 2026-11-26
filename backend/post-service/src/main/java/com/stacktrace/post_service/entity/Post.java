package com.stacktrace.post_service.entity;

import com.stacktrace.post_service.enums.PostStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Post extends BaseEntity {

    @Id
    @SequenceGenerator(
            name = "post_sequence",
            sequenceName = "post_sequence",
            allocationSize = 50
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "post_sequence"
    )
    private Long id;

    @Column(nullable = false, length = 250)
    private String title;

    @Column(nullable = false, length = 300, unique = true)
    private String slug;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 500)
    private String coverImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status = PostStatus.DRAFT;

    @Column(nullable = false)
    private Long authorId;

    private Instant publishedAt;

    private Instant deletedAt;

    @Column(length = 300)
    private String excerpt;

    /** Estimated minutes to read (words / 200, at least 1). */
    @Column(nullable = false)
    private int readingTime = 1;

    /** Denormalized counters, updated atomically; drive the trending score. */
    @Column(nullable = false)
    private int likeCount;

    @Column(nullable = false)
    private int commentCount;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "post_tag",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @BatchSize(size = 50)
    private Set<Tag> tags = new LinkedHashSet<>();

    @Column(name = "scheduled_publish_at")
    private Instant scheduledPublishAt;
}
