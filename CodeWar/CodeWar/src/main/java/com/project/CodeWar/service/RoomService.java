package com.project.CodeWar.service;

import com.project.CodeWar.dtos.CfUser;
import com.project.CodeWar.entity.Room;
import com.project.CodeWar.entity.RoomStatus;

import java.util.List;
import java.util.Map;

public interface RoomService {
    Room createRoom(Long userId);
    Room joinRoom(String roomCode, Long userId);
    Room getRoomByCode(String roomCode);
    List<Room> getRoomsByUser(Long userId);
    void updateRoomStatus(String roomCode, Long userId, RoomStatus status);
    List<Map<String, Object>> getRoomParticipantsRatings(String roomCode);
    int calculateProblemRating(String roomCode);
}