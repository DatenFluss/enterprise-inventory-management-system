package com.enterprise.inventorymanagement.model.request;

import com.enterprise.inventorymanagement.model.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "item_requests")
public class ItemRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user (department manager) who made the request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    // The source warehouse
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse sourceWarehouse;

    // The target department
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_department_id", nullable = false)
    private Department targetDepartment;

    // List of requested items with their quantities
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RequestItem> requestItems = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private RequestStatus status;

    private String comments;

    // Response from warehouse operator
    private String responseComments;

    // The warehouse operator who processed the request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processor_id")
    private User processor;

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;

    @Column(name = "processed_date")
    private LocalDateTime processedDate;

    // Helper method to add items to the request
    public void addRequestItem(RequestItem item) {
        requestItems.add(item);
        item.setRequest(this);
    }

    // Helper method to remove items from the request
    public void removeRequestItem(RequestItem item) {
        requestItems.remove(item);
        item.setRequest(null);
    }

    @PrePersist
    protected void onCreate() {
        requestDate = LocalDateTime.now();
        if (status == null) {
            status = RequestStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if ((status == RequestStatus.APPROVED || status == RequestStatus.REJECTED) && processedDate == null) {
            processedDate = LocalDateTime.now();
        }
    }
}
