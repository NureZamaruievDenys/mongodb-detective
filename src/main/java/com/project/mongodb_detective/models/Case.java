package com.project.mongodb_detective.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "case")
@Data
public class Case {
    @Id
    private String id;
    private CaseType caseType;
    private String description;
    private LocalDateTime date;

    public enum CaseType {THEFT, MURER};
}
