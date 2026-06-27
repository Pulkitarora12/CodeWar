package com.project.CodeWar.service;

import com.project.CodeWar.entity.RoomProblem;

import java.util.List;
import java.util.Optional;

public interface ProblemService {
    RoomProblem pickProblemForRoom(String roomCode);
    List<RoomProblem> getRoomProblems(String roomCode);
    Optional<RoomProblem> getCurrentProblem(String roomCode);
}