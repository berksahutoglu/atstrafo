package com.example.talepyonetimbackend.talepyonetim.repository;

import com.example.talepyonetimbackend.talepyonetim.model.Project;
import com.example.talepyonetimbackend.talepyonetim.model.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    // İsimden proje arama - uniqueness için kontrol amaçlı
    Optional<Project> findByName(String name);
    
    // İsmin var olup olmadığını kontrol etme
    boolean existsByName(String name);
    
    // Oluşturan kullanıcının ID'sine göre projeleri getirme
    List<Project> findByCreatorId(Long creatorId);
    
    // İsme göre projeleri arama (contains)
    List<Project> findByNameContainingIgnoreCase(String name);
    
    // Duruma göre projeleri arama
    List<Project> findByStatusOrderByCreatedAtDesc(ProjectStatus status);
}
