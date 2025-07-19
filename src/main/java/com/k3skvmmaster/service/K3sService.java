package com.k3skvmmaster.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.k3skvmmaster.util.K3sUtil;

@Service
public class K3sService {
    @Autowired
    private K3sUtil k3sUtil;

    public String joinCluster(String nodeIp) throws Exception {
        return k3sUtil.joinCluster(nodeIp);
    }

    public String getJoinToken() throws Exception {
        return k3sUtil.getJoinToken();
    }
}
