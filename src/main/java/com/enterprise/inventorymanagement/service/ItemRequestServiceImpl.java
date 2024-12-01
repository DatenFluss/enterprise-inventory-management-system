package com.enterprise.inventorymanagement.service;

import com.enterprise.inventorymanagement.exceptions.ResourceNotFoundException;
import com.enterprise.inventorymanagement.model.*;
import com.enterprise.inventorymanagement.model.dto.ItemRequestDTO;
import com.enterprise.inventorymanagement.model.dto.RequestItemDTO;
import com.enterprise.inventorymanagement.model.request.ItemRequest;
import com.enterprise.inventorymanagement.model.request.RequestItem;
import com.enterprise.inventorymanagement.model.request.RequestStatus;
import com.enterprise.inventorymanagement.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemRequestServiceImpl extends ServiceCommon implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final RequestItemRepository requestItemRepository;
    private final WarehouseRepository warehouseRepository;
    private final DepartmentRepository departmentRepository;

    @Autowired
    public ItemRequestServiceImpl(
            UserRepository userRepository,
            InventoryItemRepository itemRepository,
            RoleRepository roleRepository,
            EnterpriseRepository enterpriseRepository,
            ItemRequestRepository itemRequestRepository,
            RequestItemRepository requestItemRepository,
            WarehouseRepository warehouseRepository,
            DepartmentRepository departmentRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationFacade authenticationFacade) {
        super(userRepository, itemRepository, roleRepository, enterpriseRepository, passwordEncoder, authenticationFacade);
        this.itemRequestRepository = itemRequestRepository;
        this.requestItemRepository = requestItemRepository;
        this.warehouseRepository = warehouseRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    @Transactional
    public ItemRequestDTO createItemRequest(Long userId, ItemRequestDTO requestDTO) {
        // Validate user
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate warehouse
        Warehouse warehouse = warehouseRepository.findById(requestDTO.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));

        // Validate department
        Department department = departmentRepository.findById(requestDTO.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        // Create the request
        ItemRequest request = new ItemRequest();
        request.setRequester(requester);
        request.setSourceWarehouse(warehouse);
        request.setTargetDepartment(department);
        request.setComments(requestDTO.getComments());
        request.setStatus(RequestStatus.PENDING);

        // Add request items
        for (RequestItemDTO itemDTO : requestDTO.getRequestItems()) {
            InventoryItem item = itemRepository.findById(itemDTO.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemDTO.getItemId()));

            // Validate quantity
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

        // Save the request
        request = itemRequestRepository.save(request);
        return convertToRequestDTO(request);
    }

    @Override
    @Transactional
    public void handleItemRequest(Long requestId, boolean approved, String responseComments) {
        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Request is no longer pending");
        }

        User currentUser = getCurrentAuthenticatedUser();
        request.setProcessor(currentUser);
        request.setResponseComments(responseComments);
        request.setProcessedDate(LocalDateTime.now());

        if (approved) {
            // Validate quantities again
            for (RequestItem requestItem : request.getRequestItems()) {
                InventoryItem item = requestItem.getInventoryItem();
                if (requestItem.getQuantity() > item.getQuantity()) {
                    throw new IllegalStateException(
                        String.format("Insufficient quantity available for item: %s", item.getName())
                    );
                }
                // Update inventory quantities
                item.setQuantity(item.getQuantity() - requestItem.getQuantity());
                itemRepository.save(item);
            }
            request.setStatus(RequestStatus.APPROVED);
        } else {
            request.setStatus(RequestStatus.REJECTED);
        }

        itemRequestRepository.save(request);
    }

    @Override
    public List<ItemRequestDTO> getRequestsByWarehouseId(Long warehouseId) {
        return itemRequestRepository.findByWarehouseId(warehouseId)
                .stream()
                .map(this::convertToRequestDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDTO> getRequestsByWarehouseIdAndStatus(Long warehouseId, RequestStatus status) {
        return itemRequestRepository.findByWarehouseIdAndStatus(warehouseId, status)
                .stream()
                .map(this::convertToRequestDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDTO> getRequestsByDepartmentId(Long departmentId) {
        return itemRequestRepository.findByDepartmentId(departmentId)
                .stream()
                .map(this::convertToRequestDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDTO> getRequestsByDepartmentIdAndStatus(Long departmentId, RequestStatus status) {
        return itemRequestRepository.findByDepartmentIdAndStatus(departmentId, status)
                .stream()
                .map(this::convertToRequestDTO)
                .collect(Collectors.toList());
    }

    private ItemRequestDTO convertToRequestDTO(ItemRequest request) {
        List<RequestItemDTO> itemDTOs = request.getRequestItems().stream()
                .map(this::convertToRequestItemDTO)
                .collect(Collectors.toList());

        return ItemRequestDTO.builder()
                .id(request.getId())
                .warehouseId(request.getSourceWarehouse().getId())
                .warehouseName(request.getSourceWarehouse().getName())
                .departmentId(request.getTargetDepartment().getId())
                .departmentName(request.getTargetDepartment().getName())
                .requestItems(itemDTOs)
                .status(request.getStatus().toString())
                .comments(request.getComments())
                .responseComments(request.getResponseComments())
                .requesterId(request.getRequester().getId())
                .requesterName(request.getRequester().getUsername())
                .processorId(request.getProcessor() != null ? request.getProcessor().getId() : null)
                .processorName(request.getProcessor() != null ? request.getProcessor().getUsername() : null)
                .requestDate(request.getRequestDate())
                .processedDate(request.getProcessedDate())
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