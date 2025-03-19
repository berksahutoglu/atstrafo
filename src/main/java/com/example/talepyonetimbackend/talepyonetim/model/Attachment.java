package com.example.talepyonetimbackend.talepyonetim.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "attachments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String fileName;
    private String fileType;
    private String filePath;
    private Long fileSize;
    
    // Dosyanın ilişkili olduğu talep
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private Request request;
    
    // Dosyanın ilişkili olduğu satış talebi
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_request_id")
    private SalesAndMarketingRequest salesRequest;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
    
    private String uploadedBy;
    
    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}
