package com.enterprise.inventorymanagement.model.request;

import com.enterprise.inventorymanagement.model.InventoryItem;
import com.enterprise.inventorymanagement.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "item_requests")
public class ItemRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user who made the request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    // The requested item
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.ORDINAL)
    @Column(length = 20, nullable = false)
    private RequestStatus status;

    private String comments;

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;

}
