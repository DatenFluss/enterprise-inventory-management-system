package com.enterprise.inventorymanagemet.model.dto;

import com.enterprise.inventorymanagemet.model.request.RequestStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HandleItemRequestDTO {
    private RequestStatus status;
    private String comments;
}

