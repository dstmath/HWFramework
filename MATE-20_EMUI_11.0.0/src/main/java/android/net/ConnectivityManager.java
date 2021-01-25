package android.net;

import android.Manifest;
import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.INetworkPolicyManager;
import android.net.ISocketKeepaliveCallback;
import android.net.ITetheringEventCallback;
import android.net.IpSecManager;
import android.net.NetworkRequest;
import android.net.SocketKeepalive;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.INetworkActivityListener;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ServiceSpecificException;
import android.provider.Settings;
import android.security.keystore.KeyProperties;
import android.telephony.SubscriptionManager;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseIntArray;
import com.android.internal.R;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwConnectivityManagerConstants;
import com.android.internal.telephony.ITelephony;
import com.android.internal.util.FunctionalUtils;
import com.android.internal.util.Preconditions;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import libcore.net.event.NetworkEventDispatcher;

public class ConnectivityManager implements HwConnectivityManagerConstants {
    @Deprecated
    public static final String ACTION_BACKGROUND_DATA_SETTING_CHANGED = "android.net.conn.BACKGROUND_DATA_SETTING_CHANGED";
    public static final String ACTION_CAPTIVE_PORTAL_SIGN_IN = "android.net.conn.CAPTIVE_PORTAL";
    public static final String ACTION_CAPTIVE_PORTAL_TEST_COMPLETED = "android.net.conn.CAPTIVE_PORTAL_TEST_COMPLETED";
    public static final String ACTION_DATA_ACTIVITY_CHANGE = "android.net.conn.DATA_ACTIVITY_CHANGE";
    public static final String ACTION_LTEDATA_COMPLETED_ACTION = "android.net.wifi.LTEDATA_COMPLETED_ACTION";
    public static final String ACTION_PROMPT_LOST_VALIDATION = "android.net.conn.PROMPT_LOST_VALIDATION";
    public static final String ACTION_PROMPT_PARTIAL_CONNECTIVITY = "android.net.conn.PROMPT_PARTIAL_CONNECTIVITY";
    public static final String ACTION_PROMPT_UNVALIDATED = "android.net.conn.PROMPT_UNVALIDATED";
    public static final String ACTION_RESTRICT_BACKGROUND_CHANGED = "android.net.conn.RESTRICT_BACKGROUND_CHANGED";
    @UnsupportedAppUsage
    public static final String ACTION_TETHER_STATE_CHANGED = "android.net.conn.TETHER_STATE_CHANGED";
    private static final NetworkRequest ALREADY_UNREGISTERED = new NetworkRequest.Builder().clearCapabilities().build();
    private static final int BASE = 524288;
    public static final int CALLBACK_AVAILABLE = 524290;
    public static final int CALLBACK_BLK_CHANGED = 524299;
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
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    @Deprecated
    public static final int DEFAULT_NETWORK_PREFERENCE = 1;
    private static final int EXPIRE_LEGACY_REQUEST = 524296;
    public static final String EXTRA_ACTIVE_LOCAL_ONLY = "localOnlyArray";
    @UnsupportedAppUsage
    public static final String EXTRA_ACTIVE_TETHER = "tetherArray";
    public static final String EXTRA_ADD_TETHER_TYPE = "extraAddTetherType";
    @UnsupportedAppUsage
    public static final String EXTRA_AVAILABLE_TETHER = "availableArray";
    public static final String EXTRA_CAPTIVE_PORTAL = "android.net.extra.CAPTIVE_PORTAL";
    @SystemApi
    public static final String EXTRA_CAPTIVE_PORTAL_PROBE_SPEC = "android.net.extra.CAPTIVE_PORTAL_PROBE_SPEC";
    public static final String EXTRA_CAPTIVE_PORTAL_URL = "android.net.extra.CAPTIVE_PORTAL_URL";
    @SystemApi
    public static final String EXTRA_CAPTIVE_PORTAL_USER_AGENT = "android.net.extra.CAPTIVE_PORTAL_USER_AGENT";
    public static final String EXTRA_DEVICE_TYPE = "deviceType";
    @UnsupportedAppUsage
    public static final String EXTRA_ERRORED_TETHER = "erroredArray";
    @Deprecated
    public static final String EXTRA_EXTRA_INFO = "extraInfo";
    public static final String EXTRA_INET_CONDITION = "inetCondition";
    public static final String EXTRA_IS_ACTIVE = "isActive";
    public static final String EXTRA_IS_CAPTIVE_PORTAL = "captivePortal";
    @Deprecated
    public static final String EXTRA_IS_FAILOVER = "isFailover";
    public static final String EXTRA_IS_LTE_MOBILE_DATA_STATUS = "lte_mobile_data_status";
    public static final String EXTRA_NETWORK = "android.net.extra.NETWORK";
    @Deprecated
    public static final String EXTRA_NETWORK_INFO = "networkInfo";
    public static final String EXTRA_NETWORK_REQUEST = "android.net.extra.NETWORK_REQUEST";
    @Deprecated
    public static final String EXTRA_NETWORK_TYPE = "networkType";
    public static final String EXTRA_NO_CONNECTIVITY = "noConnectivity";
    @Deprecated
    public static final String EXTRA_OTHER_NETWORK_INFO = "otherNetwork";
    public static final String EXTRA_PROVISION_CALLBACK = "extraProvisionCallback";
    public static final String EXTRA_REALTIME_NS = "tsNanos";
    public static final String EXTRA_REASON = "reason";
    public static final String EXTRA_REM_TETHER_TYPE = "extraRemTetherType";
    public static final String EXTRA_RUN_PROVISION = "extraRunProvision";
    public static final String EXTRA_SET_ALARM = "extraSetAlarm";
    @UnsupportedAppUsage
    public static final String INET_CONDITION_ACTION = "android.net.conn.INET_CONDITION_ACTION";
    private static final int LISTEN = 1;
    public static final int LTE_STATE_CONNECTED = 1;
    public static final int LTE_STATE_CONNECTTING = 0;
    public static final int LTE_STATE_DISCONNECTED = 3;
    public static final int LTE_STATE_DISCONNECTTING = 2;
    public static final int MAX_NETWORK_TYPE = 54;
    private static final int MAX_NETWORK_TYPE_GOOGLE = 18;
    public static final int MAX_RADIO_TYPE = 18;
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
    public static final int TETHER_ERROR_DHCPSERVER_ERROR = 12;
    public static final int TETHER_ERROR_DISABLE_NAT_ERROR = 9;
    public static final int TETHER_ERROR_ENABLE_NAT_ERROR = 8;
    @SystemApi
    public static final int TETHER_ERROR_ENTITLEMENT_UNKONWN = 13;
    public static final int TETHER_ERROR_IFACE_CFG_ERROR = 10;
    public static final int TETHER_ERROR_MASTER_ERROR = 5;
    @SystemApi
    public static final int TETHER_ERROR_NO_ERROR = 0;
    @SystemApi
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
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 130143562)
    @Deprecated
    public static final int TYPE_MOBILE_CBS = 12;
    @Deprecated
    public static final int TYPE_MOBILE_DUN = 4;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 130143562)
    @Deprecated
    public static final int TYPE_MOBILE_EMERGENCY = 15;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 130143562)
    @Deprecated
    public static final int TYPE_MOBILE_FOTA = 10;
    @Deprecated
    public static final int TYPE_MOBILE_HIPRI = 5;
    @UnsupportedAppUsage
    @Deprecated
    public static final int TYPE_MOBILE_IA = 14;
    @UnsupportedAppUsage
    @Deprecated
    public static final int TYPE_MOBILE_IMS = 11;
    @Deprecated
    public static final int TYPE_MOBILE_MMS = 2;
    @Deprecated
    public static final int TYPE_MOBILE_SUPL = 3;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 130143562)
    public static final int TYPE_NONE = -1;
    @UnsupportedAppUsage
    @Deprecated
    public static final int TYPE_PROXY = 16;
    @Deprecated
    public static final int TYPE_TEST = 18;
    @Deprecated
    public static final int TYPE_VPN = 17;
    @Deprecated
    public static final int TYPE_WIFI = 1;
    @UnsupportedAppUsage
    @Deprecated
    public static final int TYPE_WIFI_P2P = 13;
    @Deprecated
    public static final int TYPE_WIMAX = 6;
    private static int lastLegacyRequestId = 0;
    private static CallbackHandler sCallbackHandler;
    private static final HashMap<NetworkRequest, NetworkCallback> sCallbacks = new HashMap<>();
    private static ConnectivityManager sInstance;
    @UnsupportedAppUsage
    private static final HashMap<NetworkCapabilities, LegacyRequest> sLegacyRequests = new HashMap<>();
    private static final SparseIntArray sLegacyTypeToCapability = new SparseIntArray();
    private static final SparseIntArray sLegacyTypeToTransport = new SparseIntArray();
    private final Context mContext;
    HwCustConnectivityManager mCust = ((HwCustConnectivityManager) HwCustUtils.createObj(HwCustConnectivityManager.class, new Object[0]));
    private INetworkManagementService mNMService;
    private INetworkPolicyManager mNPManager;
    private final ArrayMap<OnNetworkActiveListener, INetworkActivityListener> mNetworkActivityListeners = new ArrayMap<>();
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 130143562)
    private final IConnectivityManager mService;
    @GuardedBy({"mTetheringEventCallbacks"})
    private final ArrayMap<OnTetheringEventCallback, ITetheringEventCallback> mTetheringEventCallbacks = new ArrayMap<>();

    @Retention(RetentionPolicy.SOURCE)
    public @interface EntitlementResultCode {
    }

    public interface Errors {
        public static final int TOO_MANY_REQUESTS = 1;
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface MultipathPreference {
    }

    public interface OnNetworkActiveListener {
        void onNetworkActive();
    }

    @SystemApi
    public interface OnTetheringEntitlementResultListener {
        void onTetheringEntitlementResult(int i);
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
        return (networkType >= 0 && networkType <= 18) || (networkType >= 34 && networkType <= 45) || ((networkType >= 46 && networkType <= 47) || networkType == 48 || (networkType >= 49 && networkType <= 54));
    }

    @UnsupportedAppUsage
    @Deprecated
    public static String getNetworkTypeName(int type) {
        switch (type) {
            case -1:
                return KeyProperties.DIGEST_NONE;
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

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 130143562)
    @Deprecated
    public static boolean isNetworkTypeMobile(int networkType) {
        if (networkType == 0 || networkType == 2 || networkType == 3 || networkType == 4 || networkType == 5 || networkType == 14 || networkType == 15) {
            return true;
        }
        switch (networkType) {
            case 10:
            case 11:
            case 12:
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

    @Deprecated
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

    public boolean setAlwaysOnVpnPackageForUser(int userId, String vpnPackage, boolean lockdownEnabled, List<String> lockdownWhitelist) {
        try {
            return this.mService.setAlwaysOnVpnPackage(userId, vpnPackage, lockdownEnabled, lockdownWhitelist);
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

    public boolean isVpnLockdownEnabled(int userId) {
        try {
            return this.mService.isVpnLockdownEnabled(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<String> getVpnLockdownWhitelist(int userId) {
        try {
            return this.mService.getVpnLockdownWhitelist(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
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

    @Deprecated
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

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    public NetworkCapabilities[] getDefaultNetworkCapabilitiesForUser(int userId) {
        try {
            return this.mService.getDefaultNetworkCapabilitiesForUser(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 109783091)
    public LinkProperties getActiveLinkProperties() {
        try {
            return this.mService.getActiveLinkProperties();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 130143562)
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

    @Deprecated
    public int startUsingNetworkFeature(int networkType, String feature) {
        NetworkRequest request;
        checkLegacyRoutingApiAccess();
        NetworkCapabilities netCap = networkCapabilitiesForFeature(networkType, feature);
        if (netCap == null) {
            Log.i(TAG, "Can't satisfy startUsingNetworkFeature for " + networkType + ", " + feature);
            return 3;
        }
        HwCustConnectivityManager hwCustConnectivityManager = this.mCust;
        if (hwCustConnectivityManager != null && hwCustConnectivityManager.enforceStartUsingNetworkFeaturePermissionFail(this.mContext, legacyTypeForNetworkCapabilities(netCap))) {
            return 3;
        }
        HwFrameworkFactory.getHwInnerConnectivityManager().checkHwFeature(feature, netCap, networkType);
        synchronized (sLegacyRequests) {
            LegacyRequest l = sLegacyRequests.get(netCap);
            if (l != null) {
                Log.i(TAG, "renewing startUsingNetworkFeature request " + l.networkRequest);
                renewRequestLocked(l);
                if (l.currentNetwork != null) {
                    return 0;
                }
                return 1;
            }
            request = requestNetworkForFeatureLocked(netCap);
        }
        if (request != null) {
            Log.i(TAG, "starting startUsingNetworkFeature for request " + request);
            return 1;
        }
        Log.i(TAG, " request Failed");
        return 3;
    }

    @Deprecated
    public int stopUsingNetworkFeature(int networkType, String feature) {
        checkLegacyRoutingApiAccess();
        NetworkCapabilities netCap = networkCapabilitiesForFeature(networkType, feature);
        if (netCap == null) {
            Log.i(TAG, "Can't satisfy stopUsingNetworkFeature for " + networkType + ", " + feature);
            return -1;
        }
        HwFrameworkFactory.getHwInnerConnectivityManager().checkHwFeature(feature, netCap, networkType);
        if (!removeRequestForFeature(netCap)) {
            return 1;
        }
        Log.i(TAG, "stopUsingNetworkFeature for " + networkType + ", " + feature);
        return 1;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0061, code lost:
        if (r11.equals("enableDUN") != false) goto L_0x0079;
     */
    @UnsupportedAppUsage
    private NetworkCapabilities networkCapabilitiesForFeature(int networkType, String feature) {
        char c = 1;
        if (networkType == 0) {
            String[] result = HwFrameworkFactory.getHwInnerConnectivityManager().getFeature(feature);
            if (result[0] != null && !"".equals(result[0])) {
                feature = result[0];
            }
            switch (feature.hashCode()) {
                case -1451370941:
                    if (feature.equals(HuaweiTelephonyConfigs.FEATURE_ENABLE_HIPRI)) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -631682191:
                    if (feature.equals("enableCBS")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case -631680646:
                    break;
                case -631676084:
                    if (feature.equals("enableIMS")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case -631672240:
                    if (feature.equals(HuaweiTelephonyConfigs.FEATURE_ENABLE_MMS)) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case 1892790521:
                    if (feature.equals("enableFOTA")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 1893183457:
                    if (feature.equals(HuaweiTelephonyConfigs.FEATURE_ENABLE_SUPL)) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case 1998933033:
                    if (feature.equals("enableDUNAlways")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
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

    private int inferLegacyTypeForNetworkCapabilities(NetworkCapabilities netCap) {
        int result;
        String type;
        NetworkCapabilities testCap;
        HwCustConnectivityManager hwCustConnectivityManager;
        if (netCap == null || !netCap.hasTransport(0)) {
            return -1;
        }
        if (!netCap.hasCapability(1) && ((hwCustConnectivityManager = this.mCust) == null || !hwCustConnectivityManager.canHandleEimsNetworkCapabilities(netCap))) {
            return -1;
        }
        String type2 = null;
        int result2 = -1;
        if (!netCap.hasCapability(5)) {
            if (!netCap.hasCapability(4)) {
                if (!netCap.hasCapability(3)) {
                    if (!netCap.hasCapability(2)) {
                        if (!netCap.hasCapability(1)) {
                            if (!netCap.hasCapability(12)) {
                                Iterator<Map.Entry<String, Integer>> it = HwFrameworkFactory.getHwInnerConnectivityManager().inferLegacyTypeForNetworkCapabilitiesEx(netCap, this.mCust, sLegacyTypeToTransport, sLegacyTypeToCapability).entrySet().iterator();
                                while (true) {
                                    if (!it.hasNext()) {
                                        type = type2;
                                        result = result2;
                                        break;
                                    }
                                    Map.Entry<String, Integer> entry = it.next();
                                    type2 = entry.getKey();
                                    result2 = entry.getValue().intValue();
                                    if (type2 == null) {
                                        type = type2;
                                        result = result2;
                                        break;
                                    }
                                }
                            } else {
                                type = HuaweiTelephonyConfigs.FEATURE_ENABLE_HIPRI;
                                result = 5;
                            }
                        } else {
                            type = HuaweiTelephonyConfigs.FEATURE_ENABLE_SUPL;
                            result = 3;
                        }
                    } else {
                        type = "enableDUN";
                        result = 4;
                    }
                } else {
                    type = "enableFOTA";
                    result = 10;
                }
            } else {
                type = "enableIMS";
                result = 11;
            }
        } else {
            type = "enableCBS";
            result = 12;
        }
        if (type == null || (testCap = networkCapabilitiesForFeature(0, type)) == null || !testCap.equalsNetCapabilities(netCap) || !testCap.equalsTransportTypes(netCap)) {
            return -1;
        }
        return result;
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

    /* access modifiers changed from: private */
    public static class LegacyRequest {
        Network currentNetwork;
        int delay;
        int expireSequenceNumber;
        NetworkCallback networkCallback;
        NetworkCapabilities networkCapabilities;
        NetworkRequest networkRequest;

        private LegacyRequest() {
            this.delay = -1;
            this.networkCallback = new NetworkCallback() {
                /* class android.net.ConnectivityManager.LegacyRequest.AnonymousClass1 */

                @Override // android.net.ConnectivityManager.NetworkCallback
                public void onAvailable(Network network) {
                    LegacyRequest.this.currentNetwork = network;
                    Log.i(ConnectivityManager.TAG, "startUsingNetworkFeature got Network:" + network);
                    ConnectivityManager.setProcessDefaultNetworkForHostResolution(network);
                }

                @Override // android.net.ConnectivityManager.NetworkCallback
                public void onLost(Network network) {
                    if (network.equals(LegacyRequest.this.currentNetwork)) {
                        LegacyRequest.this.clearDnsBinding();
                    }
                    Log.i(ConnectivityManager.TAG, "startUsingNetworkFeature lost Network:" + network);
                }
            };
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearDnsBinding() {
            if (this.currentNetwork != null) {
                this.currentNetwork = null;
                ConnectivityManager.setProcessDefaultNetworkForHostResolution(null);
            }
        }
    }

    private NetworkRequest findRequestForFeature(NetworkCapabilities netCap) {
        synchronized (sLegacyRequests) {
            LegacyRequest l = sLegacyRequests.get(netCap);
            if (l == null) {
                return null;
            }
            return l.networkRequest;
        }
    }

    private void renewRequestLocked(LegacyRequest l) {
        l.expireSequenceNumber = getNewLastLegacyRequestId();
        Log.i(TAG, "renewing request to seqNum " + l.expireSequenceNumber);
        sendExpireMsgForFeature(l.networkCapabilities, l.expireSequenceNumber, l.delay);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void expireRequest(NetworkCapabilities netCap, int sequenceNum) {
        synchronized (sLegacyRequests) {
            LegacyRequest l = sLegacyRequests.get(netCap);
            if (l != null) {
                int ourSeqNum = l.expireSequenceNumber;
                if (l.expireSequenceNumber == sequenceNum) {
                    removeRequestForFeature(netCap);
                }
                Log.i(TAG, "expireRequest with " + ourSeqNum + ", " + sequenceNum);
            }
        }
    }

    @UnsupportedAppUsage
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
            Log.i(TAG, "sending expire msg with seqNum " + seqNum + " and delay " + delay);
            CallbackHandler handler = getDefaultHandler();
            handler.sendMessageDelayed(handler.obtainMessage(EXPIRE_LEGACY_REQUEST, seqNum, 0, netCap), (long) delay);
        }
    }

    @UnsupportedAppUsage
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

    public static class PacketKeepaliveCallback {
        @UnsupportedAppUsage
        public void onStarted() {
        }

        @UnsupportedAppUsage
        public void onStopped() {
        }

        @UnsupportedAppUsage
        public void onError(int error) {
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
        private final ISocketKeepaliveCallback mCallback;
        private final ExecutorService mExecutor;
        private final Network mNetwork;
        private volatile Integer mSlot;

        @UnsupportedAppUsage
        public void stop() {
            try {
                this.mExecutor.execute(new Runnable() {
                    /* class android.net.$$Lambda$ConnectivityManager$PacketKeepalive$8nwufwzyblnuYRFEYIKx7L4Vg */

                    @Override // java.lang.Runnable
                    public final void run() {
                        ConnectivityManager.PacketKeepalive.this.lambda$stop$0$ConnectivityManager$PacketKeepalive();
                    }
                });
            } catch (RejectedExecutionException e) {
            }
        }

        public /* synthetic */ void lambda$stop$0$ConnectivityManager$PacketKeepalive() {
            try {
                if (this.mSlot != null) {
                    ConnectivityManager.this.mService.stopKeepalive(this.mNetwork, this.mSlot.intValue());
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Error stopping packet keepalive: ", e);
                throw e.rethrowFromSystemServer();
            }
        }

        private PacketKeepalive(Network network, final PacketKeepaliveCallback callback) {
            Preconditions.checkNotNull(network, "network cannot be null");
            Preconditions.checkNotNull(callback, "callback cannot be null");
            this.mNetwork = network;
            this.mExecutor = Executors.newSingleThreadExecutor();
            this.mCallback = new ISocketKeepaliveCallback.Stub(ConnectivityManager.this) {
                /* class android.net.ConnectivityManager.PacketKeepalive.AnonymousClass1 */

                @Override // android.net.ISocketKeepaliveCallback
                public void onStarted(int slot) {
                    Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(slot, callback) {
                        /* class android.net.$$Lambda$ConnectivityManager$PacketKeepalive$1$iOtsqOYp69ztB6u3PYNuiI_PGo */
                        private final /* synthetic */ int f$1;
                        private final /* synthetic */ ConnectivityManager.PacketKeepaliveCallback f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                        public final void runOrThrow() {
                            ConnectivityManager.PacketKeepalive.AnonymousClass1.this.lambda$onStarted$1$ConnectivityManager$PacketKeepalive$1(this.f$1, this.f$2);
                        }
                    });
                }

                public /* synthetic */ void lambda$onStarted$1$ConnectivityManager$PacketKeepalive$1(int slot, PacketKeepaliveCallback callback) throws Exception {
                    PacketKeepalive.this.mExecutor.execute(new Runnable(slot, callback) {
                        /* class android.net.$$Lambda$ConnectivityManager$PacketKeepalive$1$NfMgP6Nh6Ep6LcaiJ10o_zBccII */
                        private final /* synthetic */ int f$1;
                        private final /* synthetic */ ConnectivityManager.PacketKeepaliveCallback f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            ConnectivityManager.PacketKeepalive.AnonymousClass1.this.lambda$onStarted$0$ConnectivityManager$PacketKeepalive$1(this.f$1, this.f$2);
                        }
                    });
                }

                public /* synthetic */ void lambda$onStarted$0$ConnectivityManager$PacketKeepalive$1(int slot, PacketKeepaliveCallback callback) {
                    PacketKeepalive.this.mSlot = Integer.valueOf(slot);
                    callback.onStarted();
                }

                @Override // android.net.ISocketKeepaliveCallback
                public void onStopped() {
                    Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(callback) {
                        /* class android.net.$$Lambda$ConnectivityManager$PacketKeepalive$1$H5tzn67t3ydWL8tXpl9UyOmDcc */
                        private final /* synthetic */ ConnectivityManager.PacketKeepaliveCallback f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                        public final void runOrThrow() {
                            ConnectivityManager.PacketKeepalive.AnonymousClass1.this.lambda$onStopped$3$ConnectivityManager$PacketKeepalive$1(this.f$1);
                        }
                    });
                    PacketKeepalive.this.mExecutor.shutdown();
                }

                public /* synthetic */ void lambda$onStopped$3$ConnectivityManager$PacketKeepalive$1(PacketKeepaliveCallback callback) throws Exception {
                    PacketKeepalive.this.mExecutor.execute(new Runnable(callback) {
                        /* class android.net.$$Lambda$ConnectivityManager$PacketKeepalive$1$WmmtbYWlzqLV8wWUDKe3CWjvy0 */
                        private final /* synthetic */ ConnectivityManager.PacketKeepaliveCallback f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            ConnectivityManager.PacketKeepalive.AnonymousClass1.this.lambda$onStopped$2$ConnectivityManager$PacketKeepalive$1(this.f$1);
                        }
                    });
                }

                public /* synthetic */ void lambda$onStopped$2$ConnectivityManager$PacketKeepalive$1(PacketKeepaliveCallback callback) {
                    PacketKeepalive.this.mSlot = null;
                    callback.onStopped();
                }

                @Override // android.net.ISocketKeepaliveCallback
                public void onError(int error) {
                    Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(callback, error) {
                        /* class android.net.$$Lambda$ConnectivityManager$PacketKeepalive$1$nt5Pgsn85fhX6h9EJ0eAK_PXAjU */
                        private final /* synthetic */ ConnectivityManager.PacketKeepaliveCallback f$1;
                        private final /* synthetic */ int f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                        public final void runOrThrow() {
                            ConnectivityManager.PacketKeepalive.AnonymousClass1.this.lambda$onError$5$ConnectivityManager$PacketKeepalive$1(this.f$1, this.f$2);
                        }
                    });
                    PacketKeepalive.this.mExecutor.shutdown();
                }

                public /* synthetic */ void lambda$onError$5$ConnectivityManager$PacketKeepalive$1(PacketKeepaliveCallback callback, int error) throws Exception {
                    PacketKeepalive.this.mExecutor.execute(new Runnable(callback, error) {
                        /* class android.net.$$Lambda$ConnectivityManager$PacketKeepalive$1$JWcQQZv8Qrs81cZBMAOZZ8MUeU */
                        private final /* synthetic */ ConnectivityManager.PacketKeepaliveCallback f$1;
                        private final /* synthetic */ int f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            ConnectivityManager.PacketKeepalive.AnonymousClass1.this.lambda$onError$4$ConnectivityManager$PacketKeepalive$1(this.f$1, this.f$2);
                        }
                    });
                }

                public /* synthetic */ void lambda$onError$4$ConnectivityManager$PacketKeepalive$1(PacketKeepaliveCallback callback, int error) {
                    PacketKeepalive.this.mSlot = null;
                    callback.onError(error);
                }

                @Override // android.net.ISocketKeepaliveCallback
                public void onDataReceived() {
                }
            };
        }
    }

    @UnsupportedAppUsage
    public PacketKeepalive startNattKeepalive(Network network, int intervalSeconds, PacketKeepaliveCallback callback, InetAddress srcAddr, int srcPort, InetAddress dstAddr) {
        PacketKeepalive k = new PacketKeepalive(network, callback);
        try {
            this.mService.startNattKeepalive(network, intervalSeconds, k.mCallback, srcAddr.getHostAddress(), srcPort, dstAddr.getHostAddress());
            return k;
        } catch (RemoteException e) {
            Log.e(TAG, "Error starting packet keepalive: ", e);
            throw e.rethrowFromSystemServer();
        }
    }

    public SocketKeepalive createSocketKeepalive(Network network, IpSecManager.UdpEncapsulationSocket socket, InetAddress source, InetAddress destination, Executor executor, SocketKeepalive.Callback callback) {
        ParcelFileDescriptor dup;
        try {
            dup = ParcelFileDescriptor.dup(socket.getFileDescriptor());
        } catch (IOException e) {
            dup = new ParcelFileDescriptor(new FileDescriptor());
        }
        return new NattSocketKeepalive(this.mService, network, dup, socket.getResourceId(), source, destination, executor, callback);
    }

    @SystemApi
    public SocketKeepalive createNattKeepalive(Network network, ParcelFileDescriptor pfd, InetAddress source, InetAddress destination, Executor executor, SocketKeepalive.Callback callback) {
        ParcelFileDescriptor dup;
        try {
            dup = pfd.dup();
        } catch (IOException e) {
            dup = new ParcelFileDescriptor(new FileDescriptor());
        }
        return new NattSocketKeepalive(this.mService, network, dup, -1, source, destination, executor, callback);
    }

    @SystemApi
    public SocketKeepalive createSocketKeepalive(Network network, Socket socket, Executor executor, SocketKeepalive.Callback callback) {
        ParcelFileDescriptor dup;
        try {
            dup = ParcelFileDescriptor.fromSocket(socket);
        } catch (UncheckedIOException e) {
            dup = new ParcelFileDescriptor(new FileDescriptor());
        }
        return new TcpSocketKeepalive(this.mService, network, dup, executor, callback);
    }

    @Deprecated
    public boolean requestRouteToHost(int networkType, int hostAddress) {
        return requestRouteToHostAddress(networkType, NetworkUtils.intToInetAddress(hostAddress));
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    @Deprecated
    public void setBackgroundDataSetting(boolean allowBackgroundData) {
    }

    @UnsupportedAppUsage
    @Deprecated
    public NetworkQuotaInfo getActiveNetworkQuotaInfo() {
        try {
            return this.mService.getActiveNetworkQuotaInfo();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    @Deprecated
    public boolean getMobileDataEnabled() {
        IBinder b = ServiceManager.getService("phone");
        if (b != null) {
            try {
                ITelephony it = ITelephony.Stub.asInterface(b);
                int subId = SubscriptionManager.getDefaultDataSubscriptionId();
                Log.i(TAG, "getMobileDataEnabled()+ subId=" + subId);
                boolean retVal = it.isUserDataEnabled(subId);
                Log.i(TAG, "getMobileDataEnabled()- subId=" + subId + " retVal=" + retVal);
                return retVal;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.i(TAG, "getMobileDataEnabled()- remote exception retVal=false");
            return false;
        }
    }

    private INetworkManagementService getNetworkManagementService() {
        synchronized (this) {
            if (this.mNMService != null) {
                return this.mNMService;
            }
            this.mNMService = INetworkManagementService.Stub.asInterface(ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
            return this.mNMService;
        }
    }

    public void addDefaultNetworkActiveListener(final OnNetworkActiveListener l) {
        INetworkActivityListener rl = new INetworkActivityListener.Stub() {
            /* class android.net.ConnectivityManager.AnonymousClass1 */

            @Override // android.os.INetworkActivityListener
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

    @UnsupportedAppUsage
    public static ConnectivityManager from(Context context) {
        return (ConnectivityManager) context.getSystemService("connectivity");
    }

    public NetworkRequest getDefaultRequest() {
        try {
            return this.mService.getDefaultRequest();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
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

    @UnsupportedAppUsage
    @Deprecated
    private static ConnectivityManager getInstance() {
        if (getInstanceOrNull() != null) {
            return getInstanceOrNull();
        }
        throw new IllegalStateException("No ConnectivityManager yet constructed");
    }

    @UnsupportedAppUsage
    public String[] getTetherableIfaces() {
        try {
            return this.mService.getTetherableIfaces();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public String[] getTetheredIfaces() {
        try {
            return this.mService.getTetheredIfaces();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    public int tether(String iface) {
        try {
            String pkgName = this.mContext.getOpPackageName();
            Log.i(TAG, "tether caller:" + pkgName);
            return this.mService.tether(iface, pkgName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
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
    public static abstract class OnStartTetheringCallback {
        public void onTetheringStarted() {
        }

        public void onTetheringFailed() {
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
            /* class android.net.ConnectivityManager.AnonymousClass2 */

            /* access modifiers changed from: protected */
            @Override // android.os.ResultReceiver
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

    @SystemApi
    public static abstract class OnTetheringEventCallback {
        public void onUpstreamChanged(Network network) {
        }
    }

    @SystemApi
    public void registerTetheringEventCallback(final Executor executor, final OnTetheringEventCallback callback) {
        Preconditions.checkNotNull(callback, "OnTetheringEventCallback cannot be null.");
        synchronized (this.mTetheringEventCallbacks) {
            Preconditions.checkArgument(!this.mTetheringEventCallbacks.containsKey(callback), "callback was already registered.");
            ITetheringEventCallback remoteCallback = new ITetheringEventCallback.Stub() {
                /* class android.net.ConnectivityManager.AnonymousClass3 */

                @Override // android.net.ITetheringEventCallback
                public void onUpstreamChanged(Network network) throws RemoteException {
                    Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(executor, callback, network) {
                        /* class android.net.$$Lambda$ConnectivityManager$3$BfAvTRJTF0an2PdeqkENEBULYBU */
                        private final /* synthetic */ Executor f$0;
                        private final /* synthetic */ ConnectivityManager.OnTetheringEventCallback f$1;
                        private final /* synthetic */ Network f$2;

                        {
                            this.f$0 = r1;
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                        public final void runOrThrow() {
                            this.f$0.execute(new Runnable(this.f$2) {
                                /* class android.net.$$Lambda$ConnectivityManager$3$Hh_etCAvVs2IV58umWLOd1O4yk */
                                private final /* synthetic */ Network f$1;

                                {
                                    this.f$1 = r2;
                                }

                                @Override // java.lang.Runnable
                                public final void run() {
                                    ConnectivityManager.OnTetheringEventCallback.this.onUpstreamChanged(this.f$1);
                                }
                            });
                        }
                    });
                }
            };
            try {
                String pkgName = this.mContext.getOpPackageName();
                Log.i(TAG, "registerTetheringUpstreamCallback:" + pkgName);
                this.mService.registerTetheringEventCallback(remoteCallback, pkgName);
                this.mTetheringEventCallbacks.put(callback, remoteCallback);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @SystemApi
    public void unregisterTetheringEventCallback(OnTetheringEventCallback callback) {
        synchronized (this.mTetheringEventCallbacks) {
            ITetheringEventCallback remoteCallback = this.mTetheringEventCallbacks.remove(callback);
            Preconditions.checkNotNull(remoteCallback, "callback was not registered.");
            try {
                String pkgName = this.mContext.getOpPackageName();
                Log.i(TAG, "unregisterTetheringEventCallback:" + pkgName);
                this.mService.unregisterTetheringEventCallback(remoteCallback, pkgName);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @UnsupportedAppUsage
    public String[] getTetherableUsbRegexs() {
        try {
            return this.mService.getTetherableUsbRegexs();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public String[] getTetherableWifiRegexs() {
        try {
            return this.mService.getTetherableWifiRegexs();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public String[] getTetherableBluetoothRegexs() {
        try {
            return this.mService.getTetherableBluetoothRegexs();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public int setUsbTethering(boolean enable) {
        try {
            String pkgName = this.mContext.getOpPackageName();
            Log.i(TAG, "setUsbTethering caller:" + pkgName);
            return this.mService.setUsbTethering(enable, pkgName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public int getLastTetherError(String iface) {
        try {
            return this.mService.getLastTetherError(iface);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void getLatestTetheringEntitlementResult(int type, boolean showEntitlementUi, final Executor executor, final OnTetheringEntitlementResultListener listener) {
        Preconditions.checkNotNull(listener, "TetheringEntitlementResultListener cannot be null.");
        ResultReceiver wrappedListener = new ResultReceiver(null) {
            /* class android.net.ConnectivityManager.AnonymousClass4 */

            /* access modifiers changed from: protected */
            @Override // android.os.ResultReceiver
            public void onReceiveResult(int resultCode, Bundle resultData) {
                Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(executor, listener, resultCode) {
                    /* class android.net.$$Lambda$ConnectivityManager$4$Jku9vR1DwqMOUorHyaTIOdhOAs */
                    private final /* synthetic */ Executor f$0;
                    private final /* synthetic */ ConnectivityManager.OnTetheringEntitlementResultListener f$1;
                    private final /* synthetic */ int f$2;

                    {
                        this.f$0 = r1;
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                    public final void runOrThrow() {
                        this.f$0.execute(new Runnable(this.f$2) {
                            /* class android.net.$$Lambda$ConnectivityManager$4$GbcJVaUJXpIrYQi94EYHYBwTJI */
                            private final /* synthetic */ int f$1;

                            {
                                this.f$1 = r2;
                            }

                            @Override // java.lang.Runnable
                            public final void run() {
                                ConnectivityManager.OnTetheringEntitlementResultListener.this.onTetheringEntitlementResult(this.f$1);
                            }
                        });
                    }
                });
            }
        };
        try {
            String pkgName = this.mContext.getOpPackageName();
            Log.i(TAG, "getLatestTetheringEntitlementResult:" + pkgName);
            this.mService.getLatestTetheringEntitlementResult(type, wrappedListener, showEntitlementUi, pkgName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void reportInetCondition(int networkType, int percentage) {
        printStackTrace();
        try {
            this.mService.reportInetCondition(networkType, percentage);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void reportBadNetwork(Network network) {
        printStackTrace();
        try {
            this.mService.reportNetworkConnectivity(network, true);
            this.mService.reportNetworkConnectivity(network, false);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void reportNetworkConnectivity(Network network, boolean hasConnectivity) {
        printStackTrace();
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

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 130143562)
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

    @SystemApi
    public void setAirplaneMode(boolean enable) {
        try {
            this.mService.setAirplaneMode(enable);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public int registerNetworkFactory(Messenger messenger, String name) {
        try {
            return this.mService.registerNetworkFactory(messenger, name);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public void unregisterNetworkFactory(Messenger messenger) {
        try {
            this.mService.unregisterNetworkFactory(messenger);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int registerNetworkAgent(Messenger messenger, NetworkInfo ni, LinkProperties lp, NetworkCapabilities nc, int score, NetworkMisc misc) {
        return registerNetworkAgent(messenger, ni, lp, nc, score, misc, -1);
    }

    public int registerNetworkAgent(Messenger messenger, NetworkInfo ni, LinkProperties lp, NetworkCapabilities nc, int score, NetworkMisc misc, int factorySerialNumber) {
        try {
            return this.mService.registerNetworkAgent(messenger, ni, lp, nc, score, misc, factorySerialNumber);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static class NetworkCallback {
        private NetworkRequest networkRequest;

        public void onPreCheck(Network network) {
        }

        public void onAvailable(Network network, NetworkCapabilities networkCapabilities, LinkProperties linkProperties, boolean blocked) {
            onAvailable(network);
            if (!networkCapabilities.hasCapability(21)) {
                onNetworkSuspended(network);
            }
            onCapabilitiesChanged(network, networkCapabilities);
            onLinkPropertiesChanged(network, linkProperties);
            onBlockedStatusChanged(network, blocked);
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

        public void onBlockedStatusChanged(Network network, boolean blocked) {
        }

        public NetworkRequest getNetworkRequest() {
            return this.networkRequest;
        }

        public void setNetworkRequest(NetworkRequest networkRequest2) {
            this.networkRequest = networkRequest2;
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
            case CALLBACK_PRECHECK /* 524289 */:
                return "CALLBACK_PRECHECK";
            case 524290:
                return "CALLBACK_AVAILABLE";
            case 524291:
                return "CALLBACK_LOSING";
            case 524292:
                return "CALLBACK_LOST";
            case CALLBACK_UNAVAIL /* 524293 */:
                return "CALLBACK_UNAVAIL";
            case CALLBACK_CAP_CHANGED /* 524294 */:
                return "CALLBACK_CAP_CHANGED";
            case CALLBACK_IP_CHANGED /* 524295 */:
                return "CALLBACK_IP_CHANGED";
            case EXPIRE_LEGACY_REQUEST /* 524296 */:
                return "EXPIRE_LEGACY_REQUEST";
            case CALLBACK_SUSPENDED /* 524297 */:
                return "CALLBACK_SUSPENDED";
            case CALLBACK_RESUMED /* 524298 */:
                return "CALLBACK_RESUMED";
            case CALLBACK_BLK_CHANGED /* 524299 */:
                return "CALLBACK_BLK_CHANGED";
            default:
                return Integer.toString(whichCallback);
        }
    }

    /* access modifiers changed from: private */
    public class CallbackHandler extends Handler {
        private static final boolean DBG = false;
        private static final String TAG = "ConnectivityManager.CallbackHandler";

        CallbackHandler(Looper looper) {
            super(looper);
        }

        CallbackHandler(ConnectivityManager connectivityManager, Handler handler) {
            this(((Handler) Preconditions.checkNotNull(handler, "Handler cannot be null.")).getLooper());
        }

        @Override // android.os.Handler
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
                if (callback == null) {
                    Log.w(TAG, "callback not found for " + ConnectivityManager.getCallbackName(message.what) + " message");
                    return;
                } else if (message.what == 524293) {
                    ConnectivityManager.sCallbacks.remove(request);
                    callback.networkRequest = ConnectivityManager.ALREADY_UNREGISTERED;
                }
            }
            boolean blocked = true;
            switch (message.what) {
                case ConnectivityManager.CALLBACK_PRECHECK /* 524289 */:
                    callback.onPreCheck(network);
                    return;
                case 524290:
                    NetworkCapabilities cap = (NetworkCapabilities) getObject(message, NetworkCapabilities.class);
                    LinkProperties lp = (LinkProperties) getObject(message, LinkProperties.class);
                    if (message.arg1 == 0) {
                        blocked = false;
                    }
                    callback.onAvailable(network, cap, lp, blocked);
                    return;
                case 524291:
                    callback.onLosing(network, message.arg1);
                    return;
                case 524292:
                    callback.onLost(network);
                    return;
                case ConnectivityManager.CALLBACK_UNAVAIL /* 524293 */:
                    callback.onUnavailable();
                    return;
                case ConnectivityManager.CALLBACK_CAP_CHANGED /* 524294 */:
                    callback.onCapabilitiesChanged(network, (NetworkCapabilities) getObject(message, NetworkCapabilities.class));
                    return;
                case ConnectivityManager.CALLBACK_IP_CHANGED /* 524295 */:
                    callback.onLinkPropertiesChanged(network, (LinkProperties) getObject(message, LinkProperties.class));
                    return;
                case ConnectivityManager.EXPIRE_LEGACY_REQUEST /* 524296 */:
                default:
                    return;
                case ConnectivityManager.CALLBACK_SUSPENDED /* 524297 */:
                    callback.onNetworkSuspended(network);
                    return;
                case ConnectivityManager.CALLBACK_RESUMED /* 524298 */:
                    callback.onNetworkResumed(network);
                    return;
                case ConnectivityManager.CALLBACK_BLK_CHANGED /* 524299 */:
                    if (message.arg1 == 0) {
                        blocked = false;
                    }
                    callback.onBlockedStatusChanged(network, blocked);
                    return;
            }
        }

        private <T> T getObject(Message msg, Class<T> c) {
            return (T) msg.getData().getParcelable(c.getSimpleName());
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
        RemoteException e;
        ServiceSpecificException e2;
        NetworkRequest request;
        printStackTrace();
        checkCallbackNotNull(callback);
        Preconditions.checkArgument(action == 2 || need != null, "null NetworkCapabilities");
        HwCustConnectivityManager hwCustConnectivityManager = this.mCust;
        if (hwCustConnectivityManager != null && hwCustConnectivityManager.isSubInactiveFromNetworkCapabilities(need) && (HuaweiTelephonyConfigs.isHisiPlatform() || HuaweiTelephonyConfigs.isMTKPlatform())) {
            return null;
        }
        HwCustConnectivityManager hwCustConnectivityManager2 = this.mCust;
        if (hwCustConnectivityManager2 == null || (!hwCustConnectivityManager2.isDisableRequestBySIM2(need) && !this.mCust.isBlockNetworkRequest(need))) {
            try {
                synchronized (sCallbacks) {
                    try {
                        if (!(callback.networkRequest == null || callback.networkRequest == ALREADY_UNREGISTERED)) {
                            Log.e(TAG, "NetworkCallback was already registered");
                        }
                        Messenger messenger = new Messenger(handler);
                        Binder binder = new Binder();
                        if (action == 1) {
                            request = this.mService.listenForNetwork(need, messenger, binder);
                        } else {
                            String pkgName = this.mContext.getOpPackageName();
                            Log.i(TAG, "requestNetwork and the calling app is: " + pkgName);
                            request = this.mService.requestNetwork(need, messenger, timeoutMs, binder, legacyType);
                        }
                        if (request != null) {
                            sCallbacks.put(request, callback);
                        }
                        callback.networkRequest = request;
                        return request;
                    } catch (Throwable th) {
                        th = th;
                    }
                }
                try {
                    throw th;
                } catch (RemoteException e3) {
                    e = e3;
                } catch (ServiceSpecificException e4) {
                    e2 = e4;
                    throw convertServiceException(e2);
                }
            } catch (RemoteException e5) {
                e = e5;
                throw e.rethrowFromSystemServer();
            } catch (ServiceSpecificException e6) {
                e2 = e6;
                throw convertServiceException(e2);
            }
        } else {
            Log.i(TAG, "SIM2 data disable by cust");
            return null;
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
        checkTimeout(timeoutMs);
        requestNetwork(request, networkCallback, timeoutMs, inferLegacyTypeForNetworkCapabilities(request.networkCapabilities), getDefaultHandler());
    }

    public void requestNetwork(NetworkRequest request, NetworkCallback networkCallback, Handler handler, int timeoutMs) {
        checkTimeout(timeoutMs);
        requestNetwork(request, networkCallback, timeoutMs, inferLegacyTypeForNetworkCapabilities(request.networkCapabilities), new CallbackHandler(this, handler));
    }

    public void requestNetwork(NetworkRequest request, PendingIntent operation) {
        printStackTrace();
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
        printStackTrace();
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
        printStackTrace();
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
        printStackTrace();
        checkCallbackNotNull(networkCallback);
        List<NetworkRequest> reqs = new ArrayList<>();
        synchronized (sCallbacks) {
            if (networkCallback.networkRequest == null) {
                networkCallback.networkRequest = ALREADY_UNREGISTERED;
                return;
            }
            Preconditions.checkArgument(networkCallback.networkRequest != null, "NetworkCallback was not registered");
            if (networkCallback.networkRequest == ALREADY_UNREGISTERED) {
                Log.i(TAG, "NetworkCallback was already unregistered");
                return;
            }
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
            networkCallback.networkRequest = ALREADY_UNREGISTERED;
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

    public void setAcceptPartialConnectivity(Network network, boolean accept, boolean always) {
        try {
            this.mService.setAcceptPartialConnectivity(network, accept, always);
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

    @SystemApi
    public void startCaptivePortalApp(Network network, Bundle appExtras) {
        try {
            this.mService.startCaptivePortalAppInternal(network, appExtras);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public boolean shouldAvoidBadWifi() {
        try {
            return this.mService.shouldAvoidBadWifi();
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
        boolean isSameNetId = netId == NetworkUtils.getBoundNetworkForProcess();
        if (netId != 0) {
            netId = network.getNetIdForResolv();
        }
        if (!NetworkUtils.bindProcessToNetwork(netId)) {
            return false;
        }
        if (!isSameNetId) {
            try {
                Proxy.setHttpProxySystemProperty(getInstance().getDefaultProxy());
            } catch (SecurityException e) {
                Log.e(TAG, "Can't set proxy properties", e);
            }
            InetAddress.clearDnsCache();
            NetworkEventDispatcher.getInstance().onNetworkConfigurationChanged();
        }
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

    @UnsupportedAppUsage
    @Deprecated
    public static boolean setProcessDefaultNetworkForHostResolution(Network network) {
        return NetworkUtils.bindProcessToNetworkForHostResolution(network == null ? 0 : network.getNetIdForResolv());
    }

    private INetworkPolicyManager getNetworkPolicyManager() {
        synchronized (this) {
            if (this.mNPManager != null) {
                return this.mNPManager;
            }
            this.mNPManager = INetworkPolicyManager.Stub.asInterface(ServiceManager.getService(Context.NETWORK_POLICY_SERVICE));
            return this.mNPManager;
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

    public int getConnectionOwnerUid(int protocol, InetSocketAddress local, InetSocketAddress remote) {
        try {
            return this.mService.getConnectionOwnerUid(new ConnectionInfo(protocol, local, remote));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private void printStackTrace() {
        String stackTrace;
        if (DEBUG) {
            StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
            StringBuffer sb = new StringBuffer();
            int i = 3;
            while (i < callStack.length && (stackTrace = callStack[i].toString()) != null && !stackTrace.contains("android.os")) {
                sb.append(" [");
                sb.append(stackTrace);
                sb.append("]");
                i++;
            }
            Log.i(TAG, "StackLog:" + sb.toString());
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
