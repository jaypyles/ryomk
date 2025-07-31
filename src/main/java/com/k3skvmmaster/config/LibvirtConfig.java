package com.k3skvmmaster.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@Data
public class LibvirtConfig {

    @Value("${libvirt.connection.uri}")
    private String libvirtUri;

    @Value("${vm.base.image.path}")
    private String baseImagePath;

    @Value("${vm.images.directory}")
    private String vmImagesDirectory;
}
