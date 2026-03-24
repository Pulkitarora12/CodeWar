package com.project.CodeWar.repository;

import com.project.CodeWar.entity.Room;
import com.project.CodeWar.entity.RoomStatus;
import com.project.CodeWar.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByRoomCode(String roomCode);

    List<Room> findByCreatedBy(User user);

    List<Room> findByStatus(RoomStatus status);

    boolean existsByRoomCode(String roomCode);

    List<Room> findByParticipantsContaining(User user);
}