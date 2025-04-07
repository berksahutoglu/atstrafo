package com.example.talepyonetimbackend.talepyonetim.service;

import com.example.talepyonetimbackend.talepyonetim.dto.ProjectDto;
import com.example.talepyonetimbackend.talepyonetim.exception.ResourceAlreadyExistsException;
import com.example.talepyonetimbackend.talepyonetim.exception.ResourceNotFoundException;
import com.example.talepyonetimbackend.talepyonetim.model.Project;
import com.example.talepyonetimbackend.talepyonetim.model.ProjectStatus;
import com.example.talepyonetimbackend.talepyonetim.model.User;
import com.example.talepyonetimbackend.talepyonetim.repository.ProjectRepository;
import com.example.talepyonetimbackend.talepyonetim.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    /**
     * Tüm projeleri getirir.
     */
    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * ID'ye göre proje getirir.
     */
    public ProjectDto getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proje bulunamadı: " + id));
        return convertToDto(project);
    }

    /**
     * Yeni bir proje oluşturur.
     * Eğer aynı isimde bir proje zaten varsa hata fırlatır.
     */
    public ProjectDto createProject(ProjectDto projectDto) {
        // İsim kontrolü
        if (projectRepository.existsByName(projectDto.getName())) {
            throw new ResourceAlreadyExistsException("Bu isimde bir proje zaten mevcut: " + projectDto.getName());
        }

        // Mevcut kullanıcıyı al
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        // Proje nesnesini oluştur
        Project project = Project.builder()
                .name(projectDto.getName())
                .creator(currentUser)
                .status(ProjectStatus.PENDING)
                .build();

        // Projeyi kaydet
        project = projectRepository.save(project);
        
        return convertToDto(project);
    }

    /**
     * Belirli bir durumdaki projeleri getirir.
     */
    public List<ProjectDto> getProjectsByStatus(ProjectStatus status) {
        return projectRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Var olan bir projeyi günceller.
     */
    public ProjectDto updateProject(Long id, ProjectDto projectDto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proje bulunamadı: " + id));

        // İsim değiştiyse ve yeni isim zaten kullanımdaysa
        if (!project.getName().equals(projectDto.getName()) && 
                projectRepository.existsByName(projectDto.getName())) {
            throw new ResourceAlreadyExistsException("Bu isimde bir proje zaten mevcut: " + projectDto.getName());
        }

        // Projeyi güncelle
        project.setName(projectDto.getName());
        
        // Status değiştiyse güncelle
        if (projectDto.getStatus() != null) {
            project.setStatus(projectDto.getStatus());
        }
        
        project = projectRepository.save(project);
        
        return convertToDto(project);
    }
    
    /**
     * Projenin durumunu günceller.
     */
    public ProjectDto updateProjectStatus(Long id, ProjectStatus newStatus) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proje bulunamadı: " + id));
        
        // Durumu güncelle
        project.setStatus(newStatus);
        
        // Projeyi kaydet
        project = projectRepository.save(project);
        
        return convertToDto(project);
    }

    /**
     * Projeyi siler.
     */
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Proje bulunamadı: " + id);
        }
        projectRepository.deleteById(id);
    }

    /**
     * Proje entity'sini DTO'ya dönüştürür.
     */
    private ProjectDto convertToDto(Project project) {
        return ProjectDto.builder()
                .id(project.getId())
                .name(project.getName())
                .status(project.getStatus())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .creatorId(project.getCreator() != null ? project.getCreator().getId() : null)
                .creatorName(project.getCreator() != null ? project.getCreator().getFullName() : null)
                .build();
    }
}
