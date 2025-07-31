package com.k3skvmmaster.model.dto;

import com.k3skvmmaster.model.common.CommonRequest;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class VmRequest extends CommonRequest {
    @NotBlank
    private String name;

    @Min(1)
    private Integer vcpu = 1;

    @Min(512)
    private Integer memory = 2048;

    @NotBlank
    private String ipAddress;

    @NotBlank
    private String gateway;

    @NotBlank
    private String systemUser;

    @NotBlank
    private String isoPath;

    private String rootPassword = "ubuntu";
    private String user = "ubuntu";

    private String libvirtUri;
    private Boolean installNfsDeps = true;
}