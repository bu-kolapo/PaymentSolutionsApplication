package com.paymentsolutions.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CBSResponse {
    private boolean success;
    private String cbsReference;
    private String message;
    private String errorCode;
}