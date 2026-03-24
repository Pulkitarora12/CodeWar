package com.project.CodeWar.service.impl;

import com.project.CodeWar.dtos.CfProblem;
import com.project.CodeWar.dtos.CfProblemsetResponse;
import com.project.CodeWar.entity.Room;
import com.project.CodeWar.entity.RoomProblem;
import com.project.CodeWar.repository.RoomProblemRepository;
import com.project.CodeWar.repository.RoomRepository;
import com.project.CodeWar.service.ProblemService;
import com.project.CodeWar.service.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class ProblemServiceImpl implements ProblemService {

    private static final Logger logger = LoggerFactory.getLogger(ProblemServiceImpl.class);

    private static final String CF_PROBLEMSET_URL = "https://codeforces.com/api/problemset.problems";

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomProblemRepository roomProblemRepository;

    @Autowired
    private RoomService roomService;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public RoomProblem pickProblemForRoom(String roomCode) {
        logger.info("Picking problem for room: {}", roomCode);

        // Step 1 — get room
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Step 2 — calculate problem rating
        int problemRating = roomService.calculateProblemRating(roomCode);
        logger.info("Calculated problem rating: {} for room: {}", problemRating, roomCode);

        // Step 3 — fetch all problems from CF API
        logger.info("Hitting CF problemset API");
        CfProblemsetResponse response = restTemplate.getForObject(CF_PROBLEMSET_URL, CfProblemsetResponse.class);

        if (response == null || !"OK".equals(response.getStatus())) {
            throw new RuntimeException("Could not fetch problems from Codeforces");
        }

        // Step 4 — filter by rating, exclude used, sort by contestId descending (latest first)
        List<CfProblem> filtered = response.getResult().getProblems().stream()
                .filter(p -> p.getRating() != null && p.getRating().equals(problemRating))
                .filter(p -> !roomProblemRepository.existsByRoomAndContestIdAndProblemIndex(
                        room, p.getContestId(), p.getIndex()))
                .sorted(Comparator.comparingInt(CfProblem::getContestId).reversed()) // ← sort latest first
                .collect(Collectors.toList());

        logger.info("Found {} eligible problems with rating: {}", filtered.size(), problemRating);

        if (filtered.isEmpty()) {
            throw new RuntimeException("No eligible problems found for rating: " + problemRating);
        }

        // Step 5 — pick randomly from top N latest problems only
        int poolSize = Math.min(50, filtered.size()); // ← only consider the 50 most recent
        CfProblem picked = filtered.get(new Random().nextInt(poolSize));
        logger.info("Picked problem: {} {} — {}", picked.getContestId(), picked.getIndex(), picked.getName());

        // Step 6 — save to DB
        RoomProblem roomProblem = new RoomProblem();
        roomProblem.setRoom(room);
        roomProblem.setContestId(picked.getContestId());
        roomProblem.setProblemIndex(picked.getIndex());
        roomProblem.setProblemName(picked.getName());
        roomProblem.setRating(picked.getRating());
        roomProblem.setProblemUrl("https://codeforces.com/problemset/problem/"
                + picked.getContestId() + "/" + picked.getIndex());
        roomProblemRepository.save(roomProblem);

        logger.info("Problem saved to DB for room: {}", roomCode);
        return roomProblem;
    }
}