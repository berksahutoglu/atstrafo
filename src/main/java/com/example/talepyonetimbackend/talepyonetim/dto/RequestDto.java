package com.example.talepyonetimbackend.talepyonetim.dto;

import com.example.talepyonetimbackend.talepyonetim.model.RequestStatus;
import com.example.talepyonetimbackend.talepyonetim.model.Unit;
import com.example.talepyonetimbackend.talepyonetim.model.Urgency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RequestDto {
    private Long id;

    @NotBlank(message = "Başlık gerekli")
    private String title;

    private String description;

    @NotNull(message = "Miktar gerekli")
    @Positive(message = "Miktar pozitif olmalı")
    private Integer quantity;
    
    @NotNull(message = "Birim gerekli")
    private Unit unit = Unit.PIECE; // Varsayılan değer: adet

    @NotNull(message = "Aciliyet seviyesi gerekli")
    private Urgency urgency;

    private RequestStatus status;

    private String requesterName;
    private String approverName;
    private String receiverName;

    private String comment;
    private String orderNumber;
    private LocalDateTime orderDate;

    private String deliveryNotes;
    private LocalDateTime deliveryDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Sipariş için
    private Long orderId;
    
    // Üretim departmanı tarafından oluşturuldu mu?
    private boolean createdByProduction;
    
    // Satış ve pazarlama talebi ile ilişki
    private Long salesRequestId;
}
