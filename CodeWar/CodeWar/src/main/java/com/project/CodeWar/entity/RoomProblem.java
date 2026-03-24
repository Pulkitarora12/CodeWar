package com.project.CodeWar.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "room_problems")
public class RoomProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private Integer contestId;

    @Column(nullable = false)
    private String problemIndex;

    @Column(nullable = false)
    private String problemName;

    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = false)
    private String problemUrl;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime assignedAt;
}