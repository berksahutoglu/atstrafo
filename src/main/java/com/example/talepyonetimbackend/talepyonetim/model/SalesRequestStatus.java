package com.example.talepyonetimbackend.talepyonetim.model;

public enum SalesRequestStatus {
    PENDING,          // Bekliyor (Yeni oluşturuldu)
    PROCESSING,       // İşleniyor (Üretim tarafından işleme alındı)
    CONVERTED,        // Talebe Dönüştürüldü (Üretim tarafından talep oluşturuldu)
    APPROVED,         // Onaylandı (Onay sürecinden geçti)
    ORDERED,          // Sipariş Verildi
    DELIVERED,        // Teslim Edildi
    COMPLETED,        // Tamamlandı
    CANCELLED         // İptal Edildi
}
