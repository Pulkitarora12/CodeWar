package com.project.CodeWar.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "contests")
public class Contest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id", nullable = false, unique = true)
    private Room room;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContestStatus status = ContestStatus.ONGOING;

    @PrePersist
    public void setEndTime() {
        if (this.endTime == null) {
            this.endTime = LocalDateTime.now().plusMinutes(30);
        }
    }
}