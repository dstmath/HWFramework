package android.net;

import android.Manifest;
import android.annotation.SystemApi;
import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.net.INetworkPolicyManager;
import android.net.NetworkRequest;
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
import android.os.ServiceSpecificException;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseIntArray;
import com.android.internal.R;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwConnectivityManagerConstants;
import com.android.internal.telephony.ITelephony;
import com.android.internal.util.Preconditions;
import huawei.cust.HwCustUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import libcore.net.event.NetworkEventDispatcher;

public class ConnectivityManager implements HwConnectivityManagerConstants {
    @Deprecated
    public static final String ACTION_BACKGROUND_DATA_SETTING_CHANGED = "android.net.conn.BACKGROUND_DATA_SETTING_CHANGED";
    public static final String ACTION_CAPTIVE_PORTAL_SIGN_IN = "android.net.conn.CAPTIVE_PORTAL";
    public static final String ACTION_CAPTIVE_PORTAL_TEST_COMPLETED = "android.net.conn.CAPTIVE_PORTAL_TEST_COMPLETED";
    public static final String ACTION_DATA_ACTIVITY_CHANGE = "android.net.conn.DATA_ACTIVITY_CHANGE";
    public static final String ACTION_PROMPT_LOST_VALIDATION = "android.net.conn.PROMPT_LOST_VALIDATION";
    public static final String ACTION_PROMPT_UNVALIDATED = "android.net.conn.PROMPT_UNVALIDATED";
    public static final String ACTION_RESTRICT_BACKGROUND_CHANGED = "android.net.conn.RESTRICT_BACKGROUND_CHANGED";
    public static final String ACTION_TETHER_STATE_CHANGED = "android.net.conn.TETHER_STATE_CHANGED";
    private static final NetworkRequest ALREADY_UNREGISTERED = new NetworkRequest.Builder().clearCapabilities().build();
    private static final int BASE = 524288;
    public static final int CALLBACK_AVAILABLE = 524290;
    public static final int CALLBACK_CAP_CHANGED = 524294;
    public static final int CALLBACK_IP_CHANGED = 524295;
    public static final int CALLBACK_LOSING = 524291;
    public static final int CALLBACK_LOST = 524292;
    public static final int CALLBACK_PRECHECK = 524289;
    public static final int CALLBACK_RESUMED = 524298;
    public static final int CALLBACK_SUSPENDED = 524297;
    public static final int CALLBACK_UNAVAIL = 524293;
    @Deprecated
    public static final String CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    public static final String CONNECTIVITY_ACTION_SUPL = "android.net.conn.CONNECTIVITY_CHANGE_SUPL";
    @Deprecated
    public static final int DEFAULT_NETWORK_PREFERENCE = 1;
    private static final int EXPIRE_LEGACY_REQUEST = 524296;
    public static final String EXTRA_ACTIVE_LOCAL_ONLY = "localOnlyArray";
    public static final String EXTRA_ACTIVE_TETHER = "tetherArray";
    public static final String EXTRA_ADD_TETHER_TYPE = "extraAddTetherType";
    public static final String EXTRA_AVAILABLE_TETHER = "availableArray";
    public static final String EXTRA_CAPTIVE_PORTAL = "android.net.extra.CAPTIVE_PORTAL";
    public static final String EXTRA_CAPTIVE_PORTAL_PROBE_SPEC = "android.net.extra.CAPTIVE_PORTAL_PROBE_SPEC";
    public static final String EXTRA_CAPTIVE_PORTAL_URL = "android.net.extra.CAPTIVE_PORTAL_URL";
    public static final String EXTRA_CAPTIVE_PORTAL_USER_AGENT = "android.net.extra.CAPTIVE_PORTAL_USER_AGENT";
    public static final String EXTRA_DEVICE_TYPE = "deviceType";
    public static final String EXTRA_ERRORED_TETHER = "erroredArray";
    public static final String EXTRA_EXTRA_INFO = "extraInfo";
    public static final String EXTRA_INET_CONDITION = "inetCondition";
    public static final String EXTRA_IS_ACTIVE = "isActive";
    public static final String EXTRA_IS_CAPTIVE_PORTAL = "captivePortal";
    public static final String EXTRA_IS_FAILOVER = "isFailover";
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
    public static final int MAX_NETWORK_TYPE = 47;
    public static final int MAX_RADIO_TYPE = 17;
    private static final int MIN_NETWORK_TYPE = 0;
    public static final int MULTIPATH_PREFERENCE_HANDOVER = 1;
    public static final int MULTIPATH_PREFERENCE_PERFORMANCE = 4;
    public static final int MULTIPATH_PREFERENCE_RELIABILITY = 2;
    public static final int MULTIPATH_PREFERENCE_UNMETERED = 7;
    public static final int NETID_UNSET = 0;
    public static final String PRIVATE_DNS_DEFAULT_MODE_FALLBACK = "opportunistic";
    public static final String PRIVATE_DNS_MODE_OFF = "off";
    public static final String PRIVATE_DNS_MODE_OPPORTUNISTIC = "opportunistic";
    public static final String PRIVATE_DNS_MODE_PROVIDER_HOSTNAME = "hostname";
    private static final int REQUEST = 2;
    public static final int REQUEST_ID_UNSET = 0;
    public static final int RESTRICT_BACKGROUND_STATUS_DISABLED = 1;
    public static final int RESTRICT_BACKGROUND_STATUS_ENABLED = 3;
    public static final int RESTRICT_BACKGROUND_STATUS_WHITELISTED = 2;
    private static final String TAG = "ConnectivityManager";
    @SystemApi
    public static final int TETHERING_BLUETOOTH = 2;
    public static final int TETHERING_INVALID = -1;
    @SystemApi
    public static final int TETHERING_USB = 1;
    @SystemApi
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
    @Deprecated
    public static final int TYPE_BLUETOOTH = 7;
    @Deprecated
    public static final int TYPE_DUMMY = 8;
    @Deprecated
    public static final int TYPE_ETHERNET = 9;
    @Deprecated
    public static final int TYPE_MOBILE = 0;
    @Deprecated
    public static final int TYPE_MOBILE_CBS = 12;
    @Deprecated
    public static final int TYPE_MOBILE_DUN = 4;
    @Deprecated
    public static final int TYPE_MOBILE_EMERGENCY = 15;
    @Deprecated
    public static final int TYPE_MOBILE_FOTA = 10;
    @Deprecated
    public static final int TYPE_MOBILE_HIPRI = 5;
    @Deprecated
    public static final int TYPE_MOBILE_IA = 14;
    @Deprecated
    public static final int TYPE_MOBILE_IMS = 11;
    @Deprecated
    public static final int TYPE_MOBILE_MMS = 2;
    @Deprecated
    public static final int TYPE_MOBILE_SUPL = 3;
    public static final int TYPE_NONE = -1;
    @Deprecated
    public static final int TYPE_PROXY = 16;
    @Deprecated
    public static final int TYPE_VPN = 17;
    @Deprecated
    public static final int TYPE_WIFI = 1;
    @Deprecated
    public static final int TYPE_WIFI_P2P = 13;
    @Deprecated
    public static final int TYPE_WIMAX = 6;
    private static int lastLegacyRequestId = 0;
    private static CallbackHandler sCallbackHandler;
    /* access modifiers changed from: private */
    public static final HashMap<NetworkRequest, NetworkCallback> sCallbacks = new HashMap<>();
    private static ConnectivityManager sInstance;
    private static HashMap<NetworkCapabilities, LegacyRequest> sLegacyRequests = new HashMap<>();
    private static final SparseIntArray sLegacyTypeToCapability = new SparseIntArray();
    private static final SparseIntArray sLegacyTypeToTransport = new SparseIntArray();
    private final Context mContext;
    HwCustConnectivityManager mCust = ((HwCustConnectivityManager) HwCustUtils.createObj(HwCustConnectivityManager.class, new Object[0]));
    private INetworkManagementService mNMService;
    private INetworkPolicyManager mNPManager;
    private final ArrayMap<OnNetworkActiveListener, INetworkActivityListener> mNetworkActivityListeners = new ArrayMap<>();
    /* access modifiers changed from: private */
    public final IConnectivityManager mService;

    private class CallbackHandler extends Handler {
        private static final boolean DBG = false;
        private static final String TAG = "ConnectivityManager.CallbackHandler";

        CallbackHandler(Looper looper) {
            super(looper);
        }

        CallbackHandler(ConnectivityManager connectivityManager, Handler handler) {
            this(((Handler) Preconditions.checkNotNull(handler, "Handler cannot be null.")).getLooper());
        }

        public void handleMessage(Message message) {
            NetworkCallback callback;
            if (message.what == ConnectivityManager.EXPIRE_LEGACY_REQUEST) {
                ConnectivityManager.this.expireRequest((NetworkCapabilities) message.obj, message.arg1);
                return;
            }
            NetworkRequest request = (NetworkRequest) getObject(message, NetworkRequest.class);
            Network network = (Network) getObject(message, Network.class);
            synchronized (ConnectivityManager.sCallbacks) {
                callback = (NetworkCallback) ConnectivityManager.sCallbacks.get(request);
            }
            if (callback == null) {
                Log.w(TAG, "callback not found for " + ConnectivityManager.getCallbackName(message.what) + " message");
                return;
            }
            switch (message.what) {
                case ConnectivityManager.CALLBACK_PRECHECK /*524289*/:
                    callback.onPreCheck(network);
                    break;
                case ConnectivityManager.CALLBACK_AVAILABLE /*524290*/:
                    callback.onAvailable(network, (NetworkCapabilities) getObject(message, NetworkCapabilities.class), (LinkProperties) getObject(message, LinkProperties.class));
                    break;
                case ConnectivityManager.CALLBACK_LOSING /*524291*/:
                    callback.onLosing(network, message.arg1);
                    break;
                case ConnectivityManager.CALLBACK_LOST /*524292*/:
                    callback.onLost(network);
                    break;
                case ConnectivityManager.CALLBACK_UNAVAIL /*524293*/:
                    callback.onUnavailable();
                    break;
                case ConnectivityManager.CALLBACK_CAP_CHANGED /*524294*/:
                    callback.onCapabilitiesChanged(network, (NetworkCapabilities) getObject(message, NetworkCapabilities.class));
                    break;
                case ConnectivityManager.CALLBACK_IP_CHANGED /*524295*/:
                    callback.onLinkPropertiesChanged(network, (LinkProperties) getObject(message, LinkProperties.class));
                    break;
                case ConnectivityManager.CALLBACK_SUSPENDED /*524297*/:
                    callback.onNetworkSuspended(network);
                    break;
                case ConnectivityManager.CALLBACK_RESUMED /*524298*/:
                    callback.onNetworkResumed(network);
                    break;
            }
        }

        private <T> T getObject(Message msg, Class<T> c) {
            return msg.getData().getParcelable(c.getSimpleName());
        }
    }

    public interface Errors {
        public static final int TOO_MANY_REQUESTS = 1;
    }

    private static class LegacyRequest {
        Network currentNetwork;
        int delay;
        int expireSequenceNumber;
        NetworkCallback networkCallback;
        NetworkCapabilities networkCapabilities;
        NetworkRequest networkRequest;

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

        /* access modifiers changed from: private */
        public void clearDnsBinding() {
            if (this.currentNetwork != null) {
                this.currentNetwork = null;
                ConnectivityManager.setProcessDefaultNetworkForHostResolution(null);
            }
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface MultipathPreference {
    }

    public static class NetworkCallback {
        /* access modifiers changed from: private */
        public NetworkRequest networkRequest;

        public void onPreCheck(Network network) {
        }

        public void onAvailable(Network network, NetworkCapabilities networkCapabilities, LinkProperties linkProperties) {
            onAvailable(network);
            if (!networkCapabilities.hasCapability(21)) {
                onNetworkSuspended(network);
            }
            onCapabilitiesChanged(network, networkCapabilities);
            onLinkPropertiesChanged(network, linkProperties);
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

    public interface OnNetworkActiveListener {
        void onNetworkActive();
    }

    @SystemApi
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
        public static final int MIN_INTERVAL = 10;
        public static final int NATT_PORT = 4500;
        public static final int NO_KEEPALIVE = -1;
        public static final int SUCCESS = 0;
        private static final String TAG = "PacketKeepalive";
        /* access modifiers changed from: private */
        public final PacketKeepaliveCallback mCallback;
        private final Looper mLooper;
        /* access modifiers changed from: private */
        public final Messenger mMessenger;
        private final Network mNetwork;
        /* access modifiers changed from: private */
        public volatile Integer mSlot;

        /* access modifiers changed from: package-private */
        public void stopLooper() {
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
            this.mMessenger = new Messenger(new Handler(this.mLooper, ConnectivityManager.this) {
                public void handleMessage(Message message) {
                    if (message.what != 528397) {
                        Log.e(PacketKeepalive.TAG, "Unhandled message " + Integer.toHexString(message.what));
                        return;
                    }
                    int error = message.arg2;
                    if (error == 0) {
                        try {
                            if (PacketKeepalive.this.mSlot == null) {
                                Integer unused = PacketKeepalive.this.mSlot = Integer.valueOf(message.arg1);
                                PacketKeepalive.this.mCallback.onStarted();
                                return;
                            }
                            Integer unused2 = PacketKeepalive.this.mSlot = null;
                            PacketKeepalive.this.stopLooper();
                            PacketKeepalive.this.mCallback.onStopped();
                        } catch (Exception e) {
                            Log.e(PacketKeepalive.TAG, "Exception in keepalive callback(" + error + ")", e);
                        }
                    } else {
                        PacketKeepalive.this.stopLooper();
                        PacketKeepalive.this.mCallback.onError(error);
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

    @Retention(RetentionPolicy.SOURCE)
    public @interface RestrictBackgroundStatus {
    }

    public static class TooManyRequestsException extends RuntimeException {
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
        sLegacyTypeToTransport.put(1, 1);
        sLegacyTypeToTransport.put(13, 1);
        sLegacyTypeToTransport.put(7, 2);
        sLegacyTypeToTransport.put(9, 3);
        HwFrameworkFactory.getHwInnerConnectivityManager().setLegacyTypeToTransportEx(sLegacyTypeToTransport);
        sLegacyTypeToCapability.put(12, 5);
        sLegacyTypeToCapability.put(4, 2);
        sLegacyTypeToCapability.put(10, 3);
        sLegacyTypeToCapability.put(11, 4);
        sLegacyTypeToCapability.put(2, 0);
        sLegacyTypeToCapability.put(3, 1);
        sLegacyTypeToCapability.put(13, 6);
        HwFrameworkFactory.getHwInnerConnectivityManager().setLegacyTypeToCapabilityEx(sLegacyTypeToCapability);
    }

    @Deprecated
    public static boolean isNetworkTypeValid(int networkType) {
        return (networkType >= 0 && networkType <= 17) || (networkType >= 34 && networkType <= 45) || ((networkType >= 46 && networkType <= 47) || networkType == 48);
    }

    @Deprecated
    public static String getNetworkTypeName(int type) {
        switch (type) {
            case -1:
                return "NONE";
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
            default:
                return HwFrameworkFactory.getHwInnerConnectivityManager().getNetworkTypeNameEx(type);
        }
    }

    @Deprecated
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
                return true;
            default:
                return HwFrameworkFactory.getHwInnerConnectivityManager().isNetworkTypeMobileEx(networkType);
        }
    }

    @Deprecated
    public static boolean isNetworkTypeWifi(int networkType) {
        if (networkType == 1 || networkType == 13) {
            return true;
        }
        return HwFrameworkFactory.getHwInnerConnectivityManager().isNetworkTypeWifiEx(networkType);
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

    public boolean isAlwaysOnVpnPackageSupportedForUser(int userId, String vpnPackage) {
        try {
            return this.mService.isAlwaysOnVpnPackageSupported(userId, vpnPackage);
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

    @SystemApi
    public String getCaptivePortalServerUrl() {
        try {
            return this.mService.getCaptivePortalServerUrl();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x007d, code lost:
        if (r2 == null) goto L_0x0097;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x007f, code lost:
        android.util.Log.d(TAG, "starting startUsingNetworkFeature for request " + r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0096, code lost:
        return 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0097, code lost:
        android.util.Log.d(TAG, " request Failed");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x009e, code lost:
        return 3;
     */
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
                LegacyRequest l = sLegacyRequests.get(netCap);
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

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0061, code lost:
        if (r11.equals("enableDUN") != false) goto L_0x0079;
     */
    private NetworkCapabilities networkCapabilitiesForFeature(int networkType, String feature) {
        char c = 1;
        if (networkType == 0) {
            String[] result = HwFrameworkFactory.getHwInnerConnectivityManager().getFeature(feature);
            if (result[0] != null && !"".equals(result[0])) {
                feature = result[0];
            }
            switch (feature.hashCode()) {
                case -1451370941:
                    if (feature.equals("enableHIPRI")) {
                        c = 4;
                        break;
                    }
                case -631682191:
                    if (feature.equals("enableCBS")) {
                        c = 0;
                        break;
                    }
                case -631680646:
                    break;
                case -631676084:
                    if (feature.equals("enableIMS")) {
                        c = 5;
                        break;
                    }
                case -631672240:
                    if (feature.equals("enableMMS")) {
                        c = 6;
                        break;
                    }
                case 1892790521:
                    if (feature.equals("enableFOTA")) {
                        c = 3;
                        break;
                    }
                case 1893183457:
                    if (feature.equals("enableSUPL")) {
                        c = 7;
                        break;
                    }
                case 1998933033:
                    if (feature.equals("enableDUNAlways")) {
                        c = 2;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    return networkCapabilitiesForType(12);
                case 1:
                case 2:
                    return networkCapabilitiesForType(4);
                case 3:
                    return networkCapabilitiesForType(10);
                case 4:
                    return networkCapabilitiesForType(5);
                case 5:
                    return networkCapabilitiesForType(11);
                case 6:
                    return networkCapabilitiesForType(2);
                case 7:
                    return networkCapabilitiesForType(3);
                default:
                    return HwFrameworkFactory.getHwInnerConnectivityManager().networkCapabilitiesForFeatureEx(this.mCust, feature);
            }
        } else if (networkType != 1 || !"p2p".equals(feature)) {
            return null;
        } else {
            return networkCapabilitiesForType(13);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v1, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v6, resolved type: java.lang.String} */
    /* JADX WARNING: Multi-variable type inference failed */
    private int inferLegacyTypeForNetworkCapabilities(NetworkCapabilities netCap) {
        int result;
        String type;
        if (netCap == null || !netCap.hasTransport(0)) {
            return -1;
        }
        if (!netCap.hasCapability(1) && (this.mCust == null || !this.mCust.canHandleEimsNetworkCapabilities(netCap))) {
            return -1;
        }
        String type2 = null;
        int result2 = -1;
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
        } else {
            for (Map.Entry<String, Integer> entry : HwFrameworkFactory.getHwInnerConnectivityManager().inferLegacyTypeForNetworkCapabilitiesEx(netCap, this.mCust, sLegacyTypeToTransport, sLegacyTypeToCapability).entrySet()) {
                type2 = entry.getKey();
                result2 = entry.getValue().intValue();
                if (type2 == null) {
                    break;
                }
            }
            type = type2;
            result = result2;
        }
        if (type != null) {
            NetworkCapabilities testCap = networkCapabilitiesForFeature(0, type);
            if (testCap == null || !testCap.equalsNetCapabilities(netCap) || !testCap.equalsTransportTypes(netCap)) {
                return -1;
            }
            return result;
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
        return HwFrameworkFactory.getHwInnerConnectivityManager().legacyTypeForNetworkCapabilitiesEx(netCap, this.mCust);
    }

    private NetworkRequest findRequestForFeature(NetworkCapabilities netCap) {
        synchronized (sLegacyRequests) {
            LegacyRequest l = sLegacyRequests.get(netCap);
            if (l == null) {
                return null;
            }
            NetworkRequest networkRequest = l.networkRequest;
            return networkRequest;
        }
    }

    private void renewRequestLocked(LegacyRequest l) {
        l.expireSequenceNumber = getNewLastLegacyRequestId();
        Log.d(TAG, "renewing request to seqNum " + l.expireSequenceNumber);
        sendExpireMsgForFeature(l.networkCapabilities, l.expireSequenceNumber, l.delay);
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001b, code lost:
        android.util.Log.d(TAG, "expireRequest with " + r0 + ", " + r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0039, code lost:
        return;
     */
    public void expireRequest(NetworkCapabilities netCap, int sequenceNum) {
        synchronized (sLegacyRequests) {
            LegacyRequest l = sLegacyRequests.get(netCap);
            if (l != null) {
                int ourSeqNum = l.expireSequenceNumber;
                if (l.expireSequenceNumber == sequenceNum) {
                    removeRequestForFeature(netCap);
                }
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
            l = sLegacyRequests.remove(netCap);
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
        boolean z = transport != -1;
        Preconditions.checkArgument(z, "unknown legacy type: " + type);
        nc.addTransportType(transport);
        nc.addCapability(sLegacyTypeToCapability.get(type, 12));
        nc.maybeMarkCapabilitiesRestricted();
        return nc;
    }

    public PacketKeepalive startNattKeepalive(Network network, int intervalSeconds, PacketKeepaliveCallback callback, InetAddress srcAddr, int srcPort, InetAddress dstAddr) {
        Network network2 = network;
        PacketKeepalive k = new PacketKeepalive(network2, callback);
        try {
            this.mService.startNattKeepalive(network2, intervalSeconds, k.mMessenger, new Binder(), srcAddr.getHostAddress(), srcPort, dstAddr.getHostAddress());
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

    @Deprecated
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
                ITelephony it = ITelephony.Stub.asInterface(b);
                int subId = SubscriptionManager.getDefaultDataSubscriptionId();
                Log.d(TAG, "getMobileDataEnabled()+ subId=" + subId);
                boolean retVal = it.isUserDataEnabled(subId);
                Log.d(TAG, "getMobileDataEnabled()- subId=" + subId + " retVal=" + retVal);
                return retVal;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.d(TAG, "getMobileDataEnabled()- remote exception retVal=false");
            return false;
        }
    }

    private INetworkManagementService getNetworkManagementService() {
        synchronized (this) {
            if (this.mNMService != null) {
                INetworkManagementService iNetworkManagementService = this.mNMService;
                return iNetworkManagementService;
            }
            this.mNMService = INetworkManagementService.Stub.asInterface(ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
            INetworkManagementService iNetworkManagementService2 = this.mNMService;
            return iNetworkManagementService2;
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
        INetworkActivityListener rl = this.mNetworkActivityListeners.get(l);
        Preconditions.checkArgument(rl != null, "Listener was not registered.");
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

    public static final void enforceChangePermission(Context context) {
        int uid = Binder.getCallingUid();
        Settings.checkAndNoteChangeNetworkStateOperation(context, uid, Settings.getPackageNameForUid(context, uid), true);
    }

    public static final void enforceTetherChangePermission(Context context, String callingPkg) {
        Preconditions.checkNotNull(context, "Context cannot be null");
        Preconditions.checkNotNull(callingPkg, "callingPkg cannot be null");
        if (context.getResources().getStringArray(R.array.config_mobile_hotspot_provision_app).length == 2) {
            context.enforceCallingOrSelfPermission(Manifest.permission.TETHER_PRIVILEGED, "ConnectivityService");
        } else {
            Settings.checkAndNoteWriteSettingsOperation(context, Binder.getCallingUid(), callingPkg, true);
        }
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
            String pkgName = this.mContext.getOpPackageName();
            Log.i(TAG, "tether caller:" + pkgName);
            return this.mService.tether(iface, pkgName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int untether(String iface) {
        try {
            String pkgName = this.mContext.getOpPackageName();
            Log.i(TAG, "untether caller:" + pkgName);
            return this.mService.untether(iface, pkgName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public boolean isTetheringSupported() {
        try {
            return this.mService.isTetheringSupported(this.mContext.getOpPackageName());
        } catch (SecurityException e) {
            return false;
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void startTethering(int type, boolean showProvisioningUi, OnStartTetheringCallback callback) {
        startTethering(type, showProvisioningUi, callback, null);
    }

    @SystemApi
    public void startTethering(int type, boolean showProvisioningUi, final OnStartTetheringCallback callback, Handler handler) {
        Preconditions.checkNotNull(callback, "OnStartTetheringCallback cannot be null.");
        ResultReceiver wrappedCallback = new ResultReceiver(handler) {
            /* access modifiers changed from: protected */
            public void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == 0) {
                    callback.onTetheringStarted();
                } else {
                    callback.onTetheringFailed();
                }
            }
        };
        try {
            String pkgName = this.mContext.getOpPackageName();
            Log.i(TAG, "startTethering caller:" + pkgName);
            this.mService.startTethering(type, wrappedCallback, showProvisioningUi, pkgName);
        } catch (RemoteException e) {
            Log.e(TAG, "Exception trying to start tethering.", e);
            wrappedCallback.send(2, null);
        }
    }

    @SystemApi
    public void stopTethering(int type) {
        try {
            String pkgName = this.mContext.getOpPackageName();
            Log.i(TAG, "stopTethering caller:" + pkgName);
            this.mService.stopTethering(type, pkgName);
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
            String pkgName = this.mContext.getOpPackageName();
            Log.i(TAG, "setUsbTethering caller:" + pkgName);
            return this.mService.setUsbTethering(enable, pkgName);
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

    @Deprecated
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

    private static RuntimeException convertServiceException(ServiceSpecificException e) {
        if (e.errorCode == 1) {
            return new TooManyRequestsException();
        }
        Log.w(TAG, "Unknown service error code " + e.errorCode);
        return new RuntimeException(e);
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
            case EXPIRE_LEGACY_REQUEST /*524296*/:
                return "EXPIRE_LEGACY_REQUEST";
            case CALLBACK_SUSPENDED /*524297*/:
                return "CALLBACK_SUSPENDED";
            case CALLBACK_RESUMED /*524298*/:
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
        NetworkRequest request;
        NetworkCapabilities networkCapabilities = need;
        NetworkCallback networkCallback = callback;
        int i = action;
        checkCallbackNotNull(callback);
        Preconditions.checkArgument(i == 2 || networkCapabilities != null, "null NetworkCapabilities");
        if (this.mCust != null && this.mCust.isSubInactiveFromNetworkCapabilities(networkCapabilities) && (HuaweiTelephonyConfigs.isHisiPlatform() || HuaweiTelephonyConfigs.isMTKPlatform())) {
            return null;
        }
        if (this.mCust == null || (!this.mCust.isDisableRequestBySIM2(networkCapabilities) && !this.mCust.isBlockNetworkRequest(networkCapabilities))) {
            try {
                synchronized (sCallbacks) {
                    try {
                        if (!(callback.networkRequest == null || callback.networkRequest == ALREADY_UNREGISTERED)) {
                            Log.e(TAG, "NetworkCallback was already registered");
                        }
                        Messenger messenger = new Messenger(handler);
                        Binder binder = new Binder();
                        if (i == 1) {
                            request = this.mService.listenForNetwork(networkCapabilities, messenger, binder);
                        } else {
                            String pkgName = this.mContext.getOpPackageName();
                            Log.d(TAG, "requestNetwork and the calling app is: " + pkgName);
                            request = this.mService.requestNetwork(networkCapabilities, messenger, timeoutMs, binder, legacyType);
                        }
                        if (request != null) {
                            sCallbacks.put(request, networkCallback);
                        }
                        NetworkRequest unused = networkCallback.networkRequest = request;
                        return request;
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                }
            } catch (RemoteException e) {
                e = e;
                CallbackHandler callbackHandler = handler;
                throw e.rethrowFromSystemServer();
            } catch (ServiceSpecificException e2) {
                e = e2;
                CallbackHandler callbackHandler2 = handler;
                throw convertServiceException(e);
            }
        } else {
            Log.d(TAG, "SIM2 data disable by cust");
            return null;
        }
    }

    public void requestNetwork(NetworkRequest request, NetworkCallback networkCallback, int timeoutMs, int legacyType, Handler handler) {
        sendRequestForNetwork(request.networkCapabilities, networkCallback, timeoutMs, 2, legacyType, new CallbackHandler(this, handler));
    }

    public void requestNetwork(NetworkRequest request, NetworkCallback networkCallback) {
        requestNetwork(request, networkCallback, (Handler) getDefaultHandler());
    }

    public void requestNetwork(NetworkRequest request, NetworkCallback networkCallback, Handler handler) {
        requestNetwork(request, networkCallback, 0, inferLegacyTypeForNetworkCapabilities(request.networkCapabilities), new CallbackHandler(this, handler));
    }

    public void requestNetwork(NetworkRequest request, NetworkCallback networkCallback, int timeoutMs) {
        checkTimeout(timeoutMs);
        requestNetwork(request, networkCallback, timeoutMs, inferLegacyTypeForNetworkCapabilities(request.networkCapabilities), getDefaultHandler());
    }

    public void requestNetwork(NetworkRequest request, NetworkCallback networkCallback, Handler handler, int timeoutMs) {
        checkTimeout(timeoutMs);
        requestNetwork(request, networkCallback, timeoutMs, inferLegacyTypeForNetworkCapabilities(request.networkCapabilities), new CallbackHandler(this, handler));
    }

    public void requestNetwork(NetworkRequest request, PendingIntent operation) {
        checkPendingIntentNotNull(operation);
        try {
            this.mService.pendingRequestForNetwork(request.networkCapabilities, operation);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (ServiceSpecificException e2) {
            throw convertServiceException(e2);
        }
    }

    public void releaseNetworkRequest(PendingIntent operation) {
        checkPendingIntentNotNull(operation);
        try {
            this.mService.releasePendingNetworkRequest(operation);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private static void checkPendingIntentNotNull(PendingIntent intent) {
        Preconditions.checkNotNull(intent, "PendingIntent cannot be null.");
    }

    private static void checkCallbackNotNull(NetworkCallback callback) {
        Preconditions.checkNotNull(callback, "null NetworkCallback");
    }

    private static void checkTimeout(int timeoutMs) {
        Preconditions.checkArgumentPositive(timeoutMs, "timeoutMs must be strictly positive.");
    }

    public void registerNetworkCallback(NetworkRequest request, NetworkCallback networkCallback) {
        registerNetworkCallback(request, networkCallback, getDefaultHandler());
    }

    public void registerNetworkCallback(NetworkRequest request, NetworkCallback networkCallback, Handler handler) {
        sendRequestForNetwork(request.networkCapabilities, networkCallback, 0, 1, -1, new CallbackHandler(this, handler));
    }

    public void registerNetworkCallback(NetworkRequest request, PendingIntent operation) {
        checkPendingIntentNotNull(operation);
        try {
            this.mService.pendingListenForNetwork(request.networkCapabilities, operation);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (ServiceSpecificException e2) {
            throw convertServiceException(e2);
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
        checkCallbackNotNull(networkCallback);
        List<NetworkRequest> reqs = new ArrayList<>();
        synchronized (sCallbacks) {
            if (networkCallback.networkRequest == null) {
                NetworkRequest unused = networkCallback.networkRequest = ALREADY_UNREGISTERED;
                return;
            }
            boolean z = false;
            Preconditions.checkArgument(networkCallback.networkRequest != null, "NetworkCallback was not registered");
            if (networkCallback.networkRequest != ALREADY_UNREGISTERED) {
                z = true;
            }
            Preconditions.checkArgument(z, "NetworkCallback was already unregistered");
            for (Map.Entry<NetworkRequest, NetworkCallback> e : sCallbacks.entrySet()) {
                if (e.getValue() == networkCallback) {
                    reqs.add(e.getKey());
                }
            }
            for (NetworkRequest r : reqs) {
                try {
                    this.mService.releaseNetworkRequest(r);
                    sCallbacks.remove(r);
                } catch (RemoteException e2) {
                    throw e2.rethrowFromSystemServer();
                }
            }
            NetworkRequest unused2 = networkCallback.networkRequest = ALREADY_UNREGISTERED;
        }
    }

    public void unregisterNetworkCallback(PendingIntent operation) {
        checkPendingIntentNotNull(operation);
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
        if (this.mContext.checkCallingOrSelfPermission(Manifest.permission.USE_LEGACY_INTERFACE) != 0 && this.mContext.checkCallingOrSelfPermission("com.android.permission.INJECT_OMADM_SETTINGS") != 0) {
            unsupportedStartingFrom(23);
        }
    }

    @Deprecated
    public static boolean setProcessDefaultNetworkForHostResolution(Network network) {
        return NetworkUtils.bindProcessToNetworkForHostResolution(network == null ? 0 : network.netId);
    }

    private INetworkPolicyManager getNetworkPolicyManager() {
        synchronized (this) {
            if (this.mNPManager != null) {
                INetworkPolicyManager iNetworkPolicyManager = this.mNPManager;
                return iNetworkPolicyManager;
            }
            this.mNPManager = INetworkPolicyManager.Stub.asInterface(ServiceManager.getService(Context.NETWORK_POLICY_SERVICE));
            INetworkPolicyManager iNetworkPolicyManager2 = this.mNPManager;
            return iNetworkPolicyManager2;
        }
    }

    public int getRestrictBackgroundStatus() {
        try {
            return getNetworkPolicyManager().getRestrictBackgroundByCaller();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public byte[] getNetworkWatchlistConfigHash() {
        try {
            return this.mService.getNetworkWatchlistConfigHash();
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to get watchlist config hash");
            throw e.rethrowFromSystemServer();
        }
    }

    private static int getNewLastLegacyRequestId() {
        if (lastLegacyRequestId == Integer.MAX_VALUE) {
            lastLegacyRequestId = 0;
        }
        int i = lastLegacyRequestId;
        lastLegacyRequestId = i + 1;
        return i;
    }
}
