package com.android.server.hdmi;

import android.hardware.hdmi.IHdmiControlCallback;

/* access modifiers changed from: package-private */
public final class SystemAudioActionFromTv extends SystemAudioAction {
    SystemAudioActionFromTv(HdmiCecLocalDevice sourceAddress, int avrAddress, boolean targetStatus, IHdmiControlCallback callback) {
        super(sourceAddress, avrAddress, targetStatus, callback);
        HdmiUtils.verifyAddressType(getSourceAddress(), 0);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public boolean start() {
        removeSystemAudioActionInProgress();
        sendSystemAudioModeRequest();
        return true;
    }
}
