package android.telephony.ims.compat.feature;

import com.android.ims.internal.IImsRcsFeature;

public class RcsFeature extends ImsFeature {
    private final IImsRcsFeature mImsRcsBinder = new IImsRcsFeature.Stub() {
    };

    public void onFeatureReady() {
    }

    public void onFeatureRemoved() {
    }

    public final IImsRcsFeature getBinder() {
        return this.mImsRcsBinder;
    }
}
