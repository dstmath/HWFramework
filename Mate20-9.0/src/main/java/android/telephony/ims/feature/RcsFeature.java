package android.telephony.ims.feature;

import android.annotation.SystemApi;
import android.telephony.ims.aidl.IImsRcsFeature;
import android.telephony.ims.feature.ImsFeature;

@SystemApi
public class RcsFeature extends ImsFeature {
    private final IImsRcsFeature mImsRcsBinder = new IImsRcsFeature.Stub() {
    };

    public void changeEnabledCapabilities(CapabilityChangeRequest request, ImsFeature.CapabilityCallbackProxy c) {
    }

    public void onFeatureRemoved() {
    }

    public void onFeatureReady() {
    }

    public final IImsRcsFeature getBinder() {
        return this.mImsRcsBinder;
    }
}
