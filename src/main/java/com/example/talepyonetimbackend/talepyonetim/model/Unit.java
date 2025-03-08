package com.example.talepyonetimbackend.talepyonetim.model;

public enum Unit {
    KILOGRAM("kg"),    // Kilogram
    METER("metre"),    // Metre
    TON("ton"),        // Ton
    PIECE("adet"),     // Adet
    LITER("litre"),    // Litre
    PACKAGE("paket"),  // Paket
    BOX("kutu"),       // Kutu
    PALLET("palet"),
    SIZE("boy");
    
    private String displayName;
    
    Unit(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
