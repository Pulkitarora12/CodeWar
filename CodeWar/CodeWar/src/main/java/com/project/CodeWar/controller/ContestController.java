package com.project.CodeWar.controller;

import com.project.CodeWar.dtos.LeaderboardResponse;
import com.project.CodeWar.service.ContestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contest")
public class ContestController {

    private static final Logger logger = LoggerFactory.getLogger(ContestController.class);

    @Autowired
    private ContestService contestService;

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

    @GetMapping("/{contestId}/leaderboard")
    public ResponseEntity<LeaderboardResponse> getLeaderboard(@PathVariable Long contestId) {
        return ResponseEntity.ok(contestService.getLeaderboard(contestId));
    }

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