package com.android.server.wifi;

import com.google.errorprone.annotations.CompileTimeConstant;
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
    LogMessage dump(@CompileTimeConstant String str);

    void e(String str);

    void eC(@CompileTimeConstant String str);

    @CheckReturnValue
    LogMessage err(@CompileTimeConstant String str);

    void i(String str);

    void iC(@CompileTimeConstant String str);

    @CheckReturnValue
    LogMessage info(@CompileTimeConstant String str);

    void tC(@CompileTimeConstant String str);

    @CheckReturnValue
    LogMessage trace(@CompileTimeConstant String str);

    @CheckReturnValue
    LogMessage trace(String str, int i);

    void v(String str);

    void w(String str);

    void wC(@CompileTimeConstant String str);

    @CheckReturnValue
    LogMessage warn(@CompileTimeConstant String str);
}
