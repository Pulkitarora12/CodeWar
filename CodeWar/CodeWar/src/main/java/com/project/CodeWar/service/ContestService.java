package com.project.CodeWar.service;

import com.project.CodeWar.entity.Contest;
import com.project.CodeWar.entity.RoomProblem;

public interface ContestService {
    Contest startContest(String roomCode);
    void endContest(Long contestId);
}