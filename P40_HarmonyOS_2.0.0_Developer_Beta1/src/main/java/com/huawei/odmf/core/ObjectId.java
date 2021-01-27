package com.huawei.odmf.core;

public interface ObjectId {
    String getEntityName();

    Object getId();

    String getUriString();

    void setEntityName(String str);

    void setId(Object obj);

    void setUriString(String str);
}
