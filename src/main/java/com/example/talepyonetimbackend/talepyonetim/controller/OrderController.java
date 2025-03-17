package com.example.talepyonetimbackend.talepyonetim.controller;

import com.example.talepyonetimbackend.talepyonetim.dto.OrderDto;
import com.example.talepyonetimbackend.talepyonetim.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_REQUESTER')")
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody OrderDto orderDto) {
        return new ResponseEntity<>(orderService.createOrder(orderDto), HttpStatus.CREATED);
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasAuthority('ROLE_REQUESTER')")
    public ResponseEntity<List<OrderDto>> getMyOrders() {
        return ResponseEntity.ok(orderService.getMyOrders());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_REQUESTER', 'ROLE_APPROVER', 'ROLE_RECEIVER')")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('ROLE_APPROVER')")
    public ResponseEntity<List<OrderDto>> getPendingOrders() {
        return ResponseEntity.ok(orderService.getPendingOrders());
    }
}
