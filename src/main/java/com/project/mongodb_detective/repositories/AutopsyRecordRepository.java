package com.project.mongodb_detective.repositories;

import com.project.mongodb_detective.models.AutopsyRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutopsyRecordRepository extends MongoRepository<AutopsyRecord, String> {
    List<AutopsyRecord> findAllByCaseId(String caseId);
}