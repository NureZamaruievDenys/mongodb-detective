package com.project.mongodb_detective.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@Document(collection = "weapon_licenses")
public class WeaponLicense {
    @Id
    private String id;

    @Indexed
    private String caseId;

    @Indexed
    private String citizenId;

    private String weaponType;
    private String serialNumber;
    private LocalDate issueDate;
}