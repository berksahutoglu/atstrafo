package com.example.talepyonetimbackend.talepyonetim.dto;

import com.example.talepyonetimbackend.talepyonetim.model.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDto {
    private Long id;
    
    private String orderNumber;
    
    private OrderStatus status;
    
    private String createdByName;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private String notes;
    
    @NotNull(message = "En az bir talep gereklidir")
    @NotEmpty(message = "En az bir talep gereklidir")
    @Valid
    private List<RequestDto> requests;
}
