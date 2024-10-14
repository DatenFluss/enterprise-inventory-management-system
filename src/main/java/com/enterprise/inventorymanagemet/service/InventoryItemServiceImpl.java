package com.enterprise.inventorymanagemet.service;

import com.enterprise.inventorymanagemet.model.InventoryItem;
import com.enterprise.inventorymanagemet.model.request.ItemRequest;
import com.enterprise.inventorymanagemet.model.User;
import com.enterprise.inventorymanagemet.model.request.RequestStatus;
import com.enterprise.inventorymanagemet.repository.*;
import com.enterprise.inventorymanagemet.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryItemServiceImpl extends ServiceCommon implements InventoryItemService {

    private final ItemRequestRepository itemRequestRepository;

    @Autowired
    public InventoryItemServiceImpl(
            UserRepository userRepository,
            InventoryItemRepository itemRepository,
            RoleRepository roleRepository,
            EnterpriseRepository enterpriseRepository,
            ItemRequestRepository itemRequestRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationFacade authenticationFacade
    ) {
        super(
                userRepository,
                itemRepository,
                roleRepository,
                enterpriseRepository,
                passwordEncoder,
                authenticationFacade
        );
        this.itemRequestRepository = itemRequestRepository;
    }

    @Override
    @PreAuthorize("hasAuthority('CREATE_ITEM')")
    public InventoryItem saveItem(InventoryItem item) {
        if (itemExistsByName(item.getName())) {
            throw new IllegalArgumentException("An item with this name already exists.");
        }
        return itemRepository.save(item);
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_ITEMS')")
    public Optional<InventoryItem> getItemById(Long id) {
        return itemRepository.findById(id);
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_ITEMS')")
    public List<InventoryItem> getAllItems() {
        return itemRepository.findAll();
    }

    @Override
    @PreAuthorize("hasAuthority('UPDATE_ITEM')")
    public InventoryItem updateItem(Long id, InventoryItem updatedItem) {
        return itemRepository.findById(id)
                .map(item -> {
                    item.setName(updatedItem.getName());
                    item.setQuantity(updatedItem.getQuantity());
                    item.setLocation(updatedItem.getLocation());
                    item.setDescription(updatedItem.getDescription());
                    return itemRepository.save(item);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id " + id));
    }

    @Override
    @PreAuthorize("hasAuthority('DELETE_ITEM')")
    public void deleteItem(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Item not found with id " + id);
        }
        itemRepository.deleteById(id);
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_ITEMS')")
    public Optional<InventoryItem> getItemByName(String name) {
        return itemRepository.findByName(name);
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_ITEMS')")
    public List<InventoryItem> searchItemsByName(String keyword) {
        return itemRepository.findByNameContainingIgnoreCase(keyword);
    }

    @Override
    public boolean itemExistsByName(String name) {
        return itemRepository.existsByName(name);
    }

    /**
     * Employee requests an item.
     */
    @Override
    @PreAuthorize("hasAuthority('REQUEST_ITEM')")
    public void requestItem(Long itemId, int quantity, String comments) {
        User currentUser = getCurrentAuthenticatedUser();

        InventoryItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setRequester(currentUser);
        itemRequest.setInventoryItem(item);
        itemRequest.setQuantity(quantity);
        itemRequest.setStatus(RequestStatus.PENDING);
        itemRequest.setComments(comments);
        itemRequest.setRequestDate(LocalDateTime.now());

        itemRequestRepository.save(itemRequest);
    }

    /**
     * View all available items.
     */
    @Override
    @PreAuthorize("hasAuthority('VIEW_AVAILABLE_ITEMS')")
    public List<InventoryItem> viewAvailableItems() {
        return itemRepository.findByQuantityGreaterThan(0);
    }

}
