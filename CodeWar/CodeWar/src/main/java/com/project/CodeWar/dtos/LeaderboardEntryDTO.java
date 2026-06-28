package com.project.CodeWar.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaderboardEntryDTO implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private int score;
    private Long timeTakenSeconds;
    private int failedAttempts;
}