package com.k3skvmmaster.model;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClusterRequest {
    @NotBlank
    private String clusterName;

    @NotBlank
    private String masterVmName;

    @Min(0)
    private Integer workerCount = 0;

    private List<String> workerVmNames;
    private String networkCidr = "192.168.50.0/24";
    private String gateway = "192.168.50.1";
}