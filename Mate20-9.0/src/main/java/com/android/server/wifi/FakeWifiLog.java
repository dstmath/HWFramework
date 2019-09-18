package com.android.server.wifi;

import com.android.server.wifi.WifiLog;

public class FakeWifiLog implements WifiLog {
    private static final DummyLogMessage sDummyLogMessage = new DummyLogMessage();

    public WifiLog.LogMessage err(String format) {
        return sDummyLogMessage;
    }

    public WifiLog.LogMessage warn(String format) {
        return sDummyLogMessage;
    }

    public WifiLog.LogMessage info(String format) {
        return sDummyLogMessage;
    }

    public WifiLog.LogMessage trace(String format) {
        return sDummyLogMessage;
    }

    public WifiLog.LogMessage trace(String format, int numFramesToIgnore) {
        return sDummyLogMessage;
    }

    public WifiLog.LogMessage dump(String format) {
        return sDummyLogMessage;
    }

    public void eC(String msg) {
    }

    public void wC(String msg) {
    }

    public void iC(String msg) {
    }

    public void tC(String msg) {
    }

    public void e(String msg) {
    }

    public void w(String msg) {
    }

    public void i(String msg) {
    }

    public void d(String msg) {
    }

    public void v(String msg) {
    }
}
