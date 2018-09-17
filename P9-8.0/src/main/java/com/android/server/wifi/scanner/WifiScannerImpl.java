package com.android.server.wifi.scanner;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiScanner.ScanData;
import android.os.Looper;
import android.util.LocalLog;
import com.android.server.wifi.Clock;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiMonitor;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.WifiNative.PnoEventHandler;
import com.android.server.wifi.WifiNative.PnoSettings;
import com.android.server.wifi.WifiNative.ScanCapabilities;
import com.android.server.wifi.WifiNative.ScanEventHandler;
import com.android.server.wifi.WifiNative.ScanSettings;
import java.util.Comparator;

public abstract class WifiScannerImpl {
    public static final WifiScannerImplFactory DEFAULT_FACTORY = new WifiScannerImplFactory() {
        public WifiScannerImpl create(Context context, Looper looper, Clock clock) {
            WifiNative wifiNative = WifiInjector.getInstance().getWifiNative();
            WifiMonitor wifiMonitor = WifiInjector.getInstance().getWifiMonitor();
            if (wifiNative.getBgScanCapabilities(new ScanCapabilities())) {
                return new HalWifiScannerImpl(context, wifiNative, wifiMonitor, looper, clock);
            }
            return new WificondScannerImpl(context, wifiNative, wifiMonitor, looper, clock);
        }
    };
    protected static final Comparator<ScanResult> SCAN_RESULT_SORT_COMPARATOR = new Comparator<ScanResult>() {
        public int compare(ScanResult r1, ScanResult r2) {
            return r2.level - r1.level;
        }
    };

    public interface WifiScannerImplFactory {
        WifiScannerImpl create(Context context, Looper looper, Clock clock);
    }

    public abstract void cleanup();

    public abstract ChannelHelper getChannelHelper();

    public abstract ScanData[] getLatestBatchedScanResults(boolean z);

    public abstract ScanData getLatestSingleScanResults();

    public abstract boolean getScanCapabilities(ScanCapabilities scanCapabilities);

    public abstract boolean isHwPnoSupported(boolean z);

    public abstract void logWifiScan(String str);

    public abstract void pauseBatchedScan();

    public abstract boolean resetHwPnoList();

    public abstract void restartBatchedScan();

    public abstract boolean setHwPnoList(PnoSettings pnoSettings, PnoEventHandler pnoEventHandler);

    public abstract void setWifiScanLogger(LocalLog localLog);

    public abstract boolean shouldScheduleBackgroundScanForHwPno();

    public abstract boolean startBatchedScan(ScanSettings scanSettings, ScanEventHandler scanEventHandler);

    public abstract boolean startSingleScan(ScanSettings scanSettings, ScanEventHandler scanEventHandler);

    public abstract void stopBatchedScan();
}
