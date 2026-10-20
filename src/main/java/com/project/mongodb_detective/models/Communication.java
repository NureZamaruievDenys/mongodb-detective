package com.project.mongodb_detective.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "communications")
public class Communication {
    @Id
    private String id;

    @Indexed
    private String caseId;

    @Indexed
    private String senderPhone;

    @Indexed
    private String receiverPhone;

    private String type;
    private int duration;
    private LocalDateTime timestamp;
    private String messageText;
}