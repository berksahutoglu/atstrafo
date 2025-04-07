package com.example.talepyonetimbackend.talepyonetim.controller;

import com.example.talepyonetimbackend.talepyonetim.dto.RequestDto;
import com.example.talepyonetimbackend.talepyonetim.dto.SalesAndMarketingRequestDto;
import com.example.talepyonetimbackend.talepyonetim.model.SalesRequestStatus;
import com.example.talepyonetimbackend.talepyonetim.service.SalesAndMarketingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyAuthority('ROLE_SALESANDMARKETING', 'ROLE_PRODUCTION')")
    public ResponseEntity<List<SalesAndMarketingRequestDto>> getRequestsByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(salesService.getRequestsByProjectId(projectId));
    }
    
    @GetMapping("/project/{projectId}/status/{status}")
    @PreAuthorize("hasAnyAuthority('ROLE_SALESANDMARKETING', 'ROLE_PRODUCTION')")
    public ResponseEntity<List<SalesAndMarketingRequestDto>> getRequestsByProjectAndStatus(
            @PathVariable Long projectId, 
            @PathVariable SalesRequestStatus status) {
        return ResponseEntity.ok(salesService.getRequestsByProjectIdAndStatus(projectId, status));
    }
    
    @GetMapping("/status-list")
    @PreAuthorize("hasAnyAuthority('ROLE_SALESANDMARKETING', 'ROLE_PRODUCTION')")
    public ResponseEntity<List<SalesAndMarketingRequestDto>> getRequestsByStatusList(
            @RequestParam("statuses") List<SalesRequestStatus> statuses) {
        return ResponseEntity.ok(salesService.getRequestsByStatusList(statuses));
    }
    
    @GetMapping("/project-stats")
    @PreAuthorize("hasAnyAuthority('ROLE_SALESANDMARKETING', 'ROLE_PRODUCTION')")
    public ResponseEntity<Map<String, Object>> getProjectStats() {
        return ResponseEntity.ok(salesService.getProjectStats());
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

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_PRODUCTION', 'ROLE_APPROVER')")
    public ResponseEntity<SalesAndMarketingRequestDto> updateSalesRequestStatus(
            @PathVariable Long id,
            @RequestBody Map<String, SalesRequestStatus> statusUpdate) {
        SalesRequestStatus newStatus = statusUpdate.get("status");
        return ResponseEntity.ok(salesService.updateSalesRequestStatus(id, newStatus));
    }
}
