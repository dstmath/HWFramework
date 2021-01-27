package com.huawei.ace.runtime;

public interface ActionEventCallback {
    void onMessageEvent(String str);

    void onRouterEvent(String str);
}
