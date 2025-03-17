package com.example.talepyonetimbackend.talepyonetim.model;

public enum Role {
    ROLE_REQUESTER,         // Talep oluşturan çalışan
    ROLE_APPROVER,          // Talepleri değerlendiren ve sipariş veren çalışan
    ROLE_RECEIVER,          // Teslim alan çalışan
    ROLE_SALESANDMARKETING, // Satış ve pazarlama çalışanı
    ROLE_PRODUCTION         // Üretim çalışanı
}
