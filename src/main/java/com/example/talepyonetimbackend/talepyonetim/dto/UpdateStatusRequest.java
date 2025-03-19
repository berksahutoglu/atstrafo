package com.example.talepyonetimbackend.talepyonetim.dto;

import com.example.talepyonetimbackend.talepyonetim.model.RequestStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateStatusRequest {
    @NotNull(message = "Durum gerekli")
    private RequestStatus status;

    private String comment;
    private String orderNumber;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate estimatedDeliveryDate; // Tahmini Termin Tarihi
}
