package com.project.CodeWar.repository;

import com.project.CodeWar.entity.AppRole;
import com.project.CodeWar.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(AppRole appRole);
}
