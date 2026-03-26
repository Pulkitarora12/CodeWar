package com.project.CodeWar.repository;

import com.project.CodeWar.entity.Contest;
import com.project.CodeWar.entity.Room;
import com.project.CodeWar.entity.Score;
import com.project.CodeWar.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {

    Optional<Score> findByContestAndUser(Contest contest, User user);

    List<Score> findByContest(Contest contest);
}