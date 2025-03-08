package com.example.talepyonetimbackend.talepyonetim.service;

import com.example.talepyonetimbackend.talepyonetim.dto.DeliveryRequest;
import com.example.talepyonetimbackend.talepyonetim.dto.RequestDto;
import com.example.talepyonetimbackend.talepyonetim.dto.UpdateStatusRequest;
import com.example.talepyonetimbackend.talepyonetim.exception.ResourceNotFoundException;
import com.example.talepyonetimbackend.talepyonetim.model.Request;
import com.example.talepyonetimbackend.talepyonetim.model.RequestStatus;
import com.example.talepyonetimbackend.talepyonetim.model.User;
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
        dto.setComment(request.getComment());
        dto.setOrderNumber(request.getOrderNumber());
        dto.setOrderDate(request.getOrderDate());
        dto.setDeliveryNotes(request.getDeliveryNotes());
        dto.setDeliveryDate(request.getDeliveryDate());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setUpdatedAt(request.getUpdatedAt());

        return dto;
    }
}
