package com.example.talepyonetimbackend.talepyonetim.controller;

import com.example.talepyonetimbackend.talepyonetim.dto.AttachmentDto;
import com.example.talepyonetimbackend.talepyonetim.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {
    private static final Logger logger = Logger.getLogger(AttachmentController.class.getName());

    private final AttachmentService attachmentService;

    @Autowired
    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    // Tekli dosya yükleme
    @PostMapping("/upload")
    @PreAuthorize("hasAnyAuthority('ROLE_REQUESTER', 'ROLE_PRODUCTION', 'ROLE_SALESANDMARKETING')")
    public ResponseEntity<AttachmentDto> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "requestId", required = false) Long requestId,
            @RequestParam(value = "salesRequestId", required = false) Long salesRequestId) {
        
        try {
            if (requestId == null && salesRequestId == null) {
                return ResponseEntity.badRequest().build();
            }
            
            AttachmentDto attachment = attachmentService.uploadFile(file, requestId, salesRequestId);
            return new ResponseEntity<>(attachment, HttpStatus.CREATED);
        } catch (IOException e) {
            logger.severe("Error uploading file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Çoklu dosya yükleme - YENİ
    @PostMapping("/upload-multiple")
    @PreAuthorize("hasAnyAuthority('ROLE_REQUESTER', 'ROLE_PRODUCTION', 'ROLE_SALESANDMARKETING')")
    public ResponseEntity<List<AttachmentDto>> uploadMultipleFiles(
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "requestId", required = false) Long requestId,
            @RequestParam(value = "salesRequestId", required = false) Long salesRequestId) {
        
        try {
            logger.info("Received upload request - files: " + (files != null ? files.length : "null") + 
                       ", requestId: " + requestId + ", salesRequestId: " + salesRequestId);
            
            if (requestId == null && salesRequestId == null) {
                logger.warning("Bad request - both requestId and salesRequestId are null");
                return ResponseEntity.badRequest().build();
            }
            
            if (files == null || files.length == 0) {
                logger.warning("Bad request - no files provided");
                return ResponseEntity.badRequest().body(null);
            }
            
            List<AttachmentDto> attachments = attachmentService.uploadMultipleFiles(files, requestId, salesRequestId);
            return new ResponseEntity<>(attachments, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.severe("Error uploading files: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/request/{requestId}")
    @PreAuthorize("hasAnyAuthority('ROLE_REQUESTER', 'ROLE_PRODUCTION', 'ROLE_APPROVER', 'ROLE_RECEIVER')")
    public ResponseEntity<List<AttachmentDto>> getAttachmentsByRequestId(@PathVariable Long requestId) {
        List<AttachmentDto> attachments = attachmentService.getAttachmentsByRequestId(requestId);
        return ResponseEntity.ok(attachments);
    }
    
    @GetMapping("/sales/{salesRequestId}")
    @PreAuthorize("hasAnyAuthority('ROLE_SALESANDMARKETING', 'ROLE_PRODUCTION')")
    public ResponseEntity<List<AttachmentDto>> getAttachmentsBySalesRequestId(@PathVariable Long salesRequestId) {
        List<AttachmentDto> attachments = attachmentService.getAttachmentsBySalesRequestId(salesRequestId);
        return ResponseEntity.ok(attachments);
    }
    
    @GetMapping("/{attachmentId}/download")
    @PreAuthorize("hasAnyAuthority('ROLE_REQUESTER', 'ROLE_PRODUCTION', 'ROLE_APPROVER', 'ROLE_RECEIVER', 'ROLE_SALESANDMARKETING')")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long attachmentId) {
        try {
            Resource resource = attachmentService.downloadFile(attachmentId);
            String contentType = "application/octet-stream";
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            logger.severe("Error downloading file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{attachmentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_REQUESTER', 'ROLE_PRODUCTION', 'ROLE_SALESANDMARKETING')")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long attachmentId) {
        try {
            attachmentService.deleteAttachment(attachmentId);
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            logger.severe("Error deleting file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}