package com.mtvs.flykidsbackend.drone.repository;

import com.mtvs.flykidsbackend.drone.entity.RouteDeviationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteDeviationLogRepository extends JpaRepository<RouteDeviationLog, Long> {
}
