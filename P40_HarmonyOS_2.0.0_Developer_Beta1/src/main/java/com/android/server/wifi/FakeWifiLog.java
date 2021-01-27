package com.android.server.wifi;

import com.android.server.wifi.WifiLog;

public class FakeWifiLog implements WifiLog {
    private static final DummyLogMessage sDummyLogMessage = new DummyLogMessage();

    @Override // com.android.server.wifi.WifiLog
    public WifiLog.LogMessage err(String format) {
        return sDummyLogMessage;
    }

    @Override // com.android.server.wifi.WifiLog
    public WifiLog.LogMessage warn(String format) {
        return sDummyLogMessage;
    }

    @Override // com.android.server.wifi.WifiLog
    public WifiLog.LogMessage info(String format) {
        return sDummyLogMessage;
    }

    @Override // com.android.server.wifi.WifiLog
    public WifiLog.LogMessage trace(String format) {
        return sDummyLogMessage;
    }

    @Override // com.android.server.wifi.WifiLog
    public WifiLog.LogMessage trace(String format, int numFramesToIgnore) {
        return sDummyLogMessage;
    }

    @Override // com.android.server.wifi.WifiLog
    public WifiLog.LogMessage dump(String format) {
        return sDummyLogMessage;
    }

    @Override // com.android.server.wifi.WifiLog
    public void eC(String msg) {
    }

    @Override // com.android.server.wifi.WifiLog
    public void wC(String msg) {
    }

    @Override // com.android.server.wifi.WifiLog
    public void iC(String msg) {
    }

    @Override // com.android.server.wifi.WifiLog
    public void tC(String msg) {
    }

    @Override // com.android.server.wifi.WifiLog
    public void e(String msg) {
    }

    @Override // com.android.server.wifi.WifiLog
    public void w(String msg) {
    }

    @Override // com.android.server.wifi.WifiLog
    public void i(String msg) {
    }

    @Override // com.android.server.wifi.WifiLog
    public void d(String msg) {
    }

    @Override // com.android.server.wifi.WifiLog
    public void v(String msg) {
    }
}
