package com.huawei.odmf.model.api;

import java.util.Map;

public interface ObjectModel {
    String getDatabaseName();

    String getDatabaseVersion();

    int getDatabaseVersionCode();

    Map<String, ? extends Entity> getEntities();

    Entity getEntity(Class cls);

    Entity getEntity(String str);

    String getModelName();
}
