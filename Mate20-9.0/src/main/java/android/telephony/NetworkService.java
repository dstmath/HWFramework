package android.telephony;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.INetworkService;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;

public abstract class NetworkService extends Service {
    private static final int NETWORK_SERVICE_CREATE_NETWORK_SERVICE_PROVIDER = 1;
    public static final String NETWORK_SERVICE_EXTRA_SLOT_ID = "android.telephony.extra.SLOT_ID";
    private static final int NETWORK_SERVICE_GET_REGISTRATION_STATE = 4;
    private static final int NETWORK_SERVICE_INDICATION_NETWORK_STATE_CHANGED = 7;
    public static final String NETWORK_SERVICE_INTERFACE = "android.telephony.NetworkService";
    private static final int NETWORK_SERVICE_REGISTER_FOR_STATE_CHANGE = 5;
    private static final int NETWORK_SERVICE_REMOVE_ALL_NETWORK_SERVICE_PROVIDERS = 3;
    private static final int NETWORK_SERVICE_REMOVE_NETWORK_SERVICE_PROVIDER = 2;
    private static final int NETWORK_SERVICE_UNREGISTER_FOR_STATE_CHANGE = 6;
    private final String TAG = NetworkService.class.getSimpleName();
    @VisibleForTesting
    public final INetworkServiceWrapper mBinder = new INetworkServiceWrapper();
    /* access modifiers changed from: private */
    public final NetworkServiceHandler mHandler;
    private final HandlerThread mHandlerThread = new HandlerThread(this.TAG);
    /* access modifiers changed from: private */
    public final SparseArray<NetworkServiceProvider> mServiceMap = new SparseArray<>();

    private class INetworkServiceWrapper extends INetworkService.Stub {
        private INetworkServiceWrapper() {
        }

        public void createNetworkServiceProvider(int slotId) {
            NetworkService.this.mHandler.obtainMessage(1, slotId, 0, null).sendToTarget();
        }

        public void removeNetworkServiceProvider(int slotId) {
            NetworkService.this.mHandler.obtainMessage(2, slotId, 0, null).sendToTarget();
        }

        public void getNetworkRegistrationState(int slotId, int domain, INetworkServiceCallback callback) {
            NetworkService.this.mHandler.obtainMessage(4, slotId, domain, callback).sendToTarget();
        }

        public void registerForNetworkRegistrationStateChanged(int slotId, INetworkServiceCallback callback) {
            NetworkService.this.mHandler.obtainMessage(5, slotId, 0, callback).sendToTarget();
        }

        public void unregisterForNetworkRegistrationStateChanged(int slotId, INetworkServiceCallback callback) {
            NetworkService.this.mHandler.obtainMessage(6, slotId, 0, callback).sendToTarget();
        }
    }

    private class NetworkServiceHandler extends Handler {
        NetworkServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            int slotId = message.arg1;
            INetworkServiceCallback callback = (INetworkServiceCallback) message.obj;
            NetworkServiceProvider serviceProvider = (NetworkServiceProvider) NetworkService.this.mServiceMap.get(slotId);
            switch (message.what) {
                case 1:
                    if (serviceProvider == null) {
                        NetworkService.this.mServiceMap.put(slotId, NetworkService.this.createNetworkServiceProvider(slotId));
                        return;
                    }
                    return;
                case 2:
                    if (serviceProvider != null) {
                        serviceProvider.onDestroy();
                        NetworkService.this.mServiceMap.remove(slotId);
                        return;
                    }
                    return;
                case 3:
                    for (int i = 0; i < NetworkService.this.mServiceMap.size(); i++) {
                        NetworkServiceProvider serviceProvider2 = (NetworkServiceProvider) NetworkService.this.mServiceMap.get(i);
                        if (serviceProvider2 != null) {
                            serviceProvider2.onDestroy();
                        }
                    }
                    NetworkService.this.mServiceMap.clear();
                    return;
                case 4:
                    if (serviceProvider != null) {
                        serviceProvider.getNetworkRegistrationState(message.arg2, new NetworkServiceCallback(callback));
                        return;
                    }
                    return;
                case 5:
                    if (serviceProvider != null) {
                        serviceProvider.registerForStateChanged(callback);
                        return;
                    }
                    return;
                case 6:
                    if (serviceProvider != null) {
                        serviceProvider.unregisterForStateChanged(callback);
                        return;
                    }
                    return;
                case 7:
                    if (serviceProvider != null) {
                        serviceProvider.notifyStateChangedToCallbacks();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public class NetworkServiceProvider {
        private final List<INetworkServiceCallback> mNetworkRegistrationStateChangedCallbacks = new ArrayList();
        private final int mSlotId;

        public NetworkServiceProvider(int slotId) {
            this.mSlotId = slotId;
        }

        public final int getSlotId() {
            return this.mSlotId;
        }

        public void getNetworkRegistrationState(int domain, NetworkServiceCallback callback) {
            callback.onGetNetworkRegistrationStateComplete(1, null);
        }

        public final void notifyNetworkRegistrationStateChanged() {
            NetworkService.this.mHandler.obtainMessage(7, this.mSlotId, 0, null).sendToTarget();
        }

        /* access modifiers changed from: private */
        public void registerForStateChanged(INetworkServiceCallback callback) {
            synchronized (this.mNetworkRegistrationStateChangedCallbacks) {
                this.mNetworkRegistrationStateChangedCallbacks.add(callback);
            }
        }

        /* access modifiers changed from: private */
        public void unregisterForStateChanged(INetworkServiceCallback callback) {
            synchronized (this.mNetworkRegistrationStateChangedCallbacks) {
                this.mNetworkRegistrationStateChangedCallbacks.remove(callback);
            }
        }

        /* access modifiers changed from: private */
        public void notifyStateChangedToCallbacks() {
            for (INetworkServiceCallback callback : this.mNetworkRegistrationStateChangedCallbacks) {
                try {
                    callback.onNetworkStateChanged();
                } catch (RemoteException e) {
                }
            }
        }

        /* access modifiers changed from: protected */
        public void onDestroy() {
            this.mNetworkRegistrationStateChangedCallbacks.clear();
        }
    }

    /* access modifiers changed from: protected */
    public abstract NetworkServiceProvider createNetworkServiceProvider(int i);

    public NetworkService() {
        this.mHandlerThread.start();
        this.mHandler = new NetworkServiceHandler(this.mHandlerThread.getLooper());
        log("network service created");
    }

    public IBinder onBind(Intent intent) {
        if (intent != null && NETWORK_SERVICE_INTERFACE.equals(intent.getAction())) {
            return this.mBinder;
        }
        loge("Unexpected intent " + intent);
        return null;
    }

    public boolean onUnbind(Intent intent) {
        this.mHandler.obtainMessage(3, 0, 0, null).sendToTarget();
        return false;
    }

    public void onDestroy() {
        this.mHandlerThread.quit();
    }

    private final void log(String s) {
        Rlog.d(this.TAG, s);
    }

    private final void loge(String s) {
        Rlog.e(this.TAG, s);
    }
}
