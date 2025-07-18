package com.k3skvmmaster.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class VmResponse {
    private String name;
    private String status;
    private String ipAddress;
    private Integer vcpu;
    private Integer memory;
    private LocalDateTime createdAt;
    private String k3sRole;
}