package android.net.nsd;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.Preconditions;
import java.util.concurrent.CountDownLatch;

public final class NsdManager {
    public static final String ACTION_NSD_STATE_CHANGED = "android.net.nsd.STATE_CHANGED";
    private static final int BASE = 393216;
    private static final boolean DBG = false;
    public static final int DISABLE = 393241;
    public static final int DISCOVER_SERVICES = 393217;
    public static final int DISCOVER_SERVICES_FAILED = 393219;
    public static final int DISCOVER_SERVICES_STARTED = 393218;
    public static final int ENABLE = 393240;
    private static final SparseArray<String> EVENT_NAMES = new SparseArray<>();
    public static final String EXTRA_NSD_STATE = "nsd_state";
    public static final int FAILURE_ALREADY_ACTIVE = 3;
    public static final int FAILURE_INTERNAL_ERROR = 0;
    public static final int FAILURE_MAX_LIMIT = 4;
    private static final int FIRST_LISTENER_KEY = 1;
    public static final int NATIVE_DAEMON_EVENT = 393242;
    public static final int NSD_STATE_DISABLED = 1;
    public static final int NSD_STATE_ENABLED = 2;
    public static final int PROTOCOL_DNS_SD = 1;
    public static final int REGISTER_SERVICE = 393225;
    public static final int REGISTER_SERVICE_FAILED = 393226;
    public static final int REGISTER_SERVICE_SUCCEEDED = 393227;
    public static final int RESOLVE_SERVICE = 393234;
    public static final int RESOLVE_SERVICE_FAILED = 393235;
    public static final int RESOLVE_SERVICE_SUCCEEDED = 393236;
    public static final int SERVICE_FOUND = 393220;
    public static final int SERVICE_LOST = 393221;
    public static final int STOP_DISCOVERY = 393222;
    public static final int STOP_DISCOVERY_FAILED = 393223;
    public static final int STOP_DISCOVERY_SUCCEEDED = 393224;
    private static final String TAG = NsdManager.class.getSimpleName();
    public static final int UNREGISTER_SERVICE = 393228;
    public static final int UNREGISTER_SERVICE_FAILED = 393229;
    public static final int UNREGISTER_SERVICE_SUCCEEDED = 393230;
    private final AsyncChannel mAsyncChannel = new AsyncChannel();
    private final CountDownLatch mConnected = new CountDownLatch(1);
    private final Context mContext;
    private ServiceHandler mHandler;
    private int mListenerKey = 1;
    private final SparseArray mListenerMap = new SparseArray();
    private final Object mMapLock = new Object();
    private final INsdManager mService;
    private final SparseArray<NsdServiceInfo> mServiceMap = new SparseArray<>();

    public interface DiscoveryListener {
        void onDiscoveryStarted(String str);

        void onDiscoveryStopped(String str);

        void onServiceFound(NsdServiceInfo nsdServiceInfo);

        void onServiceLost(NsdServiceInfo nsdServiceInfo);

        void onStartDiscoveryFailed(String str, int i);

        void onStopDiscoveryFailed(String str, int i);
    }

    public interface RegistrationListener {
        void onRegistrationFailed(NsdServiceInfo nsdServiceInfo, int i);

        void onServiceRegistered(NsdServiceInfo nsdServiceInfo);

        void onServiceUnregistered(NsdServiceInfo nsdServiceInfo);

        void onUnregistrationFailed(NsdServiceInfo nsdServiceInfo, int i);
    }

    public interface ResolveListener {
        void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i);

        void onServiceResolved(NsdServiceInfo nsdServiceInfo);
    }

    static {
        EVENT_NAMES.put(DISCOVER_SERVICES, "DISCOVER_SERVICES");
        EVENT_NAMES.put(DISCOVER_SERVICES_STARTED, "DISCOVER_SERVICES_STARTED");
        EVENT_NAMES.put(DISCOVER_SERVICES_FAILED, "DISCOVER_SERVICES_FAILED");
        EVENT_NAMES.put(SERVICE_FOUND, "SERVICE_FOUND");
        EVENT_NAMES.put(SERVICE_LOST, "SERVICE_LOST");
        EVENT_NAMES.put(STOP_DISCOVERY, "STOP_DISCOVERY");
        EVENT_NAMES.put(STOP_DISCOVERY_FAILED, "STOP_DISCOVERY_FAILED");
        EVENT_NAMES.put(STOP_DISCOVERY_SUCCEEDED, "STOP_DISCOVERY_SUCCEEDED");
        EVENT_NAMES.put(REGISTER_SERVICE, "REGISTER_SERVICE");
        EVENT_NAMES.put(REGISTER_SERVICE_FAILED, "REGISTER_SERVICE_FAILED");
        EVENT_NAMES.put(REGISTER_SERVICE_SUCCEEDED, "REGISTER_SERVICE_SUCCEEDED");
        EVENT_NAMES.put(UNREGISTER_SERVICE, "UNREGISTER_SERVICE");
        EVENT_NAMES.put(UNREGISTER_SERVICE_FAILED, "UNREGISTER_SERVICE_FAILED");
        EVENT_NAMES.put(UNREGISTER_SERVICE_SUCCEEDED, "UNREGISTER_SERVICE_SUCCEEDED");
        EVENT_NAMES.put(RESOLVE_SERVICE, "RESOLVE_SERVICE");
        EVENT_NAMES.put(RESOLVE_SERVICE_FAILED, "RESOLVE_SERVICE_FAILED");
        EVENT_NAMES.put(RESOLVE_SERVICE_SUCCEEDED, "RESOLVE_SERVICE_SUCCEEDED");
        EVENT_NAMES.put(ENABLE, "ENABLE");
        EVENT_NAMES.put(DISABLE, "DISABLE");
        EVENT_NAMES.put(NATIVE_DAEMON_EVENT, "NATIVE_DAEMON_EVENT");
    }

    public static String nameOf(int event) {
        String name = EVENT_NAMES.get(event);
        if (name == null) {
            return Integer.toString(event);
        }
        return name;
    }

    public NsdManager(Context context, INsdManager service) {
        this.mService = service;
        this.mContext = context;
        init();
    }

    @VisibleForTesting
    public void disconnect() {
        this.mAsyncChannel.disconnect();
        this.mHandler.getLooper().quitSafely();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            Object listener;
            NsdServiceInfo ns;
            int what = message.what;
            int key = message.arg2;
            if (what == 69632) {
                NsdManager.this.mAsyncChannel.sendMessage(AsyncChannel.CMD_CHANNEL_FULL_CONNECTION);
            } else if (what == 69634) {
                NsdManager.this.mConnected.countDown();
            } else if (what != 69636) {
                synchronized (NsdManager.this.mMapLock) {
                    listener = NsdManager.this.mListenerMap.get(key);
                    ns = (NsdServiceInfo) NsdManager.this.mServiceMap.get(key);
                }
                if (listener == null) {
                    String str = NsdManager.TAG;
                    Log.d(str, "Stale key " + message.arg2);
                    return;
                }
                switch (what) {
                    case NsdManager.DISCOVER_SERVICES_STARTED /*{ENCODED_INT: 393218}*/:
                        ((DiscoveryListener) listener).onDiscoveryStarted(NsdManager.getNsdServiceInfoType((NsdServiceInfo) message.obj));
                        return;
                    case NsdManager.DISCOVER_SERVICES_FAILED /*{ENCODED_INT: 393219}*/:
                        NsdManager.this.removeListener(key);
                        ((DiscoveryListener) listener).onStartDiscoveryFailed(NsdManager.getNsdServiceInfoType(ns), message.arg1);
                        return;
                    case NsdManager.SERVICE_FOUND /*{ENCODED_INT: 393220}*/:
                        ((DiscoveryListener) listener).onServiceFound((NsdServiceInfo) message.obj);
                        return;
                    case NsdManager.SERVICE_LOST /*{ENCODED_INT: 393221}*/:
                        ((DiscoveryListener) listener).onServiceLost((NsdServiceInfo) message.obj);
                        return;
                    case NsdManager.STOP_DISCOVERY /*{ENCODED_INT: 393222}*/:
                    case NsdManager.REGISTER_SERVICE /*{ENCODED_INT: 393225}*/:
                    case NsdManager.UNREGISTER_SERVICE /*{ENCODED_INT: 393228}*/:
                    case 393231:
                    case 393232:
                    case 393233:
                    case NsdManager.RESOLVE_SERVICE /*{ENCODED_INT: 393234}*/:
                    default:
                        String str2 = NsdManager.TAG;
                        Log.d(str2, "Ignored " + message);
                        return;
                    case NsdManager.STOP_DISCOVERY_FAILED /*{ENCODED_INT: 393223}*/:
                        NsdManager.this.removeListener(key);
                        ((DiscoveryListener) listener).onStopDiscoveryFailed(NsdManager.getNsdServiceInfoType(ns), message.arg1);
                        return;
                    case NsdManager.STOP_DISCOVERY_SUCCEEDED /*{ENCODED_INT: 393224}*/:
                        NsdManager.this.removeListener(key);
                        ((DiscoveryListener) listener).onDiscoveryStopped(NsdManager.getNsdServiceInfoType(ns));
                        return;
                    case NsdManager.REGISTER_SERVICE_FAILED /*{ENCODED_INT: 393226}*/:
                        NsdManager.this.removeListener(key);
                        ((RegistrationListener) listener).onRegistrationFailed(ns, message.arg1);
                        return;
                    case NsdManager.REGISTER_SERVICE_SUCCEEDED /*{ENCODED_INT: 393227}*/:
                        ((RegistrationListener) listener).onServiceRegistered((NsdServiceInfo) message.obj);
                        return;
                    case NsdManager.UNREGISTER_SERVICE_FAILED /*{ENCODED_INT: 393229}*/:
                        NsdManager.this.removeListener(key);
                        ((RegistrationListener) listener).onUnregistrationFailed(ns, message.arg1);
                        return;
                    case NsdManager.UNREGISTER_SERVICE_SUCCEEDED /*{ENCODED_INT: 393230}*/:
                        NsdManager.this.removeListener(message.arg2);
                        ((RegistrationListener) listener).onServiceUnregistered(ns);
                        return;
                    case NsdManager.RESOLVE_SERVICE_FAILED /*{ENCODED_INT: 393235}*/:
                        NsdManager.this.removeListener(key);
                        ((ResolveListener) listener).onResolveFailed(ns, message.arg1);
                        return;
                    case NsdManager.RESOLVE_SERVICE_SUCCEEDED /*{ENCODED_INT: 393236}*/:
                        NsdManager.this.removeListener(key);
                        ((ResolveListener) listener).onServiceResolved((NsdServiceInfo) message.obj);
                        return;
                }
            } else {
                Log.e(NsdManager.TAG, "Channel lost");
            }
        }
    }

    private int nextListenerKey() {
        this.mListenerKey = Math.max(1, this.mListenerKey + 1);
        return this.mListenerKey;
    }

    private int putListener(Object listener, NsdServiceInfo s) {
        int key;
        checkListener(listener);
        synchronized (this.mMapLock) {
            Preconditions.checkArgument(this.mListenerMap.indexOfValue(listener) == -1, "listener already in use");
            key = nextListenerKey();
            this.mListenerMap.put(key, listener);
            this.mServiceMap.put(key, s);
        }
        return key;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeListener(int key) {
        synchronized (this.mMapLock) {
            this.mListenerMap.remove(key);
            this.mServiceMap.remove(key);
        }
    }

    private int getListenerKey(Object listener) {
        int keyAt;
        checkListener(listener);
        synchronized (this.mMapLock) {
            int valueIndex = this.mListenerMap.indexOfValue(listener);
            Preconditions.checkArgument(valueIndex != -1, "listener not registered");
            keyAt = this.mListenerMap.keyAt(valueIndex);
        }
        return keyAt;
    }

    /* access modifiers changed from: private */
    public static String getNsdServiceInfoType(NsdServiceInfo s) {
        if (s == null) {
            return "?";
        }
        return s.getServiceType();
    }

    private void init() {
        Messenger messenger = getMessenger();
        if (messenger == null) {
            fatal("Failed to obtain service Messenger");
        }
        HandlerThread t = new HandlerThread("NsdManager");
        t.start();
        this.mHandler = new ServiceHandler(t.getLooper());
        this.mAsyncChannel.connect(this.mContext, this.mHandler, messenger);
        try {
            this.mConnected.await();
        } catch (InterruptedException e) {
            fatal("Interrupted wait at init");
        }
    }

    private static void fatal(String msg) {
        Log.e(TAG, msg);
        throw new RuntimeException(msg);
    }

    public void registerService(NsdServiceInfo serviceInfo, int protocolType, RegistrationListener listener) {
        Preconditions.checkArgument(serviceInfo.getPort() > 0, "Invalid port number");
        checkServiceInfo(serviceInfo);
        checkProtocol(protocolType);
        this.mAsyncChannel.sendMessage(REGISTER_SERVICE, 0, putListener(listener, serviceInfo), serviceInfo);
    }

    public void unregisterService(RegistrationListener listener) {
        this.mAsyncChannel.sendMessage(UNREGISTER_SERVICE, 0, getListenerKey(listener));
    }

    public void discoverServices(String serviceType, int protocolType, DiscoveryListener listener) {
        Preconditions.checkStringNotEmpty(serviceType, "Service type cannot be empty");
        checkProtocol(protocolType);
        NsdServiceInfo s = new NsdServiceInfo();
        s.setServiceType(serviceType);
        this.mAsyncChannel.sendMessage(DISCOVER_SERVICES, 0, putListener(listener, s), s);
    }

    public void stopServiceDiscovery(DiscoveryListener listener) {
        this.mAsyncChannel.sendMessage(STOP_DISCOVERY, 0, getListenerKey(listener));
    }

    public void resolveService(NsdServiceInfo serviceInfo, ResolveListener listener) {
        checkServiceInfo(serviceInfo);
        this.mAsyncChannel.sendMessage(RESOLVE_SERVICE, 0, putListener(listener, serviceInfo), serviceInfo);
    }

    public void setEnabled(boolean enabled) {
        try {
            this.mService.setEnabled(enabled);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private Messenger getMessenger() {
        try {
            return this.mService.getMessenger();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private static void checkListener(Object listener) {
        Preconditions.checkNotNull(listener, "listener cannot be null");
    }

    private static void checkProtocol(int protocolType) {
        boolean z = true;
        if (protocolType != 1) {
            z = false;
        }
        Preconditions.checkArgument(z, "Unsupported protocol");
    }

    private static void checkServiceInfo(NsdServiceInfo serviceInfo) {
        Preconditions.checkNotNull(serviceInfo, "NsdServiceInfo cannot be null");
        Preconditions.checkStringNotEmpty(serviceInfo.getServiceName(), "Service name cannot be empty");
        Preconditions.checkStringNotEmpty(serviceInfo.getServiceType(), "Service type cannot be empty");
    }
}
