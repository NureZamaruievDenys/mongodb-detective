package com.project.mongodb_detective.models;

import com.project.mongodb_detective.models.enums.RelationType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "citizens")
public class Citizen {
    @Id
    private String id;
    private String caseId;

    private String fullName;
    private int age;
    private String phone;
    private String bankAccount;
    private String criminalRecord;

    private List<String> physicalTraits;
    private Vehicle vehicle;
    private List<Relationship> relationships;

    private boolean isSuspect;

    @Data
    public static class Vehicle {
        private String color;
        private String plate;
        private String model;
    }

    @Data
    public static class Relationship {
        private String citizenId;
        private RelationType relationType;
    }
}