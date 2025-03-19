package com.example.talepyonetimbackend.talepyonetim.dto;

import lombok.Data;

@Data
public class AttachmentDto {
    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String uploadedAt;
    private String uploadedBy;
    private String downloadUrl;
}
