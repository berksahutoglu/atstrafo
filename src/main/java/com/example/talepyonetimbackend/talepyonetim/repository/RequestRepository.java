package com.example.talepyonetimbackend.talepyonetim.repository;

import com.example.talepyonetimbackend.talepyonetim.model.Order;
import com.example.talepyonetimbackend.talepyonetim.model.Request;
import com.example.talepyonetimbackend.talepyonetim.model.RequestStatus;
import com.example.talepyonetimbackend.talepyonetim.model.SalesAndMarketingRequest;
import com.example.talepyonetimbackend.talepyonetim.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByRequesterOrderByCreatedAtDesc(User requester);
    List<Request> findByStatusOrderByUrgencyAscCreatedAtAsc(RequestStatus status);
    List<Request> findByStatusInOrderByOrderDateDesc(List<RequestStatus> statuses);
    
    // Onaylanan talepler için metot
    List<Request> findByStatusOrderByCreatedAtDesc(RequestStatus status);
    
    // Teslim alınan talepler için yeni metot
    List<Request> findByStatusOrderByDeliveryDateDesc(RequestStatus status);
    
    // Order ile ilgili sorgular
    List<Request> findByOrder(Order order);
    
    // Üretim departmanı tarafından oluşturulan talepler
    List<Request> findByCreatedByProductionIsTrue();
    
    // Bekleyen üretim departmanı taleplerini getirme
    List<Request> findByCreatedByProductionIsTrueAndStatusOrderByUrgencyAscCreatedAtAsc(RequestStatus status);
    
    // Belirli bir projeye ait talepleri getiren metot
    List<Request> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    
    // Satış talebine göre talepleri getir
    List<Request> findBySalesRequest_Id(Long salesRequestId);
    
    // Satış ve pazarlama talebinden dönüştürülen talepler
    List<Request> findBySalesRequestIsNotNull();
    
    // Belirli bir satış ve pazarlama talebine ait talebi bulma
    Request findBySalesRequest(SalesAndMarketingRequest salesRequest);
}
