package com.enterprise.inventorymanagement.service;

import com.enterprise.inventorymanagement.exceptions.ResourceNotFoundException;
import com.enterprise.inventorymanagement.model.*;
import com.enterprise.inventorymanagement.model.dto.ItemDTO;
import com.enterprise.inventorymanagement.model.dto.ItemRequestDTO;
import com.enterprise.inventorymanagement.model.dto.RequestItemDTO;
import com.enterprise.inventorymanagement.model.dto.WarehouseDTO;
import com.enterprise.inventorymanagement.model.request.ItemRequest;
import com.enterprise.inventorymanagement.model.request.RequestItem;
import com.enterprise.inventorymanagement.model.request.RequestStatus;
import com.enterprise.inventorymanagement.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final InventoryItemRepository itemRepository;
    private final ItemRequestRepository requestRepository;

    @Autowired
    public WarehouseServiceImpl(WarehouseRepository warehouseRepository,
                                EnterpriseRepository enterpriseRepository,
                                UserRepository userRepository,
                                RoleRepository roleRepository,
                                InventoryItemRepository itemRepository,
                                ItemRequestRepository requestRepository) {
        this.warehouseRepository = warehouseRepository;
        this.enterpriseRepository = enterpriseRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.itemRepository = itemRepository;
        this.requestRepository = requestRepository;
    }

    @Override
    public WarehouseDTO createWarehouse(Long enterpriseId, Warehouse warehouse) {
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise not found with id: " + enterpriseId));

        if (warehouseRepository.existsByNameAndEnterpriseId(warehouse.getName(), enterpriseId)) {
            throw new IllegalArgumentException("Warehouse with this name already exists in the enterprise");
        }

        warehouse.setEnterprise(enterprise);
        return convertToDTO(warehouseRepository.save(warehouse));
    }

    @Override
    public WarehouseDTO updateWarehouse(Long warehouseId, Warehouse warehouseDetails) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + warehouseId));

        if (!warehouse.getName().equals(warehouseDetails.getName()) &&
                warehouseRepository.existsByNameAndEnterpriseId(warehouseDetails.getName(), warehouse.getEnterprise().getId())) {
            throw new IllegalArgumentException("Warehouse with this name already exists in the enterprise");
        }

        warehouse.setName(warehouseDetails.getName());
        warehouse.setDescription(warehouseDetails.getDescription());
        warehouse.setLocation(warehouseDetails.getLocation());

        return convertToDTO(warehouseRepository.save(warehouse));
    }

    @Override
    public void deleteWarehouse(Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + warehouseId));

        if (!warehouse.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot delete warehouse that contains items");
        }

        warehouseRepository.deleteById(warehouseId);
    }

    @Override
    public WarehouseDTO getWarehouseById(Long warehouseId) {
        return warehouseRepository.findById(warehouseId)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + warehouseId));
    }

    @Override
    public List<WarehouseDTO> getWarehousesByEnterpriseId(Long enterpriseId) {
        return warehouseRepository.findByEnterpriseId(enterpriseId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public WarehouseDTO assignManager(Long warehouseId, Long userId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + warehouseId));

        User manager = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!manager.getEnterprise().getId().equals(warehouse.getEnterprise().getId())) {
            throw new IllegalArgumentException("Manager must belong to the same enterprise");
        }

        warehouse.setManager(manager);
        return convertToDTO(warehouseRepository.save(warehouse));
    }

    @Override
    public WarehouseDTO assignOperator(Long warehouseId, Long userId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + warehouseId));

        User operator = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if user is an enterprise owner
        if (operator.getRole().getName() == RoleName.ROLE_ENTERPRISE_OWNER) {
            throw new IllegalArgumentException("Enterprise owners cannot be assigned as warehouse operators");
        }

        // Check if user already has a warehouse assignment
        if (warehouseRepository.findByOperatorId(userId).isPresent()) {
            throw new IllegalArgumentException("User is already assigned as an operator to another warehouse");
        }

        // Get the warehouse operator role
        Role operatorRole = roleRepository.findByName(RoleName.ROLE_WAREHOUSE_OPERATOR)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse operator role not found"));

        // Set the operator's role
        operator.setRole(operatorRole);
        userRepository.save(operator);

        // Set the warehouse operator
        warehouse.setOperator(operator);
        return convertToDTO(warehouseRepository.save(warehouse));
    }

    @Override
    public WarehouseDTO getWarehouseByOperatorId(Long operatorId) {
        return warehouseRepository.findByOperatorId(operatorId)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("No warehouse found for operator with id: " + operatorId));
    }

    @Override
    public List<ItemDTO> getWarehouseItems(Long warehouseId) {
        return itemRepository.findAllByWarehouseId(warehouseId)
                .stream()
                .map(this::convertToItemDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDTO addWarehouseItem(Long warehouseId, ItemDTO itemDTO) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + warehouseId));

        InventoryItem item = new InventoryItem();
        item.setName(itemDTO.getName());
        item.setDescription(itemDTO.getDescription());
        item.setQuantity(itemDTO.getQuantity());
        item.setWarehouse(warehouse);
        item.setEnterprise(warehouse.getEnterprise());
        item.setUpdatedAt(LocalDateTime.now());

        return convertToItemDTO(itemRepository.save(item));
    }

    @Override
    public List<ItemRequestDTO> getWarehouseRequests(Long warehouseId) {
        return requestRepository.findBySourceWarehouseId(warehouseId)
                .stream()
                .map(this::convertToRequestDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDTO> getWarehouseRequestsByStatus(Long warehouseId, RequestStatus status) {
        return requestRepository.findBySourceWarehouseIdAndStatus(warehouseId, status)
                .stream()
                .map(this::convertToRequestDTO)
                .collect(Collectors.toList());
    }

    private WarehouseDTO convertToDTO(Warehouse warehouse) {
        Integer itemCount = itemRepository.countItemsByWarehouseId(warehouse.getId());
        return WarehouseDTO.builder()
                .id(warehouse.getId())
                .name(warehouse.getName())
                .description(warehouse.getDescription())
                .location(warehouse.getLocation())
                .enterpriseId(warehouse.getEnterprise().getId())
                .enterpriseName(warehouse.getEnterprise().getName())
                .managerId(warehouse.getManager() != null ? warehouse.getManager().getId() : null)
                .managerName(warehouse.getManager() != null ? warehouse.getManager().getFullName() : null)
                .operatorId(warehouse.getOperator() != null ? warehouse.getOperator().getId() : null)
                .operatorName(warehouse.getOperator() != null ? warehouse.getOperator().getFullName() : null)
                .itemCount(itemCount != null ? itemCount : 0)
                .build();
    }

    private ItemDTO convertToItemDTO(InventoryItem item) {
        return ItemDTO.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .quantity(item.getQuantity())
                .warehouseId(item.getWarehouse() != null ? item.getWarehouse().getId() : null)
                .warehouseName(item.getWarehouse() != null ? item.getWarehouse().getName() : null)
                .departmentId(item.getDepartment() != null ? item.getDepartment().getId() : null)
                .departmentName(item.getDepartment() != null ? item.getDepartment().getName() : null)
                .enterpriseId(item.getEnterprise().getId())
                .enterpriseName(item.getEnterprise().getName())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
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
