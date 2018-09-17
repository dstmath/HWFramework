package com.android.server.wifi;

import android.hardware.wifi.V1_0.IWifiApIface;
import android.hardware.wifi.V1_0.IWifiChip;
import android.hardware.wifi.V1_0.IWifiChip.ChipDebugInfo;
import android.hardware.wifi.V1_0.IWifiChipEventCallback;
import android.hardware.wifi.V1_0.IWifiChipEventCallback.Stub;
import android.hardware.wifi.V1_0.IWifiIface;
import android.hardware.wifi.V1_0.IWifiRttController;
import android.hardware.wifi.V1_0.IWifiRttControllerEventCallback;
import android.hardware.wifi.V1_0.IWifiStaIface;
import android.hardware.wifi.V1_0.IWifiStaIface.StaIfaceCapabilityMask;
import android.hardware.wifi.V1_0.IWifiStaIfaceEventCallback;
import android.hardware.wifi.V1_0.RttConfig;
import android.hardware.wifi.V1_0.RttResponder;
import android.hardware.wifi.V1_0.RttResult;
import android.hardware.wifi.V1_0.StaApfPacketFilterCapabilities;
import android.hardware.wifi.V1_0.StaBackgroundScanBucketParameters;
import android.hardware.wifi.V1_0.StaBackgroundScanCapabilities;
import android.hardware.wifi.V1_0.StaBackgroundScanParameters;
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
import android.hardware.wifi.V1_0.WifiStatus;
import android.net.apf.ApfCapabilities;
import android.net.wifi.RttManager;
import android.net.wifi.RttManager.ResponderConfig;
import android.net.wifi.RttManager.RttCapabilities;
import android.net.wifi.RttManager.RttParams;
import android.net.wifi.RttManager.WifiInformationElement;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiLinkLayerStats;
import android.net.wifi.WifiScanner.ScanData;
import android.net.wifi.WifiSsid;
import android.net.wifi.WifiWakeReasonAndCounts;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.system.OsConstants;
import android.util.MutableBoolean;
import android.util.MutableInt;
import com.android.internal.util.ArrayUtils;
import com.android.server.connectivity.KeepalivePacketData;
import com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA.AnonymousClass1;
import com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA.AnonymousClass10;
import com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA.AnonymousClass11;
import com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA.AnonymousClass12;
import com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA.AnonymousClass13;
import com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA.AnonymousClass14;
import com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA.AnonymousClass15;
import com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA.AnonymousClass16;
import com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA.AnonymousClass17;
import com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA.AnonymousClass2;
import com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA.AnonymousClass3;
import com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA.AnonymousClass4;
import com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA.AnonymousClass5;
import com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA.AnonymousClass6;
import com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA.AnonymousClass7;
import com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA.AnonymousClass8;
import com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA.AnonymousClass9;
import com.android.server.wifi.HalDeviceManager.ManagerStatusListener;
import com.android.server.wifi.WifiLog.LogMessage;
import com.android.server.wifi.WifiNative.BucketSettings;
import com.android.server.wifi.WifiNative.ChannelSettings;
import com.android.server.wifi.WifiNative.RingBufferStatus;
import com.android.server.wifi.WifiNative.RoamingCapabilities;
import com.android.server.wifi.WifiNative.RoamingConfig;
import com.android.server.wifi.WifiNative.RttEventHandler;
import com.android.server.wifi.WifiNative.RxFateReport;
import com.android.server.wifi.WifiNative.ScanCapabilities;
import com.android.server.wifi.WifiNative.ScanEventHandler;
import com.android.server.wifi.WifiNative.ScanSettings;
import com.android.server.wifi.WifiNative.TxFateReport;
import com.android.server.wifi.WifiNative.VendorHalDeathEventHandler;
import com.android.server.wifi.WifiNative.WifiLoggerEventHandler;
import com.android.server.wifi.WifiNative.WifiRssiEventHandler;
import com.android.server.wifi.hotspot2.anqp.Constants;
import com.android.server.wifi.util.BitMask;
import com.android.server.wifi.util.InformationElementUtil;
import com.android.server.wifi.util.NativeUtil;
import java.util.ArrayList;
import java.util.Set;

public class WifiVendorHal {
    public static final boolean AP_MODE = false;
    public static final boolean STA_MODE = true;
    private static final int[][] sFeatureCapabilityTranslation = new int[][]{new int[]{2, 128}, new int[]{4, 256}, new int[]{32, 2}, new int[]{StaIfaceCapabilityMask.TDLS, 512}, new int[]{4096, StaIfaceCapabilityMask.TDLS}, new int[]{8192, StaIfaceCapabilityMask.TDLS_OFFCHANNEL}, new int[]{65536, 4}, new int[]{524288, 8}, new int[]{1048576, 8192}, new int[]{2097152, 4096}, new int[]{8388608, 16}, new int[]{16777216, 32}, new int[]{33554432, 64}};
    public static final Object sLock = new Object();
    private static final ApfCapabilities sNoApfCapabilities = new ApfCapabilities(0, 0, 0);
    private static final WifiLog sNoLog = new FakeWifiLog();
    static final int sRssiMonCmdId = 7551;
    Boolean mChannelsForBandSupport = null;
    private VendorHalDeathEventHandler mDeathEventHandler;
    private String mDriverDescription;
    private String mFirmwareDescription;
    private final HalDeviceManager mHalDeviceManager;
    private final HalDeviceManagerStatusListener mHalDeviceManagerStatusCallbacks;
    private final Handler mHalEventHandler;
    private IWifiApIface mIWifiApIface;
    private IWifiChip mIWifiChip;
    private final IWifiChipEventCallback mIWifiChipEventCallback;
    private IWifiRttController mIWifiRttController;
    private IWifiStaIface mIWifiStaIface;
    private final IWifiStaIfaceEventCallback mIWifiStaIfaceEventCallback;
    private int mLastScanCmdId;
    boolean mLinkLayerStatsDebug = false;
    WifiLog mLog = new LogcatLog("WifiVendorHal");
    private WifiLoggerEventHandler mLogEventHandler = null;
    private final Looper mLooper;
    private int mRttCmdId;
    private int mRttCmdIdNext = 1;
    private final RttEventCallback mRttEventCallback;
    private RttEventHandler mRttEventHandler;
    private int mRttResponderCmdId = 0;
    CurrentBackgroundScan mScan = null;
    WifiLog mVerboseLog = sNoLog;
    private WifiRssiEventHandler mWifiRssiEventHandler;

    private class ChipEventCallback extends Stub {
        /* synthetic */ ChipEventCallback(WifiVendorHal this$0, ChipEventCallback -this1) {
            this();
        }

        private ChipEventCallback() {
        }

        public void onChipReconfigured(int modeId) {
            WifiVendorHal.this.mVerboseLog.d("onChipReconfigured " + modeId);
        }

        public void onChipReconfigureFailure(WifiStatus status) {
            WifiVendorHal.this.mVerboseLog.d("onChipReconfigureFailure " + status);
        }

        public void onIfaceAdded(int type, String name) {
            WifiVendorHal.this.mVerboseLog.d("onIfaceAdded " + type + ", name: " + name);
        }

        public void onIfaceRemoved(int type, String name) {
            WifiVendorHal.this.mVerboseLog.d("onIfaceRemoved " + type + ", name: " + name);
        }

        public void onDebugRingBufferDataAvailable(WifiDebugRingBufferStatus status, ArrayList<Byte> data) {
            WifiVendorHal.this.mHalEventHandler.post(new AnonymousClass16(this, status, data));
        }

        /* JADX WARNING: Missing block: B:7:0x000e, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        /* synthetic */ void lambda$-com_android_server_wifi_WifiVendorHal$ChipEventCallback_95528(WifiDebugRingBufferStatus status, ArrayList data) {
            synchronized (WifiVendorHal.sLock) {
                if (WifiVendorHal.this.mLogEventHandler == null || status == null || data == null) {
                } else {
                    WifiLoggerEventHandler eventHandler = WifiVendorHal.this.mLogEventHandler;
                    eventHandler.onRingBufferData(WifiVendorHal.ringBufferStatus(status), NativeUtil.byteArrayFromArrayList(data));
                }
            }
        }

        public void onDebugErrorAlert(int errorCode, ArrayList<Byte> debugData) {
            WifiVendorHal.this.mVerboseLog.d("onDebugErrorAlert " + errorCode);
            WifiVendorHal.this.mHalEventHandler.post(new AnonymousClass17(errorCode, this, debugData));
        }

        /* JADX WARNING: Missing block: B:7:0x000e, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        /* synthetic */ void lambda$-com_android_server_wifi_WifiVendorHal$ChipEventCallback_97191(ArrayList debugData, int errorCode) {
            synchronized (WifiVendorHal.sLock) {
                if (WifiVendorHal.this.mLogEventHandler == null || debugData == null) {
                } else {
                    WifiLoggerEventHandler eventHandler = WifiVendorHal.this.mLogEventHandler;
                    eventHandler.onWifiAlert(errorCode, NativeUtil.byteArrayFromArrayList(debugData));
                }
            }
        }
    }

    class CurrentBackgroundScan {
        public int cmdId;
        public ScanEventHandler eventHandler = null;
        public ScanData[] latestScanResults = null;
        public StaBackgroundScanParameters param;
        public boolean paused = false;
        final /* synthetic */ WifiVendorHal this$0;

        CurrentBackgroundScan(WifiVendorHal this$0, int id, ScanSettings settings) {
            int i = 0;
            this.this$0 = this$0;
            this.cmdId = id;
            this.param = new StaBackgroundScanParameters();
            this.param.basePeriodInMs = settings.base_period_ms;
            this.param.maxApPerScan = settings.max_ap_per_scan;
            this.param.reportThresholdPercent = settings.report_threshold_percent;
            this.param.reportThresholdNumScans = settings.report_threshold_num_scans;
            if (settings.buckets != null) {
                BucketSettings[] bucketSettingsArr = settings.buckets;
                int length = bucketSettingsArr.length;
                while (i < length) {
                    this.param.buckets.add(this$0.makeStaBackgroundScanBucketParametersFromBucketSettings(bucketSettingsArr[i]));
                    i++;
                }
            }
        }
    }

    public class HalDeviceManagerStatusListener implements ManagerStatusListener {
        public void onStatusChanged() {
            boolean isReady = WifiVendorHal.this.mHalDeviceManager.isReady();
            WifiVendorHal.this.mVerboseLog.i("Device Manager onStatusChanged. isReady(): " + isReady + ", isStarted(): " + WifiVendorHal.this.mHalDeviceManager.isStarted());
            if (!isReady) {
                VendorHalDeathEventHandler handler;
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

    private class RttEventCallback extends IWifiRttControllerEventCallback.Stub {
        /* synthetic */ RttEventCallback(WifiVendorHal this$0, RttEventCallback -this1) {
            this();
        }

        private RttEventCallback() {
        }

        /* JADX WARNING: Missing block: B:8:0x0014, code:
            return;
     */
        /* JADX WARNING: Missing block: B:12:0x0022, code:
            r2 = new android.net.wifi.RttManager.RttResult[r8.size()];
            r1 = 0;
     */
        /* JADX WARNING: Missing block: B:14:0x002a, code:
            if (r1 >= r2.length) goto L_0x003e;
     */
        /* JADX WARNING: Missing block: B:15:0x002c, code:
            r2[r1] = com.android.server.wifi.WifiVendorHal.frameworkRttResultFromHalRttResult((android.hardware.wifi.V1_0.RttResult) r8.get(r1));
            r1 = r1 + 1;
     */
        /* JADX WARNING: Missing block: B:19:0x003e, code:
            r0.onRttResults(r2);
     */
        /* JADX WARNING: Missing block: B:20:0x0041, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onResults(int cmdId, ArrayList<RttResult> results) {
            synchronized (WifiVendorHal.sLock) {
                if (cmdId != WifiVendorHal.this.mRttCmdId || WifiVendorHal.this.mRttEventHandler == null) {
                } else {
                    RttEventHandler eventHandler = WifiVendorHal.this.mRttEventHandler;
                    WifiVendorHal.this.mRttCmdId = 0;
                }
            }
        }
    }

    private class StaIfaceEventCallback extends IWifiStaIfaceEventCallback.Stub {
        /* synthetic */ StaIfaceEventCallback(WifiVendorHal this$0, StaIfaceEventCallback -this1) {
            this();
        }

        private StaIfaceEventCallback() {
        }

        /* JADX WARNING: Missing block: B:8:0x002d, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onBackgroundScanFailure(int cmdId) {
            WifiVendorHal.this.mVerboseLog.d("onBackgroundScanFailure " + cmdId);
            synchronized (WifiVendorHal.sLock) {
                if (WifiVendorHal.this.mScan == null || cmdId != WifiVendorHal.this.mScan.cmdId) {
                } else {
                    ScanEventHandler eventHandler = WifiVendorHal.this.mScan.eventHandler;
                    eventHandler.onScanStatus(3);
                }
            }
        }

        /* JADX WARNING: Missing block: B:8:0x002d, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onBackgroundFullScanResult(int cmdId, int bucketsScanned, StaScanResult result) {
            WifiVendorHal.this.mVerboseLog.d("onBackgroundFullScanResult " + cmdId);
            synchronized (WifiVendorHal.sLock) {
                if (WifiVendorHal.this.mScan == null || cmdId != WifiVendorHal.this.mScan.cmdId) {
                } else {
                    ScanEventHandler eventHandler = WifiVendorHal.this.mScan.eventHandler;
                    eventHandler.onFullScanResult(WifiVendorHal.hidlToFrameworkScanResult(result), bucketsScanned);
                }
            }
        }

        /* JADX WARNING: Missing block: B:8:0x002d, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onBackgroundScanResults(int cmdId, ArrayList<StaScanData> scanDatas) {
            WifiVendorHal.this.mVerboseLog.d("onBackgroundScanResults " + cmdId);
            synchronized (WifiVendorHal.sLock) {
                if (WifiVendorHal.this.mScan == null || cmdId != WifiVendorHal.this.mScan.cmdId) {
                } else {
                    ScanEventHandler eventHandler = WifiVendorHal.this.mScan.eventHandler;
                    WifiVendorHal.this.mScan.latestScanResults = WifiVendorHal.hidlToFrameworkScanDatas(cmdId, scanDatas);
                    eventHandler.onScanStatus(0);
                }
            }
        }

        /* JADX WARNING: Missing block: B:8:0x0036, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onRssiThresholdBreached(int cmdId, byte[] currBssid, int currRssi) {
            WifiVendorHal.this.mVerboseLog.d("onRssiThresholdBreached " + cmdId + "currRssi " + currRssi);
            synchronized (WifiVendorHal.sLock) {
                if (WifiVendorHal.this.mWifiRssiEventHandler == null || cmdId != WifiVendorHal.sRssiMonCmdId) {
                } else {
                    WifiRssiEventHandler eventHandler = WifiVendorHal.this.mWifiRssiEventHandler;
                    eventHandler.onRssiThresholdBreached((byte) currRssi);
                }
            }
        }
    }

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

    private LogMessage enter(String format) {
        if (this.mVerboseLog == sNoLog) {
            return sNoLog.info(format);
        }
        return this.mVerboseLog.trace("% " + format).c(Thread.currentThread().getStackTrace()[3].getMethodName());
    }

    private static String niceMethodName(StackTraceElement[] trace, int start) {
        if (start >= trace.length) {
            return "";
        }
        StackTraceElement s = trace[start];
        String name = s.getMethodName();
        if (name.contains("lambda$")) {
            String myFile = s.getFileName();
            if (myFile != null) {
                for (int i = start + 1; i < trace.length; i++) {
                    if (myFile.equals(trace[i].getFileName())) {
                        name = trace[i].getMethodName();
                        break;
                    }
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
        this.mIWifiStaIfaceEventCallback = new StaIfaceEventCallback(this, null);
        this.mIWifiChipEventCallback = new ChipEventCallback(this, null);
        this.mRttEventCallback = new RttEventCallback(this, null);
    }

    private void handleRemoteException(RemoteException e) {
        this.mVerboseLog.err("% RemoteException in HIDL call %").c(niceMethodName(Thread.currentThread().getStackTrace(), 3)).c(e.toString()).flush();
        clearState();
    }

    public boolean initialize(VendorHalDeathEventHandler handler) {
        synchronized (sLock) {
            this.mHalDeviceManager.initialize();
            this.mHalDeviceManager.registerStatusListener(this.mHalDeviceManagerStatusCallbacks, this.mLooper);
            this.mDeathEventHandler = handler;
        }
        return true;
    }

    public boolean isVendorHalSupported() {
        boolean isSupported;
        synchronized (sLock) {
            isSupported = this.mHalDeviceManager.isSupported();
        }
        return isSupported;
    }

    public boolean startVendorHalAp() {
        return startVendorHal(false);
    }

    public boolean startVendorHalSta() {
        return startVendorHal(true);
    }

    public boolean startVendorHal(boolean isStaMode) {
        synchronized (sLock) {
            boolean boolResult;
            if (this.mIWifiStaIface != null) {
                boolResult = boolResult(false);
                return boolResult;
            } else if (this.mIWifiApIface != null) {
                boolResult = boolResult(false);
                return boolResult;
            } else if (this.mHalDeviceManager.start()) {
                IWifiIface iface;
                if (isStaMode) {
                    this.mIWifiStaIface = this.mHalDeviceManager.createStaIface(null, null);
                    if (this.mIWifiStaIface == null) {
                        boolResult = startFailedTo("create STA Iface");
                        return boolResult;
                    }
                    iface = this.mIWifiStaIface;
                    if (registerStaIfaceCallback()) {
                        this.mIWifiRttController = this.mHalDeviceManager.createRttController(iface);
                        if (this.mIWifiRttController == null) {
                            boolResult = startFailedTo("create RTT controller");
                            return boolResult;
                        } else if (registerRttEventCallback()) {
                            enableLinkLayerStats();
                        } else {
                            boolResult = startFailedTo("register RTT iface callback");
                            return boolResult;
                        }
                    }
                    boolResult = startFailedTo("register sta iface callback");
                    return boolResult;
                }
                this.mIWifiApIface = this.mHalDeviceManager.createApIface(null, null);
                if (this.mIWifiApIface == null) {
                    boolResult = startFailedTo("create AP Iface");
                    return boolResult;
                }
                iface = this.mIWifiApIface;
                this.mIWifiChip = this.mHalDeviceManager.getChip(iface);
                if (this.mIWifiChip == null) {
                    boolResult = startFailedTo("get the chip created for the Iface");
                    return boolResult;
                } else if (registerChipCallback()) {
                    this.mLog.i("Vendor Hal started successfully");
                    return true;
                } else {
                    boolResult = startFailedTo("register chip callback");
                    return boolResult;
                }
            } else {
                boolResult = startFailedTo("start the vendor HAL");
                return boolResult;
            }
        }
    }

    private boolean startFailedTo(String message) {
        this.mVerboseLog.err("Failed to %. Vendor Hal start failed").c(message).flush();
        this.mHalDeviceManager.stop();
        clearState();
        return false;
    }

    private boolean registerStaIfaceCallback() {
        synchronized (sLock) {
            boolean boolResult;
            if (this.mIWifiStaIface == null) {
                boolResult = boolResult(false);
                return boolResult;
            } else if (this.mIWifiStaIfaceEventCallback == null) {
                boolResult = boolResult(false);
                return boolResult;
            } else {
                try {
                    boolResult = ok(this.mIWifiStaIface.registerEventCallback(this.mIWifiStaIfaceEventCallback));
                    return boolResult;
                } catch (RemoteException e) {
                    handleRemoteException(e);
                    return false;
                }
            }
        }
    }

    private boolean registerChipCallback() {
        synchronized (sLock) {
            boolean boolResult;
            if (this.mIWifiChip == null) {
                boolResult = boolResult(false);
                return boolResult;
            } else if (this.mIWifiChipEventCallback == null) {
                boolResult = boolResult(false);
                return boolResult;
            } else {
                try {
                    boolResult = ok(this.mIWifiChip.registerEventCallback(this.mIWifiChipEventCallback));
                    return boolResult;
                } catch (RemoteException e) {
                    handleRemoteException(e);
                    return false;
                }
            }
        }
    }

    private boolean registerRttEventCallback() {
        synchronized (sLock) {
            boolean boolResult;
            if (this.mIWifiRttController == null) {
                boolResult = boolResult(false);
                return boolResult;
            }
            try {
                boolResult = ok(this.mIWifiRttController.registerEventCallback(this.mRttEventCallback));
                return boolResult;
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
            this.mLog.i("Vendor Hal stopped");
        }
    }

    private void clearState() {
        this.mIWifiChip = null;
        this.mIWifiStaIface = null;
        this.mIWifiApIface = null;
        this.mIWifiRttController = null;
        this.mDriverDescription = null;
        this.mFirmwareDescription = null;
        this.mChannelsForBandSupport = null;
    }

    public boolean isHalStarted() {
        boolean z = true;
        synchronized (sLock) {
            if (this.mIWifiStaIface == null && this.mIWifiApIface == null) {
                z = false;
            }
        }
        return z;
    }

    public boolean getBgScanCapabilities(ScanCapabilities capabilities) {
        synchronized (sLock) {
            boolean boolResult;
            if (this.mIWifiStaIface == null) {
                boolResult = boolResult(false);
                return boolResult;
            }
            try {
                MutableBoolean ans = new MutableBoolean(false);
                ScanCapabilities out = capabilities;
                this.mIWifiStaIface.getBackgroundScanCapabilities(new AnonymousClass12(this, capabilities, ans));
                boolResult = ans.value;
                return boolResult;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    /* synthetic */ void lambda$-com_android_server_wifi_WifiVendorHal_16028(ScanCapabilities out, MutableBoolean ans, WifiStatus status, StaBackgroundScanCapabilities cap) {
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

    private StaBackgroundScanBucketParameters makeStaBackgroundScanBucketParametersFromBucketSettings(BucketSettings bs) {
        StaBackgroundScanBucketParameters pa = new StaBackgroundScanBucketParameters();
        pa.bucketIdx = bs.bucket;
        pa.band = makeWifiBandFromFrameworkBand(bs.band);
        if (bs.channels != null) {
            for (ChannelSettings cs : bs.channels) {
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
        switch (frameworkBand) {
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
            case 6:
                return 6;
            case 7:
                return 7;
            default:
                throw new IllegalArgumentException("bad band " + frameworkBand);
        }
    }

    private int makeReportSchemeFromBucketSettingsReportEvents(int reportUnderscoreEvents) {
        int ans = 0;
        BitMask in = new BitMask(reportUnderscoreEvents);
        if (in.testAndClear(1)) {
            ans = 1;
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

    public boolean startBgScan(ScanSettings settings, ScanEventHandler eventHandler) {
        if (eventHandler == null) {
            return boolResult(false);
        }
        synchronized (sLock) {
            if (this.mIWifiStaIface == null) {
                boolean boolResult = boolResult(false);
                return boolResult;
            }
            try {
                if (!(this.mScan == null || (this.mScan.paused ^ 1) == 0)) {
                    ok(this.mIWifiStaIface.stopBackgroundScan(this.mScan.cmdId));
                    this.mScan = null;
                }
                this.mLastScanCmdId = (this.mLastScanCmdId % 9) + 1;
                CurrentBackgroundScan scan = new CurrentBackgroundScan(this, this.mLastScanCmdId, settings);
                if (ok(this.mIWifiStaIface.startBackgroundScan(scan.cmdId, scan.param))) {
                    scan.eventHandler = eventHandler;
                    this.mScan = scan;
                    return true;
                }
                return false;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    public void stopBgScan() {
        synchronized (sLock) {
            if (this.mIWifiStaIface == null) {
                return;
            }
            try {
                if (this.mScan != null) {
                    ok(this.mIWifiStaIface.stopBackgroundScan(this.mScan.cmdId));
                    this.mScan = null;
                }
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void pauseBgScan() {
        synchronized (sLock) {
            try {
                if (this.mIWifiStaIface == null) {
                    return;
                } else if (!(this.mScan == null || (this.mScan.paused ^ 1) == 0)) {
                    if (ok(this.mIWifiStaIface.stopBackgroundScan(this.mScan.cmdId))) {
                        this.mScan.paused = true;
                    } else {
                        return;
                    }
                }
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void restartBgScan() {
        synchronized (sLock) {
            if (this.mIWifiStaIface == null) {
                return;
            }
            try {
                if (this.mScan != null && this.mScan.paused) {
                    if (ok(this.mIWifiStaIface.startBackgroundScan(this.mScan.cmdId, this.mScan.param))) {
                        this.mScan.paused = false;
                    } else {
                        return;
                    }
                }
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public ScanData[] getBgScanResults() {
        synchronized (sLock) {
            if (this.mIWifiStaIface == null) {
                return null;
            } else if (this.mScan == null) {
                return null;
            } else {
                ScanData[] scanDataArr = this.mScan.latestScanResults;
                return scanDataArr;
            }
        }
    }

    public WifiLinkLayerStats getWifiLinkLayerStats() {
        AnonymousClass1AnswerBox answer = new Object() {
            public StaLinkLayerStats value = null;
        };
        synchronized (sLock) {
            try {
                if (this.mIWifiStaIface == null) {
                    return null;
                }
                this.mIWifiStaIface.getLinkLayerStats(new AnonymousClass10(this, answer));
                return frameworkFromHalLinkLayerStats(answer.value);
            } catch (RemoteException e) {
                handleRemoteException(e);
                return null;
            }
        }
    }

    /* synthetic */ void lambda$-com_android_server_wifi_WifiVendorHal_25591(AnonymousClass1AnswerBox answer, WifiStatus status, StaLinkLayerStats stats) {
        if (ok(status)) {
            answer.value = stats;
        }
    }

    static WifiLinkLayerStats frameworkFromHalLinkLayerStats(StaLinkLayerStats stats) {
        if (stats == null) {
            return null;
        }
        WifiLinkLayerStats out = new WifiLinkLayerStats();
        out.beacon_rx = stats.iface.beaconRx;
        out.rssi_mgmt = stats.iface.avgRssiMgmt;
        out.rxmpdu_be = stats.iface.wmeBePktStats.rxMpdu;
        out.txmpdu_be = stats.iface.wmeBePktStats.txMpdu;
        out.lostmpdu_be = stats.iface.wmeBePktStats.lostMpdu;
        out.retries_be = stats.iface.wmeBePktStats.retries;
        out.rxmpdu_bk = stats.iface.wmeBkPktStats.rxMpdu;
        out.txmpdu_bk = stats.iface.wmeBkPktStats.txMpdu;
        out.lostmpdu_bk = stats.iface.wmeBkPktStats.lostMpdu;
        out.retries_bk = stats.iface.wmeBkPktStats.retries;
        out.rxmpdu_vi = stats.iface.wmeViPktStats.rxMpdu;
        out.txmpdu_vi = stats.iface.wmeViPktStats.txMpdu;
        out.lostmpdu_vi = stats.iface.wmeViPktStats.lostMpdu;
        out.retries_vi = stats.iface.wmeViPktStats.retries;
        out.rxmpdu_vo = stats.iface.wmeVoPktStats.rxMpdu;
        out.txmpdu_vo = stats.iface.wmeVoPktStats.txMpdu;
        out.lostmpdu_vo = stats.iface.wmeVoPktStats.lostMpdu;
        out.retries_vo = stats.iface.wmeVoPktStats.retries;
        if (stats.radios.size() > 0) {
            StaLinkLayerRadioStats radioStats = (StaLinkLayerRadioStats) stats.radios.get(0);
            out.on_time = radioStats.onTimeInMs;
            out.tx_time = radioStats.txTimeInMs;
            out.tx_time_per_level = new int[radioStats.txTimeInMsPerLevel.size()];
            for (int i = 0; i < out.tx_time_per_level.length; i++) {
                out.tx_time_per_level[i] = ((Integer) radioStats.txTimeInMsPerLevel.get(i)).intValue();
            }
            out.rx_time = radioStats.rxTimeInMs;
            out.on_time_scan = radioStats.onTimeInMsForScan;
        }
        return out;
    }

    private void enableLinkLayerStats() {
        synchronized (sLock) {
            try {
                if (!ok(this.mIWifiStaIface.enableLinkLayerStatsCollection(this.mLinkLayerStatsDebug))) {
                    this.mLog.e("unable to enable link layer stats collection");
                }
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
        return;
    }

    int wifiFeatureMaskFromStaCapabilities(int capabilities) {
        int features = 0;
        for (int i = 0; i < sFeatureCapabilityTranslation.length; i++) {
            if ((sFeatureCapabilityTranslation[i][1] & capabilities) != 0) {
                features |= sFeatureCapabilityTranslation[i][0];
            }
        }
        return features;
    }

    public int getSupportedFeatureSet() {
        try {
            MutableInt feat = new MutableInt(0);
            synchronized (sLock) {
                if (this.mIWifiStaIface != null) {
                    this.mIWifiStaIface.getCapabilities(new AnonymousClass9(this, feat));
                }
            }
            int featureSet = feat.value;
            Set<Integer> supportedIfaceTypes = this.mHalDeviceManager.getSupportedIfaceTypes();
            if (supportedIfaceTypes.contains(Integer.valueOf(0))) {
                featureSet |= 1;
            }
            if (supportedIfaceTypes.contains(Integer.valueOf(1))) {
                featureSet |= 16;
            }
            if (supportedIfaceTypes.contains(Integer.valueOf(2))) {
                featureSet |= 8;
            }
            if (supportedIfaceTypes.contains(Integer.valueOf(3))) {
                featureSet |= 64;
            }
            return featureSet;
        } catch (RemoteException e) {
            handleRemoteException(e);
            return 0;
        }
    }

    /* synthetic */ void lambda$-com_android_server_wifi_WifiVendorHal_32009(MutableInt feat, WifiStatus status, int capabilities) {
        if (ok(status)) {
            feat.value = wifiFeatureMaskFromStaCapabilities(capabilities);
        }
    }

    public RttCapabilities getRttCapabilities() {
        synchronized (sLock) {
            if (this.mIWifiRttController == null) {
                return null;
            }
            try {
                AnonymousClass2AnswerBox box = new Object() {
                    public RttCapabilities value = null;
                };
                this.mIWifiRttController.getCapabilities(new AnonymousClass6(this, box));
                RttCapabilities rttCapabilities = box.value;
                return rttCapabilities;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return null;
            }
        }
    }

    /* synthetic */ void lambda$-com_android_server_wifi_WifiVendorHal_33500(AnonymousClass2AnswerBox box, WifiStatus status, android.hardware.wifi.V1_0.RttCapabilities capabilities) {
        if (ok(status)) {
            this.mVerboseLog.info("rtt capabilites %").c(capabilities.toString()).flush();
            RttCapabilities ans = new RttCapabilities();
            ans.oneSidedRttSupported = capabilities.rttOneSidedSupported;
            ans.twoSided11McRttSupported = capabilities.rttFtmSupported;
            ans.lciSupported = capabilities.lciSupported;
            ans.lcrSupported = capabilities.lcrSupported;
            ans.preambleSupported = frameworkPreambleFromHalPreamble(capabilities.preambleSupport);
            ans.bwSupported = frameworkBwFromHalBw(capabilities.bwSupport);
            ans.responderSupported = capabilities.responderSupported;
            ans.secureRttSupported = false;
            ans.mcVersion = capabilities.mcVersion & Constants.BYTE_MASK;
            box.value = ans;
        }
    }

    static RttManager.RttResult frameworkRttResultFromHalRttResult(RttResult result) {
        RttManager.RttResult ans = new RttManager.RttResult();
        ans.bssid = NativeUtil.macAddressFromByteArray(result.addr);
        ans.burstNumber = result.burstNum;
        ans.measurementFrameNumber = result.measurementNumber;
        ans.successMeasurementFrameNumber = result.successNumber;
        ans.frameNumberPerBurstPeer = result.numberPerBurstPeer;
        ans.status = result.status;
        ans.retryAfterDuration = result.retryAfterDuration;
        ans.measurementType = result.type;
        ans.rssi = result.rssi;
        ans.rssiSpread = result.rssiSpread;
        ans.txRate = result.txRate.bitRateInKbps;
        ans.rxRate = result.rxRate.bitRateInKbps;
        ans.rtt = result.rtt;
        ans.rttStandardDeviation = result.rttSd;
        ans.rttSpread = result.rttSpread;
        ans.distance = result.distanceInMm / 10;
        ans.distanceStandardDeviation = result.distanceSdInMm / 10;
        ans.distanceSpread = result.distanceSpreadInMm / 10;
        ans.ts = result.timeStampInUs;
        ans.burstDuration = result.burstDurationInMs;
        ans.negotiatedBurstNum = result.negotiatedBurstNum;
        ans.LCI = ieFromHal(result.lci);
        ans.LCR = ieFromHal(result.lcr);
        ans.secure = false;
        return ans;
    }

    static WifiInformationElement ieFromHal(android.hardware.wifi.V1_0.WifiInformationElement ie) {
        if (ie == null) {
            return null;
        }
        WifiInformationElement ans = new WifiInformationElement();
        ans.id = ie.id;
        ans.data = NativeUtil.byteArrayFromArrayList(ie.data);
        return ans;
    }

    static RttConfig halRttConfigFromFrameworkRttParams(RttParams params) {
        RttConfig rttConfig = new RttConfig();
        if (params.bssid != null) {
            byte[] addr = NativeUtil.macAddressToByteArray(params.bssid);
            for (int i = 0; i < rttConfig.addr.length; i++) {
                rttConfig.addr[i] = addr[i];
            }
        }
        rttConfig.type = halRttTypeFromFrameworkRttType(params.requestType);
        rttConfig.peer = halPeerFromFrameworkPeer(params.deviceType);
        rttConfig.channel.width = halChannelWidthFromFrameworkChannelWidth(params.channelWidth);
        rttConfig.channel.centerFreq = params.frequency;
        rttConfig.channel.centerFreq0 = params.centerFreq0;
        rttConfig.channel.centerFreq1 = params.centerFreq1;
        rttConfig.burstPeriod = params.interval;
        rttConfig.numBurst = params.numberBurst;
        rttConfig.numFramesPerBurst = params.numSamplesPerBurst;
        rttConfig.numRetriesPerRttFrame = params.numRetriesPerMeasurementFrame;
        rttConfig.numRetriesPerFtmr = params.numRetriesPerFTMR;
        rttConfig.mustRequestLci = params.LCIRequest;
        rttConfig.mustRequestLcr = params.LCRRequest;
        rttConfig.burstDuration = params.burstTimeout;
        rttConfig.preamble = halPreambleFromFrameworkPreamble(params.preamble);
        rttConfig.bw = halBwFromFrameworkBw(params.bandwidth);
        return rttConfig;
    }

    static int halRttTypeFromFrameworkRttType(int frameworkRttType) {
        switch (frameworkRttType) {
            case 1:
                return 1;
            case 2:
                return 2;
            default:
                throw new IllegalArgumentException("bad " + frameworkRttType);
        }
    }

    static int frameworkRttTypeFromHalRttType(int halType) {
        switch (halType) {
            case 1:
                return 1;
            case 2:
                return 2;
            default:
                throw new IllegalArgumentException("bad " + halType);
        }
    }

    static int halPeerFromFrameworkPeer(int frameworkPeer) {
        switch (frameworkPeer) {
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
            default:
                throw new IllegalArgumentException("bad " + frameworkPeer);
        }
    }

    static int frameworkPeerFromHalPeer(int halPeer) {
        switch (halPeer) {
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
            default:
                throw new IllegalArgumentException("bad " + halPeer);
        }
    }

    static int halChannelWidthFromFrameworkChannelWidth(int frameworkChannelWidth) {
        switch (frameworkChannelWidth) {
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
            default:
                throw new IllegalArgumentException("bad " + frameworkChannelWidth);
        }
    }

    static int frameworkChannelWidthFromHalChannelWidth(int halChannelWidth) {
        switch (halChannelWidth) {
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
            default:
                throw new IllegalArgumentException("bad " + halChannelWidth);
        }
    }

    static int halPreambleFromFrameworkPreamble(int rttManagerPreamble) {
        BitMask checkoff = new BitMask(rttManagerPreamble);
        int flags = 0;
        if (checkoff.testAndClear(1)) {
            flags = 1;
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
        throw new IllegalArgumentException("bad " + rttManagerPreamble);
    }

    static int frameworkPreambleFromHalPreamble(int halPreamble) {
        BitMask checkoff = new BitMask(halPreamble);
        int flags = 0;
        if (checkoff.testAndClear(1)) {
            flags = 1;
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
        throw new IllegalArgumentException("bad " + halPreamble);
    }

    static int halBwFromFrameworkBw(int rttManagerBandwidth) {
        BitMask checkoff = new BitMask(rttManagerBandwidth);
        int flags = 0;
        if (checkoff.testAndClear(1)) {
            flags = 1;
        }
        if (checkoff.testAndClear(2)) {
            flags |= 2;
        }
        if (checkoff.testAndClear(4)) {
            flags |= 4;
        }
        if (checkoff.testAndClear(8)) {
            flags |= 8;
        }
        if (checkoff.testAndClear(16)) {
            flags |= 16;
        }
        if (checkoff.testAndClear(32)) {
            flags |= 32;
        }
        if (checkoff.value == 0) {
            return flags;
        }
        throw new IllegalArgumentException("bad " + rttManagerBandwidth);
    }

    static int frameworkBwFromHalBw(int rttBw) {
        BitMask checkoff = new BitMask(rttBw);
        int flags = 0;
        if (checkoff.testAndClear(1)) {
            flags = 1;
        }
        if (checkoff.testAndClear(2)) {
            flags |= 2;
        }
        if (checkoff.testAndClear(4)) {
            flags |= 4;
        }
        if (checkoff.testAndClear(8)) {
            flags |= 8;
        }
        if (checkoff.testAndClear(16)) {
            flags |= 16;
        }
        if (checkoff.testAndClear(32)) {
            flags |= 32;
        }
        if (checkoff.value == 0) {
            return flags;
        }
        throw new IllegalArgumentException("bad " + rttBw);
    }

    static ArrayList<RttConfig> halRttConfigArrayFromFrameworkRttParamsArray(RttParams[] params) {
        ArrayList<RttConfig> configs = new ArrayList(length);
        for (RttParams halRttConfigFromFrameworkRttParams : params) {
            RttConfig config = halRttConfigFromFrameworkRttParams(halRttConfigFromFrameworkRttParams);
            if (config != null) {
                configs.add(config);
            }
        }
        return configs;
    }

    public boolean requestRtt(RttParams[] params, RttEventHandler handler) {
        try {
            ArrayList<RttConfig> rttConfigs = halRttConfigArrayFromFrameworkRttParamsArray(params);
            synchronized (sLock) {
                boolean boolResult;
                if (this.mIWifiRttController == null) {
                    boolResult = boolResult(false);
                    return boolResult;
                } else if (this.mRttCmdId != 0) {
                    boolResult = boolResult(false);
                    return boolResult;
                } else {
                    int i = this.mRttCmdIdNext;
                    this.mRttCmdIdNext = i + 1;
                    this.mRttCmdId = i;
                    this.mRttEventHandler = handler;
                    if (this.mRttCmdIdNext <= 0) {
                        this.mRttCmdIdNext = 1;
                    }
                    try {
                        if (ok(this.mIWifiRttController.rangeRequest(this.mRttCmdId, rttConfigs))) {
                            return true;
                        }
                        this.mRttCmdId = 0;
                        return false;
                    } catch (RemoteException e) {
                        handleRemoteException(e);
                        return false;
                    }
                }
            }
        } catch (IllegalArgumentException e2) {
            this.mLog.err("Illegal argument for RTT request").c(e2.toString()).flush();
            return false;
        }
    }

    public boolean cancelRtt(RttParams[] params) {
        ArrayList<RttConfig> rttConfigs = halRttConfigArrayFromFrameworkRttParamsArray(params);
        synchronized (sLock) {
            boolean boolResult;
            if (this.mIWifiRttController == null) {
                boolResult = boolResult(false);
                return boolResult;
            } else if (this.mRttCmdId == 0) {
                boolResult = boolResult(false);
                return boolResult;
            } else {
                ArrayList<byte[]> addrs = new ArrayList(rttConfigs.size());
                for (RttConfig x : rttConfigs) {
                    addrs.add(x.addr);
                }
                try {
                    WifiStatus status = this.mIWifiRttController.rangeCancel(this.mRttCmdId, addrs);
                    this.mRttCmdId = 0;
                    if (ok(status)) {
                        return true;
                    }
                    return false;
                } catch (RemoteException e) {
                    handleRemoteException(e);
                    return false;
                }
            }
        }
    }

    private RttResponder getRttResponder() {
        synchronized (sLock) {
            if (this.mIWifiRttController == null) {
                return null;
            }
            AnonymousClass3AnswerBox answer = new Object() {
                public RttResponder value = null;
            };
            try {
                this.mIWifiRttController.getResponderInfo(new AnonymousClass7(this, answer));
                RttResponder rttResponder = answer.value;
                return rttResponder;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return null;
            }
        }
    }

    /* synthetic */ void lambda$-com_android_server_wifi_WifiVendorHal_49882(AnonymousClass3AnswerBox answer, WifiStatus status, RttResponder info) {
        if (ok(status)) {
            answer.value = info;
        }
    }

    private ResponderConfig frameworkResponderConfigFromHalRttResponder(RttResponder info) {
        ResponderConfig config = new ResponderConfig();
        config.frequency = info.channel.centerFreq;
        config.centerFreq0 = info.channel.centerFreq0;
        config.centerFreq1 = info.channel.centerFreq1;
        config.channelWidth = frameworkChannelWidthFromHalChannelWidth(info.channel.width);
        config.preamble = frameworkPreambleFromHalPreamble(info.preamble);
        return config;
    }

    /* JADX WARNING: Missing block: B:23:0x0059, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ResponderConfig enableRttResponder(int timeoutSeconds) {
        RttResponder info = getRttResponder();
        synchronized (sLock) {
            if (this.mIWifiRttController == null) {
                return null;
            } else if (this.mRttResponderCmdId != 0) {
                this.mLog.e("responder mode already enabled - this shouldn't happen");
                return null;
            } else {
                ResponderConfig config = null;
                int id = this.mRttCmdIdNext;
                this.mRttCmdIdNext = id + 1;
                if (this.mRttCmdIdNext <= 0) {
                    this.mRttCmdIdNext = 1;
                }
                try {
                    if (ok(this.mIWifiRttController.enableResponder(id, null, timeoutSeconds, info))) {
                        this.mRttResponderCmdId = id;
                        config = frameworkResponderConfigFromHalRttResponder(info);
                        this.mVerboseLog.i("enabling rtt " + this.mRttResponderCmdId);
                    }
                } catch (RemoteException e) {
                    handleRemoteException(e);
                    return null;
                }
            }
        }
    }

    public boolean disableRttResponder() {
        synchronized (sLock) {
            boolean boolResult;
            if (this.mIWifiRttController == null) {
                boolResult = boolResult(false);
                return boolResult;
            } else if (this.mRttResponderCmdId == 0) {
                boolResult = boolResult(false);
                return boolResult;
            } else {
                try {
                    WifiStatus status = this.mIWifiRttController.disableResponder(this.mRttResponderCmdId);
                    this.mRttResponderCmdId = 0;
                    if (ok(status)) {
                        return true;
                    }
                    return false;
                } catch (RemoteException e) {
                    handleRemoteException(e);
                    return false;
                }
            }
        }
    }

    public boolean setScanningMacOui(byte[] oui) {
        if (oui == null) {
            return boolResult(false);
        }
        if (oui.length != 3) {
            return boolResult(false);
        }
        synchronized (sLock) {
            try {
                if (this.mIWifiStaIface == null) {
                    boolean boolResult = boolResult(false);
                    return boolResult;
                } else if (ok(this.mIWifiStaIface.setScanningMacOui(oui))) {
                    return true;
                } else {
                    return false;
                }
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    public int[] getChannelsForBand(int band) {
        int[] iArr;
        enter("%").c((long) band).flush();
        synchronized (sLock) {
            try {
                AnonymousClass4AnswerBox box = new Object() {
                    public int[] value = null;
                };
                int hb = makeWifiBandFromFrameworkBand(band);
                if (this.mIWifiStaIface != null) {
                    this.mIWifiStaIface.getValidFrequenciesForBand(hb, new AnonymousClass11(this, box));
                } else if (this.mIWifiApIface != null) {
                    this.mIWifiApIface.getValidFrequenciesForBand(hb, new AnonymousClass1(this, box));
                }
                iArr = box.value;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return null;
            }
        }
        return iArr;
    }

    /* synthetic */ void lambda$-com_android_server_wifi_WifiVendorHal_54636(AnonymousClass4AnswerBox box, WifiStatus status, ArrayList frequencies) {
        if (status.code == 4) {
            this.mChannelsForBandSupport = Boolean.valueOf(false);
        }
        if (ok(status)) {
            this.mChannelsForBandSupport = Boolean.valueOf(true);
            box.value = intArrayFromArrayList(frequencies);
        }
    }

    /* synthetic */ void lambda$-com_android_server_wifi_WifiVendorHal_55149(AnonymousClass4AnswerBox box, WifiStatus status, ArrayList frequencies) {
        if (status.code == 4) {
            this.mChannelsForBandSupport = Boolean.valueOf(false);
        }
        if (ok(status)) {
            this.mChannelsForBandSupport = Boolean.valueOf(true);
            box.value = intArrayFromArrayList(frequencies);
        }
    }

    private int[] intArrayFromArrayList(ArrayList<Integer> in) {
        int[] ans = new int[in.size()];
        int i = 0;
        for (Integer e : in) {
            int i2 = i + 1;
            ans[i] = e.intValue();
            i = i2;
        }
        return ans;
    }

    public boolean isGetChannelsForBandSupported() {
        if (this.mChannelsForBandSupport != null) {
            return this.mChannelsForBandSupport.booleanValue();
        }
        getChannelsForBand(1);
        if (this.mChannelsForBandSupport != null) {
            return this.mChannelsForBandSupport.booleanValue();
        }
        return false;
    }

    public ApfCapabilities getApfCapabilities() {
        synchronized (sLock) {
            try {
                ApfCapabilities apfCapabilities;
                if (this.mIWifiStaIface == null) {
                    this.mLog.e("mIWifiStaIface is null, getApfCapabilities(0, 0, 0)");
                    apfCapabilities = sNoApfCapabilities;
                    return apfCapabilities;
                }
                AnonymousClass5AnswerBox box = new Object() {
                    public ApfCapabilities value = WifiVendorHal.sNoApfCapabilities;
                };
                this.mIWifiStaIface.getApfPacketFilterCapabilities(new AnonymousClass8(this, box));
                apfCapabilities = box.value;
                return apfCapabilities;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return sNoApfCapabilities;
            }
        }
    }

    /* synthetic */ void lambda$-com_android_server_wifi_WifiVendorHal_57159(AnonymousClass5AnswerBox box, WifiStatus status, StaApfPacketFilterCapabilities capabilities) {
        if (ok(status)) {
            box.value = new ApfCapabilities(capabilities.version, capabilities.maxLength, OsConstants.ARPHRD_ETHER);
            this.mLog.d("getApfCapabilities:(version:" + capabilities.version + ", maxLength:" + capabilities.maxLength + ", " + OsConstants.ARPHRD_ETHER + ")");
            return;
        }
        this.mLog.e("getApfCapabilities failed");
    }

    public boolean installPacketFilter(byte[] filter) {
        if (filter == null) {
            return boolResult(false);
        }
        ArrayList<Byte> program = NativeUtil.byteArrayToArrayList(filter);
        enter("filter length %").c((long) filter.length).flush();
        synchronized (sLock) {
            try {
                if (this.mIWifiStaIface == null) {
                    boolean boolResult = boolResult(false);
                    return boolResult;
                } else if (ok(this.mIWifiStaIface.installApfPacketFilter(0, program))) {
                    return true;
                } else {
                    return false;
                }
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    public boolean setCountryCodeHal(String countryCode) {
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
                    if (this.mIWifiApIface == null) {
                        boolean boolResult = boolResult(false);
                        return boolResult;
                    } else if (ok(this.mIWifiApIface.setCountryCode(code))) {
                        return true;
                    } else {
                        return false;
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

    public boolean setLoggingEventHandler(WifiLoggerEventHandler handler) {
        if (handler == null) {
            return boolResult(false);
        }
        synchronized (sLock) {
            boolean boolResult;
            if (this.mIWifiChip == null) {
                boolResult = boolResult(false);
                return boolResult;
            } else if (this.mLogEventHandler != null) {
                boolResult = boolResult(false);
                return boolResult;
            } else {
                try {
                    if (ok(this.mIWifiChip.enableDebugErrorAlerts(true))) {
                        this.mLogEventHandler = handler;
                        return true;
                    }
                    return false;
                } catch (RemoteException e) {
                    handleRemoteException(e);
                    return false;
                }
            }
        }
    }

    public boolean resetLogHandler() {
        synchronized (sLock) {
            boolean boolResult;
            if (this.mIWifiChip == null) {
                boolResult = boolResult(false);
                return boolResult;
            } else if (this.mLogEventHandler == null) {
                boolResult = boolResult(false);
                return boolResult;
            } else {
                try {
                    if (!ok(this.mIWifiChip.enableDebugErrorAlerts(false))) {
                        return false;
                    } else if (ok(this.mIWifiChip.stopLoggingToDebugRingBuffer())) {
                        this.mLogEventHandler = null;
                        return true;
                    } else {
                        return false;
                    }
                } catch (RemoteException e) {
                    handleRemoteException(e);
                    return false;
                }
            }
        }
    }

    public boolean startLoggingRingBuffer(int verboseLevel, int flags, int maxIntervalInSec, int minDataSizeInBytes, String ringName) {
        enter("verboseLevel=%, flags=%, maxIntervalInSec=%, minDataSizeInBytes=%, ringName=%").c((long) verboseLevel).c((long) flags).c((long) maxIntervalInSec).c((long) minDataSizeInBytes).c(ringName).flush();
        synchronized (sLock) {
            boolean boolResult;
            if (this.mIWifiChip == null) {
                boolResult = boolResult(false);
                return boolResult;
            }
            try {
                boolResult = ok(this.mIWifiChip.startLoggingToDebugRingBuffer(ringName, verboseLevel, maxIntervalInSec, minDataSizeInBytes));
                return boolResult;
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
                this.mIWifiChip.requestChipDebugInfo(new -$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA(this));
                this.mLog.info("Driver: % Firmware: %").c(this.mDriverDescription).c(this.mFirmwareDescription).flush();
            }
        } catch (RemoteException e) {
            handleRemoteException(e);
        }
    }

    /* synthetic */ void lambda$-com_android_server_wifi_WifiVendorHal_64421(WifiStatus status, ChipDebugInfo chipDebugInfo) {
        if (ok(status)) {
            this.mDriverDescription = chipDebugInfo.driverDescription;
            this.mFirmwareDescription = chipDebugInfo.firmwareDescription;
        }
    }

    private static RingBufferStatus ringBufferStatus(WifiDebugRingBufferStatus h) {
        RingBufferStatus ans = new RingBufferStatus();
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
            flags = 1;
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

    private static RingBufferStatus[] makeRingBufferStatusArray(ArrayList<WifiDebugRingBufferStatus> ringBuffers) {
        RingBufferStatus[] ans = new RingBufferStatus[ringBuffers.size()];
        int i = 0;
        for (WifiDebugRingBufferStatus b : ringBuffers) {
            int i2 = i + 1;
            ans[i] = ringBufferStatus(b);
            i = i2;
        }
        return ans;
    }

    public RingBufferStatus[] getRingBufferStatus() {
        AnonymousClass6AnswerBox ans = new Object() {
            public RingBufferStatus[] value = null;
        };
        synchronized (sLock) {
            if (this.mIWifiChip == null) {
                return null;
            }
            try {
                this.mIWifiChip.getDebugRingBuffersStatus(new AnonymousClass3(this, ans));
                return ans.value;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return null;
            }
        }
    }

    /* synthetic */ void lambda$-com_android_server_wifi_WifiVendorHal_67364(AnonymousClass6AnswerBox ans, WifiStatus status, ArrayList ringBuffers) {
        if (ok(status)) {
            ans.value = makeRingBufferStatusArray(ringBuffers);
        }
    }

    public boolean getRingBufferData(String ringName) {
        enter("ringName %").c(ringName).flush();
        synchronized (sLock) {
            boolean boolResult;
            if (this.mIWifiChip == null) {
                boolResult = boolResult(false);
                return boolResult;
            }
            try {
                boolResult = ok(this.mIWifiChip.forceDumpToDebugRingBuffer(ringName));
                return boolResult;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    public byte[] getFwMemoryDump() {
        AnonymousClass7AnswerBox ans = new Object() {
            public byte[] value;
        };
        synchronized (sLock) {
            if (this.mIWifiChip == null) {
                return null;
            }
            try {
                this.mIWifiChip.requestFirmwareDebugDump(new AnonymousClass5(this, ans));
                return ans.value;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return null;
            }
        }
    }

    /* synthetic */ void lambda$-com_android_server_wifi_WifiVendorHal_68635(AnonymousClass7AnswerBox ans, WifiStatus status, ArrayList blob) {
        if (ok(status)) {
            ans.value = NativeUtil.byteArrayFromArrayList(blob);
        }
    }

    public byte[] getDriverStateDump() {
        AnonymousClass8AnswerBox ans = new Object() {
            public byte[] value;
        };
        synchronized (sLock) {
            if (this.mIWifiChip == null) {
                return null;
            }
            try {
                this.mIWifiChip.requestDriverDebugDump(new AnonymousClass4(this, ans));
                return ans.value;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return null;
            }
        }
    }

    /* synthetic */ void lambda$-com_android_server_wifi_WifiVendorHal_69329(AnonymousClass8AnswerBox ans, WifiStatus status, ArrayList blob) {
        if (ok(status)) {
            ans.value = NativeUtil.byteArrayFromArrayList(blob);
        }
    }

    public boolean startPktFateMonitoring() {
        synchronized (sLock) {
            boolean boolResult;
            if (this.mIWifiStaIface == null) {
                boolResult = boolResult(false);
                return boolResult;
            }
            try {
                boolResult = ok(this.mIWifiStaIface.startDebugPacketFateMonitoring());
                return boolResult;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    private byte halToFrameworkPktFateFrameType(int type) {
        switch (type) {
            case 0:
                return (byte) 0;
            case 1:
                return (byte) 1;
            case 2:
                return (byte) 2;
            default:
                throw new IllegalArgumentException("bad " + type);
        }
    }

    private byte halToFrameworkRxPktFate(int type) {
        switch (type) {
            case 0:
                return (byte) 0;
            case 1:
                return (byte) 1;
            case 2:
                return (byte) 2;
            case 3:
                return (byte) 3;
            case 4:
                return (byte) 4;
            case 5:
                return (byte) 5;
            case 6:
                return (byte) 6;
            case 7:
                return (byte) 7;
            case 8:
                return (byte) 8;
            case 9:
                return (byte) 9;
            case 10:
                return (byte) 10;
            default:
                throw new IllegalArgumentException("bad " + type);
        }
    }

    private byte halToFrameworkTxPktFate(int type) {
        switch (type) {
            case 0:
                return (byte) 0;
            case 1:
                return (byte) 1;
            case 2:
                return (byte) 2;
            case 3:
                return (byte) 3;
            case 4:
                return (byte) 4;
            case 5:
                return (byte) 5;
            case 6:
                return (byte) 6;
            case 7:
                return (byte) 7;
            case 8:
                return (byte) 8;
            case 9:
                return (byte) 9;
            default:
                throw new IllegalArgumentException("bad " + type);
        }
    }

    public boolean getTxPktFates(TxFateReport[] reportBufs) {
        if (ArrayUtils.isEmpty(reportBufs)) {
            return boolResult(false);
        }
        synchronized (sLock) {
            boolean boolResult;
            if (this.mIWifiStaIface == null) {
                boolResult = boolResult(false);
                return boolResult;
            }
            try {
                MutableBoolean ok = new MutableBoolean(false);
                this.mIWifiStaIface.getDebugTxPacketFates(new AnonymousClass14(this, reportBufs, ok));
                boolResult = ok.value;
                return boolResult;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    /* synthetic */ void lambda$-com_android_server_wifi_WifiVendorHal_74196(TxFateReport[] reportBufs, MutableBoolean ok, WifiStatus status, ArrayList fates) {
        if (ok(status)) {
            int i = 0;
            for (WifiDebugTxPacketFateReport fate : fates) {
                if (i >= reportBufs.length) {
                    break;
                }
                int i2 = i + 1;
                reportBufs[i] = new TxFateReport(halToFrameworkTxPktFate(fate.fate), fate.frameInfo.driverTimestampUsec, halToFrameworkPktFateFrameType(fate.frameInfo.frameType), NativeUtil.byteArrayFromArrayList(fate.frameInfo.frameContent));
                i = i2;
            }
            ok.value = true;
        }
    }

    public boolean getRxPktFates(RxFateReport[] reportBufs) {
        if (ArrayUtils.isEmpty(reportBufs)) {
            return boolResult(false);
        }
        synchronized (sLock) {
            boolean boolResult;
            if (this.mIWifiStaIface == null) {
                boolResult = boolResult(false);
                return boolResult;
            }
            try {
                MutableBoolean ok = new MutableBoolean(false);
                this.mIWifiStaIface.getDebugRxPacketFates(new AnonymousClass13(this, reportBufs, ok));
                boolResult = ok.value;
                return boolResult;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    /* synthetic */ void lambda$-com_android_server_wifi_WifiVendorHal_75983(RxFateReport[] reportBufs, MutableBoolean ok, WifiStatus status, ArrayList fates) {
        if (ok(status)) {
            int i = 0;
            for (WifiDebugRxPacketFateReport fate : fates) {
                if (i >= reportBufs.length) {
                    break;
                }
                int i2 = i + 1;
                reportBufs[i] = new RxFateReport(halToFrameworkRxPktFate(fate.fate), fate.frameInfo.driverTimestampUsec, halToFrameworkPktFateFrameType(fate.frameInfo.frameType), NativeUtil.byteArrayFromArrayList(fate.frameInfo.frameContent));
                i = i2;
            }
            ok.value = true;
        }
    }

    public int startSendingOffloadedPacket(int slot, byte[] srcMac, KeepalivePacketData keepAlivePacket, int periodInMs) {
        enter("slot=% periodInMs=%").c((long) slot).c((long) periodInMs).flush();
        ArrayList<Byte> data = NativeUtil.byteArrayToArrayList(keepAlivePacket.data);
        short protocol = (short) keepAlivePacket.protocol;
        synchronized (sLock) {
            if (this.mIWifiStaIface == null) {
                return -1;
            }
            try {
                if (ok(this.mIWifiStaIface.startSendingKeepAlivePackets(slot, data, protocol, srcMac, keepAlivePacket.dstMac, periodInMs))) {
                    return 0;
                }
                return -1;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return -1;
            }
        }
    }

    public int stopSendingOffloadedPacket(int slot) {
        enter("slot=%").c((long) slot).flush();
        synchronized (sLock) {
            if (this.mIWifiStaIface == null) {
                return -1;
            }
            try {
                if (ok(this.mIWifiStaIface.stopSendingKeepAlivePackets(slot))) {
                    return 0;
                }
                return -1;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return -1;
            }
        }
    }

    public int startRssiMonitoring(byte maxRssi, byte minRssi, WifiRssiEventHandler rssiEventHandler) {
        enter("maxRssi=% minRssi=%").c((long) maxRssi).c((long) minRssi).flush();
        if (maxRssi <= minRssi || rssiEventHandler == null) {
            return -1;
        }
        synchronized (sLock) {
            if (this.mIWifiStaIface == null) {
                return -1;
            }
            try {
                this.mIWifiStaIface.stopRssiMonitoring(sRssiMonCmdId);
                if (ok(this.mIWifiStaIface.startRssiMonitoring(sRssiMonCmdId, maxRssi, minRssi))) {
                    this.mWifiRssiEventHandler = rssiEventHandler;
                    return 0;
                }
                return -1;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return -1;
            }
        }
    }

    public int stopRssiMonitoring() {
        synchronized (sLock) {
            this.mWifiRssiEventHandler = null;
            if (this.mIWifiStaIface == null) {
                return -1;
            }
            try {
                this.mIWifiStaIface.stopRssiMonitoring(sRssiMonCmdId);
                if (ok(this.mIWifiStaIface.stopRssiMonitoring(sRssiMonCmdId))) {
                    return 0;
                }
                return -1;
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
        for (Integer e : a) {
            int i2 = i + 1;
            b[i] = e.intValue();
            i = i2;
        }
        return b;
    }

    private static WifiWakeReasonAndCounts halToFrameworkWakeReasons(WifiDebugHostWakeReasonStats h) {
        if (h == null) {
            return null;
        }
        WifiWakeReasonAndCounts ans = new WifiWakeReasonAndCounts();
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

    public WifiWakeReasonAndCounts getWlanWakeReasonCount() {
        AnonymousClass9AnswerBox ans = new Object() {
            public WifiDebugHostWakeReasonStats value = null;
        };
        synchronized (sLock) {
            if (this.mIWifiChip == null) {
                return null;
            }
            try {
                this.mIWifiChip.getDebugHostWakeReasonStats(new AnonymousClass2(this, ans));
                WifiWakeReasonAndCounts halToFrameworkWakeReasons = halToFrameworkWakeReasons(ans.value);
                return halToFrameworkWakeReasons;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return null;
            }
        }
    }

    /* synthetic */ void lambda$-com_android_server_wifi_WifiVendorHal_83424(AnonymousClass9AnswerBox ans, WifiStatus status, WifiDebugHostWakeReasonStats stats) {
        if (ok(status)) {
            ans.value = stats;
        }
    }

    public boolean configureNeighborDiscoveryOffload(boolean enabled) {
        enter("enabled=%").c(enabled).flush();
        synchronized (sLock) {
            if (this.mIWifiStaIface == null) {
                boolean boolResult = boolResult(false);
                return boolResult;
            }
            try {
                if (ok(this.mIWifiStaIface.enableNdOffload(enabled))) {
                    return true;
                }
                return false;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    public boolean getRoamingCapabilities(RoamingCapabilities capabilities) {
        synchronized (sLock) {
            boolean boolResult;
            if (this.mIWifiStaIface == null) {
                boolResult = boolResult(false);
                return boolResult;
            }
            try {
                MutableBoolean ok = new MutableBoolean(false);
                RoamingCapabilities out = capabilities;
                this.mIWifiStaIface.getRoamingCapabilities(new AnonymousClass15(this, capabilities, ok));
                boolResult = ok.value;
                return boolResult;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    /* synthetic */ void lambda$-com_android_server_wifi_WifiVendorHal_85051(RoamingCapabilities out, MutableBoolean ok, WifiStatus status, StaRoamingCapabilities cap) {
        if (ok(status)) {
            out.maxBlacklistSize = cap.maxBlacklistSize;
            out.maxWhitelistSize = cap.maxWhitelistSize;
            ok.value = true;
        }
    }

    public int enableFirmwareRoaming(int state) {
        synchronized (sLock) {
            if (this.mIWifiStaIface == null) {
                return 6;
            }
            byte val;
            switch (state) {
                case 0:
                    val = (byte) 0;
                    break;
                case 1:
                    val = (byte) 1;
                    break;
                default:
                    try {
                        this.mLog.e("enableFirmwareRoaming invalid argument " + state);
                        return 7;
                    } catch (RemoteException e) {
                        handleRemoteException(e);
                        return 9;
                    }
            }
            WifiStatus status = this.mIWifiStaIface.setRoamingState(val);
            this.mVerboseLog.d("setRoamingState returned " + status.code);
            int i = status.code;
            return i;
        }
    }

    public boolean configureRoaming(RoamingConfig config) {
        synchronized (sLock) {
            if (this.mIWifiStaIface == null) {
                boolean boolResult = boolResult(false);
                return boolResult;
            }
            try {
                StaRoamingConfig roamingConfig = new StaRoamingConfig();
                if (config.blacklistBssids != null) {
                    for (String bssid : config.blacklistBssids) {
                        roamingConfig.bssidBlacklist.add(NativeUtil.macAddressToByteArray(bssid));
                    }
                }
                if (config.whitelistSsids != null) {
                    for (String ssidStr : config.whitelistSsids) {
                        String unquotedSsidStr = WifiInfo.removeDoubleQuotes(ssidStr);
                        int len = unquotedSsidStr.length();
                        if (len > 32) {
                            this.mLog.err("configureRoaming: skip invalid SSID %").r(unquotedSsidStr).flush();
                        } else {
                            byte[] ssid = new byte[len];
                            for (int i = 0; i < len; i++) {
                                ssid[i] = (byte) unquotedSsidStr.charAt(i);
                            }
                            roamingConfig.ssidWhitelist.add(ssid);
                        }
                    }
                }
                if (ok(this.mIWifiStaIface.configureRoaming(roamingConfig))) {
                    return true;
                }
                return false;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            } catch (IllegalArgumentException e2) {
                this.mLog.err("Illegal argument for roaming configuration").c(e2.toString()).flush();
                return false;
            }
        }
    }

    private static byte[] hidlIeArrayToFrameworkIeBlob(ArrayList<android.hardware.wifi.V1_0.WifiInformationElement> ies) {
        if (ies == null || ies.isEmpty()) {
            return new byte[0];
        }
        ArrayList<Byte> ieBlob = new ArrayList();
        for (android.hardware.wifi.V1_0.WifiInformationElement ie : ies) {
            ieBlob.add(Byte.valueOf(ie.id));
            ieBlob.addAll(ie.data);
        }
        return NativeUtil.byteArrayFromArrayList(ieBlob);
    }

    private static ScanResult hidlToFrameworkScanResult(StaScanResult scanResult) {
        if (scanResult == null) {
            return null;
        }
        ScanResult frameworkScanResult = new ScanResult();
        frameworkScanResult.SSID = NativeUtil.encodeSsid(scanResult.ssid);
        frameworkScanResult.wifiSsid = WifiSsid.createFromByteArray(NativeUtil.byteArrayFromArrayList(scanResult.ssid));
        frameworkScanResult.BSSID = NativeUtil.macAddressFromByteArray(scanResult.bssid);
        frameworkScanResult.level = scanResult.rssi;
        frameworkScanResult.frequency = scanResult.frequency;
        frameworkScanResult.timestamp = scanResult.timeStampInUs;
        frameworkScanResult.bytes = hidlIeArrayToFrameworkIeBlob(scanResult.informationElements);
        frameworkScanResult.informationElements = InformationElementUtil.parseInformationElements(frameworkScanResult.bytes);
        return frameworkScanResult;
    }

    private static ScanResult[] hidlToFrameworkScanResults(ArrayList<StaScanResult> scanResults) {
        if (scanResults == null || scanResults.isEmpty()) {
            return new ScanResult[0];
        }
        ScanResult[] frameworkScanResults = new ScanResult[scanResults.size()];
        int i = 0;
        for (StaScanResult scanResult : scanResults) {
            int i2 = i + 1;
            frameworkScanResults[i] = hidlToFrameworkScanResult(scanResult);
            i = i2;
        }
        return frameworkScanResults;
    }

    private static int hidlToFrameworkScanDataFlags(int flag) {
        if (flag == 1) {
            return 1;
        }
        return 0;
    }

    private static ScanData[] hidlToFrameworkScanDatas(int cmdId, ArrayList<StaScanData> scanDatas) {
        if (scanDatas == null || scanDatas.isEmpty()) {
            return new ScanData[0];
        }
        ScanData[] frameworkScanDatas = new ScanData[scanDatas.size()];
        int i = 0;
        for (StaScanData scanData : scanDatas) {
            int i2 = i + 1;
            int i3 = cmdId;
            frameworkScanDatas[i] = new ScanData(i3, hidlToFrameworkScanDataFlags(scanData.flags), scanData.bucketsScanned, false, hidlToFrameworkScanResults(scanData.results));
            i = i2;
        }
        return frameworkScanDatas;
    }
}
