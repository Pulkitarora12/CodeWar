package com.project.CodeWar.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class LeaderboardResponse implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private Long contestId;
    private List<LeaderboardEntryDTO> entries;
    private String status;
}