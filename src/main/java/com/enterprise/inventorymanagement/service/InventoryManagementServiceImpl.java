package com.enterprise.inventorymanagement.service;

import com.enterprise.inventorymanagement.exceptions.ResourceNotFoundException;
import com.enterprise.inventorymanagement.model.Department;
import com.enterprise.inventorymanagement.model.Enterprise;
import com.enterprise.inventorymanagement.model.InventoryItem;
import com.enterprise.inventorymanagement.model.Warehouse;
import com.enterprise.inventorymanagement.model.dto.ItemDTO;
import com.enterprise.inventorymanagement.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InventoryManagementServiceImpl implements InventoryManagementService {

    private final InventoryItemRepository itemRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final WarehouseRepository warehouseRepository;
    private final DepartmentRepository departmentRepository;

    @Autowired
    public InventoryManagementServiceImpl(
            InventoryItemRepository itemRepository,
            EnterpriseRepository enterpriseRepository,
            WarehouseRepository warehouseRepository,
            DepartmentRepository departmentRepository) {
        this.itemRepository = itemRepository;
        this.enterpriseRepository = enterpriseRepository;
        this.warehouseRepository = warehouseRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    @Transactional
    public ItemDTO createItem(Long enterpriseId, ItemDTO itemDTO) {
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise not found"));

        if (itemExistsByName(itemDTO.getName(), enterpriseId)) {
            throw new IllegalArgumentException("Item with this name already exists in the enterprise");
        }

        InventoryItem item = new InventoryItem();
        item.setName(itemDTO.getName());
        item.setDescription(itemDTO.getDescription());
        item.setQuantity(itemDTO.getQuantity());
        item.setEnterprise(enterprise);

        if (itemDTO.getWarehouseId() != null) {
            Warehouse warehouse = warehouseRepository.findById(itemDTO.getWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));
            item.setWarehouse(warehouse);
        }

        if (itemDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(itemDTO.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            item.setDepartment(department);
        }

        return convertToDTO(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDTO updateItem(Long id, ItemDTO itemDTO) {
        InventoryItem item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        item.setName(itemDTO.getName());
        item.setDescription(itemDTO.getDescription());
        item.setQuantity(itemDTO.getQuantity());

        if (itemDTO.getWarehouseId() != null) {
            Warehouse warehouse = warehouseRepository.findById(itemDTO.getWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));
            item.setWarehouse(warehouse);
        }

        if (itemDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(itemDTO.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            item.setDepartment(department);
        }

        return convertToDTO(itemRepository.save(item));
    }

    @Override
    @Transactional
    public void deleteItem(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Item not found");
        }
        itemRepository.deleteById(id);
    }

    @Override
    public Optional<ItemDTO> getItemById(Long id) {
        return itemRepository.findById(id).map(this::convertToDTO);
    }

    @Override
    public List<ItemDTO> getAllItemsByEnterpriseId(Long enterpriseId) {
        return itemRepository.findAllByEnterpriseId(enterpriseId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDTO> getItemsByWarehouseId(Long warehouseId) {
        return itemRepository.findAllByWarehouseId(warehouseId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDTO> getItemsByDepartmentId(Long departmentId) {
        return itemRepository.findAllByDepartmentId(departmentId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDTO> searchItemsByName(String keyword) {
        return itemRepository.findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean itemExistsByName(String name, Long enterpriseId) {
        return itemRepository.existsByNameAndEnterpriseId(name, enterpriseId);
    }

    private ItemDTO convertToDTO(InventoryItem item) {
        return ItemDTO.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .quantity(item.getQuantity())
                .warehouseId(item.getWarehouseId())
                .warehouseName(item.getWarehouseName())
                .departmentId(item.getDepartmentId())
                .departmentName(item.getDepartmentName())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
} 