package com.android.server.hdmi;

import android.hardware.hdmi.IHdmiControlCallback;

final class SystemAudioActionFromTv extends SystemAudioAction {
    SystemAudioActionFromTv(HdmiCecLocalDevice sourceAddress, int avrAddress, boolean targetStatus, IHdmiControlCallback callback) {
        super(sourceAddress, avrAddress, targetStatus, callback);
        HdmiUtils.verifyAddressType(getSourceAddress(), 0);
    }

    boolean start() {
        removeSystemAudioActionInProgress();
        sendSystemAudioModeRequest();
        return true;
    }
}
