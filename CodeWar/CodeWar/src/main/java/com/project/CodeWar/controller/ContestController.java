package com.project.CodeWar.controller;

import com.project.CodeWar.service.ContestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}