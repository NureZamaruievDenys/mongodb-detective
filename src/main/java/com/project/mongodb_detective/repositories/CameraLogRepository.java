package com.project.mongodb_detective.repositories;

import com.project.mongodb_detective.models.CameraLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CameraLogRepository extends MongoRepository<CameraLog, String> {
    List<CameraLog> findAllByCaseId(String caseId);
}