package com.github.leleact.dubbo;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.spring.AnnotationBean;
import com.alibaba.dubbo.config.spring.ServiceBean;
import com.alibaba.dubbo.config.spring.extension.SpringExtensionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ManagerService {

    private static Logger logger = LoggerFactory.getLogger(ManagerService.class);

    @SuppressWarnings("rawtypes")
    private static Collection<ServiceBean> services;

    private static Map<Class<?>, Object> interfaceMapRef = new ConcurrentHashMap<>();

    private static ManagerService instance;

    private static ApplicationConfig application;

    private ManagerService() {}

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static synchronized ManagerService getInstance() {
        if (null != instance) return instance;
        instance = new ManagerService();
        services = new HashSet<>();
        try {
            Field field = SpringExtensionFactory.class.getDeclaredField("contexts");
            field.setAccessible(true);
            Set<ApplicationContext> contexts = (Set<ApplicationContext>)field.get(new SpringExtensionFactory());
            for (ApplicationContext context : contexts){
                services.addAll(context.getBeansOfType(ServiceBean.class).values());

                Object o = context.getBean(AnnotationBean.class);
                if (o != null) {
                    Field oldVersion = AnnotationBean.class.getDeclaredField("serviceConfigs");
                    if (oldVersion != null) {
                        oldVersion.setAccessible(true);
                        Set<ServiceBean<?>> serviceConfigSet = (Set<ServiceBean<?>>) oldVersion.get(o);
                        services.addAll(serviceConfigSet);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Get All Dubbo Service Error", e);
            return instance;
        }
        for (ServiceBean<?> bean : services) {
            interfaceMapRef.putIfAbsent(bean.getInterfaceClass(), bean.getRef());
        }

        if (!services.isEmpty()) {
            ServiceBean<?> bean = services.toArray(new ServiceBean[]{})[0];
            application = bean.getApplication();
        }

        return instance;
    }

    public Map.Entry<Class<?>, Object> getRef(String interfaceClass) {
        Set<Map.Entry<Class<?>, Object>> entrySet = interfaceMapRef.entrySet();
        for (Map.Entry<Class<?>, Object> entry : entrySet) {
            if (entry.getKey().getName().equals(interfaceClass)) { return entry; }
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public Collection<ServiceBean> getServices() {
        return services;
    }

    public ApplicationConfig getApplication() {
        return application;
    }

    public Map<Class<?>, Object> getInterfaceMapRef() {
        return interfaceMapRef;
    }
}
