package com.project.mongodb_detective.models;

import com.project.mongodb_detective.models.enums.WeaponType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "autopsy_records")
public class AutopsyRecord {
    @Id
    private String id;

    @Indexed
    private String caseId;

    @Indexed
    private String victimId;

    private boolean gender;
    private int estimatedAge;
    private List<String> physicalTraits;
    private LocalDateTime estimatedDeathTime;
    private String causeOfDeath;
    private WeaponType weaponType;
}