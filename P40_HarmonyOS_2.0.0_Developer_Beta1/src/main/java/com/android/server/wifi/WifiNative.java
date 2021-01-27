package com.android.server.wifi;

import android.app.AlarmManager;
import android.net.InterfaceConfiguration;
import android.net.MacAddress;
import android.net.TrafficStats;
import android.net.apf.ApfCapabilities;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiScanner;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.Immutable;
import com.android.internal.util.HexDump;
import com.android.server.net.BaseNetworkObserver;
import com.android.server.wifi.HalDeviceManager;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.hwUtil.WifiCommonUtils;
import com.android.server.wifi.util.FrameParser;
import com.android.server.wifi.util.NativeUtil;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;

public class WifiNative implements IHwWifiNativeInner {
    private static final int BASIC_PROBE_REQUEST_FRAME_SIZE = 26;
    public static final int BLUETOOTH_COEXISTENCE_MODE_DISABLED = 1;
    public static final int BLUETOOTH_COEXISTENCE_MODE_ENABLED = 0;
    public static final int BLUETOOTH_COEXISTENCE_MODE_SENSE = 2;
    private static final int CONNECT_TO_HOSTAPD_RETRY_INTERVAL_MS = 100;
    private static final int CONNECT_TO_HOSTAPD_RETRY_TIMES = 50;
    private static final int CONNECT_TO_SUPPLICANT_RETRY_INTERVAL_MS = 50;
    private static final int CONNECT_TO_SUPPLICANT_RETRY_TIMES = 70;
    private static final long DEFAULT_DELAY_60_SECS = 60000;
    public static final int DISABLE_FIRMWARE_ROAMING = 0;
    public static final int EAP_SIM_NOT_SUBSCRIBED = 1031;
    public static final int EAP_SIM_VENDOR_SPECIFIC_CERT_EXPIRED = 16385;
    public static final int ENABLE_FIRMWARE_ROAMING = 1;
    public static final int RX_FILTER_TYPE_V4_MULTICAST = 0;
    public static final int RX_FILTER_TYPE_V6_MULTICAST = 1;
    public static final int SCAN_TYPE_HIGH_ACCURACY = 2;
    public static final int SCAN_TYPE_LOW_LATENCY = 0;
    public static final int SCAN_TYPE_LOW_POWER = 1;
    public static final int SEND_MGMT_FRAME_ERROR_ALREADY_STARTED = 5;
    public static final int SEND_MGMT_FRAME_ERROR_MCS_UNSUPPORTED = 2;
    public static final int SEND_MGMT_FRAME_ERROR_NO_ACK = 3;
    public static final int SEND_MGMT_FRAME_ERROR_TIMEOUT = 4;
    public static final int SEND_MGMT_FRAME_ERROR_UNKNOWN = 1;
    public static final int SET_FIRMWARE_ROAMING_BUSY = 2;
    public static final int SET_FIRMWARE_ROAMING_FAILURE = 1;
    public static final int SET_FIRMWARE_ROAMING_SUCCESS = 0;
    public static final String SIM_AUTH_RESP_TYPE_GSM_AUTH = "GSM-AUTH";
    public static final String SIM_AUTH_RESP_TYPE_UMTS_AUTH = "UMTS-AUTH";
    public static final String SIM_AUTH_RESP_TYPE_UMTS_AUTS = "UMTS-AUTS";
    private static final String TAG = "WifiNative";
    public static final int WIFI_SCAN_FAILED = 3;
    public static final int WIFI_SCAN_RESULTS_AVAILABLE = 0;
    public static final int WIFI_SCAN_THRESHOLD_NUM_SCANS = 1;
    public static final int WIFI_SCAN_THRESHOLD_PERCENT = 2;
    private AlarmManager mAlarmManager;
    private AlarmManager.OnAlarmListener mCheckWifiHalIsUsedListener = new AlarmManager.OnAlarmListener() {
        /* class com.android.server.wifi.WifiNative.AnonymousClass1 */

        @Override // android.app.AlarmManager.OnAlarmListener
        public void onAlarm() {
            synchronized (WifiNative.this.mLock) {
                if (WifiNative.this.mAlarmManager != null) {
                    if (!WifiNative.this.isWifiHalUsed()) {
                        WifiNative.this.mWifiVendorHal.reset();
                        WifiNative.this.mHwWifiNativeEx.hwUnregisterHwWifiExt();
                        SystemProperties.set(SupplicantStaIfaceHal.INIT_STOP_PROPERTY, "wificond");
                        Log.i(WifiNative.TAG, "shutdown wificond service");
                    }
                    WifiNative.this.mAlarmManager.cancel(WifiNative.this.mCheckWifiHalIsUsedListener);
                    Log.i(WifiNative.TAG, "alarm cancel");
                }
            }
        }
    };
    private final Handler mHandler;
    private final HostapdHal mHostapdHal;
    public final IHwWifiNativeEx mHwWifiNativeEx;
    private final IfaceManager mIfaceMgr = new IfaceManager();
    private final Object mLock = new Object();
    private final INetworkManagementService mNwManagementService;
    private final PropertyService mPropertyService;
    private final Random mRandom;
    private HashSet<StatusListener> mStatusListeners = new HashSet<>();
    private final SupplicantStaIfaceHal mSupplicantStaIfaceHal;
    private boolean mVerboseLoggingEnabled = false;
    private final WifiMetrics mWifiMetrics;
    private final WifiMonitor mWifiMonitor;
    private final WifiVendorHal mWifiVendorHal;
    private final WificondControl mWificondControl;

    public static class BucketSettings {
        public int band;
        public int bucket;
        public ChannelSettings[] channels;
        public int max_period_ms;
        public int num_channels;
        public int period_ms;
        public int report_events;
        public int step_count;
    }

    public static class ChannelSettings {
        public int dwell_time_ms;
        public int frequency;
        public boolean passive;
    }

    public interface DppEventCallback {
        void onFailure(int i);

        void onProgress(int i);

        void onSuccessConfigReceived(WifiConfiguration wifiConfiguration);

        void onSuccessConfigSent();
    }

    public interface HostapdDeathEventHandler {
        void onDeath();
    }

    public interface InterfaceCallback {
        void onDestroyed(String str);

        void onDown(String str);

        void onUp(String str);
    }

    public interface PnoEventHandler {
        void onPnoNetworkFound(ScanResult[] scanResultArr);

        void onPnoScanFailed();
    }

    public static class PnoSettings {
        public int band5GHzBonus;
        public int currentConnectionBonus;
        public int initialScoreMax;
        public boolean isConnected;
        public int min24GHzRssi;
        public int min5GHzRssi;
        public PnoNetwork[] networkList;
        public int periodInMs;
        public int sameNetworkBonus;
        public int secureBonus;
    }

    public static class RoamingCapabilities {
        public int maxBlacklistSize;
        public int maxWhitelistSize;
    }

    public static class RoamingConfig {
        public ArrayList<String> blacklistBssids;
        public ArrayList<String> whitelistSsids;
    }

    public static class ScanCapabilities {
        public int max_ap_cache_per_scan;
        public int max_rssi_sample_size;
        public int max_scan_buckets;
        public int max_scan_cache_size;
        public int max_scan_reporting_threshold;
    }

    public interface ScanEventHandler {
        void onFullScanResult(ScanResult scanResult, int i);

        void onScanPaused(WifiScanner.ScanData[] scanDataArr);

        void onScanRestarted();

        void onScanStatus(int i);
    }

    public interface SendMgmtFrameCallback {
        void onAck(int i);

        void onFailure(int i);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SendMgmtFrameError {
    }

    public static class SignalPollResult {
        public int associationFrequency;
        public int currentChload;
        public int currentNoise;
        public int currentRssi;
        public int currentRxBytes;
        public int currentRxPackets;
        public int currentSnr;
        public int currentTxBytes;
        public int currentTxFailed;
        public int currentTxPackets;
        public int currentUlDelay;
        public int rxBitrate;
        public int txBitrate;
    }

    public interface SoftApListener {
        void OnApLinkedStaJoin(String str);

        void OnApLinkedStaLeave(String str);

        void onFailure();

        void onNumAssociatedStationsChanged(int i);

        void onSoftApChannelSwitched(int i, int i2);
    }

    public interface StatusListener {
        void onStatusChanged(boolean z);
    }

    public interface SupplicantDeathEventHandler {
        void onDeath();
    }

    public static class TxPacketCounters {
        public int txFailed;
        public int txSucceeded;
    }

    public interface VendorHalDeathEventHandler {
        void onDeath();
    }

    public interface VendorHalRadioModeChangeEventHandler {
        void onDbs();

        void onMcc(int i);

        void onSbs(int i);

        void onScc(int i);
    }

    public interface WifiLoggerEventHandler {
        void onRingBufferData(RingBufferStatus ringBufferStatus, byte[] bArr);

        void onWifiAlert(int i, byte[] bArr);
    }

    public interface WifiRssiEventHandler {
        void onRssiThresholdBreached(byte b);
    }

    public interface WificondDeathEventHandler {
        void onDeath();
    }

    private static native byte[] readKernelLogNative();

    private static native int registerNatives();

    public WifiNative(WifiVendorHal vendorHal, SupplicantStaIfaceHal staIfaceHal, HostapdHal hostapdHal, WificondControl condControl, WifiMonitor wifiMonitor, INetworkManagementService nwService, PropertyService propertyService, WifiMetrics wifiMetrics, Handler handler, Random random) {
        this.mWifiVendorHal = vendorHal;
        this.mSupplicantStaIfaceHal = staIfaceHal;
        this.mHostapdHal = hostapdHal;
        this.mWificondControl = condControl;
        this.mWifiMonitor = wifiMonitor;
        this.mNwManagementService = nwService;
        this.mPropertyService = propertyService;
        this.mWifiMetrics = wifiMetrics;
        this.mHwWifiNativeEx = HwWifiServiceFactory.getHwWifiNativeEx(this, this.mSupplicantStaIfaceHal);
        this.mHandler = handler;
        this.mRandom = random;
    }

    public void setAlarmManager(AlarmManager alarmManager) {
        synchronized (this.mLock) {
            this.mAlarmManager = alarmManager;
        }
    }

    public void enableVerboseLogging(int verbose) {
        this.mVerboseLoggingEnabled = verbose > 0;
        this.mWificondControl.enableVerboseLogging(this.mVerboseLoggingEnabled);
        this.mSupplicantStaIfaceHal.enableVerboseLogging(this.mVerboseLoggingEnabled);
        this.mWifiVendorHal.enableVerboseLogging(this.mVerboseLoggingEnabled);
    }

    /* access modifiers changed from: private */
    public static class Iface {
        public static final int IFACE_TYPE_AP = 0;
        public static final int IFACE_TYPE_STA_FOR_CONNECTIVITY = 1;
        public static final int IFACE_TYPE_STA_FOR_SCAN = 2;
        public InterfaceCallback externalListener;
        public long featureSet;
        public final int id;
        public boolean isUp;
        public String name;
        public NetworkObserverInternal networkObserver;
        public final int type;

        @Retention(RetentionPolicy.SOURCE)
        public @interface IfaceType {
        }

        Iface(int id2, int type2) {
            this.id = id2;
            this.type = type2;
        }

        public String toString() {
            String typeString;
            StringBuffer sb = new StringBuffer();
            int i = this.type;
            if (i == 0) {
                typeString = "AP";
            } else if (i == 1) {
                typeString = "STA_CONNECTIVITY";
            } else if (i != 2) {
                typeString = "<UNKNOWN>";
            } else {
                typeString = "STA_SCAN";
            }
            sb.append("Iface:");
            sb.append("{");
            sb.append("Name=");
            sb.append(this.name);
            sb.append(",");
            sb.append("Id=");
            sb.append(this.id);
            sb.append(",");
            sb.append("Type=");
            sb.append(typeString);
            sb.append("}");
            return sb.toString();
        }
    }

    /* access modifiers changed from: private */
    public static class IfaceManager {
        private final Object mIfaceLock;
        private HashMap<Integer, Iface> mIfaces;
        private int mNextId;

        private IfaceManager() {
            this.mIfaces = new HashMap<>();
            this.mIfaceLock = new Object();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private Iface allocateIface(int type) {
            Iface iface;
            synchronized (this.mIfaceLock) {
                iface = new Iface(this.mNextId, type);
                this.mIfaces.put(Integer.valueOf(this.mNextId), iface);
                this.mNextId++;
            }
            return iface;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private Iface removeIface(int id) {
            Iface remove;
            synchronized (this.mIfaceLock) {
                remove = this.mIfaces.remove(Integer.valueOf(id));
            }
            return remove;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private Iface getIface(int id) {
            return this.mIfaces.get(Integer.valueOf(id));
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private Iface getIface(String ifaceName) {
            synchronized (this.mIfaceLock) {
                for (Iface iface : this.mIfaces.values()) {
                    if (TextUtils.equals(iface.name, ifaceName)) {
                        return iface;
                    }
                }
                return null;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private Iterator<Integer> getIfaceIdIter() {
            return this.mIfaces.keySet().iterator();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean hasAnyIface() {
            return !this.mIfaces.isEmpty();
        }

        private boolean hasAnyIfaceOfType(int type) {
            synchronized (this.mIfaceLock) {
                for (Iface iface : this.mIfaces.values()) {
                    if (iface.type == type) {
                        return true;
                    }
                }
                return false;
            }
        }

        private Iface findAnyIfaceOfType(int type) {
            synchronized (this.mIfaceLock) {
                for (Iface iface : this.mIfaces.values()) {
                    if (iface.type == type) {
                        return iface;
                    }
                }
                return null;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean hasAnyStaIfaceForConnectivity() {
            return hasAnyIfaceOfType(1);
        }

        private boolean hasAnyStaIfaceForScan() {
            return hasAnyIfaceOfType(2);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean hasAnyApIface() {
            return hasAnyIfaceOfType(0);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String findAnyStaIfaceName() {
            Iface iface = findAnyIfaceOfType(1);
            if (iface == null) {
                iface = findAnyIfaceOfType(2);
            }
            if (iface == null) {
                return null;
            }
            return iface.name;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String findAnyApIfaceName() {
            Iface iface = findAnyIfaceOfType(0);
            if (iface == null) {
                return null;
            }
            return iface.name;
        }

        public Iface removeExistingIface(int newIfaceId) {
            Iface removedIface;
            synchronized (this.mIfaceLock) {
                removedIface = null;
                if (this.mIfaces.size() > 2) {
                    Log.wtf(WifiNative.TAG, "More than 1 existing interface found");
                }
                Iterator<Map.Entry<Integer, Iface>> iter = this.mIfaces.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<Integer, Iface> entry = iter.next();
                    if (entry.getKey().intValue() != newIfaceId) {
                        removedIface = entry.getValue();
                        iter.remove();
                    }
                }
            }
            return removedIface;
        }
    }

    private boolean startHal() {
        synchronized (this.mLock) {
            Log.i(TAG, "startHal cancel listener");
            if (this.mAlarmManager != null) {
                this.mAlarmManager.cancel(this.mCheckWifiHalIsUsedListener);
            }
            if (this.mHwWifiNativeEx.isSpHalNull()) {
                SystemProperties.set(SupplicantStaIfaceHal.INIT_START_PROPERTY, "wificond");
                this.mHwWifiNativeEx.hwRegisterNatives();
                this.mWifiVendorHal.initialize();
                Log.i(TAG, "startHal start wificond & hild service");
            }
            if (!this.mIfaceMgr.hasAnyIface()) {
                if (!this.mWifiVendorHal.isVendorHalSupported()) {
                    Log.i(TAG, "Vendor Hal not supported, ignoring start.");
                } else if (!this.mWifiVendorHal.startVendorHal()) {
                    Log.e(TAG, "Failed to start vendor HAL");
                    return false;
                }
            }
            return true;
        }
    }

    private void stopHalAndWificondIfNecessary() {
        synchronized (this.mLock) {
            if (!this.mIfaceMgr.hasAnyIface()) {
                if (!this.mWificondControl.tearDownInterfaces()) {
                    Log.e(TAG, "Failed to teardown ifaces from wificond");
                }
                if (this.mWifiVendorHal.isVendorHalSupported()) {
                    this.mWifiVendorHal.stopVendorHal();
                } else {
                    Log.i(TAG, "Vendor Hal not supported, ignoring stop.");
                }
            }
        }
    }

    private boolean startAndWaitForSupplicantConnection() {
        if (!this.mSupplicantStaIfaceHal.isInitializationStarted() && !this.mSupplicantStaIfaceHal.initialize()) {
            return false;
        }
        if (!this.mSupplicantStaIfaceHal.startDaemon()) {
            Log.e(TAG, "Failed to startup supplicant");
            return false;
        }
        boolean connected = false;
        int connectTries = 0;
        while (true) {
            if (connected) {
                break;
            }
            int connectTries2 = connectTries + 1;
            if (connectTries >= CONNECT_TO_SUPPLICANT_RETRY_TIMES) {
                break;
            }
            connected = this.mSupplicantStaIfaceHal.isInitializationComplete() && this.mSupplicantStaIfaceHal.isStaIfacesEmpty();
            if (connected) {
                break;
            }
            try {
                Log.w(TAG, "startMonitoring connectTries sleep:70");
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
            connectTries = connectTries2;
        }
        return connected;
    }

    private boolean startSupplicant() {
        synchronized (this.mLock) {
            if (!this.mIfaceMgr.hasAnyStaIfaceForConnectivity()) {
                if (!startAndWaitForSupplicantConnection()) {
                    Log.e(TAG, "Failed to connect to supplicant");
                    return false;
                } else if (!this.mSupplicantStaIfaceHal.registerDeathHandler(new SupplicantDeathHandlerInternal())) {
                    Log.e(TAG, "Failed to register supplicant death handler");
                    return false;
                }
            }
            return true;
        }
    }

    private void stopSupplicantIfNecessary() {
        synchronized (this.mLock) {
            if (!this.mIfaceMgr.hasAnyStaIfaceForConnectivity()) {
                if (!this.mSupplicantStaIfaceHal.deregisterDeathHandler()) {
                    Log.e(TAG, "Failed to deregister supplicant death handler");
                }
                this.mSupplicantStaIfaceHal.terminate();
            }
        }
    }

    private boolean startHostapd() {
        synchronized (this.mLock) {
            if (!this.mIfaceMgr.hasAnyApIface()) {
                if (!startAndWaitForHostapdConnection()) {
                    Log.e(TAG, "Failed to connect to hostapd");
                    return false;
                } else if (!this.mHostapdHal.registerDeathHandler(new HostapdDeathHandlerInternal())) {
                    Log.e(TAG, "Failed to register hostapd death handler");
                    return false;
                }
            }
            return true;
        }
    }

    private void stopHostapdIfNecessary() {
        synchronized (this.mLock) {
            if (!this.mIfaceMgr.hasAnyApIface()) {
                if (!this.mHostapdHal.deregisterDeathHandler()) {
                    Log.e(TAG, "Failed to deregister hostapd death handler");
                }
                this.mHostapdHal.terminate();
            }
        }
    }

    private boolean registerNetworkObserver(NetworkObserverInternal observer) {
        if (observer == null) {
            return false;
        }
        try {
            this.mNwManagementService.registerObserver(observer);
            return true;
        } catch (RemoteException | IllegalStateException e) {
            Log.e(TAG, "Unable to register observer", e);
            return false;
        }
    }

    private boolean unregisterNetworkObserver(NetworkObserverInternal observer) {
        if (observer == null) {
            return false;
        }
        try {
            this.mNwManagementService.unregisterObserver(observer);
            return true;
        } catch (RemoteException | IllegalStateException e) {
            Log.e(TAG, "Unable to unregister observer", e);
            return false;
        }
    }

    private void onClientInterfaceForConnectivityDestroyed(Iface iface) {
        synchronized (this.mLock) {
            this.mWifiMonitor.stopMonitoring(iface.name);
            if (!unregisterNetworkObserver(iface.networkObserver)) {
                Log.e(TAG, "Failed to unregister network observer on " + iface);
            }
            if (!this.mSupplicantStaIfaceHal.teardownIface(iface.name)) {
                Log.e(TAG, "Failed to teardown iface in supplicant on " + iface);
            }
            if (!this.mWificondControl.tearDownClientInterface(iface.name)) {
                Log.e(TAG, "Failed to teardown iface in wificond on " + iface);
            }
            stopSupplicantIfNecessary();
            stopHalAndWificondIfNecessary();
        }
    }

    private void onClientInterfaceForScanDestroyed(Iface iface) {
        synchronized (this.mLock) {
            this.mWifiMonitor.stopMonitoring(iface.name);
            if (!unregisterNetworkObserver(iface.networkObserver)) {
                Log.e(TAG, "Failed to unregister network observer on " + iface);
            }
            if (!this.mWificondControl.tearDownClientInterface(iface.name)) {
                Log.e(TAG, "Failed to teardown iface in wificond on " + iface);
            }
            stopHalAndWificondIfNecessary();
        }
    }

    private void onSoftApInterfaceDestroyed(Iface iface) {
        synchronized (this.mLock) {
            if (!unregisterNetworkObserver(iface.networkObserver)) {
                Log.e(TAG, "Failed to unregister network observer on " + iface);
            }
            if (!this.mHostapdHal.removeAccessPoint(iface.name)) {
                Log.e(TAG, "Failed to remove access point on " + iface);
            }
            if (!this.mWificondControl.tearDownSoftApInterface(iface.name)) {
                Log.e(TAG, "Failed to teardown iface in wificond on " + iface);
            }
            stopHostapdIfNecessary();
            stopHalAndWificondIfNecessary();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onInterfaceDestroyed(Iface iface) {
        synchronized (this.mLock) {
            if (iface.type == 1) {
                onClientInterfaceForConnectivityDestroyed(iface);
            } else if (iface.type == 2) {
                onClientInterfaceForScanDestroyed(iface);
            } else if (iface.type == 0) {
                onSoftApInterfaceDestroyed(iface);
            }
            iface.externalListener.onDestroyed(iface.name);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isWifiHalUsed() {
        synchronized (this.mLock) {
            WifiInjector wifiInjector = WifiInjector.getInstance();
            if (!(this.mIfaceMgr == null || wifiInjector == null)) {
                if (wifiInjector.getWifiSettingsStore() != null) {
                    Log.i(TAG, "wifiEnabled:" + wifiInjector.getWifiSettingsStore().isWifiToggleEnabled() + " scanEnabled:" + wifiInjector.getWifiSettingsStore().isScanAlwaysAvailable() + " ifaceMgr.isNotEmpty:" + this.mIfaceMgr.hasAnyIface() + " location:" + wifiInjector.getWifiPermissionsUtil().isLocationModeEnabled());
                    if (wifiInjector.getWifiPermissionsUtil().isLocationModeEnabled()) {
                        if (!this.mIfaceMgr.hasAnyIface() && !wifiInjector.getWifiSettingsStore().isScanAlwaysAvailable()) {
                            if (!wifiInjector.getWifiSettingsStore().isWifiToggleEnabled()) {
                                return false;
                            }
                        }
                        return true;
                    }
                    if (!this.mIfaceMgr.hasAnyIface()) {
                        if (!wifiInjector.getWifiSettingsStore().isWifiToggleEnabled()) {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public class InterfaceDestoyedListenerInternal implements HalDeviceManager.InterfaceDestroyedListener {
        private final int mInterfaceId;

        InterfaceDestoyedListenerInternal(int ifaceId) {
            this.mInterfaceId = ifaceId;
        }

        @Override // com.android.server.wifi.HalDeviceManager.InterfaceDestroyedListener
        public void onDestroyed(String ifaceName) {
            synchronized (WifiNative.this.mLock) {
                Iface iface = WifiNative.this.mIfaceMgr.removeIface(this.mInterfaceId);
                if (iface == null) {
                    if (WifiNative.this.mVerboseLoggingEnabled) {
                        Log.v(WifiNative.TAG, "Received iface destroyed notification on an invalid iface=" + ifaceName);
                    }
                    return;
                }
                WifiNative.this.onInterfaceDestroyed(iface);
                Log.i(WifiNative.TAG, "Successfully torn down " + iface);
                if (!WifiNative.this.isWifiHalUsed() && WifiNative.this.mAlarmManager != null) {
                    Log.i(WifiNative.TAG, "start click");
                    WifiNative.this.mAlarmManager.cancel(WifiNative.this.mCheckWifiHalIsUsedListener);
                    WifiNative.this.mAlarmManager.set(2, SystemClock.elapsedRealtime() + 60000, WifiNative.TAG, WifiNative.this.mCheckWifiHalIsUsedListener, null);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onNativeDaemonDeath() {
        synchronized (this.mLock) {
            Iterator<StatusListener> it = this.mStatusListeners.iterator();
            while (it.hasNext()) {
                it.next().onStatusChanged(false);
            }
            Iterator<StatusListener> it2 = this.mStatusListeners.iterator();
            while (it2.hasNext()) {
                it2.next().onStatusChanged(true);
            }
        }
    }

    private class VendorHalDeathHandlerInternal implements VendorHalDeathEventHandler {
        private VendorHalDeathHandlerInternal() {
        }

        @Override // com.android.server.wifi.WifiNative.VendorHalDeathEventHandler
        public void onDeath() {
            synchronized (WifiNative.this.mLock) {
                Log.i(WifiNative.TAG, "Vendor HAL died");
                if (WifiNative.this.isWifiHalUsed()) {
                    Log.i(WifiNative.TAG, "Cleaning up hal internal state.");
                    WifiNative.this.onNativeDaemonDeath();
                    WifiNative.this.mWifiMetrics.incrementNumHalCrashes();
                }
            }
        }
    }

    private class WificondDeathHandlerInternal implements WificondDeathEventHandler {
        private WificondDeathHandlerInternal() {
        }

        @Override // com.android.server.wifi.WifiNative.WificondDeathEventHandler
        public void onDeath() {
            synchronized (WifiNative.this.mLock) {
                Log.i(WifiNative.TAG, "wificond died");
                if (WifiNative.this.isWifiHalUsed()) {
                    Log.i(WifiNative.TAG, "Cleaning up wificond internal state.");
                    WifiNative.this.onNativeDaemonDeath();
                    WifiNative.this.mWifiMetrics.incrementNumWificondCrashes();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class SupplicantDeathHandlerInternal implements SupplicantDeathEventHandler {
        private SupplicantDeathHandlerInternal() {
        }

        @Override // com.android.server.wifi.WifiNative.SupplicantDeathEventHandler
        public void onDeath() {
            synchronized (WifiNative.this.mLock) {
                Log.i(WifiNative.TAG, "wpa_supplicant died. Cleaning up internal state.");
                WifiNative.this.onNativeDaemonDeath();
                WifiNative.this.mWifiMetrics.incrementNumSupplicantCrashes();
            }
        }
    }

    /* access modifiers changed from: private */
    public class HostapdDeathHandlerInternal implements HostapdDeathEventHandler {
        private HostapdDeathHandlerInternal() {
        }

        @Override // com.android.server.wifi.WifiNative.HostapdDeathEventHandler
        public void onDeath() {
            synchronized (WifiNative.this.mLock) {
                Log.i(WifiNative.TAG, "hostapd died.");
                if (WifiNative.this.isWifiHalUsed()) {
                    Log.i(WifiNative.TAG, "Cleaning up hostapd internal state.");
                    WifiNative.this.onNativeDaemonDeath();
                    WifiNative.this.mWifiMetrics.incrementNumHostapdCrashes();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onInterfaceStateChanged(Iface iface, boolean isUp) {
        synchronized (this.mLock) {
            if (isUp == iface.isUp) {
                if (this.mVerboseLoggingEnabled) {
                    Log.v(TAG, "Interface status unchanged on " + iface + " from " + isUp + ", Ignoring...");
                }
                return;
            }
            Log.i(TAG, "Interface state changed on " + iface + ", isUp=" + isUp);
            if (isUp) {
                iface.externalListener.onUp(iface.name);
                this.mHwWifiNativeEx.setChipsetWifiCategory();
                this.mHwWifiNativeEx.setChipsetWifiFeatrureCapability();
            } else {
                iface.externalListener.onDown(iface.name);
                if (iface.type != 1) {
                    if (iface.type != 2) {
                        if (iface.type == 0) {
                            this.mWifiMetrics.incrementNumSoftApInterfaceDown();
                        }
                    }
                }
                this.mWifiMetrics.incrementNumClientInterfaceDown();
            }
            iface.isUp = isUp;
        }
    }

    /* access modifiers changed from: private */
    public class NetworkObserverInternal extends BaseNetworkObserver {
        private final int mInterfaceId;

        NetworkObserverInternal(int id) {
            this.mInterfaceId = id;
        }

        public void interfaceLinkStateChanged(String ifaceName, boolean unusedIsLinkUp) {
            WifiNative.this.mHandler.post(new Runnable(ifaceName) {
                /* class com.android.server.wifi.$$Lambda$WifiNative$NetworkObserverInternal$x2MXZjJ72D1RHdyNl7XjvdO7CHI */
                private final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    WifiNative.NetworkObserverInternal.this.lambda$interfaceLinkStateChanged$0$WifiNative$NetworkObserverInternal(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$interfaceLinkStateChanged$0$WifiNative$NetworkObserverInternal(String ifaceName) {
            synchronized (WifiNative.this.mLock) {
                Iface ifaceWithId = WifiNative.this.mIfaceMgr.getIface(this.mInterfaceId);
                if (ifaceWithId == null) {
                    if (WifiNative.this.mVerboseLoggingEnabled) {
                        Log.v(WifiNative.TAG, "Received iface link up/down notification on an invalid iface=" + this.mInterfaceId);
                    }
                    return;
                }
                Iface ifaceWithName = WifiNative.this.mIfaceMgr.getIface(ifaceName);
                if (ifaceWithName != null) {
                    if (ifaceWithName == ifaceWithId) {
                        WifiNative.this.onInterfaceStateChanged(ifaceWithName, WifiNative.this.isInterfaceUp(ifaceName));
                        return;
                    }
                }
                if (WifiNative.this.mVerboseLoggingEnabled) {
                    Log.v(WifiNative.TAG, "Received iface link up/down notification on an invalid iface=" + ifaceName);
                }
            }
        }
    }

    private class VendorHalRadioModeChangeHandlerInternal implements VendorHalRadioModeChangeEventHandler {
        private VendorHalRadioModeChangeHandlerInternal() {
        }

        @Override // com.android.server.wifi.WifiNative.VendorHalRadioModeChangeEventHandler
        public void onMcc(int band) {
            synchronized (WifiNative.this.mLock) {
                Log.i(WifiNative.TAG, "Device is in MCC mode now");
                WifiNative.this.mWifiMetrics.incrementNumRadioModeChangeToMcc();
            }
        }

        @Override // com.android.server.wifi.WifiNative.VendorHalRadioModeChangeEventHandler
        public void onScc(int band) {
            synchronized (WifiNative.this.mLock) {
                Log.i(WifiNative.TAG, "Device is in SCC mode now");
                WifiNative.this.mWifiMetrics.incrementNumRadioModeChangeToScc();
            }
        }

        @Override // com.android.server.wifi.WifiNative.VendorHalRadioModeChangeEventHandler
        public void onSbs(int band) {
            synchronized (WifiNative.this.mLock) {
                Log.i(WifiNative.TAG, "Device is in SBS mode now");
                WifiNative.this.mWifiMetrics.incrementNumRadioModeChangeToSbs();
            }
        }

        @Override // com.android.server.wifi.WifiNative.VendorHalRadioModeChangeEventHandler
        public void onDbs() {
            synchronized (WifiNative.this.mLock) {
                Log.i(WifiNative.TAG, "Device is in DBS mode now");
                WifiNative.this.mWifiMetrics.incrementNumRadioModeChangeToDbs();
            }
        }
    }

    private String handleIfaceCreationWhenVendorHalNotSupported(Iface newIface) {
        String string;
        synchronized (this.mLock) {
            Iface existingIface = this.mIfaceMgr.removeExistingIface(newIface.id);
            if (existingIface != null) {
                onInterfaceDestroyed(existingIface);
                Log.i(TAG, "Successfully torn down " + existingIface);
            }
            string = this.mPropertyService.getString("wifi.interface", "wlan0");
        }
        return string;
    }

    private String createStaIface(Iface iface, boolean lowPrioritySta) {
        synchronized (this.mLock) {
            if (this.mWifiVendorHal.isVendorHalSupported()) {
                return this.mWifiVendorHal.createStaIface(lowPrioritySta, new InterfaceDestoyedListenerInternal(iface.id));
            }
            Log.i(TAG, "Vendor Hal not supported, ignoring createStaIface.");
            return handleIfaceCreationWhenVendorHalNotSupported(iface);
        }
    }

    private String createApIface(Iface iface) {
        synchronized (this.mLock) {
            if (this.mWifiVendorHal.isVendorHalSupported()) {
                return this.mWifiVendorHal.createApIface(new InterfaceDestoyedListenerInternal(iface.id));
            }
            Log.i(TAG, "Vendor Hal not supported, ignoring createApIface.");
            return handleIfaceCreationWhenVendorHalNotSupported(iface);
        }
    }

    private boolean handleIfaceRemovalWhenVendorHalNotSupported(Iface iface) {
        synchronized (this.mLock) {
            this.mIfaceMgr.removeIface(iface.id);
            onInterfaceDestroyed(iface);
            Log.i(TAG, "Successfully torn down " + iface);
        }
        return true;
    }

    private boolean removeStaIface(Iface iface) {
        synchronized (this.mLock) {
            if (this.mWifiVendorHal.isVendorHalSupported()) {
                return this.mWifiVendorHal.removeStaIface(iface.name);
            }
            Log.i(TAG, "Vendor Hal not supported, ignoring removeStaIface.");
            return handleIfaceRemovalWhenVendorHalNotSupported(iface);
        }
    }

    private boolean removeApIface(Iface iface) {
        synchronized (this.mLock) {
            if (this.mWifiVendorHal.isVendorHalSupported()) {
                return this.mWifiVendorHal.removeApIface(iface.name);
            }
            Log.i(TAG, "Vendor Hal not supported, ignoring removeApIface.");
            return handleIfaceRemovalWhenVendorHalNotSupported(iface);
        }
    }

    public boolean initialize() {
        synchronized (this.mLock) {
            if (!this.mHwWifiNativeEx.isSpHalNull()) {
                Log.i(TAG, "initialize, so is already loaded and start hidl service");
                if (!this.mWifiVendorHal.initialize(new VendorHalDeathHandlerInternal())) {
                    Log.e(TAG, "Failed to initialize vendor HAL");
                    return false;
                }
            } else {
                Log.i(TAG, "initialize, last config is not need start hidl service");
                this.mWifiVendorHal.reset();
                this.mWifiVendorHal.registerHandler(new VendorHalDeathHandlerInternal());
            }
            if (!this.mWificondControl.initialize(new WificondDeathHandlerInternal())) {
                Log.e(TAG, "Failed to initialize wificond");
                return false;
            }
            this.mWifiVendorHal.registerRadioModeChangeHandler(new VendorHalRadioModeChangeHandlerInternal());
            return true;
        }
    }

    public void registerStatusListener(StatusListener listener) {
        this.mStatusListeners.add(listener);
    }

    private void initializeNwParamsForClientInterface(String ifaceName) {
        try {
            this.mNwManagementService.clearInterfaceAddresses(ifaceName);
            this.mNwManagementService.setInterfaceIpv6PrivacyExtensions(ifaceName, true);
            this.mNwManagementService.disableIpv6(ifaceName);
        } catch (RemoteException | IllegalStateException e) {
            Log.e(TAG, "Unable to change interface settings", e);
        }
    }

    public String setupInterfaceForClientInConnectivityMode(InterfaceCallback interfaceCallback) {
        synchronized (this.mLock) {
            if (!startHal()) {
                Log.e(TAG, "Failed to start Hal");
                this.mWifiMetrics.incrementNumSetupClientInterfaceFailureDueToHal();
                return null;
            } else if (!startSupplicant()) {
                Log.e(TAG, "Failed to start supplicant");
                this.mWifiMetrics.incrementNumSetupClientInterfaceFailureDueToSupplicant();
                return null;
            } else {
                Iface iface = this.mIfaceMgr.allocateIface(1);
                if (iface == null) {
                    Log.e(TAG, "Failed to allocate new STA iface");
                    return null;
                }
                iface.externalListener = interfaceCallback;
                iface.name = createStaIface(iface, false);
                if (TextUtils.isEmpty(iface.name)) {
                    Log.e(TAG, "Failed to create STA iface in vendor HAL");
                    this.mIfaceMgr.removeIface(iface.id);
                    this.mWifiMetrics.incrementNumSetupClientInterfaceFailureDueToHal();
                    return null;
                } else if (this.mWificondControl.setupInterfaceForClientMode(iface.name) == null) {
                    Log.e(TAG, "Failed to setup iface in wificond on " + iface);
                    teardownInterface(iface.name);
                    this.mWifiMetrics.incrementNumSetupClientInterfaceFailureDueToWificond();
                    return null;
                } else if (!this.mSupplicantStaIfaceHal.setupIface(iface.name)) {
                    Log.e(TAG, "Failed to setup iface in supplicant on " + iface);
                    teardownInterface(iface.name);
                    this.mWifiMetrics.incrementNumSetupClientInterfaceFailureDueToSupplicant();
                    return null;
                } else {
                    iface.networkObserver = new NetworkObserverInternal(iface.id);
                    if (!registerNetworkObserver(iface.networkObserver)) {
                        Log.e(TAG, "Failed to register network observer on " + iface);
                        teardownInterface(iface.name);
                        return null;
                    }
                    this.mWifiMonitor.startMonitoring(iface.name);
                    onInterfaceStateChanged(iface, isInterfaceUp(iface.name));
                    initializeNwParamsForClientInterface(iface.name);
                    Log.i(TAG, "Successfully setup " + iface);
                    iface.featureSet = getSupportedFeatureSetInternal(iface.name);
                    return iface.name;
                }
            }
        }
    }

    public String setupInterfaceForClientInScanMode(InterfaceCallback interfaceCallback) {
        synchronized (this.mLock) {
            if (!startHal()) {
                Log.e(TAG, "Failed to start Hal");
                this.mWifiMetrics.incrementNumSetupClientInterfaceFailureDueToHal();
                return null;
            }
            Iface iface = this.mIfaceMgr.allocateIface(2);
            if (iface == null) {
                Log.e(TAG, "Failed to allocate new STA iface");
                return null;
            }
            iface.externalListener = interfaceCallback;
            iface.name = createStaIface(iface, true);
            if (TextUtils.isEmpty(iface.name)) {
                Log.e(TAG, "Failed to create iface in vendor HAL");
                this.mIfaceMgr.removeIface(iface.id);
                this.mWifiMetrics.incrementNumSetupClientInterfaceFailureDueToHal();
                return null;
            } else if (this.mWificondControl.setupInterfaceForClientMode(iface.name) == null) {
                Log.e(TAG, "Failed to setup iface in wificond=" + iface.name);
                teardownInterface(iface.name);
                this.mWifiMetrics.incrementNumSetupClientInterfaceFailureDueToWificond();
                return null;
            } else {
                iface.networkObserver = new NetworkObserverInternal(iface.id);
                if (!registerNetworkObserver(iface.networkObserver)) {
                    Log.e(TAG, "Failed to register network observer for iface=" + iface.name);
                    teardownInterface(iface.name);
                    return null;
                }
                this.mWifiMonitor.startMonitoring(iface.name);
                onInterfaceStateChanged(iface, isInterfaceUp(iface.name));
                Log.i(TAG, "Successfully setup " + iface);
                iface.featureSet = getSupportedFeatureSetInternal(iface.name);
                return iface.name;
            }
        }
    }

    public String setupInterfaceForSoftApMode(InterfaceCallback interfaceCallback) {
        synchronized (this.mLock) {
            if (!startHal()) {
                Log.e(TAG, "Failed to start Hal");
                this.mWifiMetrics.incrementNumSetupSoftApInterfaceFailureDueToHal();
                return null;
            } else if (!startHostapd()) {
                Log.e(TAG, "Failed to start hostapd");
                this.mWifiMetrics.incrementNumSetupSoftApInterfaceFailureDueToHostapd();
                return null;
            } else {
                Iface iface = this.mIfaceMgr.allocateIface(0);
                if (iface == null) {
                    Log.e(TAG, "Failed to allocate new AP iface");
                    return null;
                }
                iface.externalListener = interfaceCallback;
                iface.name = createApIface(iface);
                if (TextUtils.isEmpty(iface.name)) {
                    Log.e(TAG, "Failed to create AP iface in vendor HAL");
                    this.mIfaceMgr.removeIface(iface.id);
                    this.mWifiMetrics.incrementNumSetupSoftApInterfaceFailureDueToHal();
                    return null;
                } else if (this.mWificondControl.setupInterfaceForSoftApMode(iface.name) == null) {
                    Log.e(TAG, "Failed to setup iface in wificond on " + iface);
                    teardownInterface(iface.name);
                    this.mWifiMetrics.incrementNumSetupSoftApInterfaceFailureDueToWificond();
                    return null;
                } else {
                    iface.networkObserver = new NetworkObserverInternal(iface.id);
                    if (!registerNetworkObserver(iface.networkObserver)) {
                        Log.e(TAG, "Failed to register network observer on " + iface);
                        teardownInterface(iface.name);
                        return null;
                    }
                    onInterfaceStateChanged(iface, isInterfaceUp(iface.name));
                    Log.i(TAG, "Successfully setup " + iface);
                    iface.featureSet = getSupportedFeatureSetInternal(iface.name);
                    return iface.name;
                }
            }
        }
    }

    public boolean isInterfaceUp(String ifaceName) {
        synchronized (this.mLock) {
            if (this.mIfaceMgr.getIface(ifaceName) == null) {
                Log.e(TAG, "Trying to get iface state on invalid iface=" + ifaceName);
                return false;
            }
            InterfaceConfiguration config = null;
            try {
                config = this.mNwManagementService.getInterfaceConfig(ifaceName);
            } catch (RemoteException | IllegalStateException e) {
                Log.e(TAG, "Unable to get interface config", e);
            }
            if (config == null) {
                return false;
            }
            return config.isUp();
        }
    }

    public void teardownInterface(String ifaceName) {
        synchronized (this.mLock) {
            Iface iface = this.mIfaceMgr.getIface(ifaceName);
            if (iface == null) {
                Log.e(TAG, "Trying to teardown an invalid iface=" + ifaceName);
                return;
            }
            if (iface.type != 1) {
                if (iface.type != 2) {
                    if (iface.type == 0 && !removeApIface(iface)) {
                        Log.e(TAG, "Failed to remove iface in vendor HAL=" + ifaceName);
                        return;
                    }
                    Log.i(TAG, "Successfully initiated teardown for iface=" + ifaceName);
                }
            }
            if (!removeStaIface(iface)) {
                Log.e(TAG, "Failed to remove iface in vendor HAL=" + ifaceName);
                return;
            }
            Log.i(TAG, "Successfully initiated teardown for iface=" + ifaceName);
        }
    }

    public void teardownAllInterfaces() {
        synchronized (this.mLock) {
            Iterator<Integer> ifaceIdIter = this.mIfaceMgr.getIfaceIdIter();
            while (ifaceIdIter.hasNext()) {
                Iface iface = this.mIfaceMgr.getIface(ifaceIdIter.next().intValue());
                ifaceIdIter.remove();
                onInterfaceDestroyed(iface);
                Log.i(TAG, "Successfully torn down " + iface);
            }
            Log.i(TAG, "Successfully torn down all ifaces");
        }
    }

    @Override // com.android.server.wifi.IHwWifiNativeInner
    public String getClientInterfaceName() {
        String findAnyStaIfaceName;
        synchronized (this.mLock) {
            findAnyStaIfaceName = this.mIfaceMgr.findAnyStaIfaceName();
        }
        return findAnyStaIfaceName;
    }

    @Override // com.android.server.wifi.IHwWifiNativeInner
    public String getSoftApInterfaceName() {
        String findAnyApIfaceName;
        synchronized (this.mLock) {
            findAnyApIfaceName = this.mIfaceMgr.findAnyApIfaceName();
        }
        return findAnyApIfaceName;
    }

    public SignalPollResult signalPoll(String ifaceName) {
        return this.mWificondControl.signalPoll(ifaceName);
    }

    public TxPacketCounters getTxPacketCounters(String ifaceName) {
        return this.mWificondControl.getTxPacketCounters(ifaceName);
    }

    @Override // com.android.server.wifi.IHwWifiNativeInner
    public int[] getChannelsForBand(int band) {
        return this.mWificondControl.getChannelsForBand(band);
    }

    public boolean scan(String ifaceName, int scanType, Set<Integer> freqs, List<String> hiddenNetworkSSIDs) {
        return this.mWificondControl.scan(ifaceName, scanType, freqs, hiddenNetworkSSIDs);
    }

    public ArrayList<ScanDetail> getScanResults(String ifaceName) {
        return this.mWificondControl.getScanResults(ifaceName, 0);
    }

    public ArrayList<ScanDetail> getPnoScanResults(String ifaceName) {
        return this.mWificondControl.getScanResults(ifaceName, 1);
    }

    public boolean startPnoScan(String ifaceName, PnoSettings pnoSettings) {
        return this.mWificondControl.startPnoScan(ifaceName, pnoSettings);
    }

    public boolean stopPnoScan(String ifaceName) {
        return this.mWificondControl.stopPnoScan(ifaceName);
    }

    public void sendMgmtFrame(String ifaceName, byte[] frame, SendMgmtFrameCallback callback, int mcs) {
        this.mWificondControl.sendMgmtFrame(ifaceName, frame, callback, mcs);
    }

    public void probeLink(String ifaceName, MacAddress receiverMac, SendMgmtFrameCallback callback, int mcs) {
        if (callback == null) {
            Log.e(TAG, "callback cannot be null!");
        } else if (receiverMac == null) {
            Log.e(TAG, "Receiver MAC address cannot be null!");
            callback.onFailure(1);
        } else {
            String senderMacStr = getMacAddress(ifaceName);
            if (senderMacStr == null) {
                Log.e(TAG, "Failed to get this device's MAC Address");
                callback.onFailure(1);
                return;
            }
            sendMgmtFrame(ifaceName, buildProbeRequestFrame(receiverMac.toByteArray(), NativeUtil.macAddressToByteArray(senderMacStr)), callback, mcs);
        }
    }

    private byte[] buildProbeRequestFrame(byte[] receiverMac, byte[] transmitterMac) {
        ByteBuffer frame = ByteBuffer.allocate(26);
        frame.order(ByteOrder.LITTLE_ENDIAN);
        frame.put((byte) 64);
        frame.put((byte) 0);
        frame.putShort(60);
        frame.put(receiverMac);
        frame.put(transmitterMac);
        frame.put(receiverMac);
        frame.putShort((short) (this.mRandom.nextInt() & 65520));
        frame.put((byte) 0);
        frame.put((byte) 0);
        return frame.array();
    }

    private boolean startAndWaitForHostapdConnection() {
        if (!this.mHostapdHal.isInitializationStarted() && !this.mHostapdHal.initialize()) {
            return false;
        }
        if (!this.mHostapdHal.startDaemon()) {
            Log.e(TAG, "Failed to startup hostapd");
            return false;
        }
        boolean connected = false;
        int connectTries = 0;
        while (true) {
            if (connected) {
                break;
            }
            int connectTries2 = connectTries + 1;
            if (connectTries >= 50 || (connected = this.mHostapdHal.isInitializationComplete())) {
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            connectTries = connectTries2;
        }
        return connected;
    }

    public boolean startSoftAp(String ifaceName, WifiConfiguration config, SoftApListener listener) {
        if (!this.mWificondControl.registerApListener(ifaceName, listener)) {
            Log.e(TAG, "Failed to register ap listener");
            return false;
        } else if (this.mHostapdHal.addAccessPoint(ifaceName, config, listener)) {
            return true;
        } else {
            Log.e(TAG, "Failed to add acccess point");
            this.mWifiMetrics.incrementNumSetupSoftApInterfaceFailureDueToHostapd();
            return false;
        }
    }

    public boolean setMacAddress(String interfaceName, MacAddress mac) {
        return this.mWifiVendorHal.setMacAddress(interfaceName, mac);
    }

    public MacAddress getFactoryMacAddress(String interfaceName) {
        return this.mWifiVendorHal.getFactoryMacAddress(interfaceName);
    }

    public void setSupplicantLogLevel(boolean turnOnVerbose) {
        this.mSupplicantStaIfaceHal.setLogLevel(turnOnVerbose);
    }

    public boolean reconnect(String ifaceName) {
        boolean result = this.mSupplicantStaIfaceHal.reconnect(ifaceName);
        if (result) {
            WifiCommonUtils.notifyDeviceState(WifiCommonUtils.DEVICE_WLAN, WifiCommonUtils.STATE_CONNECT_START, "");
        }
        return result;
    }

    public boolean reassociate(String ifaceName) {
        boolean result = this.mSupplicantStaIfaceHal.reassociate(ifaceName);
        if (result) {
            WifiCommonUtils.notifyDeviceState(WifiCommonUtils.DEVICE_WLAN, WifiCommonUtils.STATE_CONNECT_START, "");
        }
        return result;
    }

    public boolean disconnect(String ifaceName) {
        return this.mSupplicantStaIfaceHal.disconnect(ifaceName);
    }

    public String getMacAddress(String ifaceName) {
        return this.mSupplicantStaIfaceHal.getMacAddress(ifaceName);
    }

    public boolean startFilteringMulticastV4Packets(String ifaceName) {
        if (!this.mSupplicantStaIfaceHal.stopRxFilter(ifaceName) || !this.mSupplicantStaIfaceHal.removeRxFilter(ifaceName, 0) || !this.mSupplicantStaIfaceHal.startRxFilter(ifaceName)) {
            return false;
        }
        return true;
    }

    public boolean stopFilteringMulticastV4Packets(String ifaceName) {
        if (!this.mSupplicantStaIfaceHal.stopRxFilter(ifaceName) || !this.mSupplicantStaIfaceHal.addRxFilter(ifaceName, 0) || !this.mSupplicantStaIfaceHal.startRxFilter(ifaceName)) {
            return false;
        }
        return true;
    }

    public boolean startFilteringMulticastV6Packets(String ifaceName) {
        if (!this.mSupplicantStaIfaceHal.stopRxFilter(ifaceName) || !this.mSupplicantStaIfaceHal.removeRxFilter(ifaceName, 1) || !this.mSupplicantStaIfaceHal.startRxFilter(ifaceName)) {
            return false;
        }
        return true;
    }

    public boolean stopFilteringMulticastV6Packets(String ifaceName) {
        if (!this.mSupplicantStaIfaceHal.stopRxFilter(ifaceName) || !this.mSupplicantStaIfaceHal.addRxFilter(ifaceName, 1) || !this.mSupplicantStaIfaceHal.startRxFilter(ifaceName)) {
            return false;
        }
        return true;
    }

    public boolean setBluetoothCoexistenceMode(String ifaceName, int mode) {
        return this.mSupplicantStaIfaceHal.setBtCoexistenceMode(ifaceName, mode);
    }

    public boolean setBluetoothCoexistenceScanMode(String ifaceName, boolean setCoexScanMode) {
        return this.mSupplicantStaIfaceHal.setBtCoexistenceScanModeEnabled(ifaceName, setCoexScanMode);
    }

    public boolean setSuspendOptimizations(String ifaceName, boolean enabled) {
        Log.w(TAG, "apf dbg, setSuspendOptimizations: enabled=" + enabled);
        return this.mSupplicantStaIfaceHal.setSuspendModeEnabled(ifaceName, enabled);
    }

    public boolean setCountryCode(String ifaceName, String countryCode) {
        return this.mSupplicantStaIfaceHal.setCountryCode(ifaceName, countryCode);
    }

    public void startTdls(String ifaceName, String macAddr, boolean enable) {
        if (enable) {
            this.mSupplicantStaIfaceHal.initiateTdlsDiscover(ifaceName, macAddr);
            this.mSupplicantStaIfaceHal.initiateTdlsSetup(ifaceName, macAddr);
            return;
        }
        this.mSupplicantStaIfaceHal.initiateTdlsTeardown(ifaceName, macAddr);
    }

    public boolean startWpsPbc(String ifaceName, String bssid) {
        return this.mSupplicantStaIfaceHal.startWpsPbc(ifaceName, bssid);
    }

    public boolean startWpsPinKeypad(String ifaceName, String pin) {
        return this.mSupplicantStaIfaceHal.startWpsPinKeypad(ifaceName, pin);
    }

    public String startWpsPinDisplay(String ifaceName, String bssid) {
        return this.mSupplicantStaIfaceHal.startWpsPinDisplay(ifaceName, bssid);
    }

    public boolean setExternalSim(String ifaceName, boolean external) {
        return this.mSupplicantStaIfaceHal.setExternalSim(ifaceName, external);
    }

    public boolean simAuthResponse(String ifaceName, int id, String type, String response) {
        if (SIM_AUTH_RESP_TYPE_GSM_AUTH.equals(type)) {
            return this.mSupplicantStaIfaceHal.sendCurrentNetworkEapSimGsmAuthResponse(ifaceName, response);
        }
        if (SIM_AUTH_RESP_TYPE_UMTS_AUTH.equals(type)) {
            return this.mSupplicantStaIfaceHal.sendCurrentNetworkEapSimUmtsAuthResponse(ifaceName, response);
        }
        if (SIM_AUTH_RESP_TYPE_UMTS_AUTS.equals(type)) {
            return this.mSupplicantStaIfaceHal.sendCurrentNetworkEapSimUmtsAutsResponse(ifaceName, response);
        }
        return false;
    }

    public boolean simAuthFailedResponse(String ifaceName, int id) {
        return this.mSupplicantStaIfaceHal.sendCurrentNetworkEapSimGsmAuthFailure(ifaceName);
    }

    public boolean umtsAuthFailedResponse(String ifaceName, int id) {
        return this.mSupplicantStaIfaceHal.sendCurrentNetworkEapSimUmtsAuthFailure(ifaceName);
    }

    public boolean simIdentityResponse(String ifaceName, int id, String unencryptedResponse, String encryptedResponse) {
        return this.mSupplicantStaIfaceHal.sendCurrentNetworkEapIdentityResponse(ifaceName, unencryptedResponse, encryptedResponse);
    }

    public String getEapAnonymousIdentity(String ifaceName) {
        return this.mSupplicantStaIfaceHal.getCurrentNetworkEapAnonymousIdentity(ifaceName);
    }

    public boolean startWpsRegistrar(String ifaceName, String bssid, String pin) {
        return this.mSupplicantStaIfaceHal.startWpsRegistrar(ifaceName, bssid, pin);
    }

    public boolean cancelWps(String ifaceName) {
        return this.mSupplicantStaIfaceHal.cancelWps(ifaceName);
    }

    public boolean setDeviceName(String ifaceName, String name) {
        return this.mSupplicantStaIfaceHal.setWpsDeviceName(ifaceName, name);
    }

    public boolean setDeviceType(String ifaceName, String type) {
        return this.mSupplicantStaIfaceHal.setWpsDeviceType(ifaceName, type);
    }

    public boolean setConfigMethods(String ifaceName, String cfg) {
        return this.mSupplicantStaIfaceHal.setWpsConfigMethods(ifaceName, cfg);
    }

    public boolean setManufacturer(String ifaceName, String value) {
        return this.mSupplicantStaIfaceHal.setWpsManufacturer(ifaceName, value);
    }

    public boolean setModelName(String ifaceName, String value) {
        return this.mSupplicantStaIfaceHal.setWpsModelName(ifaceName, value);
    }

    public boolean setModelNumber(String ifaceName, String value) {
        return this.mSupplicantStaIfaceHal.setWpsModelNumber(ifaceName, value);
    }

    public boolean setSerialNumber(String ifaceName, String value) {
        return this.mSupplicantStaIfaceHal.setWpsSerialNumber(ifaceName, value);
    }

    public void setPowerSave(String ifaceName, boolean enabled) {
        this.mSupplicantStaIfaceHal.setPowerSave(ifaceName, enabled);
    }

    public boolean setLowLatencyMode(boolean enabled) {
        return this.mWifiVendorHal.setLowLatencyMode(enabled);
    }

    public boolean setConcurrencyPriority(boolean isStaHigherPriority) {
        return this.mSupplicantStaIfaceHal.setConcurrencyPriority(isStaHigherPriority);
    }

    public boolean enableStaAutoReconnect(String ifaceName, boolean enable) {
        return this.mSupplicantStaIfaceHal.enableAutoReconnect(ifaceName, enable);
    }

    public boolean migrateNetworksFromSupplicant(String ifaceName, Map<String, WifiConfiguration> configs, SparseArray<Map<String, String>> networkExtras) {
        return this.mSupplicantStaIfaceHal.loadNetworks(ifaceName, configs, networkExtras);
    }

    public boolean connectToNetwork(String ifaceName, WifiConfiguration configuration) {
        this.mWificondControl.abortScan(ifaceName);
        boolean result = this.mSupplicantStaIfaceHal.connectToNetwork(ifaceName, configuration);
        if (result) {
            WifiCommonUtils.notifyDeviceState(WifiCommonUtils.DEVICE_WLAN, WifiCommonUtils.STATE_CONNECT_START, "");
        }
        return result;
    }

    public boolean roamToNetwork(String ifaceName, WifiConfiguration configuration) {
        this.mWificondControl.abortScan(ifaceName);
        boolean result = this.mSupplicantStaIfaceHal.roamToNetwork(ifaceName, configuration);
        if (result) {
            WifiCommonUtils.notifyDeviceState(WifiCommonUtils.DEVICE_WLAN, WifiCommonUtils.STATE_CONNECT_START, "");
        }
        return result;
    }

    public boolean removeAllNetworks(String ifaceName) {
        return this.mSupplicantStaIfaceHal.removeAllNetworks(ifaceName);
    }

    public boolean setConfiguredNetworkBSSID(String ifaceName, String bssid) {
        return this.mSupplicantStaIfaceHal.setCurrentNetworkBssid(ifaceName, bssid);
    }

    public boolean requestAnqp(String ifaceName, String bssid, Set<Integer> anqpIds, Set<Integer> hs20Subtypes) {
        if (bssid == null || ((anqpIds == null || anqpIds.isEmpty()) && (hs20Subtypes == null || hs20Subtypes.isEmpty()))) {
            Log.e(TAG, "Invalid arguments for ANQP request.");
            return false;
        }
        ArrayList<Short> anqpIdList = new ArrayList<>();
        if (anqpIds != null) {
            for (Integer anqpId : anqpIds) {
                anqpIdList.add(Short.valueOf(anqpId.shortValue()));
            }
        }
        ArrayList<Integer> hs20SubtypeList = new ArrayList<>();
        hs20SubtypeList.addAll(hs20Subtypes);
        return this.mSupplicantStaIfaceHal.initiateAnqpQuery(ifaceName, bssid, anqpIdList, hs20SubtypeList);
    }

    public boolean requestIcon(String ifaceName, String bssid, String fileName) {
        if (bssid != null && fileName != null) {
            return this.mSupplicantStaIfaceHal.initiateHs20IconQuery(ifaceName, bssid, fileName);
        }
        Log.e(TAG, "Invalid arguments for Icon request.");
        return false;
    }

    public String getCurrentNetworkWpsNfcConfigurationToken(String ifaceName) {
        return this.mSupplicantStaIfaceHal.getCurrentNetworkWpsNfcConfigurationToken(ifaceName);
    }

    public void removeNetworkIfCurrent(String ifaceName, int networkId) {
        this.mSupplicantStaIfaceHal.removeNetworkIfCurrent(ifaceName, networkId);
    }

    public int addDppPeerUri(String ifaceName, String uri) {
        return this.mSupplicantStaIfaceHal.addDppPeerUri(ifaceName, uri);
    }

    public boolean removeDppUri(String ifaceName, int bootstrapId) {
        return this.mSupplicantStaIfaceHal.removeDppUri(ifaceName, bootstrapId);
    }

    public boolean stopDppInitiator(String ifaceName) {
        return this.mSupplicantStaIfaceHal.stopDppInitiator(ifaceName);
    }

    public boolean startDppConfiguratorInitiator(String ifaceName, int peerBootstrapId, int ownBootstrapId, String ssid, String password, String psk, int netRole, int securityAkm) {
        return this.mSupplicantStaIfaceHal.startDppConfiguratorInitiator(ifaceName, peerBootstrapId, ownBootstrapId, ssid, password, psk, netRole, securityAkm);
    }

    public boolean startDppEnrolleeInitiator(String ifaceName, int peerBootstrapId, int ownBootstrapId) {
        return this.mSupplicantStaIfaceHal.startDppEnrolleeInitiator(ifaceName, peerBootstrapId, ownBootstrapId);
    }

    public void registerDppEventCallback(DppEventCallback dppEventCallback) {
        this.mSupplicantStaIfaceHal.registerDppCallback(dppEventCallback);
    }

    public boolean isHalStarted() {
        return this.mWifiVendorHal.isHalStarted();
    }

    public boolean getBgScanCapabilities(String ifaceName, ScanCapabilities capabilities) {
        return this.mWifiVendorHal.getBgScanCapabilities(ifaceName, capabilities);
    }

    public static class HiddenNetwork {
        public String ssid;

        public boolean equals(Object otherObj) {
            if (this == otherObj) {
                return true;
            }
            if (otherObj == null || getClass() != otherObj.getClass()) {
                return false;
            }
            return Objects.equals(this.ssid, ((HiddenNetwork) otherObj).ssid);
        }

        public int hashCode() {
            return Objects.hash(this.ssid);
        }
    }

    public static class ScanSettings {
        public int base_period_ms;
        public BucketSettings[] buckets;
        public String handlerId;
        public HiddenNetwork[] hiddenNetworks;
        public boolean isHiddenSingleScan;
        public int max_ap_per_scan;
        public int num_buckets;
        public int report_threshold_num_scans;
        public int report_threshold_percent;
        public int scanType;

        public String toString() {
            return this.handlerId;
        }
    }

    public static class PnoNetwork {
        public byte auth_bit_field;
        public byte flags;
        public int[] frequencies;
        public String ssid;

        public boolean equals(Object otherObj) {
            if (this == otherObj) {
                return true;
            }
            if (otherObj == null || getClass() != otherObj.getClass()) {
                return false;
            }
            PnoNetwork other = (PnoNetwork) otherObj;
            if (!Objects.equals(this.ssid, other.ssid) || this.flags != other.flags || this.auth_bit_field != other.auth_bit_field || !Arrays.equals(this.frequencies, other.frequencies)) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return Objects.hash(this.ssid, Byte.valueOf(this.flags), Byte.valueOf(this.auth_bit_field), this.frequencies);
        }
    }

    public boolean startBgScan(String ifaceName, ScanSettings settings, ScanEventHandler eventHandler) {
        return this.mWifiVendorHal.startBgScan(ifaceName, settings, eventHandler);
    }

    public void stopBgScan(String ifaceName) {
        this.mWifiVendorHal.stopBgScan(ifaceName);
    }

    public void pauseBgScan(String ifaceName) {
        this.mWifiVendorHal.pauseBgScan(ifaceName);
    }

    public void restartBgScan(String ifaceName) {
        this.mWifiVendorHal.restartBgScan(ifaceName);
    }

    public WifiScanner.ScanData[] getBgScanResults(String ifaceName) {
        return this.mWifiVendorHal.getBgScanResults(ifaceName);
    }

    public WifiLinkLayerStats getWifiLinkLayerStats(String ifaceName) {
        return this.mWifiVendorHal.getWifiLinkLayerStats(ifaceName);
    }

    public long getSupportedFeatureSet(String ifaceName) {
        synchronized (this.mLock) {
            Iface iface = this.mIfaceMgr.getIface(ifaceName);
            if (iface == null) {
                Log.e(TAG, "Could not get Iface object for interface " + ifaceName);
                return 0;
            }
            return iface.featureSet;
        }
    }

    private long getSupportedFeatureSetInternal(String ifaceName) {
        return ((long) this.mSupplicantStaIfaceHal.getAdvancedKeyMgmtCapabilities(ifaceName)) | this.mWifiVendorHal.getSupportedFeatureSet(ifaceName);
    }

    public boolean setScanningMacOui(String ifaceName, byte[] oui) {
        return this.mWifiVendorHal.setScanningMacOui(ifaceName, oui);
    }

    public ApfCapabilities getApfCapabilities(String ifaceName) {
        return this.mWifiVendorHal.getApfCapabilities(ifaceName);
    }

    public boolean installPacketFilter(String ifaceName, byte[] filter) {
        return this.mWifiVendorHal.installPacketFilter(ifaceName, filter);
    }

    public byte[] readPacketFilter(String ifaceName) {
        return this.mWifiVendorHal.readPacketFilter(ifaceName);
    }

    public boolean setCountryCodeHal(String ifaceName, String countryCode) {
        return this.mWifiVendorHal.setCountryCodeHal(ifaceName, countryCode);
    }

    public boolean setLoggingEventHandler(WifiLoggerEventHandler handler) {
        return this.mWifiVendorHal.setLoggingEventHandler(handler);
    }

    public boolean startLoggingRingBuffer(int verboseLevel, int flags, int maxInterval, int minDataSize, String ringName) {
        return this.mWifiVendorHal.startLoggingRingBuffer(verboseLevel, flags, maxInterval, minDataSize, ringName);
    }

    public int getSupportedLoggerFeatureSet() {
        return this.mWifiVendorHal.getSupportedLoggerFeatureSet();
    }

    public boolean resetLogHandler() {
        return this.mWifiVendorHal.resetLogHandler();
    }

    public String getDriverVersion() {
        return this.mWifiVendorHal.getDriverVersion();
    }

    public String getFirmwareVersion() {
        return this.mWifiVendorHal.getFirmwareVersion();
    }

    public static class RingBufferStatus {
        public static final int HAS_ASCII_ENTRIES = 2;
        public static final int HAS_BINARY_ENTRIES = 1;
        public static final int HAS_PER_PACKET_ENTRIES = 4;
        int flag;
        String name;
        int readBytes;
        int ringBufferByteSize;
        int ringBufferId;
        int verboseLevel;
        int writtenBytes;
        int writtenRecords;

        public String toString() {
            return "name: " + this.name + " flag: " + this.flag + " ringBufferId: " + this.ringBufferId + " ringBufferByteSize: " + this.ringBufferByteSize + " verboseLevel: " + this.verboseLevel + " writtenBytes: " + this.writtenBytes + " readBytes: " + this.readBytes + " writtenRecords: " + this.writtenRecords;
        }
    }

    public RingBufferStatus[] getRingBufferStatus() {
        return this.mWifiVendorHal.getRingBufferStatus();
    }

    public boolean getRingBufferData(String ringName) {
        return this.mWifiVendorHal.getRingBufferData(ringName);
    }

    public boolean flushRingBufferData() {
        return this.mWifiVendorHal.flushRingBufferData();
    }

    public byte[] getFwMemoryDump() {
        return this.mWifiVendorHal.getFwMemoryDump();
    }

    public byte[] getDriverStateDump() {
        return this.mWifiVendorHal.getDriverStateDump();
    }

    @Immutable
    static abstract class FateReport {
        static final int MAX_DRIVER_TIMESTAMP_MSEC = 4294967;
        static final int USEC_PER_MSEC = 1000;
        static final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS");
        final long mDriverTimestampUSec;
        final long mEstimatedWallclockMSec = convertDriverTimestampUSecToWallclockMSec(this.mDriverTimestampUSec);
        final byte mFate;
        final byte[] mFrameBytes;
        final byte mFrameType;

        /* access modifiers changed from: protected */
        public abstract String directionToString();

        /* access modifiers changed from: protected */
        public abstract String fateToString();

        FateReport(byte fate, long driverTimestampUSec, byte frameType, byte[] frameBytes) {
            this.mFate = fate;
            this.mDriverTimestampUSec = driverTimestampUSec;
            this.mFrameType = frameType;
            this.mFrameBytes = frameBytes;
        }

        public String toTableRowString() {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            FrameParser parser = new FrameParser(this.mFrameType, this.mFrameBytes);
            dateFormatter.setTimeZone(TimeZone.getDefault());
            pw.format("%-15s  %12s  %-9s  %-32s  %-12s  %-23s  %s\n", Long.valueOf(this.mDriverTimestampUSec), dateFormatter.format(new Date(this.mEstimatedWallclockMSec)), directionToString(), fateToString(), parser.mMostSpecificProtocolString, parser.mTypeString, parser.mResultString);
            return sw.toString();
        }

        public String toVerboseStringWithPiiAllowed() {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            FrameParser parser = new FrameParser(this.mFrameType, this.mFrameBytes);
            pw.format("Frame direction: %s\n", directionToString());
            pw.format("Frame timestamp: %d\n", Long.valueOf(this.mDriverTimestampUSec));
            pw.format("Frame fate: %s\n", fateToString());
            pw.format("Frame type: %s\n", frameTypeToString(this.mFrameType));
            pw.format("Frame protocol: %s\n", parser.mMostSpecificProtocolString);
            pw.format("Frame protocol type: %s\n", parser.mTypeString);
            pw.format("Frame length: %d\n", Integer.valueOf(this.mFrameBytes.length));
            pw.append((CharSequence) "Frame bytes");
            pw.append((CharSequence) HexDump.dumpHexString(this.mFrameBytes));
            pw.append((CharSequence) "\n");
            return sw.toString();
        }

        public static String getTableHeader() {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.format("\n%-15s  %-12s  %-9s  %-32s  %-12s  %-23s  %s\n", "Time usec", "Walltime", "Direction", "Fate", "Protocol", "Type", "Result");
            pw.format("%-15s  %-12s  %-9s  %-32s  %-12s  %-23s  %s\n", "---------", "--------", "---------", "----", "--------", "----", "------");
            return sw.toString();
        }

        private static String frameTypeToString(byte frameType) {
            if (frameType == 0) {
                return "unknown";
            }
            if (frameType == 1) {
                return "data";
            }
            if (frameType != 2) {
                return Byte.toString(frameType);
            }
            return "802.11 management";
        }

        private static long convertDriverTimestampUSecToWallclockMSec(long driverTimestampUSec) {
            long wallclockMillisNow = System.currentTimeMillis();
            long driverTimestampMillis = driverTimestampUSec / 1000;
            long boottimeTimestampMillis = SystemClock.elapsedRealtime() % 4294967;
            if (boottimeTimestampMillis < driverTimestampMillis) {
                boottimeTimestampMillis += 4294967;
            }
            return wallclockMillisNow - (boottimeTimestampMillis - driverTimestampMillis);
        }
    }

    @Immutable
    public static final class TxFateReport extends FateReport {
        @Override // com.android.server.wifi.WifiNative.FateReport
        public /* bridge */ /* synthetic */ String toTableRowString() {
            return super.toTableRowString();
        }

        @Override // com.android.server.wifi.WifiNative.FateReport
        public /* bridge */ /* synthetic */ String toVerboseStringWithPiiAllowed() {
            return super.toVerboseStringWithPiiAllowed();
        }

        TxFateReport(byte fate, long driverTimestampUSec, byte frameType, byte[] frameBytes) {
            super(fate, driverTimestampUSec, frameType, frameBytes);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.wifi.WifiNative.FateReport
        public String directionToString() {
            return "TX";
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.wifi.WifiNative.FateReport
        public String fateToString() {
            switch (this.mFate) {
                case 0:
                    return "acked";
                case 1:
                    return "sent";
                case 2:
                    return "firmware queued";
                case 3:
                    return "firmware dropped (invalid frame)";
                case 4:
                    return "firmware dropped (no bufs)";
                case 5:
                    return "firmware dropped (other)";
                case 6:
                    return "driver queued";
                case 7:
                    return "driver dropped (invalid frame)";
                case 8:
                    return "driver dropped (no bufs)";
                case 9:
                    return "driver dropped (other)";
                default:
                    return Byte.toString(this.mFate);
            }
        }
    }

    @Immutable
    public static final class RxFateReport extends FateReport {
        @Override // com.android.server.wifi.WifiNative.FateReport
        public /* bridge */ /* synthetic */ String toTableRowString() {
            return super.toTableRowString();
        }

        @Override // com.android.server.wifi.WifiNative.FateReport
        public /* bridge */ /* synthetic */ String toVerboseStringWithPiiAllowed() {
            return super.toVerboseStringWithPiiAllowed();
        }

        RxFateReport(byte fate, long driverTimestampUSec, byte frameType, byte[] frameBytes) {
            super(fate, driverTimestampUSec, frameType, frameBytes);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.wifi.WifiNative.FateReport
        public String directionToString() {
            return "RX";
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.wifi.WifiNative.FateReport
        public String fateToString() {
            switch (this.mFate) {
                case 0:
                    return "success";
                case 1:
                    return "firmware queued";
                case 2:
                    return "firmware dropped (filter)";
                case 3:
                    return "firmware dropped (invalid frame)";
                case 4:
                    return "firmware dropped (no bufs)";
                case 5:
                    return "firmware dropped (other)";
                case 6:
                    return "driver queued";
                case 7:
                    return "driver dropped (filter)";
                case 8:
                    return "driver dropped (invalid frame)";
                case 9:
                    return "driver dropped (no bufs)";
                case 10:
                    return "driver dropped (other)";
                default:
                    return Byte.toString(this.mFate);
            }
        }
    }

    public boolean startPktFateMonitoring(String ifaceName) {
        return this.mWifiVendorHal.startPktFateMonitoring(ifaceName);
    }

    public boolean getTxPktFates(String ifaceName, TxFateReport[] reportBufs) {
        return this.mWifiVendorHal.getTxPktFates(ifaceName, reportBufs);
    }

    public boolean getRxPktFates(String ifaceName, RxFateReport[] reportBufs) {
        return this.mWifiVendorHal.getRxPktFates(ifaceName, reportBufs);
    }

    public long getTxPackets(String ifaceName) {
        return TrafficStats.getTxPackets(ifaceName);
    }

    public long getRxPackets(String ifaceName) {
        return TrafficStats.getRxPackets(ifaceName);
    }

    public int startSendingOffloadedPacket(String ifaceName, int slot, byte[] dstMac, byte[] packet, int protocol, int period) {
        return this.mWifiVendorHal.startSendingOffloadedPacket(ifaceName, slot, NativeUtil.macAddressToByteArray(getMacAddress(ifaceName)), dstMac, packet, protocol, period);
    }

    public int stopSendingOffloadedPacket(String ifaceName, int slot) {
        return this.mWifiVendorHal.stopSendingOffloadedPacket(ifaceName, slot);
    }

    public int startRssiMonitoring(String ifaceName, byte maxRssi, byte minRssi, WifiRssiEventHandler rssiEventHandler) {
        return this.mWifiVendorHal.startRssiMonitoring(ifaceName, maxRssi, minRssi, rssiEventHandler);
    }

    public int stopRssiMonitoring(String ifaceName) {
        return this.mWifiVendorHal.stopRssiMonitoring(ifaceName);
    }

    public WlanWakeReasonAndCounts getWlanWakeReasonCount() {
        return this.mWifiVendorHal.getWlanWakeReasonCount();
    }

    public boolean configureNeighborDiscoveryOffload(String ifaceName, boolean enabled) {
        return this.mWifiVendorHal.configureNeighborDiscoveryOffload(ifaceName, enabled);
    }

    public boolean getRoamingCapabilities(String ifaceName, RoamingCapabilities capabilities) {
        return this.mWifiVendorHal.getRoamingCapabilities(ifaceName, capabilities);
    }

    public int enableFirmwareRoaming(String ifaceName, int state) {
        return this.mWifiVendorHal.enableFirmwareRoaming(ifaceName, state);
    }

    public boolean configureRoaming(String ifaceName, RoamingConfig config) {
        return this.mWifiVendorHal.configureRoaming(ifaceName, config);
    }

    public boolean resetRoamingConfiguration(String ifaceName) {
        return this.mWifiVendorHal.configureRoaming(ifaceName, new RoamingConfig());
    }

    public boolean selectTxPowerScenario(SarInfo sarInfo) {
        return this.mWifiVendorHal.selectTxPowerScenario(sarInfo);
    }

    static {
        System.loadLibrary("wifi-service");
        registerNatives();
    }

    public synchronized String readKernelLog() {
        byte[] bytes = readKernelLogNative();
        if (bytes == null) {
            return "*** failed to read kernel log ***";
        }
        try {
            return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            return new String(bytes, StandardCharsets.ISO_8859_1);
        }
    }
}
