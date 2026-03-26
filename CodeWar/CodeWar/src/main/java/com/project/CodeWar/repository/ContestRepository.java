package com.project.CodeWar.repository;

import com.project.CodeWar.entity.Contest;
import com.project.CodeWar.entity.ContestStatus;
import com.project.CodeWar.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ContestRepository extends JpaRepository<Contest, Long> {

    Optional<Contest> findByRoom(Room room);

    Optional<Contest> findByRoomRoomCode(String roomCode);

    List<Contest> findByStatus(ContestStatus status);
}