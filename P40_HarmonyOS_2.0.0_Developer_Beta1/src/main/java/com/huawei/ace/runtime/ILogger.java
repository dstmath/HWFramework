package com.huawei.ace.runtime;

public interface ILogger {
    void d(String str, String str2);

    void e(String str, String str2);

    void i(String str, String str2);

    boolean isDebuggable();

    void w(String str, String str2);
}
