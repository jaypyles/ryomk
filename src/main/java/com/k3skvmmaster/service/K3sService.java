package com.k3skvmmaster.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.k3skvmmaster.util.K3sUtil;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config;

@Service
public class K3sService {
    @Autowired
    private K3sUtil k3sUtil;

    private CoreV1Api coreV1Api;

    public K3sService() throws IOException {
        ApiClient client = Config.defaultClient();
        io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);
        this.coreV1Api = new CoreV1Api(client);
    }

    public void deletePod(String podName) throws Exception {
        coreV1Api.deleteNamespacedPod(podName, "default").execute();
    }

    public void deleteNode(String nodeName) throws Exception {
        coreV1Api.deleteNode(nodeName).execute();
    }

    public String joinCluster(String nodeIp) throws Exception {
        return k3sUtil.joinCluster(nodeIp);
    }

    public String getJoinToken() throws Exception {
        return k3sUtil.getJoinToken();
    }
}
