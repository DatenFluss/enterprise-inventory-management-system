package com.enterprise.inventorymanagement.model;

public class MessageResponse {

    private String message;

    public MessageResponse(String message) {
        this.message = message;
    }

    // Getter
    public String getMessage() {
        return message;
    }

    // Setter (optional, if you need it)
    public void setMessage(String message) {
        this.message = message;
    }
}
