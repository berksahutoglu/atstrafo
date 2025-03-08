package com.example.talepyonetimbackend.talepyonetim.model;

public enum RequestStatus {
    PENDING,   // Beklemede
    APPROVED,  // Onaylandı
    ORDERED,   // Sipariş Verildi
    DELIVERED, // Teslim Edildi
    REJECTED   // Reddedildi
}
