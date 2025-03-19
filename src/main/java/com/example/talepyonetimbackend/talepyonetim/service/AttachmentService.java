package com.example.talepyonetimbackend.talepyonetim.service;

import com.example.talepyonetimbackend.talepyonetim.dto.AttachmentDto;
import com.example.talepyonetimbackend.talepyonetim.exception.ResourceNotFoundException;
import com.example.talepyonetimbackend.talepyonetim.model.Attachment;
import com.example.talepyonetimbackend.talepyonetim.model.Request;
import com.example.talepyonetimbackend.talepyonetim.model.SalesAndMarketingRequest;
import com.example.talepyonetimbackend.talepyonetim.repository.AttachmentRepository;
import com.example.talepyonetimbackend.talepyonetim.repository.RequestRepository;
import com.example.talepyonetimbackend.talepyonetim.repository.SalesAndMarketingRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AttachmentService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private final AttachmentRepository attachmentRepository;
    private final RequestRepository requestRepository;
    private final SalesAndMarketingRequestRepository salesRepository;
    private final UserService userService;

    @Autowired
    public AttachmentService(AttachmentRepository attachmentRepository, 
                           RequestRepository requestRepository,
                           SalesAndMarketingRequestRepository salesRepository,
                           UserService userService) {
        this.attachmentRepository = attachmentRepository;
        this.requestRepository = requestRepository;
        this.salesRepository = salesRepository;
        this.userService = userService;
    }

    public AttachmentDto uploadFile(MultipartFile file, Long requestId, Long salesRequestId) throws IOException {
        // Dosya adını temizle
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        
        // Tekil bir dosya adı oluştur
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + fileExtension;
        
        // Yükleme dizinini oluştur
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Dosyayı fiziksel olarak kaydet
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Veritabanı kaydını oluştur
        Attachment attachment = new Attachment();
        attachment.setFileName(originalFileName);
        attachment.setFileType(file.getContentType());
        attachment.setFilePath(fileName);
        attachment.setFileSize(file.getSize());
        attachment.setUploadedBy(userService.getCurrentUser().getFullName());
        
        // İlişkiyi oluştur
        if (requestId != null) {
            Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Talep bulunamadı: " + requestId));
            attachment.setRequest(request);
        } else if (salesRequestId != null) {
            SalesAndMarketingRequest salesRequest = salesRepository.findById(salesRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Satış talebi bulunamadı: " + salesRequestId));
            attachment.setSalesRequest(salesRequest);
        }
        
        Attachment savedAttachment = attachmentRepository.save(attachment);
        
        return convertToDto(savedAttachment);
    }
    
    // Çoklu dosya yükleme metodu - YENİ
    public List<AttachmentDto> uploadMultipleFiles(MultipartFile[] files, Long requestId, Long salesRequestId) throws IOException {
        List<AttachmentDto> results = new ArrayList<>();
        
        // Her dosyayı tekil olarak işle
        for (MultipartFile file : files) {
            try {
                AttachmentDto dto = uploadFile(file, requestId, salesRequestId);
                results.add(dto);
            } catch (IOException e) {
                // Hata oluşursa loglama yap ama diğer dosyaları işlemeye devam et
                System.err.println("Error uploading file: " + file.getOriginalFilename() + " - " + e.getMessage());
            }
        }
        
        return results;
    }
    
    public Resource downloadFile(Long attachmentId) throws MalformedURLException {
        Attachment attachment = attachmentRepository.findById(attachmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Dosya bulunamadı: " + attachmentId));
        
        Path filePath = Paths.get(uploadDir).resolve(attachment.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());
        
        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new RuntimeException("Dosya okunamıyor: " + attachment.getFileName());
        }
    }
    
    public List<AttachmentDto> getAttachmentsByRequestId(Long requestId) {
        List<Attachment> attachments = attachmentRepository.findByRequestId(requestId);
        return attachments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<AttachmentDto> getAttachmentsBySalesRequestId(Long salesRequestId) {
        List<Attachment> attachments = attachmentRepository.findBySalesRequestId(salesRequestId);
        return attachments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public void deleteAttachment(Long attachmentId) throws IOException {
        Attachment attachment = attachmentRepository.findById(attachmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Dosya bulunamadı: " + attachmentId));
        
        // Fiziksel dosyayı sil
        Path filePath = Paths.get(uploadDir).resolve(attachment.getFilePath());
        Files.deleteIfExists(filePath);
        
        // Veritabanı kaydını sil
        attachmentRepository.delete(attachment);
    }
    
    private AttachmentDto convertToDto(Attachment attachment) {
        AttachmentDto dto = new AttachmentDto();
        dto.setId(attachment.getId());
        dto.setFileName(attachment.getFileName());
        dto.setFileType(attachment.getFileType());
        dto.setFileSize(attachment.getFileSize());
        dto.setUploadedAt(attachment.getUploadedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        dto.setUploadedBy(attachment.getUploadedBy());
        dto.setDownloadUrl("/api/attachments/" + attachment.getId() + "/download");
        return dto;
    }
}