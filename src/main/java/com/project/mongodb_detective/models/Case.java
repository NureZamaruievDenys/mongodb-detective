package com.project.mongodb_detective.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "cases")
public class Case {
    @Id
    private String id;
    private String description;
    private LocalDateTime createdAt;
    private String status;
    private String killerId;
    private String victimId;
    private List<String> decoyIds;
    private List<String> bystanderIds;
    private String motive;
}