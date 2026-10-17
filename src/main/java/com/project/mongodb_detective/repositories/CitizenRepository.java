package com.project.mongodb_detective.repositories;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "citizen")
@Data
public class CitizenRepository {
    @Id
    private String id;
    private String caseId;
    private String fullName, phone, bank;
    private int age;
    private List<String> physicalTraits;
    private Vehicle vehicle;

    @Data
    public static class Vehicle {
        private String model, color, plateNumber;
        private LocalDateTime registrationDate;
    }
    private List<Relation> relations;

    @Data
    public static class Relation {
        private String citizenId;
        private RelationType relationType;
    }
    private boolean isSuspect;
    private enum RelationType {PARENT, CHILD, HUSBAND, WIFE, FRIEND, BOSS};
}
