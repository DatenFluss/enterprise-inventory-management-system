package com.enterprise.inventorymanagemet.service;

import com.enterprise.inventorymanagemet.model.InventoryItem;
import com.enterprise.inventorymanagemet.model.ItemRequest;
import com.enterprise.inventorymanagemet.model.RequestStatus;
import com.enterprise.inventorymanagemet.model.User;
import com.enterprise.inventorymanagemet.repository.*;
import com.enterprise.inventorymanagemet.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
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

    /**
     * Employee requests an item.
     */
    public void requestItem(Long itemId, int quantity, String comments) {
        User currentUser = getCurrentAuthenticatedUser();

        if (!hasPermission(currentUser, "REQUEST_ITEM")) {
            throw new AccessDeniedException("You do not have permission to request items.");
        }

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
    public List<InventoryItem> viewAvailableItems() {
        User currentUser = getCurrentAuthenticatedUser();

        // Check if user has VIEW_AVAILABLE_ITEMS permission
        if (!hasPermission(currentUser, "VIEW_AVAILABLE_ITEMS")) {
            throw new AccessDeniedException("You do not have permission to view available items.");
        }

        // Return available items
        return itemRepository.findAllAvailableItems();
    }

}

