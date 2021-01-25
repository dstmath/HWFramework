package android.telephony.ims.feature;

import android.annotation.SystemApi;
import android.telephony.ims.aidl.IImsRcsFeature;
import android.telephony.ims.feature.ImsFeature;

@SystemApi
public class RcsFeature extends ImsFeature {
    private final IImsRcsFeature mImsRcsBinder = new IImsRcsFeature.Stub() {
        /* class android.telephony.ims.feature.RcsFeature.AnonymousClass1 */
    };

    @Override // android.telephony.ims.feature.ImsFeature
    public void changeEnabledCapabilities(CapabilityChangeRequest request, ImsFeature.CapabilityCallbackProxy c) {
    }

    @Override // android.telephony.ims.feature.ImsFeature
    public void onFeatureRemoved() {
    }

    @Override // android.telephony.ims.feature.ImsFeature
    public void onFeatureReady() {
    }

    @Override // android.telephony.ims.feature.ImsFeature
    public final IImsRcsFeature getBinder() {
        return this.mImsRcsBinder;
    }
}
