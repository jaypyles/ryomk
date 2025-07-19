package com.k3skvmmaster.util.mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.kubernetes.client.openapi.models.V1Node;
import io.kubernetes.client.openapi.models.V1NodeList;

@Component
public class KubernetesDataMapper {

    public Map<String, Map<String, String>> mapNodesToRto(V1NodeList nodes) {
        List<V1Node> nodeList = nodes.getItems();

        Map<String, Map<String, String>> nodeMap = nodeList.stream()
                .collect(Collectors.toMap(
                        node -> node.getMetadata().getName(),
                        node -> {
                            Map<String, String> properties = new HashMap<>();
                            properties.put("status", node.getStatus().getConditions().stream()
                                    .filter(cond -> "Ready".equals(cond.getType()))
                                    .findFirst()
                                    .map(cond -> cond.getStatus())
                                    .orElse("Unknown"));

                            properties.put("ip", node.getStatus().getAddresses().stream()
                                    .filter(addr -> "InternalIP".equals(addr.getType()))
                                    .findFirst()
                                    .map(addr -> addr.getAddress())
                                    .orElse("Unknown"));

                            properties.put("osImage", node.getStatus().getNodeInfo().getOsImage());
                            properties.put("kubeletVersion", node.getStatus().getNodeInfo().getKubeletVersion());
                            return properties;
                        }));

        return nodeMap;
    }
}
