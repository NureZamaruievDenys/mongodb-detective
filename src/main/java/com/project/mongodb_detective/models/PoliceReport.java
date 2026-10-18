package com.project.mongodb_detective.models;

import com.project.mongodb_detective.models.enums.ReportType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "police_reports")
public class PoliceReport {
    @Id
    private String id;
    private String caseId;

    private String author;
    private ReportType reportType;
    private String textContent;
    private List<String> tags;
}