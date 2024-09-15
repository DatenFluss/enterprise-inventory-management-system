package com.enterprise.inventorymanagemet.service;

import com.enterprise.inventorymanagemet.model.InventoryItem;
import com.enterprise.inventorymanagemet.repository.InventoryItemRepository;
import com.enterprise.inventorymanagemet.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryItemServiceImpl implements InventoryItemService {

    private final InventoryItemRepository itemRepository;

    @Autowired
    public InventoryItemServiceImpl(InventoryItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public InventoryItem saveItem(InventoryItem item) {
        return itemRepository.save(item);
    }

    @Override
    public Optional<InventoryItem> getItemById(Long id) {
        return itemRepository.findById(id);
    }

    @Override
    public List<InventoryItem> getAllItems() {
        return itemRepository.findAll();
    }

    @Override
    public InventoryItem updateItem(Long id, InventoryItem updatedItem) {
        return itemRepository.findById(id)
                .map(item -> {
                    item.setName(updatedItem.getName());
                    item.setQuantity(updatedItem.getQuantity());
                    item.setLocation(updatedItem.getLocation());
                    return itemRepository.save(item);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id " + id));
    }

    @Override
    public void deleteItem(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Item not found with id " + id);
        }
        itemRepository.deleteById(id);
    }
}

