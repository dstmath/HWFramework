package com.android.server.wifi;

import javax.annotation.CheckReturnValue;

public interface WifiLog {
    public static final char PLACEHOLDER = '%';

    public interface LogMessage {
        @CheckReturnValue
        LogMessage c(char c);

        @CheckReturnValue
        LogMessage c(long j);

        @CheckReturnValue
        LogMessage c(String str);

        @CheckReturnValue
        LogMessage c(boolean z);

        void flush();

        @CheckReturnValue
        LogMessage r(String str);
    }

    void d(String str);

    @CheckReturnValue
    LogMessage dump(String str);

    void e(String str);

    void eC(String str);

    @CheckReturnValue
    LogMessage err(String str);

    void i(String str);

    void iC(String str);

    @CheckReturnValue
    LogMessage info(String str);

    void tC(String str);

    @CheckReturnValue
    LogMessage trace(String str);

    void v(String str);

    void w(String str);

    void wC(String str);

    @CheckReturnValue
    LogMessage warn(String str);
}
