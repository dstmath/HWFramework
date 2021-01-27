package android.telephony.ims;

import android.annotation.SystemApi;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.ims.aidl.IImsConfig;
import android.telephony.ims.aidl.IImsMmTelFeature;
import android.telephony.ims.aidl.IImsRcsFeature;
import android.telephony.ims.aidl.IImsRegistration;
import android.telephony.ims.aidl.IImsServiceController;
import android.telephony.ims.aidl.IImsServiceControllerListener;
import android.telephony.ims.feature.ImsFeature;
import android.telephony.ims.feature.MmTelFeature;
import android.telephony.ims.feature.RcsFeature;
import android.telephony.ims.stub.ImsConfigImplBase;
import android.telephony.ims.stub.ImsFeatureConfiguration;
import android.telephony.ims.stub.ImsRegistrationImplBase;
import android.util.Log;
import android.util.SparseArray;
import com.android.ims.internal.IImsFeatureStatusCallback;
import com.android.internal.annotations.VisibleForTesting;

@SystemApi
public class ImsService extends Service {
    private static final String LOG_TAG = "ImsService";
    public static final String SERVICE_INTERFACE = "android.telephony.ims.ImsService";
    private final SparseArray<SparseArray<ImsFeature>> mFeaturesBySlot = new SparseArray<>();
    protected final IBinder mImsServiceController = new IImsServiceController.Stub() {
        /* class android.telephony.ims.ImsService.AnonymousClass1 */

        @Override // android.telephony.ims.aidl.IImsServiceController
        public void setListener(IImsServiceControllerListener l) {
            ImsService.this.mListener = l;
        }

        @Override // android.telephony.ims.aidl.IImsServiceController
        public IImsMmTelFeature createMmTelFeature(int slotId, IImsFeatureStatusCallback c) {
            return ImsService.this.createMmTelFeatureInternal(slotId, c);
        }

        @Override // android.telephony.ims.aidl.IImsServiceController
        public IImsRcsFeature createRcsFeature(int slotId, IImsFeatureStatusCallback c) {
            return ImsService.this.createRcsFeatureInternal(slotId, c);
        }

        @Override // android.telephony.ims.aidl.IImsServiceController
        public void removeImsFeature(int slotId, int featureType, IImsFeatureStatusCallback c) {
            ImsService.this.removeImsFeature(slotId, featureType, c);
        }

        @Override // android.telephony.ims.aidl.IImsServiceController
        public ImsFeatureConfiguration querySupportedImsFeatures() {
            return ImsService.this.querySupportedImsFeatures();
        }

        @Override // android.telephony.ims.aidl.IImsServiceController
        public void notifyImsServiceReadyForFeatureCreation() {
            ImsService.this.readyForFeatureCreation();
        }

        @Override // android.telephony.ims.aidl.IImsServiceController
        public IImsConfig getConfig(int slotId) {
            ImsConfigImplBase c = ImsService.this.getConfig(slotId);
            if (c != null) {
                return c.getIImsConfig();
            }
            return null;
        }

        @Override // android.telephony.ims.aidl.IImsServiceController
        public IImsRegistration getRegistration(int slotId) {
            ImsRegistrationImplBase r = ImsService.this.getRegistration(slotId);
            if (r != null) {
                return r.getBinder();
            }
            return null;
        }

        @Override // android.telephony.ims.aidl.IImsServiceController
        public void enableIms(int slotId) {
            ImsService.this.enableIms(slotId);
        }

        @Override // android.telephony.ims.aidl.IImsServiceController
        public void disableIms(int slotId) {
            ImsService.this.disableIms(slotId);
        }
    };
    private IImsServiceControllerListener mListener;

    public static class Listener extends IImsServiceControllerListener.Stub {
        @Override // android.telephony.ims.aidl.IImsServiceControllerListener
        public void onUpdateSupportedImsFeatures(ImsFeatureConfiguration c) {
        }
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        if (!SERVICE_INTERFACE.equals(intent.getAction())) {
            return null;
        }
        Log.i(LOG_TAG, "ImsService Bound.");
        return this.mImsServiceController;
    }

    @VisibleForTesting
    public SparseArray<ImsFeature> getFeatures(int slotId) {
        return this.mFeaturesBySlot.get(slotId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private IImsMmTelFeature createMmTelFeatureInternal(int slotId, IImsFeatureStatusCallback c) {
        MmTelFeature f = createMmTelFeature(slotId);
        if (f != null) {
            setupFeature(f, slotId, 1, c);
            return f.getBinder();
        }
        Log.e(LOG_TAG, "createMmTelFeatureInternal: null feature returned.");
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private IImsRcsFeature createRcsFeatureInternal(int slotId, IImsFeatureStatusCallback c) {
        RcsFeature f = createRcsFeature(slotId);
        if (f != null) {
            setupFeature(f, slotId, 2, c);
            return f.getBinder();
        }
        Log.e(LOG_TAG, "createRcsFeatureInternal: null feature returned.");
        return null;
    }

    private void setupFeature(ImsFeature f, int slotId, int featureType, IImsFeatureStatusCallback c) {
        f.initialize(this, slotId);
        f.addImsFeatureStatusCallback(c);
        addImsFeature(slotId, featureType, f);
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

    public ImsFeatureConfiguration querySupportedImsFeatures() {
        return new ImsFeatureConfiguration();
    }

    public final void onUpdateSupportedImsFeatures(ImsFeatureConfiguration c) throws RemoteException {
        IImsServiceControllerListener iImsServiceControllerListener = this.mListener;
        if (iImsServiceControllerListener != null) {
            iImsServiceControllerListener.onUpdateSupportedImsFeatures(c);
            return;
        }
        throw new IllegalStateException("Framework is not ready");
    }

    public void readyForFeatureCreation() {
    }

    public void enableIms(int slotId) {
    }

    public void disableIms(int slotId) {
    }

    public MmTelFeature createMmTelFeature(int slotId) {
        return null;
    }

    public RcsFeature createRcsFeature(int slotId) {
        return null;
    }

    public ImsConfigImplBase getConfig(int slotId) {
        return new ImsConfigImplBase();
    }

    public ImsRegistrationImplBase getRegistration(int slotId) {
        return new ImsRegistrationImplBase();
    }
}
