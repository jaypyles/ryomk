package com.k3skvmmaster.util;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.k3skvmmaster.config.K3sConfig;

@Component
public class K3sUtil {
    private static final Logger logger = LoggerFactory.getLogger(SshUtil.class);

    private final K3sConfig k3sConfig;

    public K3sUtil(K3sConfig k3sConfig) throws IOException {
        this.k3sConfig = k3sConfig;
    }

    public String getJoinToken() throws Exception {
        try (AutoCloseSshSession ssh = new AutoCloseSshSession("ubuntu", k3sConfig.getMasterNodeIp())) {
            String command = "sudo cat /var/lib/rancher/k3s/server/node-token";
            AutoCloseSshSession.SshCommandResult result = ssh.executeSshCommand(command, "get-join-token");

            return result.getOutput();

        } catch (IOException e) {
            logger.error("Failed to get join token: {}", e.getMessage(), e);
            throw new IOException("Failed to get join token: {}", e);

        }
    }

    public String joinCluster(String nodeIp) throws Exception {
        logger.info("Attempting to join cluster with master node at: {}, with worker node: {}",
                k3sConfig.getMasterNodeIp(), nodeIp);

        try (AutoCloseSshSession ssh = new AutoCloseSshSession("ubuntu", nodeIp, true)) {

            String token = getJoinToken();
            logger.info("Recieved token: {}", token);

            String masterIp = k3sConfig.getMasterNodeIp();

            String command = String.format(
                    "bash -c 'export K3S_URL=https://%s:6443 && export K3S_TOKEN=%s && export INSTALL_K3S_VERSION=%s && curl -sfL https://get.k3s.io | sh -'",
                    masterIp.trim(), token.trim(), k3sConfig.getMasterVersion());

            AutoCloseSshSession.SshCommandResult result = ssh.executeSshCommand(command, "join-cluster");

            return result.getOutput();

        } catch (IOException e) {
            logger.error("Failed to join cluster: {}", e.getMessage(), e);
            throw new IOException("Failed to join cluster: {}", e);

        }

    }
}
