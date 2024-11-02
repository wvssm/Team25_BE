package com.team25.backend.repository;

import com.team25.backend.entity.Manager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ManagerRepository extends JpaRepository<Manager, Long> {
  List<Manager> findByWorkingRegion(String workingRegion);
  Optional<Manager> findByUserId(Long userId);
}