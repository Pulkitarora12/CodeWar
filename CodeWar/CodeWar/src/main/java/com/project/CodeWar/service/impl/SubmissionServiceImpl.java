package com.project.CodeWar.service.impl;

import com.project.CodeWar.entity.*;
import com.project.CodeWar.repository.*;
import com.project.CodeWar.security.util.AuthUtil;
import com.project.CodeWar.service.SubmissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionServiceImpl.class);

    private static final String CF_SUBMISSIONS_URL =
            "https://codeforces.com/api/user.status?handle={handle}&count=20";

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ContestRepository contestRepository;

    @Autowired
    private RoomProblemRepository roomProblemRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private ScoreEntryRepository scoreEntryRepository;

    @Autowired
    private AuthUtil authUtil;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Transactional
    public Submission checkAndRecordSubmission(String roomCode) {
        logger.info("Checking submissions for room: {}", roomCode);

        // Step 1 — get room
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomCode));

        // Step 2 — get active contest for this room
        Contest contest = contestRepository.findByRoom(room)
                .orElseThrow(() -> new RuntimeException("No contest found for room: " + roomCode));

        if (contest.getStatus() != ContestStatus.ONGOING) {
            throw new RuntimeException("Contest is not ongoing for room: " + roomCode);
        }

        // Step 3 — get current user
        User user = authUtil.loggedInUser();

        if (user.getCodeforcesHandle() == null || user.getCodeforcesHandle().isBlank()) {
            throw new RuntimeException("User has no Codeforces handle linked");
        }

        // Step 4 — if user already has an AC, skip CF API call entirely
        boolean alreadyAc = submissionRepository.existsByContestAndUserAndResult(
                contest, user, SubmissionResult.AC);
        if (alreadyAc) {
            logger.info("User {} already has AC in this contest, skipping CF API call", user.getUserName());
            return submissionRepository.findByContestAndUser(contest, user)
                    .orElseThrow(() -> new RuntimeException("Submission not found despite AC flag"));
        }

        // Step 5 — get current problem for this room
        RoomProblem currentProblem = roomProblemRepository
                .findTopByRoomOrderByAssignedAtDesc(room)
                .orElseThrow(() -> new RuntimeException("No problem assigned to room: " + roomCode));

        // Step 6 — call CF API to fetch recent submissions by user
        logger.info("Calling CF API for handle: {}", user.getCodeforcesHandle());
        Map response = restTemplate.getForObject(
                CF_SUBMISSIONS_URL,
                Map.class,
                user.getCodeforcesHandle()
        );

        if (response == null || !"OK".equals(response.get("status"))) {
            throw new RuntimeException("Failed to fetch submissions from Codeforces");
        }

        List<Map<String, Object>> cfSubmissions = (List<Map<String, Object>>) response.get("result");

        // Step 7 — find the latest submission for the current problem
        Map<String, Object> matched = cfSubmissions.stream()
                .filter(sub -> {
                    Map<String, Object> problem = (Map<String, Object>) sub.get("problem");
                    Integer contestId = (Integer) problem.get("contestId");
                    String index = (String) problem.get("index");
                    return currentProblem.getContestId().equals(contestId)
                            && currentProblem.getProblemIndex().equals(index);
                })
                .findFirst()
                .orElse(null);

        if (matched == null) {
            logger.info("No submission found on CF for problem {} {} by user {}",
                    currentProblem.getContestId(), currentProblem.getProblemIndex(), user.getUserName());
            return null;
        }

        // Step 8 — map CF verdict to our SubmissionResult enum
        String verdict = (String) matched.get("verdict");
        SubmissionResult result = mapVerdict(verdict);
        logger.info("CF verdict: {} → mapped to: {}", verdict, result);

        // Step 9 — save submission to DB
        Submission submission = new Submission();
        submission.setContest(contest);
        submission.setProblem(currentProblem);
        submission.setUser(user);
        submission.setResult(result);
        submissionRepository.save(submission);
        logger.info("Submission saved for user: {} with result: {}", user.getUserName(), result);

        // Step 10 — update score entry
        if (result == SubmissionResult.AC) {
            ScoreEntry scoreEntry = scoreEntryRepository
                    .findByContestAndUser(contest, user)
                    .orElseGet(() -> {
                        ScoreEntry newEntry = new ScoreEntry();
                        newEntry.setContest(contest);
                        newEntry.setUser(user);
                        newEntry.setScore(0);
                        return newEntry;
                    });

            scoreEntry.setScore(100);
            scoreEntryRepository.save(scoreEntry);
            logger.info("Score set to 100 for user: {}", user.getUserName());
        }

        return submission;
    }

    private SubmissionResult mapVerdict(String verdict) {
        if (verdict == null) return SubmissionResult.TESTING;
        return switch (verdict) {
            case "OK"                      -> SubmissionResult.AC;
            case "WRONG_ANSWER"            -> SubmissionResult.WRONG_ANSWER;
            case "TIME_LIMIT_EXCEEDED"     -> SubmissionResult.TIME_LIMIT_EXCEEDED;
            case "MEMORY_LIMIT_EXCEEDED"   -> SubmissionResult.MEMORY_LIMIT_EXCEEDED;
            case "RUNTIME_ERROR"           -> SubmissionResult.RUNTIME_ERROR;
            case "COMPILATION_ERROR"       -> SubmissionResult.COMPILATION_ERROR;
            case "IDLENESS_LIMIT_EXCEEDED" -> SubmissionResult.IDLENESS_LIMIT_EXCEEDED;
            case "PARTIAL"                 -> SubmissionResult.PARTIAL;
            case "SKIPPED", "CHALLENGED"   -> SubmissionResult.SKIPPED;
            case "TESTING"                 -> SubmissionResult.TESTING;
            default                        -> SubmissionResult.OTHER;
        };
    }
}