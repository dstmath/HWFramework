package android.telephony.ims.compat.feature;

import com.android.ims.internal.IImsRcsFeature;

public class RcsFeature extends ImsFeature {
    private final IImsRcsFeature mImsRcsBinder = new IImsRcsFeature.Stub() {
        /* class android.telephony.ims.compat.feature.RcsFeature.AnonymousClass1 */
    };

    @Override // android.telephony.ims.compat.feature.ImsFeature
    public void onFeatureReady() {
    }

    @Override // android.telephony.ims.compat.feature.ImsFeature
    public void onFeatureRemoved() {
    }

    @Override // android.telephony.ims.compat.feature.ImsFeature
    public final IImsRcsFeature getBinder() {
        return this.mImsRcsBinder;
    }
}
