package com.project.mongodb_detective.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "testimonies")
@CompoundIndex(name = "unique_interrogation_idx", def = "{'caseId': 1, 'speakerCitizenId': 1, 'aboutCitizenId': 1}")
public class Testimony {
    @Id
    private String id;
    private String caseId;

    private String speakerCitizenId;
    private String aboutCitizenId;
    private String statement;
    private boolean isSuspect;
}