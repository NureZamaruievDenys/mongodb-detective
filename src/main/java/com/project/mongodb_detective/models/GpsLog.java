package com.project.mongodb_detective.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "gps_logs")
public class GpsLog {
    @Id
    private String id;
    private String caseId;

    private String citizenId;
    private LocalDateTime timestamp;
    private double latitude;
    private double longitude;
}