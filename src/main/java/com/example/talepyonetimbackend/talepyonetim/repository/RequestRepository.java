package com.example.talepyonetimbackend.talepyonetim.repository;

import com.example.talepyonetimbackend.talepyonetim.model.Request;
import com.example.talepyonetimbackend.talepyonetim.model.RequestStatus;
import com.example.talepyonetimbackend.talepyonetim.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
