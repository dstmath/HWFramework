package com.android.server.hdmi;

import com.android.server.hdmi.HdmiControlService;

public class ArcTerminationActionFromAvr extends HdmiCecFeatureAction {
    private static final int STATE_ARC_TERMINATED = 2;
    private static final int STATE_WAITING_FOR_INITIATE_ARC_RESPONSE = 1;
    private static final int TIMEOUT_MS = 1000;

    ArcTerminationActionFromAvr(HdmiCecLocalDevice source) {
        super(source);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public boolean start() {
        this.mState = 1;
        addTimer(this.mState, 1000);
        sendTerminateArc();
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public boolean processCommand(HdmiCecMessage cmd) {
        if (this.mState != 1 || cmd.getOpcode() != 194) {
            return false;
        }
        this.mState = 2;
        audioSystem().setArcStatus(false);
        if (audioSystem().getLocalActivePort() == 17) {
            audioSystem().routeToInputFromPortId(audioSystem().getRoutingPort());
        }
        finish();
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public void handleTimerEvent(int state) {
        if (this.mState == state && this.mState == 1) {
            handleTerminateArcTimeout();
        }
    }

    /* access modifiers changed from: protected */
    public void sendTerminateArc() {
        sendCommand(HdmiCecMessageBuilder.buildTerminateArc(getSourceAddress(), 0), new HdmiControlService.SendMessageCallback() {
            /* class com.android.server.hdmi.$$Lambda$ArcTerminationActionFromAvr$Q5Tewk7_xZ9w3X8CStv_tIZuDQY */

            @Override // com.android.server.hdmi.HdmiControlService.SendMessageCallback
            public final void onSendCompleted(int i) {
                ArcTerminationActionFromAvr.this.lambda$sendTerminateArc$0$ArcTerminationActionFromAvr(i);
            }
        });
    }

    public /* synthetic */ void lambda$sendTerminateArc$0$ArcTerminationActionFromAvr(int result) {
        if (result != 0) {
            HdmiLogger.debug("Terminate ARC was not successfully sent.", new Object[0]);
            finish();
        }
    }

    private void handleTerminateArcTimeout() {
        HdmiLogger.debug("handleTerminateArcTimeout", new Object[0]);
        finish();
    }
}
