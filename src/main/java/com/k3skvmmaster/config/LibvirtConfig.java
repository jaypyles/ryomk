package com.k3skvmmaster.config;

import org.libvirt.Connect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@Data
public class LibvirtConfig {

    private static final Logger logger = LoggerFactory.getLogger(LibvirtConfig.class);

    @Value("${libvirt.connection.uri}")
    private String libvirtUri;

    @Value("${vm.base.image.path}")
    private String baseImagePath;

    @Value("${vm.images.directory}")
    private String vmImagesDirectory;

    @Bean
    public Connect libvirtConnect() throws Exception {
        logger.info("Attempting to connect to libvirt at: {}", libvirtUri);

        // Debug: Check SSH key accessibility
        try {
            java.nio.file.Path sshKeyPath = java.nio.file.Paths.get("/root/.ssh/id_rsa");
            if (java.nio.file.Files.exists(sshKeyPath)) {
                logger.info("SSH key exists at: {}", sshKeyPath);
                logger.info("SSH key readable: {}", java.nio.file.Files.isReadable(sshKeyPath));
            } else {
                logger.warn("SSH key not found at: {}", sshKeyPath);
            }
        } catch (Exception e) {
            logger.error("Error checking SSH key: {}", e.getMessage());
        }

        try {
            Connect connect = new Connect(libvirtUri);
            logger.info("Successfully connected to libvirt");
            return connect;
        } catch (Exception e) {
            logger.error("Failed to connect to libvirt: {}", e.getMessage(), e);
            throw e;
        }
    }
}
