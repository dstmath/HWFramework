package com.android.server.wifi;

import com.android.server.wifi.WifiLog;

public class DummyLogMessage implements WifiLog.LogMessage {
    @Override // com.android.server.wifi.WifiLog.LogMessage
    public WifiLog.LogMessage r(String value) {
        return this;
    }

    @Override // com.android.server.wifi.WifiLog.LogMessage
    public WifiLog.LogMessage c(String value) {
        return this;
    }

    @Override // com.android.server.wifi.WifiLog.LogMessage
    public WifiLog.LogMessage c(long value) {
        return this;
    }

    @Override // com.android.server.wifi.WifiLog.LogMessage
    public WifiLog.LogMessage c(char value) {
        return this;
    }

    @Override // com.android.server.wifi.WifiLog.LogMessage
    public WifiLog.LogMessage c(boolean value) {
        return this;
    }

    @Override // com.android.server.wifi.WifiLog.LogMessage
    public void flush() {
    }
}
