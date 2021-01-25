package com.huawei.nb.coordinator.helper;

public interface DataRequestListener {
    void onFailure(RequestResult requestResult);

    void onSuccess(String str);
}
