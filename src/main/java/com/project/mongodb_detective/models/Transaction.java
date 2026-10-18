package com.project.mongodb_detective.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "transactions")
public class Transaction {
    @Id
    private String id;
    private String caseId;

    private String fromAccount;
    private String toAccount;
    private double amount;
    private LocalDateTime timestamp;
    private String purpose;
}