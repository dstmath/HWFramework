package com.android.server.wifi;

import java.io.FileDescriptor;
import java.io.PrintWriter;

public class BaseWifiDiagnostics {
    public static final byte CONNECTION_EVENT_FAILED = (byte) 2;
    public static final byte CONNECTION_EVENT_STARTED = (byte) 0;
    public static final byte CONNECTION_EVENT_SUCCEEDED = (byte) 1;
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

    synchronized void reportConnectionEvent(long connectionId, byte event) {
    }

    public synchronized void captureBugReportData(int reason) {
    }

    public synchronized void captureAlertData(int errorCode, byte[] alertData) {
    }

    public synchronized void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        dump(pw);
        pw.println("*** firmware logging disabled, no debug data ****");
        pw.println("set config_wifi_enable_wifi_firmware_debugging to enable");
    }

    protected synchronized void dump(PrintWriter pw) {
        pw.println("Chipset information :-----------------------------------------------");
        pw.println("FW Version is: " + this.mFirmwareVersion);
        pw.println("Driver Version is: " + this.mDriverVersion);
        pw.println("Supported Feature set: " + this.mSupportedFeatureSet);
    }
}
