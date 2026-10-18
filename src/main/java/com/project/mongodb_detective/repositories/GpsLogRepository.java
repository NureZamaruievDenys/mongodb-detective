package com.project.mongodb_detective.repositories;

import com.project.mongodb_detective.models.GpsLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GpsLogRepository extends MongoRepository<GpsLog, String> {
    List<GpsLog> findAllByCaseId(String caseId);
}