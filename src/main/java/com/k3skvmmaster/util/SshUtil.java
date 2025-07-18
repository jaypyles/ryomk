package com.k3skvmmaster.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.k3skvmmaster.config.LibvirtConfig;

@Component
public class SshUtil {

    private final LibvirtConfig libvirtConfig;
    private static final Logger logger = LoggerFactory.getLogger(SshUtil.class);

    public SshUtil(LibvirtConfig libvirtConfig) {
        this.libvirtConfig = libvirtConfig;
    }

    public void writeRemoteFile(String content, String remotePath) throws IOException {
        try (AutoCloseSshSession ssh = new AutoCloseSshSession()) {

            ChannelSftp channel = ssh.getChannel();

            // Create directory if it doesn't exist
            createDirectoryIfNotExists(channel, remotePath.substring(0, remotePath.lastIndexOf('/')));

            // Write file
            try (InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
                channel.put(inputStream, remotePath);
            }

            // Set permissions to 644
            channel.chmod(0644, remotePath);

            logger.info("Successfully wrote file to remote path: {}", remotePath);

        } catch (JSchException | SftpException e) {
            logger.error("Failed to write remote file: {}", e.getMessage(), e);
            throw new IOException("Failed to write remote file", e);
        }
    }

    private void createDirectoryIfNotExists(ChannelSftp channelSftp, String directory) throws SftpException {
        try {
            channelSftp.ls(directory);
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                // Directory doesn't exist, create it
                String[] dirs = directory.split("/");
                String currentPath = "";

                for (String dir : dirs) {
                    if (!dir.isEmpty()) {
                        currentPath += "/" + dir;
                        try {
                            channelSftp.ls(currentPath);
                        } catch (SftpException ex) {
                            if (ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                                channelSftp.mkdir(currentPath);
                                logger.info("Created directory: {}", currentPath);
                            }
                        }
                    }
                }
            } else {
                throw e;
            }
        }
    }

    public String prepareDiskImage(String vmName, String hostname, String rootPassword, String sshKeyContent)
            throws IOException {

        try (AutoCloseSshSession ssh = new AutoCloseSshSession()) {
            ChannelSftp channel = ssh.getChannel();

            String targetImagePath = libvirtConfig.getVmImagesDirectory() + "/" + vmName + "-disk.qcow2";

            // Check if target image already exists
            try {
                channel.ls(targetImagePath);
                logger.info("Disk image already exists at: {}", targetImagePath);
                return targetImagePath;
            } catch (SftpException e) {
                if (e.id != ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    throw new IOException("Error checking if disk image exists", e);
                }
                // Image doesn't exist, proceed with creation
            }

            // Copy base image from local container to remote host using SFTP
            logger.info("Copying base image from local {} to remote {}", libvirtConfig.getBaseImagePath(),
                    targetImagePath);
            channel.put(libvirtConfig.getBaseImagePath(), targetImagePath);
            logger.info("Successfully copied base image to remote host");

            // Write SSH key content to temporary file on remote system
            String sshKeyPath = "/tmp/" + vmName + "-ssh-key.pub";

            // IMPORTANT: use the same ssh context's channel for writing the file
            try (InputStream inputStream = new ByteArrayInputStream(sshKeyContent.getBytes(StandardCharsets.UTF_8))) {
                // Create directory if needed before writing the file:
                createDirectoryIfNotExists(channel, sshKeyPath.substring(0, sshKeyPath.lastIndexOf('/')));

                channel.put(inputStream, sshKeyPath);
                channel.chmod(0644, sshKeyPath);
            } catch (SftpException ex) {
                logger.error("Failed to write SSH key file: {}", ex.getMessage(), ex);
                throw new IOException("Failed to write SSH key file", ex);
            }

            // Customize the image with virt-customize
            String customizeCommand = String.format(
                    "virt-customize -a %s --hostname %s --root-password password:%s --ssh-inject 'root:file:%s'",
                    targetImagePath, hostname, rootPassword, sshKeyPath);

            logger.info("Customizing disk image with command: {}", customizeCommand);

            // Execute virt-customize command
            AutoCloseSshSession.SshCommandResult result = ssh.executeSshCommand(customizeCommand, "virt-customize");

            if (!result.isSuccess()) {
                throw new IOException("Failed to customize disk image, exit code: " + result.getExitCode());
            }

            logger.info("Successfully prepared disk image at: {}", targetImagePath);
            return targetImagePath;

        } catch (JSchException | SftpException e) {
            logger.error("Failed to prepare disk image: {}", e.getMessage(), e);
            throw new IOException("Failed to prepare disk image", e);
        }
    }

    public void deleteDiskImage(String vmName) throws IOException {
        try (AutoCloseSshSession ssh = new AutoCloseSshSession()) {
            ChannelSftp channel = ssh.getChannel();
            String imagePath = libvirtConfig.getVmImagesDirectory() + "/" + vmName + "-disk.qcow2";

            // Check if image exists before trying to delete
            try {
                channel.ls(imagePath);
            } catch (SftpException e) {
                if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    logger.info("Disk image does not exist at: {}", imagePath);
                    return;
                } else {
                    throw new IOException("Error checking if disk image exists", e);
                }
            }

            // Delete the image
            channel.rm(imagePath);
            logger.info("Successfully deleted disk image at: {}", imagePath);

        } catch (JSchException | SftpException e) {
            logger.error("Failed to delete disk image: {}", e.getMessage(), e);
            throw new IOException("Failed to delete disk image", e);
        }
    }

    public String createRemoteIso(String remoteDir, String isoPath, String isoName, String systemUser)
            throws IOException {

        try (AutoCloseSshSession ssh = new AutoCloseSshSession()) {
            // Create the ISO path
            String vmImageIsoPath = String.format(isoPath, systemUser, isoName);

            // Create ISO using remote command
            String command = String.format(
                    "cd %s && genisoimage -output %s -volid cidata -joliet -rock .",
                    remoteDir,
                    vmImageIsoPath);

            // Execute command
            AutoCloseSshSession.SshCommandResult result = ssh.executeSshCommand(command, "ISO creation");

            if (!result.isSuccess()) {
                throw new IOException("Failed to create ISO, exit code: " + result.getExitCode());
            }

            logger.info("Successfully created ISO at: {}", vmImageIsoPath);
            return vmImageIsoPath;

        } catch (JSchException e) {
            logger.error("Failed to create remote ISO: {}", e.getMessage(), e);
            throw new IOException("Failed to create remote ISO", e);
        }
    }
}