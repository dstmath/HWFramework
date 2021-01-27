package com.huawei.ace.runtime;

public interface AceEventCallback {
    String onEvent(int i, String str, String str2);

    void onFinish();

    void onStatusBarBgColorChanged(int i);
}
