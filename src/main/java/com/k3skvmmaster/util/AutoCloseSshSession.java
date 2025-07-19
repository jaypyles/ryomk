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

    private final String baseImagePath = "/k3s-base.qcow2";
    private final String vmImagesDirectory = "/var/lib/libvirt/images";
    private final String privateKeyPath = "/root/.ssh/id_rsa";

    private final Session session;
    private final ChannelSftp channel;

    private static final String DEFAULT_USERNAME = "jayden";
    private static final String DEFAULT_HOST = "192.168.50.201";

    public AutoCloseSshSession() throws JSchException {
        this(DEFAULT_USERNAME, DEFAULT_HOST, false);
    }

    public AutoCloseSshSession(String username, String host) throws JSchException {
        this(username, host, false);
    }

    public AutoCloseSshSession(String username, String host, Boolean usePassword) throws JSchException {
        logger.info("Trying to connect to server: {}", host);

        if (username == null || username.isEmpty()) {
            username = DEFAULT_USERNAME;
        }

        if (host == null || host.isEmpty()) {
            host = DEFAULT_HOST;
        }

        JSch jsch = new JSch();
        jsch.addIdentity(privateKeyPath);

        try {
            session = jsch.getSession(username.trim(), host.trim(), 22);
            session.setConfig("StrictHostKeyChecking", "no");

            if (usePassword) {
                session.setConfig("PreferredAuthentications", "password");
                session.setPassword("ubuntu");
            }

            session.setTimeout(30000);
            session.connect();

            if (!session.isConnected()) {
                throw new JSchException("SSH session failed to connect to " + host);
            }

            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();

            if (!channel.isConnected()) {
                throw new JSchException("SFTP channel failed to connect on " + host);
            }

            logger.info("SSH session and SFTP channel established with {}@{}", username, host);
        } catch (Exception e) {
            logger.error("Failed to establish SSH session: {}", e.getMessage(), e);
            throw new JSchException("SSH session setup failed for " + username + "@" + host, e);
        }
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
