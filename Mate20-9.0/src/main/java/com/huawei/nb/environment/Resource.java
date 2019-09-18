package com.huawei.nb.environment;

public interface Resource<I> {
    I getResourceByType(ResourceType resourceType);
}
