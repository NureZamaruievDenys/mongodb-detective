package com.project.mongodb_detective.repositories;

import com.project.mongodb_detective.models.Testimony;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestimonyRepository extends MongoRepository<Testimony, String> {
    List<Testimony> findAllByCaseId(String caseId);
}