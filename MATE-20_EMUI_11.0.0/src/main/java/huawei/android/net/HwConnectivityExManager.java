package huawei.android.net;

import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ServiceSpecificException;
import android.util.Log;
import com.android.internal.util.Preconditions;
import com.huawei.android.net.IUsbP2pCallback;
import com.huawei.android.os.ServiceManagerEx;
import huawei.android.net.IConnectivityExManager;
import huawei.android.net.slice.AppInfoCallback;
import huawei.android.net.slice.IAppInfoCallback;
import huawei.android.net.slice.INetworkSliceStateListener;
import huawei.android.net.slice.NetworkSliceStateListener;
import huawei.android.net.slice.TrafficDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class HwConnectivityExManager {
    private static final NetworkRequest ALREADY_UNREGISTERED = new NetworkRequest.Builder().clearCapabilities().build();
    public static final int ERROR_CODE_ILLEGAL_INPUT = 2;
    public static final int ERROR_CODE_NOT_SUPPORT_NR_SLICE = 1;
    public static final int ERROR_CODE_NO_AVAILABLE_SLICE = 4;
    public static final int ERROR_CODE_PERMISSION_DENIED = 3;
    private static final int INVALID_NETID = -1;
    public static final int REQUEST_SUCCESS = 0;
    private static final int SLICE_CALLBACK_AVAILABLE = 524290;
    private static final int SLICE_CALLBACK_BLK_CHANGED = 524299;
    private static final int SLICE_CALLBACK_CAP_CHANGED = 524294;
    private static final int SLICE_CALLBACK_IP_CHANGED = 524295;
    private static final int SLICE_CALLBACK_LOSING = 524291;
    private static final int SLICE_CALLBACK_LOST = 524292;
    private static final int SLICE_CALLBACK_PRECHECK = 524289;
    private static final int SLICE_CALLBACK_RESUMED = 524298;
    private static final int SLICE_CALLBACK_SUSPENDED = 524297;
    private static final int SLICE_CALLBACK_UNAVAIL = 524293;
    private static final String TAG = "HwConnectivityExManager";
    public static final int UNKNOWN_ID = -1;
    public static final int USB_P2P_CALLBACK_STATE_CHANGE = 2;
    public static final int USB_P2P_CALLBACK_UNAVAIL = 1;
    public static final int USB_P2P_NO_CHANGE = -1;
    public static final int USB_P2P_TYPE_LISTEN = 2;
    public static final int USB_P2P_TYPE_REQUEST = 1;
    private static volatile HwConnectivityExManager mInstance = null;
    private static CallbackHandler sCallbackHandler;
    private static NetworkCallbackHandler sNetworkCallbackHandler;
    private static final Map<NetworkRequest, ConnectivityManager.NetworkCallback> sSliceCallbacks = new ConcurrentHashMap();
    private final Map<IUsbP2pCallback, Integer> mCallbackIdMap = new ConcurrentHashMap();
    private IConnectivityExManager mService = null;
    private final Map<NetworkSliceStateListener, INetworkSliceStateListener> mStateListeners = new ConcurrentHashMap();

    private HwConnectivityExManager() {
        bindService();
    }

    public static synchronized HwConnectivityExManager getDefault() {
        HwConnectivityExManager hwConnectivityExManager;
        synchronized (HwConnectivityExManager.class) {
            if (mInstance == null) {
                mInstance = new HwConnectivityExManager();
            }
            hwConnectivityExManager = mInstance;
        }
        return hwConnectivityExManager;
    }

    public void setSmartKeyguardLevel(String level) {
        try {
            this.mService.setSmartKeyguardLevel(level);
        } catch (RemoteException e) {
        }
    }

    public void setUseCtrlSocket(boolean flag) {
    }

    public void setApIpv4AddressFixed(boolean isFixed) {
        Log.i(TAG, "setApIpv4AddressFixed:" + isFixed);
        if (this.mService == null) {
            bindService();
        }
        IConnectivityExManager iConnectivityExManager = this.mService;
        if (iConnectivityExManager != null) {
            try {
                iConnectivityExManager.setApIpv4AddressFixed(isFixed);
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException" + e.getMessage());
            }
        }
    }

    public boolean isApIpv4AddressFixed() {
        Log.i(TAG, "isApIpv4AddressFixed");
        if (this.mService == null) {
            bindService();
        }
        IConnectivityExManager iConnectivityExManager = this.mService;
        if (iConnectivityExManager == null) {
            return false;
        }
        try {
            return iConnectivityExManager.isApIpv4AddressFixed();
        } catch (RemoteException e) {
            Log.d(TAG, "RemoteException" + e.getMessage());
            return false;
        }
    }

    public boolean bindUidProcessToNetwork(int netId, int uid) {
        if (this.mService == null) {
            bindService();
        }
        IConnectivityExManager iConnectivityExManager = this.mService;
        if (iConnectivityExManager == null) {
            return false;
        }
        try {
            return iConnectivityExManager.bindUidProcessToNetwork(netId, uid);
        } catch (RemoteException e) {
            Log.e(TAG, "bindUidProcessToNetwork RemoteException occurs.");
            return false;
        }
    }

    public boolean unbindAllUidProcessToNetwork(int netId) {
        if (this.mService == null) {
            bindService();
        }
        IConnectivityExManager iConnectivityExManager = this.mService;
        if (iConnectivityExManager == null) {
            return false;
        }
        try {
            return iConnectivityExManager.unbindAllUidProcessToNetwork(netId);
        } catch (RemoteException e) {
            Log.e(TAG, "unbindAllUidProcessToNetwork RemoteException occurs.");
            return false;
        }
    }

    public boolean isUidProcessBindedToNetwork(int netId, int uid) {
        if (this.mService == null) {
            bindService();
        }
        IConnectivityExManager iConnectivityExManager = this.mService;
        if (iConnectivityExManager == null) {
            return false;
        }
        try {
            return iConnectivityExManager.isUidProcessBindedToNetwork(netId, uid);
        } catch (RemoteException e) {
            Log.e(TAG, "isUidProcessBindedToNetwork RemoteException occurs.");
            return false;
        }
    }

    public boolean isAllUidProcessUnbindToNetwork(int netId) {
        if (this.mService == null) {
            bindService();
        }
        IConnectivityExManager iConnectivityExManager = this.mService;
        if (iConnectivityExManager == null) {
            return false;
        }
        try {
            return iConnectivityExManager.isAllUidProcessUnbindToNetwork(netId);
        } catch (RemoteException e) {
            Log.e(TAG, "isAllUidProcessUnbindToNetwork RemoteException occurs.");
            return false;
        }
    }

    public int getNetIdBySlotId(int slotId) {
        if (this.mService == null) {
            bindService();
        }
        IConnectivityExManager iConnectivityExManager = this.mService;
        if (iConnectivityExManager == null) {
            return -1;
        }
        try {
            return iConnectivityExManager.getNetIdBySlotId(slotId);
        } catch (RemoteException e) {
            Log.e(TAG, "getNetIdBySlotId RemoteException occurs.");
            return -1;
        }
    }

    public boolean isNetworkSliceSupported() {
        if (this.mService == null) {
            bindService();
        }
        IConnectivityExManager iConnectivityExManager = this.mService;
        if (iConnectivityExManager == null) {
            return false;
        }
        try {
            return iConnectivityExManager.isNetworkSliceSupported();
        } catch (RemoteException e) {
            Log.e(TAG, "isNetworkSliceSupported RemoteException occurs.");
            return false;
        }
    }

    public void initAppInfo(String appId, int uid, final AppInfoCallback appInfoCallback) {
        Preconditions.checkNotNull(appInfoCallback, "appInfoCallback null");
        if (!isNetworkSliceSupported()) {
            appInfoCallback.onPermissionCheckCallback(false);
            return;
        }
        if (this.mService == null) {
            bindService();
        }
        if (this.mService != null) {
            try {
                this.mService.initAppInfo(appId, uid, new IAppInfoCallback.Stub() {
                    /* class huawei.android.net.HwConnectivityExManager.AnonymousClass1 */

                    @Override // huawei.android.net.slice.IAppInfoCallback
                    public void onPermissionCheckCallback(boolean isSuccess) {
                        appInfoCallback.onPermissionCheckCallback(isSuccess);
                    }
                });
            } catch (RemoteException e) {
                Log.e(TAG, "initAppInfo RemoteException occurs.");
            }
        }
    }

    public boolean registerListener(int uid, final NetworkSliceStateListener networkSliceStateListener) {
        Preconditions.checkNotNull(networkSliceStateListener, "networkSliceStateListener null");
        if (!isNetworkSliceSupported()) {
            return false;
        }
        if (this.mService == null) {
            bindService();
        }
        if (this.mService == null) {
            return false;
        }
        INetworkSliceStateListener listener = new INetworkSliceStateListener.Stub() {
            /* class huawei.android.net.HwConnectivityExManager.AnonymousClass2 */

            @Override // huawei.android.net.slice.INetworkSliceStateListener
            public void onNetworkSliceStateChanged(int retCode) {
                networkSliceStateListener.onNetworkSliceStateChanged(retCode);
            }
        };
        try {
            boolean result = this.mService.registerListener(uid, listener);
            if (result) {
                this.mStateListeners.put(networkSliceStateListener, listener);
            }
            return result;
        } catch (RemoteException e) {
            Log.e(TAG, "registerListener RemoteException occurs.");
            return false;
        }
    }

    public boolean unregisterListener(int uid, NetworkSliceStateListener networkSliceStateListener) {
        IConnectivityExManager iConnectivityExManager;
        Preconditions.checkNotNull(networkSliceStateListener, "networkSliceStateListener null");
        if (!isNetworkSliceSupported()) {
            return false;
        }
        if (this.mService == null) {
            bindService();
        }
        INetworkSliceStateListener listener = this.mStateListeners.get(networkSliceStateListener);
        if (listener == null || (iConnectivityExManager = this.mService) == null) {
            return false;
        }
        try {
            boolean result = iConnectivityExManager.unregisterListener(uid, listener);
            if (result) {
                this.mStateListeners.remove(networkSliceStateListener);
            }
            return result;
        } catch (RemoteException e) {
            Log.e(TAG, "unregisterListener RemoteException occurs.");
            return false;
        }
    }

    public int requestNetworkSlice(int uid, TrafficDescriptor trafficDescriptor, ConnectivityManager.NetworkCallback networkCallback, int timeoutMs) {
        if (!isNetworkSliceSupported()) {
            return 1;
        }
        if (Stream.of(trafficDescriptor, networkCallback).anyMatch($$Lambda$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE)) {
            return 2;
        }
        if (this.mService == null) {
            Log.e(TAG, "mService is null");
            bindService();
        }
        IConnectivityExManager iConnectivityExManager = this.mService;
        if (iConnectivityExManager != null) {
            try {
                if (!iConnectivityExManager.hasPermissionForSlice(uid)) {
                    return 3;
                }
                synchronized (sSliceCallbacks) {
                    if (!(networkCallback.getNetworkRequest() == null || networkCallback.getNetworkRequest() == ALREADY_UNREGISTERED)) {
                        Log.e(TAG, "NetworkCallback was already registered");
                    }
                    Messenger messenger = new Messenger(getNetworkCallbackHandler());
                    new Binder();
                    NetworkRequest networkRequest = this.mService.requestNetworkSlice(uid, trafficDescriptor, messenger, timeoutMs);
                    if (networkRequest == null) {
                        return 4;
                    }
                    sSliceCallbacks.put(networkRequest, networkCallback);
                    networkCallback.setNetworkRequest(networkRequest);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "requestNetworkSlice RemoteException occurs.");
            }
        }
        return 0;
    }

    public boolean releaseNetworkSlice(int uid, ConnectivityManager.NetworkCallback networkCallback) {
        if (!isNetworkSliceSupported()) {
            return false;
        }
        List<NetworkRequest> requests = new ArrayList<>();
        synchronized (sSliceCallbacks) {
            if (networkCallback.getNetworkRequest() == null) {
                networkCallback.setNetworkRequest(ALREADY_UNREGISTERED);
                return false;
            } else if (networkCallback.getNetworkRequest() == ALREADY_UNREGISTERED) {
                Log.i(TAG, "releaseNetworkSlice: NetworkCallback was already unregistered");
                return true;
            } else {
                for (Map.Entry<NetworkRequest, ConnectivityManager.NetworkCallback> entry : sSliceCallbacks.entrySet()) {
                    if (entry.getValue() == networkCallback) {
                        requests.add(entry.getKey());
                    }
                }
                for (NetworkRequest request : requests) {
                    if (request != null) {
                        try {
                            this.mService.releaseNetworkSlice(uid, request.requestId);
                        } catch (RemoteException e) {
                            Log.e(TAG, "releaseNetworkSlice RemoteException occurs.");
                        }
                        sSliceCallbacks.remove(request);
                    }
                }
                networkCallback.setNetworkRequest(ALREADY_UNREGISTERED);
                return true;
            }
        }
    }

    public int getUsbP2pState() {
        if (this.mService == null) {
            bindService();
        }
        IConnectivityExManager iConnectivityExManager = this.mService;
        if (iConnectivityExManager == null) {
            return 0;
        }
        try {
            return iConnectivityExManager.getUsbP2pState();
        } catch (RemoteException e) {
            Log.e(TAG, "getNetIdBySlotId RemoteException occurs.");
            return 0;
        }
    }

    public void requestUsbP2p(IUsbP2pCallback callback) {
        if (this.mService == null) {
            bindService();
        }
        if (this.mService != null) {
            sendRequestsForUsbP2p(callback, getDefaultHandler(), 1);
        }
    }

    public void registerUsbP2pCallback(IUsbP2pCallback callback) {
        if (this.mService == null) {
            bindService();
        }
        if (this.mService != null) {
            sendRequestsForUsbP2p(callback, getDefaultHandler(), 2);
        }
    }

    public void unregisterUsbP2pCallback(IUsbP2pCallback callback) {
        if (this.mService == null) {
            bindService();
        }
        if (this.mService != null) {
            sendReleaseForUsbP2p(callback);
        }
    }

    private void bindService() {
        this.mService = IConnectivityExManager.Stub.asInterface(ServiceManagerEx.getService("hwConnectivityExService"));
    }

    private void sendRequestsForUsbP2p(IUsbP2pCallback callback, CallbackHandler handler, int actionType) {
        int requestId;
        Preconditions.checkNotNull(callback, "null callback.");
        int requestId2 = this.mCallbackIdMap.getOrDefault(callback, -1).intValue();
        Messenger messenger = new Messenger(handler);
        Binder binder = new Binder();
        if (actionType == 1) {
            try {
                requestId = this.mService.requestForUsbP2p(requestId2, messenger, binder);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            } catch (ServiceSpecificException e2) {
                throw new ConnectivityManager.TooManyRequestsException();
            }
        } else {
            requestId = this.mService.listenForUsbP2p(requestId2, messenger, binder);
        }
        this.mCallbackIdMap.put(callback, Integer.valueOf(requestId));
    }

    private void sendReleaseForUsbP2p(IUsbP2pCallback callback) {
        Integer requestId;
        Preconditions.checkNotNull(callback, "null callback");
        if (this.mCallbackIdMap.containsKey(callback) && (requestId = this.mCallbackIdMap.get(callback)) != null) {
            try {
                this.mService.releaseUsbP2pRequest(requestId.intValue());
            } catch (RemoteException e) {
                e.rethrowFromSystemServer();
            }
            this.mCallbackIdMap.remove(callback);
        }
    }

    /* access modifiers changed from: private */
    public final class CallbackHandler extends Handler {
        CallbackHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            Log.i(HwConnectivityExManager.TAG, "handleMessage: " + msg);
            super.handleMessage(msg);
            int state = msg.arg1;
            IUsbP2pCallback callback = getCallbackById(msg.arg2);
            if (callback == null) {
                Log.e(HwConnectivityExManager.TAG, "handleMessage: null callback");
                return;
            }
            int i = msg.what;
            if (i == 1) {
                if (state != -1) {
                    callback.onStateChange(state);
                }
                HwConnectivityExManager.this.mCallbackIdMap.remove(callback);
            } else if (i == 2) {
                callback.onStateChange(state);
            }
        }

        private IUsbP2pCallback getCallbackById(int requestId) {
            for (IUsbP2pCallback element : HwConnectivityExManager.this.mCallbackIdMap.keySet()) {
                Integer currentId = (Integer) HwConnectivityExManager.this.mCallbackIdMap.get(element);
                if (currentId != null && currentId.intValue() == requestId) {
                    return element;
                }
            }
            return null;
        }
    }

    /* access modifiers changed from: private */
    public class NetworkCallbackHandler extends Handler {
        private static final boolean DBG = false;
        private static final String TAG = "NetworkCallbackHandler";

        NetworkCallbackHandler(Looper looper) {
            super(looper);
        }

        NetworkCallbackHandler(HwConnectivityExManager hwConnectivityExManager, Handler handler) {
            this(((Handler) Preconditions.checkNotNull(handler, "Handler cannot be null.")).getLooper());
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            ConnectivityManager.NetworkCallback callback;
            NetworkRequest request = (NetworkRequest) getObject(message, NetworkRequest.class);
            Network network = (Network) getObject(message, Network.class);
            synchronized (HwConnectivityExManager.sSliceCallbacks) {
                callback = (ConnectivityManager.NetworkCallback) HwConnectivityExManager.sSliceCallbacks.get(request);
                if (callback == null) {
                    Log.w(TAG, "callback not found for " + ConnectivityManager.getCallbackName(message.what) + " message");
                    return;
                } else if (message.what == HwConnectivityExManager.SLICE_CALLBACK_UNAVAIL) {
                    HwConnectivityExManager.sSliceCallbacks.remove(request);
                    callback.setNetworkRequest(HwConnectivityExManager.ALREADY_UNREGISTERED);
                }
            }
            boolean blocked = true;
            switch (message.what) {
                case HwConnectivityExManager.SLICE_CALLBACK_PRECHECK /* 524289 */:
                    callback.onPreCheck(network);
                    return;
                case HwConnectivityExManager.SLICE_CALLBACK_AVAILABLE /* 524290 */:
                    NetworkCapabilities cap = (NetworkCapabilities) getObject(message, NetworkCapabilities.class);
                    LinkProperties lp = (LinkProperties) getObject(message, LinkProperties.class);
                    if (message.arg1 == 0) {
                        blocked = false;
                    }
                    callback.onAvailable(network, cap, lp, blocked);
                    return;
                case HwConnectivityExManager.SLICE_CALLBACK_LOSING /* 524291 */:
                    callback.onLosing(network, message.arg1);
                    return;
                case HwConnectivityExManager.SLICE_CALLBACK_LOST /* 524292 */:
                    callback.onLost(network);
                    return;
                case HwConnectivityExManager.SLICE_CALLBACK_UNAVAIL /* 524293 */:
                    callback.onUnavailable();
                    return;
                case HwConnectivityExManager.SLICE_CALLBACK_CAP_CHANGED /* 524294 */:
                    callback.onCapabilitiesChanged(network, (NetworkCapabilities) getObject(message, NetworkCapabilities.class));
                    return;
                case HwConnectivityExManager.SLICE_CALLBACK_IP_CHANGED /* 524295 */:
                    callback.onLinkPropertiesChanged(network, (LinkProperties) getObject(message, LinkProperties.class));
                    return;
                case 524296:
                default:
                    Log.w(TAG, "message type is not supported");
                    return;
                case HwConnectivityExManager.SLICE_CALLBACK_SUSPENDED /* 524297 */:
                    callback.onNetworkSuspended(network);
                    return;
                case HwConnectivityExManager.SLICE_CALLBACK_RESUMED /* 524298 */:
                    callback.onNetworkResumed(network);
                    return;
                case HwConnectivityExManager.SLICE_CALLBACK_BLK_CHANGED /* 524299 */:
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

    /* access modifiers changed from: private */
    public static final class CallbackThread extends HandlerThread {

        /* access modifiers changed from: private */
        public static class Singleton {
            private static final CallbackThread INSTANCE = CallbackThread.createInstance();

            private Singleton() {
            }
        }

        private CallbackThread() {
            super("CallbackThread");
        }

        /* access modifiers changed from: private */
        public static CallbackThread createInstance() {
            CallbackThread t = new CallbackThread();
            t.start();
            return t;
        }

        public static CallbackThread get() {
            return Singleton.INSTANCE;
        }

        public static Looper getInstanceLooper() {
            return Singleton.INSTANCE.getLooper();
        }
    }

    private CallbackHandler getDefaultHandler() {
        CallbackHandler callbackHandler;
        synchronized (this.mCallbackIdMap) {
            if (sCallbackHandler == null) {
                sCallbackHandler = new CallbackHandler(CallbackThread.getInstanceLooper());
            }
            callbackHandler = sCallbackHandler;
        }
        return callbackHandler;
    }

    private NetworkCallbackHandler getNetworkCallbackHandler() {
        NetworkCallbackHandler networkCallbackHandler;
        synchronized (sSliceCallbacks) {
            if (sNetworkCallbackHandler == null) {
                sNetworkCallbackHandler = new NetworkCallbackHandler(CallbackThread.getInstanceLooper());
            }
            networkCallbackHandler = sNetworkCallbackHandler;
        }
        return networkCallbackHandler;
    }
}
