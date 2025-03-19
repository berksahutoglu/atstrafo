package com.example.talepyonetimbackend.talepyonetim.service;

import com.example.talepyonetimbackend.talepyonetim.dto.DeliveryRequest;
import com.example.talepyonetimbackend.talepyonetim.dto.RequestDto;
import com.example.talepyonetimbackend.talepyonetim.dto.UpdateStatusRequest;
import com.example.talepyonetimbackend.talepyonetim.exception.ResourceNotFoundException;
import com.example.talepyonetimbackend.talepyonetim.model.*;
import com.example.talepyonetimbackend.talepyonetim.repository.RequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequestService {

    private final RequestRepository requestRepository;
    private final UserService userService;

    public RequestService(RequestRepository requestRepository, UserService userService) {
        this.requestRepository = requestRepository;
        this.userService = userService;
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
            // Normalde burada salesRequestRepository'den ilgili talebi çekecektik
            // Ancak şu anda öncelik hatanın çözülmesi olduğu için ilşkiyi sonra kurabiliriz
            // TODO: Satış talebi ile ilişkilendirme yap
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
