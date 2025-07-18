package com.k3skvmmaster.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.k3skvmmaster.model.VmRequest;

@Component
public class CloudInitUtil {

    @Autowired
    private SshUtil sshUtil;

    public String generateUserData(VmRequest request) {
        return String.format("""
                #cloud-config
                users:
                  - name: %s
                    sudo: ['ALL=(ALL) NOPASSWD:ALL']
                    groups: users
                    shell: /bin/bash
                    lock_passwd: false
                    passwd: "%s"

                    ssh_authorized_keys:
                      - %s
                """,
                request.getUser(),
                request.getRootPassword(),
                request.getSshKey() != null ? request.getSshKey() : "");
    }

    public String generateMetaData(VmRequest request) {
        return String.format("""
                instance-id: %s
                local-hostname: %s
                """,
                request.getName(),
                request.getName());
    }

    public String generateNetworkConfig(VmRequest request) {
        return String.format("""
                version: 2
                ethernets:
                  enp1s0:
                    dhcp4: false
                    addresses:
                      - %s/24
                    gateway4: %s
                    nameservers:
                      addresses:
                        - 8.8.8.8
                        - 8.8.4.4
                """,
                request.getIpAddress(),
                request.getGateway());
    }

    public String createCloudInitIso(VmRequest request) throws IOException {
        String tempDir = Files.createTempDirectory("cloud-init-" + request.getName()).toString();

        // Create cloud-init files
        Files.write(Path.of(tempDir, "user-data"), generateUserData(request).getBytes());
        Files.write(Path.of(tempDir, "meta-data"), generateMetaData(request).getBytes());
        Files.write(Path.of(tempDir, "network-config"), generateNetworkConfig(request).getBytes());

        // Create ISO
        String isoPath = "/tmp/" + request.getName() + "-cloud-init.iso";
        createIso(tempDir, isoPath);

        // Cleanup temp directory
        FileUtils.deleteDirectory(new File(tempDir));

        return isoPath;
    }

    private void createIso(String sourceDir, String isoPath) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(
                "genisoimage", "-output", isoPath,
                "-volid", "cidata", "-joliet", "-rock",
                sourceDir);
        Process process = pb.start();
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Failed to create ISO, exit code: " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while creating ISO", e);
        }
    }

    public String createRemoteCloudInitIso(VmRequest request) throws IOException {
        String remoteDir = String.format("/home/%s/cloud-init/cidata", request.getSystemUser());

        // Write cloud-init files to remote system
        sshUtil.writeRemoteFile(generateUserData(request), remoteDir + "/user-data");
        sshUtil.writeRemoteFile(generateMetaData(request), remoteDir + "/meta-data");
        sshUtil.writeRemoteFile(generateNetworkConfig(request), remoteDir + "/network-config");

        // Create ISO on remote system
        String isoPath = sshUtil.createRemoteIso(
                remoteDir,
                request.getIsoPath(),
                request.getName(),
                request.getSystemUser());

        return isoPath;
    }
}