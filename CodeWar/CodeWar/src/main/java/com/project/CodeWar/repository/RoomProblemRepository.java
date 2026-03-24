package com.project.CodeWar.repository;

import com.project.CodeWar.entity.Room;
import com.project.CodeWar.entity.RoomProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomProblemRepository extends JpaRepository<RoomProblem, Long> {

    List<RoomProblem> findByRoom(Room room);

    Optional<RoomProblem> findTopByRoomOrderByAssignedAtDesc(Room room);

    boolean existsByRoomAndContestIdAndProblemIndex(Room room, Integer contestId, String problemIndex);
}