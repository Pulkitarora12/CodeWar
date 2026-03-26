package com.project.CodeWar.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaderboardDTO {

    private String roomCode;
    private Long contestId;
    private List<Entry> entries;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Entry {
        private String username;
        private Integer score;
        private boolean solved;       // true if they got AC
        private String solvedAt;      // formatted time since contest start e.g. "12m 34s"
    }
}