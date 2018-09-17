package android.net;

import android.Manifest.permission;
import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.INetworkActivityListener;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.ITelephony.Stub;
import com.android.internal.util.MessageUtils;
import com.android.internal.util.Preconditions;
import huawei.cust.HwCustUtils;
import java.net.InetAddress;
import java.util.HashMap;
import libcore.net.event.NetworkEventDispatcher;

public class ConnectivityManager {
    @Deprecated
    public static final String ACTION_BACKGROUND_DATA_SETTING_CHANGED = "android.net.conn.BACKGROUND_DATA_SETTING_CHANGED";
    public static final String ACTION_BTT_NETWORK_CONNECTION_CHANGED = "android.intent.action.BlueToothTethering_NETWORK_CONNECTION_CHANGED";
    public static final String ACTION_CAPTIVE_PORTAL_SIGN_IN = "android.net.conn.CAPTIVE_PORTAL";
    public static final String ACTION_CAPTIVE_PORTAL_TEST_COMPLETED = "android.net.conn.CAPTIVE_PORTAL_TEST_COMPLETED";
    public static final String ACTION_DATA_ACTIVITY_CHANGE = "android.net.conn.DATA_ACTIVITY_CHANGE";
    public static final String ACTION_LTEDATA_COMPLETED_ACTION = "android.net.wifi.LTEDATA_COMPLETED_ACTION";
    public static final String ACTION_PROMPT_LOST_VALIDATION = "android.net.conn.PROMPT_LOST_VALIDATION";
    public static final String ACTION_PROMPT_UNVALIDATED = "android.net.conn.PROMPT_UNVALIDATED";
    public static final String ACTION_RESTRICT_BACKGROUND_CHANGED = "android.net.conn.RESTRICT_BACKGROUND_CHANGED";
    public static final String ACTION_TETHER_STATE_CHANGED = "android.net.conn.TETHER_STATE_CHANGED";
    private static final int BASE = 524288;
    public static final int CALLBACK_AVAILABLE = 524290;
    public static final int CALLBACK_CAP_CHANGED = 524294;
    public static final int CALLBACK_EXIT = 524297;
    public static final int CALLBACK_IP_CHANGED = 524295;
    public static final int CALLBACK_LOSING = 524291;
    public static final int CALLBACK_LOST = 524292;
    public static final int CALLBACK_PRECHECK = 524289;
    public static final int CALLBACK_RELEASED = 524296;
    public static final int CALLBACK_RESUMED = 524300;
    public static final int CALLBACK_SUSPENDED = 524299;
    public static final int CALLBACK_UNAVAIL = 524293;
    public static final String CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    public static final String CONNECTIVITY_ACTION_SUPL = "android.net.conn.CONNECTIVITY_CHANGE_SUPL";
    @Deprecated
    public static final int DEFAULT_NETWORK_PREFERENCE = 1;
    private static final int EXPIRE_LEGACY_REQUEST = 524298;
    public static final String EXTRA_ACTIVE_LOCAL_ONLY = "localOnlyArray";
    public static final String EXTRA_ACTIVE_TETHER = "tetherArray";
    public static final String EXTRA_ADD_TETHER_TYPE = "extraAddTetherType";
    public static final String EXTRA_AVAILABLE_TETHER = "availableArray";
    public static final String EXTRA_BTT_CONNECT_STATE = "btt_connect_state";
    public static final String EXTRA_CAPTIVE_PORTAL = "android.net.extra.CAPTIVE_PORTAL";
    public static final String EXTRA_CAPTIVE_PORTAL_URL = "android.net.extra.CAPTIVE_PORTAL_URL";
    public static final String EXTRA_CAPTIVE_PORTAL_USER_AGENT = "android.net.extra.CAPTIVE_PORTAL_USER_AGENT";
    public static final String EXTRA_DEVICE_TYPE = "deviceType";
    public static final String EXTRA_ERRORED_TETHER = "erroredArray";
    public static final String EXTRA_EXTRA_INFO = "extraInfo";
    public static final String EXTRA_INET_CONDITION = "inetCondition";
    public static final String EXTRA_IS_ACTIVE = "isActive";
    public static final String EXTRA_IS_CAPTIVE_PORTAL = "captivePortal";
    public static final String EXTRA_IS_FAILOVER = "isFailover";
    public static final String EXTRA_IS_LTE_MOBILE_DATA_STATUS = "lte_mobile_data_status";
    public static final String EXTRA_NETWORK = "android.net.extra.NETWORK";
    @Deprecated
    public static final String EXTRA_NETWORK_INFO = "networkInfo";
    public static final String EXTRA_NETWORK_REQUEST = "android.net.extra.NETWORK_REQUEST";
    public static final String EXTRA_NETWORK_TYPE = "networkType";
    public static final String EXTRA_NO_CONNECTIVITY = "noConnectivity";
    public static final String EXTRA_OTHER_NETWORK_INFO = "otherNetwork";
    public static final String EXTRA_PROVISION_CALLBACK = "extraProvisionCallback";
    public static final String EXTRA_REALTIME_NS = "tsNanos";
    public static final String EXTRA_REASON = "reason";
    public static final String EXTRA_REM_TETHER_TYPE = "extraRemTetherType";
    public static final String EXTRA_RUN_PROVISION = "extraRunProvision";
    public static final String EXTRA_SET_ALARM = "extraSetAlarm";
    public static final String INET_CONDITION_ACTION = "android.net.conn.INET_CONDITION_ACTION";
    private static final int LISTEN = 1;
    public static final int LTE_STATE_CONNECTED = 1;
    public static final int LTE_STATE_CONNECTTING = 0;
    public static final int LTE_STATE_DISCONNECTED = 3;
    public static final int LTE_STATE_DISCONNECTTING = 2;
    public static final int MAX_NETWORK_TYPE = 47;
    public static final int MAX_RADIO_TYPE = 17;
    public static final int MULTIPATH_PREFERENCE_HANDOVER = 1;
    public static final int MULTIPATH_PREFERENCE_PERFORMANCE = 4;
    public static final int MULTIPATH_PREFERENCE_RELIABILITY = 2;
    public static final int MULTIPATH_PREFERENCE_UNMETERED = 7;
    public static final int NETID_UNSET = 0;
    private static final int REQUEST = 2;
    public static final int REQUEST_ID_UNSET = 0;
    public static final int RESTRICT_BACKGROUND_STATUS_DISABLED = 1;
    public static final int RESTRICT_BACKGROUND_STATUS_ENABLED = 3;
    public static final int RESTRICT_BACKGROUND_STATUS_WHITELISTED = 2;
    private static final String TAG = "ConnectivityManager";
    public static final int TETHERING_BLUETOOTH = 2;
    public static final int TETHERING_INVALID = -1;
    public static final int TETHERING_P2P = 3;
    public static final int TETHERING_USB = 1;
    public static final int TETHERING_WIFI = 0;
    public static final int TETHER_ERROR_DISABLE_NAT_ERROR = 9;
    public static final int TETHER_ERROR_ENABLE_NAT_ERROR = 8;
    public static final int TETHER_ERROR_IFACE_CFG_ERROR = 10;
    public static final int TETHER_ERROR_MASTER_ERROR = 5;
    public static final int TETHER_ERROR_NO_ERROR = 0;
    public static final int TETHER_ERROR_PROVISION_FAILED = 11;
    public static final int TETHER_ERROR_SERVICE_UNAVAIL = 2;
    public static final int TETHER_ERROR_TETHER_IFACE_ERROR = 6;
    public static final int TETHER_ERROR_UNAVAIL_IFACE = 4;
    public static final int TETHER_ERROR_UNKNOWN_IFACE = 1;
    public static final int TETHER_ERROR_UNSUPPORTED = 3;
    public static final int TETHER_ERROR_UNTETHER_IFACE_ERROR = 7;
    public static final int TYPE_BLUETOOTH = 7;
    public static final int TYPE_DUMMY = 8;
    public static final int TYPE_ETHERNET = 9;
    public static final int TYPE_MOBILE = 0;
    public static final int TYPE_MOBILE_BIP0 = 38;
    public static final int TYPE_MOBILE_BIP1 = 39;
    public static final int TYPE_MOBILE_BIP2 = 40;
    public static final int TYPE_MOBILE_BIP3 = 41;
    public static final int TYPE_MOBILE_BIP4 = 42;
    public static final int TYPE_MOBILE_BIP5 = 43;
    public static final int TYPE_MOBILE_BIP6 = 44;
    public static final int TYPE_MOBILE_CBS = 12;
    public static final int TYPE_MOBILE_CMMAIL = 37;
    public static final int TYPE_MOBILE_DM = 34;
    public static final int TYPE_MOBILE_DUN = 4;
    public static final int TYPE_MOBILE_EMERGENCY = 15;
    public static final int TYPE_MOBILE_FOTA = 10;
    @Deprecated
    public static final int TYPE_MOBILE_HIPRI = 5;
    public static final int TYPE_MOBILE_IA = 14;
    public static final int TYPE_MOBILE_IMS = 11;
    public static final int TYPE_MOBILE_INTERNAL_DEFAULT = 48;
    @Deprecated
    public static final int TYPE_MOBILE_MMS = 2;
    public static final int TYPE_MOBILE_NET = 36;
    @Deprecated
    public static final int TYPE_MOBILE_SUPL = 3;
    public static final int TYPE_MOBILE_WAP = 35;
    public static final int TYPE_MOBILE_XCAP = 45;
    public static final int TYPE_NONE = -1;
    public static final int TYPE_PROXY = 16;
    public static final int TYPE_VPN = 17;
    public static final int TYPE_WIFI = 1;
    public static final int TYPE_WIFI_MMS = 46;
    public static final int TYPE_WIFI_P2P = 13;
    public static final int TYPE_WIFI_XCAP = 47;
    public static final int TYPE_WIMAX = 6;
    private static int lastLegacyRequestId = 0;
    private static CallbackHandler sCallbackHandler;
    private static final HashMap<NetworkRequest, NetworkCallback> sCallbacks = new HashMap();
    private static ConnectivityManager sInstance;
    private static HashMap<NetworkCapabilities, LegacyRequest> sLegacyRequests = new HashMap();
    private static final SparseIntArray sLegacyTypeToCapability = new SparseIntArray();
    private static final SparseIntArray sLegacyTypeToTransport = new SparseIntArray();
    private final Context mContext;
    HwCustConnectivityManager mCust = ((HwCustConnectivityManager) HwCustUtils.createObj(HwCustConnectivityManager.class, new Object[0]));
    private INetworkManagementService mNMService;
    private INetworkPolicyManager mNPManager;
    private final ArrayMap<OnNetworkActiveListener, INetworkActivityListener> mNetworkActivityListeners = new ArrayMap();
    private final IConnectivityManager mService;

    private class CallbackHandler extends Handler {
        private static final boolean DBG = false;
        private static final String TAG = "ConnectivityManager.CallbackHandler";

        CallbackHandler(Looper looper) {
            super(looper);
        }

        CallbackHandler(ConnectivityManager this$0, Handler handler) {
            this(handler.getLooper());
        }

        public void handleMessage(Message message) {
            NetworkRequest request = (NetworkRequest) getObject(message, NetworkRequest.class);
            Network network = (Network) getObject(message, Network.class);
            NetworkCallback callback;
            switch (message.what) {
                case ConnectivityManager.CALLBACK_PRECHECK /*524289*/:
                    callback = getCallback(request, "PRECHECK");
                    if (callback != null) {
                        callback.onPreCheck(network);
                        return;
                    }
                    return;
                case ConnectivityManager.CALLBACK_AVAILABLE /*524290*/:
                    callback = getCallback(request, "AVAILABLE");
                    if (callback != null) {
                        callback.onAvailable(network);
                        return;
                    }
                    return;
                case ConnectivityManager.CALLBACK_LOSING /*524291*/:
                    callback = getCallback(request, "LOSING");
                    if (callback != null) {
                        callback.onLosing(network, message.arg1);
                        return;
                    }
                    return;
                case ConnectivityManager.CALLBACK_LOST /*524292*/:
                    callback = getCallback(request, "LOST");
                    if (callback != null) {
                        callback.onLost(network);
                        return;
                    }
                    return;
                case ConnectivityManager.CALLBACK_UNAVAIL /*524293*/:
                    callback = getCallback(request, "UNAVAIL");
                    if (callback != null) {
                        callback.onUnavailable();
                        return;
                    }
                    return;
                case ConnectivityManager.CALLBACK_CAP_CHANGED /*524294*/:
                    callback = getCallback(request, "CAP_CHANGED");
                    if (callback != null) {
                        callback.onCapabilitiesChanged(network, (NetworkCapabilities) getObject(message, NetworkCapabilities.class));
                        return;
                    }
                    return;
                case ConnectivityManager.CALLBACK_IP_CHANGED /*524295*/:
                    callback = getCallback(request, "IP_CHANGED");
                    if (callback != null) {
                        callback.onLinkPropertiesChanged(network, (LinkProperties) getObject(message, LinkProperties.class));
                        return;
                    }
                    return;
                case ConnectivityManager.CALLBACK_RELEASED /*524296*/:
                    synchronized (ConnectivityManager.sCallbacks) {
                        callback = (NetworkCallback) ConnectivityManager.sCallbacks.remove(request);
                    }
                    if (callback == null) {
                        Log.e(TAG, "callback not found for RELEASED message");
                        return;
                    }
                    return;
                case ConnectivityManager.EXPIRE_LEGACY_REQUEST /*524298*/:
                    ConnectivityManager.this.expireRequest((NetworkCapabilities) message.obj, message.arg1);
                    return;
                case ConnectivityManager.CALLBACK_SUSPENDED /*524299*/:
                    callback = getCallback(request, "SUSPENDED");
                    if (callback != null) {
                        callback.onNetworkSuspended(network);
                        return;
                    }
                    return;
                case ConnectivityManager.CALLBACK_RESUMED /*524300*/:
                    callback = getCallback(request, "RESUMED");
                    if (callback != null) {
                        callback.onNetworkResumed(network);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }

        private <T> T getObject(Message msg, Class<T> c) {
            return msg.getData().getParcelable(c.getSimpleName());
        }

        private NetworkCallback getCallback(NetworkRequest req, String name) {
            NetworkCallback callback;
            synchronized (ConnectivityManager.sCallbacks) {
                callback = (NetworkCallback) ConnectivityManager.sCallbacks.get(req);
            }
            if (callback == null) {
                Log.e(TAG, "callback not found for " + name + " message");
            }
            return callback;
        }
    }

    public static class NetworkCallback {
        private NetworkRequest networkRequest;

        public void onPreCheck(Network network) {
        }

        public void onAvailable(Network network) {
        }

        public void onLosing(Network network, int maxMsToLive) {
        }

        public void onLost(Network network) {
        }

        public void onUnavailable() {
        }

        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
        }

        public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
        }

        public void onNetworkSuspended(Network network) {
        }

        public void onNetworkResumed(Network network) {
        }
    }

    private static class LegacyRequest {
        Network currentNetwork;
        int delay;
        int expireSequenceNumber;
        NetworkCallback networkCallback;
        NetworkCapabilities networkCapabilities;
        NetworkRequest networkRequest;

        /* synthetic */ LegacyRequest(LegacyRequest -this0) {
            this();
        }

        private LegacyRequest() {
            this.delay = -1;
            this.networkCallback = new NetworkCallback() {
                public void onAvailable(Network network) {
                    LegacyRequest.this.currentNetwork = network;
                    Log.d(ConnectivityManager.TAG, "startUsingNetworkFeature got Network:" + network);
                    ConnectivityManager.setProcessDefaultNetworkForHostResolution(network);
                }

                public void onLost(Network network) {
                    if (network.equals(LegacyRequest.this.currentNetwork)) {
                        LegacyRequest.this.clearDnsBinding();
                    }
                    Log.d(ConnectivityManager.TAG, "startUsingNetworkFeature lost Network:" + network);
                }
            };
        }

        private void clearDnsBinding() {
            if (this.currentNetwork != null) {
                this.currentNetwork = null;
                ConnectivityManager.setProcessDefaultNetworkForHostResolution(null);
            }
        }
    }

    private static class NoPreloadHolder {
        public static final SparseArray<String> sMagicDecoderRing = MessageUtils.findMessageNames(new Class[]{ConnectivityManager.class}, new String[]{"CALLBACK_"});

        private NoPreloadHolder() {
        }
    }

    public interface OnNetworkActiveListener {
        void onNetworkActive();
    }

    public static abstract class OnStartTetheringCallback {
        public void onTetheringStarted() {
        }

        public void onTetheringFailed() {
        }
    }

    public class PacketKeepalive {
        public static final int BINDER_DIED = -10;
        public static final int ERROR_HARDWARE_ERROR = -31;
        public static final int ERROR_HARDWARE_UNSUPPORTED = -30;
        public static final int ERROR_INVALID_INTERVAL = -24;
        public static final int ERROR_INVALID_IP_ADDRESS = -21;
        public static final int ERROR_INVALID_LENGTH = -23;
        public static final int ERROR_INVALID_NETWORK = -20;
        public static final int ERROR_INVALID_PORT = -22;
        public static final int NATT_PORT = 4500;
        public static final int NO_KEEPALIVE = -1;
        public static final int SUCCESS = 0;
        private static final String TAG = "PacketKeepalive";
        private final PacketKeepaliveCallback mCallback;
        private final Looper mLooper;
        private final Messenger mMessenger;
        private final Network mNetwork;
        private volatile Integer mSlot;

        /* synthetic */ PacketKeepalive(ConnectivityManager this$0, Network network, PacketKeepaliveCallback callback, PacketKeepalive -this3) {
            this(network, callback);
        }

        void stopLooper() {
            this.mLooper.quit();
        }

        public void stop() {
            try {
                ConnectivityManager.this.mService.stopKeepalive(this.mNetwork, this.mSlot.intValue());
            } catch (RemoteException e) {
                Log.e(TAG, "Error stopping packet keepalive: ", e);
                stopLooper();
            }
        }

        private PacketKeepalive(Network network, PacketKeepaliveCallback callback) {
            Preconditions.checkNotNull(network, "network cannot be null");
            Preconditions.checkNotNull(callback, "callback cannot be null");
            this.mNetwork = network;
            this.mCallback = callback;
            HandlerThread thread = new HandlerThread(TAG);
            thread.start();
            this.mLooper = thread.getLooper();
            this.mMessenger = new Messenger(new Handler(this.mLooper) {
                public void handleMessage(Message message) {
                    switch (message.what) {
                        case NetworkAgent.EVENT_PACKET_KEEPALIVE /*528397*/:
                            int error = message.arg2;
                            if (error == 0) {
                                try {
                                    if (PacketKeepalive.this.mSlot == null) {
                                        PacketKeepalive.this.mSlot = Integer.valueOf(message.arg1);
                                        PacketKeepalive.this.mCallback.onStarted();
                                        return;
                                    }
                                    PacketKeepalive.this.mSlot = null;
                                    PacketKeepalive.this.stopLooper();
                                    PacketKeepalive.this.mCallback.onStopped();
                                    return;
                                } catch (Exception e) {
                                    Log.e(PacketKeepalive.TAG, "Exception in keepalive callback(" + error + ")", e);
                                    return;
                                }
                            }
                            PacketKeepalive.this.stopLooper();
                            PacketKeepalive.this.mCallback.onError(error);
                            return;
                        default:
                            Log.e(PacketKeepalive.TAG, "Unhandled message " + Integer.toHexString(message.what));
                            return;
                    }
                }
            });
        }
    }

    public static class PacketKeepaliveCallback {
        public void onStarted() {
        }

        public void onStopped() {
        }

        public void onError(int error) {
        }
    }

    static {
        sLegacyTypeToTransport.put(0, 0);
        sLegacyTypeToTransport.put(12, 0);
        sLegacyTypeToTransport.put(4, 0);
        sLegacyTypeToTransport.put(10, 0);
        sLegacyTypeToTransport.put(5, 0);
        sLegacyTypeToTransport.put(11, 0);
        sLegacyTypeToTransport.put(2, 0);
        sLegacyTypeToTransport.put(3, 0);
        sLegacyTypeToTransport.put(38, 0);
        sLegacyTypeToTransport.put(39, 0);
        sLegacyTypeToTransport.put(40, 0);
        sLegacyTypeToTransport.put(41, 0);
        sLegacyTypeToTransport.put(42, 0);
        sLegacyTypeToTransport.put(43, 0);
        sLegacyTypeToTransport.put(44, 0);
        sLegacyTypeToTransport.put(45, 0);
        sLegacyTypeToTransport.put(1, 1);
        sLegacyTypeToTransport.put(13, 1);
        sLegacyTypeToTransport.put(46, 1);
        sLegacyTypeToTransport.put(47, 1);
        sLegacyTypeToTransport.put(7, 2);
        sLegacyTypeToTransport.put(9, 3);
        sLegacyTypeToTransport.put(48, 26);
        sLegacyTypeToCapability.put(12, 5);
        sLegacyTypeToCapability.put(4, 2);
        sLegacyTypeToCapability.put(10, 3);
        sLegacyTypeToCapability.put(11, 4);
        sLegacyTypeToCapability.put(2, 0);
        sLegacyTypeToCapability.put(3, 1);
        sLegacyTypeToCapability.put(13, 6);
        sLegacyTypeToCapability.put(38, 19);
        sLegacyTypeToCapability.put(39, 20);
        sLegacyTypeToCapability.put(40, 21);
        sLegacyTypeToCapability.put(41, 22);
        sLegacyTypeToCapability.put(42, 23);
        sLegacyTypeToCapability.put(43, 24);
        sLegacyTypeToCapability.put(44, 25);
        sLegacyTypeToCapability.put(45, 9);
        sLegacyTypeToCapability.put(46, 0);
        sLegacyTypeToCapability.put(47, 9);
        sLegacyTypeToCapability.put(48, 26);
    }

    private static int getNewLastLegacyRequestId() {
        if (lastLegacyRequestId == Integer.MAX_VALUE) {
            lastLegacyRequestId = 0;
        }
        int i = lastLegacyRequestId;
        lastLegacyRequestId = i + 1;
        return i;
    }

    @Deprecated
    public static boolean isNetworkTypeValid(int networkType) {
        if (networkType >= 0 && networkType <= 17) {
            return true;
        }
        if (networkType >= 34 && networkType <= 45) {
            return true;
        }
        if ((networkType < 46 || networkType > 47) && networkType != 48) {
            return false;
        }
        return true;
    }

    public static String getNetworkTypeName(int type) {
        switch (type) {
            case 0:
                return "MOBILE";
            case 1:
                return "WIFI";
            case 2:
                return "MOBILE_MMS";
            case 3:
                return "MOBILE_SUPL";
            case 4:
                return "MOBILE_DUN";
            case 5:
                return "MOBILE_HIPRI";
            case 6:
                return "WIMAX";
            case 7:
                return "BLUETOOTH";
            case 8:
                return "DUMMY";
            case 9:
                return "ETHERNET";
            case 10:
                return "MOBILE_FOTA";
            case 11:
                return "MOBILE_IMS";
            case 12:
                return "MOBILE_CBS";
            case 13:
                return "WIFI_P2P";
            case 14:
                return "MOBILE_IA";
            case 15:
                return "MOBILE_EMERGENCY";
            case 16:
                return "PROXY";
            case 17:
                return "VPN";
            case 38:
                return "MOBILE_BIP0";
            case 39:
                return "MOBILE_BIP1";
            case 40:
                return "MOBILE_BIP2";
            case 41:
                return "MOBILE_BIP3";
            case 42:
                return "MOBILE_BIP4";
            case 43:
                return "MOBILE_BIP5";
            case 44:
                return "MOBILE_BIP6";
            case 45:
                return "MOBILE_XCAP";
            case 46:
                return "WIFI_MMS";
            case 47:
                return "WIFI_XCAP";
            case 48:
                return "MOBILE_INTERNAL_DEFAULT";
            default:
                return Integer.toString(type);
        }
    }

    public static boolean isNetworkTypeMobile(int networkType) {
        switch (networkType) {
            case 0:
            case 2:
            case 3:
            case 4:
            case 5:
            case 10:
            case 11:
            case 12:
            case 14:
            case 15:
            case 38:
            case 39:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 48:
                return true;
            default:
                return false;
        }
    }

    public static boolean isNetworkTypeWifi(int networkType) {
        switch (networkType) {
            case 1:
            case 13:
            case 46:
            case 47:
                return true;
            default:
                return false;
        }
    }

    @Deprecated
    public void setNetworkPreference(int preference) {
    }

    @Deprecated
    public int getNetworkPreference() {
        return -1;
    }

    public NetworkInfo getActiveNetworkInfo() {
        try {
            return this.mService.getActiveNetworkInfo();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Network getActiveNetwork() {
        try {
            return this.mService.getActiveNetwork();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Network getActiveNetworkForUid(int uid) {
        return getActiveNetworkForUid(uid, false);
    }

    public Network getActiveNetworkForUid(int uid, boolean ignoreBlocked) {
        try {
            return this.mService.getActiveNetworkForUid(uid, ignoreBlocked);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setAlwaysOnVpnPackageForUser(int userId, String vpnPackage, boolean lockdownEnabled) {
        try {
            return this.mService.setAlwaysOnVpnPackage(userId, vpnPackage, lockdownEnabled);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getAlwaysOnVpnPackageForUser(int userId) {
        try {
            return this.mService.getAlwaysOnVpnPackage(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public NetworkInfo getActiveNetworkInfoForUid(int uid) {
        return getActiveNetworkInfoForUid(uid, false);
    }

    public NetworkInfo getActiveNetworkInfoForUid(int uid, boolean ignoreBlocked) {
        try {
            return this.mService.getActiveNetworkInfoForUid(uid, ignoreBlocked);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public NetworkInfo getNetworkInfo(int networkType) {
        try {
            return this.mService.getNetworkInfo(networkType);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public NetworkInfo getNetworkInfo(Network network) {
        return getNetworkInfoForUid(network, Process.myUid(), false);
    }

    public NetworkInfo getNetworkInfoForUid(Network network, int uid, boolean ignoreBlocked) {
        try {
            return this.mService.getNetworkInfoForUid(network, uid, ignoreBlocked);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public NetworkInfo[] getAllNetworkInfo() {
        try {
            return this.mService.getAllNetworkInfo();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public Network getNetworkForType(int networkType) {
        try {
            return this.mService.getNetworkForType(networkType);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Network[] getAllNetworks() {
        try {
            return this.mService.getAllNetworks();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public NetworkCapabilities[] getDefaultNetworkCapabilitiesForUser(int userId) {
        try {
            return this.mService.getDefaultNetworkCapabilitiesForUser(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public LinkProperties getActiveLinkProperties() {
        try {
            return this.mService.getActiveLinkProperties();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public LinkProperties getLinkProperties(int networkType) {
        try {
            return this.mService.getLinkPropertiesForType(networkType);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public LinkProperties getLinkProperties(Network network) {
        try {
            return this.mService.getLinkProperties(network);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public NetworkCapabilities getNetworkCapabilities(Network network) {
        try {
            return this.mService.getNetworkCapabilities(network);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getCaptivePortalServerUrl() {
        try {
            return this.mService.getCaptivePortalServerUrl();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /* JADX WARNING: Missing block: B:24:0x0086, code:
            if (r2 == null) goto L_0x00a6;
     */
    /* JADX WARNING: Missing block: B:25:0x0088, code:
            android.util.Log.d(TAG, "starting startUsingNetworkFeature for request " + r2);
     */
    /* JADX WARNING: Missing block: B:26:0x00a2, code:
            return 1;
     */
    /* JADX WARNING: Missing block: B:30:0x00a6, code:
            android.util.Log.d(TAG, " request Failed");
     */
    /* JADX WARNING: Missing block: B:31:0x00af, code:
            return 3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @Deprecated
    public int startUsingNetworkFeature(int networkType, String feature) {
        checkLegacyRoutingApiAccess();
        NetworkCapabilities netCap = networkCapabilitiesForFeature(networkType, feature);
        if (netCap == null) {
            Log.d(TAG, "Can't satisfy startUsingNetworkFeature for " + networkType + ", " + feature);
            return 3;
        } else if (this.mCust != null && this.mCust.enforceStartUsingNetworkFeaturePermissionFail(this.mContext, legacyTypeForNetworkCapabilities(netCap))) {
            return 3;
        } else {
            HwFrameworkFactory.getHwInnerConnectivityManager().checkHwFeature(feature, netCap, networkType);
            synchronized (sLegacyRequests) {
                LegacyRequest l = (LegacyRequest) sLegacyRequests.get(netCap);
                if (l != null) {
                    Log.d(TAG, "renewing startUsingNetworkFeature request " + l.networkRequest);
                    renewRequestLocked(l);
                    if (l.currentNetwork != null) {
                        return 0;
                    }
                    return 1;
                }
                NetworkRequest request = requestNetworkForFeatureLocked(netCap);
            }
        }
    }

    @Deprecated
    public int stopUsingNetworkFeature(int networkType, String feature) {
        checkLegacyRoutingApiAccess();
        NetworkCapabilities netCap = networkCapabilitiesForFeature(networkType, feature);
        if (netCap == null) {
            Log.d(TAG, "Can't satisfy stopUsingNetworkFeature for " + networkType + ", " + feature);
            return -1;
        }
        HwFrameworkFactory.getHwInnerConnectivityManager().checkHwFeature(feature, netCap, networkType);
        if (removeRequestForFeature(netCap)) {
            Log.d(TAG, "stopUsingNetworkFeature for " + networkType + ", " + feature);
        }
        return 1;
    }

    private NetworkCapabilities networkCapabilitiesForFeature(int networkType, String feature) {
        if (networkType == 0) {
            String[] result = HwFrameworkFactory.getHwInnerConnectivityManager().getFeature(feature);
            if (!(result[0] == null || (ProxyInfo.LOCAL_EXCL_LIST.equals(result[0]) ^ 1) == 0)) {
                feature = result[0];
            }
            if (feature.equals("enableCBS")) {
                return networkCapabilitiesForType(12);
            }
            if (feature.equals("enableDUN") || feature.equals("enableDUNAlways")) {
                return networkCapabilitiesForType(4);
            }
            if (feature.equals("enableFOTA")) {
                return networkCapabilitiesForType(10);
            }
            if (feature.equals("enableHIPRI")) {
                return networkCapabilitiesForType(5);
            }
            if (feature.equals("enableIMS")) {
                return networkCapabilitiesForType(11);
            }
            if (feature.equals("enableMMS")) {
                return networkCapabilitiesForType(2);
            }
            if (feature.equals("enableSUPL")) {
                return networkCapabilitiesForType(3);
            }
            if (feature.equals("enableBIP0")) {
                return networkCapabilitiesForType(38);
            }
            if (feature.equals("enableBIP1")) {
                return networkCapabilitiesForType(39);
            }
            if (feature.equals("enableBIP2")) {
                return networkCapabilitiesForType(40);
            }
            if (feature.equals("enableBIP3")) {
                return networkCapabilitiesForType(41);
            }
            if (feature.equals("enableBIP4")) {
                return networkCapabilitiesForType(42);
            }
            if (feature.equals("enableBIP5")) {
                return networkCapabilitiesForType(43);
            }
            if (feature.equals("enableBIP6")) {
                return networkCapabilitiesForType(44);
            }
            if (feature.equals("enableXCAP")) {
                return networkCapabilitiesForType(45);
            }
            if (feature.equals("enableInternalDefault")) {
                return networkCapabilitiesForType(48);
            }
            if (feature.equals("enableEmergency") && this.mCust != null) {
                return this.mCust.networkCapabilitiesForEimsType(15);
            }
        } else if (networkType == 1 && "p2p".equals(feature)) {
            return networkCapabilitiesForType(13);
        }
        return null;
    }

    private int inferLegacyTypeForNetworkCapabilities(NetworkCapabilities netCap) {
        if (netCap == null || !netCap.hasTransport(0)) {
            return -1;
        }
        if (!netCap.hasCapability(1)) {
            int canHandleEimsNetworkCapabilities;
            if (this.mCust != null) {
                canHandleEimsNetworkCapabilities = this.mCust.canHandleEimsNetworkCapabilities(netCap);
            } else {
                canHandleEimsNetworkCapabilities = 0;
            }
            if ((canHandleEimsNetworkCapabilities ^ 1) != 0) {
                return -1;
            }
        }
        String type = null;
        int result = -1;
        if (netCap.hasCapability(5)) {
            type = "enableCBS";
            result = 12;
        } else if (netCap.hasCapability(4)) {
            type = "enableIMS";
            result = 11;
        } else if (netCap.hasCapability(3)) {
            type = "enableFOTA";
            result = 10;
        } else if (netCap.hasCapability(2)) {
            type = "enableDUN";
            result = 4;
        } else if (netCap.hasCapability(1)) {
            type = "enableSUPL";
            result = 3;
        } else if (netCap.hasCapability(12)) {
            type = "enableHIPRI";
            result = 5;
        } else if (netCap.hasCapability(19)) {
            type = "enableBIP0";
            result = 38;
        } else if (netCap.hasCapability(20)) {
            type = "enableBIP1";
            result = 39;
        } else if (netCap.hasCapability(21)) {
            type = "enableBIP2";
            result = 40;
        } else if (netCap.hasCapability(22)) {
            type = "enableBIP3";
            result = 41;
        } else if (netCap.hasCapability(23)) {
            type = "enableBIP4";
            result = 42;
        } else if (netCap.hasCapability(24)) {
            type = "enableBIP5";
            result = 43;
        } else if (netCap.hasCapability(25)) {
            type = "enableBIP6";
            result = 44;
        } else if (netCap.hasCapability(9)) {
            type = "enableXCAP";
            result = 45;
        } else if (netCap.hasCapability(26)) {
            type = "enableInternalDefault";
            result = 48;
        } else if (this.mCust != null && this.mCust.canHandleEimsNetworkCapabilities(netCap)) {
            type = "enableEmergency";
            result = 15;
            sLegacyTypeToTransport.put(15, 0);
            sLegacyTypeToCapability.put(15, 10);
        }
        if (type != null) {
            NetworkCapabilities testCap = networkCapabilitiesForFeature(0, type);
            if (testCap.equalsNetCapabilities(netCap) && testCap.equalsTransportTypes(netCap)) {
                return result;
            }
            return -1;
        }
        return -1;
    }

    private int legacyTypeForNetworkCapabilities(NetworkCapabilities netCap) {
        if (netCap == null) {
            return -1;
        }
        if (netCap.hasCapability(5)) {
            return 12;
        }
        if (netCap.hasCapability(4)) {
            return 11;
        }
        if (netCap.hasCapability(3)) {
            return 10;
        }
        if (netCap.hasCapability(2)) {
            return 4;
        }
        if (netCap.hasCapability(1)) {
            return 3;
        }
        if (netCap.hasCapability(0)) {
            return 2;
        }
        if (netCap.hasCapability(12)) {
            return 5;
        }
        if (netCap.hasCapability(6)) {
            return 13;
        }
        if (netCap.hasCapability(19)) {
            return 38;
        }
        if (netCap.hasCapability(20)) {
            return 39;
        }
        if (netCap.hasCapability(21)) {
            return 40;
        }
        if (netCap.hasCapability(22)) {
            return 41;
        }
        if (netCap.hasCapability(23)) {
            return 42;
        }
        if (netCap.hasCapability(24)) {
            return 43;
        }
        if (netCap.hasCapability(25)) {
            return 44;
        }
        if (netCap.hasCapability(9)) {
            if (netCap.hasTransport(1)) {
                return 47;
            }
            if (netCap.hasTransport(0)) {
                return 45;
            }
        }
        if (netCap.hasCapability(26)) {
            return 48;
        }
        if (this.mCust == null || !this.mCust.canHandleEimsNetworkCapabilities(netCap)) {
            return -1;
        }
        return 15;
    }

    private NetworkRequest findRequestForFeature(NetworkCapabilities netCap) {
        synchronized (sLegacyRequests) {
            LegacyRequest l = (LegacyRequest) sLegacyRequests.get(netCap);
            if (l != null) {
                NetworkRequest networkRequest = l.networkRequest;
                return networkRequest;
            }
            return null;
        }
    }

    private void renewRequestLocked(LegacyRequest l) {
        l.expireSequenceNumber = getNewLastLegacyRequestId();
        Log.d(TAG, "renewing request to seqNum " + l.expireSequenceNumber);
        sendExpireMsgForFeature(l.networkCapabilities, l.expireSequenceNumber, l.delay);
    }

    /* JADX WARNING: Missing block: B:12:0x001a, code:
            android.util.Log.d(TAG, "expireRequest with " + r1 + ", " + r7);
     */
    /* JADX WARNING: Missing block: B:13:0x003f, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void expireRequest(NetworkCapabilities netCap, int sequenceNum) {
        synchronized (sLegacyRequests) {
            LegacyRequest l = (LegacyRequest) sLegacyRequests.get(netCap);
            if (l == null) {
                return;
            }
            int ourSeqNum = l.expireSequenceNumber;
            if (l.expireSequenceNumber == sequenceNum) {
                removeRequestForFeature(netCap);
            }
        }
    }

    private NetworkRequest requestNetworkForFeatureLocked(NetworkCapabilities netCap) {
        int type = legacyTypeForNetworkCapabilities(netCap);
        try {
            int delay = this.mService.getRestoreDefaultNetworkDelay(type);
            LegacyRequest l = new LegacyRequest();
            l.networkCapabilities = netCap;
            l.delay = delay;
            l.expireSequenceNumber = getNewLastLegacyRequestId();
            l.networkRequest = sendRequestForNetwork(netCap, l.networkCallback, 0, 2, type, getDefaultHandler());
            if (l.networkRequest == null) {
                return null;
            }
            sLegacyRequests.put(netCap, l);
            sendExpireMsgForFeature(netCap, l.expireSequenceNumber, delay);
            return l.networkRequest;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private void sendExpireMsgForFeature(NetworkCapabilities netCap, int seqNum, int delay) {
        if (delay >= 0) {
            Log.d(TAG, "sending expire msg with seqNum " + seqNum + " and delay " + delay);
            CallbackHandler handler = getDefaultHandler();
            handler.sendMessageDelayed(handler.obtainMessage(EXPIRE_LEGACY_REQUEST, seqNum, 0, netCap), (long) delay);
        }
    }

    private boolean removeRequestForFeature(NetworkCapabilities netCap) {
        LegacyRequest l;
        synchronized (sLegacyRequests) {
            l = (LegacyRequest) sLegacyRequests.remove(netCap);
        }
        if (l == null) {
            return false;
        }
        unregisterNetworkCallback(l.networkCallback);
        l.clearDnsBinding();
        return true;
    }

    public static NetworkCapabilities networkCapabilitiesForType(int type) {
        NetworkCapabilities nc = new NetworkCapabilities();
        int transport = sLegacyTypeToTransport.get(type, -1);
        if (transport == -1) {
            throw new IllegalArgumentException("unknown legacy type: " + type);
        }
        nc.addTransportType(transport);
        nc.addCapability(sLegacyTypeToCapability.get(type, 12));
        nc.maybeMarkCapabilitiesRestricted();
        return nc;
    }

    public PacketKeepalive startNattKeepalive(Network network, int intervalSeconds, PacketKeepaliveCallback callback, InetAddress srcAddr, int srcPort, InetAddress dstAddr) {
        PacketKeepalive k = new PacketKeepalive(this, network, callback, null);
        try {
            this.mService.startNattKeepalive(network, intervalSeconds, k.mMessenger, new Binder(), srcAddr.getHostAddress(), srcPort, dstAddr.getHostAddress());
            return k;
        } catch (RemoteException e) {
            Log.e(TAG, "Error starting packet keepalive: ", e);
            k.stopLooper();
            return null;
        }
    }

    @Deprecated
    public boolean requestRouteToHost(int networkType, int hostAddress) {
        return requestRouteToHostAddress(networkType, NetworkUtils.intToInetAddress(hostAddress));
    }

    @Deprecated
    public boolean requestRouteToHostAddress(int networkType, InetAddress hostAddress) {
        checkLegacyRoutingApiAccess();
        try {
            return this.mService.requestRouteToHostAddress(networkType, hostAddress.getAddress());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public boolean getBackgroundDataSetting() {
        return true;
    }

    @Deprecated
    public void setBackgroundDataSetting(boolean allowBackgroundData) {
    }

    public NetworkQuotaInfo getActiveNetworkQuotaInfo() {
        try {
            return this.mService.getActiveNetworkQuotaInfo();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public boolean getMobileDataEnabled() {
        IBinder b = ServiceManager.getService(Context.TELEPHONY_SERVICE);
        if (b != null) {
            try {
                ITelephony it = Stub.asInterface(b);
                int subId = SubscriptionManager.getDefaultDataSubscriptionId();
                Log.d(TAG, "getMobileDataEnabled()+ subId=" + subId);
                boolean retVal = it.getDataEnabled(subId);
                Log.d(TAG, "getMobileDataEnabled()- subId=" + subId + " retVal=" + retVal);
                return retVal;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        Log.d(TAG, "getMobileDataEnabled()- remote exception retVal=false");
        return false;
    }

    private INetworkManagementService getNetworkManagementService() {
        synchronized (this) {
            INetworkManagementService iNetworkManagementService;
            if (this.mNMService != null) {
                iNetworkManagementService = this.mNMService;
                return iNetworkManagementService;
            }
            this.mNMService = INetworkManagementService.Stub.asInterface(ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
            iNetworkManagementService = this.mNMService;
            return iNetworkManagementService;
        }
    }

    public void addDefaultNetworkActiveListener(final OnNetworkActiveListener l) {
        INetworkActivityListener rl = new INetworkActivityListener.Stub() {
            public void onNetworkActive() throws RemoteException {
                l.onNetworkActive();
            }
        };
        try {
            getNetworkManagementService().registerNetworkActivityListener(rl);
            this.mNetworkActivityListeners.put(l, rl);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void removeDefaultNetworkActiveListener(OnNetworkActiveListener l) {
        INetworkActivityListener rl = (INetworkActivityListener) this.mNetworkActivityListeners.get(l);
        if (rl == null) {
            throw new IllegalArgumentException("Listener not registered: " + l);
        }
        try {
            getNetworkManagementService().unregisterNetworkActivityListener(rl);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isDefaultNetworkActive() {
        try {
            return getNetworkManagementService().isNetworkActive();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ConnectivityManager(Context context, IConnectivityManager service) {
        this.mContext = (Context) Preconditions.checkNotNull(context, "missing context");
        this.mService = (IConnectivityManager) Preconditions.checkNotNull(service, "missing IConnectivityManager");
        sInstance = this;
    }

    public static ConnectivityManager from(Context context) {
        return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public static final boolean checkChangePermission(Context context) {
        int uid = Binder.getCallingUid();
        return Settings.checkAndNoteChangeNetworkStateOperation(context, uid, Settings.getPackageNameForUid(context, uid), false);
    }

    public static final void enforceChangePermission(Context context) {
        int uid = Binder.getCallingUid();
        Settings.checkAndNoteChangeNetworkStateOperation(context, uid, Settings.getPackageNameForUid(context, uid), true);
    }

    public static final void enforceTetherChangePermission(Context context) {
        if (context.getResources().getStringArray(17236017).length == 2) {
            context.enforceCallingOrSelfPermission(permission.TETHER_PRIVILEGED, "ConnectivityService");
            return;
        }
        int uid = Binder.getCallingUid();
        Settings.checkAndNoteWriteSettingsOperation(context, uid, Settings.getPackageNameForUid(context, uid), true);
    }

    @Deprecated
    static ConnectivityManager getInstanceOrNull() {
        return sInstance;
    }

    @Deprecated
    private static ConnectivityManager getInstance() {
        if (getInstanceOrNull() != null) {
            return getInstanceOrNull();
        }
        throw new IllegalStateException("No ConnectivityManager yet constructed");
    }

    public String[] getTetherableIfaces() {
        try {
            return this.mService.getTetherableIfaces();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String[] getTetheredIfaces() {
        try {
            return this.mService.getTetheredIfaces();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String[] getTetheringErroredIfaces() {
        try {
            return this.mService.getTetheringErroredIfaces();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String[] getTetheredDhcpRanges() {
        try {
            return this.mService.getTetheredDhcpRanges();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int tether(String iface) {
        try {
            return this.mService.tether(iface);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int untether(String iface) {
        try {
            return this.mService.untether(iface);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isTetheringSupported() {
        try {
            return this.mService.isTetheringSupported();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void startTethering(int type, boolean showProvisioningUi, OnStartTetheringCallback callback) {
        startTethering(type, showProvisioningUi, callback, null);
    }

    public void startTethering(int type, boolean showProvisioningUi, final OnStartTetheringCallback callback, Handler handler) {
        Preconditions.checkNotNull(callback, "OnStartTetheringCallback cannot be null.");
        ResultReceiver wrappedCallback = new ResultReceiver(handler) {
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == 0) {
                    callback.onTetheringStarted();
                } else {
                    callback.onTetheringFailed();
                }
            }
        };
        try {
            this.mService.startTethering(type, wrappedCallback, showProvisioningUi);
        } catch (RemoteException e) {
            Log.e(TAG, "Exception trying to start tethering.", e);
            wrappedCallback.send(2, null);
        }
    }

    public void stopTethering(int type) {
        try {
            this.mService.stopTethering(type);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String[] getTetherableUsbRegexs() {
        try {
            return this.mService.getTetherableUsbRegexs();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String[] getTetherableWifiRegexs() {
        try {
            return this.mService.getTetherableWifiRegexs();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String[] getTetherableBluetoothRegexs() {
        try {
            return this.mService.getTetherableBluetoothRegexs();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int setUsbTethering(boolean enable) {
        try {
            return this.mService.setUsbTethering(enable);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getLastTetherError(String iface) {
        try {
            return this.mService.getLastTetherError(iface);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void reportInetCondition(int networkType, int percentage) {
        try {
            this.mService.reportInetCondition(networkType, percentage);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void reportBadNetwork(Network network) {
        try {
            this.mService.reportNetworkConnectivity(network, true);
            this.mService.reportNetworkConnectivity(network, false);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void reportNetworkConnectivity(Network network, boolean hasConnectivity) {
        try {
            this.mService.reportNetworkConnectivity(network, hasConnectivity);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setGlobalProxy(ProxyInfo p) {
        try {
            this.mService.setGlobalProxy(p);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ProxyInfo getGlobalProxy() {
        try {
            return this.mService.getGlobalProxy();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ProxyInfo getProxyForNetwork(Network network) {
        try {
            return this.mService.getProxyForNetwork(network);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ProxyInfo getDefaultProxy() {
        return getProxyForNetwork(getBoundNetworkForProcess());
    }

    public boolean isNetworkSupported(int networkType) {
        try {
            return this.mService.isNetworkSupported(networkType);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isActiveNetworkMetered() {
        try {
            return this.mService.isActiveNetworkMetered();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean updateLockdownVpn() {
        try {
            return this.mService.updateLockdownVpn();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int checkMobileProvisioning(int suggestedTimeOutMs) {
        try {
            return this.mService.checkMobileProvisioning(suggestedTimeOutMs);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getMobileProvisioningUrl() {
        try {
            return this.mService.getMobileProvisioningUrl();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void setProvisioningNotificationVisible(boolean visible, int networkType, String action) {
        try {
            this.mService.setProvisioningNotificationVisible(visible, networkType, action);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setAirplaneMode(boolean enable) {
        try {
            this.mService.setAirplaneMode(enable);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void registerNetworkFactory(Messenger messenger, String name) {
        try {
            this.mService.registerNetworkFactory(messenger, name);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void unregisterNetworkFactory(Messenger messenger) {
        try {
            this.mService.unregisterNetworkFactory(messenger);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int registerNetworkAgent(Messenger messenger, NetworkInfo ni, LinkProperties lp, NetworkCapabilities nc, int score, NetworkMisc misc) {
        try {
            return this.mService.registerNetworkAgent(messenger, ni, lp, nc, score, misc);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static String getCallbackName(int whichCallback) {
        switch (whichCallback) {
            case CALLBACK_PRECHECK /*524289*/:
                return "CALLBACK_PRECHECK";
            case CALLBACK_AVAILABLE /*524290*/:
                return "CALLBACK_AVAILABLE";
            case CALLBACK_LOSING /*524291*/:
                return "CALLBACK_LOSING";
            case CALLBACK_LOST /*524292*/:
                return "CALLBACK_LOST";
            case CALLBACK_UNAVAIL /*524293*/:
                return "CALLBACK_UNAVAIL";
            case CALLBACK_CAP_CHANGED /*524294*/:
                return "CALLBACK_CAP_CHANGED";
            case CALLBACK_IP_CHANGED /*524295*/:
                return "CALLBACK_IP_CHANGED";
            case CALLBACK_RELEASED /*524296*/:
                return "CALLBACK_RELEASED";
            case CALLBACK_EXIT /*524297*/:
                return "CALLBACK_EXIT";
            case EXPIRE_LEGACY_REQUEST /*524298*/:
                return "EXPIRE_LEGACY_REQUEST";
            case CALLBACK_SUSPENDED /*524299*/:
                return "CALLBACK_SUSPENDED";
            case CALLBACK_RESUMED /*524300*/:
                return "CALLBACK_RESUMED";
            default:
                return Integer.toString(whichCallback);
        }
    }

    private CallbackHandler getDefaultHandler() {
        CallbackHandler callbackHandler;
        synchronized (sCallbacks) {
            if (sCallbackHandler == null) {
                sCallbackHandler = new CallbackHandler(ConnectivityThread.getInstanceLooper());
            }
            callbackHandler = sCallbackHandler;
        }
        return callbackHandler;
    }

    private NetworkRequest sendRequestForNetwork(NetworkCapabilities need, NetworkCallback callback, int timeoutMs, int action, int legacyType, CallbackHandler handler) {
        if (callback == null) {
            throw new IllegalArgumentException("null NetworkCallback");
        } else if (need != null || action == 2) {
            try {
                NetworkRequest request;
                synchronized (sCallbacks) {
                    Messenger messenger = new Messenger((Handler) handler);
                    Binder binder = new Binder();
                    if (action == 1) {
                        request = this.mService.listenForNetwork(need, messenger, binder);
                    } else {
                        request = this.mService.requestNetwork(need, messenger, timeoutMs, binder, legacyType);
                    }
                    if (request != null) {
                        sCallbacks.put(request, callback);
                    }
                    callback.networkRequest = request;
                }
                return request;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("null NetworkCapabilities");
        }
    }

    public void requestNetwork(NetworkRequest request, NetworkCallback networkCallback, int timeoutMs, int legacyType, Handler handler) {
        sendRequestForNetwork(request.networkCapabilities, networkCallback, timeoutMs, 2, legacyType, new CallbackHandler(this, handler));
    }

    public void requestNetwork(NetworkRequest request, NetworkCallback networkCallback) {
        requestNetwork(request, networkCallback, getDefaultHandler());
    }

    public void requestNetwork(NetworkRequest request, NetworkCallback networkCallback, Handler handler) {
        requestNetwork(request, networkCallback, 0, inferLegacyTypeForNetworkCapabilities(request.networkCapabilities), new CallbackHandler(this, handler));
    }

    public void requestNetwork(NetworkRequest request, NetworkCallback networkCallback, int timeoutMs) {
        if (timeoutMs <= 0) {
            throw new IllegalArgumentException("Non-positive timeoutMs: " + timeoutMs);
        }
        requestNetwork(request, networkCallback, timeoutMs, inferLegacyTypeForNetworkCapabilities(request.networkCapabilities), getDefaultHandler());
    }

    public void requestNetwork(NetworkRequest request, NetworkCallback networkCallback, Handler handler, int timeoutMs) {
        if (timeoutMs <= 0) {
            throw new IllegalArgumentException("Non-positive timeoutMs");
        }
        requestNetwork(request, networkCallback, timeoutMs, inferLegacyTypeForNetworkCapabilities(request.networkCapabilities), new CallbackHandler(this, handler));
    }

    public void requestNetwork(NetworkRequest request, PendingIntent operation) {
        checkPendingIntent(operation);
        try {
            this.mService.pendingRequestForNetwork(request.networkCapabilities, operation);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void releaseNetworkRequest(PendingIntent operation) {
        checkPendingIntent(operation);
        try {
            this.mService.releasePendingNetworkRequest(operation);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private void checkPendingIntent(PendingIntent intent) {
        if (intent == null) {
            throw new IllegalArgumentException("PendingIntent cannot be null.");
        }
    }

    public void registerNetworkCallback(NetworkRequest request, NetworkCallback networkCallback) {
        registerNetworkCallback(request, networkCallback, getDefaultHandler());
    }

    public void registerNetworkCallback(NetworkRequest request, NetworkCallback networkCallback, Handler handler) {
        sendRequestForNetwork(request.networkCapabilities, networkCallback, 0, 1, -1, new CallbackHandler(this, handler));
    }

    public void registerNetworkCallback(NetworkRequest request, PendingIntent operation) {
        checkPendingIntent(operation);
        try {
            this.mService.pendingListenForNetwork(request.networkCapabilities, operation);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void registerDefaultNetworkCallback(NetworkCallback networkCallback) {
        registerDefaultNetworkCallback(networkCallback, getDefaultHandler());
    }

    public void registerDefaultNetworkCallback(NetworkCallback networkCallback, Handler handler) {
        sendRequestForNetwork(null, networkCallback, 0, 2, -1, new CallbackHandler(this, handler));
    }

    public boolean requestBandwidthUpdate(Network network) {
        try {
            return this.mService.requestBandwidthUpdate(network);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void unregisterNetworkCallback(NetworkCallback networkCallback) {
        if (networkCallback == null || networkCallback.networkRequest == null || networkCallback.networkRequest.requestId == 0) {
            throw new IllegalArgumentException("Invalid NetworkCallback");
        }
        try {
            this.mService.releaseNetworkRequest(networkCallback.networkRequest);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void unregisterNetworkCallback(PendingIntent operation) {
        releaseNetworkRequest(operation);
    }

    public void setAcceptUnvalidated(Network network, boolean accept, boolean always) {
        try {
            this.mService.setAcceptUnvalidated(network, accept, always);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setAvoidUnvalidated(Network network) {
        try {
            this.mService.setAvoidUnvalidated(network);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void startCaptivePortalApp(Network network) {
        try {
            this.mService.startCaptivePortalApp(network);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getMultipathPreference(Network network) {
        try {
            return this.mService.getMultipathPreference(network);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void factoryReset() {
        try {
            this.mService.factoryReset();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean bindProcessToNetwork(Network network) {
        return setProcessDefaultNetwork(network);
    }

    @Deprecated
    public static boolean setProcessDefaultNetwork(Network network) {
        int netId = network == null ? 0 : network.netId;
        if (netId == NetworkUtils.getBoundNetworkForProcess()) {
            return true;
        }
        if (!NetworkUtils.bindProcessToNetwork(netId)) {
            return false;
        }
        try {
            Proxy.setHttpProxySystemProperty(getInstance().getDefaultProxy());
        } catch (SecurityException e) {
            Log.e(TAG, "Can't set proxy properties", e);
        }
        InetAddress.clearDnsCache();
        NetworkEventDispatcher.getInstance().onNetworkConfigurationChanged();
        return true;
    }

    public Network getBoundNetworkForProcess() {
        return getProcessDefaultNetwork();
    }

    @Deprecated
    public static Network getProcessDefaultNetwork() {
        int netId = NetworkUtils.getBoundNetworkForProcess();
        if (netId == 0) {
            return null;
        }
        return new Network(netId);
    }

    private void unsupportedStartingFrom(int version) {
        if (Process.myUid() != 1000 && this.mContext.getApplicationInfo().targetSdkVersion >= version) {
            throw new UnsupportedOperationException("This method is not supported in target SDK version " + version + " and above");
        }
    }

    private void checkLegacyRoutingApiAccess() {
        String permForOmadm = "com.android.permission.INJECT_OMADM_SETTINGS";
        if (this.mContext.checkCallingOrSelfPermission(permission.USE_LEGACY_INTERFACE) != 0 && this.mContext.checkCallingOrSelfPermission(permForOmadm) != 0) {
            unsupportedStartingFrom(23);
        }
    }

    @Deprecated
    public static boolean setProcessDefaultNetworkForHostResolution(Network network) {
        return NetworkUtils.bindProcessToNetworkForHostResolution(network == null ? 0 : network.netId);
    }

    private INetworkPolicyManager getNetworkPolicyManager() {
        synchronized (this) {
            INetworkPolicyManager iNetworkPolicyManager;
            if (this.mNPManager != null) {
                iNetworkPolicyManager = this.mNPManager;
                return iNetworkPolicyManager;
            }
            this.mNPManager = INetworkPolicyManager.Stub.asInterface(ServiceManager.getService(Context.NETWORK_POLICY_SERVICE));
            iNetworkPolicyManager = this.mNPManager;
            return iNetworkPolicyManager;
        }
    }

    public int getRestrictBackgroundStatus() {
        try {
            return getNetworkPolicyManager().getRestrictBackgroundByCaller();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setLteMobileDataEnabled(boolean enable) {
        Log.d(TAG, "[enter]setLteMobileDataEnabled " + enable);
        try {
            this.mService.setLteMobileDataEnabled(enable);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public int checkLteConnectState() {
        Log.d(TAG, "[enter]checkLteConnectState");
        int lteState = 3;
        try {
            return this.mService.checkLteConnectState();
        } catch (RemoteException e) {
            e.printStackTrace();
            return lteState;
        }
    }

    public long getLteTotalRxBytes() {
        Log.d(TAG, "[enter]getLteTotalRxBytes");
        long lteTotalRxBytes = 0;
        try {
            return this.mService.getLteTotalRxBytes();
        } catch (RemoteException e) {
            e.printStackTrace();
            return lteTotalRxBytes;
        }
    }

    public long getLteTotalTxBytes() {
        Log.d(TAG, "[enter]getLteTotalTxBytes");
        long lteTotalTxBytes = 0;
        try {
            return this.mService.getLteTotalTxBytes();
        } catch (RemoteException e) {
            e.printStackTrace();
            return lteTotalTxBytes;
        }
    }

    private static final String whatToString(int what) {
        return (String) NoPreloadHolder.sMagicDecoderRing.get(what, Integer.toString(what));
    }
}
