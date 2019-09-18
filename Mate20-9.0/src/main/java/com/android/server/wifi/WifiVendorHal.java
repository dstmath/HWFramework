package com.android.server.wifi;

import android.hardware.wifi.V1_0.IWifiApIface;
import android.hardware.wifi.V1_0.IWifiChip;
import android.hardware.wifi.V1_0.IWifiChipEventCallback;
import android.hardware.wifi.V1_0.IWifiIface;
import android.hardware.wifi.V1_0.IWifiRttController;
import android.hardware.wifi.V1_0.IWifiRttControllerEventCallback;
import android.hardware.wifi.V1_0.IWifiStaIface;
import android.hardware.wifi.V1_0.IWifiStaIfaceEventCallback;
import android.hardware.wifi.V1_0.RttCapabilities;
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
import android.hardware.wifi.V1_0.WifiInformationElement;
import android.hardware.wifi.V1_0.WifiStatus;
import android.hardware.wifi.V1_2.IWifiChipEventCallback;
import android.hardware.wifi.V1_2.IWifiStaIface;
import android.net.MacAddress;
import android.net.apf.ApfCapabilities;
import android.net.wifi.RttManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiScanner;
import android.net.wifi.WifiSsid;
import android.net.wifi.WifiWakeReasonAndCounts;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.system.OsConstants;
import android.text.TextUtils;
import android.util.Log;
import android.util.MutableBoolean;
import android.util.MutableInt;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.HexDump;
import com.android.server.wifi.HalDeviceManager;
import com.android.server.wifi.WifiLog;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.WifiVendorHal;
import com.android.server.wifi.util.BitMask;
import com.android.server.wifi.util.InformationElementUtil;
import com.android.server.wifi.util.NativeUtil;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WifiVendorHal {
    private static final int CAPABILITY_SIZE = 16;
    private static final int[][] sChipFeatureCapabilityTranslation = {new int[]{67108864, 256}, new int[]{128, 512}, new int[]{256, 1024}};
    public static final Object sLock = new Object();
    /* access modifiers changed from: private */
    public static final ApfCapabilities sNoApfCapabilities = new ApfCapabilities(0, 0, 0);
    private static final WifiLog sNoLog = new FakeWifiLog();
    @VisibleForTesting
    static final int sRssiMonCmdId = 7551;
    private static final int[][] sStaFeatureCapabilityTranslation = {new int[]{2, 128}, new int[]{4, 256}, new int[]{32, 2}, new int[]{1024, 512}, new int[]{4096, 1024}, new int[]{8192, 2048}, new int[]{65536, 4}, new int[]{524288, 8}, new int[]{1048576, 8192}, new int[]{2097152, 4096}, new int[]{8388608, 16}, new int[]{16777216, 32}, new int[]{33554432, 64}};
    /* access modifiers changed from: private */
    public WifiNative.VendorHalDeathEventHandler mDeathEventHandler;
    private String mDriverDescription;
    private String mFirmwareDescription;
    /* access modifiers changed from: private */
    public final HalDeviceManager mHalDeviceManager;
    private final HalDeviceManagerStatusListener mHalDeviceManagerStatusCallbacks;
    /* access modifiers changed from: private */
    public final Handler mHalEventHandler;
    /* access modifiers changed from: private */
    public HashMap<String, IWifiApIface> mIWifiApIfaces = new HashMap<>();
    private IWifiChip mIWifiChip;
    /* access modifiers changed from: private */
    public final ChipEventCallback mIWifiChipEventCallback;
    private final ChipEventCallbackV12 mIWifiChipEventCallbackV12;
    private IWifiRttController mIWifiRttController;
    private final IWifiStaIfaceEventCallback mIWifiStaIfaceEventCallback;
    /* access modifiers changed from: private */
    public HashMap<String, IWifiStaIface> mIWifiStaIfaces = new HashMap<>();
    private int mLastScanCmdId;
    @VisibleForTesting
    boolean mLinkLayerStatsDebug = false;
    @VisibleForTesting
    WifiLog mLog = new LogcatLog("WifiVendorHal");
    /* access modifiers changed from: private */
    public WifiNative.WifiLoggerEventHandler mLogEventHandler = null;
    private final Looper mLooper;
    /* access modifiers changed from: private */
    public WifiNative.VendorHalRadioModeChangeEventHandler mRadioModeChangeEventHandler;
    /* access modifiers changed from: private */
    public int mRttCmdId;
    private int mRttCmdIdNext = 1;
    private final RttEventCallback mRttEventCallback;
    /* access modifiers changed from: private */
    public WifiNative.RttEventHandler mRttEventHandler;
    private int mRttResponderCmdId = 0;
    @VisibleForTesting
    CurrentBackgroundScan mScan = null;
    @VisibleForTesting
    WifiLog mVerboseLog = sNoLog;
    /* access modifiers changed from: private */
    public WifiNative.WifiRssiEventHandler mWifiRssiEventHandler;

    private class ApInterfaceDestroyedListenerInternal implements HalDeviceManager.InterfaceDestroyedListener {
        private final HalDeviceManager.InterfaceDestroyedListener mExternalListener;

        ApInterfaceDestroyedListenerInternal(HalDeviceManager.InterfaceDestroyedListener externalListener) {
            this.mExternalListener = externalListener;
        }

        public void onDestroyed(String ifaceName) {
            synchronized (WifiVendorHal.sLock) {
                WifiVendorHal.this.mIWifiApIfaces.remove(ifaceName);
            }
            if (this.mExternalListener != null) {
                this.mExternalListener.onDestroyed(ifaceName);
            }
        }
    }

    private class ChipEventCallback extends IWifiChipEventCallback.Stub {
        private ChipEventCallback() {
        }

        public void onChipReconfigured(int modeId) {
            WifiLog wifiLog = WifiVendorHal.this.mVerboseLog;
            wifiLog.d("onChipReconfigured " + modeId);
        }

        public void onChipReconfigureFailure(WifiStatus status) {
            WifiLog wifiLog = WifiVendorHal.this.mVerboseLog;
            wifiLog.d("onChipReconfigureFailure " + status);
        }

        public void onIfaceAdded(int type, String name) {
            WifiLog wifiLog = WifiVendorHal.this.mVerboseLog;
            wifiLog.d("onIfaceAdded " + type + ", name: " + name);
        }

        public void onIfaceRemoved(int type, String name) {
            WifiLog wifiLog = WifiVendorHal.this.mVerboseLog;
            wifiLog.d("onIfaceRemoved " + type + ", name: " + name);
        }

        public void onDebugRingBufferDataAvailable(WifiDebugRingBufferStatus status, ArrayList<Byte> data) {
            WifiVendorHal.this.mHalEventHandler.post(new Runnable(status, data) {
                private final /* synthetic */ WifiDebugRingBufferStatus f$1;
                private final /* synthetic */ ArrayList f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    WifiVendorHal.ChipEventCallback.lambda$onDebugRingBufferDataAvailable$0(WifiVendorHal.ChipEventCallback.this, this.f$1, this.f$2);
                }
            });
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x0017, code lost:
            r0 = r8.size();
            r2 = false;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
            r1.onRingBufferData(com.android.server.wifi.WifiVendorHal.access$1500(r7), com.android.server.wifi.util.NativeUtil.byteArrayFromArrayList(r8));
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x002c, code lost:
            if (r8.size() == r0) goto L_0x0032;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x002e, code lost:
            r2 = true;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0031, code lost:
            r2 = true;
         */
        public static /* synthetic */ void lambda$onDebugRingBufferDataAvailable$0(ChipEventCallback chipEventCallback, WifiDebugRingBufferStatus status, ArrayList data) {
            int sizeBefore;
            boolean conversionFailure;
            synchronized (WifiVendorHal.sLock) {
                if (!(WifiVendorHal.this.mLogEventHandler == null || status == null)) {
                    if (data != null) {
                        WifiNative.WifiLoggerEventHandler eventHandler = WifiVendorHal.this.mLogEventHandler;
                    }
                }
                return;
            }
            if (conversionFailure) {
                Log.wtf("WifiVendorHal", "Conversion failure detected in onDebugRingBufferDataAvailable. The input ArrayList |data| is potentially corrupted. Starting size=" + sizeBefore + ", final size=" + data.size());
            }
        }

        public void onDebugErrorAlert(int errorCode, ArrayList<Byte> debugData) {
            WifiLog wifiLog = WifiVendorHal.this.mLog;
            wifiLog.w("onDebugErrorAlert " + errorCode);
            WifiVendorHal.this.mHalEventHandler.post(new Runnable(debugData, errorCode) {
                private final /* synthetic */ ArrayList f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    WifiVendorHal.ChipEventCallback.lambda$onDebugErrorAlert$1(WifiVendorHal.ChipEventCallback.this, this.f$1, this.f$2);
                }
            });
        }

        public static /* synthetic */ void lambda$onDebugErrorAlert$1(ChipEventCallback chipEventCallback, ArrayList debugData, int errorCode) {
            synchronized (WifiVendorHal.sLock) {
                if (WifiVendorHal.this.mLogEventHandler != null) {
                    if (debugData != null) {
                        WifiNative.WifiLoggerEventHandler eventHandler = WifiVendorHal.this.mLogEventHandler;
                        eventHandler.onWifiAlert(errorCode, NativeUtil.byteArrayFromArrayList(debugData));
                    }
                }
            }
        }
    }

    private class ChipEventCallbackV12 extends IWifiChipEventCallback.Stub {
        private ChipEventCallbackV12() {
        }

        public void onChipReconfigured(int modeId) {
            WifiVendorHal.this.mIWifiChipEventCallback.onChipReconfigured(modeId);
        }

        public void onChipReconfigureFailure(WifiStatus status) {
            WifiVendorHal.this.mIWifiChipEventCallback.onChipReconfigureFailure(status);
        }

        public void onIfaceAdded(int type, String name) {
            WifiVendorHal.this.mIWifiChipEventCallback.onIfaceAdded(type, name);
        }

        public void onIfaceRemoved(int type, String name) {
            WifiVendorHal.this.mIWifiChipEventCallback.onIfaceRemoved(type, name);
        }

        public void onDebugRingBufferDataAvailable(WifiDebugRingBufferStatus status, ArrayList<Byte> data) {
            WifiVendorHal.this.mIWifiChipEventCallback.onDebugRingBufferDataAvailable(status, data);
        }

        public void onDebugErrorAlert(int errorCode, ArrayList<Byte> debugData) {
            WifiVendorHal.this.mIWifiChipEventCallback.onDebugErrorAlert(errorCode, debugData);
        }

        private boolean areSameIfaceNames(List<IWifiChipEventCallback.IfaceInfo> ifaceList1, List<IWifiChipEventCallback.IfaceInfo> ifaceList2) {
            return ((List) ifaceList1.stream().map($$Lambda$WifiVendorHal$ChipEventCallbackV12$SrMSOw3LUVF_Z64G_aL0Tguwt3A.INSTANCE).collect(Collectors.toList())).containsAll((List) ifaceList2.stream().map($$Lambda$WifiVendorHal$ChipEventCallbackV12$nAuRYe8SQ_MJ2XaULMZNtUE0izo.INSTANCE).collect(Collectors.toList()));
        }

        private boolean areSameIfaces(List<IWifiChipEventCallback.IfaceInfo> ifaceList1, List<IWifiChipEventCallback.IfaceInfo> ifaceList2) {
            return ifaceList1.containsAll(ifaceList2);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x0032, code lost:
            if (r9.size() == 0) goto L_0x010a;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0039, code lost:
            if (r9.size() <= 2) goto L_0x003d;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x003d, code lost:
            r3 = r9.get(0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0049, code lost:
            if (r9.size() != 2) goto L_0x0052;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x004b, code lost:
            r4 = r9.get(1);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0052, code lost:
            r4 = null;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0053, code lost:
            if (r4 == null) goto L_0x0090;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x0061, code lost:
            if (r3.ifaceInfos.size() == r4.ifaceInfos.size()) goto L_0x0090;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x0063, code lost:
            r0 = r8.this$0.mLog;
            r0.e("Unexpected number of iface info in list " + r3.ifaceInfos.size() + ", " + r4.ifaceInfos.size());
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x008f, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x0090, code lost:
            r6 = r3.ifaceInfos.size();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:23:0x0096, code lost:
            if (r6 == 0) goto L_0x00f1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:24:0x0098, code lost:
            if (r6 <= 2) goto L_0x009b;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:26:0x009f, code lost:
            if (r9.size() != 2) goto L_0x00c7;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:27:0x00a1, code lost:
            if (r6 != 1) goto L_0x00c7;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:29:0x00ab, code lost:
            if (areSameIfaceNames(r3.ifaceInfos, r4.ifaceInfos) == false) goto L_0x00b7;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:30:0x00ad, code lost:
            r8.this$0.mLog.e("Unexpected for both radio infos to have same iface");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:31:0x00b6, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:0x00bb, code lost:
            if (r3.bandInfo == r4.bandInfo) goto L_0x00c1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:34:0x00bd, code lost:
            r1.onDbs();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:35:0x00c1, code lost:
            r1.onSbs(r3.bandInfo);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:37:0x00cb, code lost:
            if (r9.size() != 1) goto L_0x00f0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:38:0x00cd, code lost:
            if (r6 != 2) goto L_0x00f0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:40:0x00e3, code lost:
            if (r3.ifaceInfos.get(0).channel == r3.ifaceInfos.get(1).channel) goto L_0x00eb;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:41:0x00e5, code lost:
            r1.onMcc(r3.bandInfo);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:42:0x00eb, code lost:
            r1.onScc(r3.bandInfo);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:43:0x00f0, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:44:0x00f1, code lost:
            r0 = r8.this$0.mLog;
            r0.e("Unexpected number of iface info in list " + r6);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:45:0x0109, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:46:0x010a, code lost:
            r0 = r8.this$0.mLog;
            r0.e("Unexpected number of radio info in list " + r9.size());
         */
        /* JADX WARNING: Code restructure failed: missing block: B:47:0x0126, code lost:
            return;
         */
        public void onRadioModeChange(ArrayList<IWifiChipEventCallback.RadioModeInfo> radioModeInfoList) {
            WifiLog wifiLog = WifiVendorHal.this.mVerboseLog;
            wifiLog.d("onRadioModeChange " + radioModeInfoList);
            synchronized (WifiVendorHal.sLock) {
                if (WifiVendorHal.this.mRadioModeChangeEventHandler != null) {
                    if (radioModeInfoList != null) {
                        WifiNative.VendorHalRadioModeChangeEventHandler handler = WifiVendorHal.this.mRadioModeChangeEventHandler;
                    }
                }
            }
        }
    }

    @VisibleForTesting
    class CurrentBackgroundScan {
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

    public class HalDeviceManagerStatusListener implements HalDeviceManager.ManagerStatusListener {
        public HalDeviceManagerStatusListener() {
        }

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

    private class RttEventCallback extends IWifiRttControllerEventCallback.Stub {
        private RttEventCallback() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x0021, code lost:
            r0 = new android.net.wifi.RttManager.RttResult[r6.size()];
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0028, code lost:
            r2 = r3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x002a, code lost:
            if (r2 >= r0.length) goto L_0x003b;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x002c, code lost:
            r0[r2] = com.android.server.wifi.WifiVendorHal.frameworkRttResultFromHalRttResult(r6.get(r2));
            r3 = r2 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x003b, code lost:
            r1.onRttResults(r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x003e, code lost:
            return;
         */
        public void onResults(int cmdId, ArrayList<RttResult> results) {
            synchronized (WifiVendorHal.sLock) {
                if (cmdId == WifiVendorHal.this.mRttCmdId) {
                    if (WifiVendorHal.this.mRttEventHandler != null) {
                        WifiNative.RttEventHandler eventHandler = WifiVendorHal.this.mRttEventHandler;
                        int i = 0;
                        int unused = WifiVendorHal.this.mRttCmdId = 0;
                    }
                }
            }
        }
    }

    private class StaIfaceEventCallback extends IWifiStaIfaceEventCallback.Stub {
        private StaIfaceEventCallback() {
        }

        public void onBackgroundScanFailure(int cmdId) {
            WifiLog wifiLog = WifiVendorHal.this.mVerboseLog;
            wifiLog.d("onBackgroundScanFailure " + cmdId);
            synchronized (WifiVendorHal.sLock) {
                if (WifiVendorHal.this.mScan != null) {
                    if (cmdId == WifiVendorHal.this.mScan.cmdId) {
                        WifiNative.ScanEventHandler eventHandler = WifiVendorHal.this.mScan.eventHandler;
                        eventHandler.onScanStatus(3);
                    }
                }
            }
        }

        public void onBackgroundFullScanResult(int cmdId, int bucketsScanned, StaScanResult result) {
            WifiLog wifiLog = WifiVendorHal.this.mVerboseLog;
            wifiLog.d("onBackgroundFullScanResult " + cmdId);
            synchronized (WifiVendorHal.sLock) {
                if (WifiVendorHal.this.mScan != null) {
                    if (cmdId == WifiVendorHal.this.mScan.cmdId) {
                        WifiNative.ScanEventHandler eventHandler = WifiVendorHal.this.mScan.eventHandler;
                        eventHandler.onFullScanResult(WifiVendorHal.hidlToFrameworkScanResult(result), bucketsScanned);
                    }
                }
            }
        }

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

        public void onRssiThresholdBreached(int cmdId, byte[] currBssid, int currRssi) {
            WifiLog wifiLog = WifiVendorHal.this.mVerboseLog;
            wifiLog.d("onRssiThresholdBreached " + cmdId + "currRssi " + currRssi);
            synchronized (WifiVendorHal.sLock) {
                if (WifiVendorHal.this.mWifiRssiEventHandler != null) {
                    if (cmdId == WifiVendorHal.sRssiMonCmdId) {
                        WifiNative.WifiRssiEventHandler eventHandler = WifiVendorHal.this.mWifiRssiEventHandler;
                        eventHandler.onRssiThresholdBreached((byte) currRssi);
                    }
                }
            }
        }
    }

    private class StaInterfaceDestroyedListenerInternal implements HalDeviceManager.InterfaceDestroyedListener {
        private final HalDeviceManager.InterfaceDestroyedListener mExternalListener;

        StaInterfaceDestroyedListenerInternal(HalDeviceManager.InterfaceDestroyedListener externalListener) {
            this.mExternalListener = externalListener;
        }

        public void onDestroyed(String ifaceName) {
            synchronized (WifiVendorHal.sLock) {
                WifiVendorHal.this.mIWifiStaIfaces.remove(ifaceName);
            }
            if (this.mExternalListener != null) {
                this.mExternalListener.onDestroyed(ifaceName);
            }
        }
    }

    public void enableVerboseLogging(boolean verbose) {
        synchronized (sLock) {
            if (verbose) {
                try {
                    this.mVerboseLog = this.mLog;
                    enter("verbose=true").flush();
                } catch (Throwable th) {
                    throw th;
                }
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
        this.mVerboseLog.err("% returns %").c(niceMethodName(Thread.currentThread().getStackTrace(), 3)).c(HexDump.dumpHexString(result)).flush();
        return result;
    }

    private WifiLog.LogMessage enter(String format) {
        if (this.mVerboseLog == sNoLog) {
            return sNoLog.info(format);
        }
        return this.mVerboseLog.trace(format, 1);
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
        this.mRttEventCallback = new RttEventCallback();
    }

    private void handleRemoteException(RemoteException e) {
        this.mVerboseLog.err("% RemoteException in HIDL call %").c(niceMethodName(Thread.currentThread().getStackTrace(), 3)).c(e.toString()).flush();
        clearState();
    }

    public boolean initialize(WifiNative.VendorHalDeathEventHandler handler) {
        synchronized (sLock) {
            this.mHalDeviceManager.initialize();
            this.mHalDeviceManager.registerStatusListener(this.mHalDeviceManagerStatusCallbacks, null);
            this.mDeathEventHandler = handler;
        }
        return true;
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

    public String createStaIface(boolean lowPrioritySta, HalDeviceManager.InterfaceDestroyedListener destroyedListener) {
        synchronized (sLock) {
            IWifiStaIface iface = this.mHalDeviceManager.createStaIface(lowPrioritySta, new StaInterfaceDestroyedListenerInternal(destroyedListener), null);
            if (iface == null) {
                this.mLog.err("Failed to create STA iface").flush();
                String stringResult = stringResult(null);
                return stringResult;
            }
            HalDeviceManager halDeviceManager = this.mHalDeviceManager;
            String ifaceName = HalDeviceManager.getName(iface);
            if (TextUtils.isEmpty(ifaceName)) {
                this.mLog.err("Failed to get iface name").flush();
                String stringResult2 = stringResult(null);
                return stringResult2;
            } else if (!registerStaIfaceCallback(iface)) {
                this.mLog.err("Failed to register STA iface callback").flush();
                String stringResult3 = stringResult(null);
                return stringResult3;
            } else {
                this.mIWifiRttController = this.mHalDeviceManager.createRttController();
                if (this.mIWifiRttController == null) {
                    this.mLog.err("Failed to create RTT controller").flush();
                    String stringResult4 = stringResult(null);
                    return stringResult4;
                } else if (!registerRttEventCallback()) {
                    this.mLog.err("Failed to register RTT controller callback").flush();
                    String stringResult5 = stringResult(null);
                    return stringResult5;
                } else if (!retrieveWifiChip(iface)) {
                    this.mLog.err("Failed to get wifi chip").flush();
                    String stringResult6 = stringResult(null);
                    return stringResult6;
                } else {
                    enableLinkLayerStats(iface);
                    this.mIWifiStaIfaces.put(ifaceName, iface);
                    return ifaceName;
                }
            }
        }
    }

    public boolean removeStaIface(String ifaceName) {
        synchronized (sLock) {
            IWifiStaIface iface = getStaIface(ifaceName);
            if (iface == null) {
                boolean boolResult = boolResult(false);
                return boolResult;
            } else if (!this.mHalDeviceManager.removeIface(iface)) {
                this.mLog.err("Failed to remove STA iface").flush();
                boolean boolResult2 = boolResult(false);
                return boolResult2;
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

    public String createApIface(HalDeviceManager.InterfaceDestroyedListener destroyedListener) {
        synchronized (sLock) {
            IWifiApIface iface = this.mHalDeviceManager.createApIface(new ApInterfaceDestroyedListenerInternal(destroyedListener), null);
            if (iface == null) {
                this.mLog.err("Failed to create AP iface").flush();
                String stringResult = stringResult(null);
                return stringResult;
            }
            HalDeviceManager halDeviceManager = this.mHalDeviceManager;
            String ifaceName = HalDeviceManager.getName(iface);
            if (TextUtils.isEmpty(ifaceName)) {
                this.mLog.err("Failed to get iface name").flush();
                String stringResult2 = stringResult(null);
                return stringResult2;
            } else if (!retrieveWifiChip(iface)) {
                this.mLog.err("Failed to get wifi chip").flush();
                String stringResult3 = stringResult(null);
                return stringResult3;
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
                boolean boolResult = boolResult(false);
                return boolResult;
            } else if (!this.mHalDeviceManager.removeIface(iface)) {
                this.mLog.err("Failed to remove AP iface").flush();
                boolean boolResult2 = boolResult(false);
                return boolResult2;
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
                return false;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0024, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0025, code lost:
        handleRemoteException(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0029, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x002b, code lost:
        throw r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000c, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:4:0x0006, B:15:0x0018] */
    private boolean registerStaIfaceCallback(IWifiStaIface iface) {
        synchronized (sLock) {
            if (iface == null) {
                boolean boolResult = boolResult(false);
                return boolResult;
            } else if (this.mIWifiStaIfaceEventCallback == null) {
                boolean boolResult2 = boolResult(false);
                return boolResult2;
            } else {
                boolean ok = ok(iface.registerEventCallback(this.mIWifiStaIfaceEventCallback));
                return ok;
            }
        }
    }

    private boolean registerChipCallback() {
        WifiStatus status;
        synchronized (sLock) {
            if (this.mIWifiChip == null) {
                boolean boolResult = boolResult(false);
                return boolResult;
            }
            try {
                android.hardware.wifi.V1_2.IWifiChip iWifiChipV12 = getWifiChipForV1_2Mockable();
                if (iWifiChipV12 != null) {
                    status = iWifiChipV12.registerEventCallback_1_2(this.mIWifiChipEventCallbackV12);
                } else {
                    status = this.mIWifiChip.registerEventCallback(this.mIWifiChipEventCallback);
                }
                boolean ok = ok(status);
                return ok;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    private boolean registerRttEventCallback() {
        synchronized (sLock) {
            if (this.mIWifiRttController == null) {
                boolean boolResult = boolResult(false);
                return boolResult;
            } else if (this.mRttEventCallback == null) {
                boolean boolResult2 = boolResult(false);
                return boolResult2;
            } else {
                try {
                    boolean ok = ok(this.mIWifiRttController.registerEventCallback(this.mRttEventCallback));
                    return ok;
                } catch (RemoteException e) {
                    handleRemoteException(e);
                    return false;
                }
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
    public void clearState() {
        this.mIWifiChip = null;
        this.mIWifiStaIfaces.clear();
        this.mIWifiApIfaces.clear();
        this.mIWifiRttController = null;
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
                boolean boolResult = boolResult(false);
                return boolResult;
            }
            try {
                MutableBoolean ans = new MutableBoolean(false);
                iface.getBackgroundScanCapabilities(new IWifiStaIface.getBackgroundScanCapabilitiesCallback(capabilities, ans) {
                    private final /* synthetic */ WifiNative.ScanCapabilities f$1;
                    private final /* synthetic */ MutableBoolean f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void onValues(WifiStatus wifiStatus, StaBackgroundScanCapabilities staBackgroundScanCapabilities) {
                        WifiVendorHal.lambda$getBgScanCapabilities$0(WifiVendorHal.this, this.f$1, this.f$2, wifiStatus, staBackgroundScanCapabilities);
                    }
                });
                boolean z = ans.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getBgScanCapabilities$0(WifiVendorHal wifiVendorHal, WifiNative.ScanCapabilities out, MutableBoolean ans, WifiStatus status, StaBackgroundScanCapabilities cap) {
        if (wifiVendorHal.ok(status)) {
            wifiVendorHal.mVerboseLog.info("scan capabilities %").c(cap.toString()).flush();
            out.max_scan_cache_size = cap.maxCacheSize;
            out.max_ap_cache_per_scan = cap.maxApCachePerScan;
            out.max_scan_buckets = cap.maxBuckets;
            out.max_rssi_sample_size = 0;
            out.max_scan_reporting_threshold = cap.maxReportingThreshold;
            ans.value = true;
        }
    }

    /* access modifiers changed from: private */
    public StaBackgroundScanBucketParameters makeStaBackgroundScanBucketParametersFromBucketSettings(WifiNative.BucketSettings bs) {
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
                boolean boolResult = boolResult(false);
                return boolResult;
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
            WifiScanner.ScanData[] scanDataArr = this.mScan.latestScanResults;
            return scanDataArr;
        }
    }

    public WifiLinkLayerStats getWifiLinkLayerStats(String ifaceName) {
        AnonymousClass1AnswerBox answer = new Object() {
            public StaLinkLayerStats value = null;
        };
        synchronized (sLock) {
            try {
                IWifiStaIface iface = getStaIface(ifaceName);
                if (iface == null) {
                    return null;
                }
                iface.getLinkLayerStats(new IWifiStaIface.getLinkLayerStatsCallback(answer) {
                    private final /* synthetic */ WifiVendorHal.AnonymousClass1AnswerBox f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(WifiStatus wifiStatus, StaLinkLayerStats staLinkLayerStats) {
                        WifiVendorHal.lambda$getWifiLinkLayerStats$1(WifiVendorHal.this, this.f$1, wifiStatus, staLinkLayerStats);
                    }
                });
                return frameworkFromHalLinkLayerStats(answer.value);
            } catch (RemoteException e) {
                handleRemoteException(e);
                return null;
            }
        }
    }

    public static /* synthetic */ void lambda$getWifiLinkLayerStats$1(WifiVendorHal wifiVendorHal, AnonymousClass1AnswerBox answer, WifiStatus status, StaLinkLayerStats stats) {
        if (wifiVendorHal.ok(status)) {
            answer.value = stats;
        }
    }

    @VisibleForTesting
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
            StaLinkLayerRadioStats radioStats = stats.radios.get(0);
            out.on_time = radioStats.onTimeInMs;
            out.tx_time = radioStats.txTimeInMs;
            out.tx_time_per_level = new int[radioStats.txTimeInMsPerLevel.size()];
            for (int i = 0; i < out.tx_time_per_level.length; i++) {
                out.tx_time_per_level[i] = radioStats.txTimeInMsPerLevel.get(i).intValue();
            }
            out.rx_time = radioStats.rxTimeInMs;
            out.on_time_scan = radioStats.onTimeInMsForScan;
        }
        out.timeStampInMs = stats.timeStampInMs;
        return out;
    }

    private void enableLinkLayerStats(IWifiStaIface iface) {
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
        for (int i = 0; i < sChipFeatureCapabilityTranslation.length; i++) {
            if ((sChipFeatureCapabilityTranslation[i][1] & capabilities) != 0) {
                features |= sChipFeatureCapabilityTranslation[i][0];
            }
        }
        return features;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int wifiFeatureMaskFromStaCapabilities(int capabilities) {
        int features = 0;
        for (int i = 0; i < sStaFeatureCapabilityTranslation.length; i++) {
            if ((sStaFeatureCapabilityTranslation[i][1] & capabilities) != 0) {
                features |= sStaFeatureCapabilityTranslation[i][0];
            }
        }
        return features;
    }

    public int getSupportedFeatureSet(String ifaceName) {
        if (!this.mHalDeviceManager.isStarted()) {
            return 0;
        }
        try {
            MutableInt feat = new MutableInt(0);
            synchronized (sLock) {
                if (this.mIWifiChip != null) {
                    this.mIWifiChip.getCapabilities(new IWifiChip.getCapabilitiesCallback(feat) {
                        private final /* synthetic */ MutableInt f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void onValues(WifiStatus wifiStatus, int i) {
                            WifiVendorHal.lambda$getSupportedFeatureSet$2(WifiVendorHal.this, this.f$1, wifiStatus, i);
                        }
                    });
                }
                IWifiStaIface iface = getStaIface(ifaceName);
                if (iface != null) {
                    iface.getCapabilities(new IWifiStaIface.getCapabilitiesCallback(feat) {
                        private final /* synthetic */ MutableInt f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void onValues(WifiStatus wifiStatus, int i) {
                            WifiVendorHal.lambda$getSupportedFeatureSet$3(WifiVendorHal.this, this.f$1, wifiStatus, i);
                        }
                    });
                }
            }
            int featureSet = feat.value;
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
                featureSet |= 64;
            }
            return featureSet;
        } catch (RemoteException e) {
            handleRemoteException(e);
            return 0;
        }
    }

    public static /* synthetic */ void lambda$getSupportedFeatureSet$2(WifiVendorHal wifiVendorHal, MutableInt feat, WifiStatus status, int capabilities) {
        if (wifiVendorHal.ok(status)) {
            feat.value = wifiVendorHal.wifiFeatureMaskFromChipCapabilities(capabilities);
        }
    }

    public static /* synthetic */ void lambda$getSupportedFeatureSet$3(WifiVendorHal wifiVendorHal, MutableInt feat, WifiStatus status, int capabilities) {
        if (wifiVendorHal.ok(status)) {
            feat.value |= wifiVendorHal.wifiFeatureMaskFromStaCapabilities(capabilities);
        }
    }

    public RttManager.RttCapabilities getRttCapabilities() {
        synchronized (sLock) {
            if (this.mIWifiRttController == null) {
                return null;
            }
            try {
                AnonymousClass2AnswerBox box = new Object() {
                    public RttManager.RttCapabilities value = null;
                };
                this.mIWifiRttController.getCapabilities(new IWifiRttController.getCapabilitiesCallback(box) {
                    private final /* synthetic */ WifiVendorHal.AnonymousClass2AnswerBox f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(WifiStatus wifiStatus, RttCapabilities rttCapabilities) {
                        WifiVendorHal.lambda$getRttCapabilities$4(WifiVendorHal.this, this.f$1, wifiStatus, rttCapabilities);
                    }
                });
                RttManager.RttCapabilities rttCapabilities = box.value;
                return rttCapabilities;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return null;
            }
        }
    }

    public static /* synthetic */ void lambda$getRttCapabilities$4(WifiVendorHal wifiVendorHal, AnonymousClass2AnswerBox box, WifiStatus status, RttCapabilities capabilities) {
        if (wifiVendorHal.ok(status)) {
            wifiVendorHal.mVerboseLog.info("rtt capabilites %").c(capabilities.toString()).flush();
            RttManager.RttCapabilities ans = new RttManager.RttCapabilities();
            ans.oneSidedRttSupported = capabilities.rttOneSidedSupported;
            ans.twoSided11McRttSupported = capabilities.rttFtmSupported;
            ans.lciSupported = capabilities.lciSupported;
            ans.lcrSupported = capabilities.lcrSupported;
            ans.preambleSupported = frameworkPreambleFromHalPreamble(capabilities.preambleSupport);
            ans.bwSupported = frameworkBwFromHalBw(capabilities.bwSupport);
            ans.responderSupported = capabilities.responderSupported;
            ans.secureRttSupported = false;
            ans.mcVersion = capabilities.mcVersion & 255;
            box.value = ans;
        }
    }

    @VisibleForTesting
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

    @VisibleForTesting
    static RttManager.WifiInformationElement ieFromHal(WifiInformationElement ie) {
        if (ie == null) {
            return null;
        }
        RttManager.WifiInformationElement ans = new RttManager.WifiInformationElement();
        ans.id = ie.id;
        ans.data = NativeUtil.byteArrayFromArrayList(ie.data);
        return ans;
    }

    @VisibleForTesting
    static RttConfig halRttConfigFromFrameworkRttParams(RttManager.RttParams params) {
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

    @VisibleForTesting
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

    @VisibleForTesting
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

    @VisibleForTesting
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

    @VisibleForTesting
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

    @VisibleForTesting
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

    @VisibleForTesting
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

    @VisibleForTesting
    static int halPreambleFromFrameworkPreamble(int rttManagerPreamble) {
        BitMask checkoff = new BitMask(rttManagerPreamble);
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
        throw new IllegalArgumentException("bad " + rttManagerPreamble);
    }

    @VisibleForTesting
    static int frameworkPreambleFromHalPreamble(int halPreamble) {
        BitMask checkoff = new BitMask(halPreamble);
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
        throw new IllegalArgumentException("bad " + halPreamble);
    }

    @VisibleForTesting
    static int halBwFromFrameworkBw(int rttManagerBandwidth) {
        BitMask checkoff = new BitMask(rttManagerBandwidth);
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

    @VisibleForTesting
    static int frameworkBwFromHalBw(int rttBw) {
        BitMask checkoff = new BitMask(rttBw);
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

    @VisibleForTesting
    static ArrayList<RttConfig> halRttConfigArrayFromFrameworkRttParamsArray(RttManager.RttParams[] params) {
        ArrayList<RttConfig> configs = new ArrayList<>(length);
        for (RttManager.RttParams halRttConfigFromFrameworkRttParams : params) {
            RttConfig config = halRttConfigFromFrameworkRttParams(halRttConfigFromFrameworkRttParams);
            if (config != null) {
                configs.add(config);
            }
        }
        return configs;
    }

    public boolean requestRtt(RttManager.RttParams[] params, WifiNative.RttEventHandler handler) {
        try {
            ArrayList<RttConfig> rttConfigs = halRttConfigArrayFromFrameworkRttParamsArray(params);
            synchronized (sLock) {
                if (this.mIWifiRttController == null) {
                    boolean boolResult = boolResult(false);
                    return boolResult;
                } else if (this.mRttCmdId != 0) {
                    boolean boolResult2 = boolResult(false);
                    return boolResult2;
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

    public boolean cancelRtt(RttManager.RttParams[] params) {
        ArrayList<RttConfig> rttConfigs = halRttConfigArrayFromFrameworkRttParamsArray(params);
        synchronized (sLock) {
            if (this.mIWifiRttController == null) {
                boolean boolResult = boolResult(false);
                return boolResult;
            } else if (this.mRttCmdId == 0) {
                boolean boolResult2 = boolResult(false);
                return boolResult2;
            } else {
                ArrayList<byte[]> addrs = new ArrayList<>(rttConfigs.size());
                Iterator<RttConfig> it = rttConfigs.iterator();
                while (it.hasNext()) {
                    addrs.add(it.next().addr);
                }
                try {
                    WifiStatus status = this.mIWifiRttController.rangeCancel(this.mRttCmdId, addrs);
                    this.mRttCmdId = 0;
                    if (!ok(status)) {
                        return false;
                    }
                    return true;
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
                this.mIWifiRttController.getResponderInfo(new IWifiRttController.getResponderInfoCallback(answer) {
                    private final /* synthetic */ WifiVendorHal.AnonymousClass3AnswerBox f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(WifiStatus wifiStatus, RttResponder rttResponder) {
                        WifiVendorHal.lambda$getRttResponder$5(WifiVendorHal.this, this.f$1, wifiStatus, rttResponder);
                    }
                });
                RttResponder rttResponder = answer.value;
                return rttResponder;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return null;
            }
        }
    }

    public static /* synthetic */ void lambda$getRttResponder$5(WifiVendorHal wifiVendorHal, AnonymousClass3AnswerBox answer, WifiStatus status, RttResponder info) {
        if (wifiVendorHal.ok(status)) {
            answer.value = info;
        }
    }

    private RttManager.ResponderConfig frameworkResponderConfigFromHalRttResponder(RttResponder info) {
        RttManager.ResponderConfig config = new RttManager.ResponderConfig();
        config.frequency = info.channel.centerFreq;
        config.centerFreq0 = info.channel.centerFreq0;
        config.centerFreq1 = info.channel.centerFreq1;
        config.channelWidth = frameworkChannelWidthFromHalChannelWidth(info.channel.width);
        config.preamble = frameworkPreambleFromHalPreamble(info.preamble);
        return config;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x005a, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x005b, code lost:
        handleRemoteException(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005f, code lost:
        return null;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    public RttManager.ResponderConfig enableRttResponder(int timeoutSeconds) {
        RttResponder info = getRttResponder();
        synchronized (sLock) {
            if (this.mIWifiRttController == null) {
                return null;
            }
            if (this.mRttResponderCmdId != 0) {
                this.mLog.err("responder mode already enabled - this shouldn't happen").flush();
                return null;
            }
            RttManager.ResponderConfig config = null;
            int id = this.mRttCmdIdNext;
            this.mRttCmdIdNext = id + 1;
            if (this.mRttCmdIdNext <= 0) {
                this.mRttCmdIdNext = 1;
            }
            if (ok(this.mIWifiRttController.enableResponder(id, null, timeoutSeconds, info))) {
                this.mRttResponderCmdId = id;
                config = frameworkResponderConfigFromHalRttResponder(info);
                WifiLog wifiLog = this.mVerboseLog;
                wifiLog.i("enabling rtt " + this.mRttResponderCmdId);
            }
            return config;
        }
    }

    public boolean disableRttResponder() {
        synchronized (sLock) {
            if (this.mIWifiRttController == null) {
                boolean boolResult = boolResult(false);
                return boolResult;
            } else if (this.mRttResponderCmdId == 0) {
                boolean boolResult2 = boolResult(false);
                return boolResult2;
            } else {
                try {
                    WifiStatus status = this.mIWifiRttController.disableResponder(this.mRttResponderCmdId);
                    this.mRttResponderCmdId = 0;
                    if (!ok(status)) {
                        return false;
                    }
                    return true;
                } catch (RemoteException e) {
                    handleRemoteException(e);
                    return false;
                }
            }
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
                IWifiStaIface iface = getStaIface(ifaceName);
                if (iface == null) {
                    boolean boolResult = boolResult(false);
                    return boolResult;
                } else if (!ok(iface.setScanningMacOui(oui))) {
                    return false;
                } else {
                    return true;
                }
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public boolean setMacAddress(String ifaceName, MacAddress mac) {
        byte[] macByteArray = mac.toByteArray();
        synchronized (sLock) {
            try {
                android.hardware.wifi.V1_2.IWifiStaIface ifaceV12 = getWifiStaIfaceForV1_2Mockable(ifaceName);
                if (ifaceV12 == null) {
                    boolean boolResult = boolResult(false);
                    return boolResult;
                } else if (!ok(ifaceV12.setMacAddress(macByteArray))) {
                    return false;
                } else {
                    return true;
                }
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public ApfCapabilities getApfCapabilities(String ifaceName) {
        synchronized (sLock) {
            try {
                IWifiStaIface iface = getStaIface(ifaceName);
                if (iface == null) {
                    this.mLog.e("mIWifiStaIface is null, getApfCapabilities(0, 0, 0)");
                    ApfCapabilities apfCapabilities = sNoApfCapabilities;
                    return apfCapabilities;
                }
                AnonymousClass4AnswerBox box = new Object() {
                    public ApfCapabilities value = WifiVendorHal.sNoApfCapabilities;
                };
                iface.getApfPacketFilterCapabilities(new IWifiStaIface.getApfPacketFilterCapabilitiesCallback(box) {
                    private final /* synthetic */ WifiVendorHal.AnonymousClass4AnswerBox f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(WifiStatus wifiStatus, StaApfPacketFilterCapabilities staApfPacketFilterCapabilities) {
                        WifiVendorHal.lambda$getApfCapabilities$6(WifiVendorHal.this, this.f$1, wifiStatus, staApfPacketFilterCapabilities);
                    }
                });
                ApfCapabilities apfCapabilities2 = box.value;
                return apfCapabilities2;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return sNoApfCapabilities;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public static /* synthetic */ void lambda$getApfCapabilities$6(WifiVendorHal wifiVendorHal, AnonymousClass4AnswerBox box, WifiStatus status, StaApfPacketFilterCapabilities capabilities) {
        if (!wifiVendorHal.ok(status)) {
            wifiVendorHal.mLog.e("getApfCapabilities failed");
            return;
        }
        box.value = new ApfCapabilities(capabilities.version, capabilities.maxLength, OsConstants.ARPHRD_ETHER);
        WifiLog wifiLog = wifiVendorHal.mLog;
        wifiLog.d("getApfCapabilities:(version:" + capabilities.version + ", maxLength:" + capabilities.maxLength + ", " + OsConstants.ARPHRD_ETHER + ")");
    }

    public boolean installPacketFilter(String ifaceName, byte[] filter) {
        if (filter == null) {
            return boolResult(false);
        }
        ArrayList<Byte> program = NativeUtil.byteArrayToArrayList(filter);
        enter("filter length %").c((long) filter.length).flush();
        synchronized (sLock) {
            try {
                IWifiStaIface iface = getStaIface(ifaceName);
                if (iface == null) {
                    boolean boolResult = boolResult(false);
                    return boolResult;
                } else if (!ok(iface.installApfPacketFilter(0, program))) {
                    return false;
                } else {
                    return true;
                }
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public byte[] readPacketFilter(String ifaceName) {
        AnonymousClass5AnswerBox answer = new Object() {
            public byte[] data = null;
        };
        enter("").flush();
        synchronized (sLock) {
            try {
                android.hardware.wifi.V1_2.IWifiStaIface ifaceV12 = getWifiStaIfaceForV1_2Mockable(ifaceName);
                if (ifaceV12 == null) {
                    byte[] byteArrayResult = byteArrayResult(null);
                    return byteArrayResult;
                }
                ifaceV12.readApfPacketFilterData(new IWifiStaIface.readApfPacketFilterDataCallback(answer) {
                    private final /* synthetic */ WifiVendorHal.AnonymousClass5AnswerBox f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                        WifiVendorHal.lambda$readPacketFilter$7(WifiVendorHal.this, this.f$1, wifiStatus, arrayList);
                    }
                });
                byte[] byteArrayResult2 = byteArrayResult(answer.data);
                return byteArrayResult2;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return byteArrayResult(null);
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public static /* synthetic */ void lambda$readPacketFilter$7(WifiVendorHal wifiVendorHal, AnonymousClass5AnswerBox answer, WifiStatus status, ArrayList dataByteArray) {
        if (wifiVendorHal.ok(status)) {
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
                        boolean boolResult = boolResult(false);
                        return boolResult;
                    } else if (!ok(iface.setCountryCode(code))) {
                        return false;
                    } else {
                        return true;
                    }
                } catch (RemoteException e) {
                    handleRemoteException(e);
                    return false;
                } catch (Throwable th) {
                    throw th;
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
                boolean boolResult = boolResult(false);
                return boolResult;
            } else if (this.mLogEventHandler != null) {
                boolean boolResult2 = boolResult(false);
                return boolResult2;
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
            if (this.mIWifiChip == null) {
                boolean boolResult = boolResult(false);
                return boolResult;
            } else if (this.mLogEventHandler == null) {
                boolean boolResult2 = boolResult(false);
                return boolResult2;
            } else {
                try {
                    if (!ok(this.mIWifiChip.enableDebugErrorAlerts(false))) {
                        return false;
                    }
                    if (!ok(this.mIWifiChip.stopLoggingToDebugRingBuffer())) {
                        return false;
                    }
                    this.mLogEventHandler = null;
                    return true;
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
            if (this.mIWifiChip == null) {
                boolean boolResult = boolResult(false);
                return boolResult;
            }
            try {
                boolean ok = ok(this.mIWifiChip.startLoggingToDebugRingBuffer(ringName, verboseLevel, maxIntervalInSec, minDataSizeInBytes));
                return ok;
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
                    public final void onValues(WifiStatus wifiStatus, IWifiChip.ChipDebugInfo chipDebugInfo) {
                        WifiVendorHal.lambda$requestChipDebugInfo$8(WifiVendorHal.this, wifiStatus, chipDebugInfo);
                    }
                });
                this.mLog.info("Driver: % Firmware: %").c(this.mDriverDescription).c(this.mFirmwareDescription).flush();
            }
        } catch (RemoteException e) {
            handleRemoteException(e);
        }
    }

    public static /* synthetic */ void lambda$requestChipDebugInfo$8(WifiVendorHal wifiVendorHal, WifiStatus status, IWifiChip.ChipDebugInfo chipDebugInfo) {
        if (wifiVendorHal.ok(status)) {
            wifiVendorHal.mDriverDescription = chipDebugInfo.driverDescription;
            wifiVendorHal.mFirmwareDescription = chipDebugInfo.firmwareDescription;
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
            public WifiNative.RingBufferStatus[] value = null;
        };
        synchronized (sLock) {
            if (this.mIWifiChip == null) {
                return null;
            }
            try {
                this.mIWifiChip.getDebugRingBuffersStatus(new IWifiChip.getDebugRingBuffersStatusCallback(ans) {
                    private final /* synthetic */ WifiVendorHal.AnonymousClass6AnswerBox f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                        WifiVendorHal.lambda$getRingBufferStatus$9(WifiVendorHal.this, this.f$1, wifiStatus, arrayList);
                    }
                });
                return ans.value;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return null;
            }
        }
    }

    public static /* synthetic */ void lambda$getRingBufferStatus$9(WifiVendorHal wifiVendorHal, AnonymousClass6AnswerBox ans, WifiStatus status, ArrayList ringBuffers) {
        if (wifiVendorHal.ok(status)) {
            ans.value = makeRingBufferStatusArray(ringBuffers);
        }
    }

    public boolean getRingBufferData(String ringName) {
        enter("ringName %").c(ringName).flush();
        synchronized (sLock) {
            if (this.mIWifiChip == null) {
                boolean boolResult = boolResult(false);
                return boolResult;
            }
            try {
                boolean ok = ok(this.mIWifiChip.forceDumpToDebugRingBuffer(ringName));
                return ok;
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
                this.mIWifiChip.requestFirmwareDebugDump(new IWifiChip.requestFirmwareDebugDumpCallback(ans) {
                    private final /* synthetic */ WifiVendorHal.AnonymousClass7AnswerBox f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                        WifiVendorHal.lambda$getFwMemoryDump$10(WifiVendorHal.this, this.f$1, wifiStatus, arrayList);
                    }
                });
                return ans.value;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return null;
            }
        }
    }

    public static /* synthetic */ void lambda$getFwMemoryDump$10(WifiVendorHal wifiVendorHal, AnonymousClass7AnswerBox ans, WifiStatus status, ArrayList blob) {
        if (wifiVendorHal.ok(status)) {
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
                this.mIWifiChip.requestDriverDebugDump(new IWifiChip.requestDriverDebugDumpCallback(ans) {
                    private final /* synthetic */ WifiVendorHal.AnonymousClass8AnswerBox f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                        WifiVendorHal.lambda$getDriverStateDump$11(WifiVendorHal.this, this.f$1, wifiStatus, arrayList);
                    }
                });
                return ans.value;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return null;
            }
        }
    }

    public static /* synthetic */ void lambda$getDriverStateDump$11(WifiVendorHal wifiVendorHal, AnonymousClass8AnswerBox ans, WifiStatus status, ArrayList blob) {
        if (wifiVendorHal.ok(status)) {
            ans.value = NativeUtil.byteArrayFromArrayList(blob);
        }
    }

    public boolean startPktFateMonitoring(String ifaceName) {
        synchronized (sLock) {
            android.hardware.wifi.V1_0.IWifiStaIface iface = getStaIface(ifaceName);
            if (iface == null) {
                boolean boolResult = boolResult(false);
                return boolResult;
            }
            try {
                boolean ok = ok(iface.startDebugPacketFateMonitoring());
                return ok;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    private byte halToFrameworkPktFateFrameType(int type) {
        switch (type) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            default:
                throw new IllegalArgumentException("bad " + type);
        }
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
                boolean boolResult = boolResult(false);
                return boolResult;
            }
            try {
                MutableBoolean ok = new MutableBoolean(false);
                iface.getDebugTxPacketFates(new IWifiStaIface.getDebugTxPacketFatesCallback(reportBufs, ok) {
                    private final /* synthetic */ WifiNative.TxFateReport[] f$1;
                    private final /* synthetic */ MutableBoolean f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                        WifiVendorHal.lambda$getTxPktFates$12(WifiVendorHal.this, this.f$1, this.f$2, wifiStatus, arrayList);
                    }
                });
                boolean z = ok.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getTxPktFates$12(WifiVendorHal wifiVendorHal, WifiNative.TxFateReport[] reportBufs, MutableBoolean ok, WifiStatus status, ArrayList fates) {
        WifiVendorHal wifiVendorHal2 = wifiVendorHal;
        WifiNative.TxFateReport[] txFateReportArr = reportBufs;
        if (wifiVendorHal2.ok(status)) {
            int i = 0;
            Iterator it = fates.iterator();
            while (it.hasNext()) {
                WifiDebugTxPacketFateReport fate = (WifiDebugTxPacketFateReport) it.next();
                if (i >= txFateReportArr.length) {
                    break;
                }
                WifiNative.TxFateReport txFateReport = new WifiNative.TxFateReport(wifiVendorHal2.halToFrameworkTxPktFate(fate.fate), fate.frameInfo.driverTimestampUsec, wifiVendorHal2.halToFrameworkPktFateFrameType(fate.frameInfo.frameType), NativeUtil.byteArrayFromArrayList(fate.frameInfo.frameContent));
                txFateReportArr[i] = txFateReport;
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
                boolean boolResult = boolResult(false);
                return boolResult;
            }
            try {
                MutableBoolean ok = new MutableBoolean(false);
                iface.getDebugRxPacketFates(new IWifiStaIface.getDebugRxPacketFatesCallback(reportBufs, ok) {
                    private final /* synthetic */ WifiNative.RxFateReport[] f$1;
                    private final /* synthetic */ MutableBoolean f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                        WifiVendorHal.lambda$getRxPktFates$13(WifiVendorHal.this, this.f$1, this.f$2, wifiStatus, arrayList);
                    }
                });
                boolean z = ok.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getRxPktFates$13(WifiVendorHal wifiVendorHal, WifiNative.RxFateReport[] reportBufs, MutableBoolean ok, WifiStatus status, ArrayList fates) {
        WifiVendorHal wifiVendorHal2 = wifiVendorHal;
        WifiNative.RxFateReport[] rxFateReportArr = reportBufs;
        if (wifiVendorHal2.ok(status)) {
            int i = 0;
            Iterator it = fates.iterator();
            while (it.hasNext()) {
                WifiDebugRxPacketFateReport fate = (WifiDebugRxPacketFateReport) it.next();
                if (i >= rxFateReportArr.length) {
                    break;
                }
                WifiNative.RxFateReport rxFateReport = new WifiNative.RxFateReport(wifiVendorHal2.halToFrameworkRxPktFate(fate.fate), fate.frameInfo.driverTimestampUsec, wifiVendorHal2.halToFrameworkPktFateFrameType(fate.frameInfo.frameType), NativeUtil.byteArrayFromArrayList(fate.frameInfo.frameContent));
                rxFateReportArr[i] = rxFateReport;
                i++;
            }
            ok.value = true;
        }
    }

    public int startSendingOffloadedPacket(String ifaceName, int slot, byte[] srcMac, byte[] dstMac, byte[] packet, int protocol, int periodInMs) {
        int i = slot;
        int i2 = periodInMs;
        enter("slot=% periodInMs=%").c((long) i).c((long) i2).flush();
        ArrayList<Byte> data = NativeUtil.byteArrayToArrayList(packet);
        synchronized (sLock) {
            try {
                android.hardware.wifi.V1_0.IWifiStaIface iface = getStaIface(ifaceName);
                if (iface == null) {
                    return -1;
                }
                try {
                    if (!ok(iface.startSendingKeepAlivePackets(i, data, (short) protocol, srcMac, dstMac, i2))) {
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
                int i3 = protocol;
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
                this.mIWifiChip.getDebugHostWakeReasonStats(new IWifiChip.getDebugHostWakeReasonStatsCallback(ans) {
                    private final /* synthetic */ WifiVendorHal.AnonymousClass9AnswerBox f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(WifiStatus wifiStatus, WifiDebugHostWakeReasonStats wifiDebugHostWakeReasonStats) {
                        WifiVendorHal.lambda$getWlanWakeReasonCount$14(WifiVendorHal.this, this.f$1, wifiStatus, wifiDebugHostWakeReasonStats);
                    }
                });
                WifiWakeReasonAndCounts halToFrameworkWakeReasons = halToFrameworkWakeReasons(ans.value);
                return halToFrameworkWakeReasons;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return null;
            }
        }
    }

    public static /* synthetic */ void lambda$getWlanWakeReasonCount$14(WifiVendorHal wifiVendorHal, AnonymousClass9AnswerBox ans, WifiStatus status, WifiDebugHostWakeReasonStats stats) {
        if (wifiVendorHal.ok(status)) {
            ans.value = stats;
        }
    }

    public boolean configureNeighborDiscoveryOffload(String ifaceName, boolean enabled) {
        enter("enabled=%").c(enabled).flush();
        synchronized (sLock) {
            android.hardware.wifi.V1_0.IWifiStaIface iface = getStaIface(ifaceName);
            if (iface == null) {
                boolean boolResult = boolResult(false);
                return boolResult;
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
                boolean boolResult = boolResult(false);
                return boolResult;
            }
            try {
                MutableBoolean ok = new MutableBoolean(false);
                iface.getRoamingCapabilities(new IWifiStaIface.getRoamingCapabilitiesCallback(capabilities, ok) {
                    private final /* synthetic */ WifiNative.RoamingCapabilities f$1;
                    private final /* synthetic */ MutableBoolean f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void onValues(WifiStatus wifiStatus, StaRoamingCapabilities staRoamingCapabilities) {
                        WifiVendorHal.lambda$getRoamingCapabilities$15(WifiVendorHal.this, this.f$1, this.f$2, wifiStatus, staRoamingCapabilities);
                    }
                });
                boolean z = ok.value;
                return z;
            } catch (RemoteException e) {
                handleRemoteException(e);
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$getRoamingCapabilities$15(WifiVendorHal wifiVendorHal, WifiNative.RoamingCapabilities out, MutableBoolean ok, WifiStatus status, StaRoamingCapabilities cap) {
        if (wifiVendorHal.ok(status)) {
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
                return 6;
            }
            switch (state) {
                case 0:
                    val = 0;
                    break;
                case 1:
                    val = 1;
                    break;
                default:
                    try {
                        this.mLog.err("enableFirmwareRoaming invalid argument %").c((long) state).flush();
                        return 7;
                    } catch (RemoteException e) {
                        handleRemoteException(e);
                        return 9;
                    }
            }
            WifiStatus status = iface.setRoamingState(val);
            WifiLog wifiLog = this.mVerboseLog;
            wifiLog.d("setRoamingState returned " + status.code);
            int i = status.code;
            return i;
        }
    }

    public boolean configureRoaming(String ifaceName, WifiNative.RoamingConfig config) {
        synchronized (sLock) {
            android.hardware.wifi.V1_0.IWifiStaIface iface = getStaIface(ifaceName);
            if (iface == null) {
                boolean boolResult = boolResult(false);
                return boolResult;
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
                        String unquotedSsidStr = WifiInfo.removeDoubleQuotes(it2.next());
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
        if (this.mIWifiChip == null) {
            return null;
        }
        return android.hardware.wifi.V1_1.IWifiChip.castFrom(this.mIWifiChip);
    }

    /* access modifiers changed from: protected */
    public android.hardware.wifi.V1_2.IWifiChip getWifiChipForV1_2Mockable() {
        if (this.mIWifiChip == null) {
            return null;
        }
        return android.hardware.wifi.V1_2.IWifiChip.castFrom(this.mIWifiChip);
    }

    /* access modifiers changed from: protected */
    public android.hardware.wifi.V1_2.IWifiStaIface getWifiStaIfaceForV1_2Mockable(String ifaceName) {
        android.hardware.wifi.V1_0.IWifiStaIface iface = getStaIface(ifaceName);
        if (iface == null) {
            return null;
        }
        return android.hardware.wifi.V1_2.IWifiStaIface.castFrom(iface);
    }

    private int frameworkToHalTxPowerScenario(int scenario) {
        if (scenario == 1) {
            return 0;
        }
        throw new IllegalArgumentException("bad scenario: " + scenario);
    }

    public boolean selectTxPowerScenario(int scenario) {
        WifiStatus status;
        synchronized (sLock) {
            try {
                android.hardware.wifi.V1_1.IWifiChip iWifiChipV11 = getWifiChipForV1_1Mockable();
                if (iWifiChipV11 == null) {
                    boolean boolResult = boolResult(false);
                    return boolResult;
                }
                if (scenario != 0) {
                    try {
                        status = iWifiChipV11.selectTxPowerScenario(frameworkToHalTxPowerScenario(scenario));
                    } catch (IllegalArgumentException e) {
                        this.mLog.err("Illegal argument for select tx power scenario").c(e.toString()).flush();
                        return false;
                    }
                } else {
                    status = iWifiChipV11.resetTxPowerScenario();
                }
                if (!ok(status)) {
                    return false;
                }
                return true;
            } catch (RemoteException e2) {
                handleRemoteException(e2);
                return false;
            } catch (Throwable th) {
                throw th;
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
        frameworkScanResult.SSID = NativeUtil.encodeSsid(scanResult.ssid);
        frameworkScanResult.wifiSsid = WifiSsid.createFromByteArray(NativeUtil.byteArrayFromArrayList(scanResult.ssid));
        frameworkScanResult.BSSID = NativeUtil.macAddressFromByteArray(scanResult.bssid);
        frameworkScanResult.level = scanResult.rssi;
        frameworkScanResult.frequency = scanResult.frequency;
        frameworkScanResult.timestamp = scanResult.timeStampInUs;
        frameworkScanResult.capabilities = generateScanResultCapabilities(frameworkScanResult.informationElements, scanResult.capability);
        return frameworkScanResult;
    }

    private static String generateScanResultCapabilities(ScanResult.InformationElement[] ies, short capabilityInt) {
        BitSet hidlCapability = new BitSet(16);
        for (int i = 0; i < 16; i++) {
            if (((1 << i) & capabilityInt) != 0) {
                hidlCapability.set(i);
            }
        }
        InformationElementUtil.Capabilities capabilities = new InformationElementUtil.Capabilities();
        capabilities.from(ies, hidlCapability);
        return capabilities.generateCapabilitiesString();
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
            int flags = hidlToFrameworkScanDataFlags(scanData.flags);
            int i2 = cmdId;
            int i3 = flags;
            WifiScanner.ScanData scanData2 = new WifiScanner.ScanData(i2, i3, scanData.bucketsScanned, false, hidlToFrameworkScanResults(scanData.results));
            frameworkScanDatas[i] = scanData2;
            i++;
        }
        return frameworkScanDatas;
    }
}
