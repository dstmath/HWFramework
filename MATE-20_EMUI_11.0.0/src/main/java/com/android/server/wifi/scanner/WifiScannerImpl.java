package com.android.server.wifi.scanner;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiScanner;
import android.os.Looper;
import android.text.TextUtils;
import android.util.LocalLog;
import com.android.server.wifi.Clock;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiMonitor;
import com.android.server.wifi.WifiNative;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Comparator;

public abstract class WifiScannerImpl {
    public static final WifiScannerImplFactory DEFAULT_FACTORY = new WifiScannerImplFactory() {
        /* class com.android.server.wifi.scanner.WifiScannerImpl.AnonymousClass1 */

        @Override // com.android.server.wifi.scanner.WifiScannerImpl.WifiScannerImplFactory
        public WifiScannerImpl create(Context context, Looper looper, Clock clock) {
            WifiNative wifiNative = WifiInjector.getInstance().getWifiNative();
            WifiMonitor wifiMonitor = WifiInjector.getInstance().getWifiMonitor();
            String ifaceName = wifiNative.getClientInterfaceName();
            if (TextUtils.isEmpty(ifaceName)) {
                return null;
            }
            if (wifiNative.getBgScanCapabilities(ifaceName, new WifiNative.ScanCapabilities())) {
                return new HalWifiScannerImpl(context, ifaceName, wifiNative, wifiMonitor, looper, clock);
            }
            return new WificondScannerImpl(context, ifaceName, wifiNative, wifiMonitor, new WificondChannelHelper(wifiNative), looper, clock);
        }
    };
    protected static final Comparator<ScanResult> SCAN_RESULT_SORT_COMPARATOR = new Comparator<ScanResult>() {
        /* class com.android.server.wifi.scanner.WifiScannerImpl.AnonymousClass2 */

        public int compare(ScanResult r1, ScanResult r2) {
            return r2.level - r1.level;
        }
    };

    public interface WifiScannerImplFactory {
        WifiScannerImpl create(Context context, Looper looper, Clock clock);
    }

    public abstract void cleanup();

    /* access modifiers changed from: protected */
    public abstract void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr);

    public abstract ChannelHelper getChannelHelper();

    public abstract WifiScanner.ScanData[] getLatestBatchedScanResults(boolean z);

    public abstract WifiScanner.ScanData getLatestSingleScanResults();

    public abstract boolean getScanCapabilities(WifiNative.ScanCapabilities scanCapabilities);

    public abstract boolean isHwPnoSupported(boolean z);

    public abstract void logWifiScan(String str);

    public abstract void pauseBatchedScan();

    public abstract boolean resetHwPnoList();

    public abstract void restartBatchedScan();

    public abstract boolean setHwPnoList(WifiNative.PnoSettings pnoSettings, WifiNative.PnoEventHandler pnoEventHandler);

    public abstract void setWifiScanLogger(LocalLog localLog);

    public abstract boolean startBatchedScan(WifiNative.ScanSettings scanSettings, WifiNative.ScanEventHandler scanEventHandler);

    public abstract boolean startSingleScan(WifiNative.ScanSettings scanSettings, WifiNative.ScanEventHandler scanEventHandler);

    public abstract void stopBatchedScan();
}
