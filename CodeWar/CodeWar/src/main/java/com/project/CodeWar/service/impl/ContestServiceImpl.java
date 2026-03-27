package com.project.CodeWar.service.impl;

import com.project.CodeWar.dtos.CfSubmission;
import com.project.CodeWar.dtos.LeaderboardEntryDTO;
import com.project.CodeWar.dtos.LeaderboardResponse;
import com.project.CodeWar.entity.*;
import com.project.CodeWar.repository.*;
import com.project.CodeWar.security.util.AuthUtil;
import com.project.CodeWar.service.ContestService;
import com.project.CodeWar.service.CodeforcesService;
import com.project.CodeWar.service.ProblemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

@Service
public class ContestServiceImpl implements ContestService {

    private static final Logger logger = LoggerFactory.getLogger(ContestServiceImpl.class);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    @Autowired
    private ContestRepository contestRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomProblemRepository roomProblemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CodeforcesService codeforcesService;

    @Autowired
    private ProblemService problemService;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Value("${frontend.url}")
    private String frontendUrl;

    @PostConstruct
    public void reScheduleActiveContests() {
        logger.info("App startup — checking for active contests to re-schedule");

        List<Contest> activeContests = contestRepository.findByStatus(ContestStatus.ACTIVE);

        if (activeContests.isEmpty()) {
            logger.info("No active contests found on startup");
            return;
        }

        for (Contest contest : activeContests) {
            long remainingSeconds = Duration.between(LocalDateTime.now(), contest.getEndTime()).getSeconds();

            if (remainingSeconds > 0) {
                logger.info("Re-scheduling contestId: {} — {}s remaining", contest.getId(), remainingSeconds);
                scheduleContestEnd(contest.getId(), remainingSeconds);
            } else {
                logger.info("ContestId: {} already expired on startup — ending now", contest.getId());
                try {
                    endContest(contest.getId());
                } catch (Exception e) {
                    logger.error("Failed to end expired contestId: {} — {}", contest.getId(), e.getMessage());
                }
            }
        }
    }

    @Override
    public Map<String, Object> startContest(String roomCode) {
        logger.info("Starting contest for room: {}", roomCode);

        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // check no active contest already exists
        Optional<Contest> existing = contestRepository.findByRoomProblem_RoomAndStatus(room, ContestStatus.ACTIVE);
        if (existing.isPresent()) {
            throw new RuntimeException("An active contest already exists for this room");
        }

        // pick problem
        RoomProblem roomProblem = problemService.pickProblemForRoom(roomCode);
        logger.info("Problem picked: {} {}", roomProblem.getContestId(), roomProblem.getProblemIndex());

        // create contest
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusMinutes(30);

        Contest contest = new Contest();
        contest.setRoomProblem(roomProblem);
        contest.setStartTime(startTime);
        contest.setEndTime(endTime);
        contest.setStatus(ContestStatus.ACTIVE);
        contestRepository.save(contest);

        contestRepository.save(contest);

        // update room status
        room.setStatus(RoomStatus.IN_PROGRESS);
        roomRepository.save(room);

        logger.info("Contest created with id: {} for room: {}", contest.getId(), roomCode);

        return Map.of(
                "contestId", contest.getId(),
                "problemName", roomProblem.getProblemName(),
                "problemUrl", roomProblem.getProblemUrl(),
                "rating", roomProblem.getRating(),
                "startTime", startTime,
                "endTime", endTime
        );
    }

    @Override
    public Map<String, Object> checkSubmission(Long contestId) {
        logger.info("Checking submission for contestId: {}", contestId);

        // 1. Verify contest existence and status
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new RuntimeException("Contest not found"));

        if (contest.getStatus() == ContestStatus.COMPLETED) {
            throw new RuntimeException("Contest is already completed");
        }

        // 2. Identify user and verification status
        User user = authUtil.loggedInUser();

        if (!user.isCodeforcesVerified()) {
            throw new RuntimeException("Please verify your Codeforces handle first");
        }

        // 3. IMMUTABILITY CHECK: Return existing data if already solved
        Optional<Submission> existingSubmission = submissionRepository.findByContestAndUser(contest, user);

        if (existingSubmission.isPresent() && existingSubmission.get().isSolved()) {
            Submission sub = existingSubmission.get();
            // Calculate the existing score based on the original failed attempts
            int existingScore = Math.max(0, 100 - (sub.getFailedAttempts() * 5));

            logger.info("User {} has already solved contest {}. Returning existing record.", user.getUserName(), contestId);

            return Map.of(
                    "solved", true,
                    "failedAttempts", sub.getFailedAttempts(),
                    "timeTakenSeconds", sub.getTimeTakenSeconds() != null ? sub.getTimeTakenSeconds() : 0,
                    "score", existingScore,
                    "message", "Problem already solved!"
            );
        }

        // 4. Fetch and filter Codeforces submissions
        RoomProblem roomProblem = contest.getRoomProblem();
        String handle = user.getCodeforcesHandle();

        List<CfSubmission> allSubmissions = codeforcesService.getRecentSubmissions(handle, 30);

        List<CfSubmission> problemSubmissions = allSubmissions.stream()
                .filter(s -> s.getProblem() != null
                        && roomProblem.getContestId().equals(s.getProblem().getContestId())
                        && roomProblem.getProblemIndex().equals(s.getProblem().getIndex()))
                .toList();

        // 5. Analyze results
        int failedAttempts = (int) problemSubmissions.stream()
                .filter(s -> !"OK".equals(s.getVerdict()))
                .count();

        Optional<CfSubmission> acSubmission = problemSubmissions.stream()
                .filter(s -> "OK".equals(s.getVerdict()))
                .findFirst();

        boolean solved = acSubmission.isPresent();

        // 6. Update or create submission record
        Submission submission = existingSubmission.orElse(new Submission());
        submission.setContest(contest);
        submission.setUser(user);
        submission.setFailedAttempts(failedAttempts);
        submission.setSolved(solved);

        int score = 0;

        // 7. Handle first-time success (AC)
        if (solved && submission.getSubmittedAt() == null) {
            submission.setSubmittedAt(LocalDateTime.now());

            long startEpoch = contest.getStartTime().toEpochSecond(ZoneOffset.UTC);
            long acEpoch = acSubmission.get().getCreationTimeSeconds();
            long timeTaken = acEpoch - startEpoch;
            submission.setTimeTakenSeconds(Math.max(timeTaken, 0));

            // Persistence: Official score record
            score = Math.max(0, 100 - (failedAttempts * 5));
            saveScore(contest, user, score);

            // Real-time Update: WebSocket Broadcast
            LeaderboardResponse updatedLeaderboard = getLeaderboard(contestId);
            messagingTemplate.convertAndSend("/topic/contest/" + contestId + "/leaderboard", updatedLeaderboard);

            logger.info("AC found for user: {} in contestId: {} — score: {}", user.getUserName(), contestId, score);
        }

        submissionRepository.save(submission);

        return Map.of(
                "solved", solved,
                "failedAttempts", failedAttempts,
                "timeTakenSeconds", submission.getTimeTakenSeconds() != null ? submission.getTimeTakenSeconds() : 0,
                "score", solved ? Math.max(0, 100 - (failedAttempts * 5)) : 0
        );
    }

    @Override
    public void endContest(Long contestId) {
        logger.info("Ending contest: {}", contestId);

        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new RuntimeException("Contest not found"));

        if (contest.getStatus() == ContestStatus.COMPLETED) {
            throw new RuntimeException("Contest is already completed");
        }

        // assign score=0 to all participants who didn't solve
        List<User> participants = contest.getRoomProblem().getRoom().getParticipants();

        for (User participant : participants) {
            Optional<Score> existingScore = scoreRepository.findByContestAndUser(contest, participant);
            if (existingScore.isEmpty()) {
                logger.info("Assigning score=0 to user: {} for contestId: {}",
                        participant.getUserName(), contestId);
                saveScore(contest, participant, 0);
            }
        }

        contest.setStatus(ContestStatus.COMPLETED);
        contestRepository.save(contest);

        logger.info("Contest {} marked as COMPLETED", contestId);
    }

    private void saveScore(Contest contest, User user, int score) {
        Score scoreEntity = scoreRepository
                .findByContestAndUser(contest, user)
                .orElse(new Score());

        scoreEntity.setContest(contest);
        scoreEntity.setRoom(contest.getRoomProblem().getRoom());
        scoreEntity.setUser(user);
        scoreEntity.setScore(score);
        scoreRepository.save(scoreEntity);

        logger.info("Score saved — user: {}, contestId: {}, score: {}",
                user.getUserName(), contest.getId(), score);
    }

    private void scheduleContestEnd(Long contestId, long delaySeconds) {
        logger.info("Scheduling auto-end for contestId: {} after {}s", contestId, delaySeconds);
        scheduler.schedule(() -> {
            try {
                logger.info("Auto-ending contestId: {}", contestId);
                endContest(contestId);
            } catch (Exception e) {
                logger.error("Failed to auto-end contestId: {} — {}", contestId, e.getMessage());
            }
        }, delaySeconds, TimeUnit.SECONDS);
    }

    @Override
    public List<Map<String, Object>> getContestsByRoom(String roomCode) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        List<Contest> contests = contestRepository.findByRoomProblem_RoomOrderByStartTimeDesc(room);

        return contests.stream().map(contest -> {
            Map<String, Object> map = new HashMap<>();
            map.put("contestId", contest.getId());
            map.put("status", contest.getStatus());
            map.put("startTime", contest.getStartTime().toString());
            map.put("endTime", contest.getEndTime() != null ? contest.getEndTime().toString() : "");
            map.put("problemName", contest.getRoomProblem().getProblemName());
            map.put("problemUrl", contest.getRoomProblem().getProblemUrl());
            map.put("rating", contest.getRoomProblem().getRating() != null ? contest.getRoomProblem().getRating() : "N/A");
            map.put("problemIndex", contest.getRoomProblem().getProblemIndex());
            map.put("cfContestId", contest.getRoomProblem().getContestId());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getContestDetails(Long contestId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new RuntimeException("Contest not found"));
        
        LeaderboardResponse leaderboard = getLeaderboard(contestId);
        
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("contestId", contest.getId());
        map.put("status", contest.getStatus());
        map.put("startTime", contest.getStartTime().toString());
        map.put("endTime", contest.getEndTime() != null ? contest.getEndTime().toString() : "");
        map.put("problemName", contest.getRoomProblem().getProblemName());
        map.put("problemUrl", contest.getRoomProblem().getProblemUrl());
        map.put("rating", contest.getRoomProblem().getRating() != null ? contest.getRoomProblem().getRating() : "N/A");
        map.put("problemIndex", contest.getRoomProblem().getProblemIndex());
        map.put("cfContestId", contest.getRoomProblem().getContestId());
        map.put("leaderboard", leaderboard.getEntries());
        return map;
    }

    @Override
    public LeaderboardResponse getLeaderboard(Long contestId) {
        // 1. Verify contest exists
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new RuntimeException("Contest not found"));

        // 2. Fetch all scores for this contest
        List<Score> scores = scoreRepository.findByContestId(contestId);

        // 3. Fetch all submissions to get time and attempts
        List<Submission> submissions = submissionRepository.findByContestId(contestId);

        // Create a map of UserID -> Submission for quick lookup
        Map<Long, Submission> userSubmissions = submissions.stream()
                .collect(Collectors.toMap(s -> s.getUser().getUserId(), s -> s));

        // 4. Map to DTOs using Score as the primary driver
        List<LeaderboardEntryDTO> entries = scores.stream()
                .map(score -> {
                    Submission sub = userSubmissions.get(score.getUser().getUserId());
                    return new LeaderboardEntryDTO(
                            score.getUser().getUserName(),
                            score.getScore(), // From Score Entity
                            sub != null ? sub.getTimeTakenSeconds() : 0L, // From Submission
                            sub != null ? sub.getFailedAttempts() : 0     // From Submission
                    );
                })
                // 5. Sort: Higher score first, then lower time taken
                .sorted((a, b) -> {
                    if (b.getScore() != a.getScore()) {
                        return b.getScore() - a.getScore();
                    }
                    // Use 0 as default for time comparison if null
                    Long timeA = a.getTimeTakenSeconds() != null ? a.getTimeTakenSeconds() : Long.MAX_VALUE;
                    Long timeB = b.getTimeTakenSeconds() != null ? b.getTimeTakenSeconds() : Long.MAX_VALUE;
                    return timeA.compareTo(timeB);
                })
                .toList();

        return new LeaderboardResponse(contestId, entries);
    }
}