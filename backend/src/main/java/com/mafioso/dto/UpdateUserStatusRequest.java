package com.mafioso.dto;

/**
 * Admin request payload for activating or deactivating a user account.
 */
public class UpdateUserStatusRequest {

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
