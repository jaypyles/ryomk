package com.k3skvmmaster.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class AutoCloseSshSession implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(AutoCloseSshSession.class);

    private final String libvirtUri = "qemu+ssh://jayden@192.168.50.201/system";
    private final String baseImagePath = "/k3s-base.qcow2";
    private final String vmImagesDirectory = "/var/lib/libvirt/images";
    private final String privateKeyPath = "/root/.ssh/id_rsa";

    private final Session session;
    private final ChannelSftp channel;

    public AutoCloseSshSession()
            throws JSchException {

        JSch jsch = new JSch();
        jsch.addIdentity(privateKeyPath);

        session = jsch.getSession(getUsernameFromUri(), getHostFromUri(), 22);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
    }

    @Override
    public void close() {
        if (channel != null && channel.isConnected()) {
            channel.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    private String getHostFromUri() {
        String[] parts = libvirtUri.split("@");
        if (parts.length > 1) {
            return parts[1].split("/")[0];
        }
        return "192.168.50.201"; // fallback
    }

    private String getUsernameFromUri() {
        String[] parts = libvirtUri.split("://");
        if (parts.length > 1) {
            String[] userHost = parts[1].split("@");
            if (userHost.length > 0) {
                return userHost[0];
            }
        }
        return "jayden"; // fallback
    }

    public Session getSession() {
        return session;
    }

    public ChannelSftp getChannel() {
        return channel;
    }

    public String getBaseImagePath() {
        return baseImagePath;
    }

    public String getVmImagesDirectory() {
        return vmImagesDirectory;
    }

    /**
     * Executes a command over SSH and returns the result with logging.
     */
    public SshCommandResult executeSshCommand(String command, String operationName) throws IOException {
        ChannelExec channelExec = null;
        try {
            logger.info("Executing {} command: {}", operationName, command);

            channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);
            InputStream in = channelExec.getInputStream();
            InputStream err = channelExec.getErrStream();
            channelExec.connect();

            while (!channelExec.isClosed()) {
                Thread.sleep(100);
            }

            byte[] buffer = new byte[1024];
            StringBuilder output = new StringBuilder();
            StringBuilder error = new StringBuilder();

            while (in.available() > 0) {
                int len = in.read(buffer, 0, buffer.length);
                if (len < 0)
                    break;
                output.append(new String(buffer, 0, len, StandardCharsets.UTF_8));
            }
            while (err.available() > 0) {
                int len = err.read(buffer, 0, buffer.length);
                if (len < 0)
                    break;
                error.append(new String(buffer, 0, len, StandardCharsets.UTF_8));
            }

            int exitCode = channelExec.getExitStatus();
            logger.info("{} command exit code: {}", operationName, exitCode);

            if (!output.isEmpty()) {
                logger.info("{} command output: {}", operationName, output);
            }
            if (!error.isEmpty()) {
                logger.error("{} command error: {}", operationName, error);
            }

            return new SshCommandResult(exitCode, output.toString(), error.toString());

        } catch (JSchException | InterruptedException e) {
            logger.error("Failed to execute {} command: {}", operationName, e.getMessage(), e);
            throw new IOException("Failed to execute " + operationName + " command", e);
        } finally {
            if (channelExec != null && channelExec.isConnected()) {
                channelExec.disconnect();
            }
        }
    }

    public static class SshCommandResult {
        private final int exitCode;
        private final String output;
        private final String error;

        public SshCommandResult(int exitCode, String output, String error) {
            this.exitCode = exitCode;
            this.output = output;
            this.error = error;
        }

        public int getExitCode() {
            return exitCode;
        }

        public String getOutput() {
            return output;
        }

        public String getError() {
            return error;
        }

        public boolean isSuccess() {
            return exitCode == 0;
        }
    }
}
