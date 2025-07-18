package com.k3skvmmaster.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.k3skvmmaster.model.VmRequest;
import com.k3skvmmaster.model.VmResponse;
import com.k3skvmmaster.service.VmService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/vms")
public class VmController {

    @Autowired
    private VmService vmService;

    @PostMapping
    public ResponseEntity<VmResponse> createVm(@Valid @RequestBody VmRequest request) throws Exception {
        VmResponse response = vmService.createVm(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<VmResponse>> getAllVms() {
        List<VmResponse> vms = vmService.getAllVms();
        return ResponseEntity.ok(vms);
    }

    @GetMapping("/{vmName}")
    public ResponseEntity<VmResponse> getVm(@PathVariable String vmName) {
        VmResponse vm = vmService.getVm(vmName);
        return ResponseEntity.ok(vm);
    }

    @DeleteMapping("/{vmName}")
    public ResponseEntity<Void> deleteVm(@PathVariable String vmName) {
        vmService.deleteVm(vmName);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{vmName}/start")
    public ResponseEntity<Void> startVm(@PathVariable String vmName) {
        vmService.startVm(vmName);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{vmName}/stop")
    public ResponseEntity<Void> stopVm(@PathVariable String vmName) {
        vmService.stopVm(vmName);
        return ResponseEntity.ok().build();
    }
}