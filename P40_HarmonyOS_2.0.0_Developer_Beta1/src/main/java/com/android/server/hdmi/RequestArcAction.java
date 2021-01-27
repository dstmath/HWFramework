package com.android.server.hdmi;

/* access modifiers changed from: package-private */
public abstract class RequestArcAction extends HdmiCecFeatureAction {
    protected static final int STATE_WATING_FOR_REQUEST_ARC_REQUEST_RESPONSE = 1;
    private static final String TAG = "RequestArcAction";
    protected final int mAvrAddress;

    RequestArcAction(HdmiCecLocalDevice source, int avrAddress) {
        super(source);
        HdmiUtils.verifyAddressType(getSourceAddress(), 0);
        HdmiUtils.verifyAddressType(avrAddress, 5);
        this.mAvrAddress = avrAddress;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public boolean processCommand(HdmiCecMessage cmd) {
        if (this.mState != 1 || !HdmiUtils.checkCommandSource(cmd, this.mAvrAddress, TAG) || cmd.getOpcode() != 0) {
            return false;
        }
        int originalOpcode = cmd.getParams()[0] & 255;
        if (originalOpcode == 196) {
            disableArcTransmission();
            finish();
            return true;
        } else if (originalOpcode != 195) {
            return false;
        } else {
            tv().setArcStatus(false);
            finish();
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public final void disableArcTransmission() {
        addAndStartAction(new SetArcTransmissionStateAction(localDevice(), this.mAvrAddress, false));
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public final void handleTimerEvent(int state) {
        if (this.mState == state && state == 1) {
            HdmiLogger.debug("[T] RequestArcAction.", new Object[0]);
            disableArcTransmission();
            finish();
        }
    }
}
