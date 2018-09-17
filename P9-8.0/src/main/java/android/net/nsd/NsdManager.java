package android.net.nsd;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.util.AsyncChannel;
import java.util.concurrent.CountDownLatch;

public final class NsdManager {
    public static final String ACTION_NSD_STATE_CHANGED = "android.net.nsd.STATE_CHANGED";
    private static final int BASE = 393216;
    private static final int BUSY_LISTENER_KEY = -1;
    private static final boolean DBG = false;
    public static final int DISABLE = 393241;
    public static final int DISCOVER_SERVICES = 393217;
    public static final int DISCOVER_SERVICES_FAILED = 393219;
    public static final int DISCOVER_SERVICES_STARTED = 393218;
    public static final int ENABLE = 393240;
    private static final SparseArray<String> EVENT_NAMES = new SparseArray();
    public static final String EXTRA_NSD_STATE = "nsd_state";
    public static final int FAILURE_ALREADY_ACTIVE = 3;
    public static final int FAILURE_INTERNAL_ERROR = 0;
    public static final int FAILURE_MAX_LIMIT = 4;
    private static final int INVALID_LISTENER_KEY = 0;
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
    private final SparseArray<NsdServiceInfo> mServiceMap = new SparseArray();

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

    private class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 69632:
                    NsdManager.this.mAsyncChannel.sendMessage(69633);
                    return;
                case 69634:
                    NsdManager.this.mConnected.countDown();
                    return;
                case 69636:
                    Log.e(NsdManager.TAG, "Channel lost");
                    return;
                default:
                    Object listener = NsdManager.this.getListener(message.arg2);
                    if (listener == null) {
                        Log.d(NsdManager.TAG, "Stale key " + message.arg2);
                        return;
                    }
                    NsdServiceInfo ns = NsdManager.this.getNsdService(message.arg2);
                    switch (message.what) {
                        case NsdManager.DISCOVER_SERVICES_STARTED /*393218*/:
                            ((DiscoveryListener) listener).onDiscoveryStarted(NsdManager.this.getNsdServiceInfoType((NsdServiceInfo) message.obj));
                            break;
                        case NsdManager.DISCOVER_SERVICES_FAILED /*393219*/:
                            NsdManager.this.removeListener(message.arg2);
                            ((DiscoveryListener) listener).onStartDiscoveryFailed(NsdManager.this.getNsdServiceInfoType(ns), message.arg1);
                            break;
                        case NsdManager.SERVICE_FOUND /*393220*/:
                            ((DiscoveryListener) listener).onServiceFound((NsdServiceInfo) message.obj);
                            break;
                        case NsdManager.SERVICE_LOST /*393221*/:
                            ((DiscoveryListener) listener).onServiceLost((NsdServiceInfo) message.obj);
                            break;
                        case NsdManager.STOP_DISCOVERY_FAILED /*393223*/:
                            NsdManager.this.removeListener(message.arg2);
                            ((DiscoveryListener) listener).onStopDiscoveryFailed(NsdManager.this.getNsdServiceInfoType(ns), message.arg1);
                            break;
                        case NsdManager.STOP_DISCOVERY_SUCCEEDED /*393224*/:
                            NsdManager.this.removeListener(message.arg2);
                            ((DiscoveryListener) listener).onDiscoveryStopped(NsdManager.this.getNsdServiceInfoType(ns));
                            break;
                        case NsdManager.REGISTER_SERVICE_FAILED /*393226*/:
                            NsdManager.this.removeListener(message.arg2);
                            ((RegistrationListener) listener).onRegistrationFailed(ns, message.arg1);
                            break;
                        case NsdManager.REGISTER_SERVICE_SUCCEEDED /*393227*/:
                            ((RegistrationListener) listener).onServiceRegistered((NsdServiceInfo) message.obj);
                            break;
                        case NsdManager.UNREGISTER_SERVICE_FAILED /*393229*/:
                            NsdManager.this.removeListener(message.arg2);
                            ((RegistrationListener) listener).onUnregistrationFailed(ns, message.arg1);
                            break;
                        case NsdManager.UNREGISTER_SERVICE_SUCCEEDED /*393230*/:
                            NsdManager.this.removeListener(message.arg2);
                            ((RegistrationListener) listener).onServiceUnregistered(ns);
                            break;
                        case NsdManager.RESOLVE_SERVICE_FAILED /*393235*/:
                            NsdManager.this.removeListener(message.arg2);
                            ((ResolveListener) listener).onResolveFailed(ns, message.arg1);
                            break;
                        case NsdManager.RESOLVE_SERVICE_SUCCEEDED /*393236*/:
                            NsdManager.this.removeListener(message.arg2);
                            ((ResolveListener) listener).onServiceResolved((NsdServiceInfo) message.obj);
                            break;
                        default:
                            Log.d(NsdManager.TAG, "Ignored " + message);
                            break;
                    }
                    return;
            }
        }
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
        String name = (String) EVENT_NAMES.get(event);
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

    private int putListener(Object listener, NsdServiceInfo s) {
        if (listener == null) {
            return 0;
        }
        synchronized (this.mMapLock) {
            if (this.mListenerMap.indexOfValue(listener) != -1) {
                return -1;
            }
            int key;
            do {
                key = this.mListenerKey;
                this.mListenerKey = key + 1;
            } while (key == 0);
            this.mListenerMap.put(key, listener);
            this.mServiceMap.put(key, s);
            return key;
        }
    }

    private Object getListener(int key) {
        if (key == 0) {
            return null;
        }
        Object obj;
        synchronized (this.mMapLock) {
            obj = this.mListenerMap.get(key);
        }
        return obj;
    }

    private NsdServiceInfo getNsdService(int key) {
        NsdServiceInfo nsdServiceInfo;
        synchronized (this.mMapLock) {
            nsdServiceInfo = (NsdServiceInfo) this.mServiceMap.get(key);
        }
        return nsdServiceInfo;
    }

    private void removeListener(int key) {
        if (key != 0) {
            synchronized (this.mMapLock) {
                this.mListenerMap.remove(key);
                this.mServiceMap.remove(key);
            }
        }
    }

    private int getListenerKey(Object listener) {
        synchronized (this.mMapLock) {
            int valueIndex = this.mListenerMap.indexOfValue(listener);
            if (valueIndex != -1) {
                int keyAt = this.mListenerMap.keyAt(valueIndex);
                return keyAt;
            }
            return 0;
        }
    }

    private String getNsdServiceInfoType(NsdServiceInfo s) {
        if (s == null) {
            return "?";
        }
        return s.getServiceType();
    }

    private void init() {
        Messenger messenger = getMessenger();
        if (messenger == null) {
            throw new RuntimeException("Failed to initialize");
        }
        HandlerThread t = new HandlerThread("NsdManager");
        t.start();
        this.mHandler = new ServiceHandler(t.getLooper());
        this.mAsyncChannel.connect(this.mContext, this.mHandler, messenger);
        try {
            this.mConnected.await();
        } catch (InterruptedException e) {
            Log.e(TAG, "interrupted wait at init");
        }
    }

    public void registerService(NsdServiceInfo serviceInfo, int protocolType, RegistrationListener listener) {
        if (TextUtils.isEmpty(serviceInfo.getServiceName()) || TextUtils.isEmpty(serviceInfo.getServiceType())) {
            throw new IllegalArgumentException("Service name or type cannot be empty");
        } else if (serviceInfo.getPort() <= 0) {
            throw new IllegalArgumentException("Invalid port number");
        } else if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        } else if (protocolType != 1) {
            throw new IllegalArgumentException("Unsupported protocol");
        } else {
            int key = putListener(listener, serviceInfo);
            if (key == -1) {
                throw new IllegalArgumentException("listener already in use");
            }
            this.mAsyncChannel.sendMessage(REGISTER_SERVICE, 0, key, serviceInfo);
        }
    }

    public void unregisterService(RegistrationListener listener) {
        int id = getListenerKey(listener);
        if (id == 0) {
            throw new IllegalArgumentException("listener not registered");
        } else if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        } else {
            this.mAsyncChannel.sendMessage(UNREGISTER_SERVICE, 0, id);
        }
    }

    public void discoverServices(String serviceType, int protocolType, DiscoveryListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        } else if (TextUtils.isEmpty(serviceType)) {
            throw new IllegalArgumentException("Service type cannot be empty");
        } else if (protocolType != 1) {
            throw new IllegalArgumentException("Unsupported protocol");
        } else {
            NsdServiceInfo s = new NsdServiceInfo();
            s.setServiceType(serviceType);
            int key = putListener(listener, s);
            if (key == -1) {
                throw new IllegalArgumentException("listener already in use");
            }
            this.mAsyncChannel.sendMessage(DISCOVER_SERVICES, 0, key, s);
        }
    }

    public void stopServiceDiscovery(DiscoveryListener listener) {
        int id = getListenerKey(listener);
        if (id == 0) {
            throw new IllegalArgumentException("service discovery not active on listener");
        } else if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        } else {
            this.mAsyncChannel.sendMessage(STOP_DISCOVERY, 0, id);
        }
    }

    public void resolveService(NsdServiceInfo serviceInfo, ResolveListener listener) {
        if (TextUtils.isEmpty(serviceInfo.getServiceName()) || TextUtils.isEmpty(serviceInfo.getServiceType())) {
            throw new IllegalArgumentException("Service name or type cannot be empty");
        } else if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        } else {
            int key = putListener(listener, serviceInfo);
            if (key == -1) {
                throw new IllegalArgumentException("listener already in use");
            }
            this.mAsyncChannel.sendMessage(RESOLVE_SERVICE, 0, key, serviceInfo);
        }
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
}
