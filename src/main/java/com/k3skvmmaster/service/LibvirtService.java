package com.k3skvmmaster.service;

import org.libvirt.Connect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LibvirtService {

    private static final Logger logger = LoggerFactory.getLogger(LibvirtService.class);

    public Connect connect(String uri) throws Exception {
        logger.info("Attempting to connect to libvirt at: {}", uri);

        try {
            Connect conn = new Connect(uri);
            logger.info("Connected to libvirt.");
            return conn;
        } catch (Exception e) {
            logger.error("Failed to connect to libvirt: {}", e.getMessage(), e);
            throw e;
        }
    }
}