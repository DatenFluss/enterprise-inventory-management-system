package com.enterprise.inventorymanagement.service;

import com.enterprise.inventorymanagement.exceptions.ResourceNotFoundException;
import com.enterprise.inventorymanagement.model.Enterprise;
import com.enterprise.inventorymanagement.model.User;
import com.enterprise.inventorymanagement.model.Warehouse;
import com.enterprise.inventorymanagement.model.dto.WarehouseDTO;
import com.enterprise.inventorymanagement.repository.EnterpriseRepository;
import com.enterprise.inventorymanagement.repository.UserRepository;
import com.enterprise.inventorymanagement.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final UserRepository userRepository;

    @Autowired
    public WarehouseServiceImpl(WarehouseRepository warehouseRepository,
                                EnterpriseRepository enterpriseRepository,
                                UserRepository userRepository) {
        this.warehouseRepository = warehouseRepository;
        this.enterpriseRepository = enterpriseRepository;
        this.userRepository = userRepository;
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

    private WarehouseDTO convertToDTO(Warehouse warehouse) {
        return WarehouseDTO.builder()
                .id(warehouse.getId())
                .name(warehouse.getName())
                .description(warehouse.getDescription())
                .location(warehouse.getLocation())
                .enterpriseId(warehouse.getEnterprise().getId())
                .enterpriseName(warehouse.getEnterprise().getName())
                .managerId(warehouse.getManager() != null ? warehouse.getManager().getId() : null)
                .managerName(warehouse.getManager() != null ? warehouse.getManager().getFullName() : null)
                .itemCount(warehouse.getItems().size())
                .build();
    }
}
