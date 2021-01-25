package com.android.server.wifi;

import android.hardware.wifi.V1_0.IWifiApIface;
import android.hardware.wifi.V1_0.IWifiChip;
import android.hardware.wifi.V1_0.IWifiChipEventCallback;
import android.hardware.wifi.V1_0.IWifiIface;
import android.hardware.wifi.V1_0.IWifiStaIface;
import android.hardware.wifi.V1_0.IWifiStaIfaceEventCallback;
import android.hardware.wifi.V1_0.StaApfPacketFilterCapabilities;
import android.hardware.wifi.V1_0.StaBackgroundScanBucketParameters;
import android.hardware.wifi.V1_0.StaBackgroundScanCapabilities;
import android.hardware.wifi.V1_0.StaBackgroundScanParameters;
import android.hardware.wifi.V1_0.StaLinkLayerIfaceStats;
import android.hardware.wifi.V1_0.StaLinkLayerRadioStats;
import android.hardware.wifi.V1_0.StaLinkLayerStats;
import android.hardware.wifi.V1_0.StaRoamingCapabilities;
import android.hardware.wifi.V1_0.StaRoamingConfig;
import android.hardware.wifi.V1_0.StaScanData;
import android.hardware.wifi.V1_0.StaScanResult;
import android.hardware.wifi.V1_0.WifiDebugHostWakeReasonStats;
import android.hardware.wifi.V1_0.WifiDebugRingBufferStatus;
import android.hardware.wifi.V1_0.WifiDebugRxPacketFateReport;
import android.hardware.wifi.V1_0.WifiDebugTxPacketFateReport;
import android.hardware.wifi.V1_0.WifiInformationElement;
import android.hardware.wifi.V1_0.WifiStatus;
import android.hardware.wifi.V1_2.IWifiChipEventCallback;
import android.hardware.wifi.V1_2.IWifiStaIface;
import android.hardware.wifi.V1_3.IWifiChip;
import android.hardware.wifi.V1_3.IWifiStaIface;
import android.hardware.wifi.V1_3.WifiChannelStats;
import android.net.MacAddress;
import android.net.apf.ApfCapabilities;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiScanner;
import android.net.wifi.WifiSsid;
import android.os.Handler;
import android.os.IHwInterface;
import android.os.Looper;
import android.os.RemoteException;
import android.system.OsConstants;
import android.text.TextUtils;
import android.util.Log;
import android.util.MutableBoolean;
import android.util.MutableLong;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.HexDump;
import com.android.server.wifi.HalDeviceManager;
import com.android.server.wifi.WifiLinkLayerStats;
import com.android.server.wifi.WifiLog;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.WifiVendorHal;
import com.android.server.wifi.hwUtil.GenerateScanResult;
import com.android.server.wifi.util.BitMask;
import com.android.server.wifi.util.NativeUtil;
import com.google.errorprone.annotations.CompileTimeConstant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WifiVendorHal {
    private static final int[][] sChipFeatureCapabilityTranslation = {new int[]{67108864, 256}, new int[]{128, 512}, new int[]{256, 1024}};
    private static final long[][] sChipFeatureCapabilityTranslation13 = {new long[]{1073741824, 4096}, new long[]{4294967296L, 8192}};
    private static final Object sLock = new Object();
    private static final ApfCapabilities sNoApfCapabilities = new ApfCapabilities(0, 0, 0);
    private static final WifiLog sNoLog = new FakeWifiLog();
    @VisibleForTesting
    static final int sRssiMonCmdId = 7551;
    private static final int[][] sStaFeatureCapabilityTranslation = {new int[]{2, 128}, new int[]{4, 256}, new int[]{32, 2}, new int[]{1024, 512}, new int[]{4096, 1024}, new int[]{8192, 2048}, new int[]{65536, 4}, new int[]{524288, 8}, new int[]{1048576, 8192}, new int[]{2097152, 4096}, new int[]{8388608, 16}, new int[]{16777216, 32}, new int[]{33554432, 64}};
    private WifiNative.VendorHalDeathEventHandler mDeathEventHandler;
    private String mDriverDescription;
    private String mFirmwareDescription;
    private final HalDeviceManager mHalDeviceManager;
    private final HalDeviceManagerStatusListener mHalDeviceManagerStatusCallbacks;
    private final Handler mHalEventHandler;
    private HashMap<String, IWifiApIface> mIWifiApIfaces = new HashMap<>();
    private IWifiChip mIWifiChip;
    private final ChipEventCallback mIWifiChipEventCallback;
    private final ChipEventCallbackV12 mIWifiChipEventCallbackV12;
    private final IWifiStaIfaceEventCallback mIWifiStaIfaceEventCallback;
    private HashMap<String, IWifiStaIface> mIWifiStaIfaces = new HashMap<>();
    private int mLastScanCmdId;
    @VisibleForTesting
    boolean mLinkLayerStatsDebug = false;
    @VisibleForTesting
    WifiLog mLog = new LogcatLog("WifiVendorHal");
    private WifiNative.WifiLoggerEventHandler mLogEventHandler = null;
    private final Looper mLooper;
    private WifiNative.VendorHalRadioModeChangeEventHandler mRadioModeChangeEventHandler;
    @VisibleForTesting
    CurrentBackgroundScan mScan = null;
    @VisibleForTesting
    WifiLog mVerboseLog = sNoLog;
    private WifiNative.WifiRssiEventHandler mWifiRssiEventHandler;

    public void enableVerboseLogging(boolean verbose) {
        synchronized (sLock) {
            if (verbose) {
                this.mVerboseLog = this.mLog;
                enter("verbose=true").flush();
            } else {
                enter("verbose=false").flush();
                this.mVerboseLog = sNoLog;
            }
        }
    }

    private boolean ok(WifiStatus status) {
        if (status.code == 0) {
            return true;
        }
        this.mLog.err("% failed %").c(niceMethodName(Thread.currentThread().getStackTrace(), 3)).c(status.toString()).flush();
        return false;
    }

    private boolean boolResult(boolean result) {
        if (this.mVerboseLog == sNoLog) {
            return result;
        }
        this.mVerboseLog.err("% returns %").c(niceMethodName(Thread.currentThread().getStackTrace(), 3)).c(result).flush();
        return result;
    }

    private String stringResult(String result) {
        if (this.mVerboseLog == sNoLog) {
            return result;
        }
        this.mVerboseLog.err("% returns %").c(niceMethodName(Thread.currentThread().getStackTrace(), 3)).c(result).flush();
        return result;
    }

    private byte[] byteArrayResult(byte[] result) {
        if (this.mVerboseLog == sNoLog) {
            return result;
        }
        this.mVerboseLog.err("% returns %").c(niceMethodName(Thread.currentThread().getStackTrace(), 3)).c(result == null ? "(null)" : HexDump.dumpHexString(result)).flush();
        return result;
    }

    private WifiLog.LogMessage enter(@CompileTimeConstant String format) {
        WifiLog wifiLog = this.mVerboseLog;
        WifiLog wifiLog2 = sNoLog;
        if (wifiLog == wifiLog2) {
            return wifiLog2.info(format);
        }
        return wifiLog.trace(format, 1);
    }

    private static String niceMethodName(StackTraceElement[] trace, int start) {
        String myFile;
        if (start >= trace.length) {
            return "";
        }
        StackTraceElement s = trace[start];
        String name = s.getMethodName();
        if (name.contains("lambda$") && (myFile = s.getFileName()) != null) {
            int i = start + 1;
            while (true) {
                if (i >= trace.length) {
                    break;
                } else if (myFile.equals(trace[i].getFileName())) {
                    name = trace[i].getMethodName();
                    break;
                } else {
                    i++;
                }
            }
        }
        return name + "(l." + s.getLineNumber() + ")";
    }

    public WifiVendorHal(HalDeviceManager halDeviceManager, Looper looper) {
        this.mHalDeviceManager = halDeviceManager;
        this.mLooper = looper;
        this.mHalEventHandler = new Handler(looper);
        this.mHalDeviceManagerStatusCallbacks = new HalDeviceManagerStatusListener();
        this.mIWifiStaIfaceEventCallback = new StaIfaceEventCallback();
        this.mIWifiChipEventCallback = new ChipEventCallback();
        this.mIWifiChipEventCallbackV12 = new ChipEventCallbackV12();
    }

    private void handleRemoteException(RemoteException e) {
        this.mVerboseLog.err("% RemoteException in HIDL call %").c(niceMethodName(Thread.currentThread().getStackTrace(), 3)).c(e.toString()).flush();
        clearState();
    }

    public boolean initialize(WifiNative.VendorHalDeathEventHandler handler) {
        synchronized (sLock) {
            this.mHalDeviceManager.initialize();
            this.mHalDeviceManager.registerStatusListener(this.mHalDeviceManagerStatusCallbacks, this.mHalEventHandler);
            this.mDeathEventHandler = handler;
        }
        return true;
    }

    public void initialize() {
        synchronized (sLock) {
            this.mHalDeviceManager.initialize();
        }
    }

    public void registerHandler(WifiNative.VendorHalDeathEventHandler handler) {
        synchronized (sLock) {
            this.mHalDeviceManager.registerStatusListener(this.mHalDeviceManagerStatusCallbacks, this.mHalEventHandler);
            this.mDeathEventHandler = handler;
        }
    }

    public void reset() {
        synchronized (sLock) {
            this.mHalDeviceManager.reset();
        }
    }

    public void registerRadioModeChangeHandler(WifiNative.VendorHalRadioModeChangeEventHandler handler) {
        synchronized (sLock) {
            this.mRadioModeChangeEventHandler = handler;
        }
    }

    public boolean isVendorHalSupported() {
        boolean isSupported;
        synchronized (sLock) {
            isSupported = this.mHalDeviceManager.isSupported();
        }
        return isSupported;
    }

    public boolean startVendorHalAp() {
        synchronized (sLock) {
            if (!startVendorHal()) {
                return false;
            }
            if (!TextUtils.isEmpty(createApIface(null))) {
                return true;
            }
            stopVendorHal();
            return false;
        }
    }

    public boolean startVendorHalSta() {
        synchronized (sLock) {
            if (!startVendorHal()) {
                return false;
            }
            if (!TextUtils.isEmpty(createStaIface(false, null))) {
                return true;
            }
            stopVendorHal();
            return false;
        }
    }

    public boolean startVendorHal() {
        synchronized (sLock) {
            if (!this.mHalDeviceManager.start()) {
                this.mLog.err("Failed to start vendor HAL").flush();
                return false;
            }
            this.mLog.info("Vendor Hal started successfully").flush();
            return true;
        }
    }

    private IWifiStaIface getStaIface(String ifaceName) {
        IWifiStaIface iWifiStaIface;
        synchronized (sLock) {
            iWifiStaIface = this.mIWifiStaIfaces.get(ifaceName);
        }
        return iWifiStaIface;
    }

    /* access modifiers changed from: private */
    public class StaInterfaceDestroyedListenerInternal implements HalDeviceManager.InterfaceDestroyedListener {
        private final HalDeviceManager.InterfaceDestroyedListener mExternalListener;

        StaInterfaceDestroyedListenerInternal(HalDeviceManager.InterfaceDestroyedListener externalListener) {
            this.mExternalListener = externalListener;
        }

        @Override // com.android.server.wifi.HalDeviceManager.InterfaceDestroyedListener
        public void onDestroyed(String ifaceName) {
            synchronized (WifiVendorHal.sLock) {
                WifiVendorHal.this.mIWifiStaIfaces.remove(ifaceName);
            }
            HalDeviceManager.InterfaceDestroyedListener interfaceDestroyedListener = this.mExternalListener;
            if (interfaceDestroyedListener != null) {
                interfaceDestroyedListener.onDestroyed(ifaceName);
            }
        }
    }

    public String createStaIface(boolean lowPrioritySta, HalDeviceManager.InterfaceDestroyedListener destroyedListener) {
        synchronized (sLock) {
            IWifiStaIface iface = this.mHalDeviceManager.createStaIface(lowPrioritySta, new StaInterfaceDestroyedListenerInternal(destroyedListener), null);
            if (iface == null) {
                this.mLog.err("Failed to create STA iface").flush();
                return stringResult(null);
            }
            HalDeviceManager halDeviceManager = this.mHalDeviceManager;
            String ifaceName = HalDeviceManager.getName(iface);
            if (TextUtils.isEmpty(ifaceName)) {
                this.mLog.err("Failed to get iface name").flush();
                return stringResult(null);
            } else if (!registerStaIfaceCallback(iface)) {
                this.mLog.err("Failed to register STA iface callback").flush();
                return stringResult(null);
            } else if (!retrieveWifiChip(iface)) {
                this.mLog.err("Failed to get wifi chip").flush();
                return stringResult(null);
            } else {
                enableLinkLayerStats(iface);
                this.mIWifiStaIfaces.put(ifaceName, iface);
                return ifaceName;
            }
        }
    }

    public boolean removeStaIface(String ifaceName) {
        synchronized (sLock) {
            IWifiStaIface iface = getStaIface(ifaceName);
            if (iface == null) {
                return boolResult(false);
            } else if (!this.mHalDeviceManager.removeIface(iface)) {
                this.mLog.err("Failed to remove STA iface").flush();
                return boolResult(false);
            } else {
                this.mIWifiStaIfaces.remove(ifaceName);
                return true;
            }
        }
    }

    private IWifiApIface getApIface(String ifaceName) {
        IWifiApIface iWifiApIface;
        synchronized (sLock) {
            iWifiApIface = this.mIWifiApIfaces.get(ifaceName);
        }
        return iWifiApIface;
    }

    /* access modifiers changed from: private */
    public class ApInterfaceDestroyedListenerInternal implements HalDeviceManager.InterfaceDestroyedListener {
        private final HalDeviceManager.InterfaceDestroyedListener mExternalListener;

        ApInterfaceDestroyedListenerInternal(HalDeviceManager.InterfaceDestroyedListener externalListener) {
            this.mExternalListener = externalListener;
        }

        @Override // com.android.server.wifi.HalDeviceManager.InterfaceDestroyedListener
        public void onDestroyed(String ifaceName) {
            synchronized (WifiVendorHal.sLock) {
                WifiVendorHal.this.mIWifiApIfaces.remove(ifaceName);
            }
            HalDeviceManager.InterfaceDestroyedListener interfaceDestroyedListener = this.mExternalListener;
            if (interfaceDestroyedListener != null) {
                interfaceDestroyedListener.onDestroyed(ifaceName);
            }
        }
    }

    public String createApIface(HalDeviceManager.InterfaceDestroyedListener destroyedListener) {
        synchronized (sLock) {
            IWifiApIface iface = this.mHalDeviceManager.createApIface(new ApInterfaceDestroyedListenerInternal(destroyedListener), null);
            if (iface == null) {
                this.mLog.err("Failed to create AP iface").flush();
                return stringResult(null);
            }
            HalDeviceManager halDeviceManager = this.mHalDeviceManager;
            String ifaceName = HalDeviceManager.getName(iface);
            if (TextUtils.isEmpty(ifaceName)) {
                this.mLog.err("Failed to get iface name").flush();
                return stringResult(null);
            } else if (!retrieveWifiChip(iface)) {
                this.mLog.err("Failed to get wifi chip").flush();
                return stringResult(null);
            } else {
                this.mIWifiApIfaces.put(ifaceName, iface);
                return ifaceName;
            }
        }
    }

    public boolean removeApIface(String ifaceName) {
        synchronized (sLock) {
            IWifiApIface iface = getApIface(ifaceName);
            if (iface == null) {
                return boolResult(false);
            } else if (!this.mHalDeviceManager.removeIface(iface)) {
                this.mLog.err("Failed to remove AP iface").flush();
                return boolResult(false);
            } else {
                this.mIWifiApIfaces.remove(ifaceName);
                return true;
            }
        }
    }

    private boolean retrieveWifiChip(IWifiIface iface) {
        synchronized (sLock) {
            boolean registrationNeeded = this.mIWifiChip == null;
            this.mIWifiChip = this.mHalDeviceManager.getChip(iface);
            if (this.mIWifiChip == null) {
                this.mLog.err("Failed to get the chip created for the Iface").flush();
                return false;
            } else if (!registrationNeeded) {
                return true;
            } else {
                if (registerChipCallback()) {
                    return true;
                }
                this.mLog.err("Failed to register chip callback").flush();
                this.mIWifiChip = null;
                return false;
            }
        }
    }

    private boolean registerStaIfaceCallback(IWifiStaIface iface) {
        synchronized (sLock) {
            if (iface == null) {
                return boolResult(false);
            } else if (this.mIWifiStaIfaceEventCallback == null) {
                return boolResult(false);
            } else {
                try {
                    return ok(iface.registerEventCallback(this.mIWifiStaIfaceEventCallback));
                } catch (RemoteException e) {
                    handleRemoteException(e);
                    return false;
                }
            }
        }
    }

    private boolean registerChipCallback() {
        WifiStatus status;
        synchronized (sLock) {
            if (this.mIWifiChip == null) {
                return boolResult(false);
            }
            try {
                android.hardware.wifi.V1_2.IWifiChip iWifiChipV12 = getWifiChipForV1_2Mockable();
                if (iWifiChipV12 != null) {
                    status = iWifiChipV12.registerEventCallback_1_2(this.mIWifiChipEventCallbackV12);
                } else {
                    status = this.mIWifiChip.registerEventCallback(this.mIWifiChipEventCallback);
                }
                return ok(status);
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    public void stopVendorHal() {
        synchronized (sLock) {
            this.mHalDeviceManager.stop();
            clearState();
            this.mLog.info("Vendor Hal stopped").flush();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearState() {
        this.mIWifiChip = null;
        this.mIWifiStaIfaces.clear();
        this.mIWifiApIfaces.clear();
        this.mDriverDescription = null;
        this.mFirmwareDescription = null;
    }

    public boolean isHalStarted() {
        boolean z;
        synchronized (sLock) {
            if (this.mIWifiStaIfaces.isEmpty()) {
                if (this.mIWifiApIfaces.isEmpty()) {
                    z = false;
                }
            }
            z = true;
        }
        return z;
    }

    public boolean getBgScanCapabilities(String ifaceName, WifiNative.ScanCapabilities capabilities) {
        synchronized (sLock) {
            IWifiStaIface iface = getStaIface(ifaceName);
            if (iface == null) {
                return boolResult(false);
            }
            try {
                MutableBoolean ans = new MutableBoolean(false);
                iface.getBackgroundScanCapabilities(new IWifiStaIface.getBackgroundScanCapabilitiesCallback(capabilities, ans) {
                    /* class com.android.server.wifi.$$Lambda$WifiVendorHal$qPUuRnlo2XMDrsA1gI_KLrbvPAI */
                    private final /* synthetic */ WifiNative.ScanCapabilities f$1;
                    private final /* synthetic */ MutableBoolean f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // android.hardware.wifi.V1_0.IWifiStaIface.getBackgroundScanCapabilitiesCallback
                    public final void onValues(WifiStatus wifiStatus, StaBackgroundScanCapabilities staBackgroundScanCapabilities) {
                        WifiVendorHal.this.lambda$getBgScanCapabilities$0$WifiVendorHal(this.f$1, this.f$2, wifiStatus, staBackgroundScanCapabilities);
                    }
                });
                return ans.value;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    public /* synthetic */ void lambda$getBgScanCapabilities$0$WifiVendorHal(WifiNative.ScanCapabilities out, MutableBoolean ans, WifiStatus status, StaBackgroundScanCapabilities cap) {
        if (ok(status)) {
            this.mVerboseLog.info("scan capabilities %").c(cap.toString()).flush();
            out.max_scan_cache_size = cap.maxCacheSize;
            out.max_ap_cache_per_scan = cap.maxApCachePerScan;
            out.max_scan_buckets = cap.maxBuckets;
            out.max_rssi_sample_size = 0;
            out.max_scan_reporting_threshold = cap.maxReportingThreshold;
            ans.value = true;
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public class CurrentBackgroundScan {
        public int cmdId;
        public WifiNative.ScanEventHandler eventHandler = null;
        public WifiScanner.ScanData[] latestScanResults;
        public StaBackgroundScanParameters param;
        public boolean paused;

        CurrentBackgroundScan(int id, WifiNative.ScanSettings settings) {
            this.paused = false;
            this.latestScanResults = null;
            this.cmdId = id;
            this.param = new StaBackgroundScanParameters();
            this.param.basePeriodInMs = settings.base_period_ms;
            this.param.maxApPerScan = settings.max_ap_per_scan;
            this.param.reportThresholdPercent = settings.report_threshold_percent;
            this.param.reportThresholdNumScans = settings.report_threshold_num_scans;
            if (settings.buckets != null) {
                for (WifiNative.BucketSettings bs : settings.buckets) {
                    this.param.buckets.add(WifiVendorHal.this.makeStaBackgroundScanBucketParametersFromBucketSettings(bs));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private StaBackgroundScanBucketParameters makeStaBackgroundScanBucketParametersFromBucketSettings(WifiNative.BucketSettings bs) {
        StaBackgroundScanBucketParameters pa = new StaBackgroundScanBucketParameters();
        pa.bucketIdx = bs.bucket;
        pa.band = makeWifiBandFromFrameworkBand(bs.band);
        if (bs.channels != null) {
            for (WifiNative.ChannelSettings cs : bs.channels) {
                pa.frequencies.add(Integer.valueOf(cs.frequency));
            }
        }
        pa.periodInMs = bs.period_ms;
        pa.eventReportScheme = makeReportSchemeFromBucketSettingsReportEvents(bs.report_events);
        pa.exponentialMaxPeriodInMs = bs.max_period_ms;
        pa.exponentialBase = 2;
        pa.exponentialStepCount = bs.step_count;
        return pa;
    }

    private int makeWifiBandFromFrameworkBand(int frameworkBand) {
        if (frameworkBand == 0) {
            return 0;
        }
        if (frameworkBand == 1) {
            return 1;
        }
        if (frameworkBand == 2) {
            return 2;
        }
        if (frameworkBand == 3) {
            return 3;
        }
        if (frameworkBand == 4) {
            return 4;
        }
        if (frameworkBand == 6) {
            return 6;
        }
        if (frameworkBand == 7) {
            return 7;
        }
        throw new IllegalArgumentException("bad band " + frameworkBand);
    }

    private int makeReportSchemeFromBucketSettingsReportEvents(int reportUnderscoreEvents) {
        int ans = 0;
        BitMask in = new BitMask(reportUnderscoreEvents);
        if (in.testAndClear(1)) {
            ans = 0 | 1;
        }
        if (in.testAndClear(2)) {
            ans |= 2;
        }
        if (in.testAndClear(4)) {
            ans |= 4;
        }
        if (in.value == 0) {
            return ans;
        }
        throw new IllegalArgumentException("bad " + reportUnderscoreEvents);
    }

    public boolean startBgScan(String ifaceName, WifiNative.ScanSettings settings, WifiNative.ScanEventHandler eventHandler) {
        if (eventHandler == null) {
            return boolResult(false);
        }
        synchronized (sLock) {
            IWifiStaIface iface = getStaIface(ifaceName);
            if (iface == null) {
                return boolResult(false);
            }
            try {
                if (this.mScan != null && !this.mScan.paused) {
                    ok(iface.stopBackgroundScan(this.mScan.cmdId));
                    this.mScan = null;
                }
                this.mLastScanCmdId = (this.mLastScanCmdId % 9) + 1;
                CurrentBackgroundScan scan = new CurrentBackgroundScan(this.mLastScanCmdId, settings);
                if (!ok(iface.startBackgroundScan(scan.cmdId, scan.param))) {
                    return false;
                }
                scan.eventHandler = eventHandler;
                this.mScan = scan;
                return true;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    public void stopBgScan(String ifaceName) {
        synchronized (sLock) {
            IWifiStaIface iface = getStaIface(ifaceName);
            if (iface != null) {
                try {
                    if (this.mScan != null) {
                        ok(iface.stopBackgroundScan(this.mScan.cmdId));
                        this.mScan = null;
                    }
                } catch (RemoteException e) {
                    handleRemoteException(e);
                }
            }
        }
    }

    public void pauseBgScan(String ifaceName) {
        synchronized (sLock) {
            try {
                IWifiStaIface iface = getStaIface(ifaceName);
                if (iface != null) {
                    if (this.mScan != null && !this.mScan.paused) {
                        if (ok(iface.stopBackgroundScan(this.mScan.cmdId))) {
                            this.mScan.paused = true;
                        }
                    }
                }
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void restartBgScan(String ifaceName) {
        synchronized (sLock) {
            IWifiStaIface iface = getStaIface(ifaceName);
            if (iface != null) {
                try {
                    if (this.mScan != null && this.mScan.paused) {
                        if (ok(iface.startBackgroundScan(this.mScan.cmdId, this.mScan.param))) {
                            this.mScan.paused = false;
                        }
                    }
                } catch (RemoteException e) {
                    handleRemoteException(e);
                }
            }
        }
    }

    public WifiScanner.ScanData[] getBgScanResults(String ifaceName) {
        synchronized (sLock) {
            if (getStaIface(ifaceName) == null) {
                return null;
            }
            if (this.mScan == null) {
                return null;
            }
            return this.mScan.latestScanResults;
        }
    }

    public WifiLinkLayerStats getWifiLinkLayerStats(String ifaceName) {
        if (getWifiStaIfaceForV1_3Mockable(ifaceName) != null) {
            return getWifiLinkLayerStats_1_3_Internal(ifaceName);
        }
        return getWifiLinkLayerStats_internal(ifaceName);
    }

    private WifiLinkLayerStats getWifiLinkLayerStats_internal(String ifaceName) {
        AnonymousClass1AnswerBox answer = new Object() {
            /* class com.android.server.wifi.WifiVendorHal.AnonymousClass1AnswerBox */
            public StaLinkLayerStats value = null;
        };
        synchronized (sLock) {
            try {
                IWifiStaIface iface = getStaIface(ifaceName);
                if (iface == null) {
                    return null;
                }
                iface.getLinkLayerStats(new IWifiStaIface.getLinkLayerStatsCallback(answer) {
                    /* class com.android.server.wifi.$$Lambda$WifiVendorHal$lNa_8_teac3OVuDxfIe7mrSRm7Y */
                    private final /* synthetic */ WifiVendorHal.AnonymousClass1AnswerBox f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // android.hardware.wifi.V1_0.IWifiStaIface.getLinkLayerStatsCallback
                    public final void onValues(WifiStatus wifiStatus, StaLinkLayerStats staLinkLayerStats) {
                        WifiVendorHal.this.lambda$getWifiLinkLayerStats_internal$1$WifiVendorHal(this.f$1, wifiStatus, staLinkLayerStats);
                    }
                });
                return frameworkFromHalLinkLayerStats(answer.value);
            } catch (RemoteException e) {
                handleRemoteException(e);
                return null;
            }
        }
    }

    public /* synthetic */ void lambda$getWifiLinkLayerStats_internal$1$WifiVendorHal(AnonymousClass1AnswerBox answer, WifiStatus status, StaLinkLayerStats stats) {
        if (ok(status)) {
            answer.value = stats;
        }
    }

    private WifiLinkLayerStats getWifiLinkLayerStats_1_3_Internal(String ifaceName) {
        AnonymousClass2AnswerBox answer = new Object() {
            /* class com.android.server.wifi.WifiVendorHal.AnonymousClass2AnswerBox */
            public android.hardware.wifi.V1_3.StaLinkLayerStats value = null;
        };
        synchronized (sLock) {
            try {
                android.hardware.wifi.V1_3.IWifiStaIface iface = getWifiStaIfaceForV1_3Mockable(ifaceName);
                if (iface == null) {
                    return null;
                }
                iface.getLinkLayerStats_1_3(new IWifiStaIface.getLinkLayerStats_1_3Callback(answer) {
                    /* class com.android.server.wifi.$$Lambda$WifiVendorHal$3w2QU6DXdNsy4eC63faTYVcIcMs */
                    private final /* synthetic */ WifiVendorHal.AnonymousClass2AnswerBox f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // android.hardware.wifi.V1_3.IWifiStaIface.getLinkLayerStats_1_3Callback
                    public final void onValues(WifiStatus wifiStatus, android.hardware.wifi.V1_3.StaLinkLayerStats staLinkLayerStats) {
                        WifiVendorHal.this.lambda$getWifiLinkLayerStats_1_3_Internal$2$WifiVendorHal(this.f$1, wifiStatus, staLinkLayerStats);
                    }
                });
                return frameworkFromHalLinkLayerStats_1_3(answer.value);
            } catch (RemoteException e) {
                handleRemoteException(e);
                return null;
            }
        }
    }

    public /* synthetic */ void lambda$getWifiLinkLayerStats_1_3_Internal$2$WifiVendorHal(AnonymousClass2AnswerBox answer, WifiStatus status, android.hardware.wifi.V1_3.StaLinkLayerStats stats) {
        if (ok(status)) {
            answer.value = stats;
        }
    }

    @VisibleForTesting
    static WifiLinkLayerStats frameworkFromHalLinkLayerStats(StaLinkLayerStats stats) {
        if (stats == null) {
            return null;
        }
        WifiLinkLayerStats out = new WifiLinkLayerStats();
        setIfaceStats(out, stats.iface);
        setRadioStats(out, stats.radios);
        setTimeStamp(out, stats.timeStampInMs);
        out.version = WifiLinkLayerStats.V1_0;
        return out;
    }

    @VisibleForTesting
    static WifiLinkLayerStats frameworkFromHalLinkLayerStats_1_3(android.hardware.wifi.V1_3.StaLinkLayerStats stats) {
        if (stats == null) {
            return null;
        }
        WifiLinkLayerStats out = new WifiLinkLayerStats();
        setIfaceStats(out, stats.iface);
        setRadioStats_1_3(out, stats.radios);
        setTimeStamp(out, stats.timeStampInMs);
        out.version = WifiLinkLayerStats.V1_3;
        return out;
    }

    private static void setIfaceStats(WifiLinkLayerStats stats, StaLinkLayerIfaceStats iface) {
        if (iface != null) {
            stats.beacon_rx = iface.beaconRx;
            stats.rssi_mgmt = iface.avgRssiMgmt;
            stats.rxmpdu_be = iface.wmeBePktStats.rxMpdu;
            stats.txmpdu_be = iface.wmeBePktStats.txMpdu;
            stats.lostmpdu_be = iface.wmeBePktStats.lostMpdu;
            stats.retries_be = iface.wmeBePktStats.retries;
            stats.rxmpdu_bk = iface.wmeBkPktStats.rxMpdu;
            stats.txmpdu_bk = iface.wmeBkPktStats.txMpdu;
            stats.lostmpdu_bk = iface.wmeBkPktStats.lostMpdu;
            stats.retries_bk = iface.wmeBkPktStats.retries;
            stats.rxmpdu_vi = iface.wmeViPktStats.rxMpdu;
            stats.txmpdu_vi = iface.wmeViPktStats.txMpdu;
            stats.lostmpdu_vi = iface.wmeViPktStats.lostMpdu;
            stats.retries_vi = iface.wmeViPktStats.retries;
            stats.rxmpdu_vo = iface.wmeVoPktStats.rxMpdu;
            stats.txmpdu_vo = iface.wmeVoPktStats.txMpdu;
            stats.lostmpdu_vo = iface.wmeVoPktStats.lostMpdu;
            stats.retries_vo = iface.wmeVoPktStats.retries;
        }
    }

    private static void setRadioStats(WifiLinkLayerStats stats, List<StaLinkLayerRadioStats> radios) {
        if (radios != null && radios.size() > 0) {
            StaLinkLayerRadioStats radioStats = radios.get(0);
            stats.on_time = radioStats.onTimeInMs;
            stats.tx_time = radioStats.txTimeInMs;
            stats.tx_time_per_level = new int[radioStats.txTimeInMsPerLevel.size()];
            for (int i = 0; i < stats.tx_time_per_level.length; i++) {
                stats.tx_time_per_level[i] = radioStats.txTimeInMsPerLevel.get(i).intValue();
            }
            stats.rx_time = radioStats.rxTimeInMs;
            stats.on_time_scan = radioStats.onTimeInMsForScan;
        }
    }

    private static void setRadioStats_1_3(WifiLinkLayerStats stats, List<android.hardware.wifi.V1_3.StaLinkLayerRadioStats> radios) {
        if (radios != null && radios.size() > 0) {
            android.hardware.wifi.V1_3.StaLinkLayerRadioStats radioStats = radios.get(0);
            stats.on_time = radioStats.V1_0.onTimeInMs;
            stats.tx_time = radioStats.V1_0.txTimeInMs;
            stats.tx_time_per_level = new int[radioStats.V1_0.txTimeInMsPerLevel.size()];
            for (int i = 0; i < stats.tx_time_per_level.length; i++) {
                stats.tx_time_per_level[i] = radioStats.V1_0.txTimeInMsPerLevel.get(i).intValue();
            }
            stats.rx_time = radioStats.V1_0.rxTimeInMs;
            stats.on_time_scan = radioStats.V1_0.onTimeInMsForScan;
            stats.on_time_nan_scan = radioStats.onTimeInMsForNanScan;
            stats.on_time_background_scan = radioStats.onTimeInMsForBgScan;
            stats.on_time_roam_scan = radioStats.onTimeInMsForRoamScan;
            stats.on_time_pno_scan = radioStats.onTimeInMsForPnoScan;
            stats.on_time_hs20_scan = radioStats.onTimeInMsForHs20Scan;
            for (int i2 = 0; i2 < radioStats.channelStats.size(); i2++) {
                WifiChannelStats channelStats = radioStats.channelStats.get(i2);
                WifiLinkLayerStats.ChannelStats channelStatsEntry = new WifiLinkLayerStats.ChannelStats();
                channelStatsEntry.frequency = channelStats.channel.centerFreq;
                channelStatsEntry.radioOnTimeMs = channelStats.onTimeInMs;
                channelStatsEntry.ccaBusyTimeMs = channelStats.ccaBusyTimeInMs;
                stats.channelStatsMap.put(channelStats.channel.centerFreq, channelStatsEntry);
            }
        }
    }

    private static void setTimeStamp(WifiLinkLayerStats stats, long timeStampInMs) {
        stats.timeStampInMs = timeStampInMs;
    }

    private void enableLinkLayerStats(android.hardware.wifi.V1_0.IWifiStaIface iface) {
        synchronized (sLock) {
            try {
                if (!ok(iface.enableLinkLayerStatsCollection(this.mLinkLayerStatsDebug))) {
                    this.mLog.err("unable to enable link layer stats collection").flush();
                }
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int wifiFeatureMaskFromChipCapabilities(int capabilities) {
        int features = 0;
        int i = 0;
        while (true) {
            int[][] iArr = sChipFeatureCapabilityTranslation;
            if (i >= iArr.length) {
                return features;
            }
            if ((iArr[i][1] & capabilities) != 0) {
                features |= iArr[i][0];
            }
            i++;
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public long wifiFeatureMaskFromChipCapabilities_1_3(int capabilities) {
        long features = (long) wifiFeatureMaskFromChipCapabilities(capabilities);
        int i = 0;
        while (true) {
            long[][] jArr = sChipFeatureCapabilityTranslation13;
            if (i >= jArr.length) {
                return features;
            }
            if ((((long) capabilities) & jArr[i][1]) != 0) {
                features |= jArr[i][0];
            }
            i++;
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int wifiFeatureMaskFromStaCapabilities(int capabilities) {
        int features = 0;
        int i = 0;
        while (true) {
            int[][] iArr = sStaFeatureCapabilityTranslation;
            if (i >= iArr.length) {
                return features;
            }
            if ((iArr[i][1] & capabilities) != 0) {
                features |= iArr[i][0];
            }
            i++;
        }
    }

    public long getSupportedFeatureSet(String ifaceName) {
        if (!this.mHalDeviceManager.isStarted()) {
            return 0;
        }
        try {
            MutableLong feat = new MutableLong(0);
            synchronized (sLock) {
                android.hardware.wifi.V1_3.IWifiChip iWifiChipV13 = getWifiChipForV1_3Mockable();
                if (iWifiChipV13 != null) {
                    iWifiChipV13.getCapabilities_1_3(new IWifiChip.getCapabilities_1_3Callback(feat) {
                        /* class com.android.server.wifi.$$Lambda$WifiVendorHal$XwdlM1fW3hc4x8NTlPEdTo9qP_w */
                        private final /* synthetic */ MutableLong f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // android.hardware.wifi.V1_3.IWifiChip.getCapabilities_1_3Callback
                        public final void onValues(WifiStatus wifiStatus, int i) {
                            WifiVendorHal.this.lambda$getSupportedFeatureSet$3$WifiVendorHal(this.f$1, wifiStatus, i);
                        }
                    });
                } else if (this.mIWifiChip != null) {
                    this.mIWifiChip.getCapabilities(new IWifiChip.getCapabilitiesCallback(feat) {
                        /* class com.android.server.wifi.$$Lambda$WifiVendorHal$vlEkpldMLMkeZfHkkzKdBRCNo */
                        private final /* synthetic */ MutableLong f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // android.hardware.wifi.V1_0.IWifiChip.getCapabilitiesCallback
                        public final void onValues(WifiStatus wifiStatus, int i) {
                            WifiVendorHal.this.lambda$getSupportedFeatureSet$4$WifiVendorHal(this.f$1, wifiStatus, i);
                        }
                    });
                }
                android.hardware.wifi.V1_0.IWifiStaIface iface = getStaIface(ifaceName);
                if (iface != null) {
                    iface.getCapabilities(new IWifiStaIface.getCapabilitiesCallback(feat) {
                        /* class com.android.server.wifi.$$Lambda$WifiVendorHal$gGTeGu2_MxK453qjY1pSxSOP3I */
                        private final /* synthetic */ MutableLong f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // android.hardware.wifi.V1_0.IWifiStaIface.getCapabilitiesCallback
                        public final void onValues(WifiStatus wifiStatus, int i) {
                            WifiVendorHal.this.lambda$getSupportedFeatureSet$5$WifiVendorHal(this.f$1, wifiStatus, i);
                        }
                    });
                }
            }
            long featureSet = feat.value;
            Set<Integer> supportedIfaceTypes = this.mHalDeviceManager.getSupportedIfaceTypes();
            if (supportedIfaceTypes.contains(0)) {
                featureSet |= 1;
            }
            if (supportedIfaceTypes.contains(1)) {
                featureSet |= 16;
            }
            if (supportedIfaceTypes.contains(2)) {
                featureSet |= 8;
            }
            if (supportedIfaceTypes.contains(3)) {
                return featureSet | 64;
            }
            return featureSet;
        } catch (RemoteException e) {
            handleRemoteException(e);
            return 0;
        }
    }

    public /* synthetic */ void lambda$getSupportedFeatureSet$3$WifiVendorHal(MutableLong feat, WifiStatus status, int capabilities) {
        if (ok(status)) {
            feat.value = wifiFeatureMaskFromChipCapabilities_1_3(capabilities);
        }
    }

    public /* synthetic */ void lambda$getSupportedFeatureSet$4$WifiVendorHal(MutableLong feat, WifiStatus status, int capabilities) {
        if (ok(status)) {
            feat.value = (long) wifiFeatureMaskFromChipCapabilities(capabilities);
        }
    }

    public /* synthetic */ void lambda$getSupportedFeatureSet$5$WifiVendorHal(MutableLong feat, WifiStatus status, int capabilities) {
        if (ok(status)) {
            feat.value |= (long) wifiFeatureMaskFromStaCapabilities(capabilities);
        }
    }

    public boolean setScanningMacOui(String ifaceName, byte[] oui) {
        if (oui == null) {
            return boolResult(false);
        }
        if (oui.length != 3) {
            return boolResult(false);
        }
        synchronized (sLock) {
            try {
                android.hardware.wifi.V1_0.IWifiStaIface iface = getStaIface(ifaceName);
                if (iface == null) {
                    return boolResult(false);
                } else if (!ok(iface.setScanningMacOui(oui))) {
                    return false;
                } else {
                    return true;
                }
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    public boolean setMacAddress(String ifaceName, MacAddress mac) {
        byte[] macByteArray = mac.toByteArray();
        synchronized (sLock) {
            try {
                android.hardware.wifi.V1_2.IWifiStaIface ifaceV12 = getWifiStaIfaceForV1_2Mockable(ifaceName);
                if (ifaceV12 == null) {
                    return boolResult(false);
                } else if (!ok(ifaceV12.setMacAddress(macByteArray))) {
                    return false;
                } else {
                    return true;
                }
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    public MacAddress getFactoryMacAddress(String ifaceName) {
        synchronized (sLock) {
            try {
                android.hardware.wifi.V1_3.IWifiStaIface ifaceV13 = getWifiStaIfaceForV1_3Mockable(ifaceName);
                if (ifaceV13 == null) {
                    return null;
                }
                AnonymousClass3AnswerBox box = new Object() {
                    /* class com.android.server.wifi.WifiVendorHal.AnonymousClass3AnswerBox */
                    public MacAddress mac = null;
                };
                ifaceV13.getFactoryMacAddress(new IWifiStaIface.getFactoryMacAddressCallback(box) {
                    /* class com.android.server.wifi.$$Lambda$WifiVendorHal$NcA8MRxoMqZf5Oz8FvFvnNoRQoE */
                    private final /* synthetic */ WifiVendorHal.AnonymousClass3AnswerBox f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // android.hardware.wifi.V1_3.IWifiStaIface.getFactoryMacAddressCallback
                    public final void onValues(WifiStatus wifiStatus, byte[] bArr) {
                        WifiVendorHal.this.lambda$getFactoryMacAddress$6$WifiVendorHal(this.f$1, wifiStatus, bArr);
                    }
                });
                return box.mac;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return null;
            }
        }
    }

    public /* synthetic */ void lambda$getFactoryMacAddress$6$WifiVendorHal(AnonymousClass3AnswerBox box, WifiStatus status, byte[] macBytes) {
        if (ok(status)) {
            box.mac = MacAddress.fromBytes(macBytes);
        }
    }

    public ApfCapabilities getApfCapabilities(String ifaceName) {
        synchronized (sLock) {
            try {
                android.hardware.wifi.V1_0.IWifiStaIface iface = getStaIface(ifaceName);
                if (iface == null) {
                    this.mLog.e("mIWifiStaIface is null, getApfCapabilities(0, 0, 0)");
                    return sNoApfCapabilities;
                }
                AnonymousClass4AnswerBox box = new Object() {
                    /* class com.android.server.wifi.WifiVendorHal.AnonymousClass4AnswerBox */
                    public ApfCapabilities value = WifiVendorHal.sNoApfCapabilities;
                };
                iface.getApfPacketFilterCapabilities(new IWifiStaIface.getApfPacketFilterCapabilitiesCallback(box) {
                    /* class com.android.server.wifi.$$Lambda$WifiVendorHal$teXyvL0oUnCS1kQEBqnEheMifN8 */
                    private final /* synthetic */ WifiVendorHal.AnonymousClass4AnswerBox f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // android.hardware.wifi.V1_0.IWifiStaIface.getApfPacketFilterCapabilitiesCallback
                    public final void onValues(WifiStatus wifiStatus, StaApfPacketFilterCapabilities staApfPacketFilterCapabilities) {
                        WifiVendorHal.this.lambda$getApfCapabilities$7$WifiVendorHal(this.f$1, wifiStatus, staApfPacketFilterCapabilities);
                    }
                });
                return box.value;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return sNoApfCapabilities;
            }
        }
    }

    public /* synthetic */ void lambda$getApfCapabilities$7$WifiVendorHal(AnonymousClass4AnswerBox box, WifiStatus status, StaApfPacketFilterCapabilities capabilities) {
        if (!ok(status)) {
            this.mLog.e("getApfCapabilities failed");
            return;
        }
        box.value = new ApfCapabilities(capabilities.version, capabilities.maxLength, OsConstants.ARPHRD_ETHER);
        WifiLog wifiLog = this.mLog;
        wifiLog.info("getApfCapabilities:(version:" + capabilities.version + ", maxLength:" + capabilities.maxLength + ", " + OsConstants.ARPHRD_ETHER + ")");
    }

    public boolean installPacketFilter(String ifaceName, byte[] filter) {
        if (filter == null) {
            return boolResult(false);
        }
        ArrayList<Byte> program = NativeUtil.byteArrayToArrayList(filter);
        enter("filter length %").c((long) filter.length).flush();
        synchronized (sLock) {
            try {
                android.hardware.wifi.V1_0.IWifiStaIface iface = getStaIface(ifaceName);
                if (iface == null) {
                    return boolResult(false);
                } else if (!ok(iface.installApfPacketFilter(0, program))) {
                    return false;
                } else {
                    return true;
                }
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    public byte[] readPacketFilter(String ifaceName) {
        AnonymousClass5AnswerBox answer = new Object() {
            /* class com.android.server.wifi.WifiVendorHal.AnonymousClass5AnswerBox */
            public byte[] data = null;
        };
        enter("").flush();
        synchronized (sLock) {
            try {
                android.hardware.wifi.V1_2.IWifiStaIface ifaceV12 = getWifiStaIfaceForV1_2Mockable(ifaceName);
                if (ifaceV12 == null) {
                    return byteArrayResult(null);
                }
                ifaceV12.readApfPacketFilterData(new IWifiStaIface.readApfPacketFilterDataCallback(answer) {
                    /* class com.android.server.wifi.$$Lambda$WifiVendorHal$fcBWPwN4m4v3yrrOWg1GDxxXgQ */
                    private final /* synthetic */ WifiVendorHal.AnonymousClass5AnswerBox f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // android.hardware.wifi.V1_2.IWifiStaIface.readApfPacketFilterDataCallback
                    public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                        WifiVendorHal.this.lambda$readPacketFilter$8$WifiVendorHal(this.f$1, wifiStatus, arrayList);
                    }
                });
                return byteArrayResult(answer.data);
            } catch (RemoteException e) {
                handleRemoteException(e);
                return byteArrayResult(null);
            }
        }
    }

    public /* synthetic */ void lambda$readPacketFilter$8$WifiVendorHal(AnonymousClass5AnswerBox answer, WifiStatus status, ArrayList dataByteArray) {
        if (ok(status)) {
            answer.data = NativeUtil.byteArrayFromArrayList(dataByteArray);
        }
    }

    public boolean setCountryCodeHal(String ifaceName, String countryCode) {
        if (countryCode == null) {
            return boolResult(false);
        }
        if (countryCode.length() != 2) {
            return boolResult(false);
        }
        try {
            byte[] code = NativeUtil.stringToByteArray(countryCode);
            synchronized (sLock) {
                try {
                    IWifiApIface iface = getApIface(ifaceName);
                    if (iface == null) {
                        return boolResult(false);
                    } else if (!ok(iface.setCountryCode(code))) {
                        return false;
                    } else {
                        return true;
                    }
                } catch (RemoteException e) {
                    handleRemoteException(e);
                    return false;
                }
            }
        } catch (IllegalArgumentException e2) {
            return boolResult(false);
        }
    }

    public boolean setLoggingEventHandler(WifiNative.WifiLoggerEventHandler handler) {
        if (handler == null) {
            return boolResult(false);
        }
        synchronized (sLock) {
            if (this.mIWifiChip == null) {
                return boolResult(false);
            } else if (this.mLogEventHandler != null) {
                return boolResult(false);
            } else {
                try {
                    if (!ok(this.mIWifiChip.enableDebugErrorAlerts(true))) {
                        return false;
                    }
                    this.mLogEventHandler = handler;
                    return true;
                } catch (RemoteException e) {
                    handleRemoteException(e);
                    return false;
                }
            }
        }
    }

    public boolean resetLogHandler() {
        synchronized (sLock) {
            this.mLogEventHandler = null;
            if (this.mIWifiChip == null) {
                return boolResult(false);
            }
            try {
                if (!ok(this.mIWifiChip.enableDebugErrorAlerts(false))) {
                    return false;
                }
                if (!ok(this.mIWifiChip.stopLoggingToDebugRingBuffer())) {
                    return false;
                }
                return true;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    public boolean startLoggingRingBuffer(int verboseLevel, int flags, int maxIntervalInSec, int minDataSizeInBytes, String ringName) {
        enter("verboseLevel=%, flags=%, maxIntervalInSec=%, minDataSizeInBytes=%, ringName=%").c((long) verboseLevel).c((long) flags).c((long) maxIntervalInSec).c((long) minDataSizeInBytes).c(ringName).flush();
        synchronized (sLock) {
            if (this.mIWifiChip == null) {
                return boolResult(false);
            }
            try {
                return ok(this.mIWifiChip.startLoggingToDebugRingBuffer(ringName, verboseLevel, maxIntervalInSec, minDataSizeInBytes));
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    public int getSupportedLoggerFeatureSet() {
        return -1;
    }

    public String getDriverVersion() {
        String str;
        synchronized (sLock) {
            if (this.mDriverDescription == null) {
                requestChipDebugInfo();
            }
            str = this.mDriverDescription;
        }
        return str;
    }

    public String getFirmwareVersion() {
        String str;
        synchronized (sLock) {
            if (this.mFirmwareDescription == null) {
                requestChipDebugInfo();
            }
            str = this.mFirmwareDescription;
        }
        return str;
    }

    private void requestChipDebugInfo() {
        this.mDriverDescription = null;
        this.mFirmwareDescription = null;
        try {
            if (this.mIWifiChip != null) {
                this.mIWifiChip.requestChipDebugInfo(new IWifiChip.requestChipDebugInfoCallback() {
                    /* class com.android.server.wifi.$$Lambda$WifiVendorHal$xtIgBZOv8ZXsi2hWuntD3i52tkY */

                    @Override // android.hardware.wifi.V1_0.IWifiChip.requestChipDebugInfoCallback
                    public final void onValues(WifiStatus wifiStatus, IWifiChip.ChipDebugInfo chipDebugInfo) {
                        WifiVendorHal.this.lambda$requestChipDebugInfo$9$WifiVendorHal(wifiStatus, chipDebugInfo);
                    }
                });
                this.mLog.info("Driver: % Firmware: %").c(this.mDriverDescription).c(this.mFirmwareDescription).flush();
            }
        } catch (RemoteException e) {
            handleRemoteException(e);
        }
    }

    public /* synthetic */ void lambda$requestChipDebugInfo$9$WifiVendorHal(WifiStatus status, IWifiChip.ChipDebugInfo chipDebugInfo) {
        if (ok(status)) {
            this.mDriverDescription = chipDebugInfo.driverDescription;
            this.mFirmwareDescription = chipDebugInfo.firmwareDescription;
        }
    }

    /* access modifiers changed from: private */
    public static WifiNative.RingBufferStatus ringBufferStatus(WifiDebugRingBufferStatus h) {
        WifiNative.RingBufferStatus ans = new WifiNative.RingBufferStatus();
        ans.name = h.ringName;
        ans.flag = frameworkRingBufferFlagsFromHal(h.flags);
        ans.ringBufferId = h.ringId;
        ans.ringBufferByteSize = h.sizeInBytes;
        ans.verboseLevel = h.verboseLevel;
        return ans;
    }

    private static int frameworkRingBufferFlagsFromHal(int wifiDebugRingBufferFlag) {
        BitMask checkoff = new BitMask(wifiDebugRingBufferFlag);
        int flags = 0;
        if (checkoff.testAndClear(1)) {
            flags = 0 | 1;
        }
        if (checkoff.testAndClear(2)) {
            flags |= 2;
        }
        if (checkoff.testAndClear(4)) {
            flags |= 4;
        }
        if (checkoff.value == 0) {
            return flags;
        }
        throw new IllegalArgumentException("Unknown WifiDebugRingBufferFlag " + checkoff.value);
    }

    private static WifiNative.RingBufferStatus[] makeRingBufferStatusArray(ArrayList<WifiDebugRingBufferStatus> ringBuffers) {
        WifiNative.RingBufferStatus[] ans = new WifiNative.RingBufferStatus[ringBuffers.size()];
        int i = 0;
        Iterator<WifiDebugRingBufferStatus> it = ringBuffers.iterator();
        while (it.hasNext()) {
            ans[i] = ringBufferStatus(it.next());
            i++;
        }
        return ans;
    }

    public WifiNative.RingBufferStatus[] getRingBufferStatus() {
        AnonymousClass6AnswerBox ans = new Object() {
            /* class com.android.server.wifi.WifiVendorHal.AnonymousClass6AnswerBox */
            public WifiNative.RingBufferStatus[] value = null;
        };
        synchronized (sLock) {
            if (this.mIWifiChip == null) {
                return null;
            }
            try {
                this.mIWifiChip.getDebugRingBuffersStatus(new IWifiChip.getDebugRingBuffersStatusCallback(ans) {
                    /* class com.android.server.wifi.$$Lambda$WifiVendorHal$aq7T76I9D1SC6d_j6P5zk6VHlw */
                    private final /* synthetic */ WifiVendorHal.AnonymousClass6AnswerBox f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // android.hardware.wifi.V1_0.IWifiChip.getDebugRingBuffersStatusCallback
                    public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                        WifiVendorHal.this.lambda$getRingBufferStatus$10$WifiVendorHal(this.f$1, wifiStatus, arrayList);
                    }
                });
                return ans.value;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return null;
            }
        }
    }

    public /* synthetic */ void lambda$getRingBufferStatus$10$WifiVendorHal(AnonymousClass6AnswerBox ans, WifiStatus status, ArrayList ringBuffers) {
        if (ok(status)) {
            ans.value = makeRingBufferStatusArray(ringBuffers);
        }
    }

    public boolean getRingBufferData(String ringName) {
        enter("ringName %").c(ringName).flush();
        synchronized (sLock) {
            if (this.mIWifiChip == null) {
                return boolResult(false);
            }
            try {
                return ok(this.mIWifiChip.forceDumpToDebugRingBuffer(ringName));
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    public boolean flushRingBufferData() {
        synchronized (sLock) {
            if (this.mIWifiChip == null) {
                return boolResult(false);
            }
            android.hardware.wifi.V1_3.IWifiChip iWifiChipV13 = getWifiChipForV1_3Mockable();
            if (iWifiChipV13 == null) {
                return false;
            }
            try {
                return ok(iWifiChipV13.flushRingBufferToFile());
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    public byte[] getFwMemoryDump() {
        AnonymousClass7AnswerBox ans = new Object() {
            /* class com.android.server.wifi.WifiVendorHal.AnonymousClass7AnswerBox */
            public byte[] value;
        };
        synchronized (sLock) {
            if (this.mIWifiChip == null) {
                return null;
            }
            try {
                this.mIWifiChip.requestFirmwareDebugDump(new IWifiChip.requestFirmwareDebugDumpCallback(ans) {
                    /* class com.android.server.wifi.$$Lambda$WifiVendorHal$fh5yhRsoEn3OamjidwKsPe7Ejxc */
                    private final /* synthetic */ WifiVendorHal.AnonymousClass7AnswerBox f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // android.hardware.wifi.V1_0.IWifiChip.requestFirmwareDebugDumpCallback
                    public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                        WifiVendorHal.this.lambda$getFwMemoryDump$11$WifiVendorHal(this.f$1, wifiStatus, arrayList);
                    }
                });
                return ans.value;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return null;
            }
        }
    }

    public /* synthetic */ void lambda$getFwMemoryDump$11$WifiVendorHal(AnonymousClass7AnswerBox ans, WifiStatus status, ArrayList blob) {
        if (ok(status)) {
            ans.value = NativeUtil.byteArrayFromArrayList(blob);
        }
    }

    public byte[] getDriverStateDump() {
        AnonymousClass8AnswerBox ans = new Object() {
            /* class com.android.server.wifi.WifiVendorHal.AnonymousClass8AnswerBox */
            public byte[] value;
        };
        synchronized (sLock) {
            if (this.mIWifiChip == null) {
                return null;
            }
            try {
                this.mIWifiChip.requestDriverDebugDump(new IWifiChip.requestDriverDebugDumpCallback(ans) {
                    /* class com.android.server.wifi.$$Lambda$WifiVendorHal$a8RXUcZq0Hn_XzBf6E3B6YLqHl4 */
                    private final /* synthetic */ WifiVendorHal.AnonymousClass8AnswerBox f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // android.hardware.wifi.V1_0.IWifiChip.requestDriverDebugDumpCallback
                    public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                        WifiVendorHal.this.lambda$getDriverStateDump$12$WifiVendorHal(this.f$1, wifiStatus, arrayList);
                    }
                });
                return ans.value;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return null;
            }
        }
    }

    public /* synthetic */ void lambda$getDriverStateDump$12$WifiVendorHal(AnonymousClass8AnswerBox ans, WifiStatus status, ArrayList blob) {
        if (ok(status)) {
            ans.value = NativeUtil.byteArrayFromArrayList(blob);
        }
    }

    public boolean startPktFateMonitoring(String ifaceName) {
        synchronized (sLock) {
            android.hardware.wifi.V1_0.IWifiStaIface iface = getStaIface(ifaceName);
            if (iface == null) {
                return boolResult(false);
            }
            try {
                return ok(iface.startDebugPacketFateMonitoring());
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    private byte halToFrameworkPktFateFrameType(int type) {
        if (type == 0) {
            return 0;
        }
        if (type == 1) {
            return 1;
        }
        if (type == 2) {
            return 2;
        }
        throw new IllegalArgumentException("bad " + type);
    }

    private byte halToFrameworkRxPktFate(int type) {
        switch (type) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            case 6:
                return 6;
            case 7:
                return 7;
            case 8:
                return 8;
            case 9:
                return 9;
            case 10:
                return 10;
            default:
                throw new IllegalArgumentException("bad " + type);
        }
    }

    private byte halToFrameworkTxPktFate(int type) {
        switch (type) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            case 6:
                return 6;
            case 7:
                return 7;
            case 8:
                return 8;
            case 9:
                return 9;
            default:
                throw new IllegalArgumentException("bad " + type);
        }
    }

    public boolean getTxPktFates(String ifaceName, WifiNative.TxFateReport[] reportBufs) {
        if (ArrayUtils.isEmpty(reportBufs)) {
            return boolResult(false);
        }
        synchronized (sLock) {
            android.hardware.wifi.V1_0.IWifiStaIface iface = getStaIface(ifaceName);
            if (iface == null) {
                return boolResult(false);
            }
            try {
                MutableBoolean ok = new MutableBoolean(false);
                iface.getDebugTxPacketFates(new IWifiStaIface.getDebugTxPacketFatesCallback(reportBufs, ok) {
                    /* class com.android.server.wifi.$$Lambda$WifiVendorHal$bMF1PMemHkEQ2iatQh0WVPpTYGY */
                    private final /* synthetic */ WifiNative.TxFateReport[] f$1;
                    private final /* synthetic */ MutableBoolean f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // android.hardware.wifi.V1_0.IWifiStaIface.getDebugTxPacketFatesCallback
                    public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                        WifiVendorHal.this.lambda$getTxPktFates$13$WifiVendorHal(this.f$1, this.f$2, wifiStatus, arrayList);
                    }
                });
                return ok.value;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    public /* synthetic */ void lambda$getTxPktFates$13$WifiVendorHal(WifiNative.TxFateReport[] reportBufs, MutableBoolean ok, WifiStatus status, ArrayList fates) {
        if (ok(status)) {
            int i = 0;
            Iterator it = fates.iterator();
            while (it.hasNext()) {
                WifiDebugTxPacketFateReport fate = (WifiDebugTxPacketFateReport) it.next();
                if (i >= reportBufs.length) {
                    break;
                }
                reportBufs[i] = new WifiNative.TxFateReport(halToFrameworkTxPktFate(fate.fate), fate.frameInfo.driverTimestampUsec, halToFrameworkPktFateFrameType(fate.frameInfo.frameType), NativeUtil.byteArrayFromArrayList(fate.frameInfo.frameContent));
                i++;
            }
            ok.value = true;
        }
    }

    public boolean getRxPktFates(String ifaceName, WifiNative.RxFateReport[] reportBufs) {
        if (ArrayUtils.isEmpty(reportBufs)) {
            return boolResult(false);
        }
        synchronized (sLock) {
            android.hardware.wifi.V1_0.IWifiStaIface iface = getStaIface(ifaceName);
            if (iface == null) {
                return boolResult(false);
            }
            try {
                MutableBoolean ok = new MutableBoolean(false);
                iface.getDebugRxPacketFates(new IWifiStaIface.getDebugRxPacketFatesCallback(reportBufs, ok) {
                    /* class com.android.server.wifi.$$Lambda$WifiVendorHal$XHfBJ_SOylRs_Rl4IjUDiKOzAY */
                    private final /* synthetic */ WifiNative.RxFateReport[] f$1;
                    private final /* synthetic */ MutableBoolean f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // android.hardware.wifi.V1_0.IWifiStaIface.getDebugRxPacketFatesCallback
                    public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                        WifiVendorHal.this.lambda$getRxPktFates$14$WifiVendorHal(this.f$1, this.f$2, wifiStatus, arrayList);
                    }
                });
                return ok.value;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    public /* synthetic */ void lambda$getRxPktFates$14$WifiVendorHal(WifiNative.RxFateReport[] reportBufs, MutableBoolean ok, WifiStatus status, ArrayList fates) {
        if (ok(status)) {
            int i = 0;
            Iterator it = fates.iterator();
            while (it.hasNext()) {
                WifiDebugRxPacketFateReport fate = (WifiDebugRxPacketFateReport) it.next();
                if (i >= reportBufs.length) {
                    break;
                }
                reportBufs[i] = new WifiNative.RxFateReport(halToFrameworkRxPktFate(fate.fate), fate.frameInfo.driverTimestampUsec, halToFrameworkPktFateFrameType(fate.frameInfo.frameType), NativeUtil.byteArrayFromArrayList(fate.frameInfo.frameContent));
                i++;
            }
            ok.value = true;
        }
    }

    public int startSendingOffloadedPacket(String ifaceName, int slot, byte[] srcMac, byte[] dstMac, byte[] packet, int protocol, int periodInMs) {
        enter("slot=% periodInMs=%").c((long) slot).c((long) periodInMs).flush();
        ArrayList<Byte> data = NativeUtil.byteArrayToArrayList(packet);
        synchronized (sLock) {
            try {
                android.hardware.wifi.V1_0.IWifiStaIface iface = getStaIface(ifaceName);
                if (iface == null) {
                    return -1;
                }
                try {
                    if (!ok(iface.startSendingKeepAlivePackets(slot, data, (short) protocol, srcMac, dstMac, periodInMs))) {
                        return -1;
                    }
                    return 0;
                } catch (RemoteException e) {
                    handleRemoteException(e);
                    return -1;
                } catch (Throwable th) {
                    e = th;
                    throw e;
                }
            } catch (Throwable th2) {
                e = th2;
                throw e;
            }
        }
    }

    public int stopSendingOffloadedPacket(String ifaceName, int slot) {
        enter("slot=%").c((long) slot).flush();
        synchronized (sLock) {
            android.hardware.wifi.V1_0.IWifiStaIface iface = getStaIface(ifaceName);
            if (iface == null) {
                return -1;
            }
            try {
                if (!ok(iface.stopSendingKeepAlivePackets(slot))) {
                    return -1;
                }
                return 0;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return -1;
            }
        }
    }

    public int startRssiMonitoring(String ifaceName, byte maxRssi, byte minRssi, WifiNative.WifiRssiEventHandler rssiEventHandler) {
        enter("maxRssi=% minRssi=%").c((long) maxRssi).c((long) minRssi).flush();
        if (maxRssi <= minRssi || rssiEventHandler == null) {
            return -1;
        }
        synchronized (sLock) {
            android.hardware.wifi.V1_0.IWifiStaIface iface = getStaIface(ifaceName);
            if (iface == null) {
                return -1;
            }
            try {
                iface.stopRssiMonitoring(sRssiMonCmdId);
                if (!ok(iface.startRssiMonitoring(sRssiMonCmdId, maxRssi, minRssi))) {
                    return -1;
                }
                this.mWifiRssiEventHandler = rssiEventHandler;
                return 0;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return -1;
            }
        }
    }

    public int stopRssiMonitoring(String ifaceName) {
        synchronized (sLock) {
            this.mWifiRssiEventHandler = null;
            android.hardware.wifi.V1_0.IWifiStaIface iface = getStaIface(ifaceName);
            if (iface == null) {
                return -1;
            }
            try {
                if (!ok(iface.stopRssiMonitoring(sRssiMonCmdId))) {
                    return -1;
                }
                return 0;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return -1;
            }
        }
    }

    private static int[] intsFromArrayList(ArrayList<Integer> a) {
        if (a == null) {
            return null;
        }
        int[] b = new int[a.size()];
        int i = 0;
        Iterator<Integer> it = a.iterator();
        while (it.hasNext()) {
            b[i] = it.next().intValue();
            i++;
        }
        return b;
    }

    private static WlanWakeReasonAndCounts halToFrameworkWakeReasons(WifiDebugHostWakeReasonStats h) {
        if (h == null) {
            return null;
        }
        WlanWakeReasonAndCounts ans = new WlanWakeReasonAndCounts();
        ans.totalCmdEventWake = h.totalCmdEventWakeCnt;
        ans.totalDriverFwLocalWake = h.totalDriverFwLocalWakeCnt;
        ans.totalRxDataWake = h.totalRxPacketWakeCnt;
        ans.rxUnicast = h.rxPktWakeDetails.rxUnicastCnt;
        ans.rxMulticast = h.rxPktWakeDetails.rxMulticastCnt;
        ans.rxBroadcast = h.rxPktWakeDetails.rxBroadcastCnt;
        ans.icmp = h.rxIcmpPkWakeDetails.icmpPkt;
        ans.icmp6 = h.rxIcmpPkWakeDetails.icmp6Pkt;
        ans.icmp6Ra = h.rxIcmpPkWakeDetails.icmp6Ra;
        ans.icmp6Na = h.rxIcmpPkWakeDetails.icmp6Na;
        ans.icmp6Ns = h.rxIcmpPkWakeDetails.icmp6Ns;
        ans.ipv4RxMulticast = h.rxMulticastPkWakeDetails.ipv4RxMulticastAddrCnt;
        ans.ipv6Multicast = h.rxMulticastPkWakeDetails.ipv6RxMulticastAddrCnt;
        ans.otherRxMulticast = h.rxMulticastPkWakeDetails.otherRxMulticastAddrCnt;
        ans.cmdEventWakeCntArray = intsFromArrayList(h.cmdEventWakeCntPerType);
        ans.driverFWLocalWakeCntArray = intsFromArrayList(h.driverFwLocalWakeCntPerType);
        return ans;
    }

    public WlanWakeReasonAndCounts getWlanWakeReasonCount() {
        AnonymousClass9AnswerBox ans = new Object() {
            /* class com.android.server.wifi.WifiVendorHal.AnonymousClass9AnswerBox */
            public WifiDebugHostWakeReasonStats value = null;
        };
        synchronized (sLock) {
            if (this.mIWifiChip == null) {
                return null;
            }
            try {
                this.mIWifiChip.getDebugHostWakeReasonStats(new IWifiChip.getDebugHostWakeReasonStatsCallback(ans) {
                    /* class com.android.server.wifi.$$Lambda$WifiVendorHal$4M4uW04Mmx55Iw4K25_Ct6rvI */
                    private final /* synthetic */ WifiVendorHal.AnonymousClass9AnswerBox f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // android.hardware.wifi.V1_0.IWifiChip.getDebugHostWakeReasonStatsCallback
                    public final void onValues(WifiStatus wifiStatus, WifiDebugHostWakeReasonStats wifiDebugHostWakeReasonStats) {
                        WifiVendorHal.this.lambda$getWlanWakeReasonCount$15$WifiVendorHal(this.f$1, wifiStatus, wifiDebugHostWakeReasonStats);
                    }
                });
                return halToFrameworkWakeReasons(ans.value);
            } catch (RemoteException e) {
                handleRemoteException(e);
                return null;
            }
        }
    }

    public /* synthetic */ void lambda$getWlanWakeReasonCount$15$WifiVendorHal(AnonymousClass9AnswerBox ans, WifiStatus status, WifiDebugHostWakeReasonStats stats) {
        if (ok(status)) {
            ans.value = stats;
        }
    }

    public boolean configureNeighborDiscoveryOffload(String ifaceName, boolean enabled) {
        enter("enabled=%").c(enabled).flush();
        synchronized (sLock) {
            android.hardware.wifi.V1_0.IWifiStaIface iface = getStaIface(ifaceName);
            if (iface == null) {
                return boolResult(false);
            }
            try {
                if (!ok(iface.enableNdOffload(enabled))) {
                    return false;
                }
                return true;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    public boolean getRoamingCapabilities(String ifaceName, WifiNative.RoamingCapabilities capabilities) {
        synchronized (sLock) {
            android.hardware.wifi.V1_0.IWifiStaIface iface = getStaIface(ifaceName);
            if (iface == null) {
                return boolResult(false);
            }
            try {
                MutableBoolean ok = new MutableBoolean(false);
                iface.getRoamingCapabilities(new IWifiStaIface.getRoamingCapabilitiesCallback(capabilities, ok) {
                    /* class com.android.server.wifi.$$Lambda$WifiVendorHal$rxCjKAj6M7R0PzNwiuTAmPm5RQ */
                    private final /* synthetic */ WifiNative.RoamingCapabilities f$1;
                    private final /* synthetic */ MutableBoolean f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // android.hardware.wifi.V1_0.IWifiStaIface.getRoamingCapabilitiesCallback
                    public final void onValues(WifiStatus wifiStatus, StaRoamingCapabilities staRoamingCapabilities) {
                        WifiVendorHal.this.lambda$getRoamingCapabilities$16$WifiVendorHal(this.f$1, this.f$2, wifiStatus, staRoamingCapabilities);
                    }
                });
                return ok.value;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    public /* synthetic */ void lambda$getRoamingCapabilities$16$WifiVendorHal(WifiNative.RoamingCapabilities out, MutableBoolean ok, WifiStatus status, StaRoamingCapabilities cap) {
        if (ok(status)) {
            out.maxBlacklistSize = cap.maxBlacklistSize;
            out.maxWhitelistSize = cap.maxWhitelistSize;
            ok.value = true;
        }
    }

    public int enableFirmwareRoaming(String ifaceName, int state) {
        byte val;
        synchronized (sLock) {
            android.hardware.wifi.V1_0.IWifiStaIface iface = getStaIface(ifaceName);
            if (iface == null) {
                return 1;
            }
            if (state == 0) {
                val = 0;
            } else if (state != 1) {
                try {
                    this.mLog.err("enableFirmwareRoaming invalid argument %").c((long) state).flush();
                    return 1;
                } catch (RemoteException e) {
                    handleRemoteException(e);
                    return 1;
                }
            } else {
                val = 1;
            }
            WifiStatus status = iface.setRoamingState(val);
            if (ok(status)) {
                return 0;
            }
            if (status.code == 8) {
                return 2;
            }
            return 1;
        }
    }

    public boolean configureRoaming(String ifaceName, WifiNative.RoamingConfig config) {
        synchronized (sLock) {
            android.hardware.wifi.V1_0.IWifiStaIface iface = getStaIface(ifaceName);
            if (iface == null) {
                return boolResult(false);
            }
            try {
                StaRoamingConfig roamingConfig = new StaRoamingConfig();
                if (config.blacklistBssids != null) {
                    Iterator<String> it = config.blacklistBssids.iterator();
                    while (it.hasNext()) {
                        roamingConfig.bssidBlacklist.add(NativeUtil.macAddressToByteArray(it.next()));
                    }
                }
                if (config.whitelistSsids != null) {
                    Iterator<String> it2 = config.whitelistSsids.iterator();
                    while (it2.hasNext()) {
                        roamingConfig.ssidWhitelist.add(NativeUtil.byteArrayFromArrayList(NativeUtil.decodeSsid(it2.next())));
                    }
                }
                if (!ok(iface.configureRoaming(roamingConfig))) {
                    return false;
                }
                return true;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            } catch (IllegalArgumentException e2) {
                this.mLog.err("Illegal argument for roaming configuration").c(e2.toString()).flush();
                return false;
            }
        }
    }

    /* access modifiers changed from: protected */
    public android.hardware.wifi.V1_1.IWifiChip getWifiChipForV1_1Mockable() {
        android.hardware.wifi.V1_0.IWifiChip iWifiChip = this.mIWifiChip;
        if (iWifiChip == null) {
            return null;
        }
        return android.hardware.wifi.V1_1.IWifiChip.castFrom((IHwInterface) iWifiChip);
    }

    /* access modifiers changed from: protected */
    public android.hardware.wifi.V1_2.IWifiChip getWifiChipForV1_2Mockable() {
        android.hardware.wifi.V1_0.IWifiChip iWifiChip = this.mIWifiChip;
        if (iWifiChip == null) {
            return null;
        }
        return android.hardware.wifi.V1_2.IWifiChip.castFrom((IHwInterface) iWifiChip);
    }

    /* access modifiers changed from: protected */
    public android.hardware.wifi.V1_3.IWifiChip getWifiChipForV1_3Mockable() {
        android.hardware.wifi.V1_0.IWifiChip iWifiChip = this.mIWifiChip;
        if (iWifiChip == null) {
            return null;
        }
        return android.hardware.wifi.V1_3.IWifiChip.castFrom((IHwInterface) iWifiChip);
    }

    /* access modifiers changed from: protected */
    public android.hardware.wifi.V1_2.IWifiStaIface getWifiStaIfaceForV1_2Mockable(String ifaceName) {
        android.hardware.wifi.V1_0.IWifiStaIface iface = getStaIface(ifaceName);
        if (iface == null) {
            return null;
        }
        return android.hardware.wifi.V1_2.IWifiStaIface.castFrom((IHwInterface) iface);
    }

    /* access modifiers changed from: protected */
    public android.hardware.wifi.V1_3.IWifiStaIface getWifiStaIfaceForV1_3Mockable(String ifaceName) {
        android.hardware.wifi.V1_0.IWifiStaIface iface = getStaIface(ifaceName);
        if (iface == null) {
            return null;
        }
        return android.hardware.wifi.V1_3.IWifiStaIface.castFrom((IHwInterface) iface);
    }

    private boolean sarPowerBackoffRequired_1_1(SarInfo sarInfo) {
        if (!sarInfo.sarVoiceCallSupported) {
            return false;
        }
        if (sarInfo.isVoiceCall || sarInfo.isEarPieceActive) {
            return true;
        }
        return false;
    }

    private int frameworkToHalTxPowerScenario_1_1(SarInfo sarInfo) {
        if (sarInfo.sarVoiceCallSupported && (sarInfo.isVoiceCall || sarInfo.isEarPieceActive)) {
            return 0;
        }
        throw new IllegalArgumentException("bad scenario: voice call not active/supported");
    }

    private boolean sarPowerBackoffRequired_1_2(SarInfo sarInfo) {
        if (sarInfo.sarSensorSupported) {
            return sarInfo.sensorState != 1;
        }
        if (!sarInfo.sarSapSupported || !sarInfo.isWifiSapEnabled) {
            return sarInfo.sarVoiceCallSupported && (sarInfo.isVoiceCall || sarInfo.isEarPieceActive);
        }
        return true;
    }

    private int frameworkToHalTxPowerScenario_1_2(SarInfo sarInfo) {
        if (sarInfo.sarSensorSupported) {
            int i = sarInfo.sensorState;
            if (i != 2) {
                if (i != 3) {
                    if (i != 4) {
                        throw new IllegalArgumentException("bad scenario: Invalid sensor state");
                    }
                } else if (sarInfo.isVoiceCall || sarInfo.isWifiSapEnabled) {
                    return 2;
                } else {
                    return 1;
                }
            }
            if (sarInfo.isVoiceCall || sarInfo.isWifiSapEnabled) {
                return 4;
            }
            return 3;
        } else if (!sarInfo.sarSapSupported || !sarInfo.sarVoiceCallSupported) {
            if (!sarInfo.sarVoiceCallSupported) {
                throw new IllegalArgumentException("Invalid case: voice call not supported");
            } else if (sarInfo.isVoiceCall || sarInfo.isEarPieceActive) {
                return 0;
            } else {
                throw new IllegalArgumentException("bad scenario: voice call not active");
            }
        } else if (sarInfo.isVoiceCall || sarInfo.isEarPieceActive) {
            return 2;
        } else {
            if (sarInfo.isWifiSapEnabled) {
                return 4;
            }
            throw new IllegalArgumentException("bad scenario: no voice call/softAP active");
        }
    }

    public boolean selectTxPowerScenario(SarInfo sarInfo) {
        synchronized (sLock) {
            android.hardware.wifi.V1_2.IWifiChip iWifiChipV12 = getWifiChipForV1_2Mockable();
            if (iWifiChipV12 != null) {
                return selectTxPowerScenario_1_2(iWifiChipV12, sarInfo);
            }
            android.hardware.wifi.V1_1.IWifiChip iWifiChipV11 = getWifiChipForV1_1Mockable();
            if (iWifiChipV11 == null) {
                return false;
            }
            return selectTxPowerScenario_1_1(iWifiChipV11, sarInfo);
        }
    }

    private boolean selectTxPowerScenario_1_1(android.hardware.wifi.V1_1.IWifiChip iWifiChip, SarInfo sarInfo) {
        try {
            if (sarPowerBackoffRequired_1_1(sarInfo)) {
                int halScenario = frameworkToHalTxPowerScenario_1_1(sarInfo);
                if (!sarInfo.setSarScenarioNeeded(halScenario)) {
                    return true;
                }
                if (ok(iWifiChip.selectTxPowerScenario(halScenario))) {
                    WifiLog wifiLog = this.mLog;
                    wifiLog.d("Setting SAR scenario to " + halScenario);
                    return true;
                }
                WifiLog wifiLog2 = this.mLog;
                wifiLog2.e("Failed to set SAR scenario to " + halScenario);
                return false;
            } else if (!sarInfo.resetSarScenarioNeeded()) {
                return true;
            } else {
                if (ok(iWifiChip.resetTxPowerScenario())) {
                    this.mLog.d("Resetting SAR scenario");
                    return true;
                }
                this.mLog.e("Failed to reset SAR scenario");
                return false;
            }
        } catch (RemoteException e) {
            handleRemoteException(e);
            return false;
        } catch (IllegalArgumentException e2) {
            this.mLog.err("Illegal argument for selectTxPowerScenario_1_1()").c(e2.toString()).flush();
            return false;
        }
    }

    private boolean selectTxPowerScenario_1_2(android.hardware.wifi.V1_2.IWifiChip iWifiChip, SarInfo sarInfo) {
        try {
            if (sarPowerBackoffRequired_1_2(sarInfo)) {
                int halScenario = frameworkToHalTxPowerScenario_1_2(sarInfo);
                if (!sarInfo.setSarScenarioNeeded(halScenario)) {
                    return true;
                }
                if (ok(iWifiChip.selectTxPowerScenario_1_2(halScenario))) {
                    WifiLog wifiLog = this.mLog;
                    wifiLog.d("Setting SAR scenario to " + halScenario);
                    return true;
                }
                WifiLog wifiLog2 = this.mLog;
                wifiLog2.e("Failed to set SAR scenario to " + halScenario);
                return false;
            } else if (!sarInfo.resetSarScenarioNeeded()) {
                return true;
            } else {
                if (ok(iWifiChip.resetTxPowerScenario())) {
                    this.mLog.d("Resetting SAR scenario");
                    return true;
                }
                this.mLog.e("Failed to reset SAR scenario");
                return false;
            }
        } catch (RemoteException e) {
            handleRemoteException(e);
            return false;
        } catch (IllegalArgumentException e2) {
            this.mLog.err("Illegal argument for selectTxPowerScenario_1_2()").c(e2.toString()).flush();
            return false;
        }
    }

    public boolean setLowLatencyMode(boolean enabled) {
        int mode;
        synchronized (sLock) {
            android.hardware.wifi.V1_3.IWifiChip iWifiChipV13 = getWifiChipForV1_3Mockable();
            if (iWifiChipV13 == null) {
                return false;
            }
            if (enabled) {
                mode = 1;
            } else {
                mode = 0;
            }
            try {
                if (ok(iWifiChipV13.setLatencyMode(mode))) {
                    this.mVerboseLog.d("Setting low-latency mode to " + enabled);
                    return true;
                }
                this.mLog.e("Failed to set low-latency mode to " + enabled);
                return false;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    private static byte[] hidlIeArrayToFrameworkIeBlob(ArrayList<WifiInformationElement> ies) {
        if (ies == null || ies.isEmpty()) {
            return new byte[0];
        }
        ArrayList<Byte> ieBlob = new ArrayList<>();
        Iterator<WifiInformationElement> it = ies.iterator();
        while (it.hasNext()) {
            WifiInformationElement ie = it.next();
            ieBlob.add(Byte.valueOf(ie.id));
            ieBlob.addAll(ie.data);
        }
        return NativeUtil.byteArrayFromArrayList(ieBlob);
    }

    /* access modifiers changed from: private */
    public static ScanResult hidlToFrameworkScanResult(StaScanResult scanResult) {
        if (scanResult == null) {
            return null;
        }
        ScanResult frameworkScanResult = new ScanResult();
        try {
            frameworkScanResult.SSID = NativeUtil.encodeSsid(scanResult.ssid);
            frameworkScanResult.wifiSsid = WifiSsid.createFromByteArray(NativeUtil.byteArrayFromArrayList(scanResult.ssid));
            frameworkScanResult.BSSID = NativeUtil.macAddressFromByteArray(scanResult.bssid);
            frameworkScanResult.level = scanResult.rssi;
            frameworkScanResult.frequency = scanResult.frequency;
            frameworkScanResult.timestamp = scanResult.timeStampInUs;
            frameworkScanResult.capabilities = GenerateScanResult.generateScanResultCapabilities(frameworkScanResult.informationElements, scanResult.capability);
            return frameworkScanResult;
        } catch (IllegalArgumentException e) {
            Log.e("WifiVendorHal", "Could not encode scanResult.ssid.");
            return null;
        }
    }

    private static ScanResult[] hidlToFrameworkScanResults(ArrayList<StaScanResult> scanResults) {
        if (scanResults == null || scanResults.isEmpty()) {
            return new ScanResult[0];
        }
        ScanResult[] frameworkScanResults = new ScanResult[scanResults.size()];
        int i = 0;
        Iterator<StaScanResult> it = scanResults.iterator();
        while (it.hasNext()) {
            frameworkScanResults[i] = hidlToFrameworkScanResult(it.next());
            i++;
        }
        return frameworkScanResults;
    }

    private static int hidlToFrameworkScanDataFlags(int flag) {
        if (flag == 1) {
            return 1;
        }
        return 0;
    }

    /* access modifiers changed from: private */
    public static WifiScanner.ScanData[] hidlToFrameworkScanDatas(int cmdId, ArrayList<StaScanData> scanDatas) {
        if (scanDatas == null || scanDatas.isEmpty()) {
            return new WifiScanner.ScanData[0];
        }
        WifiScanner.ScanData[] frameworkScanDatas = new WifiScanner.ScanData[scanDatas.size()];
        int i = 0;
        Iterator<StaScanData> it = scanDatas.iterator();
        while (it.hasNext()) {
            StaScanData scanData = it.next();
            frameworkScanDatas[i] = new WifiScanner.ScanData(cmdId, hidlToFrameworkScanDataFlags(scanData.flags), scanData.bucketsScanned, 0, hidlToFrameworkScanResults(scanData.results));
            i++;
        }
        return frameworkScanDatas;
    }

    private class StaIfaceEventCallback extends IWifiStaIfaceEventCallback.Stub {
        private StaIfaceEventCallback() {
        }

        @Override // android.hardware.wifi.V1_0.IWifiStaIfaceEventCallback
        public void onBackgroundScanFailure(int cmdId) {
            WifiLog wifiLog = WifiVendorHal.this.mVerboseLog;
            wifiLog.d("onBackgroundScanFailure " + cmdId);
            synchronized (WifiVendorHal.sLock) {
                if (WifiVendorHal.this.mScan != null) {
                    if (cmdId == WifiVendorHal.this.mScan.cmdId) {
                        WifiVendorHal.this.mScan.eventHandler.onScanStatus(3);
                    }
                }
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiStaIfaceEventCallback
        public void onBackgroundFullScanResult(int cmdId, int bucketsScanned, StaScanResult result) {
            WifiLog wifiLog = WifiVendorHal.this.mVerboseLog;
            wifiLog.d("onBackgroundFullScanResult " + cmdId);
            synchronized (WifiVendorHal.sLock) {
                if (WifiVendorHal.this.mScan != null) {
                    if (cmdId == WifiVendorHal.this.mScan.cmdId) {
                        WifiVendorHal.this.mScan.eventHandler.onFullScanResult(WifiVendorHal.hidlToFrameworkScanResult(result), bucketsScanned);
                    }
                }
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiStaIfaceEventCallback
        public void onBackgroundScanResults(int cmdId, ArrayList<StaScanData> scanDatas) {
            WifiLog wifiLog = WifiVendorHal.this.mVerboseLog;
            wifiLog.d("onBackgroundScanResults " + cmdId);
            synchronized (WifiVendorHal.sLock) {
                if (WifiVendorHal.this.mScan != null) {
                    if (cmdId == WifiVendorHal.this.mScan.cmdId) {
                        WifiNative.ScanEventHandler eventHandler = WifiVendorHal.this.mScan.eventHandler;
                        WifiVendorHal.this.mScan.latestScanResults = WifiVendorHal.hidlToFrameworkScanDatas(cmdId, scanDatas);
                        eventHandler.onScanStatus(0);
                    }
                }
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiStaIfaceEventCallback
        public void onRssiThresholdBreached(int cmdId, byte[] currBssid, int currRssi) {
            WifiLog wifiLog = WifiVendorHal.this.mVerboseLog;
            wifiLog.d("onRssiThresholdBreached " + cmdId + "currRssi " + currRssi);
            synchronized (WifiVendorHal.sLock) {
                if (WifiVendorHal.this.mWifiRssiEventHandler != null) {
                    if (cmdId == WifiVendorHal.sRssiMonCmdId) {
                        WifiVendorHal.this.mWifiRssiEventHandler.onRssiThresholdBreached((byte) currRssi);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class ChipEventCallback extends IWifiChipEventCallback.Stub {
        private ChipEventCallback() {
        }

        @Override // android.hardware.wifi.V1_0.IWifiChipEventCallback
        public void onChipReconfigured(int modeId) {
            WifiLog wifiLog = WifiVendorHal.this.mVerboseLog;
            wifiLog.d("onChipReconfigured " + modeId);
        }

        @Override // android.hardware.wifi.V1_0.IWifiChipEventCallback
        public void onChipReconfigureFailure(WifiStatus status) {
            WifiLog wifiLog = WifiVendorHal.this.mVerboseLog;
            wifiLog.d("onChipReconfigureFailure " + status);
        }

        @Override // android.hardware.wifi.V1_0.IWifiChipEventCallback
        public void onIfaceAdded(int type, String name) {
            WifiLog wifiLog = WifiVendorHal.this.mVerboseLog;
            wifiLog.d("onIfaceAdded " + type + ", name: " + name);
        }

        @Override // android.hardware.wifi.V1_0.IWifiChipEventCallback
        public void onIfaceRemoved(int type, String name) {
            WifiLog wifiLog = WifiVendorHal.this.mVerboseLog;
            wifiLog.d("onIfaceRemoved " + type + ", name: " + name);
        }

        @Override // android.hardware.wifi.V1_0.IWifiChipEventCallback
        public void onDebugRingBufferDataAvailable(WifiDebugRingBufferStatus status, ArrayList<Byte> data) {
            WifiVendorHal.this.mHalEventHandler.post(new Runnable(status, data) {
                /* class com.android.server.wifi.$$Lambda$WifiVendorHal$ChipEventCallback$AqzJie2OoFIziDRxaXrZoSkKfNw */
                private final /* synthetic */ WifiDebugRingBufferStatus f$1;
                private final /* synthetic */ ArrayList f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    WifiVendorHal.ChipEventCallback.this.lambda$onDebugRingBufferDataAvailable$0$WifiVendorHal$ChipEventCallback(this.f$1, this.f$2);
                }
            });
        }

        public /* synthetic */ void lambda$onDebugRingBufferDataAvailable$0$WifiVendorHal$ChipEventCallback(WifiDebugRingBufferStatus status, ArrayList data) {
            WifiNative.WifiLoggerEventHandler eventHandler;
            synchronized (WifiVendorHal.sLock) {
                if (!(WifiVendorHal.this.mLogEventHandler == null || status == null)) {
                    if (data != null) {
                        eventHandler = WifiVendorHal.this.mLogEventHandler;
                    }
                }
                return;
            }
            int sizeBefore = data.size();
            boolean conversionFailure = false;
            try {
                eventHandler.onRingBufferData(WifiVendorHal.ringBufferStatus(status), NativeUtil.byteArrayFromArrayList(data));
                if (data.size() != sizeBefore) {
                    conversionFailure = true;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                conversionFailure = true;
            }
            if (conversionFailure) {
                Log.wtf("WifiVendorHal", "Conversion failure detected in onDebugRingBufferDataAvailable. The input ArrayList |data| is potentially corrupted. Starting size=" + sizeBefore + ", final size=" + data.size());
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiChipEventCallback
        public void onDebugErrorAlert(int errorCode, ArrayList<Byte> debugData) {
            WifiLog wifiLog = WifiVendorHal.this.mLog;
            wifiLog.w("onDebugErrorAlert " + errorCode);
            WifiVendorHal.this.mHalEventHandler.post(new Runnable(debugData, errorCode) {
                /* class com.android.server.wifi.$$Lambda$WifiVendorHal$ChipEventCallback$opFP1g0mCa0rIEtg63LvzlqySHc */
                private final /* synthetic */ ArrayList f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    WifiVendorHal.ChipEventCallback.this.lambda$onDebugErrorAlert$1$WifiVendorHal$ChipEventCallback(this.f$1, this.f$2);
                }
            });
        }

        public /* synthetic */ void lambda$onDebugErrorAlert$1$WifiVendorHal$ChipEventCallback(ArrayList debugData, int errorCode) {
            synchronized (WifiVendorHal.sLock) {
                if (WifiVendorHal.this.mLogEventHandler != null) {
                    if (debugData != null) {
                        WifiVendorHal.this.mLogEventHandler.onWifiAlert(errorCode, NativeUtil.byteArrayFromArrayList(debugData));
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class ChipEventCallbackV12 extends IWifiChipEventCallback.Stub {
        private ChipEventCallbackV12() {
        }

        @Override // android.hardware.wifi.V1_0.IWifiChipEventCallback
        public void onChipReconfigured(int modeId) {
            WifiVendorHal.this.mIWifiChipEventCallback.onChipReconfigured(modeId);
        }

        @Override // android.hardware.wifi.V1_0.IWifiChipEventCallback
        public void onChipReconfigureFailure(WifiStatus status) {
            WifiVendorHal.this.mIWifiChipEventCallback.onChipReconfigureFailure(status);
        }

        @Override // android.hardware.wifi.V1_0.IWifiChipEventCallback
        public void onIfaceAdded(int type, String name) {
            WifiVendorHal.this.mIWifiChipEventCallback.onIfaceAdded(type, name);
        }

        @Override // android.hardware.wifi.V1_0.IWifiChipEventCallback
        public void onIfaceRemoved(int type, String name) {
            WifiVendorHal.this.mIWifiChipEventCallback.onIfaceRemoved(type, name);
        }

        @Override // android.hardware.wifi.V1_0.IWifiChipEventCallback
        public void onDebugRingBufferDataAvailable(WifiDebugRingBufferStatus status, ArrayList<Byte> data) {
            WifiVendorHal.this.mIWifiChipEventCallback.onDebugRingBufferDataAvailable(status, data);
        }

        @Override // android.hardware.wifi.V1_0.IWifiChipEventCallback
        public void onDebugErrorAlert(int errorCode, ArrayList<Byte> debugData) {
            WifiVendorHal.this.mIWifiChipEventCallback.onDebugErrorAlert(errorCode, debugData);
        }

        private boolean areSameIfaceNames(List<IWifiChipEventCallback.IfaceInfo> ifaceList1, List<IWifiChipEventCallback.IfaceInfo> ifaceList2) {
            return ((List) ifaceList1.stream().map($$Lambda$WifiVendorHal$ChipEventCallbackV12$SrMSOw3LUVF_Z64G_aL0Tguwt3A.INSTANCE).collect(Collectors.toList())).containsAll((List) ifaceList2.stream().map($$Lambda$WifiVendorHal$ChipEventCallbackV12$nAuRYe8SQ_MJ2XaULMZNtUE0izo.INSTANCE).collect(Collectors.toList()));
        }

        private boolean areSameIfaces(List<IWifiChipEventCallback.IfaceInfo> ifaceList1, List<IWifiChipEventCallback.IfaceInfo> ifaceList2) {
            return ifaceList1.containsAll(ifaceList2);
        }

        @Override // android.hardware.wifi.V1_2.IWifiChipEventCallback
        public void onRadioModeChange(ArrayList<IWifiChipEventCallback.RadioModeInfo> radioModeInfoList) {
            WifiNative.VendorHalRadioModeChangeEventHandler handler;
            WifiLog wifiLog = WifiVendorHal.this.mVerboseLog;
            wifiLog.d("onRadioModeChange " + radioModeInfoList);
            synchronized (WifiVendorHal.sLock) {
                if (WifiVendorHal.this.mRadioModeChangeEventHandler != null) {
                    if (radioModeInfoList != null) {
                        handler = WifiVendorHal.this.mRadioModeChangeEventHandler;
                    }
                }
                return;
            }
            if (radioModeInfoList.size() == 0 || radioModeInfoList.size() > 2) {
                WifiLog wifiLog2 = WifiVendorHal.this.mLog;
                wifiLog2.e("Unexpected number of radio info in list " + radioModeInfoList.size());
                return;
            }
            IWifiChipEventCallback.RadioModeInfo radioModeInfo0 = radioModeInfoList.get(0);
            IWifiChipEventCallback.RadioModeInfo radioModeInfo1 = radioModeInfoList.size() == 2 ? radioModeInfoList.get(1) : null;
            if (radioModeInfo1 == null || radioModeInfo0.ifaceInfos.size() == radioModeInfo1.ifaceInfos.size()) {
                int numIfacesOnEachRadio = radioModeInfo0.ifaceInfos.size();
                if (numIfacesOnEachRadio == 0 || numIfacesOnEachRadio > 2) {
                    WifiLog wifiLog3 = WifiVendorHal.this.mLog;
                    wifiLog3.e("Unexpected number of iface info in list " + numIfacesOnEachRadio);
                } else if (radioModeInfoList.size() == 2 && numIfacesOnEachRadio == 1) {
                    if (areSameIfaceNames(radioModeInfo0.ifaceInfos, radioModeInfo1.ifaceInfos)) {
                        WifiVendorHal.this.mLog.e("Unexpected for both radio infos to have same iface");
                    } else if (radioModeInfo0.bandInfo != radioModeInfo1.bandInfo) {
                        handler.onDbs();
                    } else {
                        handler.onSbs(radioModeInfo0.bandInfo);
                    }
                } else if (radioModeInfoList.size() != 1 || numIfacesOnEachRadio != 2) {
                } else {
                    if (radioModeInfo0.ifaceInfos.get(0).channel != radioModeInfo0.ifaceInfos.get(1).channel) {
                        handler.onMcc(radioModeInfo0.bandInfo);
                    } else {
                        handler.onScc(radioModeInfo0.bandInfo);
                    }
                }
            } else {
                WifiLog wifiLog4 = WifiVendorHal.this.mLog;
                wifiLog4.e("Unexpected number of iface info in list " + radioModeInfo0.ifaceInfos.size() + ", " + radioModeInfo1.ifaceInfos.size());
            }
        }
    }

    public class HalDeviceManagerStatusListener implements HalDeviceManager.ManagerStatusListener {
        public HalDeviceManagerStatusListener() {
        }

        @Override // com.android.server.wifi.HalDeviceManager.ManagerStatusListener
        public void onStatusChanged() {
            WifiNative.VendorHalDeathEventHandler handler;
            boolean isReady = WifiVendorHal.this.mHalDeviceManager.isReady();
            boolean isStarted = WifiVendorHal.this.mHalDeviceManager.isStarted();
            WifiLog wifiLog = WifiVendorHal.this.mVerboseLog;
            wifiLog.i("Device Manager onStatusChanged. isReady(): " + isReady + ", isStarted(): " + isStarted);
            if (!isReady) {
                synchronized (WifiVendorHal.sLock) {
                    WifiVendorHal.this.clearState();
                    handler = WifiVendorHal.this.mDeathEventHandler;
                }
                if (handler != null) {
                    handler.onDeath();
                }
            }
        }
    }
}
