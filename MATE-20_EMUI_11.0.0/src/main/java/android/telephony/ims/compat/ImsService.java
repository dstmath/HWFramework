package android.telephony.ims.compat;

import android.annotation.UnsupportedAppUsage;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.ims.compat.feature.ImsFeature;
import android.telephony.ims.compat.feature.MMTelFeature;
import android.telephony.ims.compat.feature.RcsFeature;
import android.util.Log;
import android.util.SparseArray;
import com.android.ims.internal.IImsFeatureStatusCallback;
import com.android.ims.internal.IImsMMTelFeature;
import com.android.ims.internal.IImsRcsFeature;
import com.android.ims.internal.IImsServiceController;
import com.android.internal.annotations.VisibleForTesting;

public class ImsService extends Service {
    private static final String LOG_TAG = "ImsService(Compat)";
    public static final String SERVICE_INTERFACE = "android.telephony.ims.compat.ImsService";
    private final SparseArray<SparseArray<ImsFeature>> mFeaturesBySlot = new SparseArray<>();
    @UnsupportedAppUsage
    protected final IBinder mImsServiceController = new IImsServiceController.Stub() {
        /* class android.telephony.ims.compat.ImsService.AnonymousClass1 */

        @Override // com.android.ims.internal.IImsServiceController
        public IImsMMTelFeature createEmergencyMMTelFeature(int slotId, IImsFeatureStatusCallback c) {
            return ImsService.this.createEmergencyMMTelFeatureInternal(slotId, c);
        }

        @Override // com.android.ims.internal.IImsServiceController
        public IImsMMTelFeature createMMTelFeature(int slotId, IImsFeatureStatusCallback c) {
            return ImsService.this.createMMTelFeatureInternal(slotId, c);
        }

        @Override // com.android.ims.internal.IImsServiceController
        public IImsRcsFeature createRcsFeature(int slotId, IImsFeatureStatusCallback c) {
            return ImsService.this.createRcsFeatureInternal(slotId, c);
        }

        @Override // com.android.ims.internal.IImsServiceController
        public void removeImsFeature(int slotId, int featureType, IImsFeatureStatusCallback c) throws RemoteException {
            ImsService.this.removeImsFeature(slotId, featureType, c);
        }
    };

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        if (!SERVICE_INTERFACE.equals(intent.getAction())) {
            return null;
        }
        Log.i(LOG_TAG, "ImsService(Compat) Bound.");
        return this.mImsServiceController;
    }

    @VisibleForTesting
    public SparseArray<ImsFeature> getFeatures(int slotId) {
        return this.mFeaturesBySlot.get(slotId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private IImsMMTelFeature createEmergencyMMTelFeatureInternal(int slotId, IImsFeatureStatusCallback c) {
        MMTelFeature f = onCreateEmergencyMMTelImsFeature(slotId);
        if (f == null) {
            return null;
        }
        setupFeature(f, slotId, 0, c);
        return f.getBinder();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private IImsMMTelFeature createMMTelFeatureInternal(int slotId, IImsFeatureStatusCallback c) {
        MMTelFeature f = onCreateMMTelImsFeature(slotId);
        if (f == null) {
            return null;
        }
        setupFeature(f, slotId, 1, c);
        return f.getBinder();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private IImsRcsFeature createRcsFeatureInternal(int slotId, IImsFeatureStatusCallback c) {
        RcsFeature f = onCreateRcsFeature(slotId);
        if (f == null) {
            return null;
        }
        setupFeature(f, slotId, 2, c);
        return f.getBinder();
    }

    private void setupFeature(ImsFeature f, int slotId, int featureType, IImsFeatureStatusCallback c) {
        f.setContext(this);
        f.setSlotId(slotId);
        f.addImsFeatureStatusCallback(c);
        addImsFeature(slotId, featureType, f);
        f.onFeatureReady();
    }

    private void addImsFeature(int slotId, int featureType, ImsFeature f) {
        synchronized (this.mFeaturesBySlot) {
            SparseArray<ImsFeature> features = this.mFeaturesBySlot.get(slotId);
            if (features == null) {
                features = new SparseArray<>();
                this.mFeaturesBySlot.put(slotId, features);
            }
            features.put(featureType, f);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeImsFeature(int slotId, int featureType, IImsFeatureStatusCallback c) {
        synchronized (this.mFeaturesBySlot) {
            SparseArray<ImsFeature> features = this.mFeaturesBySlot.get(slotId);
            if (features == null) {
                Log.w(LOG_TAG, "Can not remove ImsFeature. No ImsFeatures exist on slot " + slotId);
                return;
            }
            ImsFeature f = features.get(featureType);
            if (f == null) {
                Log.w(LOG_TAG, "Can not remove ImsFeature. No feature with type " + featureType + " exists on slot " + slotId);
                return;
            }
            f.removeImsFeatureStatusCallback(c);
            f.onFeatureRemoved();
            features.remove(featureType);
        }
    }

    public MMTelFeature onCreateEmergencyMMTelImsFeature(int slotId) {
        return null;
    }

    public MMTelFeature onCreateMMTelImsFeature(int slotId) {
        return null;
    }

    public RcsFeature onCreateRcsFeature(int slotId) {
        return null;
    }
}
