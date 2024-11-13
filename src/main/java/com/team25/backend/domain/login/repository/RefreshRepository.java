package com.team25.backend.domain.login.repository;

import com.team25.backend.domain.login.entity.Refresh;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshRepository extends JpaRepository<Refresh, Long> {
    Boolean existsByRefresh(String refresh);
    void deleteByRefresh(String refresh);
}
