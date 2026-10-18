package com.project.mongodb_detective.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@Document(collection = "weapon_licenses")
public class WeaponLicense {
    @Id
    private String id;
    private String caseId;

    private String citizenId;
    private String weaponType;
    private String caliber;
    private String serialNumber;
    private LocalDate issueDate;
}