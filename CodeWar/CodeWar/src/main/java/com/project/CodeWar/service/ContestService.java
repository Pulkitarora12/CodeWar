package com.project.CodeWar.service;

import com.project.CodeWar.entity.Contest;
import com.project.CodeWar.entity.Score;
import com.project.CodeWar.entity.Submission;

import java.util.Map;

public interface ContestService {

    Map<String, Object> startContest(String roomCode);

    Map<String, Object> checkSubmission(Long contestId);

    void endContest(Long contestId);
}