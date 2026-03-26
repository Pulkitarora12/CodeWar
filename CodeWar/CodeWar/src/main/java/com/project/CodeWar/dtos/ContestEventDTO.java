package com.project.CodeWar.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContestEventDTO {

    public enum EventType {
        STARTED,
        ENDED
    }

    private EventType type;
    private Long contestId;
    private String roomCode;

    // only relevant on STARTED
    private String problemUrl;
    private String problemName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}