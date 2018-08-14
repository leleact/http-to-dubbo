package com.github.leleact.dubbo.controller;

import com.alibaba.dubbo.config.spring.ServiceBean;
import com.github.leleact.dubbo.ManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Map;

@RestController
public class HttpToDubboController {

    private static final Logger logger = LoggerFactory.getLogger(HttpToDubboController.class);

    @Resource
    private MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter;

    @RequestMapping(value = "/{interfaceClass}/{methodName}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> call(@PathVariable("interfaceClass") String interfaceClass,
            @PathVariable("methodName") String methodName,
            @RequestParam(value = "paramCount", defaultValue = "1") Integer count, HttpServletRequest request) throws
            IllegalAccessException,
            IOException, InvocationTargetException {

        Map.Entry<Class<?>, Object> entry = ManagerService.getInstance().getRef(interfaceClass);
        if (null == entry) {
            logger.info("No Ref Service FOUND.");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Method method = null;

        Method[] methods = entry.getKey().getMethods();
        for (Method m : methods) {
            if (m.getName().equals(methodName) && count == m.getParameterTypes().length) {
                method = m;
            }
        }

        if (method == null) {
            logger.info("No Such Method name {} FOUND.", methodName);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        String req = StreamUtils.copyToString(request.getInputStream(), Charset.forName("UTF-8"));

        Class<?>[] classes = method.getParameterTypes();
        Object[] args = new Object[classes.length];
        for (int i = 0; i < classes.length; i++) {
            args[i] = mappingJackson2HttpMessageConverter.getObjectMapper().readValue(req, classes[i]);
        }

        Object result = method.invoke(entry.getValue(), args);

        return ResponseEntity.ok(mappingJackson2HttpMessageConverter.getObjectMapper().writeValueAsString(result));
    }

    @RequestMapping("/destroy")
    public ResponseEntity<String> destroy() throws Exception {
        for (ServiceBean bean : ManagerService.getInstance().getServices()) {
            bean.destroy();
        }
        return ResponseEntity.ok("ok");
    }
}
