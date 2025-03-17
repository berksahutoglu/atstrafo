package com.example.talepyonetimbackend.talepyonetim.model;

public enum OrderStatus {
    DRAFT,          // Taslak (Henüz tamamlanmamış)
    PENDING,        // Beklemede (Onay bekliyor)
    APPROVED,       // Onaylandı
    PROCESSING,     // İşleniyor (Tedarik süreci devam ediyor)
    DELIVERED,      // Teslim Edildi
    COMPLETED,      // Tamamlandı
    CANCELLED       // İptal Edildi
}
