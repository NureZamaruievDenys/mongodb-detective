package com.project.mongodb_detective.repositories;

import com.project.mongodb_detective.models.Case;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaseRepository extends MongoRepository<Case, String> {
}