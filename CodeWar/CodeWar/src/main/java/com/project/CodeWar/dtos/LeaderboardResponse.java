package com.project.CodeWar.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class LeaderboardResponse {
    private Long contestId;
    private List<LeaderboardEntryDTO> entries;
}