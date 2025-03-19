package com.example.talepyonetimbackend.talepyonetim.service;

import com.example.talepyonetimbackend.talepyonetim.dto.RequestDto;
import com.example.talepyonetimbackend.talepyonetim.dto.SalesAndMarketingRequestDto;
import com.example.talepyonetimbackend.talepyonetim.exception.ResourceNotFoundException;
import com.example.talepyonetimbackend.talepyonetim.model.*;
import com.example.talepyonetimbackend.talepyonetim.repository.RequestRepository;
import com.example.talepyonetimbackend.talepyonetim.repository.SalesAndMarketingRequestRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SalesAndMarketingService {

    private final SalesAndMarketingRequestRepository salesRepository;
    private final RequestRepository requestRepository;
    private final UserService userService;
    private final RequestService requestService;

    @Autowired
    public SalesAndMarketingService(SalesAndMarketingRequestRepository salesRepository,
                                   RequestRepository requestRepository,
                                   UserService userService,
                                   RequestService requestService) {
        this.salesRepository = salesRepository;
        this.requestRepository = requestRepository;
        this.userService = userService;
        this.requestService = requestService;
    }

    @Transactional
    public SalesAndMarketingRequestDto createSalesRequest(SalesAndMarketingRequestDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getUserByUsername(authentication.getName());
        
        // İstemci tarafından gönderilen isDomestic alanını marketType'a dönüştür
        MarketType marketType = MarketType.DOMESTIC; // Varsayılan olarak yurtiçi
        
        // Eğer isDomestic alanı gönderilmişse ve false ise, INTERNATIONAL yapalım
        if (dto.getIsDomestic() != null && !dto.getIsDomestic()) {
            marketType = MarketType.INTERNATIONAL;
        } else if (dto.getMarketType() != null) {
            // Veya zaten marketType gönderilmişse onu kullanalım
            marketType = dto.getMarketType();
        }
        
        SalesAndMarketingRequest request = SalesAndMarketingRequest.builder()
                .marketType(marketType)
                .country(dto.getCountry())
                .customerName(dto.getCustomerName())
                .power(dto.getPower())
                .quantity(dto.getQuantity())
                .outputPower(dto.getOutputPower())
                .boilerType(dto.getBoilerType())
                .windingType(dto.getWindingType())
                .isAPlus(marketType == MarketType.DOMESTIC && dto.isAPlus())
                .requestedDeliveryDate(dto.getRequestedDeliveryDate())
                .createdBy(currentUser)
                .status(SalesRequestStatus.PENDING)
                .notes(dto.getNotes())
                .build();
        
        SalesAndMarketingRequest savedRequest = salesRepository.save(request);
        return convertToDto(savedRequest);
    }
    
    public List<SalesAndMarketingRequestDto> getMySalesRequests() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getUserByUsername(authentication.getName());
        List<SalesAndMarketingRequest> requests = salesRepository.findByCreatedBy(currentUser);
        return requests.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    public List<SalesAndMarketingRequestDto> getPendingSalesRequests() {
        List<SalesAndMarketingRequest> requests = salesRepository.findByStatus(SalesRequestStatus.PENDING);
        return requests.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<SalesAndMarketingRequestDto> getAllSalesRequests() {
        List<SalesAndMarketingRequest> requests = salesRepository.findAll();
        return requests.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    public SalesAndMarketingRequestDto getSalesRequestById(Long id) {
        SalesAndMarketingRequest request = salesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Satış talebi bulunamadı: " + id));
        return convertToDto(request);
    }
    
    @Transactional
    public SalesAndMarketingRequestDto convertToProductionRequest(Long salesRequestId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getUserByUsername(authentication.getName());
        
        SalesAndMarketingRequest salesRequest = salesRepository.findById(salesRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Satış talebi bulunamadı: " + salesRequestId));
        
        if (salesRequest.getStatus() != SalesRequestStatus.PENDING) {
            throw new IllegalStateException("Bu talep zaten işlenmiş.");
        }
        
        // Sadece talebinin durumunu güncelle, Production tarafında talebi ayrıca oluşturacaklar
        salesRequest.setStatus(SalesRequestStatus.PROCESSING);
        SalesAndMarketingRequest updatedRequest = salesRepository.save(salesRequest);
        
        return convertToDto(updatedRequest);
    }
    
    public List<SalesAndMarketingRequestDto> getProcessingSalesRequests() {
        List<SalesRequestStatus> statuses = Arrays.asList(
                SalesRequestStatus.PROCESSING,
                SalesRequestStatus.CONVERTED
        );
        List<SalesAndMarketingRequest> requests = salesRepository.findByStatusIn(statuses);
        return requests.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Transactional
    public SalesAndMarketingRequestDto updateSalesRequest(Long id, SalesAndMarketingRequestDto dto) {
        // Güncel kullanıcıyı al
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getUserByUsername(authentication.getName());
        
        // Var olan talebi bul
        SalesAndMarketingRequest existingRequest = salesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Satış talebi bulunamadı: " + id));
        
        // Sadece talebin sahibi düzenleyebilir
        if (!existingRequest.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Bu talebi sadece oluşturan kişi düzenleyebilir.");
        }
        
        // Talep zaten işlemde veya dönüştürülmüş durumda ise düzenlenemez
        if (existingRequest.getStatus() != SalesRequestStatus.PENDING) {
            throw new IllegalStateException("Bu talep zaten işleme alınmış, düzenlenemez.");
        }
        
        // İstemci tarafından gönderilen isDomestic alanını marketType'a dönüştür
        MarketType marketType = MarketType.DOMESTIC; // Varsayılan olarak yurtiçi
        
        // Eğer isDomestic alanı gönderilmişse ve false ise, INTERNATIONAL yapalım
        if (dto.getIsDomestic() != null && !dto.getIsDomestic()) {
            marketType = MarketType.INTERNATIONAL;
        } else if (dto.getMarketType() != null) {
            // Veya zaten marketType gönderilmişse onu kullanalım
            marketType = dto.getMarketType();
        }
        
        // Güncelleme işlemleri
        existingRequest.setMarketType(marketType);
        existingRequest.setCountry(dto.getCountry());
        existingRequest.setCustomerName(dto.getCustomerName());
        existingRequest.setPower(dto.getPower());
        existingRequest.setQuantity(dto.getQuantity());
        existingRequest.setOutputPower(dto.getOutputPower());
        existingRequest.setBoilerType(dto.getBoilerType());
        existingRequest.setWindingType(dto.getWindingType());
        existingRequest.setAPlus(marketType == MarketType.DOMESTIC && dto.isAPlus());
        existingRequest.setRequestedDeliveryDate(dto.getRequestedDeliveryDate());
        existingRequest.setNotes(dto.getNotes());
        
        SalesAndMarketingRequest updatedRequest = salesRepository.save(existingRequest);
        return convertToDto(updatedRequest);
    }
    
    private String generateTitle(SalesAndMarketingRequest salesRequest) {
        String country = salesRequest.getCountry();
        String power = salesRequest.getPower();
        String outputPower = salesRequest.getOutputPower();
        
        StringBuilder title = new StringBuilder("Sipariş - ");
        title.append(salesRequest.getMarketType() == MarketType.DOMESTIC ? "Yurtiçi" : "Yurtdışı");
        title.append(" - ").append(country);
        title.append(" - ").append(power);
        title.append(" - ").append(outputPower);
        
        if (salesRequest.getMarketType() == MarketType.DOMESTIC && salesRequest.isAPlus()) {
            title.append(" - A+");
        }
        
        return title.toString();
    }
    
    private String generateDescription(SalesAndMarketingRequest salesRequest) {
        StringBuilder description = new StringBuilder();
        description.append("Satış ve Pazarlama Talebi #").append(salesRequest.getId()).append("\n\n");
        description.append("Pazar: ").append(salesRequest.getMarketType() == MarketType.DOMESTIC ? "Yurtiçi" : "Yurtdışı").append("\n");
        description.append("Ülke: ").append(salesRequest.getCountry()).append("\n");
        description.append("Müşteri: ").append(salesRequest.getCustomerName()).append("\n");
        description.append("Güç: ").append(salesRequest.getPower()).append("\n");
        description.append("Adet: ").append(salesRequest.getQuantity()).append("\n");
        description.append("Çıkış Gücü: ").append(salesRequest.getOutputPower()).append("\n");
        description.append("Kazan Tipi: ").append(salesRequest.getBoilerType()).append("\n");
        description.append("Sargı Tipi: ").append(salesRequest.getWindingType()).append("\n");
        
        if (salesRequest.getMarketType() == MarketType.DOMESTIC) {
            description.append("Sınıf: ").append(salesRequest.isAPlus() ? "A+" : "Normal").append("\n");
        }
        
        description.append("Talep Edilen Teslim Tarihi: ").append(salesRequest.getRequestedDeliveryDate()).append("\n\n");
        
        if (salesRequest.getNotes() != null && !salesRequest.getNotes().trim().isEmpty()) {
            description.append("Notlar: ").append(salesRequest.getNotes());
        }
        
        return description.toString();
    }
    
    public SalesAndMarketingRequestDto convertToDto(SalesAndMarketingRequest request) {
        SalesAndMarketingRequestDto dto = new SalesAndMarketingRequestDto();
        dto.setId(request.getId());
        dto.setMarketType(request.getMarketType());
        
        // Geriye uyumluluk için isDomestic alanını hesapla
        dto.setIsDomestic(request.getMarketType() == MarketType.DOMESTIC);
        
        dto.setCountry(request.getCountry());
        dto.setCustomerName(request.getCustomerName());
        dto.setPower(request.getPower());
        dto.setQuantity(request.getQuantity());
        dto.setOutputPower(request.getOutputPower());
        dto.setBoilerType(request.getBoilerType());
        dto.setWindingType(request.getWindingType());
        dto.setAPlus(request.isAPlus());
        dto.setRequestedDeliveryDate(request.getRequestedDeliveryDate());
        dto.setCreatedByName(request.getCreatedBy().getFullName());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setUpdatedAt(request.getUpdatedAt());
        dto.setStatus(request.getStatus());
        dto.setNotes(request.getNotes());
        
        if (request.getProductionRequest() != null) {
            dto.setProductionRequest(requestService.convertToDto(request.getProductionRequest()));
        }
        
        return dto;
    }
}
