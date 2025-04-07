package com.example.talepyonetimbackend.talepyonetim.dto;

import com.example.talepyonetimbackend.talepyonetim.model.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {
    private Long id;
    
    @NotBlank(message = "Proje adı boş olamaz")
    private String name;
    
    private ProjectStatus status;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String creatorName;
    private Long creatorId;
}
