package com.example.talepyonetimbackend.talepyonetim.model;

public enum ProjectStatus {
    PENDING,           // Beklemede
    IN_PROGRESS,       // Bekleyen Projeler - Onay Sürecinde
    ORDERED,           // Siparişi Verilen Projeler
    IN_PRODUCTION,     // Üretime Verilen Projeler
    COMPLETED,         // Biten Projeler
    CANCELLED          // İptal Edilen Projeler
}
