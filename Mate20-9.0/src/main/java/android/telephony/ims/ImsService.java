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
        public void setListener(IImsServiceControllerListener l) {
            IImsServiceControllerListener unused = ImsService.this.mListener = l;
        }

        public IImsMmTelFeature createMmTelFeature(int slotId, IImsFeatureStatusCallback c) {
            return ImsService.this.createMmTelFeatureInternal(slotId, c);
        }

        public IImsRcsFeature createRcsFeature(int slotId, IImsFeatureStatusCallback c) {
            return ImsService.this.createRcsFeatureInternal(slotId, c);
        }

        public void removeImsFeature(int slotId, int featureType, IImsFeatureStatusCallback c) {
            ImsService.this.removeImsFeature(slotId, featureType, c);
        }

        public ImsFeatureConfiguration querySupportedImsFeatures() {
            return ImsService.this.querySupportedImsFeatures();
        }

        public void notifyImsServiceReadyForFeatureCreation() {
            ImsService.this.readyForFeatureCreation();
        }

        public IImsConfig getConfig(int slotId) {
            ImsConfigImplBase c = ImsService.this.getConfig(slotId);
            if (c != null) {
                return c.getIImsConfig();
            }
            return null;
        }

        public IImsRegistration getRegistration(int slotId) {
            ImsRegistrationImplBase r = ImsService.this.getRegistration(slotId);
            if (r != null) {
                return r.getBinder();
            }
            return null;
        }

        public void enableIms(int slotId) {
            ImsService.this.enableIms(slotId);
        }

        public void disableIms(int slotId) {
            ImsService.this.disableIms(slotId);
        }
    };
    /* access modifiers changed from: private */
    public IImsServiceControllerListener mListener;

    public static class Listener extends IImsServiceControllerListener.Stub {
        public void onUpdateSupportedImsFeatures(ImsFeatureConfiguration c) {
        }
    }

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
    public IImsMmTelFeature createMmTelFeatureInternal(int slotId, IImsFeatureStatusCallback c) {
        MmTelFeature f = createMmTelFeature(slotId);
        if (f != null) {
            setupFeature(f, slotId, 1, c);
            return f.getBinder();
        }
        Log.e(LOG_TAG, "createMmTelFeatureInternal: null feature returned.");
        return null;
    }

    /* access modifiers changed from: private */
    public IImsRcsFeature createRcsFeatureInternal(int slotId, IImsFeatureStatusCallback c) {
        RcsFeature f = createRcsFeature(slotId);
        if (f != null) {
            setupFeature(f, slotId, 2, c);
            return f.getBinder();
        }
        Log.e(LOG_TAG, "createRcsFeatureInternal: null feature returned.");
        return null;
    }

    private void setupFeature(ImsFeature f, int slotId, int featureType, IImsFeatureStatusCallback c) {
        f.addImsFeatureStatusCallback(c);
        f.initialize(this, slotId);
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
    public void removeImsFeature(int slotId, int featureType, IImsFeatureStatusCallback c) {
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
        if (this.mListener != null) {
            this.mListener.onUpdateSupportedImsFeatures(c);
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
