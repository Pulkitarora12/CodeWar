package com.project.CodeWar.service;

import com.project.CodeWar.entity.RoomProblem;

public interface ProblemService {
    RoomProblem pickProblemForRoom(String roomCode);
}