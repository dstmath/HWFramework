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
        this.mWificondScannerDelegate = new WificondScannerImpl(context, this.mIfaceName, wifiNative, wifiMonitor, this.mChannelHelper, looper, clock);
    }

    @Override // android.os.Handler.Callback
    public boolean handleMessage(Message msg) {
        Log.w(TAG, "Unknown message received: " + msg.what);
        return true;
    }

    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public void cleanup() {
        this.mWificondScannerDelegate.cleanup();
    }

    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public boolean getScanCapabilities(WifiNative.ScanCapabilities capabilities) {
        return this.mWifiNative.getBgScanCapabilities(this.mIfaceName, capabilities);
    }

    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public ChannelHelper getChannelHelper() {
        return this.mChannelHelper;
    }

    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public boolean startSingleScan(WifiNative.ScanSettings settings, WifiNative.ScanEventHandler eventHandler) {
        return this.mWificondScannerDelegate.startSingleScan(settings, eventHandler);
    }

    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public WifiScanner.ScanData getLatestSingleScanResults() {
        return this.mWificondScannerDelegate.getLatestSingleScanResults();
    }

    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public boolean startBatchedScan(WifiNative.ScanSettings settings, WifiNative.ScanEventHandler eventHandler) {
        if (settings != null && eventHandler != null) {
            return this.mWifiNative.startBgScan(this.mIfaceName, settings, eventHandler);
        }
        Log.w(TAG, "Invalid arguments for startBatched: settings=" + settings + ",eventHandler=" + eventHandler);
        return false;
    }

    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public void stopBatchedScan() {
        this.mWifiNative.stopBgScan(this.mIfaceName);
    }

    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public void pauseBatchedScan() {
        this.mWifiNative.pauseBgScan(this.mIfaceName);
    }

    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public void restartBatchedScan() {
        this.mWifiNative.restartBgScan(this.mIfaceName);
    }

    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public WifiScanner.ScanData[] getLatestBatchedScanResults(boolean flush) {
        return this.mWifiNative.getBgScanResults(this.mIfaceName);
    }

    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public boolean setHwPnoList(WifiNative.PnoSettings settings, WifiNative.PnoEventHandler eventHandler) {
        return this.mWificondScannerDelegate.setHwPnoList(settings, eventHandler);
    }

    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public boolean resetHwPnoList() {
        return this.mWificondScannerDelegate.resetHwPnoList();
    }

    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public boolean isHwPnoSupported(boolean isConnectedPno) {
        return this.mWificondScannerDelegate.isHwPnoSupported(isConnectedPno);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mWificondScannerDelegate.dump(fd, pw, args);
    }

    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public void setWifiScanLogger(LocalLog logger) {
        this.mWificondScannerDelegate.setWifiScanLogger(logger);
    }

    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public void logWifiScan(String message) {
        this.mWificondScannerDelegate.logWifiScan(message);
    }
}
