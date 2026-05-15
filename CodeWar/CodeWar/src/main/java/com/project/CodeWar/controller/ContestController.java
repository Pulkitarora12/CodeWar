package com.project.CodeWar.controller;

import com.project.CodeWar.dtos.LeaderboardResponse;
import com.project.CodeWar.service.ContestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Contest", description = "Start, end and track coding contests")
@RestController
@RequestMapping("/api/contest")
public class ContestController {

    private static final Logger logger = LoggerFactory.getLogger(ContestController.class);

    @Autowired
    private ContestService contestService;

    @Operation(summary = "Start contest for a room")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contest started"),
            @ApiResponse(responseCode = "400", description = "Invalid room or contest already started")
    })
    @PostMapping("/start/{roomCode}")
    public ResponseEntity<?> startContest(@PathVariable String roomCode) {
        try {
            Map<String, Object> result = contestService.startContest(roomCode);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            logger.error("Error starting contest for room: {}", roomCode, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Check latest CF submission for contest")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Submission checked"),
            @ApiResponse(responseCode = "400", description = "Invalid contest ID")
    })
    @PostMapping("/{contestId}/check-submission")
    public ResponseEntity<?> checkSubmission(@PathVariable Long contestId) {
        try {
            Map<String, Object> result = contestService.checkSubmission(contestId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            logger.error("Error checking submission for contestId: {}", contestId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "End a contest")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contest ended"),
            @ApiResponse(responseCode = "400", description = "Invalid contest ID")
    })
    @PostMapping("/{contestId}/end")
    public ResponseEntity<?> endContest(@PathVariable Long contestId) {
        try {
            contestService.endContest(contestId);
            return ResponseEntity.ok(Map.of("message", "Contest ended successfully"));
        } catch (RuntimeException e) {
            logger.error("Error ending contest: {}", contestId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Get contest leaderboard")
    @GetMapping("/{contestId}/leaderboard")
    public ResponseEntity<LeaderboardResponse> getLeaderboard(@PathVariable Long contestId) {
        return ResponseEntity.ok(contestService.getLeaderboard(contestId));
    }

    @Operation(summary = "Get all contests for a room")
    @GetMapping("/room/{roomCode}")
    public ResponseEntity<?> getContestsByRoom(@PathVariable String roomCode) {
        try {
            List<Map<String, Object>> results = contestService.getContestsByRoom(roomCode);
            return ResponseEntity.ok(results);
        } catch (RuntimeException e) {
            logger.error("Error fetching contests for room {}: {}", roomCode, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Get contest details by ID")
    @GetMapping("/{contestId}/details")
    public ResponseEntity<?> getContestDetails(@PathVariable Long contestId) {
        try {
            Map<String, Object> results = contestService.getContestDetails(contestId);
            return ResponseEntity.ok(results);
        } catch (RuntimeException e) {
            logger.error("Error fetching contest details for {}: {}", contestId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}