package com.example.talepyonetimbackend.talepyonetim.controller;

import com.example.talepyonetimbackend.talepyonetim.dto.RequestDto;
import com.example.talepyonetimbackend.talepyonetim.dto.SalesAndMarketingRequestDto;
import com.example.talepyonetimbackend.talepyonetim.service.SalesAndMarketingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SalesAndMarketingController {

    private final SalesAndMarketingService salesService;

    @Autowired
    public SalesAndMarketingController(SalesAndMarketingService salesService) {
        this.salesService = salesService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_SALESANDMARKETING')")
    public ResponseEntity<SalesAndMarketingRequestDto> createSalesRequest(
            @Valid @RequestBody SalesAndMarketingRequestDto requestDto) {
        return new ResponseEntity<>(salesService.createSalesRequest(requestDto), HttpStatus.CREATED);
    }

    @GetMapping("/my-requests")
    @PreAuthorize("hasAuthority('ROLE_SALESANDMARKETING')")
    public ResponseEntity<List<SalesAndMarketingRequestDto>> getMySalesRequests() {
        return ResponseEntity.ok(salesService.getMySalesRequests());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('ROLE_PRODUCTION')")
    public ResponseEntity<List<SalesAndMarketingRequestDto>> getPendingSalesRequests() {
        return ResponseEntity.ok(salesService.getPendingSalesRequests());
    }

    @GetMapping("/processing")
    @PreAuthorize("hasAnyAuthority('ROLE_PRODUCTION', 'ROLE_SALESANDMARKETING')")
    public ResponseEntity<List<SalesAndMarketingRequestDto>> getProcessingSalesRequests() {
        return ResponseEntity.ok(salesService.getProcessingSalesRequests());
    }
    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('ROLE_PRODUCTION', 'ROLE_SALESANDMARKETING')")
    public ResponseEntity<List<SalesAndMarketingRequestDto>> getAllSalesRequests() {
        return ResponseEntity.ok(salesService.getAllSalesRequests());
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SALESANDMARKETING', 'ROLE_PRODUCTION')")
    public ResponseEntity<SalesAndMarketingRequestDto> getSalesRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(salesService.getSalesRequestById(id));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SALESANDMARKETING')")
    public ResponseEntity<SalesAndMarketingRequestDto> updateSalesRequest(
            @PathVariable Long id,
            @Valid @RequestBody SalesAndMarketingRequestDto requestDto) {
        return ResponseEntity.ok(salesService.updateSalesRequest(id, requestDto));
    }

    @PostMapping("/{id}/convert")
    @PreAuthorize("hasAuthority('ROLE_PRODUCTION')")
    public ResponseEntity<SalesAndMarketingRequestDto> convertToProductionRequest(@PathVariable Long id) {
        return ResponseEntity.ok(salesService.convertToProductionRequest(id));
    }
}
