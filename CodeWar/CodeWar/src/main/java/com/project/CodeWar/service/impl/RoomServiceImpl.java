package com.project.CodeWar.service.impl;

import com.project.CodeWar.dtos.CfUser;
import com.project.CodeWar.entity.*;
import com.project.CodeWar.repository.*;
import com.project.CodeWar.service.CodeforcesService;
import com.project.CodeWar.service.RoomService;
import com.project.CodeWar.service.UserService;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

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
    private UserService userService;

    @Autowired
    private CodeforcesService codeforcesService;

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private ContestRepository contestRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private RoomProblemRepository roomProblemRepository;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    @CacheEvict(cacheNames = "rooms", allEntries = true)
    public Room createRoom(Long userId) {
        logger.info("Creating room for userId: {}", userId);

        User user = userService.getUserEntityById(userId);

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
    @CacheEvict(cacheNames = "rooms", allEntries = true)
    public Room joinRoom(String roomCode, Long userId) {
        logger.info("User {} attempting to join room: {}", userId, roomCode);

        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (room.getStatus() == RoomStatus.COMPLETED) {
            throw new RuntimeException("Room is already completed");
        }

        User user = userService.getUserEntityById(userId);

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
    @Cacheable(cacheNames = "rooms", key = "#roomCode")
    public Room getRoomByCode(String roomCode) {
        logger.info("Fetching room with code: {}", roomCode);
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Copy fields to a detached Room object with standard ArrayList to avoid PersistentBag serialization issues
        Room detachedRoom = new Room();
        detachedRoom.setId(room.getId());
        detachedRoom.setRoomCode(room.getRoomCode());
        detachedRoom.setStatus(room.getStatus());
        detachedRoom.setCreatedAt(room.getCreatedAt());
        detachedRoom.setCreatedBy(room.getCreatedBy());
        detachedRoom.setParticipants(new ArrayList<>(room.getParticipants()));
        return detachedRoom;
    }


    @Override
    @Cacheable(cacheNames = "rooms", key = "'user::' + #userId")
    public List<Room> getRoomsByUser(Long userId) {
        logger.info("Fetching rooms for userId: {}", userId);
        User user = userService.getUserEntityById(userId);
        List<Room> rooms = roomRepository.findByParticipantsContaining(user);

        List<Room> detachedRooms = new ArrayList<>();
        for (Room room : rooms) {
            Room detachedRoom = new Room();
            detachedRoom.setId(room.getId());
            detachedRoom.setRoomCode(room.getRoomCode());
            detachedRoom.setStatus(room.getStatus());
            detachedRoom.setCreatedAt(room.getCreatedAt());
            detachedRoom.setCreatedBy(room.getCreatedBy());
            detachedRoom.setParticipants(new ArrayList<>(room.getParticipants()));
            detachedRooms.add(detachedRoom);
        }
        return detachedRooms;
    }

    private String generateUniqueRoomCode() {
        String code;
        do {
            code = "CW-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        } while (roomRepository.existsByRoomCode(code));
        return code;
    }

    @Override
    @CacheEvict(cacheNames = "rooms", allEntries = true)
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
                } else {
                    totalRating += 0;
                    count++;
                }
            } catch (Exception e) {
                logger.warn("Skipping participant {} — {}", participant.getUserName(), e.getMessage());
                count++;
                totalRating += 0;
            }
        }

        if (count == 0) {
            throw new RuntimeException("No participants with verified CF handles found");
        }

        int avgRating = totalRating / count;
        int problemRating = (int) (Math.round((avgRating) / 100.0) * 100);
        problemRating = Math.min(problemRating, 3000);
        problemRating = Math.max(problemRating, 800);

        logger.info("Avg rating: {}, Problem rating calculated: {}", avgRating, problemRating);
        return problemRating;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "rooms", allEntries = true)
    public void deleteRoom(String roomCode, Long userId) {
        logger.info("Deleting room {} by user {}", roomCode, userId);
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.getCreatedBy().getUserId().equals(userId)) {
            throw new RuntimeException("Only the room creator can delete this room");
        }

        // 1. Delete all scores associated with this room
        List<Score> scores = scoreRepository.findByRoom(room);
        scoreRepository.deleteAll(scores);

        // 2. Delete all contests (and their submissions & scores) of this room
        List<Contest> contests = contestRepository.findByRoomProblem_RoomOrderByStartTimeDesc(room);
        for (Contest contest : contests) {
            List<Submission> submissions = submissionRepository.findByContest(contest);
            submissionRepository.deleteAll(submissions);

            List<Score> contestScores = scoreRepository.findByContest(contest);
            scoreRepository.deleteAll(contestScores);

            contestRepository.delete(contest);
        }

        // 3. Delete all room problems
        List<RoomProblem> roomProblems = roomProblemRepository.findByRoom(room);
        roomProblemRepository.deleteAll(roomProblems);

        // 4. Clear participants to remove associations in the join table
        room.getParticipants().clear();
        roomRepository.save(room);

        // 5. Finally, delete the room
        roomRepository.delete(room);
        logger.info("Room {} deleted successfully", roomCode);
    }
}