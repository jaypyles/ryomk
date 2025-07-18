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

    @Value("${libvirt.connection.uri:qemu:///system}")
    private String libvirtUri;

    @Value("${vm.base.image.path:/k3s-base.qcow2}")
    private String baseImagePath;

    @Value("${vm.images.directory:/var/lib/libvirt/images}")
    private String vmImagesDirectory;

    @Value("${ssh.private.key.path:/root/.ssh/id_rsa}")
    private String privateKeyPath;

    @Bean
    public Connect libvirtConnect() throws Exception {
        logger.info("Attempting to connect to libvirt at: {}", libvirtUri);
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
