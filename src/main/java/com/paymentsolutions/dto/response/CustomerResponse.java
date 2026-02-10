package com.paymentsolutions.dto.response;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {



        private UUID id;

        private UUID merchantId;

        private String email;

        private String firstName;

        private String lastName;

        private String fullName;

        private String phone;

        private String address;

        private String city;

        private String state;

        private String postalCode;

        private String country;

        private LocalDateTime createdAt;

        private LocalDateTime updatedAt;
}


