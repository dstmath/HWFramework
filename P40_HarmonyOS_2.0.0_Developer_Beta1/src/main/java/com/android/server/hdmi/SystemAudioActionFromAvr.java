package com.android.server.hdmi;

import android.hardware.hdmi.IHdmiControlCallback;

/* access modifiers changed from: package-private */
public final class SystemAudioActionFromAvr extends SystemAudioAction {
    SystemAudioActionFromAvr(HdmiCecLocalDevice source, int avrAddress, boolean targetStatus, IHdmiControlCallback callback) {
        super(source, avrAddress, targetStatus, callback);
        HdmiUtils.verifyAddressType(getSourceAddress(), 0);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public boolean start() {
        removeSystemAudioActionInProgress();
        handleSystemAudioActionFromAvr();
        return true;
    }

    private void handleSystemAudioActionFromAvr() {
        if (this.mTargetAudioStatus == tv().isSystemAudioActivated()) {
            finishWithCallback(0);
        } else if (tv().isProhibitMode()) {
            sendCommand(HdmiCecMessageBuilder.buildFeatureAbortCommand(getSourceAddress(), this.mAvrLogicalAddress, HdmiCecKeycode.CEC_KEYCODE_F2_RED, 4));
            this.mTargetAudioStatus = false;
            sendSystemAudioModeRequest();
        } else {
            removeAction(SystemAudioAutoInitiationAction.class);
            if (this.mTargetAudioStatus) {
                setSystemAudioMode(true);
                startAudioStatusAction();
                return;
            }
            setSystemAudioMode(false);
            finishWithCallback(0);
        }
    }
}
