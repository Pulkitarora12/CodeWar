package com.project.CodeWar.controller;

import com.project.CodeWar.security.util.AuthUtil;
import com.project.CodeWar.service.CodeforcesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/codeforces")
public class CodeforcesController {

    @Autowired
    private CodeforcesService codeforcesService;

    @Autowired
    private AuthUtil authUtil;

    @PostMapping("/generate-token")
    public ResponseEntity<?> generateToken(@RequestParam String handle) {
        try {
            Long userId = authUtil.loggedInUserId();
            String token = codeforcesService.generateVerificationToken(userId, handle);
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "message", "Add this token to your Codeforces first name and click Verify"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyHandle() {
        try {
            Long userId = authUtil.loggedInUserId();
            boolean verified = codeforcesService.verifyHandle(userId);
            if (verified) {
                return ResponseEntity.ok(Map.of("message", "Codeforces account linked successfully!"));
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Token not found in your Codeforces first name"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/unlink")
    public ResponseEntity<?> unlinkHandle() {
        try {
            Long userId = authUtil.loggedInUserId();
            codeforcesService.unlinkHandle(userId);
            return ResponseEntity.ok(Map.of("message", "Codeforces account unlinked"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        try {
            Long userId = authUtil.loggedInUserId();
            var user = authUtil.loggedInUser();
            return ResponseEntity.ok(Map.of(
                    "handle", user.getCodeforcesHandle() != null ? user.getCodeforcesHandle() : "",
                    "verified", user.isCodeforcesVerified()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}