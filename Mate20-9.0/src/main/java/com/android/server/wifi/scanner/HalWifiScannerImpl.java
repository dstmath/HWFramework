package com.android.server.wifi.scanner;

import android.content.Context;
import android.net.wifi.WifiScanner;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.LocalLog;
import android.util.Log;
import com.android.server.wifi.Clock;
import com.android.server.wifi.WifiMonitor;
import com.android.server.wifi.WifiNative;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class HalWifiScannerImpl extends WifiScannerImpl implements Handler.Callback {
    private static final boolean DBG = false;
    private static final String TAG = "HalWifiScannerImpl";
    private final ChannelHelper mChannelHelper;
    private final String mIfaceName;
    private final WifiNative mWifiNative;
    private final WificondScannerImpl mWificondScannerDelegate;

    public HalWifiScannerImpl(Context context, String ifaceName, WifiNative wifiNative, WifiMonitor wifiMonitor, Looper looper, Clock clock) {
        this.mIfaceName = ifaceName;
        this.mWifiNative = wifiNative;
        this.mChannelHelper = new WificondChannelHelper(wifiNative);
        WificondScannerImpl wificondScannerImpl = new WificondScannerImpl(context, this.mIfaceName, wifiNative, wifiMonitor, this.mChannelHelper, looper, clock);
        this.mWificondScannerDelegate = wificondScannerImpl;
    }

    public boolean handleMessage(Message msg) {
        Log.w(TAG, "Unknown message received: " + msg.what);
        return true;
    }

    public void cleanup() {
        this.mWificondScannerDelegate.cleanup();
    }

    public boolean getScanCapabilities(WifiNative.ScanCapabilities capabilities) {
        return this.mWifiNative.getBgScanCapabilities(this.mIfaceName, capabilities);
    }

    public ChannelHelper getChannelHelper() {
        return this.mChannelHelper;
    }

    public boolean startSingleScan(WifiNative.ScanSettings settings, WifiNative.ScanEventHandler eventHandler) {
        return this.mWificondScannerDelegate.startSingleScan(settings, eventHandler);
    }

    public WifiScanner.ScanData getLatestSingleScanResults() {
        return this.mWificondScannerDelegate.getLatestSingleScanResults();
    }

    public boolean startBatchedScan(WifiNative.ScanSettings settings, WifiNative.ScanEventHandler eventHandler) {
        if (settings != null && eventHandler != null) {
            return this.mWifiNative.startBgScan(this.mIfaceName, settings, eventHandler);
        }
        Log.w(TAG, "Invalid arguments for startBatched: settings=" + settings + ",eventHandler=" + eventHandler);
        return false;
    }

    public void stopBatchedScan() {
        this.mWifiNative.stopBgScan(this.mIfaceName);
    }

    public void pauseBatchedScan() {
        this.mWifiNative.pauseBgScan(this.mIfaceName);
    }

    public void restartBatchedScan() {
        this.mWifiNative.restartBgScan(this.mIfaceName);
    }

    public WifiScanner.ScanData[] getLatestBatchedScanResults(boolean flush) {
        return this.mWifiNative.getBgScanResults(this.mIfaceName);
    }

    public boolean setHwPnoList(WifiNative.PnoSettings settings, WifiNative.PnoEventHandler eventHandler) {
        return this.mWificondScannerDelegate.setHwPnoList(settings, eventHandler);
    }

    public boolean resetHwPnoList() {
        return this.mWificondScannerDelegate.resetHwPnoList();
    }

    public boolean isHwPnoSupported(boolean isConnectedPno) {
        return this.mWificondScannerDelegate.isHwPnoSupported(isConnectedPno);
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mWificondScannerDelegate.dump(fd, pw, args);
    }

    public void setWifiScanLogger(LocalLog logger) {
        this.mWificondScannerDelegate.setWifiScanLogger(logger);
    }

    public void logWifiScan(String message) {
        this.mWificondScannerDelegate.logWifiScan(message);
    }
}
