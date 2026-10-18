package com.project.mongodb_detective.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "communications")
public class Communication {
    @Id
    private String id;
    private String caseId;

    private String senderPhone;
    private String receiverPhone;
    private LocalDateTime timestamp;
    private String messageText;
}