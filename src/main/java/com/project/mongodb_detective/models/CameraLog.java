package com.project.mongodb_detective.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "camera_logs")
public class CameraLog {
    @Id
    private String id;
    private String caseId;

    private String cameraId;
    private double latitude;
    private double longitude;
    private LocalDateTime timestamp;

    private List<String> spottedTraits;
    private SpottedVehicle spottedVehicle;
    private double confidenceScore;

    @Data
    public static class SpottedVehicle {
        private String color;
        private String partialPlate;
    }
}