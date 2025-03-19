package com.example.talepyonetimbackend.talepyonetim.repository;

import com.example.talepyonetimbackend.talepyonetim.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByRequestId(Long requestId);
    List<Attachment> findBySalesRequestId(Long salesRequestId);
}
