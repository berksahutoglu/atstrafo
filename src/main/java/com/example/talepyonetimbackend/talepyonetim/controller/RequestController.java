package com.example.talepyonetimbackend.talepyonetim.controller;

import com.example.talepyonetimbackend.talepyonetim.dto.DeliveryRequest;
import com.example.talepyonetimbackend.talepyonetim.dto.RequestDto;
import com.example.talepyonetimbackend.talepyonetim.dto.UpdateStatusRequest;
import com.example.talepyonetimbackend.talepyonetim.model.Role;
import com.example.talepyonetimbackend.talepyonetim.service.RequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_REQUESTER')")
    public ResponseEntity<RequestDto> createRequest(@Valid @RequestBody RequestDto requestDto) {
        return new ResponseEntity<>(requestService.createRequest(requestDto), HttpStatus.CREATED);
    }

    @GetMapping("/my-requests")
    @PreAuthorize("hasAuthority('ROLE_REQUESTER')")
    public ResponseEntity<List<RequestDto>> getMyRequests() {
        return ResponseEntity.ok(requestService.getMyRequests());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('ROLE_APPROVER')")
    public ResponseEntity<List<RequestDto>> getPendingRequests() {
        return ResponseEntity.ok(requestService.getPendingRequests());
    }
    
    // Onaylanan talepleri getiren endpoint
    @GetMapping("/approved")
    @PreAuthorize("hasAuthority('ROLE_APPROVER')")
    public ResponseEntity<List<RequestDto>> getApprovedRequests() {
        return ResponseEntity.ok(requestService.getApprovedRequests());
    }

    @GetMapping("/ordered")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEIVER', 'ROLE_APPROVER')")
    public ResponseEntity<List<RequestDto>> getOrderedRequests() {
        return ResponseEntity.ok(requestService.getOrderedRequests());
    }
    
    // Teslim alınan talepleri getiren yeni endpoint
    @GetMapping("/delivered")
    @PreAuthorize("hasAnyAuthority('ROLE_APPROVER', 'ROLE_RECEIVER')")
    public ResponseEntity<List<RequestDto>> getDeliveredRequests() {
        return ResponseEntity.ok(requestService.getDeliveredRequests());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_APPROVER')")
    public ResponseEntity<RequestDto> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest updateRequest) {
        return ResponseEntity.ok(requestService.updateStatus(id, updateRequest));
    }

    @PutMapping("/{id}/deliver")
    @PreAuthorize("hasAuthority('ROLE_RECEIVER')")
    public ResponseEntity<RequestDto> deliverRequest(
            @PathVariable Long id,
            @Valid @RequestBody DeliveryRequest deliveryRequest) {
        return ResponseEntity.ok(requestService.deliverRequest(id, deliveryRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_REQUESTER')")
    public ResponseEntity<Void> deleteRequest(@PathVariable Long id) {
        requestService.deleteRequest(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_REQUESTER')")
    public ResponseEntity<RequestDto> updateRequest(
            @PathVariable Long id,
            @Valid @RequestBody RequestDto requestDto) {
        return ResponseEntity.ok(requestService.updateRequest(id, requestDto));
    }
}
