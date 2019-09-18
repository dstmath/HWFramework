package android.net.wifi;

import android.annotation.SystemApi;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.pm.ParceledListSlice;
import android.net.DhcpInfo;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.wifi.ISoftApCallback;
import android.net.wifi.WifiManager;
import android.net.wifi.hotspot2.IProvisioningCallback;
import android.net.wifi.hotspot2.OsuProvider;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.net.wifi.hotspot2.ProvisioningCallback;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.os.WorkSource;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.AsyncChannel;
import com.android.server.net.NetworkPinner;
import dalvik.system.CloseGuard;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

public class WifiManager {
    public static final String ACTION_PASSPOINT_DEAUTH_IMMINENT = "android.net.wifi.action.PASSPOINT_DEAUTH_IMMINENT";
    public static final String ACTION_PASSPOINT_ICON = "android.net.wifi.action.PASSPOINT_ICON";
    public static final String ACTION_PASSPOINT_OSU_PROVIDERS_LIST = "android.net.wifi.action.PASSPOINT_OSU_PROVIDERS_LIST";
    public static final String ACTION_PASSPOINT_SUBSCRIPTION_REMEDIATION = "android.net.wifi.action.PASSPOINT_SUBSCRIPTION_REMEDIATION";
    public static final String ACTION_PICK_WIFI_NETWORK = "android.net.wifi.PICK_WIFI_NETWORK";
    public static final String ACTION_REQUEST_DISABLE = "android.net.wifi.action.REQUEST_DISABLE";
    public static final String ACTION_REQUEST_ENABLE = "android.net.wifi.action.REQUEST_ENABLE";
    public static final String ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE = "android.net.wifi.action.REQUEST_SCAN_ALWAYS_AVAILABLE";
    private static final int BASE = 151552;
    @Deprecated
    public static final String BATCHED_SCAN_RESULTS_AVAILABLE_ACTION = "android.net.wifi.BATCHED_RESULTS";
    public static final int BUSY = 2;
    public static final int CANCEL_WPS = 151566;
    public static final int CANCEL_WPS_FAILED = 151567;
    public static final int CANCEL_WPS_SUCCEDED = 151568;
    @SystemApi
    public static final int CHANGE_REASON_ADDED = 0;
    @SystemApi
    public static final int CHANGE_REASON_CONFIG_CHANGE = 2;
    @SystemApi
    public static final int CHANGE_REASON_REMOVED = 1;
    @SystemApi
    public static final String CONFIGURED_NETWORKS_CHANGED_ACTION = "android.net.wifi.CONFIGURED_NETWORKS_CHANGE";
    public static final int CONNECT_NETWORK = 151553;
    public static final int CONNECT_NETWORK_FAILED = 151554;
    public static final int CONNECT_NETWORK_SUCCEEDED = 151555;
    public static final int DATA_ACTIVITY_IN = 1;
    public static final int DATA_ACTIVITY_INOUT = 3;
    public static final int DATA_ACTIVITY_NONE = 0;
    public static final int DATA_ACTIVITY_NOTIFICATION = 1;
    public static final int DATA_ACTIVITY_OUT = 2;
    public static final boolean DEFAULT_POOR_NETWORK_AVOIDANCE_ENABLED = false;
    public static final int DISABLE_NETWORK = 151569;
    public static final int DISABLE_NETWORK_FAILED = 151570;
    public static final int DISABLE_NETWORK_SUCCEEDED = 151571;
    public static final int ERROR = 0;
    @Deprecated
    public static final int ERROR_AUTHENTICATING = 1;
    @Deprecated
    public static final int ERROR_AUTH_FAILURE_EAP_FAILURE = 3;
    @Deprecated
    public static final int ERROR_AUTH_FAILURE_NONE = 0;
    @Deprecated
    public static final int ERROR_AUTH_FAILURE_TIMEOUT = 1;
    @Deprecated
    public static final int ERROR_AUTH_FAILURE_WRONG_PSWD = 2;
    public static final String EXTRA_ANQP_ELEMENT_DATA = "android.net.wifi.extra.ANQP_ELEMENT_DATA";
    @Deprecated
    public static final String EXTRA_BSSID = "bssid";
    public static final String EXTRA_BSSID_LONG = "android.net.wifi.extra.BSSID_LONG";
    @SystemApi
    public static final String EXTRA_CHANGE_REASON = "changeReason";
    public static final String EXTRA_DELAY = "android.net.wifi.extra.DELAY";
    public static final String EXTRA_ESS = "android.net.wifi.extra.ESS";
    public static final String EXTRA_FILENAME = "android.net.wifi.extra.FILENAME";
    public static final String EXTRA_ICON = "android.net.wifi.extra.ICON";
    public static final String EXTRA_LINK_PROPERTIES = "linkProperties";
    @SystemApi
    public static final String EXTRA_MULTIPLE_NETWORKS_CHANGED = "multipleChanges";
    public static final String EXTRA_NETWORK_CAPABILITIES = "networkCapabilities";
    public static final String EXTRA_NETWORK_INFO = "networkInfo";
    public static final String EXTRA_NEW_RSSI = "newRssi";
    @Deprecated
    public static final String EXTRA_NEW_STATE = "newState";
    public static final String EXTRA_PPPOE_RESULT_ERROR_CODE = "pppoe_result_error_code";
    public static final String EXTRA_PPPOE_RESULT_STATUS = "pppoe_result_status";
    public static final String EXTRA_PPPOE_STATE = "pppoe_state";
    @SystemApi
    public static final String EXTRA_PREVIOUS_WIFI_AP_STATE = "previous_wifi_state";
    public static final String EXTRA_PREVIOUS_WIFI_STATE = "previous_wifi_state";
    public static final String EXTRA_RESULTS_UPDATED = "resultsUpdated";
    public static final String EXTRA_SCAN_AVAILABLE = "scan_enabled";
    public static final String EXTRA_SUBSCRIPTION_REMEDIATION_METHOD = "android.net.wifi.extra.SUBSCRIPTION_REMEDIATION_METHOD";
    @Deprecated
    public static final String EXTRA_SUPPLICANT_CONNECTED = "connected";
    @Deprecated
    public static final String EXTRA_SUPPLICANT_ERROR = "supplicantError";
    @Deprecated
    public static final String EXTRA_SUPPLICANT_ERROR_REASON = "supplicantErrorReason";
    public static final String EXTRA_URL = "android.net.wifi.extra.URL";
    public static final String EXTRA_WIFI_AP_FAILURE_REASON = "wifi_ap_error_code";
    public static final String EXTRA_WIFI_AP_INTERFACE_NAME = "wifi_ap_interface_name";
    public static final String EXTRA_WIFI_AP_MODE = "wifi_ap_mode";
    @SystemApi
    public static final String EXTRA_WIFI_AP_STATE = "wifi_state";
    @SystemApi
    public static final String EXTRA_WIFI_CONFIGURATION = "wifiConfiguration";
    @SystemApi
    public static final String EXTRA_WIFI_CREDENTIAL_EVENT_TYPE = "et";
    @SystemApi
    public static final String EXTRA_WIFI_CREDENTIAL_SSID = "ssid";
    @Deprecated
    public static final String EXTRA_WIFI_INFO = "wifiInfo";
    public static final String EXTRA_WIFI_STATE = "wifi_state";
    public static final int FORGET_NETWORK = 151556;
    public static final int FORGET_NETWORK_FAILED = 151557;
    public static final int FORGET_NETWORK_SUCCEEDED = 151558;
    public static final String HISI_AP_START_BEGIN = "hisi_ap_start_begin";
    public static final int HOTSPOT_FAILED = 2;
    public static final int HOTSPOT_OBSERVER_REGISTERED = 3;
    public static final int HOTSPOT_STARTED = 0;
    public static final int HOTSPOT_STOPPED = 1;
    protected static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    public static final int IFACE_IP_MODE_CONFIGURATION_ERROR = 0;
    public static final int IFACE_IP_MODE_LOCAL_ONLY = 2;
    public static final int IFACE_IP_MODE_TETHERED = 1;
    public static final int IFACE_IP_MODE_UNSPECIFIED = -1;
    public static final int INVALID_ARGS = 8;
    private static final int INVALID_KEY = 0;
    public static final int IN_PROGRESS = 1;
    public static final String LINK_CONFIGURATION_CHANGED_ACTION = "android.net.wifi.LINK_CONFIGURATION_CHANGED";
    private static final int MAX_ACTIVE_LOCKS = 50;
    private static final int MAX_RSSI = -55;
    public static final int MDM_CONNECTION_ERROR = 1000;
    private static final int MIN_RSSI = -100;
    public static final int NATIVE_DAEMON_EVENT = 151555;
    public static final String NETWORK_IDS_CHANGED_ACTION = "android.net.wifi.NETWORK_IDS_CHANGED";
    public static final String NETWORK_STATE_CHANGED_ACTION = "android.net.wifi.STATE_CHANGE";
    public static final int NOT_AUTHORIZED = 9;
    public static final int PPPOE_BASE = 589824;
    public static final String PPPOE_COMPLETED_ACTION = "android.net.wifi.PPPOE_COMPLETED_ACTION";
    public static final String PPPOE_RESULT_ALREADY_ONLINE = "ALREADY_ONLINE";
    public static final String PPPOE_RESULT_FAILED = "FAILURE";
    public static final String PPPOE_RESULT_SUCCESS = "SUCCESS";
    public static final int PPPOE_START = 589825;
    public static final String PPPOE_STATE_CHANGED_ACTION = "android.net.wifi.PPPOE_STATE_CHANGED";
    public static final String PPPOE_STATE_CONNECTED = "PPPOE_STATE_CONNECTED";
    public static final String PPPOE_STATE_CONNECTING = "PPPOE_STATE_CONNECTING";
    public static final String PPPOE_STATE_DISCONNECTED = "PPPOE_STATE_DISCONNECTED";
    public static final String PPPOE_STATE_DISCONNECTING = "PPPOE_STATE_DISCONNECTING";
    public static final int PPPOE_STOP = 589826;
    public static final String RSSI_CHANGED_ACTION = "android.net.wifi.RSSI_CHANGED";
    public static final int RSSI_LEVELS = 5;
    public static final int RSSI_PKTCNT_FETCH = 151572;
    public static final int RSSI_PKTCNT_FETCH_FAILED = 151574;
    public static final int RSSI_PKTCNT_FETCH_SUCCEEDED = 151573;
    public static final int SAP_START_FAILURE_GENERAL = 0;
    public static final int SAP_START_FAILURE_NO_CHANNEL = 1;
    public static final int SAVE_NETWORK = 151559;
    public static final int SAVE_NETWORK_FAILED = 151560;
    public static final int SAVE_NETWORK_SUCCEEDED = 151561;
    public static final String SCAN_RESULTS_AVAILABLE_ACTION = "android.net.wifi.SCAN_RESULTS";
    public static final int START_VOWIFI_DETECT = 151575;
    public static final int START_WPS = 151562;
    public static final int START_WPS_SUCCEEDED = 151563;
    @Deprecated
    public static final String SUPPLICANT_CONNECTION_CHANGE_ACTION = "android.net.wifi.supplicant.CONNECTION_CHANGE";
    @Deprecated
    public static final String SUPPLICANT_STATE_CHANGED_ACTION = "android.net.wifi.supplicant.STATE_CHANGE";
    public static final String SUPPLICANT_WAPI_EVENT = "android.net.wifi.supplicant.WAPI_EVENT";
    private static final String TAG = "WifiManager";
    public static final int VOWIFI_DETECTINT = 151576;
    public static final int WAPI_EVENT_AUTH_FAIL_CODE = 16;
    public static final int WAPI_EVENT_CERT_FAIL_CODE = 17;
    @SystemApi
    public static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
    @SystemApi
    public static final int WIFI_AP_STATE_DISABLED = 11;
    @SystemApi
    public static final int WIFI_AP_STATE_DISABLING = 10;
    @SystemApi
    public static final int WIFI_AP_STATE_ENABLED = 13;
    @SystemApi
    public static final int WIFI_AP_STATE_ENABLING = 12;
    @SystemApi
    public static final int WIFI_AP_STATE_FAILED = 14;
    @SystemApi
    public static final String WIFI_CREDENTIAL_CHANGED_ACTION = "android.net.wifi.WIFI_CREDENTIAL_CHANGED";
    @SystemApi
    public static final int WIFI_CREDENTIAL_FORGOT = 1;
    @SystemApi
    public static final int WIFI_CREDENTIAL_SAVED = 0;
    public static final int WIFI_DETECT_MODE_HIGH = 2;
    public static final int WIFI_DETECT_MODE_LOW = 1;
    public static final int WIFI_DETECT_MODE_OFF = 0;
    public static final int WIFI_FEATURE_ADDITIONAL_STA = 2048;
    public static final int WIFI_FEATURE_AP_STA = 32768;
    public static final int WIFI_FEATURE_AWARE = 64;
    public static final int WIFI_FEATURE_BATCH_SCAN = 512;
    public static final int WIFI_FEATURE_CONFIG_NDO = 2097152;
    public static final int WIFI_FEATURE_CONTROL_ROAMING = 8388608;
    public static final int WIFI_FEATURE_D2AP_RTT = 256;
    public static final int WIFI_FEATURE_D2D_RTT = 128;
    public static final int WIFI_FEATURE_EPR = 16384;
    public static final int WIFI_FEATURE_HAL_EPNO = 262144;
    public static final int WIFI_FEATURE_IE_WHITELIST = 16777216;
    public static final int WIFI_FEATURE_INFRA = 1;
    public static final int WIFI_FEATURE_INFRA_5G = 2;
    public static final int WIFI_FEATURE_LINK_LAYER_STATS = 65536;
    public static final int WIFI_FEATURE_LOGGER = 131072;
    public static final int WIFI_FEATURE_MKEEP_ALIVE = 1048576;
    public static final int WIFI_FEATURE_MOBILE_HOTSPOT = 16;
    public static final int WIFI_FEATURE_P2P = 8;
    public static final int WIFI_FEATURE_PASSPOINT = 4;
    public static final int WIFI_FEATURE_PNO = 1024;
    public static final int WIFI_FEATURE_RSSI_MONITOR = 524288;
    public static final int WIFI_FEATURE_SCANNER = 32;
    public static final int WIFI_FEATURE_SCAN_RAND = 33554432;
    public static final int WIFI_FEATURE_TDLS = 4096;
    public static final int WIFI_FEATURE_TDLS_OFFCHANNEL = 8192;
    public static final int WIFI_FEATURE_TRANSMIT_POWER = 4194304;
    public static final int WIFI_FEATURE_TX_POWER_LIMIT = 67108864;
    public static final int WIFI_FREQUENCY_BAND_2GHZ = 2;
    public static final int WIFI_FREQUENCY_BAND_5GHZ = 1;
    public static final int WIFI_FREQUENCY_BAND_AUTO = 0;
    public static final int WIFI_MODE_FULL = 1;
    public static final int WIFI_MODE_FULL_HIGH_PERF = 3;
    public static final int WIFI_MODE_NO_LOCKS_HELD = 0;
    public static final int WIFI_MODE_SCAN_ONLY = 2;
    public static final String WIFI_SCAN_AVAILABLE = "wifi_scan_available";
    public static final String WIFI_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_STATE_CHANGED";
    public static final int WIFI_STATE_DISABLED = 1;
    public static final int WIFI_STATE_DISABLING = 0;
    public static final int WIFI_STATE_ENABLED = 3;
    public static final int WIFI_STATE_ENABLING = 2;
    public static final int WIFI_STATE_UNKNOWN = 4;
    public static final int WPS_AUTH_FAILURE = 6;
    public static final int WPS_COMPLETED = 151565;
    public static final int WPS_FAILED = 151564;
    public static final int WPS_OVERLAP_ERROR = 3;
    public static final int WPS_TIMED_OUT = 7;
    public static final int WPS_TKIP_ONLY_PROHIBITED = 5;
    public static final int WPS_WEP_PROHIBITED = 4;
    /* access modifiers changed from: private */
    public static final Object sServiceHandlerDispatchLock = new Object();
    /* access modifiers changed from: private */
    public int mActiveLockCount;
    /* access modifiers changed from: private */
    public AsyncChannel mAsyncChannel;
    /* access modifiers changed from: private */
    public CountDownLatch mConnected;
    private Context mContext;
    @GuardedBy("mLock")
    private LocalOnlyHotspotCallbackProxy mLOHSCallbackProxy;
    @GuardedBy("mLock")
    private LocalOnlyHotspotObserverProxy mLOHSObserverProxy;
    private int mListenerKey = 1;
    /* access modifiers changed from: private */
    public final SparseArray mListenerMap = new SparseArray();
    /* access modifiers changed from: private */
    public final Object mListenerMapLock = new Object();
    private final Object mLock = new Object();
    private Looper mLooper;
    IWifiManager mService;
    private final int mTargetSdkVersion;
    public VoWifiSignalDetectInterruptCallback mVoWifiCallback = null;

    @SystemApi
    public interface ActionListener {
        void onFailure(int i);

        void onSuccess();
    }

    public static class LocalOnlyHotspotCallback {
        public static final int ERROR_GENERIC = 2;
        public static final int ERROR_INCOMPATIBLE_MODE = 3;
        public static final int ERROR_NO_CHANNEL = 1;
        public static final int ERROR_TETHERING_DISALLOWED = 4;
        public static final int REQUEST_REGISTERED = 0;

        public void onStarted(LocalOnlyHotspotReservation reservation) {
        }

        public void onStopped() {
        }

        public void onFailed(int reason) {
        }
    }

    private static class LocalOnlyHotspotCallbackProxy {
        private final Handler mHandler;
        private final Looper mLooper;
        private final Messenger mMessenger = new Messenger(this.mHandler);
        /* access modifiers changed from: private */
        public final WeakReference<WifiManager> mWifiManager;

        LocalOnlyHotspotCallbackProxy(WifiManager manager, Looper looper, final LocalOnlyHotspotCallback callback) {
            this.mWifiManager = new WeakReference<>(manager);
            this.mLooper = looper;
            this.mHandler = new Handler(looper) {
                public void handleMessage(Message msg) {
                    Log.d(WifiManager.TAG, "LocalOnlyHotspotCallbackProxy: handle message what: " + msg.what + " msg: " + msg);
                    WifiManager manager = (WifiManager) LocalOnlyHotspotCallbackProxy.this.mWifiManager.get();
                    if (manager == null) {
                        Log.w(WifiManager.TAG, "LocalOnlyHotspotCallbackProxy: handle message post GC");
                        return;
                    }
                    switch (msg.what) {
                        case 0:
                            WifiConfiguration config = (WifiConfiguration) msg.obj;
                            if (config != null) {
                                LocalOnlyHotspotCallback localOnlyHotspotCallback = callback;
                                Objects.requireNonNull(manager);
                                localOnlyHotspotCallback.onStarted(new LocalOnlyHotspotReservation(config));
                                break;
                            } else {
                                Log.e(WifiManager.TAG, "LocalOnlyHotspotCallbackProxy: config cannot be null.");
                                callback.onFailed(2);
                                return;
                            }
                        case 1:
                            Log.w(WifiManager.TAG, "LocalOnlyHotspotCallbackProxy: hotspot stopped");
                            callback.onStopped();
                            break;
                        case 2:
                            int reasonCode = msg.arg1;
                            Log.w(WifiManager.TAG, "LocalOnlyHotspotCallbackProxy: failed to start.  reason: " + reasonCode);
                            callback.onFailed(reasonCode);
                            Log.w(WifiManager.TAG, "done with the callback...");
                            break;
                        default:
                            Log.e(WifiManager.TAG, "LocalOnlyHotspotCallbackProxy unhandled message.  type: " + msg.what);
                            break;
                    }
                }
            };
        }

        public Messenger getMessenger() {
            return this.mMessenger;
        }

        public void notifyFailed(int reason) throws RemoteException {
            Message msg = Message.obtain();
            msg.what = 2;
            msg.arg1 = reason;
            this.mMessenger.send(msg);
        }
    }

    public static class LocalOnlyHotspotObserver {
        public void onRegistered(LocalOnlyHotspotSubscription subscription) {
        }

        public void onStarted(WifiConfiguration config) {
        }

        public void onStopped() {
        }
    }

    private static class LocalOnlyHotspotObserverProxy {
        private final Handler mHandler;
        private final Looper mLooper;
        private final Messenger mMessenger = new Messenger(this.mHandler);
        /* access modifiers changed from: private */
        public final WeakReference<WifiManager> mWifiManager;

        LocalOnlyHotspotObserverProxy(WifiManager manager, Looper looper, final LocalOnlyHotspotObserver observer) {
            this.mWifiManager = new WeakReference<>(manager);
            this.mLooper = looper;
            this.mHandler = new Handler(looper) {
                public void handleMessage(Message msg) {
                    Log.d(WifiManager.TAG, "LocalOnlyHotspotObserverProxy: handle message what: " + msg.what + " msg: " + msg);
                    WifiManager manager = (WifiManager) LocalOnlyHotspotObserverProxy.this.mWifiManager.get();
                    if (manager == null) {
                        Log.w(WifiManager.TAG, "LocalOnlyHotspotObserverProxy: handle message post GC");
                        return;
                    }
                    int i = msg.what;
                    if (i != 3) {
                        switch (i) {
                            case 0:
                                WifiConfiguration config = (WifiConfiguration) msg.obj;
                                if (config != null) {
                                    observer.onStarted(config);
                                    break;
                                } else {
                                    Log.e(WifiManager.TAG, "LocalOnlyHotspotObserverProxy: config cannot be null.");
                                    return;
                                }
                            case 1:
                                observer.onStopped();
                                break;
                            default:
                                Log.e(WifiManager.TAG, "LocalOnlyHotspotObserverProxy unhandled message.  type: " + msg.what);
                                break;
                        }
                    } else {
                        LocalOnlyHotspotObserver localOnlyHotspotObserver = observer;
                        Objects.requireNonNull(manager);
                        localOnlyHotspotObserver.onRegistered(new LocalOnlyHotspotSubscription());
                    }
                }
            };
        }

        public Messenger getMessenger() {
            return this.mMessenger;
        }

        public void registered() throws RemoteException {
            Message msg = Message.obtain();
            msg.what = 3;
            this.mMessenger.send(msg);
        }
    }

    public class LocalOnlyHotspotReservation implements AutoCloseable {
        private final CloseGuard mCloseGuard = CloseGuard.get();
        private final WifiConfiguration mConfig;

        @VisibleForTesting
        public LocalOnlyHotspotReservation(WifiConfiguration config) {
            this.mConfig = config;
            this.mCloseGuard.open("close");
        }

        public WifiConfiguration getWifiConfiguration() {
            return this.mConfig;
        }

        public void close() {
            try {
                WifiManager.this.stopLocalOnlyHotspot();
                this.mCloseGuard.close();
            } catch (Exception e) {
                Log.e(WifiManager.TAG, "Failed to stop Local Only Hotspot.");
            }
        }

        /* access modifiers changed from: protected */
        public void finalize() throws Throwable {
            try {
                if (this.mCloseGuard != null) {
                    this.mCloseGuard.warnIfOpen();
                }
                close();
            } finally {
                super.finalize();
            }
        }
    }

    public class LocalOnlyHotspotSubscription implements AutoCloseable {
        private final CloseGuard mCloseGuard = CloseGuard.get();

        @VisibleForTesting
        public LocalOnlyHotspotSubscription() {
            this.mCloseGuard.open("close");
        }

        public void close() {
            try {
                WifiManager.this.unregisterLocalOnlyHotspotObserver();
                this.mCloseGuard.close();
            } catch (Exception e) {
                Log.e(WifiManager.TAG, "Failed to unregister LocalOnlyHotspotObserver.");
            }
        }

        /* access modifiers changed from: protected */
        public void finalize() throws Throwable {
            try {
                if (this.mCloseGuard != null) {
                    this.mCloseGuard.warnIfOpen();
                }
                close();
            } finally {
                super.finalize();
            }
        }
    }

    public class MulticastLock {
        private final IBinder mBinder;
        private boolean mHeld;
        private int mRefCount;
        private boolean mRefCounted;
        private String mTag;

        private MulticastLock(String tag) {
            this.mTag = tag;
            this.mBinder = new Binder();
            this.mRefCount = 0;
            this.mRefCounted = true;
            this.mHeld = false;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:9:0x0012, code lost:
            if (r5.mHeld == false) goto L_0x0014;
         */
        public void acquire() {
            synchronized (this.mBinder) {
                if (this.mRefCounted) {
                    int i = this.mRefCount + 1;
                    this.mRefCount = i;
                    if (i == 1) {
                    }
                }
                try {
                    WifiManager.this.mService.acquireMulticastLock(this.mBinder, this.mTag);
                    synchronized (WifiManager.this) {
                        if (WifiManager.this.mActiveLockCount < 50) {
                            int unused = WifiManager.this.mActiveLockCount = WifiManager.this.mActiveLockCount + 1;
                        } else {
                            WifiManager.this.mService.releaseMulticastLock();
                            throw new UnsupportedOperationException("Exceeded maximum number of wifi locks");
                        }
                    }
                    this.mHeld = true;
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:9:0x0012, code lost:
            if (r4.mHeld != false) goto L_0x0014;
         */
        /* JADX WARNING: Removed duplicated region for block: B:30:0x0036 A[DONT_GENERATE] */
        /* JADX WARNING: Removed duplicated region for block: B:32:0x0038  */
        public void release() {
            synchronized (this.mBinder) {
                if (this.mRefCounted) {
                    int i = this.mRefCount - 1;
                    this.mRefCount = i;
                    if (i == 0) {
                    }
                    if (this.mRefCount >= 0) {
                        throw new RuntimeException("MulticastLock under-locked " + this.mTag);
                    }
                }
                try {
                    WifiManager.this.mService.releaseMulticastLock();
                    synchronized (WifiManager.this) {
                        WifiManager.access$910(WifiManager.this);
                    }
                    this.mHeld = false;
                    if (this.mRefCount >= 0) {
                    }
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }

        public void setReferenceCounted(boolean refCounted) {
            this.mRefCounted = refCounted;
        }

        public boolean isHeld() {
            boolean z;
            synchronized (this.mBinder) {
                z = this.mHeld;
            }
            return z;
        }

        public String toString() {
            String s3;
            String str;
            synchronized (this.mBinder) {
                String s1 = Integer.toHexString(System.identityHashCode(this));
                String s2 = this.mHeld ? "held; " : "";
                if (this.mRefCounted) {
                    s3 = "refcounted: refcount = " + this.mRefCount;
                } else {
                    s3 = "not refcounted";
                }
                str = "MulticastLock{ " + s1 + "; " + s2 + s3 + " }";
            }
            return str;
        }

        /* access modifiers changed from: protected */
        public void finalize() throws Throwable {
            super.finalize();
            setReferenceCounted(false);
            release();
        }
    }

    private static class ProvisioningCallbackProxy extends IProvisioningCallback.Stub {
        private final ProvisioningCallback mCallback;
        private final Handler mHandler;

        ProvisioningCallbackProxy(Looper looper, ProvisioningCallback callback) {
            this.mHandler = new Handler(looper);
            this.mCallback = callback;
        }

        public void onProvisioningStatus(int status) {
            this.mHandler.post(new Runnable(status) {
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    WifiManager.ProvisioningCallbackProxy.this.mCallback.onProvisioningStatus(this.f$1);
                }
            });
        }

        public void onProvisioningFailure(int status) {
            this.mHandler.post(new Runnable(status) {
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    WifiManager.ProvisioningCallbackProxy.this.mCallback.onProvisioningFailure(this.f$1);
                }
            });
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SapStartFailure {
    }

    private class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            synchronized (WifiManager.sServiceHandlerDispatchLock) {
                dispatchMessageToListeners(message);
            }
        }

        private void dispatchMessageToListeners(Message message) {
            Object listener = WifiManager.this.removeListener(message.arg2);
            switch (message.what) {
                case 69632:
                    if (message.arg1 == 0) {
                        WifiManager.this.mAsyncChannel.sendMessage(69633);
                    } else {
                        Log.e(WifiManager.TAG, "Failed to set up channel connection");
                        AsyncChannel unused = WifiManager.this.mAsyncChannel = null;
                    }
                    WifiManager.this.mConnected.countDown();
                    return;
                case 69636:
                    Log.e(WifiManager.TAG, "Channel connection lost");
                    AsyncChannel unused2 = WifiManager.this.mAsyncChannel = null;
                    getLooper().quit();
                    return;
                case WifiManager.CONNECT_NETWORK_FAILED /*151554*/:
                case WifiManager.FORGET_NETWORK_FAILED /*151557*/:
                case WifiManager.SAVE_NETWORK_FAILED /*151560*/:
                case WifiManager.DISABLE_NETWORK_FAILED /*151570*/:
                    if (listener != null) {
                        ((ActionListener) listener).onFailure(message.arg1);
                        return;
                    }
                    return;
                case 151555:
                case WifiManager.FORGET_NETWORK_SUCCEEDED /*151558*/:
                case WifiManager.SAVE_NETWORK_SUCCEEDED /*151561*/:
                case WifiManager.DISABLE_NETWORK_SUCCEEDED /*151571*/:
                    if (listener != null) {
                        ((ActionListener) listener).onSuccess();
                        return;
                    }
                    return;
                case WifiManager.START_WPS_SUCCEEDED /*151563*/:
                    if (listener != null) {
                        ((WpsCallback) listener).onStarted(((WpsResult) message.obj).pin);
                        synchronized (WifiManager.this.mListenerMapLock) {
                            WifiManager.this.mListenerMap.put(message.arg2, listener);
                        }
                        return;
                    }
                    return;
                case WifiManager.WPS_FAILED /*151564*/:
                    if (listener != null) {
                        ((WpsCallback) listener).onFailed(message.arg1);
                        return;
                    }
                    return;
                case WifiManager.WPS_COMPLETED /*151565*/:
                    if (listener != null) {
                        ((WpsCallback) listener).onSucceeded();
                        return;
                    }
                    return;
                case WifiManager.CANCEL_WPS_FAILED /*151567*/:
                    if (listener != null) {
                        ((WpsCallback) listener).onFailed(message.arg1);
                        return;
                    }
                    return;
                case WifiManager.CANCEL_WPS_SUCCEDED /*151568*/:
                    if (listener != null) {
                        ((WpsCallback) listener).onSucceeded();
                        return;
                    }
                    return;
                case WifiManager.RSSI_PKTCNT_FETCH_SUCCEEDED /*151573*/:
                    if (listener != null) {
                        RssiPacketCountInfo info = (RssiPacketCountInfo) message.obj;
                        if (info != null) {
                            ((TxPacketCountListener) listener).onSuccess(info.txgood + info.txbad);
                            return;
                        } else {
                            ((TxPacketCountListener) listener).onFailure(0);
                            return;
                        }
                    } else {
                        return;
                    }
                case WifiManager.RSSI_PKTCNT_FETCH_FAILED /*151574*/:
                    if (listener != null) {
                        ((TxPacketCountListener) listener).onFailure(message.arg1);
                        return;
                    }
                    return;
                case WifiManager.VOWIFI_DETECTINT /*151576*/:
                    Log.i(WifiManager.TAG, "receive vowifi detect int 1");
                    if (WifiManager.this.mVoWifiCallback != null) {
                        Log.i(WifiManager.TAG, "receive vowifi detect int 2");
                        WifiManager.this.mVoWifiCallback.onVoWifiSignalInterrupt(message.arg1);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public interface SoftApCallback {
        void onNumClientsChanged(int i);

        void onStateChanged(int i, int i2);
    }

    private static class SoftApCallbackProxy extends ISoftApCallback.Stub {
        private final SoftApCallback mCallback;
        private final Handler mHandler;

        SoftApCallbackProxy(Looper looper, SoftApCallback callback) {
            this.mHandler = new Handler(looper);
            this.mCallback = callback;
        }

        public void onStateChanged(int state, int failureReason) throws RemoteException {
            Log.v(WifiManager.TAG, "SoftApCallbackProxy: onStateChanged: state=" + state + ", failureReason=" + failureReason);
            this.mHandler.post(new Runnable(state, failureReason) {
                private final /* synthetic */ int f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    WifiManager.SoftApCallbackProxy.this.mCallback.onStateChanged(this.f$1, this.f$2);
                }
            });
        }

        public void onNumClientsChanged(int numClients) throws RemoteException {
            Log.v(WifiManager.TAG, "SoftApCallbackProxy: onNumClientsChanged: numClients=" + numClients);
            this.mHandler.post(new Runnable(numClients) {
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    WifiManager.SoftApCallbackProxy.this.mCallback.onNumClientsChanged(this.f$1);
                }
            });
        }
    }

    public interface TxPacketCountListener {
        void onFailure(int i);

        void onSuccess(int i);
    }

    public interface VoWifiSignalDetectInterruptCallback {
        void onVoWifiSignalInterrupt(int i);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface WifiApState {
    }

    public class WifiLock {
        private final IBinder mBinder;
        private boolean mHeld;
        int mLockType;
        private int mRefCount;
        private boolean mRefCounted;
        private String mTag;
        private WorkSource mWorkSource;

        private WifiLock(int lockType, String tag) {
            this.mTag = tag;
            this.mLockType = lockType;
            this.mBinder = new Binder();
            this.mRefCount = 0;
            this.mRefCounted = true;
            this.mHeld = false;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:9:0x0012, code lost:
            if (r7.mHeld == false) goto L_0x0014;
         */
        public void acquire() {
            synchronized (this.mBinder) {
                if (this.mRefCounted) {
                    int i = this.mRefCount + 1;
                    this.mRefCount = i;
                    if (i == 1) {
                    }
                }
                try {
                    WifiManager.this.mService.acquireWifiLock(this.mBinder, this.mLockType, this.mTag, this.mWorkSource);
                    synchronized (WifiManager.this) {
                        if (WifiManager.this.mActiveLockCount < 50) {
                            int unused = WifiManager.this.mActiveLockCount = WifiManager.this.mActiveLockCount + 1;
                        } else {
                            WifiManager.this.mService.releaseWifiLock(this.mBinder);
                            throw new UnsupportedOperationException("Exceeded maximum number of wifi locks");
                        }
                    }
                    this.mHeld = true;
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:9:0x0012, code lost:
            if (r4.mHeld != false) goto L_0x0014;
         */
        /* JADX WARNING: Removed duplicated region for block: B:30:0x0038 A[DONT_GENERATE] */
        /* JADX WARNING: Removed duplicated region for block: B:32:0x003a  */
        public void release() {
            synchronized (this.mBinder) {
                if (this.mRefCounted) {
                    int i = this.mRefCount - 1;
                    this.mRefCount = i;
                    if (i == 0) {
                    }
                    if (this.mRefCount >= 0) {
                        throw new RuntimeException("WifiLock under-locked " + this.mTag);
                    }
                }
                try {
                    WifiManager.this.mService.releaseWifiLock(this.mBinder);
                    synchronized (WifiManager.this) {
                        WifiManager.access$910(WifiManager.this);
                    }
                    this.mHeld = false;
                    if (this.mRefCount >= 0) {
                    }
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }

        public void setReferenceCounted(boolean refCounted) {
            this.mRefCounted = refCounted;
        }

        public boolean isHeld() {
            boolean z;
            synchronized (this.mBinder) {
                z = this.mHeld;
            }
            return z;
        }

        public void setWorkSource(WorkSource ws) {
            synchronized (this.mBinder) {
                if (ws != null) {
                    try {
                        if (ws.isEmpty()) {
                            ws = null;
                        }
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    } catch (Throwable th) {
                        throw th;
                    }
                }
                boolean changed = true;
                if (ws == null) {
                    this.mWorkSource = null;
                } else {
                    ws.clearNames();
                    boolean z = true;
                    if (this.mWorkSource == null) {
                        if (this.mWorkSource == null) {
                            z = false;
                        }
                        changed = z;
                        this.mWorkSource = new WorkSource(ws);
                    } else {
                        changed = !this.mWorkSource.equals(ws);
                        if (changed) {
                            this.mWorkSource.set(ws);
                        }
                    }
                }
                if (changed && this.mHeld) {
                    WifiManager.this.mService.updateWifiLockWorkSource(this.mBinder, this.mWorkSource);
                }
            }
        }

        public String toString() {
            String s3;
            String str;
            synchronized (this.mBinder) {
                String s1 = Integer.toHexString(System.identityHashCode(this));
                String s2 = this.mHeld ? "held; " : "";
                if (this.mRefCounted) {
                    s3 = "refcounted: refcount = " + this.mRefCount;
                } else {
                    s3 = "not refcounted";
                }
                str = "WifiLock{ " + s1 + "; " + s2 + s3 + " }";
            }
            return str;
        }

        /* access modifiers changed from: protected */
        public void finalize() throws Throwable {
            super.finalize();
            synchronized (this.mBinder) {
                if (this.mHeld) {
                    try {
                        WifiManager.this.mService.releaseWifiLock(this.mBinder);
                        synchronized (WifiManager.this) {
                            WifiManager.access$910(WifiManager.this);
                        }
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                }
            }
        }
    }

    public static abstract class WpsCallback {
        public abstract void onFailed(int i);

        public abstract void onStarted(String str);

        public abstract void onSucceeded();
    }

    static /* synthetic */ int access$910(WifiManager x0) {
        int i = x0.mActiveLockCount;
        x0.mActiveLockCount = i - 1;
        return i;
    }

    public WifiManager(Context context, IWifiManager service, Looper looper) {
        this.mContext = context;
        this.mService = service;
        this.mLooper = looper;
        this.mTargetSdkVersion = context.getApplicationInfo().targetSdkVersion;
    }

    public List<WifiConfiguration> getConfiguredNetworks() {
        try {
            ParceledListSlice<WifiConfiguration> parceledList = this.mService.getConfiguredNetworks();
            if (parceledList == null) {
                return Collections.emptyList();
            }
            return parceledList.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public List<WifiConfiguration> getPrivilegedConfiguredNetworks() {
        try {
            ParceledListSlice<WifiConfiguration> parceledList = this.mService.getPrivilegedConfiguredNetworks();
            if (parceledList == null) {
                return Collections.emptyList();
            }
            return parceledList.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public WifiConfiguration getMatchingWifiConfig(ScanResult scanResult) {
        try {
            return this.mService.getMatchingWifiConfig(scanResult);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<WifiConfiguration> getAllMatchingWifiConfigs(ScanResult scanResult) {
        try {
            return this.mService.getAllMatchingWifiConfigs(scanResult);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<OsuProvider> getMatchingOsuProviders(ScanResult scanResult) {
        try {
            return this.mService.getMatchingOsuProviders(scanResult);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int addNetwork(WifiConfiguration config) {
        if (config == null) {
            return -1;
        }
        config.networkId = -1;
        return addOrUpdateNetwork(config);
    }

    public int updateNetwork(WifiConfiguration config) {
        if (config == null || config.networkId < 0) {
            return -1;
        }
        return addOrUpdateNetwork(config);
    }

    private int addOrUpdateNetwork(WifiConfiguration config) {
        try {
            return this.mService.addOrUpdateNetwork(config, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void addOrUpdatePasspointConfiguration(PasspointConfiguration config) {
        try {
            if (!this.mService.addOrUpdatePasspointConfiguration(config, this.mContext.getOpPackageName())) {
                throw new IllegalArgumentException();
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void removePasspointConfiguration(String fqdn) {
        try {
            if (!this.mService.removePasspointConfiguration(fqdn, this.mContext.getOpPackageName())) {
                throw new IllegalArgumentException();
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<PasspointConfiguration> getPasspointConfigurations() {
        try {
            return this.mService.getPasspointConfigurations();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void queryPasspointIcon(long bssid, String fileName) {
        try {
            this.mService.queryPasspointIcon(bssid, fileName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int matchProviderWithCurrentNetwork(String fqdn) {
        try {
            return this.mService.matchProviderWithCurrentNetwork(fqdn);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void deauthenticateNetwork(long holdoff, boolean ess) {
        try {
            this.mService.deauthenticateNetwork(holdoff, ess);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean removeNetwork(int netId) {
        try {
            return this.mService.removeNetwork(netId, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean enableNetwork(int netId, boolean attemptConnect) {
        boolean pin = attemptConnect && this.mTargetSdkVersion < 21;
        if (pin) {
            NetworkPinner.pin(this.mContext, new NetworkRequest.Builder().clearCapabilities().addCapability(15).addTransportType(1).build());
        }
        try {
            boolean success = this.mService.enableNetwork(netId, attemptConnect, this.mContext.getOpPackageName());
            if (pin && !success) {
                NetworkPinner.unpin();
            }
            return success;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean disableNetwork(int netId) {
        try {
            return this.mService.disableNetwork(netId, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean disconnect() {
        try {
            this.mService.disconnect(this.mContext.getOpPackageName());
            return true;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean reconnect() {
        try {
            this.mService.reconnect(this.mContext.getOpPackageName());
            return true;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean reassociate() {
        try {
            this.mService.reassociate(this.mContext.getOpPackageName());
            return true;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public boolean pingSupplicant() {
        return isWifiEnabled();
    }

    private int getSupportedFeatures() {
        try {
            return this.mService.getSupportedFeatures();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private boolean isFeatureSupported(int feature) {
        return (getSupportedFeatures() & feature) == feature;
    }

    public boolean is5GHzBandSupported() {
        return isFeatureSupported(2);
    }

    public boolean isPasspointSupported() {
        return isFeatureSupported(4);
    }

    public boolean isP2pSupported() {
        return isFeatureSupported(8);
    }

    @SystemApi
    public boolean isPortableHotspotSupported() {
        return isFeatureSupported(16);
    }

    @SystemApi
    public boolean isWifiScannerSupported() {
        return isFeatureSupported(32);
    }

    public boolean isWifiAwareSupported() {
        return isFeatureSupported(64);
    }

    @SystemApi
    public boolean isDeviceToDeviceRttSupported() {
        return isFeatureSupported(128);
    }

    @SystemApi
    public boolean isDeviceToApRttSupported() {
        return isFeatureSupported(256);
    }

    public boolean isPreferredNetworkOffloadSupported() {
        return isFeatureSupported(1024);
    }

    public boolean isAdditionalStaSupported() {
        return isFeatureSupported(2048);
    }

    public boolean isTdlsSupported() {
        return isFeatureSupported(4096);
    }

    public boolean isOffChannelTdlsSupported() {
        return isFeatureSupported(8192);
    }

    public boolean isEnhancedPowerReportingSupported() {
        return isFeatureSupported(65536);
    }

    public WifiActivityEnergyInfo getControllerActivityEnergyInfo(int updateType) {
        WifiActivityEnergyInfo reportActivityInfo;
        if (this.mService == null) {
            return null;
        }
        try {
            synchronized (this) {
                reportActivityInfo = this.mService.reportActivityInfo();
            }
            return reportActivityInfo;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public boolean startScan() {
        return startScan(null);
    }

    @SystemApi
    public boolean startScan(WorkSource workSource) {
        try {
            return this.mService.startScan(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getCurrentNetworkWpsNfcConfigurationToken() {
        return null;
    }

    public WifiInfo getConnectionInfo() {
        try {
            return this.mService.getConnectionInfo(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ScanResult> getScanResults() {
        try {
            return this.mService.getScanResults(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isScanAlwaysAvailable() {
        try {
            return this.mService.isScanAlwaysAvailable();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public boolean saveConfiguration() {
        return true;
    }

    public void setCountryCode(String country) {
        try {
            this.mService.setCountryCode(country);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getCountryCode() {
        try {
            return this.mService.getCountryCode();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isDualBandSupported() {
        try {
            return this.mService.isDualBandSupported();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isDualModeSupported() {
        try {
            return this.mService.needs5GHzToAnyApBandConversion();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public DhcpInfo getDhcpInfo() {
        try {
            return this.mService.getDhcpInfo();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setWifiEnabled(boolean enabled) {
        try {
            return this.mService.setWifiEnabled(this.mContext.getOpPackageName(), enabled);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setWifiStateByManual(boolean enable) {
        try {
            this.mService.setWifiStateByManual(enable);
        } catch (RemoteException e) {
            Log.e(TAG, "setWifiStateByManual exception:", e);
        }
    }

    public void setWifiApStateByManual(boolean enable) {
        try {
            this.mService.setWifiApStateByManual(enable);
        } catch (RemoteException e) {
            Log.e(TAG, "setWifiApStateByManual exception:", e);
        }
    }

    public void setWifiEnableForP2p(boolean enable) {
        try {
            this.mService.setWifiEnableForP2p(enable);
        } catch (RemoteException e) {
            Log.e(TAG, "setWifiEnableForP2p exception:", e);
        }
    }

    public int getWifiState() {
        try {
            return this.mService.getWifiEnabledState();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isWifiEnabled() {
        return getWifiState() == 3;
    }

    public void getTxPacketCount(TxPacketCountListener listener) {
        getChannel().sendMessage(RSSI_PKTCNT_FETCH, 0, putListener(listener));
    }

    public static int calculateSignalLevel(int rssi, int numLevels) {
        if (rssi <= -100) {
            return 0;
        }
        if (rssi >= -55) {
            return numLevels - 1;
        }
        return (int) ((((float) (rssi + 100)) * ((float) (numLevels - 1))) / 45.0f);
    }

    public static int compareSignalLevel(int rssiA, int rssiB) {
        return rssiA - rssiB;
    }

    public void updateInterfaceIpState(String ifaceName, int mode) {
        try {
            this.mService.updateInterfaceIpState(ifaceName, mode);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean startSoftAp(WifiConfiguration wifiConfig) {
        try {
            return this.mService.startSoftAp(wifiConfig);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean stopSoftAp() {
        try {
            return this.mService.stopSoftAp();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void startLocalOnlyHotspot(LocalOnlyHotspotCallback callback, Handler handler) {
        Looper looper;
        synchronized (this.mLock) {
            if (handler == null) {
                try {
                    looper = this.mContext.getMainLooper();
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                looper = handler.getLooper();
            }
            LocalOnlyHotspotCallbackProxy proxy = new LocalOnlyHotspotCallbackProxy(this, looper, callback);
            int returnCode = this.mService.startLocalOnlyHotspot(proxy.getMessenger(), new Binder(), this.mContext.getOpPackageName());
            if (returnCode != 0) {
                proxy.notifyFailed(returnCode);
            } else {
                this.mLOHSCallbackProxy = proxy;
            }
        }
    }

    public void cancelLocalOnlyHotspotRequest() {
        synchronized (this.mLock) {
            stopLocalOnlyHotspot();
        }
    }

    /* access modifiers changed from: private */
    public void stopLocalOnlyHotspot() {
        synchronized (this.mLock) {
            if (this.mLOHSCallbackProxy != null) {
                this.mLOHSCallbackProxy = null;
                try {
                    this.mService.stopLocalOnlyHotspot();
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }
    }

    public void watchLocalOnlyHotspot(LocalOnlyHotspotObserver observer, Handler handler) {
        Looper looper;
        synchronized (this.mLock) {
            if (handler == null) {
                try {
                    looper = this.mContext.getMainLooper();
                } catch (RemoteException e) {
                    this.mLOHSObserverProxy = null;
                    throw e.rethrowFromSystemServer();
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                looper = handler.getLooper();
            }
            this.mLOHSObserverProxy = new LocalOnlyHotspotObserverProxy(this, looper, observer);
            this.mService.startWatchLocalOnlyHotspot(this.mLOHSObserverProxy.getMessenger(), new Binder());
            this.mLOHSObserverProxy.registered();
        }
    }

    public void unregisterLocalOnlyHotspotObserver() {
        synchronized (this.mLock) {
            if (this.mLOHSObserverProxy != null) {
                this.mLOHSObserverProxy = null;
                try {
                    this.mService.stopWatchLocalOnlyHotspot();
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }
    }

    @SystemApi
    public int getWifiApState() {
        try {
            return this.mService.getWifiApEnabledState();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public boolean isWifiApEnabled() {
        return getWifiApState() == 13;
    }

    @SystemApi
    public WifiConfiguration getWifiApConfiguration() {
        try {
            return this.mService.getWifiApConfiguration();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public boolean setWifiApConfiguration(WifiConfiguration wifiConfig) {
        try {
            return this.mService.setWifiApConfiguration(wifiConfig, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setTdlsEnabled(InetAddress remoteIPAddress, boolean enable) {
        try {
            this.mService.enableTdls(remoteIPAddress.getHostAddress(), enable);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setTdlsEnabledWithMacAddress(String remoteMacAddress, boolean enable) {
        try {
            this.mService.enableTdlsWithMacAddress(remoteMacAddress, enable);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void registerSoftApCallback(SoftApCallback callback, Handler handler) {
        if (callback != null) {
            Log.v(TAG, "registerSoftApCallback: callback=" + callback + ", handler=" + handler);
            try {
                this.mService.registerSoftApCallback(new Binder(), new SoftApCallbackProxy(handler == null ? this.mContext.getMainLooper() : handler.getLooper(), callback), callback.hashCode());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("callback cannot be null");
        }
    }

    public void unregisterSoftApCallback(SoftApCallback callback) {
        if (callback != null) {
            Log.v(TAG, "unregisterSoftApCallback: callback=" + callback);
            try {
                this.mService.unregisterSoftApCallback(callback.hashCode());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("callback cannot be null");
        }
    }

    private int putListener(Object listener) {
        int key;
        if (listener == null) {
            return 0;
        }
        synchronized (this.mListenerMapLock) {
            do {
                key = this.mListenerKey;
                this.mListenerKey = key + 1;
            } while (key == 0);
            this.mListenerMap.put(key, listener);
        }
        return key;
    }

    /* access modifiers changed from: private */
    public Object removeListener(int key) {
        Object listener;
        if (key == 0) {
            return null;
        }
        synchronized (this.mListenerMapLock) {
            listener = this.mListenerMap.get(key);
            this.mListenerMap.remove(key);
        }
        return listener;
    }

    private synchronized AsyncChannel getChannel() {
        if (this.mAsyncChannel == null) {
            Messenger messenger = getWifiServiceMessenger();
            if (messenger != null) {
                this.mAsyncChannel = new AsyncChannel();
                this.mConnected = new CountDownLatch(1);
                this.mAsyncChannel.connect(this.mContext, new ServiceHandler(this.mLooper), messenger);
                try {
                    this.mConnected.await();
                } catch (InterruptedException e) {
                    Log.e(TAG, "interrupted wait at init");
                }
            } else {
                throw new IllegalStateException("getWifiServiceMessenger() returned null!  This is invalid.");
            }
        }
        return this.mAsyncChannel;
    }

    @SystemApi
    public void connect(WifiConfiguration config, ActionListener listener) {
        Log.d(TAG, "connect, pid:" + Process.myPid() + ", tid:" + Process.myTid() + ", uid:" + Process.myUid());
        if (config != null) {
            config.callingPid = Process.myPid();
            if (this.mContext != null) {
                config.callingPackage = this.mContext.getOpPackageName();
            }
            getChannel().sendMessage(CONNECT_NETWORK, -1, putListener(listener), config);
            return;
        }
        throw new IllegalArgumentException("config cannot be null");
    }

    public void connect(int networkId, ActionListener listener) {
        Log.d(TAG, "connect, pid:" + Process.myPid() + ", tid:" + Process.myTid() + ", uid:" + Process.myUid());
        if (networkId >= 0) {
            getChannel().sendMessage(CONNECT_NETWORK, networkId, putListener(listener));
            return;
        }
        throw new IllegalArgumentException("Network id cannot be negative");
    }

    public void save(WifiConfiguration config, ActionListener listener) {
        Log.d(TAG, "save, pid:" + Process.myPid() + ", tid:" + Process.myTid() + ", uid:" + Process.myUid());
        if (config != null) {
            getChannel().sendMessage(SAVE_NETWORK, 0, putListener(listener), config);
            return;
        }
        throw new IllegalArgumentException("config cannot be null");
    }

    public void forget(int netId, ActionListener listener) {
        Log.d(TAG, "forget, pid:" + Process.myPid() + ", tid:" + Process.myTid() + ", uid:" + Process.myUid());
        if (netId >= 0) {
            getChannel().sendMessage(FORGET_NETWORK, netId, putListener(listener));
            return;
        }
        throw new IllegalArgumentException("Network id cannot be negative");
    }

    public void disable(int netId, ActionListener listener) {
        Log.d(TAG, "disable, pid:" + Process.myPid() + ", tid:" + Process.myTid() + ", uid:" + Process.myUid());
        if (HiSiWifiComm.hisiWifiEnabled()) {
            if (netId < -1) {
                throw new IllegalArgumentException("Network id cannot be negative");
            }
        } else if (netId < 0) {
            throw new IllegalArgumentException("Network id cannot be negative");
        }
        getChannel().sendMessage(DISABLE_NETWORK, netId, putListener(listener));
    }

    public void disableEphemeralNetwork(String SSID) {
        if (SSID != null) {
            try {
                this.mService.disableEphemeralNetwork(SSID, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("SSID cannot be null");
        }
    }

    public void startWps(WpsInfo config, WpsCallback listener) {
        if (HWFLOW) {
            Log.i(TAG, "startWps, pid:" + Process.myPid() + ", tid:" + Process.myTid() + ", uid:" + Process.myUid());
        }
        if (config != null) {
            getChannel().sendMessage(START_WPS, 0, putListener(listener), config);
            return;
        }
        throw new IllegalArgumentException("config cannot be null");
    }

    public void cancelWps(WpsCallback listener) {
        if (HWFLOW) {
            Log.i(TAG, "cancelWps, pid:" + Process.myPid() + ", tid:" + Process.myTid() + ", uid:" + Process.myUid());
        }
        getChannel().sendMessage(CANCEL_WPS, 0, putListener(listener));
    }

    public Messenger getWifiServiceMessenger() {
        try {
            return this.mService.getWifiServiceMessenger(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public WifiLock createWifiLock(int lockType, String tag) {
        return new WifiLock(lockType, tag);
    }

    public WifiLock createWifiLock(String tag) {
        return new WifiLock(1, tag);
    }

    public MulticastLock createMulticastLock(String tag) {
        return new MulticastLock(tag);
    }

    public boolean isMulticastEnabled() {
        try {
            return this.mService.isMulticastEnabled();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean initializeMulticastFiltering() {
        try {
            this.mService.initializeMulticastFiltering();
            return true;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.mAsyncChannel != null) {
                this.mAsyncChannel.disconnect();
            }
        } finally {
            super.finalize();
        }
    }

    public void enableVerboseLogging(int verbose) {
        try {
            this.mService.enableVerboseLogging(verbose);
        } catch (Exception e) {
            Log.e(TAG, "enableVerboseLogging " + e.toString());
        }
    }

    public int getVerboseLoggingLevel() {
        try {
            return this.mService.getVerboseLoggingLevel();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void factoryReset() {
        try {
            this.mService.factoryReset(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Network getCurrentNetwork() {
        try {
            return this.mService.getCurrentNetwork();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setEnableAutoJoinWhenAssociated(boolean enabled) {
        return false;
    }

    public boolean getEnableAutoJoinWhenAssociated() {
        return false;
    }

    public void enableWifiConnectivityManager(boolean enabled) {
        try {
            this.mService.enableWifiConnectivityManager(enabled);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public byte[] retrieveBackupData() {
        try {
            return this.mService.retrieveBackupData();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void restoreBackupData(byte[] data) {
        try {
            this.mService.restoreBackupData(data);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void restoreSupplicantBackupData(byte[] supplicantData, byte[] ipConfigData) {
        try {
            this.mService.restoreSupplicantBackupData(supplicantData, ipConfigData);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void startSubscriptionProvisioning(OsuProvider provider, ProvisioningCallback callback, Handler handler) {
        try {
            this.mService.startSubscriptionProvisioning(provider, new ProvisioningCallbackProxy(handler == null ? Looper.getMainLooper() : handler.getLooper(), callback));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public PPPOEInfo getPPPOEInfo() {
        return HwFrameworkFactory.getHwInnerWifiManager().getPPPOEInfo();
    }

    public void startPPPOE(PPPOEConfig config) {
        HwFrameworkFactory.getHwInnerWifiManager().startPPPOE(config);
    }

    public void stopPPPOE() {
        HwFrameworkFactory.getHwInnerWifiManager().stopPPPOE();
    }

    public boolean registerVoWifiSignalDetectInterrupt(VoWifiSignalDetectInterruptCallback callback) {
        this.mVoWifiCallback = callback;
        return true;
    }

    public boolean unregisterVoWifiSignalDetectInterrupt() {
        this.mVoWifiCallback = null;
        return true;
    }

    public boolean setVoWifiDetectMode(WifiDetectConfInfo info) {
        boolean ret = HwFrameworkFactory.getHwInnerWifiManager().setVoWifiDetectMode(info);
        if (info.mWifiDetectMode != 0) {
            startVoWifiDetect();
        }
        return ret;
    }

    public void startVoWifiDetect() {
        Log.i(TAG, "startVoWifiDetect");
        getChannel().sendMessage(START_VOWIFI_DETECT);
    }
}
