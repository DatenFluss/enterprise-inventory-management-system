package com.enterprise.inventorymanagemet.controller;

import com.enterprise.inventorymanagemet.model.MessageResponse;
import com.enterprise.inventorymanagemet.model.dto.HandleItemRequestDTO;
import com.enterprise.inventorymanagemet.model.dto.ItemRequestDTO;
import com.enterprise.inventorymanagemet.service.ItemRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/item-requests")
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @Autowired
    public ItemRequestController(ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    /**
     * Employee requests an item.
     */
    @PreAuthorize("hasAuthority('REQUEST_ITEM')")
    @PostMapping("/request")
    public ResponseEntity<?> requestItem(@RequestBody ItemRequestDTO requestDTO) {
        itemRequestService.requestItem(requestDTO.getItemId(), requestDTO.getQuantity(), requestDTO.getComments());
        return ResponseEntity.ok(new MessageResponse("Item request submitted successfully."));
    }

    /**
     * Manager or owner handles the item request.
     */
    @PreAuthorize("hasAuthority('HANDLE_ITEM_REQUEST')")
    @PutMapping("/handle/{requestId}")
    public ResponseEntity<?> handleItemRequest(
            @PathVariable Long requestId,
            @RequestBody HandleItemRequestDTO handleDTO) {
        itemRequestService.handleItemRequest(requestId, handleDTO.getStatus(), handleDTO.getComments());
        return ResponseEntity.ok(new MessageResponse("Item request processed successfully."));
    }
}
