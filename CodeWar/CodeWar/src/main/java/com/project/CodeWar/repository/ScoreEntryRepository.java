package com.project.CodeWar.repository;

import com.project.CodeWar.entity.Contest;
import com.project.CodeWar.entity.ScoreEntry;
import com.project.CodeWar.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScoreEntryRepository extends JpaRepository<ScoreEntry, Long> {

    Optional<ScoreEntry> findByContestAndUser(Contest contest, User user);

    // returns entries sorted by score descending — this is the leaderboard query
    List<ScoreEntry> findByContestOrderByScoreDesc(Contest contest);
}