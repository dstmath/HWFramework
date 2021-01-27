package com.android.server.hdmi;

import com.android.server.hdmi.HdmiControlService;

public class ArcInitiationActionFromAvr extends HdmiCecFeatureAction {
    private static final int MAX_RETRY_COUNT = 5;
    private static final int STATE_ARC_INITIATED = 2;
    private static final int STATE_WAITING_FOR_INITIATE_ARC_RESPONSE = 1;
    private static final int TIMEOUT_MS = 1000;
    private int mSendRequestActiveSourceRetryCount = 0;

    ArcInitiationActionFromAvr(HdmiCecLocalDevice source) {
        super(source);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public boolean start() {
        audioSystem().setArcStatus(true);
        this.mState = 1;
        addTimer(this.mState, 1000);
        sendInitiateArc();
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public boolean processCommand(HdmiCecMessage cmd) {
        if (this.mState != 1) {
            return false;
        }
        int opcode = cmd.getOpcode();
        if (opcode != 0) {
            if (opcode == 193) {
                this.mState = 2;
                if (audioSystem().getActiveSource().physicalAddress == getSourcePath() || !audioSystem().isSystemAudioActivated()) {
                    finish();
                } else {
                    sendRequestActiveSource();
                }
                return true;
            } else if (opcode != 194) {
                return false;
            } else {
                audioSystem().setArcStatus(false);
                finish();
                return true;
            }
        } else if ((cmd.getParams()[0] & 255) != 192) {
            return false;
        } else {
            audioSystem().setArcStatus(false);
            finish();
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public void handleTimerEvent(int state) {
        if (this.mState == state && this.mState == 1) {
            handleInitiateArcTimeout();
        }
    }

    /* access modifiers changed from: protected */
    public void sendInitiateArc() {
        sendCommand(HdmiCecMessageBuilder.buildInitiateArc(getSourceAddress(), 0), new HdmiControlService.SendMessageCallback() {
            /* class com.android.server.hdmi.$$Lambda$ArcInitiationActionFromAvr$qaL9xTkYpCTx60O4hdKmzJIE6k */

            @Override // com.android.server.hdmi.HdmiControlService.SendMessageCallback
            public final void onSendCompleted(int i) {
                ArcInitiationActionFromAvr.this.lambda$sendInitiateArc$0$ArcInitiationActionFromAvr(i);
            }
        });
    }

    public /* synthetic */ void lambda$sendInitiateArc$0$ArcInitiationActionFromAvr(int result) {
        if (result != 0) {
            audioSystem().setArcStatus(false);
            finish();
        }
    }

    private void handleInitiateArcTimeout() {
        HdmiLogger.debug("handleInitiateArcTimeout", new Object[0]);
        audioSystem().setArcStatus(false);
        finish();
    }

    /* access modifiers changed from: protected */
    public void sendRequestActiveSource() {
        sendCommand(HdmiCecMessageBuilder.buildRequestActiveSource(getSourceAddress()), new HdmiControlService.SendMessageCallback() {
            /* class com.android.server.hdmi.$$Lambda$ArcInitiationActionFromAvr$ysMwShprSV2Ejk2WTyEkZxajr8c */

            @Override // com.android.server.hdmi.HdmiControlService.SendMessageCallback
            public final void onSendCompleted(int i) {
                ArcInitiationActionFromAvr.this.lambda$sendRequestActiveSource$1$ArcInitiationActionFromAvr(i);
            }
        });
    }

    public /* synthetic */ void lambda$sendRequestActiveSource$1$ArcInitiationActionFromAvr(int result) {
        if (result != 0) {
            int i = this.mSendRequestActiveSourceRetryCount;
            if (i < 5) {
                this.mSendRequestActiveSourceRetryCount = i + 1;
                sendRequestActiveSource();
                return;
            }
            finish();
            return;
        }
        finish();
    }
}
