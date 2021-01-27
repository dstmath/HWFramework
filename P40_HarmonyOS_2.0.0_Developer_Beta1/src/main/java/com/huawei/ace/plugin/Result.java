package com.huawei.ace.plugin;

public interface Result {
    void error(int i, Object obj);

    void notExistFunction();

    void success(Object obj);

    void successWithRawString(String str);
}
