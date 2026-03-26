package com.project.CodeWar.repository;

import com.project.CodeWar.entity.Contest;
import com.project.CodeWar.entity.Submission;
import com.project.CodeWar.entity.SubmissionResult;
import com.project.CodeWar.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    // check if a user already has an AC in this contest (to stop polling)
    boolean existsByContestAndUserAndResult(Contest contest, User user, SubmissionResult result);

    // all submissions for a contest (for leaderboard queries)
    List<Submission> findByContest(Contest contest);

    // all accepted submissions for a contest
    List<Submission> findByContestAndResult(Contest contest, SubmissionResult result);

    // get a specific user's submission in a contest
    Optional<Submission> findByContestAndUser(Contest contest, User user);
}