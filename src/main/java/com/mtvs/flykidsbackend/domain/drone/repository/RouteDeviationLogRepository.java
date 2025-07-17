package com.mtvs.flykidsbackend.domain.drone.repository;

import com.mtvs.flykidsbackend.domain.drone.entity.RouteDeviationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteDeviationLogRepository extends JpaRepository<RouteDeviationLog, Long> {
}
