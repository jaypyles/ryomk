package com.k3skvmmaster.model.dto;

import com.k3skvmmaster.model.VmRequest;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class CreateNodeRequest extends VmRequest {
    private String k3sRole;
}
