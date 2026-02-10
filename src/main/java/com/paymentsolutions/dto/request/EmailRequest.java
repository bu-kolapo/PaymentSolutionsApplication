package com.paymentsolutions.dto.request;



import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

    /**
     * Request DTO for sending emails.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public class EmailRequest {

        @NotBlank(message = "Recipient email is required")
        @Email(message = "Invalid email format")
        private String to;

        private List<String> cc;

        private List<String> bcc;

        @NotBlank(message = "Subject is required")
        private String subject;

        @NotBlank(message = "Email body is required")
        private String body;

        private boolean isHtml;

        private List<String> attachments;
    }

