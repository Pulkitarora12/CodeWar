package com.project.CodeWar.repository;

import com.project.CodeWar.entity.Contest;
import com.project.CodeWar.entity.ContestStatus;
import com.project.CodeWar.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContestRepository extends JpaRepository<Contest, Long> {

    Optional<Contest> findByRoomProblem_RoomAndStatus(Room room, ContestStatus status);

    List<Contest> findByStatus(ContestStatus status);
}