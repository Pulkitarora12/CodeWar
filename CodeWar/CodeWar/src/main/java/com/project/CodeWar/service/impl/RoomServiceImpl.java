package com.project.CodeWar.service.impl;

import com.project.CodeWar.dtos.CfUser;
import com.project.CodeWar.entity.Room;
import com.project.CodeWar.entity.RoomStatus;
import com.project.CodeWar.entity.User;
import com.project.CodeWar.repository.RoomRepository;
import com.project.CodeWar.repository.UserRepository;
import com.project.CodeWar.service.CodeforcesService;
import com.project.CodeWar.service.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RoomServiceImpl implements RoomService {

    private static final Logger logger = LoggerFactory.getLogger(RoomServiceImpl.class);

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CodeforcesService codeforcesService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public Room createRoom(Long userId) {
        logger.info("Creating room for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String roomCode = generateUniqueRoomCode();
        logger.info("Generated room code: {}", roomCode);

        Room room = new Room();
        room.setRoomCode(roomCode);
        room.setCreatedBy(user);
        room.setStatus(RoomStatus.WAITING);
        room.getParticipants().add(user); // creator is first participant

        roomRepository.save(room);
        logger.info("Room created with code: {} for userId: {}", roomCode, userId);
        return room;
    }

    @Override
    public Room joinRoom(String roomCode, Long userId) {
        logger.info("User {} attempting to join room: {}", userId, roomCode);

        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (room.getStatus() == RoomStatus.COMPLETED) {
            throw new RuntimeException("Room is already completed");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean alreadyIn = room.getParticipants().stream()
                .anyMatch(p -> p.getUserId().equals(userId));

        if (alreadyIn) {
            logger.warn("User {} is already in room: {}", userId, roomCode);
            throw new RuntimeException("You are already in this room");
        }

        room.getParticipants().add(user);
        roomRepository.save(room);

        logger.info("User {} joined room: {}", userId, roomCode);
        return room;
    }

    @Override
    public Room getRoomByCode(String roomCode) {
        logger.info("Fetching room with code: {}", roomCode);
        return roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));
    }

    @Override
    public List<Room> getRoomsByUser(Long userId) {
        logger.info("Fetching rooms for userId: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return roomRepository.findByParticipantsContaining(user);
    }

    private String generateUniqueRoomCode() {
        String code;
        do {
            code = "CW-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        } while (roomRepository.existsByRoomCode(code));
        return code;
    }

    @Override
    public void updateRoomStatus(String roomCode, Long userId, RoomStatus status) {
        logger.info("Updating room {} status to {} by userId: {}", roomCode, status, userId);

        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.getCreatedBy().getUserId().equals(userId)) {
            logger.warn("User {} is not the creator of room {}", userId, roomCode);
            throw new RuntimeException("Only the room creator can update the status");
        }

        room.setStatus(status);
        roomRepository.save(room);
        logger.info("Room {} status updated to {}", roomCode, status);
    }

    @Override
    public List<Map<String, Object>> getRoomParticipantsRatings(String roomCode) {

        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        List<Map<String, Object>> ratings = new ArrayList<>();

        for (User participant : room.getParticipants()) {
            try {
                CfUser cfUser = codeforcesService.getUserRatingByUserId(participant.getUserId());

                ratings.add(Map.of(
                        "username", participant.getUserName(),
                        "rating", cfUser.getRating(),
                        "rank", cfUser.getRank()
                ));

            } catch (Exception e) {
                ratings.add(Map.of(
                        "username", participant.getUserName(),
                        "rating", 0,
                        "rank", 0
                ));
            }
        }

        return ratings;
    }

    @Override
    public int calculateProblemRating(String roomCode) {
        logger.info("Calculating problem rating for room: {}", roomCode);

        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        List<User> participants = room.getParticipants();

        if (participants.isEmpty()) {
            throw new RuntimeException("No participants in room");
        }

        int totalRating = 0;
        int count = 0;

        for (User participant : participants) {
            try {
                CfUser cfUser = codeforcesService.getUserRatingByUserId(participant.getUserId());
                if (cfUser.getRating() != null) {
                    totalRating += cfUser.getRating();
                    count++;
                    logger.info("Participant: {} — rating: {}", participant.getUserName(), cfUser.getRating());
                }
            } catch (Exception e) {
                logger.warn("Skipping participant {} — {}", participant.getUserName(), e.getMessage());
            }
        }

        if (count == 0) {
            throw new RuntimeException("No participants with verified CF handles found");
        }

        int avgRating = totalRating / count;
        int problemRating = (int) (Math.round((avgRating + 100) / 100.0) * 100);
        problemRating = Math.min(problemRating, 3000);

        logger.info("Avg rating: {}, Problem rating calculated: {}", avgRating, problemRating);
        return problemRating;
    }
}