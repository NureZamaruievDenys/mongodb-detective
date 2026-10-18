package com.project.mongodb_detective.models;

import com.project.mongodb_detective.models.enums.CaseType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "cases")
public class Case {
    @Id
    private String id;
    private CaseType caseType;
    private String title;
    private LocalDateTime createdAt;
    private String status;
}