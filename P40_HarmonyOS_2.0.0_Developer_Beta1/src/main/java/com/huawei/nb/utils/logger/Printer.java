package com.huawei.nb.utils.logger;

public interface Printer {
    void d(String str, Object... objArr);

    void e(String str, Object... objArr);

    void e(Throwable th, String str, Object... objArr);

    void i(String str, Object... objArr);

    Settings init(String str);

    void v(String str, Object... objArr);

    void w(String str, Object... objArr);
}
