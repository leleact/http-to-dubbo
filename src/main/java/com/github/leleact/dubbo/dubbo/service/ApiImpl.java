package com.github.leleact.dubbo.dubbo.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.leleact.dubbo.dubbo.api.Api;
import com.github.leleact.dubbo.request.CommonRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ApiImpl implements Api {

    private static final Logger log = LoggerFactory.getLogger(ApiImpl.class);

    @Override
    public String call(CommonRequest request) {
        log.info("receive request: {}", request);
        return "ok-ok-ok";
    }
}
