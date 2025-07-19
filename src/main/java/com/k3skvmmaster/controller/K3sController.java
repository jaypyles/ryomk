package com.k3skvmmaster.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.k3skvmmaster.model.dto.CreateNodeRequest;
import com.k3skvmmaster.service.K3sService;
import com.k3skvmmaster.service.VmService;

@RestController
@RequestMapping("/api/v1/clusters")
public class K3sController {

    private static final Logger logger = LoggerFactory.getLogger(K3sController.class);

    @Autowired
    private K3sService k3sService;

    @Autowired
    private VmService vmService;

    @GetMapping("/join-token")
    public ResponseEntity<String> getJoinToken() {
        try {
            String token = k3sService.getJoinToken();
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            logger.error("Failed to get join token: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get join token: " + e.getMessage());
        }
    }

    @GetMapping("/join-cluster")
    public ResponseEntity<String> getJoinCluster(@RequestParam String nodeIp) {
        try {
            String token = k3sService.joinCluster(nodeIp);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            logger.error("Failed to get join token: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get join token: " + e.getMessage());
        }
    }

    @PutMapping("/node")
    public ResponseEntity<?> createNode(@RequestBody CreateNodeRequest request) {
        logger.info("Recieved node request: {}", request.toString());
        logger.info("Recieved node request memory: {}", request.getMemory().toString());
        logger.info("Recieved node request vcpu: {}", request.getVcpu().toString());

        try {
            vmService.createVm(request);
            k3sService.joinCluster(request.getIpAddress());
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            logger.error("Failed to get join token: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get join token: " + e.getMessage());
        }
    }

    @DeleteMapping("/node")
    public ResponseEntity<?> deleteNode(@RequestParam String nodeName) {
        try {
            k3sService.deleteNode(nodeName);
            vmService.deleteVm(nodeName);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            logger.error("Failed to get join token: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get join token: " + e.getMessage());
        }
    }
}