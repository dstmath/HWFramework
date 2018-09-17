package com.android.server.hdmi;

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
        if (this.mState != 1 || (HdmiUtils.checkCommandSource(cmd, this.mAvrAddress, TAG) ^ 1) != 0) {
            return false;
        }
        switch (cmd.getOpcode()) {
            case 0:
                int originalOpcode = cmd.getParams()[0] & 255;
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
        if (this.mState == state && state == 1) {
            HdmiLogger.debug("[T] RequestArcAction.", new Object[0]);
            disableArcTransmission();
            finish();
        }
    }
}
