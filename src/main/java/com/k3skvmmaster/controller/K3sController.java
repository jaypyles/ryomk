package com.k3skvmmaster.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.k3skvmmaster.service.K3sService;

@RestController
@RequestMapping("/api/v1/clusters")
public class K3sController {

    private static final Logger logger = LoggerFactory.getLogger(K3sController.class);

    @Autowired
    private K3sService k3sService;

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

    // @PostMapping
    // public ResponseEntity<ClusterResponse> createCluster(@Valid @RequestBody
    // ClusterRequest request) {
    // ClusterResponse response = clusterService.createCluster(request).join();
    // return ResponseEntity.ok(response);
    // }

    // @GetMapping("/{clusterName}")
    // public ResponseEntity<ClusterResponse> getCluster(@PathVariable String
    // clusterName) {
    // ClusterResponse response = clusterService.getCluster(clusterName);
    // return ResponseEntity.ok(response);
    // }

    // @DeleteMapping("/{clusterName}")
    // public ResponseEntity<Void> deleteCluster(@PathVariable String clusterName) {
    // clusterService.deleteCluster(clusterName);
    // return ResponseEntity.noContent().build();
    // }

    // @GetMapping("/{clusterName}/kubeconfig")
    // public ResponseEntity<String> getKubeconfig(@PathVariable String clusterName)
    // {
    // String kubeconfig = clusterService.getKubeconfig(clusterName);
    // return ResponseEntity.ok(kubeconfig);
    // }
}