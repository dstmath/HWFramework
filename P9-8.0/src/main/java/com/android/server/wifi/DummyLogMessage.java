package com.android.server.wifi;

import com.android.server.wifi.WifiLog.LogMessage;

public class DummyLogMessage implements LogMessage {
    public LogMessage r(String value) {
        return this;
    }

    public LogMessage c(String value) {
        return this;
    }

    public LogMessage c(long value) {
        return this;
    }

    public LogMessage c(char value) {
        return this;
    }

    public LogMessage c(boolean value) {
        return this;
    }

    public void flush() {
    }
}
