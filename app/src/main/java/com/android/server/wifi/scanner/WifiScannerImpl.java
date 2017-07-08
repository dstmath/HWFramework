package com.android.server.wifi.scanner;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiScanner.HotlistSettings;
import android.net.wifi.WifiScanner.ScanData;
import android.net.wifi.WifiScanner.WifiChangeSettings;
import android.os.Looper;
import com.android.server.wifi.Clock;
import com.android.server.wifi.WifiNative.HotlistEventHandler;
import com.android.server.wifi.WifiNative.PnoEventHandler;
import com.android.server.wifi.WifiNative.PnoSettings;
import com.android.server.wifi.WifiNative.ScanCapabilities;
import com.android.server.wifi.WifiNative.ScanEventHandler;
import com.android.server.wifi.WifiNative.ScanSettings;
import com.android.server.wifi.WifiNative.SignificantWifiChangeEventHandler;
import java.util.Comparator;

public abstract class WifiScannerImpl {
    public static final WifiScannerImplFactory DEFAULT_FACTORY = null;
    protected static final Comparator<ScanResult> SCAN_RESULT_SORT_COMPARATOR = null;

    public interface WifiScannerImplFactory {
        WifiScannerImpl create(Context context, Looper looper, Clock clock);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.scanner.WifiScannerImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.scanner.WifiScannerImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.scanner.WifiScannerImpl.<clinit>():void");
    }

    public abstract void cleanup();

    public abstract ChannelHelper getChannelHelper();

    public abstract ScanData[] getLatestBatchedScanResults(boolean z);

    public abstract ScanData getLatestSingleScanResults();

    public abstract boolean getScanCapabilities(ScanCapabilities scanCapabilities);

    public abstract boolean isHwPnoSupported(boolean z);

    public abstract void pauseBatchedScan();

    public abstract void resetHotlist();

    public abstract boolean resetHwPnoList();

    public abstract void restartBatchedScan();

    public abstract boolean setHotlist(HotlistSettings hotlistSettings, HotlistEventHandler hotlistEventHandler);

    public abstract boolean setHwPnoList(PnoSettings pnoSettings, PnoEventHandler pnoEventHandler);

    public abstract boolean shouldScheduleBackgroundScanForHwPno();

    public abstract boolean startBatchedScan(ScanSettings scanSettings, ScanEventHandler scanEventHandler);

    public abstract boolean startSingleScan(ScanSettings scanSettings, ScanEventHandler scanEventHandler);

    public abstract void stopBatchedScan();

    public abstract boolean trackSignificantWifiChange(WifiChangeSettings wifiChangeSettings, SignificantWifiChangeEventHandler significantWifiChangeEventHandler);

    public abstract void untrackSignificantWifiChange();
}
