package com.android.server.hdmi;

import com.android.server.display.RampAnimator;
import com.android.server.wm.WindowState;

abstract class RequestArcAction extends HdmiCecFeatureAction {
    protected static final int STATE_WATING_FOR_REQUEST_ARC_REQUEST_RESPONSE = 1;
    private static final String TAG = "RequestArcAction";
    protected final int mAvrAddress;

    RequestArcAction(HdmiCecLocalDevice source, int avrAddress) {
        super(source);
        HdmiUtils.verifyAddressType(getSourceAddress(), 0);
        HdmiUtils.verifyAddressType(avrAddress, 5);
        this.mAvrAddress = avrAddress;
    }

    boolean processCommand(HdmiCecMessage cmd) {
        if (this.mState != STATE_WATING_FOR_REQUEST_ARC_REQUEST_RESPONSE || !HdmiUtils.checkCommandSource(cmd, this.mAvrAddress, TAG)) {
            return false;
        }
        switch (cmd.getOpcode()) {
            case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                int originalOpcode = cmd.getParams()[0] & RampAnimator.DEFAULT_MAX_BRIGHTNESS;
                if (originalOpcode == 196) {
                    disableArcTransmission();
                    finish();
                    return true;
                } else if (originalOpcode != HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_MINUS) {
                    return false;
                } else {
                    tv().setArcStatus(false);
                    finish();
                    return true;
                }
            default:
                return false;
        }
    }

    protected final void disableArcTransmission() {
        addAndStartAction(new SetArcTransmissionStateAction(localDevice(), this.mAvrAddress, false));
    }

    final void handleTimerEvent(int state) {
        if (this.mState == state && state == STATE_WATING_FOR_REQUEST_ARC_REQUEST_RESPONSE) {
            HdmiLogger.debug("[T] RequestArcAction.", new Object[0]);
            disableArcTransmission();
            finish();
        }
    }
}
