package com.huawei.odmf.user.api;

public interface IListener {
    void onObjectsChanged(ObjectContext objectContext, AllChangeToTarget allChangeToTarget);
}
