package com.enterprise.inventorymanagement.service;

import com.enterprise.inventorymanagement.exceptions.ResourceNotFoundException;
import com.enterprise.inventorymanagement.model.Department;
import com.enterprise.inventorymanagement.model.Enterprise;
import com.enterprise.inventorymanagement.model.InventoryItem;
import com.enterprise.inventorymanagement.model.User;
import com.enterprise.inventorymanagement.model.Warehouse;
import com.enterprise.inventorymanagement.model.dto.ItemDTO;
import com.enterprise.inventorymanagement.repository.DepartmentRepository;
import com.enterprise.inventorymanagement.repository.EnterpriseRepository;
import com.enterprise.inventorymanagement.repository.InventoryItemRepository;
import com.enterprise.inventorymanagement.repository.UserRepository;
import com.enterprise.inventorymanagement.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InventoryManagementServiceImpl implements InventoryManagementService {

    private final InventoryItemRepository inventoryItemRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final WarehouseRepository warehouseRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public InventoryManagementServiceImpl(
            InventoryItemRepository inventoryItemRepository,
            EnterpriseRepository enterpriseRepository,
            WarehouseRepository warehouseRepository,
            DepartmentRepository departmentRepository,
            UserRepository userRepository
    ) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.enterpriseRepository = enterpriseRepository;
        this.warehouseRepository = warehouseRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ItemDTO createItem(Long enterpriseId, ItemDTO itemDTO) {
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise not found"));

        if (inventoryItemRepository.existsByNameAndEnterpriseId(itemDTO.getName(), enterpriseId)) {
            throw new IllegalArgumentException("Item with the same name already exists in the enterprise");
        }

        InventoryItem item = new InventoryItem();
        item.setName(itemDTO.getName());
        item.setDescription(itemDTO.getDescription());
        item.setQuantity(itemDTO.getQuantity());
        item.setEnterprise(enterprise);

        if (itemDTO.getWarehouseId() != null) {
            Warehouse warehouse = warehouseRepository.findById(itemDTO.getWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));
            if (!warehouse.getEnterprise().getId().equals(enterpriseId)) {
                throw new IllegalArgumentException("Warehouse does not belong to the enterprise");
            }
            item.setWarehouse(warehouse);
        }

        if (itemDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(itemDTO.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            if (!department.getEnterprise().getId().equals(enterpriseId)) {
                throw new IllegalArgumentException("Department does not belong to the enterprise");
            }
            item.setDepartment(department);
        }

        InventoryItem saved = inventoryItemRepository.save(item);
        return toDto(saved);
    }

    @Override
    @Transactional
    public ItemDTO updateItem(Long id, ItemDTO itemDTO) {
        InventoryItem item = inventoryItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        item.setName(itemDTO.getName());
        item.setDescription(itemDTO.getDescription());
        item.setQuantity(itemDTO.getQuantity());

        if (itemDTO.getWarehouseId() != null) {
            Warehouse warehouse = warehouseRepository.findById(itemDTO.getWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));
            if (!warehouse.getEnterprise().getId().equals(item.getEnterprise().getId())) {
                throw new IllegalArgumentException("Warehouse does not belong to the enterprise");
            }
            item.setWarehouse(warehouse);
            item.setDepartment(null);
            item.setUser(null);
        } else if (itemDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(itemDTO.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            if (!department.getEnterprise().getId().equals(item.getEnterprise().getId())) {
                throw new IllegalArgumentException("Department does not belong to the enterprise");
            }
            item.setDepartment(department);
            item.setWarehouse(null);
            item.setUser(null);
        }

        InventoryItem saved = inventoryItemRepository.save(item);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deleteItem(Long id) {
        if (!inventoryItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Item not found");
        }
        inventoryItemRepository.deleteById(id);
    }

    @Override
    public Optional<ItemDTO> getItemById(Long id) {
        return inventoryItemRepository.findByIdWithRelationships(id).map(this::toDto);
    }

    @Override
    public List<ItemDTO> getAllItemsByEnterpriseId(Long enterpriseId) {
        return inventoryItemRepository.findAllByEnterpriseId(enterpriseId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDTO> getItemsByWarehouseId(Long warehouseId) {
        return inventoryItemRepository.findAllByWarehouseId(warehouseId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDTO> getItemsByDepartmentId(Long departmentId) {
        return inventoryItemRepository.findAllByDepartmentId(departmentId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDTO> searchItemsByName(String keyword) {
        return inventoryItemRepository.findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean itemExistsByName(String name, Long enterpriseId) {
        return inventoryItemRepository.existsByNameAndEnterpriseId(name, enterpriseId);
    }

    @Override
    public List<ItemDTO> getItemsInUseByUserId(Long userId) {
        return inventoryItemRepository.findAllByUserId(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void returnItemToWarehouse(Long itemId, Integer quantity, Long departmentId, Long warehouseId) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        InventoryItem departmentItem = inventoryItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));

        if (!departmentItem.getEnterprise().getId().equals(warehouse.getEnterprise().getId())
                || !departmentItem.getEnterprise().getId().equals(department.getEnterprise().getId())) {
            throw new IllegalArgumentException("Cross-enterprise item return is not allowed");
        }

        if (departmentItem.getDepartment() == null || !departmentItem.getDepartment().getId().equals(departmentId)) {
            throw new IllegalArgumentException("Item does not belong to the provided department");
        }

        if (departmentItem.getQuantity() < quantity) {
            throw new IllegalArgumentException("Insufficient quantity to return");
        }

        departmentItem.setQuantity(departmentItem.getQuantity() - quantity);
        if (departmentItem.getQuantity() == 0) {
            inventoryItemRepository.delete(departmentItem);
        } else {
            inventoryItemRepository.save(departmentItem);
        }

        InventoryItem warehouseItem = inventoryItemRepository
                .findByNameAndWarehouse_Id(departmentItem.getName(), warehouseId)
                .orElseGet(() -> {
                    InventoryItem newItem = new InventoryItem();
                    newItem.setName(departmentItem.getName());
                    newItem.setDescription(departmentItem.getDescription());
                    newItem.setEnterprise(departmentItem.getEnterprise());
                    newItem.setWarehouse(warehouse);
                    newItem.setQuantity(0);
                    newItem.setMinimumQuantity(departmentItem.getMinimumQuantity());
                    newItem.setPrice(departmentItem.getPrice());
                    return newItem;
                });

        warehouseItem.setQuantity(warehouseItem.getQuantity() + quantity);
        inventoryItemRepository.save(warehouseItem);
    }

    @Override
    @Transactional
    public void returnItemToDepartment(Long itemId, Integer quantity, Long userId, Long departmentId) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        InventoryItem userItem = inventoryItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        if (!userItem.getEnterprise().getId().equals(department.getEnterprise().getId())) {
            throw new IllegalArgumentException("Cross-enterprise item return is not allowed");
        }

        if (userItem.getUser() == null || !userItem.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Item is not assigned to the user");
        }

        if (userItem.getQuantity() < quantity) {
            throw new IllegalArgumentException("Insufficient quantity to return");
        }

        userItem.setQuantity(userItem.getQuantity() - quantity);
        if (userItem.getQuantity() == 0) {
            inventoryItemRepository.delete(userItem);
        } else {
            inventoryItemRepository.save(userItem);
        }

        InventoryItem departmentItem = inventoryItemRepository
                .findByNameAndDepartment_Id(userItem.getName(), departmentId)
                .orElseGet(() -> {
                    InventoryItem newItem = new InventoryItem();
                    newItem.setName(userItem.getName());
                    newItem.setDescription(userItem.getDescription());
                    newItem.setEnterprise(userItem.getEnterprise());
                    newItem.setDepartment(department);
                    newItem.setQuantity(0);
                    newItem.setMinimumQuantity(userItem.getMinimumQuantity());
                    newItem.setPrice(userItem.getPrice());
                    return newItem;
                });

        departmentItem.setQuantity(departmentItem.getQuantity() + quantity);
        inventoryItemRepository.save(departmentItem);
    }

    private ItemDTO toDto(InventoryItem item) {
        return ItemDTO.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .quantity(item.getQuantity())
                .warehouseId(item.getWarehouseId())
                .warehouseName(item.getWarehouseName())
                .departmentId(item.getDepartmentId())
                .departmentName(item.getDepartmentName())
                .enterpriseId(item.getEnterprise() != null ? item.getEnterprise().getId() : null)
                .enterpriseName(item.getEnterprise() != null ? item.getEnterprise().getName() : null)
                .userId(item.getUser() != null ? item.getUser().getId() : null)
                .userName(item.getUser() != null ? item.getUser().getUsername() : null)
                .checkedOutAt(item.getCheckedOutAt())
                .dueDate(item.getDueDate())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
