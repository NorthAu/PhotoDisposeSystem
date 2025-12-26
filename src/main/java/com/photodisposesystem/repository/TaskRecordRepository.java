package com.photodisposesystem.repository;

import com.photodisposesystem.model.TaskRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRecordRepository extends JpaRepository<TaskRecord, Long> {
    List<TaskRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
}
