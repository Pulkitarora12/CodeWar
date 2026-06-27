package com.project.CodeWar.controller;

import com.project.CodeWar.entity.Room;
import com.project.CodeWar.entity.RoomProblem;
import com.project.CodeWar.entity.RoomStatus;
import com.project.CodeWar.entity.User;
import com.project.CodeWar.security.util.AuthUtil;
import com.project.CodeWar.service.ProblemService;
import com.project.CodeWar.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Room", description = "Create, join and manage coding battle rooms")
@RestController
@RequestMapping("/api/room")
public class RoomController {

    private static final Logger logger = LoggerFactory.getLogger(RoomController.class);

    @Autowired
    private RoomService roomService;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private ProblemService problemService;


    @Value("${frontend.url}")
    private String frontendUrl;

    @Operation(summary = "Create a new room", description = "Generates room code and invite link")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Room created successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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

    @Operation(summary = "Join a room by code")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Joined successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid room code")
    })
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

    @Operation(summary = "Get room details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Room found"),
            @ApiResponse(responseCode = "404", description = "Room not found")
    })
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

    @Operation(summary = "Get all rooms of logged-in user")
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

    @Operation(summary = "Update room status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
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

    @Operation(summary = "Get CF ratings of all room participants")
    @GetMapping("/{roomCode}/ratings")
    public ResponseEntity<?> getRoomRatings(@PathVariable String roomCode) {
        try {
            List<Map<String, Object>> ratings = roomService.getRoomParticipantsRatings(roomCode);
            return ResponseEntity.ok(Map.of(
                    "roomCode", roomCode,
                    "participants", ratings
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Get calculated problem rating for room")
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

    @Operation(summary = "Pick a problem for the room based on average rating")
    @PostMapping("/{roomCode}/pick-problem")
    public ResponseEntity<?> pickProblem(@PathVariable String roomCode) {
        try {
            RoomProblem problem = problemService.pickProblemForRoom(roomCode);
            return ResponseEntity.ok(Map.of(
                    "problemName", problem.getProblemName(),
                    "contestId", problem.getContestId(),
                    "problemIndex", problem.getProblemIndex(),
                    "rating", problem.getRating(),
                    "problemUrl", problem.getProblemUrl(),
                    "assignedAt", problem.getAssignedAt()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Get all problems assigned to room")
    @GetMapping("/{roomCode}/problems")
    public ResponseEntity<?> getRoomProblems(@PathVariable String roomCode) {
        try {
            List<RoomProblem> problems = problemService.getRoomProblems(roomCode);
            return ResponseEntity.ok(Map.of(
                    "roomCode", roomCode,
                    "problems", problems.stream().map(p -> Map.of(
                            "problemName", p.getProblemName(),
                            "contestId", p.getContestId(),
                            "problemIndex", p.getProblemIndex(),
                            "rating", p.getRating(),
                            "problemUrl", p.getProblemUrl(),
                            "assignedAt", p.getAssignedAt()
                    )).toList()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Get latest assigned problem for room")
    @GetMapping("/{roomCode}/current-problem")
    public ResponseEntity<?> getCurrentProblem(@PathVariable String roomCode) {
        try {
            RoomProblem problem = problemService.getCurrentProblem(roomCode)
                    .orElseThrow(() -> new RuntimeException("No problem assigned yet"));
            return ResponseEntity.ok(Map.of(
                    "problemName", problem.getProblemName(),
                    "contestId", problem.getContestId(),
                    "problemIndex", problem.getProblemIndex(),
                    "rating", problem.getRating(),
                    "problemUrl", problem.getProblemUrl(),
                    "assignedAt", problem.getAssignedAt()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}