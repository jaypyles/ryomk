package com.k3skvmmaster.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@Data
public class K3sConfig {
    @Value("${k3s.master.ip}")
    private String masterNodeIp;

    @Value("${k3s.master.version}")
    private String masterVersion;

    @Value("${k3s.master.network}")
    private String masterNetwork;

    @Value("${k3s.master.bridge}")
    private String masterBridge;
}