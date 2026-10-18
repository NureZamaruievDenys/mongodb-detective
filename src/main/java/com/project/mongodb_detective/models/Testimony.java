package com.project.mongodb_detective.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "testimonies")
public class Testimony {
    @Id
    private String id;
    private String caseId;

    private String speakerCitizenId;
    private String aboutCitizenId;
    private String statement;
    private boolean isLie;
}