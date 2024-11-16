package com.enterprise.inventorymanagement.service;

import com.enterprise.inventorymanagement.model.Warehouse;
import com.enterprise.inventorymanagement.model.dto.WarehouseDTO;
import java.util.List;

public interface WarehouseService {
    WarehouseDTO createWarehouse(Long enterpriseId, Warehouse warehouse);
    WarehouseDTO updateWarehouse(Long warehouseId, Warehouse warehouse);
    void deleteWarehouse(Long warehouseId);
    WarehouseDTO getWarehouseById(Long warehouseId);
    List<WarehouseDTO> getWarehousesByEnterpriseId(Long enterpriseId);
    WarehouseDTO assignManager(Long warehouseId, Long userId);
}
