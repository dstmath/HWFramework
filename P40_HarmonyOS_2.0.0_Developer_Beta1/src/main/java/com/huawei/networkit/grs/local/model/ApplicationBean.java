package com.huawei.networkit.grs.local.model;

import java.util.HashMap;
import java.util.Map;

public class ApplicationBean {
    private long cacheControl;
    private Map<String, Service> customServices = new HashMap(16);
    private String name;
    private Map<String, Service> services = new HashMap(16);

    public String getName() {
        return this.name;
    }

    public void setName(String name2) {
        this.name = name2;
    }

    public void addService(String serviceName, Service service) {
        this.services.put(serviceName, service);
    }

    public Service getService(String serviceName) {
        return this.services.get(serviceName);
    }

    public void addCustomServices(String serviceName, Service service) {
        this.customServices.put(serviceName, service);
    }

    public Service getCustomServices(String serviceName) {
        return this.customServices.get(serviceName);
    }

    public long getCacheControl() {
        return this.cacheControl;
    }

    public void setCacheControl(long cacheControl2) {
        this.cacheControl = cacheControl2;
    }
}
