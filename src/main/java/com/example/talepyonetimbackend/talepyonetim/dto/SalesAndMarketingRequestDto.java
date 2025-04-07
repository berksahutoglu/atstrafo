package com.example.talepyonetimbackend.talepyonetim.dto;

import com.example.talepyonetimbackend.talepyonetim.model.MarketType;
import com.example.talepyonetimbackend.talepyonetim.model.SalesRequestStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SalesAndMarketingRequestDto {
    private Long id;
    
    @NotNull(message = "Yurtiçi/Yurtdışı bilgisi gereklidir")
    private MarketType marketType; // yurtiçi/yurtdışı
    
    // Geröye uyumluluk için
    private Boolean isDomestic;
    
    @NotBlank(message = "Ülke bilgisi gereklidir")
    private String country; // ülke
    
    @NotBlank(message = "Müşteri ismi gereklidir")
    private String customerName; // müşteri ismi
    
    @NotBlank(message = "Güç bilgisi gereklidir")
    private String power; // güç
    
    @NotNull(message = "Adet bilgisi gereklidir")
    @Min(value = 1, message = "Adet en az 1 olmalıdır")
    private Integer quantity; // adet
    
    @NotBlank(message = "Çıkış gücü bilgisi gereklidir")
    private String outputPower; // çıkış gücü
    
    @NotBlank(message = "Kazan tipi gereklidir")
    private String boilerType; // kazan tipi
    
    @NotBlank(message = "Sargı tipi gereklidir")
    private String windingType; // sargı tipi
    
    // Yurtiçi ise a plus/normal
    private boolean isAPlus;
    
    @NotNull(message = "Talep edilen teslim tarihi gereklidir")
    private LocalDate requestedDeliveryDate; // müşterinin talep ettiği teslim tarihi
    
    private String createdByName;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private SalesRequestStatus status;
    
    private String notes;
    
    // İlişkili üretim talepleri bilgisi
    private java.util.List<RequestDto> productionRequests;
    
    // Proje bilgisi
    private Long projectId;
    private String projectName;
    
    // Geriye uyumluluk için getter metodu
    public RequestDto getProductionRequest() {
        if (productionRequests != null && !productionRequests.isEmpty()) {
            return productionRequests.get(0);
        }
        return null;
    }
}
