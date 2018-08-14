package com.github.leleact.dubbo.dubbo.api;

import com.github.leleact.dubbo.request.CommonRequest;

public interface Api {

    String call(CommonRequest request);
}
