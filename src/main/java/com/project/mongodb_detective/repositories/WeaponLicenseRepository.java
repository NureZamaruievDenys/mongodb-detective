package com.project.mongodb_detective.repositories;

import com.project.mongodb_detective.models.WeaponLicense;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeaponLicenseRepository extends MongoRepository<WeaponLicense, String> {
    List<WeaponLicense> findAllByCaseId(String caseId);
}