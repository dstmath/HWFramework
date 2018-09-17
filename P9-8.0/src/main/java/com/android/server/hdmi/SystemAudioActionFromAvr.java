package com.android.server.hdmi;

import android.hardware.hdmi.IHdmiControlCallback;

final class SystemAudioActionFromAvr extends SystemAudioAction {
    SystemAudioActionFromAvr(HdmiCecLocalDevice source, int avrAddress, boolean targetStatus, IHdmiControlCallback callback) {
        super(source, avrAddress, targetStatus, callback);
        HdmiUtils.verifyAddressType(getSourceAddress(), 0);
    }

    boolean start() {
        removeSystemAudioActionInProgress();
        handleSystemAudioActionFromAvr();
        return true;
    }

    private void handleSystemAudioActionFromAvr() {
        if (this.mTargetAudioStatus == tv().isSystemAudioActivated()) {
            finishWithCallback(0);
        } else if (tv().isProhibitMode()) {
            sendCommand(HdmiCecMessageBuilder.buildFeatureAbortCommand(getSourceAddress(), this.mAvrLogicalAddress, 114, 4));
            this.mTargetAudioStatus = false;
            sendSystemAudioModeRequest();
        } else {
            removeAction(SystemAudioAutoInitiationAction.class);
            if (this.mTargetAudioStatus) {
                setSystemAudioMode(true);
                startAudioStatusAction();
            } else {
                setSystemAudioMode(false);
                finishWithCallback(0);
            }
        }
    }
}
