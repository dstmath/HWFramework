package com.android.server.wifi;

import java.io.FileDescriptor;
import java.io.PrintWriter;

public class BaseWifiDiagnostics {
    public static final byte CONNECTION_EVENT_FAILED = 2;
    public static final byte CONNECTION_EVENT_STARTED = 0;
    public static final byte CONNECTION_EVENT_SUCCEEDED = 1;
    public static final byte CONNECTION_EVENT_TIMEOUT = 3;
    protected String mDriverVersion;
    protected String mFirmwareVersion;
    protected int mSupportedFeatureSet;
    protected final WifiNative mWifiNative;

    public BaseWifiDiagnostics(WifiNative wifiNative) {
        this.mWifiNative = wifiNative;
    }

    public synchronized void startLogging(boolean verboseEnabled) {
        this.mFirmwareVersion = this.mWifiNative.getFirmwareVersion();
        this.mDriverVersion = this.mWifiNative.getDriverVersion();
        this.mSupportedFeatureSet = this.mWifiNative.getSupportedLoggerFeatureSet();
    }

    public synchronized void startPacketLog() {
    }

    public synchronized void stopPacketLog() {
    }

    public synchronized void stopLogging() {
    }

    public synchronized void reportConnectionEvent(byte event) {
    }

    public synchronized void captureBugReportData(int reason) {
    }

    public synchronized void captureAlertData(int errorCode, byte[] alertData) {
    }

    public synchronized void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        dump(pw);
        pw.println("*** logging disabled, no debug data ****");
    }

    public void takeBugReport(String bugTitle, String bugDetail) {
    }

    /* access modifiers changed from: protected */
    public synchronized void dump(PrintWriter pw) {
        pw.println("Chipset information :-----------------------------------------------");
        pw.println("FW Version is: " + this.mFirmwareVersion);
        pw.println("Driver Version is: " + this.mDriverVersion);
        pw.println("Supported Feature set: " + this.mSupportedFeatureSet);
    }
}
