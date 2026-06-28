package com.project.CodeWar.controller;

import com.project.CodeWar.dtos.CfUser;
import com.project.CodeWar.security.util.AuthUtil;
import com.project.CodeWar.service.CodeforcesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Codeforces", description = "Link and verify Codeforces handle")
@RestController
@RequestMapping("/api/codeforces")
public class CodeforcesController {

    @Autowired
    private CodeforcesService codeforcesService;

    @Autowired
    private AuthUtil authUtil;

    @Operation(summary = "Generate CF verification token", description = "Add this token to your Codeforces first name to verify")
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

    @Operation(summary = "Verify Codeforces handle", description = "Checks if token exists in CF first name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Handle verified"),
            @ApiResponse(responseCode = "400", description = "Token not found in CF profile")
    })
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

    @Operation(summary = "Unlink Codeforces account")
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

    @Operation(summary = "Get CF verification status of logged-in user")
    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        try {
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

    @Operation(summary = "Get CF rating by user ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rating fetched"),
            @ApiResponse(responseCode = "400", description = "User not found or handle not linked")
    })
    @GetMapping("/rating/user/{userId}")
    public ResponseEntity<?> getUserRatingByUserId(@PathVariable Long userId) {
        try {
            CfUser cfUser = codeforcesService.getUserRatingByUserId(userId);
            return ResponseEntity.ok(Map.of(
                    "userId", userId,
                    "handle", cfUser.getHandle(),
                    "rating", cfUser.getRating() != null ? cfUser.getRating() : "Unrated",
                    "rank", cfUser.getRank() != null ? cfUser.getRank() : "Unrated",
                    "maxRating", cfUser.getMaxRating() != null ? cfUser.getMaxRating() : "Unrated",
                    "maxRank", cfUser.getMaxRank() != null ? cfUser.getMaxRank() : "Unrated"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}