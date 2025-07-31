package com.k3skvmmaster.controller;

import java.util.List;

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

import com.k3skvmmaster.model.common.CommonResponse;
import com.k3skvmmaster.model.dto.CreateNodeRequest;
import com.k3skvmmaster.service.K3sService;
import com.k3skvmmaster.service.VmService;
import com.k3skvmmaster.util.mapper.KubernetesDataMapper;

import io.kubernetes.client.openapi.models.V1Node;
import io.kubernetes.client.openapi.models.V1NodeList;

@RestController
@RequestMapping("/api/v1/clusters")
public class K3sController {

    private static final Logger logger = LoggerFactory.getLogger(K3sController.class);

    @Autowired
    private K3sService k3sService;

    @Autowired
    private VmService vmService;

    @Autowired
    private KubernetesDataMapper kMapper;

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

    @GetMapping("/nodes")
    public ResponseEntity<?> getNodes() {
        try {
            V1NodeList nodes = k3sService.getNodes();
            List<V1Node> nodeList = nodes.getItems();

            String message = String.format("Successfully retrieved %d nodes", nodeList.toArray().length);

            return ResponseEntity.ok(new CommonResponse<>(message, kMapper.mapNodesToRto(nodes)));
        } catch (Exception e) {
            logger.error("Failed to join cluster: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonResponse<>("Failed to join cluster: " + e.getMessage(), false));
        }
    }

    @PutMapping("/node")
    public ResponseEntity<?> createNode(@RequestBody CreateNodeRequest request) {
        logger.info("Recieved node request: {}", request.toString());

        try {
            vmService.createVm(request);
            k3sService.joinCluster(request.getIpAddress());

            String message = String.format("%s has successfully joined cluster", request.getName());
            logger.info(message);

            return ResponseEntity.ok(new CommonResponse<>(message, true));
        } catch (Exception e) {
            logger.error("Failed to join cluster: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonResponse<>("Failed to join cluster: " + e.getMessage(), false));
        }
    }

    @DeleteMapping("/node")
    public ResponseEntity<CommonResponse<Boolean>> deleteNode(@RequestParam String nodeName,
            @RequestParam(required = false) String libvirtUri) {
        try {
            k3sService.deleteNode(nodeName);
            vmService.deleteVm(nodeName, libvirtUri);

            String message = "Node deleted successfully";
            logger.info(message);

            return ResponseEntity.ok(new CommonResponse<>(message, true));
        } catch (Exception e) {
            logger.error("Failed to delete node: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonResponse<>("Failed to delete node: " + e.getMessage(), false));
        }
    }
}