package com.k3skvmmaster.service;

import java.util.List;

import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.k3skvmmaster.model.VmRequest;
import com.k3skvmmaster.model.VmResponse;
import com.k3skvmmaster.util.CloudInitUtil;
import com.k3skvmmaster.util.LibvirtUtil;
import com.k3skvmmaster.util.SshUtil;

@Service
public class VmService {

    private static final Logger logger = LoggerFactory.getLogger(VmService.class);

    @Autowired
    private LibvirtUtil libvirtUtil;

    @Autowired
    private CloudInitUtil cloudInitUtil;

    @Autowired
    private SshUtil sshUtil;

    public VmResponse createVm(VmRequest request) throws Exception {
        // 1. Prepare disk image (copy base image and customize it)
        logger.info("Preparing disk image for VM: {}", request.getName());
        sshUtil.prepareDiskImage(request.getName(), request.getName(), request.getRootPassword());

        // 2. Generate cloud-init files on remote system
        String cloudInitIsoPath = cloudInitUtil.createRemoteCloudInitIso(request);

        // 3. Create VM domain XML
        String domainXml = libvirtUtil.generateDomainXml(request, cloudInitIsoPath);

        // 4. Define and start VM
        Domain domain = libvirtUtil.defineAndStartDomain(domainXml);

        // 5. Wait for VM to be ready
        libvirtUtil.waitForVmReady(request.getIpAddress(), 22, 300);

        return buildVmResponse(domain);
    }

    public List<VmResponse> getAllVms() {
        try {
            return libvirtUtil.getAllDomains().stream()
                    .map(this::buildVmResponse)
                    .toList();
        } catch (LibvirtException e) {
            throw new RuntimeException(e);
        }
    }

    public VmResponse getVm(String vmName) {
        try {
            Domain domain = libvirtUtil.getDomainByName(vmName);
            return buildVmResponse(domain);
        } catch (LibvirtException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteVm(String vmName) {
        try {
            // Delete the VM domain
            libvirtUtil.deleteDomain(vmName);

            // Clean up the disk image
            logger.info("Cleaning up disk image for VM: {}", vmName);
            sshUtil.deleteDiskImage(vmName);

        } catch (Exception e) {
            logger.error("Error deleting VM {}: {}", vmName, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void startVm(String vmName) {

        try {
            libvirtUtil.startDomain(vmName);
        } catch (LibvirtException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopVm(String vmName) {
        try {
            libvirtUtil.shutdownDomain(vmName);
        } catch (LibvirtException e) {
            throw new RuntimeException(e);
        }
    }

    private VmResponse buildVmResponse(Domain domain) {
        try {
            VmResponse response = new VmResponse();
            response.setName(domain.getName());
            response.setStatus(domain.isActive() == 1 ? "running" : "stopped");
            response.setVcpu(domain.getMaxVcpus());
            response.setMemory((int) domain.getMaxMemory());
            return response;
        } catch (LibvirtException e) {
            throw new RuntimeException(e);
        }
    }
}