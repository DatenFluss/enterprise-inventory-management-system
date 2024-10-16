package com.enterprise.inventorymanagement.service;

import com.enterprise.inventorymanagement.exceptions.ResourceNotFoundException;
import com.enterprise.inventorymanagement.model.InventoryItem;
import com.enterprise.inventorymanagement.model.User;
import com.enterprise.inventorymanagement.model.request.ItemRequest;
import com.enterprise.inventorymanagement.model.request.RequestStatus;
import com.enterprise.inventorymanagement.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ItemRequestService extends ServiceCommon {

    private final ItemRequestRepository itemRequestRepository;

    @Autowired
    public ItemRequestService(
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
     * Manager or owner handles the item request (approve or reject).
     */
    public void handleItemRequest(Long requestId, RequestStatus status, String responseComments) {
        User currentUser = getCurrentAuthenticatedUser();

        if (!hasPermission(currentUser, "HANDLE_ITEM_REQUEST")) {
            throw new AccessDeniedException("You do not have permission to handle item requests.");
        }

        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Item request not found"));

        if (itemRequest.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Item request has already been processed.");
        }

        if (status == RequestStatus.APPROVED) {
            InventoryItem item = itemRequest.getInventoryItem();
            int availableQuantity = item.getQuantity();

            if (itemRequest.getQuantity() > availableQuantity) {
                throw new IllegalArgumentException("Not enough items in inventory to fulfill the request.");
            }

            // Update inventory quantity
            item.setQuantity(availableQuantity - itemRequest.getQuantity());
            itemRepository.save(item);
        }

        // Update request status and comments
        itemRequest.setStatus(status);
        itemRequest.setComments(responseComments);
        itemRequestRepository.save(itemRequest);
    }
}

