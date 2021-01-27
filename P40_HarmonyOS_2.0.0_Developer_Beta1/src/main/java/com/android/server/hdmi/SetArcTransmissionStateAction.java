package com.android.server.hdmi;

import android.util.Slog;
import com.android.server.hdmi.HdmiControlService;
import com.android.server.power.IHwShutdownThread;

/* access modifiers changed from: package-private */
public final class SetArcTransmissionStateAction extends HdmiCecFeatureAction {
    private static final int STATE_WAITING_TIMEOUT = 1;
    private static final String TAG = "SetArcTransmissionStateAction";
    private final int mAvrAddress;
    private final boolean mEnabled;

    SetArcTransmissionStateAction(HdmiCecLocalDevice source, int avrAddress, boolean enabled) {
        super(source);
        HdmiUtils.verifyAddressType(getSourceAddress(), 0);
        HdmiUtils.verifyAddressType(avrAddress, 5);
        this.mAvrAddress = avrAddress;
        this.mEnabled = enabled;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public boolean start() {
        if (this.mEnabled) {
            setArcStatus(true);
            this.mState = 1;
            addTimer(this.mState, IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME);
            sendReportArcInitiated();
        } else {
            setArcStatus(false);
            finish();
        }
        return true;
    }

    private void sendReportArcInitiated() {
        sendCommand(HdmiCecMessageBuilder.buildReportArcInitiated(getSourceAddress(), this.mAvrAddress), new HdmiControlService.SendMessageCallback() {
            /* class com.android.server.hdmi.SetArcTransmissionStateAction.AnonymousClass1 */

            @Override // com.android.server.hdmi.HdmiControlService.SendMessageCallback
            public void onSendCompleted(int error) {
                if (error == 0) {
                    return;
                }
                if (error != 1) {
                    if (error == 2 || error != 3) {
                    }
                    return;
                }
                SetArcTransmissionStateAction.this.setArcStatus(false);
                HdmiLogger.debug("Failed to send <Report Arc Initiated>.", new Object[0]);
                SetArcTransmissionStateAction.this.finish();
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setArcStatus(boolean enabled) {
        boolean wasEnabled = tv().setArcStatus(enabled);
        Slog.i(TAG, "Change arc status [old:" + wasEnabled + ", new:" + enabled + "]");
        if (!enabled && wasEnabled) {
            sendCommand(HdmiCecMessageBuilder.buildReportArcTerminated(getSourceAddress(), this.mAvrAddress));
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public boolean processCommand(HdmiCecMessage cmd) {
        if (this.mState != 1 || cmd.getOpcode() != 0 || (cmd.getParams()[0] & 255) != 193) {
            return false;
        }
        HdmiLogger.debug("Feature aborted for <Report Arc Initiated>", new Object[0]);
        setArcStatus(false);
        finish();
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public void handleTimerEvent(int state) {
        if (this.mState == state && this.mState == 1) {
            finish();
        }
    }
}
