package com.stacktrace.post_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tag extends BaseEntity {

    @Id
    @SequenceGenerator(
            name = "tag_sequence",
            sequenceName = "tag_sequence",
            allocationSize = 20
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "tag_sequence"
    )
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    public Tag(String name) {
        this.name = name;
    }
}
