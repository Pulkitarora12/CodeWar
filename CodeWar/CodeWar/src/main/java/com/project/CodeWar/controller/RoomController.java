package com.project.CodeWar.controller;

import com.project.CodeWar.dtos.CfUser;
import com.project.CodeWar.entity.Room;
import com.project.CodeWar.entity.RoomStatus;
import com.project.CodeWar.entity.User;
import com.project.CodeWar.security.util.AuthUtil;
import com.project.CodeWar.service.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/room")
public class RoomController {

    private static final Logger logger = LoggerFactory.getLogger(RoomController.class);

    @Autowired
    private RoomService roomService;

    @Autowired
    private AuthUtil authUtil;

    @Value("${frontend.url}")
    private String frontendUrl;

    @PostMapping("/create")
    public ResponseEntity<?> createRoom() {
        try {
            Long userId = authUtil.loggedInUserId();
            Room room = roomService.createRoom(userId);
            String inviteLink = frontendUrl + "/join?code=" + room.getRoomCode();
            logger.info("Invite link generated: {}", inviteLink);
            return ResponseEntity.ok(Map.of(
                    "roomCode", room.getRoomCode(),
                    "inviteLink", inviteLink,
                    "status", room.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/join/{roomCode}")
    public ResponseEntity<?> joinRoom(@PathVariable String roomCode) {
        try {
            Long userId = authUtil.loggedInUserId();
            Room room = roomService.joinRoom(roomCode, userId);
            return ResponseEntity.ok(Map.of(
                    "roomCode", room.getRoomCode(),
                    "status", room.getStatus(),
                    "message", "Successfully joined the room"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{roomCode}")
    public ResponseEntity<?> getRoom(@PathVariable String roomCode) {
        try {
            Room room = roomService.getRoomByCode(roomCode);
            List<String> participants = room.getParticipants().stream()
                    .map(User::getUserName)
                    .toList();
            return ResponseEntity.ok(Map.of(
                    "roomCode", room.getRoomCode(),
                    "createdBy", room.getCreatedBy().getUserName(),
                    "participants", participants,
                    "status", room.getStatus(),
                    "createdAt", room.getCreatedAt()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/my-rooms")
    public ResponseEntity<?> getMyRooms() {
        try {
            Long userId = authUtil.loggedInUserId();
            List<Room> rooms = roomService.getRoomsByUser(userId);
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{roomCode}/status")
    public ResponseEntity<?> updateRoomStatus(@PathVariable String roomCode,
                                              @RequestParam RoomStatus status) {
        try {
            Long userId = authUtil.loggedInUserId();
            roomService.updateRoomStatus(roomCode, userId, status);
            return ResponseEntity.ok(Map.of(
                    "message", "Room status updated to " + status,
                    "roomCode", roomCode,
                    "status", status
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{roomCode}/ratings")
    public ResponseEntity<?> getRoomRatings(@PathVariable String roomCode) {
        try {
            List<CfUser> ratings = roomService.getRoomParticipantsRatings(roomCode);
            return ResponseEntity.ok(Map.of(
                    "roomCode", roomCode,
                    "participants", ratings
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{roomCode}/problem-rating")
    public ResponseEntity<?> getProblemRating(@PathVariable String roomCode) {
        try {
            int problemRating = roomService.calculateProblemRating(roomCode);
            return ResponseEntity.ok(Map.of(
                    "roomCode", roomCode,
                    "problemRating", problemRating
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}