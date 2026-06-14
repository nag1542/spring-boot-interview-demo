package com.interviewprep.platform.repository;

import com.interviewprep.platform.domain.UserAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAuditLogRepository extends JpaRepository<UserAuditLog, Long> {

    long countByUserEmail(String email);
}
