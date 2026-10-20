package com.project.mongodb_detective.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "camera_logs")
public class CameraLog {
    @Id
    private String id;

    @Indexed
    private String caseId;

    private String cameraId;
    private double x;
    private double y;
    private LocalDateTime timestamp;

    private String description;
}