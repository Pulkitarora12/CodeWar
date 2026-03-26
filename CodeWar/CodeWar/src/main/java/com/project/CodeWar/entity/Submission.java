package com.project.CodeWar.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "submissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"contest_id", "user_id"}))
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "contest_id", nullable = false)
    private Contest contest;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int failedAttempts = 0;

    private Long timeTakenSeconds; // null if not solved

    @Column(nullable = false)
    private boolean solved = false;

//    @CreationTimestamp
//    @Column(updatable = false)
    private LocalDateTime submittedAt;
}