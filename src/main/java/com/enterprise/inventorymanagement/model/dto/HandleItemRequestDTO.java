package com.enterprise.inventorymanagement.model.dto;

import com.enterprise.inventorymanagement.model.request.RequestStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HandleItemRequestDTO {
    private RequestStatus status;
    private String comments;
}

