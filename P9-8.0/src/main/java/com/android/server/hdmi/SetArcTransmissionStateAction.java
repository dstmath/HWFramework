package com.android.server.hdmi;

import android.util.Slog;
import com.android.server.power.IHwShutdownThread;

final class SetArcTransmissionStateAction extends HdmiCecFeatureAction {
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

    boolean start() {
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
        sendCommand(HdmiCecMessageBuilder.buildReportArcInitiated(getSourceAddress(), this.mAvrAddress), new SendMessageCallback() {
            public void onSendCompleted(int error) {
                switch (error) {
                    case 1:
                        SetArcTransmissionStateAction.this.setArcStatus(false);
                        HdmiLogger.debug("Failed to send <Report Arc Initiated>.", new Object[0]);
                        SetArcTransmissionStateAction.this.finish();
                        return;
                    default:
                        return;
                }
            }
        });
    }

    private void setArcStatus(boolean enabled) {
        boolean wasEnabled = tv().setArcStatus(enabled);
        Slog.i(TAG, "Change arc status [old:" + wasEnabled + ", new:" + enabled + "]");
        if (!enabled && wasEnabled) {
            sendCommand(HdmiCecMessageBuilder.buildReportArcTerminated(getSourceAddress(), this.mAvrAddress));
        }
    }

    boolean processCommand(HdmiCecMessage cmd) {
        if (this.mState != 1 || cmd.getOpcode() != 0 || (cmd.getParams()[0] & 255) != HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_PLUS) {
            return false;
        }
        HdmiLogger.debug("Feature aborted for <Report Arc Initiated>", new Object[0]);
        setArcStatus(false);
        finish();
        return true;
    }

    void handleTimerEvent(int state) {
        if (this.mState == state && this.mState == 1) {
            finish();
        }
    }
}
