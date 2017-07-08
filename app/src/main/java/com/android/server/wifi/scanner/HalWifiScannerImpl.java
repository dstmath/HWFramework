package com.android.server.wifi.scanner;

import android.content.Context;
import android.net.wifi.WifiScanner.HotlistSettings;
import android.net.wifi.WifiScanner.ScanData;
import android.net.wifi.WifiScanner.WifiChangeSettings;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.android.server.wifi.Clock;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.WifiNative.HotlistEventHandler;
import com.android.server.wifi.WifiNative.PnoEventHandler;
import com.android.server.wifi.WifiNative.PnoSettings;
import com.android.server.wifi.WifiNative.ScanCapabilities;
import com.android.server.wifi.WifiNative.ScanEventHandler;
import com.android.server.wifi.WifiNative.ScanSettings;
import com.android.server.wifi.WifiNative.SignificantWifiChangeEventHandler;

public class HalWifiScannerImpl extends WifiScannerImpl implements Callback {
    private static final boolean DBG = false;
    private static final String TAG = "HalWifiScannerImpl";
    private final ChannelHelper mChannelHelper;
    private final boolean mHalBasedPnoSupported;
    private final SupplicantWifiScannerImpl mSupplicantScannerDelegate;
    private final WifiNative mWifiNative;

    public HalWifiScannerImpl(Context context, WifiNative wifiNative, Looper looper, Clock clock) {
        this.mWifiNative = wifiNative;
        this.mChannelHelper = new HalChannelHelper(wifiNative);
        this.mSupplicantScannerDelegate = new SupplicantWifiScannerImpl(context, wifiNative, this.mChannelHelper, looper, clock);
        this.mHalBasedPnoSupported = DBG;
    }

    public boolean handleMessage(Message msg) {
        Log.w(TAG, "Unknown message received: " + msg.what);
        return true;
    }

    public void cleanup() {
        this.mSupplicantScannerDelegate.cleanup();
    }

    public boolean getScanCapabilities(ScanCapabilities capabilities) {
        return this.mWifiNative.getScanCapabilities(capabilities);
    }

    public ChannelHelper getChannelHelper() {
        return this.mChannelHelper;
    }

    public boolean startSingleScan(ScanSettings settings, ScanEventHandler eventHandler) {
        return this.mSupplicantScannerDelegate.startSingleScan(settings, eventHandler);
    }

    public ScanData getLatestSingleScanResults() {
        return this.mSupplicantScannerDelegate.getLatestSingleScanResults();
    }

    public boolean startBatchedScan(ScanSettings settings, ScanEventHandler eventHandler) {
        if (settings != null && eventHandler != null) {
            return this.mWifiNative.startScan(settings, eventHandler);
        }
        Log.w(TAG, "Invalid arguments for startBatched: settings=" + settings + ",eventHandler=" + eventHandler);
        return DBG;
    }

    public void stopBatchedScan() {
        this.mWifiNative.stopScan();
    }

    public void pauseBatchedScan() {
        this.mWifiNative.pauseScan();
    }

    public void restartBatchedScan() {
        this.mWifiNative.restartScan();
    }

    public ScanData[] getLatestBatchedScanResults(boolean flush) {
        return this.mWifiNative.getScanResults(flush);
    }

    public boolean setHwPnoList(PnoSettings settings, PnoEventHandler eventHandler) {
        if (this.mHalBasedPnoSupported) {
            return this.mWifiNative.setPnoList(settings, eventHandler);
        }
        return this.mSupplicantScannerDelegate.setHwPnoList(settings, eventHandler);
    }

    public boolean resetHwPnoList() {
        if (this.mHalBasedPnoSupported) {
            return this.mWifiNative.resetPnoList();
        }
        return this.mSupplicantScannerDelegate.resetHwPnoList();
    }

    public boolean isHwPnoSupported(boolean isConnectedPno) {
        if (this.mHalBasedPnoSupported) {
            return true;
        }
        return this.mSupplicantScannerDelegate.isHwPnoSupported(isConnectedPno);
    }

    public boolean shouldScheduleBackgroundScanForHwPno() {
        if (this.mHalBasedPnoSupported) {
            return true;
        }
        return this.mSupplicantScannerDelegate.shouldScheduleBackgroundScanForHwPno();
    }

    public boolean setHotlist(HotlistSettings settings, HotlistEventHandler eventHandler) {
        return this.mWifiNative.setHotlist(settings, eventHandler);
    }

    public void resetHotlist() {
        this.mWifiNative.resetHotlist();
    }

    public boolean trackSignificantWifiChange(WifiChangeSettings settings, SignificantWifiChangeEventHandler handler) {
        return this.mWifiNative.trackSignificantWifiChange(settings, handler);
    }

    public void untrackSignificantWifiChange() {
        this.mWifiNative.untrackSignificantWifiChange();
    }
}
