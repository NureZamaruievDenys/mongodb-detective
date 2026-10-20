package com.project.mongodb_detective.models;

import com.project.mongodb_detective.models.enums.RelationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "citizens")
public class Citizen {
    @Id
    private String id;

    @Indexed
    @NotBlank(message = "ID must be not null")
    private String caseId;

    @NotBlank(message = "Name cannot be empty")
    private String fullName;

    @Min(value = 18, message = "Person must be an adult")
    @Max(value = 120, message = "Invalid age")
    private int age;

    @NotNull(message = "Gender must be specified")
    private Boolean gender;

    @Indexed
    private String phone;

    @Indexed
    private String bankAccount;

    private List<String> physicalTraits;

    @Valid
    private Vehicle vehicle;

    @Valid
    private List<Relationship> relationships;

    private boolean isSuspect;

    @Data
    public static class Vehicle {
        private String color;
        private String plate;
    }

    @Data
    public static class Relationship {
        @NotBlank(message = "ID must be not null")
        private String citizenId;

        @NotNull(message = "Relation type is required")
        private RelationType relationType;

        @Min(value = -5, message = "Affinity cannot be less than -5")
        @Max(value = 5, message = "Affinity cannot be greater than 5")
        private int affinity;
    }
}