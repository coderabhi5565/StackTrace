package com.StackTrace.User_Service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_followers",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"follower_id", "following_id"}
                )
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Follow {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(name = "follower_id", nullable = false)
   private Long followerId;

   @Column(name = "following_id", nullable = false)
   private Long followingId;

   private LocalDateTime createdAt;

   @PrePersist
   public void created() {
      createdAt = LocalDateTime.now();
   }
}