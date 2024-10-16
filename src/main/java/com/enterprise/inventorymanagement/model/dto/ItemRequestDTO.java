package com.enterprise.inventorymanagement.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemRequestDTO {
    private Long itemId;
    private int quantity;
    private String comments;
}

