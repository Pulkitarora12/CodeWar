package com.project.CodeWar.service.impl;

import com.project.CodeWar.entity.*;
import com.project.CodeWar.repository.*;
import com.project.CodeWar.security.util.AuthUtil;
import com.project.CodeWar.service.ContestService;
import com.project.CodeWar.service.ProblemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContestServiceImpl implements ContestService {

    private static final Logger logger = LoggerFactory.getLogger(ContestServiceImpl.class);

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ContestRepository contestRepository;

    @Autowired
    private ProblemService problemService;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private ScoreEntryRepository scoreEntryRepository;

    @Override
    @Transactional
    public Contest startContest(String roomCode) {
        logger.info("Starting contest for room: {}", roomCode);

        // Step 1 — get room
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomCode));

        // Step 2 — validate caller is the room creator
        User currentUser = authUtil.loggedInUser();
        if (!room.getCreatedBy().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("Only the room creator can start the contest");
        }

        // Step 3 — make sure room is in WAITING state
        if (room.getStatus() == RoomStatus.WAITING) {
            throw new RuntimeException("Contest can only be started when room is in WAITING state or IN_PROGRESS state");
        }

        // Step 4 — make sure a contest doesn't already exist for this room
        if (contestRepository.findByRoom(room).isPresent()) {
            throw new RuntimeException("A contest already exists for this room");
        }

        // Step 5 — update room status to ACTIVE
        room.setStatus(RoomStatus.IN_PROGRESS);
        roomRepository.save(room);
        logger.info("Room {} status updated to ACTIVE", roomCode);

        // Step 6 — create and persist the contest
        Contest contest = new Contest();
        contest.setRoom(room);
        contest.setStatus(ContestStatus.ONGOING);
        contestRepository.save(contest);
        logger.info("Contest created for room: {}", roomCode);

        // Step 7 — pick a problem for the room
        RoomProblem problem = problemService.pickProblemForRoom(roomCode);
        logger.info("Problem assigned to room {}: {} {}", roomCode, problem.getContestId(), problem.getProblemIndex());

        return contest;
    }

    @Override
    @Transactional
    public void endContest(Long contestId) {
        logger.info("Ending contest: {}", contestId);

        // Step 1 — get contest
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new RuntimeException("Contest not found: " + contestId));

        if (contest.getStatus() != ContestStatus.ONGOING) {
            throw new RuntimeException("Contest is not ongoing: " + contestId);
        }

        // Step 2 — get room from contest
        Room room = contest.getRoom();

        // Step 3 — for each participant without a ScoreEntry, give them 0
        for (User participant : room.getParticipants()) {
            boolean hasEntry = scoreEntryRepository
                    .findByContestAndUser(contest, participant)
                    .isPresent();

            if (!hasEntry) {
                ScoreEntry zeroEntry = new ScoreEntry();
                zeroEntry.setContest(contest);
                zeroEntry.setUser(participant);
                zeroEntry.setScore(0);
                scoreEntryRepository.save(zeroEntry);
                logger.info("Score set to 0 for user: {}", participant.getUserName());
            }
        }

        // Step 4 — mark contest as FINISHED
        contest.setStatus(ContestStatus.FINISHED);
        contestRepository.save(contest);

        // Step 5 — mark room as COMPLETED
        room.setStatus(RoomStatus.COMPLETED);
        roomRepository.save(room);

        logger.info("Contest {} ended", contestId);
    }
}