package com.huawei.odmf.model.api;

public interface EntityId extends Attribute {
    String getGeneratorType();

    String getIdName();
}
