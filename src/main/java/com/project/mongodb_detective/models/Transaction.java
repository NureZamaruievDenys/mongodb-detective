package com.project.mongodb_detective.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "transactions")
public class Transaction {
    @Id
    private String id;

    @Indexed
    private String caseId;

    @Indexed
    private String fromAccount;

    @Indexed
    private String toAccount;

    private double amount;
    private LocalDateTime timestamp;
    private String purpose;
}