package com.enterprise.inventorymanagement.service;

import com.enterprise.inventorymanagement.model.Warehouse;
import com.enterprise.inventorymanagement.model.dto.ItemDTO;
import com.enterprise.inventorymanagement.model.dto.ItemRequestDTO;
import com.enterprise.inventorymanagement.model.dto.WarehouseDTO;
import com.enterprise.inventorymanagement.model.request.RequestStatus;

import java.util.List;

public interface WarehouseService {
    WarehouseDTO createWarehouse(Long enterpriseId, Warehouse warehouse);
    WarehouseDTO updateWarehouse(Long warehouseId, Warehouse warehouse);
    void deleteWarehouse(Long warehouseId);
    WarehouseDTO getWarehouseById(Long warehouseId);
    List<WarehouseDTO> getWarehousesByEnterpriseId(Long enterpriseId);
    WarehouseDTO assignManager(Long warehouseId, Long userId);
    WarehouseDTO assignOperator(Long warehouseId, Long userId);
    WarehouseDTO getWarehouseByOperatorId(Long operatorId);
    List<ItemDTO> getWarehouseItems(Long warehouseId);
    ItemDTO addWarehouseItem(Long warehouseId, ItemDTO itemDTO);
    List<ItemRequestDTO> getWarehouseRequests(Long warehouseId);
    List<ItemRequestDTO> getWarehouseRequestsByStatus(Long warehouseId, RequestStatus status);
}
