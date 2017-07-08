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
import android.os.INetworkActivityListener.Stub;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.preference.Preference;
import android.provider.ContactsContract.Intents.Insert;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.telephony.ITelephony;
import com.android.internal.util.Preconditions;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import libcore.net.event.NetworkEventDispatcher;

public class ConnectivityManager {
    @Deprecated
    public static final String ACTION_BACKGROUND_DATA_SETTING_CHANGED = "android.net.conn.BACKGROUND_DATA_SETTING_CHANGED";
    public static final String ACTION_BTT_NETWORK_CONNECTION_CHANGED = "android.intent.action.BlueToothTethering_NETWORK_CONNECTION_CHANGED";
    public static final String ACTION_CAPTIVE_PORTAL_SIGN_IN = "android.net.conn.CAPTIVE_PORTAL";
    public static final String ACTION_CAPTIVE_PORTAL_TEST_COMPLETED = "android.net.conn.CAPTIVE_PORTAL_TEST_COMPLETED";
    public static final String ACTION_DATA_ACTIVITY_CHANGE = "android.net.conn.DATA_ACTIVITY_CHANGE";
    public static final String ACTION_LTEDATA_COMPLETED_ACTION = "android.net.wifi.LTEDATA_COMPLETED_ACTION";
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
    public static final String EXTRA_ACTIVE_TETHER = "activeArray";
    public static final String EXTRA_ADD_TETHER_TYPE = "extraAddTetherType";
    public static final String EXTRA_AVAILABLE_TETHER = "availableArray";
    public static final String EXTRA_BTT_CONNECT_STATE = "btt_connect_state";
    public static final String EXTRA_CAPTIVE_PORTAL = "android.net.extra.CAPTIVE_PORTAL";
    public static final String EXTRA_CAPTIVE_PORTAL_URL = "android.net.extra.CAPTIVE_PORTAL_URL";
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
    public static final int MAX_NETWORK_REQUEST_TIMEOUT_MS = 6000000;
    public static final int MAX_NETWORK_TYPE = 45;
    public static final int MAX_RADIO_TYPE = 17;
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
    public static final int TYPE_MOBILE_HIPRI = 5;
    public static final int TYPE_MOBILE_IA = 14;
    public static final int TYPE_MOBILE_IMS = 11;
    public static final int TYPE_MOBILE_MMS = 2;
    public static final int TYPE_MOBILE_NET = 36;
    public static final int TYPE_MOBILE_SUPL = 3;
    public static final int TYPE_MOBILE_WAP = 35;
    public static final int TYPE_MOBILE_XCAP = 45;
    public static final int TYPE_NONE = -1;
    public static final int TYPE_PROXY = 16;
    public static final int TYPE_VPN = 17;
    public static final int TYPE_WIFI = 1;
    public static final int TYPE_WIFI_P2P = 13;
    public static final int TYPE_WIMAX = 6;
    private static int lastLegacyRequestId;
    static CallbackHandler sCallbackHandler;
    static final AtomicInteger sCallbackRefCount = null;
    private static ConnectivityManager sInstance;
    private static HashMap<NetworkCapabilities, LegacyRequest> sLegacyRequests;
    static final HashMap<NetworkRequest, NetworkCallback> sNetworkCallback = null;
    private final Context mContext;
    private INetworkManagementService mNMService;
    private INetworkPolicyManager mNPManager;
    private final ArrayMap<OnNetworkActiveListener, INetworkActivityListener> mNetworkActivityListeners;
    private final IConnectivityManager mService;

    /* renamed from: android.net.ConnectivityManager.1 */
    class AnonymousClass1 extends Stub {
        final /* synthetic */ OnNetworkActiveListener val$l;

        AnonymousClass1(OnNetworkActiveListener val$l) {
            this.val$l = val$l;
        }

        public void onNetworkActive() throws RemoteException {
            this.val$l.onNetworkActive();
        }
    }

    /* renamed from: android.net.ConnectivityManager.2 */
    class AnonymousClass2 extends ResultReceiver {
        final /* synthetic */ OnStartTetheringCallback val$callback;

        AnonymousClass2(Handler $anonymous0, OnStartTetheringCallback val$callback) {
            this.val$callback = val$callback;
            super($anonymous0);
        }

        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == 0) {
                this.val$callback.onTetheringStarted();
            } else {
                this.val$callback.onTetheringFailed();
            }
        }
    }

    private class CallbackHandler extends Handler {
        private static final boolean DBG = false;
        private static final String TAG = "ConnectivityManager.CallbackHandler";
        private final HashMap<NetworkRequest, NetworkCallback> mCallbackMap;
        private final ConnectivityManager mCm;
        private final AtomicInteger mRefCount;

        CallbackHandler(Looper looper, HashMap<NetworkRequest, NetworkCallback> callbackMap, AtomicInteger refCount, ConnectivityManager cm) {
            super(looper);
            this.mCallbackMap = callbackMap;
            this.mRefCount = refCount;
            this.mCm = cm;
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
                    }
                case ConnectivityManager.CALLBACK_AVAILABLE /*524290*/:
                    callback = getCallback(request, "AVAILABLE");
                    if (callback != null) {
                        callback.onAvailable(network);
                    }
                case ConnectivityManager.CALLBACK_LOSING /*524291*/:
                    callback = getCallback(request, "LOSING");
                    if (callback != null) {
                        callback.onLosing(network, message.arg1);
                    }
                case ConnectivityManager.CALLBACK_LOST /*524292*/:
                    callback = getCallback(request, "LOST");
                    if (callback != null) {
                        callback.onLost(network);
                    }
                case ConnectivityManager.CALLBACK_UNAVAIL /*524293*/:
                    callback = getCallback(request, "UNAVAIL");
                    if (callback != null) {
                        callback.onUnavailable();
                    }
                case ConnectivityManager.CALLBACK_CAP_CHANGED /*524294*/:
                    callback = getCallback(request, "CAP_CHANGED");
                    if (callback != null) {
                        callback.onCapabilitiesChanged(network, (NetworkCapabilities) getObject(message, NetworkCapabilities.class));
                    }
                case ConnectivityManager.CALLBACK_IP_CHANGED /*524295*/:
                    callback = getCallback(request, "IP_CHANGED");
                    if (callback != null) {
                        callback.onLinkPropertiesChanged(network, (LinkProperties) getObject(message, LinkProperties.class));
                    }
                case ConnectivityManager.CALLBACK_RELEASED /*524296*/:
                    synchronized (this.mCallbackMap) {
                        callback = (NetworkCallback) this.mCallbackMap.remove(request);
                        break;
                    }
                    if (callback != null) {
                        synchronized (this.mRefCount) {
                            if (this.mRefCount.decrementAndGet() == 0) {
                                getLooper().quit();
                            }
                            break;
                        }
                        return;
                    }
                    Log.e(TAG, "callback not found for RELEASED message");
                case ConnectivityManager.CALLBACK_EXIT /*524297*/:
                    Log.d(TAG, "Listener quitting");
                    getLooper().quit();
                case ConnectivityManager.EXPIRE_LEGACY_REQUEST /*524298*/:
                    ConnectivityManager.this.expireRequest((NetworkCapabilities) message.obj, message.arg1);
                case ConnectivityManager.CALLBACK_SUSPENDED /*524299*/:
                    callback = getCallback(request, "SUSPENDED");
                    if (callback != null) {
                        callback.onNetworkSuspended(network);
                    }
                case ConnectivityManager.CALLBACK_RESUMED /*524300*/:
                    callback = getCallback(request, "RESUMED");
                    if (callback != null) {
                        callback.onNetworkResumed(network);
                    }
                default:
            }
        }

        private Object getObject(Message msg, Class c) {
            return msg.getData().getParcelable(c.getSimpleName());
        }

        private NetworkCallback getCallback(NetworkRequest req, String name) {
            NetworkCallback callback;
            synchronized (this.mCallbackMap) {
                callback = (NetworkCallback) this.mCallbackMap.get(req);
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

        private LegacyRequest() {
            this.delay = ConnectivityManager.TYPE_NONE;
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
        public static final SparseArray<String> sMagicDecoderRing = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.ConnectivityManager.NoPreloadHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.ConnectivityManager.NoPreloadHolder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.ConnectivityManager.NoPreloadHolder.<clinit>():void");
        }

        private NoPreloadHolder() {
        }
    }

    public interface OnNetworkActiveListener {
        void onNetworkActive();
    }

    public static abstract class OnStartTetheringCallback {
        public OnStartTetheringCallback() {
        }

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
        final /* synthetic */ ConnectivityManager this$0;

        /* renamed from: android.net.ConnectivityManager.PacketKeepalive.1 */
        class AnonymousClass1 extends Handler {
            final /* synthetic */ PacketKeepalive this$1;

            AnonymousClass1(PacketKeepalive this$1, Looper $anonymous0) {
                this.this$1 = this$1;
                super($anonymous0);
            }

            public void handleMessage(Message message) {
                switch (message.what) {
                    case NetworkAgent.EVENT_PACKET_KEEPALIVE /*528397*/:
                        int error = message.arg2;
                        if (error == 0) {
                            try {
                                if (this.this$1.mSlot == null) {
                                    this.this$1.mSlot = Integer.valueOf(message.arg1);
                                    this.this$1.mCallback.onStarted();
                                    return;
                                }
                                this.this$1.mSlot = null;
                                this.this$1.stopLooper();
                                this.this$1.mCallback.onStopped();
                                return;
                            } catch (Exception e) {
                                Log.e(PacketKeepalive.TAG, "Exception in keepalive callback(" + error + ")", e);
                                return;
                            }
                        }
                        this.this$1.stopLooper();
                        this.this$1.mCallback.onError(error);
                    default:
                        Log.e(PacketKeepalive.TAG, "Unhandled message " + Integer.toHexString(message.what));
                }
            }
        }

        /* synthetic */ PacketKeepalive(ConnectivityManager this$0, Network network, PacketKeepaliveCallback callback, PacketKeepalive packetKeepalive) {
            this(this$0, network, callback);
        }

        void stopLooper() {
            this.mLooper.quit();
        }

        public void stop() {
            try {
                this.this$0.mService.stopKeepalive(this.mNetwork, this.mSlot.intValue());
            } catch (RemoteException e) {
                Log.e(TAG, "Error stopping packet keepalive: ", e);
                stopLooper();
            }
        }

        private PacketKeepalive(ConnectivityManager this$0, Network network, PacketKeepaliveCallback callback) {
            this.this$0 = this$0;
            Preconditions.checkNotNull(network, "network cannot be null");
            Preconditions.checkNotNull(callback, "callback cannot be null");
            this.mNetwork = network;
            this.mCallback = callback;
            HandlerThread thread = new HandlerThread(TAG);
            thread.start();
            this.mLooper = thread.getLooper();
            this.mMessenger = new Messenger(new AnonymousClass1(this, this.mLooper));
        }
    }

    public static class PacketKeepaliveCallback {
        public PacketKeepaliveCallback() {
        }

        public void onStarted() {
        }

        public void onStopped() {
        }

        public void onError(int error) {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.ConnectivityManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.ConnectivityManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.ConnectivityManager.<clinit>():void");
    }

    private static int getNewLastLegacyRequestId() {
        if (lastLegacyRequestId == Preference.DEFAULT_ORDER) {
            lastLegacyRequestId = TYPE_MOBILE;
        }
        int i = lastLegacyRequestId;
        lastLegacyRequestId = i + TYPE_WIFI;
        return i;
    }

    public static boolean isNetworkTypeValid(int networkType) {
        if (networkType >= 0 && networkType <= TYPE_VPN) {
            return true;
        }
        if (networkType < TYPE_MOBILE_DM || networkType > TYPE_MOBILE_XCAP) {
            return false;
        }
        return true;
    }

    public static String getNetworkTypeName(int type) {
        switch (type) {
            case TYPE_MOBILE /*0*/:
                return "MOBILE";
            case TYPE_WIFI /*1*/:
                return "WIFI";
            case TYPE_MOBILE_MMS /*2*/:
                return "MOBILE_MMS";
            case TYPE_MOBILE_SUPL /*3*/:
                return "MOBILE_SUPL";
            case TYPE_MOBILE_DUN /*4*/:
                return "MOBILE_DUN";
            case TYPE_MOBILE_HIPRI /*5*/:
                return "MOBILE_HIPRI";
            case TYPE_WIMAX /*6*/:
                return "WIMAX";
            case TYPE_BLUETOOTH /*7*/:
                return "BLUETOOTH";
            case TYPE_DUMMY /*8*/:
                return "DUMMY";
            case TYPE_ETHERNET /*9*/:
                return "ETHERNET";
            case TYPE_MOBILE_FOTA /*10*/:
                return "MOBILE_FOTA";
            case TYPE_MOBILE_IMS /*11*/:
                return "MOBILE_IMS";
            case TYPE_MOBILE_CBS /*12*/:
                return "MOBILE_CBS";
            case TYPE_WIFI_P2P /*13*/:
                return "WIFI_P2P";
            case TYPE_MOBILE_IA /*14*/:
                return "MOBILE_IA";
            case TYPE_MOBILE_EMERGENCY /*15*/:
                return "MOBILE_EMERGENCY";
            case TYPE_PROXY /*16*/:
                return "PROXY";
            case TYPE_VPN /*17*/:
                return "VPN";
            case TYPE_MOBILE_BIP0 /*38*/:
                return "MOBILE_BIP0";
            case TYPE_MOBILE_BIP1 /*39*/:
                return "MOBILE_BIP1";
            case TYPE_MOBILE_BIP2 /*40*/:
                return "MOBILE_BIP2";
            case TYPE_MOBILE_BIP3 /*41*/:
                return "MOBILE_BIP3";
            case TYPE_MOBILE_BIP4 /*42*/:
                return "MOBILE_BIP4";
            case TYPE_MOBILE_BIP5 /*43*/:
                return "MOBILE_BIP5";
            case TYPE_MOBILE_BIP6 /*44*/:
                return "MOBILE_BIP6";
            case TYPE_MOBILE_XCAP /*45*/:
                return "MOBILE_XCAP";
            default:
                return Integer.toString(type);
        }
    }

    public static boolean isNetworkTypeMobile(int networkType) {
        switch (networkType) {
            case TYPE_MOBILE /*0*/:
            case TYPE_MOBILE_MMS /*2*/:
            case TYPE_MOBILE_SUPL /*3*/:
            case TYPE_MOBILE_DUN /*4*/:
            case TYPE_MOBILE_HIPRI /*5*/:
            case TYPE_MOBILE_FOTA /*10*/:
            case TYPE_MOBILE_IMS /*11*/:
            case TYPE_MOBILE_CBS /*12*/:
            case TYPE_MOBILE_IA /*14*/:
            case TYPE_MOBILE_EMERGENCY /*15*/:
            case TYPE_MOBILE_BIP0 /*38*/:
            case TYPE_MOBILE_BIP1 /*39*/:
            case TYPE_MOBILE_BIP2 /*40*/:
            case TYPE_MOBILE_BIP3 /*41*/:
            case TYPE_MOBILE_BIP4 /*42*/:
            case TYPE_MOBILE_BIP5 /*43*/:
            case TYPE_MOBILE_BIP6 /*44*/:
            case TYPE_MOBILE_XCAP /*45*/:
                return true;
            default:
                return false;
        }
    }

    public static boolean isNetworkTypeWifi(int networkType) {
        switch (networkType) {
            case TYPE_WIFI /*1*/:
            case TYPE_WIFI_P2P /*13*/:
                return true;
            default:
                return false;
        }
    }

    public void setNetworkPreference(int preference) {
    }

    public int getNetworkPreference() {
        return TYPE_NONE;
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

    public NetworkInfo[] getAllNetworkInfo() {
        try {
            return this.mService.getAllNetworkInfo();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

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

    public int startUsingNetworkFeature(int networkType, String feature) {
        checkLegacyRoutingApiAccess();
        NetworkCapabilities netCap = networkCapabilitiesForFeature(networkType, feature);
        if (netCap == null) {
            Log.d(TAG, "Can't satisfy startUsingNetworkFeature for " + networkType + ", " + feature);
            return TYPE_MOBILE_SUPL;
        }
        HwFrameworkFactory.getHwInnerConnectivityManager().checkHwFeature(feature, netCap, networkType);
        synchronized (sLegacyRequests) {
            LegacyRequest l = (LegacyRequest) sLegacyRequests.get(netCap);
            if (l != null) {
                Log.d(TAG, "renewing startUsingNetworkFeature request " + l.networkRequest);
                renewRequestLocked(l);
                if (l.currentNetwork != null) {
                    return TYPE_MOBILE;
                }
                return TYPE_WIFI;
            }
            NetworkRequest request = requestNetworkForFeatureLocked(netCap);
            if (request != null) {
                Log.d(TAG, "starting startUsingNetworkFeature for request " + request);
                return TYPE_WIFI;
            }
            Log.d(TAG, " request Failed");
            return TYPE_MOBILE_SUPL;
        }
    }

    public int stopUsingNetworkFeature(int networkType, String feature) {
        checkLegacyRoutingApiAccess();
        NetworkCapabilities netCap = networkCapabilitiesForFeature(networkType, feature);
        if (netCap == null) {
            Log.d(TAG, "Can't satisfy stopUsingNetworkFeature for " + networkType + ", " + feature);
            return TYPE_NONE;
        }
        HwFrameworkFactory.getHwInnerConnectivityManager().checkHwFeature(feature, netCap, networkType);
        if (removeRequestForFeature(netCap)) {
            Log.d(TAG, "stopUsingNetworkFeature for " + networkType + ", " + feature);
        }
        return TYPE_WIFI;
    }

    private NetworkCapabilities networkCapabilitiesForFeature(int networkType, String feature) {
        NetworkCapabilities netCap;
        if (networkType == 0) {
            int cap;
            String[] result = HwFrameworkFactory.getHwInnerConnectivityManager().getFeature(feature);
            if (!(result[TYPE_MOBILE] == null || ProxyInfo.LOCAL_EXCL_LIST.equals(result[TYPE_MOBILE]))) {
                feature = result[TYPE_MOBILE];
            }
            if ("enableMMS".equals(feature)) {
                cap = TYPE_MOBILE;
            } else if ("enableSUPL".equals(feature)) {
                cap = TYPE_WIFI;
            } else if ("enableDUN".equals(feature) || "enableDUNAlways".equals(feature)) {
                cap = TYPE_MOBILE_MMS;
            } else if ("enableHIPRI".equals(feature)) {
                cap = TYPE_MOBILE_CBS;
            } else if ("enableFOTA".equals(feature)) {
                cap = TYPE_MOBILE_SUPL;
            } else if ("enableIMS".equals(feature)) {
                cap = TYPE_MOBILE_DUN;
            } else if ("enableCBS".equals(feature)) {
                cap = TYPE_MOBILE_HIPRI;
            } else if ("enableBIP0".equals(feature)) {
                cap = 18;
            } else if ("enableBIP1".equals(feature)) {
                cap = 19;
            } else if ("enableBIP2".equals(feature)) {
                cap = 20;
            } else if ("enableBIP3".equals(feature)) {
                cap = 21;
            } else if ("enableBIP4".equals(feature)) {
                cap = 22;
            } else if ("enableBIP5".equals(feature)) {
                cap = 23;
            } else if ("enableBIP6".equals(feature)) {
                cap = 24;
            } else if (!"enableXCAP".equals(feature)) {
                return null;
            } else {
                cap = TYPE_ETHERNET;
            }
            netCap = new NetworkCapabilities();
            netCap.addTransportType(TYPE_MOBILE).addCapability(cap);
            netCap.maybeMarkCapabilitiesRestricted();
            return netCap;
        } else if (networkType != TYPE_WIFI || !"p2p".equals(feature)) {
            return null;
        } else {
            netCap = new NetworkCapabilities();
            netCap.addTransportType(TYPE_WIFI);
            netCap.addCapability(TYPE_WIMAX);
            netCap.maybeMarkCapabilitiesRestricted();
            return netCap;
        }
    }

    private int inferLegacyTypeForNetworkCapabilities(NetworkCapabilities netCap) {
        if (netCap == null || !netCap.hasTransport(TYPE_MOBILE) || !netCap.hasCapability(TYPE_WIFI)) {
            return TYPE_NONE;
        }
        String type = null;
        int result = TYPE_NONE;
        if (netCap.hasCapability(TYPE_MOBILE_HIPRI)) {
            type = "enableCBS";
            result = TYPE_MOBILE_CBS;
        } else if (netCap.hasCapability(TYPE_MOBILE_DUN)) {
            type = "enableIMS";
            result = TYPE_MOBILE_IMS;
        } else if (netCap.hasCapability(TYPE_MOBILE_SUPL)) {
            type = "enableFOTA";
            result = TYPE_MOBILE_FOTA;
        } else if (netCap.hasCapability(TYPE_MOBILE_MMS)) {
            type = "enableDUN";
            result = TYPE_MOBILE_DUN;
        } else if (netCap.hasCapability(TYPE_WIFI)) {
            type = "enableSUPL";
            result = TYPE_MOBILE_SUPL;
        } else if (netCap.hasCapability(TYPE_MOBILE_CBS)) {
            type = "enableHIPRI";
            result = TYPE_MOBILE_HIPRI;
        } else if (netCap.hasCapability(18)) {
            type = "enableBIP0";
            result = TYPE_MOBILE_BIP0;
        } else if (netCap.hasCapability(19)) {
            type = "enableBIP1";
            result = TYPE_MOBILE_BIP1;
        } else if (netCap.hasCapability(20)) {
            type = "enableBIP2";
            result = TYPE_MOBILE_BIP2;
        } else if (netCap.hasCapability(21)) {
            type = "enableBIP3";
            result = TYPE_MOBILE_BIP3;
        } else if (netCap.hasCapability(22)) {
            type = "enableBIP4";
            result = TYPE_MOBILE_BIP4;
        } else if (netCap.hasCapability(23)) {
            type = "enableBIP5";
            result = TYPE_MOBILE_BIP5;
        } else if (netCap.hasCapability(24)) {
            type = "enableBIP6";
            result = TYPE_MOBILE_BIP6;
        } else if (netCap.hasCapability(TYPE_ETHERNET)) {
            type = "enableXCAP";
            result = TYPE_MOBILE_XCAP;
        }
        if (type != null) {
            NetworkCapabilities testCap = networkCapabilitiesForFeature(TYPE_MOBILE, type);
            if (testCap.equalsNetCapabilities(netCap) && testCap.equalsTransportTypes(netCap)) {
                return result;
            }
            return TYPE_NONE;
        }
        return TYPE_NONE;
    }

    private int legacyTypeForNetworkCapabilities(NetworkCapabilities netCap) {
        if (netCap == null) {
            return TYPE_NONE;
        }
        if (netCap.hasCapability(TYPE_MOBILE_HIPRI)) {
            return TYPE_MOBILE_CBS;
        }
        if (netCap.hasCapability(TYPE_MOBILE_DUN)) {
            return TYPE_MOBILE_IMS;
        }
        if (netCap.hasCapability(TYPE_MOBILE_SUPL)) {
            return TYPE_MOBILE_FOTA;
        }
        if (netCap.hasCapability(TYPE_MOBILE_MMS)) {
            return TYPE_MOBILE_DUN;
        }
        if (netCap.hasCapability(TYPE_WIFI)) {
            return TYPE_MOBILE_SUPL;
        }
        if (netCap.hasCapability(TYPE_MOBILE)) {
            return TYPE_MOBILE_MMS;
        }
        if (netCap.hasCapability(TYPE_MOBILE_CBS)) {
            return TYPE_MOBILE_HIPRI;
        }
        if (netCap.hasCapability(TYPE_WIMAX)) {
            return TYPE_WIFI_P2P;
        }
        if (netCap.hasCapability(18)) {
            return TYPE_MOBILE_BIP0;
        }
        if (netCap.hasCapability(19)) {
            return TYPE_MOBILE_BIP1;
        }
        if (netCap.hasCapability(20)) {
            return TYPE_MOBILE_BIP2;
        }
        if (netCap.hasCapability(21)) {
            return TYPE_MOBILE_BIP3;
        }
        if (netCap.hasCapability(22)) {
            return TYPE_MOBILE_BIP4;
        }
        if (netCap.hasCapability(23)) {
            return TYPE_MOBILE_BIP5;
        }
        if (netCap.hasCapability(24)) {
            return TYPE_MOBILE_BIP6;
        }
        if (netCap.hasCapability(TYPE_ETHERNET)) {
            return TYPE_MOBILE_XCAP;
        }
        return TYPE_NONE;
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
            Log.d(TAG, "expireRequest with " + ourSeqNum + ", " + sequenceNum);
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
            l.networkRequest = sendRequestForNetwork(netCap, l.networkCallback, TYPE_MOBILE, TYPE_MOBILE_MMS, type);
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
            sCallbackHandler.sendMessageDelayed(sCallbackHandler.obtainMessage(EXPIRE_LEGACY_REQUEST, seqNum, TYPE_MOBILE, netCap), (long) delay);
        }
    }

    private boolean removeRequestForFeature(NetworkCapabilities netCap) {
        synchronized (sLegacyRequests) {
            LegacyRequest l = (LegacyRequest) sLegacyRequests.remove(netCap);
        }
        if (l == null) {
            return false;
        }
        unregisterNetworkCallback(l.networkCallback);
        l.clearDnsBinding();
        return true;
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

    public boolean requestRouteToHost(int networkType, int hostAddress) {
        return requestRouteToHostAddress(networkType, NetworkUtils.intToInetAddress(hostAddress));
    }

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

    public boolean getMobileDataEnabled() {
        IBinder b = ServiceManager.getService(Insert.PHONE);
        if (b != null) {
            try {
                ITelephony it = ITelephony.Stub.asInterface(b);
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
            if (this.mNMService != null) {
                INetworkManagementService iNetworkManagementService = this.mNMService;
                return iNetworkManagementService;
            }
            this.mNMService = INetworkManagementService.Stub.asInterface(ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
            iNetworkManagementService = this.mNMService;
            return iNetworkManagementService;
        }
    }

    public void addDefaultNetworkActiveListener(OnNetworkActiveListener l) {
        INetworkActivityListener rl = new AnonymousClass1(l);
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
        this.mNetworkActivityListeners = new ArrayMap();
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

    public static final void enforceTetherChangePermission(Context context) {
        if (context.getResources().getStringArray(17235992).length == TYPE_MOBILE_MMS) {
            context.enforceCallingOrSelfPermission(permission.TETHER_PRIVILEGED, "ConnectivityService");
            return;
        }
        int uid = Binder.getCallingUid();
        Settings.checkAndNoteWriteSettingsOperation(context, uid, Settings.getPackageNameForUid(context, uid), true);
    }

    static ConnectivityManager getInstanceOrNull() {
        return sInstance;
    }

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

    public void startTethering(int type, boolean showProvisioningUi, OnStartTetheringCallback callback, Handler handler) {
        ResultReceiver wrappedCallback = new AnonymousClass2(handler, callback);
        try {
            this.mService.startTethering(type, wrappedCallback, showProvisioningUi);
        } catch (RemoteException e) {
            Log.e(TAG, "Exception trying to start tethering.", e);
            wrappedCallback.send(TYPE_MOBILE_MMS, null);
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

    private void incCallbackHandlerRefCount() {
        synchronized (sCallbackRefCount) {
            if (sCallbackRefCount.incrementAndGet() == TYPE_WIFI) {
                HandlerThread callbackThread = new HandlerThread(TAG);
                callbackThread.start();
                sCallbackHandler = new CallbackHandler(callbackThread.getLooper(), sNetworkCallback, sCallbackRefCount, this);
            }
        }
    }

    private void decCallbackHandlerRefCount() {
        synchronized (sCallbackRefCount) {
            if (sCallbackRefCount.decrementAndGet() == 0) {
                sCallbackHandler.obtainMessage(CALLBACK_EXIT).sendToTarget();
                sCallbackHandler = null;
            }
        }
    }

    private NetworkRequest sendRequestForNetwork(NetworkCapabilities need, NetworkCallback networkCallback, int timeoutSec, int action, int legacyType) {
        if (networkCallback == null) {
            throw new IllegalArgumentException("null NetworkCallback");
        } else if (need != null || action == TYPE_MOBILE_MMS) {
            try {
                incCallbackHandlerRefCount();
                synchronized (sNetworkCallback) {
                    if (action == TYPE_WIFI) {
                        networkCallback.networkRequest = this.mService.listenForNetwork(need, new Messenger(sCallbackHandler), new Binder());
                    } else {
                        networkCallback.networkRequest = this.mService.requestNetwork(need, new Messenger(sCallbackHandler), timeoutSec, new Binder(), legacyType);
                    }
                    if (networkCallback.networkRequest != null) {
                        sNetworkCallback.put(networkCallback.networkRequest, networkCallback);
                    }
                }
                if (networkCallback.networkRequest == null) {
                    decCallbackHandlerRefCount();
                }
                return networkCallback.networkRequest;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("null NetworkCapabilities");
        }
    }

    public void requestNetwork(NetworkRequest request, NetworkCallback networkCallback, int timeoutMs, int legacyType) {
        sendRequestForNetwork(request.networkCapabilities, networkCallback, timeoutMs, TYPE_MOBILE_MMS, legacyType);
    }

    public void requestNetwork(NetworkRequest request, NetworkCallback networkCallback) {
        requestNetwork(request, networkCallback, TYPE_MOBILE, inferLegacyTypeForNetworkCapabilities(request.networkCapabilities));
    }

    public void requestNetwork(NetworkRequest request, NetworkCallback networkCallback, int timeoutMs) {
        requestNetwork(request, networkCallback, timeoutMs, inferLegacyTypeForNetworkCapabilities(request.networkCapabilities));
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
        sendRequestForNetwork(request.networkCapabilities, networkCallback, TYPE_MOBILE, TYPE_WIFI, TYPE_NONE);
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
        sendRequestForNetwork(null, networkCallback, TYPE_MOBILE, TYPE_MOBILE_MMS, TYPE_NONE);
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
            synchronized (sNetworkCallback) {
                sNetworkCallback.remove(networkCallback.networkRequest);
            }
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

    public static boolean setProcessDefaultNetwork(Network network) {
        int netId = network == null ? TYPE_MOBILE : network.netId;
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

    public static Network getProcessDefaultNetwork() {
        int netId = NetworkUtils.getBoundNetworkForProcess();
        if (netId == 0) {
            return null;
        }
        return new Network(netId);
    }

    private void unsupportedStartingFrom(int version) {
        if (Process.myUid() != Process.SYSTEM_UID && this.mContext.getApplicationInfo().targetSdkVersion >= version) {
            throw new UnsupportedOperationException("This method is not supported in target SDK version " + version + " and above");
        }
    }

    private void checkLegacyRoutingApiAccess() {
        String permForOmadm = "com.android.permission.INJECT_OMADM_SETTINGS";
        if (this.mContext.checkCallingOrSelfPermission(permission.USE_LEGACY_INTERFACE) != 0 && this.mContext.checkCallingOrSelfPermission(permForOmadm) != 0) {
            unsupportedStartingFrom(23);
        }
    }

    public static boolean setProcessDefaultNetworkForHostResolution(Network network) {
        return NetworkUtils.bindProcessToNetworkForHostResolution(network == null ? TYPE_MOBILE : network.netId);
    }

    private INetworkPolicyManager getNetworkPolicyManager() {
        synchronized (this) {
            if (this.mNPManager != null) {
                INetworkPolicyManager iNetworkPolicyManager = this.mNPManager;
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
        int lteState = TYPE_MOBILE_SUPL;
        try {
            lteState = this.mService.checkLteConnectState();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return lteState;
    }

    public long getLteTotalRxBytes() {
        Log.d(TAG, "[enter]getLteTotalRxBytes");
        long lteTotalRxBytes = 0;
        try {
            lteTotalRxBytes = this.mService.getLteTotalRxBytes();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return lteTotalRxBytes;
    }

    public long getLteTotalTxBytes() {
        Log.d(TAG, "[enter]getLteTotalTxBytes");
        long lteTotalTxBytes = 0;
        try {
            lteTotalTxBytes = this.mService.getLteTotalTxBytes();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return lteTotalTxBytes;
    }

    private static final String whatToString(int what) {
        return (String) NoPreloadHolder.sMagicDecoderRing.get(what, Integer.toString(what));
    }
}
