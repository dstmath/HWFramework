package com.huawei.ace.plugin;

public interface EventNotifier {
    void endOfNotify();

    void error(int i, Object obj);

    void setAbilityName(String str);

    void success(int i, Object obj);

    void success(Object obj);
}
