package com.example.talepyonetimbackend.talepyonetim.repository;

import com.example.talepyonetimbackend.talepyonetim.model.Order;
import com.example.talepyonetimbackend.talepyonetim.model.OrderStatus;
import com.example.talepyonetimbackend.talepyonetim.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCreatedBy(User user);
    
    List<Order> findByStatus(OrderStatus status);
    
    Optional<Order> findByOrderNumber(String orderNumber);
}
