package com.project.mongodb_detective.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "gps_logs")
public class GpsLog {
    @Id
    private String id;

    @Indexed
    private String caseId;

    @Indexed
    private String citizenId;

    private LocalDateTime timestamp;
    private double x;
    private double y;
}