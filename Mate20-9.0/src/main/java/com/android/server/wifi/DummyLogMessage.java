package com.android.server.wifi;

import com.android.server.wifi.WifiLog;

public class DummyLogMessage implements WifiLog.LogMessage {
    public WifiLog.LogMessage r(String value) {
        return this;
    }

    public WifiLog.LogMessage c(String value) {
        return this;
    }

    public WifiLog.LogMessage c(long value) {
        return this;
    }

    public WifiLog.LogMessage c(char value) {
        return this;
    }

    public WifiLog.LogMessage c(boolean value) {
        return this;
    }

    public void flush() {
    }
}
