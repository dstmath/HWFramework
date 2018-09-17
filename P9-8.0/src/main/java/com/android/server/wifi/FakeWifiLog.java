package com.android.server.wifi;

import com.android.server.wifi.WifiLog.LogMessage;

public class FakeWifiLog implements WifiLog {
    private static final DummyLogMessage sDummyLogMessage = new DummyLogMessage();

    public LogMessage err(String format) {
        return sDummyLogMessage;
    }

    public LogMessage warn(String format) {
        return sDummyLogMessage;
    }

    public LogMessage info(String format) {
        return sDummyLogMessage;
    }

    public LogMessage trace(String format) {
        return sDummyLogMessage;
    }

    public LogMessage dump(String format) {
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
