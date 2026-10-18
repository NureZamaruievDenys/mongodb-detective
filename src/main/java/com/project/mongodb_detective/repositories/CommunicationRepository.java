package com.project.mongodb_detective.repositories;

import com.project.mongodb_detective.models.Communication;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunicationRepository extends MongoRepository<Communication, String> {
    List<Communication> findAllByCaseId(String caseId);
}