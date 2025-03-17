package com.example.talepyonetimbackend.talepyonetim.service;

import com.example.talepyonetimbackend.talepyonetim.dto.OrderDto;
import com.example.talepyonetimbackend.talepyonetim.dto.RequestDto;
import com.example.talepyonetimbackend.talepyonetim.exception.ResourceNotFoundException;
import com.example.talepyonetimbackend.talepyonetim.model.Order;
import com.example.talepyonetimbackend.talepyonetim.model.OrderStatus;
import com.example.talepyonetimbackend.talepyonetim.model.Request;
import com.example.talepyonetimbackend.talepyonetim.model.User;
import com.example.talepyonetimbackend.talepyonetim.repository.OrderRepository;
import com.example.talepyonetimbackend.talepyonetim.repository.RequestRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final RequestRepository requestRepository;
    private final RequestService requestService;
    private final UserService userService;

    @Autowired
    public OrderService(OrderRepository orderRepository, 
                       RequestRepository requestRepository,
                       RequestService requestService,
                       UserService userService) {
        this.orderRepository = orderRepository;
        this.requestRepository = requestRepository;
        this.requestService = requestService;
        this.userService = userService;
    }

    @Transactional
    public OrderDto createOrder(OrderDto orderDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getUserByUsername(authentication.getName());
        
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .status(OrderStatus.DRAFT)
                .createdBy(currentUser)
                .notes(orderDto.getNotes())
                .requests(new ArrayList<>())
                .build();
        
        Order savedOrder = orderRepository.save(order);
        
        List<Request> requests = new ArrayList<>();
        for (RequestDto requestDto : orderDto.getRequests()) {
            Request request = requestService.createRequestForOrder(requestDto, savedOrder);
            requests.add(request);
        }
        
        savedOrder.setRequests(requests);
        savedOrder.setStatus(OrderStatus.PENDING);
        
        Order updatedOrder = orderRepository.save(savedOrder);
        return convertToDto(updatedOrder);
    }
    
    public List<OrderDto> getMyOrders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getUserByUsername(authentication.getName());
        List<Order> orders = orderRepository.findByCreatedBy(currentUser);
        return orders.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sipariş bulunamadı: " + id));
        return convertToDto(order);
    }
    
    public List<OrderDto> getPendingOrders() {
        List<Order> orders = orderRepository.findByStatus(OrderStatus.PENDING);
        return orders.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private OrderDto convertToDto(Order order) {
        OrderDto orderDto = new OrderDto();
        orderDto.setId(order.getId());
        orderDto.setOrderNumber(order.getOrderNumber());
        orderDto.setStatus(order.getStatus());
        orderDto.setCreatedByName(order.getCreatedBy().getFullName());
        orderDto.setCreatedAt(order.getCreatedAt());
        orderDto.setUpdatedAt(order.getUpdatedAt());
        orderDto.setNotes(order.getNotes());
        
        List<RequestDto> requestDtos = order.getRequests().stream()
                .map(requestService::convertToDto)
                .collect(Collectors.toList());
        orderDto.setRequests(requestDtos);
        
        return orderDto;
    }
}
