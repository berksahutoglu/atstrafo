package com.example.talepyonetimbackend.talepyonetim.service;

import com.example.talepyonetimbackend.talepyonetim.dto.DeliveryRequest;
import com.example.talepyonetimbackend.talepyonetim.dto.RequestDto;
import com.example.talepyonetimbackend.talepyonetim.dto.UpdateStatusRequest;
import com.example.talepyonetimbackend.talepyonetim.exception.ResourceNotFoundException;
import com.example.talepyonetimbackend.talepyonetim.model.*;
import com.example.talepyonetimbackend.talepyonetim.repository.ProjectRepository;
import com.example.talepyonetimbackend.talepyonetim.repository.RequestRepository;
import com.example.talepyonetimbackend.talepyonetim.repository.SalesAndMarketingRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@org.springframework.context.annotation.Lazy
public class RequestService {

    private final RequestRepository requestRepository;
    private final UserService userService;
    private final ProjectRepository projectRepository;
    private final SalesAndMarketingRequestRepository salesRequestRepository;
    private SalesAndMarketingService salesAndMarketingService;

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    public void setSalesAndMarketingService(SalesAndMarketingService salesAndMarketingService) {
        this.salesAndMarketingService = salesAndMarketingService;
    }

    public RequestService(RequestRepository requestRepository, UserService userService, ProjectRepository projectRepository, 
                    SalesAndMarketingRequestRepository salesRequestRepository) {
        this.requestRepository = requestRepository;
        this.userService = userService;
        this.projectRepository = projectRepository;
        this.salesRequestRepository = salesRequestRepository;
    }

    @Transactional
    public RequestDto createRequest(RequestDto requestDto) {
        User currentUser = userService.getCurrentUser();

        Request request = new Request();
        request.setTitle(requestDto.getTitle());
        request.setDescription(requestDto.getDescription());
        request.setQuantity(requestDto.getQuantity());
        request.setUnit(requestDto.getUnit()); // Yeni unit alanını ayarla
        request.setUrgency(requestDto.getUrgency());
        request.setStatus(RequestStatus.PENDING);
        request.setRequester(currentUser);
        request.setRequesterName(currentUser.getFirstName() + " " + currentUser.getLastName());
        
        // Üretim departmanı tarafından oluşturuldu mu?
        if (requestDto.isCreatedByProduction()) {
            request.setCreatedByProduction(true);
        }
        
        // Satış talebi ile ilişkilendirme
        if (requestDto.getSalesRequestId() != null) {
            SalesAndMarketingRequest salesRequest = salesRequestRepository.findById(requestDto.getSalesRequestId())
                    .orElseThrow(() -> new ResourceNotFoundException("Satış talebi bulunamadı: " + requestDto.getSalesRequestId()));
            request.setSalesRequest(salesRequest);
        }
        
        // Proje ilişkisini kur
        if (requestDto.getProjectId() != null) {
            Project project = projectRepository.findById(requestDto.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Proje bulunamadı: " + requestDto.getProjectId()));
            request.setProject(project);
        }

        Request savedRequest = requestRepository.save(request);

        return mapToDto(savedRequest);
    }

    public List<RequestDto> getMyRequests() {
        User currentUser = userService.getCurrentUser();
        List<Request> requests = requestRepository.findByRequesterOrderByCreatedAtDesc(currentUser);

        return requests.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<RequestDto> getPendingRequests() {
        List<Request> requests = requestRepository.findByStatusOrderByUrgencyAscCreatedAtAsc(RequestStatus.PENDING);

        return requests.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    // Onaylanan talepleri getiren metot
    public List<RequestDto> getApprovedRequests() {
        List<Request> requests = requestRepository.findByStatusOrderByCreatedAtDesc(RequestStatus.APPROVED);
        
        return requests.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<RequestDto> getOrderedRequests() {
        List<Request> requests = requestRepository.findByStatusInOrderByOrderDateDesc(
                Arrays.asList(RequestStatus.ORDERED)
        );

        return requests.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    // Teslim alınan talepleri getiren yeni metot
    public List<RequestDto> getDeliveredRequests() {
        List<Request> requests = requestRepository.findByStatusOrderByDeliveryDateDesc(RequestStatus.DELIVERED);
        
        return requests.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public RequestDto updateStatus(Long requestId, UpdateStatusRequest updateRequest) {
        User currentUser = userService.getCurrentUser();

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Talep bulunamadı: " + requestId));

        request.setStatus(updateRequest.getStatus());
        request.setComment(updateRequest.getComment());
        request.setApprover(currentUser);
        request.setApproverName(currentUser.getFirstName() + " " + currentUser.getLastName());

        if (updateRequest.getStatus() == RequestStatus.ORDERED) {
            request.setOrderNumber(updateRequest.getOrderNumber());
            request.setOrderDate(LocalDateTime.now());
            
            // Tahmini teslim tarihini de kaydet
            if (updateRequest.getEstimatedDeliveryDate() != null) {
                request.setEstimatedDeliveryDate(updateRequest.getEstimatedDeliveryDate().atStartOfDay());
            }
            
            // Eğer bu talep bir satış talebine bağlı ise ve bu satış talebiyle ilişkili tüm talepler ORDERED durumundaysa
            // satış talebinin durumunu da ORDERED olarak güncelle
            if (request.getSalesRequest() != null) {
                Long salesRequestId = request.getSalesRequest().getId();
                // İlişkili tüm talepleri bul
                List<Request> relatedRequests = requestRepository.findBySalesRequest_Id(salesRequestId);
                
                // Tüm talepler ORDERED durumunda mı kontrol et
                boolean allOrdered = relatedRequests.stream()
                        .allMatch(req -> req.getId().equals(requestId) || req.getStatus() == RequestStatus.ORDERED);
                
                // Eğer tüm talepler ORDERED durumundaysa, satış talebini de ORDERED olarak güncelle
                if (allOrdered) {
                    salesAndMarketingService.updateSalesRequestStatus(salesRequestId, SalesRequestStatus.ORDERED);
                }
            }
        }

        Request savedRequest = requestRepository.save(request);

        return mapToDto(savedRequest);
    }

    @Transactional
    public RequestDto deliverRequest(Long requestId, DeliveryRequest deliveryRequest) {
        User currentUser = userService.getCurrentUser();

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Talep bulunamadı: " + requestId));

        request.setStatus(RequestStatus.DELIVERED);
        request.setDeliveryNotes(deliveryRequest.getDeliveryNotes());
        request.setDeliveryDate(LocalDateTime.now());
        request.setReceiver(currentUser);
        request.setReceiverName(currentUser.getFirstName() + " " + currentUser.getLastName());

        Request savedRequest = requestRepository.save(request);

        return mapToDto(savedRequest);
    }

    @Transactional
    public void deleteRequest(Long requestId) {
        User currentUser = userService.getCurrentUser();
        
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Talep bulunamadı: " + requestId));
        
        // Sadece talep sahibi ve durumu PENDING olanlar silinebilir
        if (!request.getRequester().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bu talebi silme yetkiniz bulunmamaktadır");
        }
        
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Sadece beklemede durumundaki talepler silinebilir");
        }
        
        requestRepository.delete(request);
    }
    
    @Transactional
    public RequestDto updateRequest(Long requestId, RequestDto requestDto) {
        User currentUser = userService.getCurrentUser();
        
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Talep bulunamadı: " + requestId));
        
        // Sadece talep sahibi ve durumu PENDING olanlar güncellenebilir
        if (!request.getRequester().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bu talebi güncelleme yetkiniz bulunmamaktadır");
        }
        
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Sadece beklemede durumundaki talepler güncellenebilir");
        }
        
        // Güncelleme işlemi
        request.setTitle(requestDto.getTitle());
        request.setDescription(requestDto.getDescription());
        request.setQuantity(requestDto.getQuantity());
        request.setUnit(requestDto.getUnit());
        request.setUrgency(requestDto.getUrgency());
        
        // Proje ilişkisini güncelle
        if (requestDto.getProjectId() != null) {
            Project project = projectRepository.findById(requestDto.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Proje bulunamadı: " + requestDto.getProjectId()));
            request.setProject(project);
        } else {
            request.setProject(null); // Proje seçimi kaldırıldıysa
        }
        
        Request savedRequest = requestRepository.save(request);
        
        return mapToDto(savedRequest);
    }

    @Transactional
    public Request createRequestForOrder(RequestDto requestDto, Order order) {
        User currentUser = userService.getCurrentUser();

        Request request = Request.builder()
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .quantity(requestDto.getQuantity())
                .unit(requestDto.getUnit())
                .urgency(requestDto.getUrgency())
                .status(RequestStatus.PENDING)
                .requester(currentUser)
                .requesterName(currentUser.getFullName())
                .order(order)
                .build();

        return requestRepository.save(request);
    }
    
    public List<RequestDto> getRequestsByProductionDepartment() {
        List<Request> requests = requestRepository.findByCreatedByProductionIsTrue();
        return requests.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<RequestDto> getPendingProductionRequests() {
        List<Request> requests = requestRepository.findByCreatedByProductionIsTrueAndStatusOrderByUrgencyAscCreatedAtAsc(RequestStatus.PENDING);
        return requests.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<RequestDto> getRequestsByProjectId(Long projectId) {
        List<Request> requests = requestRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
        return requests.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public RequestDto convertToDto(Request request) {
        return mapToDto(request);
    }
    
    private RequestDto mapToDto(Request request) {
        RequestDto dto = new RequestDto();
        dto.setId(request.getId());
        dto.setTitle(request.getTitle());
        dto.setDescription(request.getDescription());
        dto.setQuantity(request.getQuantity());
        dto.setUnit(request.getUnit()); // Unit değerini DTO'ya ekle
        dto.setUrgency(request.getUrgency());
        dto.setStatus(request.getStatus());
        dto.setRequesterName(request.getRequesterName());
        dto.setApproverName(request.getApproverName());
        dto.setReceiverName(request.getReceiverName());
        
        // Yeni alanlar
        if (request.getOrder() != null) {
            dto.setOrderId(request.getOrder().getId());
        }
        
        dto.setCreatedByProduction(request.isCreatedByProduction());
        
        if (request.getSalesRequest() != null) {
            dto.setSalesRequestId(request.getSalesRequest().getId());
        }
        
        // Proje bilgisini ekle
        if (request.getProject() != null) {
            dto.setProjectId(request.getProject().getId());
            dto.setProjectName(request.getProject().getName());
        }
        dto.setComment(request.getComment());
        dto.setOrderNumber(request.getOrderNumber());
        dto.setOrderDate(request.getOrderDate());
        dto.setEstimatedDeliveryDate(request.getEstimatedDeliveryDate());
        dto.setDeliveryNotes(request.getDeliveryNotes());
        dto.setDeliveryDate(request.getDeliveryDate());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setUpdatedAt(request.getUpdatedAt());

        return dto;
    }
}
