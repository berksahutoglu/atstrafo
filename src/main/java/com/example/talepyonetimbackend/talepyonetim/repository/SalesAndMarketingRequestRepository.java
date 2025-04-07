package com.example.talepyonetimbackend.talepyonetim.repository;

import com.example.talepyonetimbackend.talepyonetim.model.Project;
import com.example.talepyonetimbackend.talepyonetim.model.SalesAndMarketingRequest;
import com.example.talepyonetimbackend.talepyonetim.model.SalesRequestStatus;
import com.example.talepyonetimbackend.talepyonetim.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesAndMarketingRequestRepository extends JpaRepository<SalesAndMarketingRequest, Long> {
    List<SalesAndMarketingRequest> findByCreatedBy(User user);
    
    List<SalesAndMarketingRequest> findByStatus(SalesRequestStatus status);
    
    List<SalesAndMarketingRequest> findByStatusIn(List<SalesRequestStatus> statuses);
    
    List<SalesAndMarketingRequest> findByProject(Project project);
    
    List<SalesAndMarketingRequest> findByProjectAndStatus(Project project, SalesRequestStatus status);
    
    List<SalesAndMarketingRequest> findByProjectAndStatusIn(Project project, List<SalesRequestStatus> statuses);
}
