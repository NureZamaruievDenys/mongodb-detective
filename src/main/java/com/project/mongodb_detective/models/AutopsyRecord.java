package com.project.mongodb_detective.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "autopsy_records")
public class AutopsyRecord {
    @Id
    private String id;
    private String caseId;

    private String victimId;
    private String victimName;
    private LocalDateTime estimatedDeathTime;
    private String causeOfDeath;
    private String weaponDetails;
    private String doctorNotes;
}