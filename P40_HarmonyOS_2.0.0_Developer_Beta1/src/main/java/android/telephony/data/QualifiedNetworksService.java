package android.telephony.data;

import android.annotation.SystemApi;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.Rlog;
import android.telephony.data.IQualifiedNetworksService;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import java.util.List;

@SystemApi
public abstract class QualifiedNetworksService extends Service {
    private static final int QNS_CREATE_NETWORK_AVAILABILITY_PROVIDER = 1;
    private static final int QNS_REMOVE_ALL_NETWORK_AVAILABILITY_PROVIDERS = 3;
    private static final int QNS_REMOVE_NETWORK_AVAILABILITY_PROVIDER = 2;
    private static final int QNS_UPDATE_QUALIFIED_NETWORKS = 4;
    public static final String QUALIFIED_NETWORKS_SERVICE_INTERFACE = "android.telephony.data.QualifiedNetworksService";
    private static final String TAG = QualifiedNetworksService.class.getSimpleName();
    @VisibleForTesting
    public final IQualifiedNetworksServiceWrapper mBinder = new IQualifiedNetworksServiceWrapper();
    private final QualifiedNetworksServiceHandler mHandler;
    private final HandlerThread mHandlerThread = new HandlerThread(TAG);
    private final SparseArray<NetworkAvailabilityProvider> mProviders = new SparseArray<>();

    public abstract NetworkAvailabilityProvider onCreateNetworkAvailabilityProvider(int i);

    public abstract class NetworkAvailabilityProvider implements AutoCloseable {
        private IQualifiedNetworksServiceCallback mCallback;
        private SparseArray<int[]> mQualifiedNetworkTypesList = new SparseArray<>();
        private final int mSlotIndex;

        @Override // java.lang.AutoCloseable
        public abstract void close();

        public NetworkAvailabilityProvider(int slotIndex) {
            this.mSlotIndex = slotIndex;
        }

        public final int getSlotIndex() {
            return this.mSlotIndex;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void registerForQualifiedNetworkTypesChanged(IQualifiedNetworksServiceCallback callback) {
            this.mCallback = callback;
            if (this.mCallback != null) {
                for (int i = 0; i < this.mQualifiedNetworkTypesList.size(); i++) {
                    try {
                        this.mCallback.onQualifiedNetworkTypesChanged(this.mQualifiedNetworkTypesList.keyAt(i), this.mQualifiedNetworkTypesList.valueAt(i));
                    } catch (RemoteException e) {
                        QualifiedNetworksService qualifiedNetworksService = QualifiedNetworksService.this;
                        qualifiedNetworksService.loge("Failed to call onQualifiedNetworksChanged. " + e);
                    }
                }
            }
        }

        public final void updateQualifiedNetworkTypes(int apnTypes, List<Integer> qualifiedNetworkTypes) {
            QualifiedNetworksService.this.mHandler.obtainMessage(4, this.mSlotIndex, apnTypes, qualifiedNetworkTypes.stream().mapToInt($$Lambda$QualifiedNetworksService$NetworkAvailabilityProvider$sNPqwkqArvqymBmHYmxAc4rF5Es.INSTANCE).toArray()).sendToTarget();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void onUpdateQualifiedNetworkTypes(int apnTypes, int[] qualifiedNetworkTypes) {
            this.mQualifiedNetworkTypesList.put(apnTypes, qualifiedNetworkTypes);
            IQualifiedNetworksServiceCallback iQualifiedNetworksServiceCallback = this.mCallback;
            if (iQualifiedNetworksServiceCallback != null) {
                try {
                    iQualifiedNetworksServiceCallback.onQualifiedNetworkTypesChanged(apnTypes, qualifiedNetworkTypes);
                } catch (RemoteException e) {
                    QualifiedNetworksService qualifiedNetworksService = QualifiedNetworksService.this;
                    qualifiedNetworksService.loge("Failed to call onQualifiedNetworksChanged. " + e);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class QualifiedNetworksServiceHandler extends Handler {
        QualifiedNetworksServiceHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int slotIndex = message.arg1;
            NetworkAvailabilityProvider provider = (NetworkAvailabilityProvider) QualifiedNetworksService.this.mProviders.get(slotIndex);
            int i = message.what;
            if (i != 1) {
                if (i != 2) {
                    if (i == 3) {
                        for (int i2 = 0; i2 < QualifiedNetworksService.this.mProviders.size(); i2++) {
                            NetworkAvailabilityProvider provider2 = (NetworkAvailabilityProvider) QualifiedNetworksService.this.mProviders.get(i2);
                            if (provider2 != null) {
                                provider2.close();
                            }
                        }
                        QualifiedNetworksService.this.mProviders.clear();
                    } else if (i == 4 && provider != null) {
                        provider.onUpdateQualifiedNetworkTypes(message.arg2, (int[]) message.obj);
                    }
                } else if (provider != null) {
                    provider.close();
                    QualifiedNetworksService.this.mProviders.remove(slotIndex);
                }
            } else if (QualifiedNetworksService.this.mProviders.get(slotIndex) != null) {
                QualifiedNetworksService qualifiedNetworksService = QualifiedNetworksService.this;
                qualifiedNetworksService.loge("Network availability provider for slot " + slotIndex + " already existed.");
            } else {
                NetworkAvailabilityProvider provider3 = QualifiedNetworksService.this.onCreateNetworkAvailabilityProvider(slotIndex);
                if (provider3 != null) {
                    QualifiedNetworksService.this.mProviders.put(slotIndex, provider3);
                    provider3.registerForQualifiedNetworkTypesChanged((IQualifiedNetworksServiceCallback) message.obj);
                    return;
                }
                QualifiedNetworksService qualifiedNetworksService2 = QualifiedNetworksService.this;
                qualifiedNetworksService2.loge("Failed to create network availability provider. slot index = " + slotIndex);
            }
        }
    }

    public QualifiedNetworksService() {
        this.mHandlerThread.start();
        this.mHandler = new QualifiedNetworksServiceHandler(this.mHandlerThread.getLooper());
        log("Qualified networks service created");
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        if (intent != null && QUALIFIED_NETWORKS_SERVICE_INTERFACE.equals(intent.getAction())) {
            return this.mBinder;
        }
        loge("Unexpected intent " + intent);
        return null;
    }

    @Override // android.app.Service
    public boolean onUnbind(Intent intent) {
        this.mHandler.obtainMessage(3).sendToTarget();
        return false;
    }

    @Override // android.app.Service
    public void onDestroy() {
        this.mHandlerThread.quit();
    }

    private class IQualifiedNetworksServiceWrapper extends IQualifiedNetworksService.Stub {
        private IQualifiedNetworksServiceWrapper() {
        }

        @Override // android.telephony.data.IQualifiedNetworksService
        public void createNetworkAvailabilityProvider(int slotIndex, IQualifiedNetworksServiceCallback callback) {
            QualifiedNetworksService.this.mHandler.obtainMessage(1, slotIndex, 0, callback).sendToTarget();
        }

        @Override // android.telephony.data.IQualifiedNetworksService
        public void removeNetworkAvailabilityProvider(int slotIndex) {
            QualifiedNetworksService.this.mHandler.obtainMessage(2, slotIndex, 0).sendToTarget();
        }
    }

    private void log(String s) {
        Rlog.d(TAG, s);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loge(String s) {
        Rlog.e(TAG, s);
    }
}
