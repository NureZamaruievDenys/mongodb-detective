package com.project.mongodb_detective.repositories;

import com.project.mongodb_detective.models.Citizen;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CitizenRepository extends MongoRepository<Citizen, String> {
    List<Citizen> findAllByCaseId(String caseId);
}