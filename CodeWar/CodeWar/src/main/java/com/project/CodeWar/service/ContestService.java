package com.project.CodeWar.service;

import com.project.CodeWar.dtos.LeaderboardResponse;
import com.project.CodeWar.entity.Contest;
import com.project.CodeWar.entity.Score;
import com.project.CodeWar.entity.Submission;

import java.util.List;
import java.util.Map;

public interface ContestService {

    Map<String, Object> startContest(String roomCode);

    Map<String, Object> checkSubmission(Long contestId);

    void endContest(Long contestId);

    LeaderboardResponse getLeaderboard(Long contestId);

    List<Map<String, Object>> getContestsByRoom(String roomCode);

    Map<String, Object> getContestDetails(Long contestId);
}