package com.k3skvmmaster.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.k3skvmmaster.model.dto.VmRequest;
import com.k3skvmmaster.model.rto.VmResponse;
import com.k3skvmmaster.service.VmService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/vms")
public class VmController {

    private static final Logger logger = LoggerFactory.getLogger(VmController.class);

    @Autowired
    private VmService vmService;

    @PostMapping
    public ResponseEntity<?> createVm(@Valid @RequestBody VmRequest request) throws Exception {
        try {
            VmResponse response = vmService.createVm(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to get join token: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get join token: " + e.getMessage());
        }
    }

    @DeleteMapping("/{vmName}")
    public ResponseEntity<Void> deleteVm(@PathVariable String vmName,
            @RequestParam(required = false) String libvirtUri) {
        vmService.deleteVm(vmName, libvirtUri);
        return ResponseEntity.noContent().build();
    }

}