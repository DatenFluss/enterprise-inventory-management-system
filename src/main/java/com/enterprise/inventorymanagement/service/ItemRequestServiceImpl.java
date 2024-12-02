package com.enterprise.inventorymanagement.service;

import com.enterprise.inventorymanagement.exceptions.ResourceNotFoundException;
import com.enterprise.inventorymanagement.model.request.ItemRequest;
import com.enterprise.inventorymanagement.model.request.RequestItem;
import com.enterprise.inventorymanagement.model.User;
import com.enterprise.inventorymanagement.model.InventoryItem;
import com.enterprise.inventorymanagement.model.Department;
import com.enterprise.inventorymanagement.model.Warehouse;
import com.enterprise.inventorymanagement.model.dto.ItemRequestDTO;
import com.enterprise.inventorymanagement.model.dto.RequestItemDTO;
import com.enterprise.inventorymanagement.model.request.RequestStatus;
import com.enterprise.inventorymanagement.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final WarehouseRepository warehouseRepository;
    private final DepartmentRepository departmentRepository;

    @Autowired
    public ItemRequestServiceImpl(
            ItemRequestRepository itemRequestRepository,
            UserRepository userRepository,
            InventoryItemRepository inventoryItemRepository,
            WarehouseRepository warehouseRepository,
            DepartmentRepository departmentRepository) {
        this.itemRequestRepository = itemRequestRepository;
        this.userRepository = userRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.warehouseRepository = warehouseRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    @Transactional
    public ItemRequestDTO createItemRequest(Long userId, ItemRequestDTO requestDTO) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ItemRequest request = new ItemRequest();
        request.setRequester(requester);
        request.setSourceWarehouse(warehouseRepository.findById(requestDTO.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found")));
        request.setTargetDepartment(departmentRepository.findById(requestDTO.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found")));
        request.setComments(requestDTO.getComments());
        request.setStatus(RequestStatus.PENDING);
        request.setRequestDate(LocalDateTime.now());

        if (requestDTO.getRequestItems() != null) {
            for (RequestItemDTO itemDTO : requestDTO.getRequestItems()) {
                InventoryItem item = inventoryItemRepository.findById(itemDTO.getItemId())
                        .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemDTO.getItemId()));

                if (itemDTO.getQuantity() > item.getQuantity()) {
                    throw new IllegalArgumentException(
                        String.format("Requested quantity (%d) exceeds available quantity (%d) for item: %s",
                            itemDTO.getQuantity(), item.getQuantity(), item.getName())
                    );
                }

                RequestItem requestItem = new RequestItem();
                requestItem.setInventoryItem(item);
                requestItem.setQuantity(itemDTO.getQuantity());
                requestItem.setComments(itemDTO.getComments());
                request.addRequestItem(requestItem);
            }
        }

        request = itemRequestRepository.save(request);
        return convertToRequestDTO(request);
    }

    @Override
    @Transactional
    public void handleItemRequest(Long requestId, boolean approved, String responseComments) {
        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Request has already been processed");
        }

        request.setStatus(approved ? RequestStatus.APPROVED : RequestStatus.REJECTED);
        request.setResponseComments(responseComments);
        request.setProcessedDate(LocalDateTime.now());

        if (approved) {
            for (RequestItem requestItem : request.getRequestItems()) {
                InventoryItem item = requestItem.getInventoryItem();
                if (item.getQuantity() < requestItem.getQuantity()) {
                    throw new IllegalStateException(
                        String.format("Insufficient quantity available for item: %s", item.getName())
                    );
                }
                item.setQuantity(item.getQuantity() - requestItem.getQuantity());
                inventoryItemRepository.save(item);
            }
        }

        itemRequestRepository.save(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDTO> getRequestsByWarehouseId(Long warehouseId) {
        return itemRequestRepository.findBySourceWarehouseId(warehouseId)
                .stream()
                .map(this::convertToRequestDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDTO> getRequestsByWarehouseIdAndStatus(Long warehouseId, RequestStatus status) {
        return itemRequestRepository.findBySourceWarehouseIdAndStatus(warehouseId, status)
                .stream()
                .map(this::convertToRequestDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDTO> getRequestsByDepartmentId(Long departmentId) {
        return itemRequestRepository.findByTargetDepartmentId(departmentId)
                .stream()
                .map(this::convertToRequestDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDTO> getRequestsByDepartmentIdAndStatus(Long departmentId, RequestStatus status) {
        return itemRequestRepository.findByTargetDepartmentIdAndStatus(departmentId, status)
                .stream()
                .map(this::convertToRequestDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDTO> getRequestsByUserId(Long userId) {
        return itemRequestRepository.findByRequesterId(userId)
                .stream()
                .map(this::convertToRequestDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDTO> getRequestsByUserIdAndStatus(Long userId, RequestStatus status) {
        return itemRequestRepository.findByRequesterIdAndStatus(userId, status)
                .stream()
                .map(this::convertToRequestDTO)
                .collect(Collectors.toList());
    }

    private ItemRequestDTO convertToRequestDTO(ItemRequest request) {
        return ItemRequestDTO.builder()
                .id(request.getId())
                .requesterId(request.getRequester().getId())
                .requesterName(request.getRequester().getUsername())
                .warehouseId(request.getSourceWarehouse().getId())
                .warehouseName(request.getSourceWarehouse().getName())
                .departmentId(request.getTargetDepartment().getId())
                .departmentName(request.getTargetDepartment().getName())
                .status(request.getStatus())
                .requestDate(request.getRequestDate())
                .processedDate(request.getProcessedDate())
                .comments(request.getComments())
                .responseComments(request.getResponseComments())
                .processorId(request.getProcessor() != null ? request.getProcessor().getId() : null)
                .processorName(request.getProcessor() != null ? request.getProcessor().getUsername() : null)
                .requestItems(request.getRequestItems().stream()
                        .map(this::convertToRequestItemDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    private RequestItemDTO convertToRequestItemDTO(RequestItem requestItem) {
        return RequestItemDTO.builder()
                .id(requestItem.getId())
                .itemId(requestItem.getInventoryItem().getId())
                .itemName(requestItem.getInventoryItem().getName())
                .quantity(requestItem.getQuantity())
                .comments(requestItem.getComments())
                .build();
    }
} 