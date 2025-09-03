package org.project.demo.repository;

import org.project.demo.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository  extends JpaRepository<AuditLog, Long> {
}
