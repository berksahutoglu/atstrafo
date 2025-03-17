package com.example.talepyonetimbackend.talepyonetim.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales_and_marketing_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesAndMarketingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MarketType marketType; // yurtiçi/yurtdışı

    @Column(nullable = false)
    private String country; // ülke

    @Column(nullable = false)
    private String power; // güç

    @Column(nullable = false)
    private Integer quantity; // adet

    @Column(nullable = false)
    private String outputPower; // çıkış gücü

    // Yurtiçi ise a plus/normal
    private boolean isAPlus;

    @Column(nullable = false)
    private LocalDate requestedDeliveryDate; // müşterinin talep ettiği teslim tarihi

    @ManyToOne
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SalesRequestStatus status;

    private String notes;

    // Üretimle ilişki
    @OneToOne(mappedBy = "salesRequest")
    private Request productionRequest;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Lombok @Data ile oluşturulacak getter'ı geçersiz kıl
    // İleriye uyumlu olması için eski isDomestic() getter'ını koruyalım
    public boolean isDomestic() {
        return this.marketType == MarketType.DOMESTIC;
    }
}
