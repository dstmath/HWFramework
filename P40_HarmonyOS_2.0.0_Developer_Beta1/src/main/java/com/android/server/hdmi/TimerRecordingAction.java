package com.android.server.hdmi;

import android.util.Slog;
import com.android.server.hdmi.HdmiControlService;
import java.util.Arrays;

public class TimerRecordingAction extends HdmiCecFeatureAction {
    private static final int STATE_WAITING_FOR_TIMER_STATUS = 1;
    private static final String TAG = "TimerRecordingAction";
    private static final int TIMER_STATUS_TIMEOUT_MS = 120000;
    private final byte[] mRecordSource;
    private final int mRecorderAddress;
    private final int mSourceType;

    TimerRecordingAction(HdmiCecLocalDevice source, int recorderAddress, int sourceType, byte[] recordSource) {
        super(source);
        this.mRecorderAddress = recorderAddress;
        this.mSourceType = sourceType;
        this.mRecordSource = recordSource;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public boolean start() {
        sendTimerMessage();
        return true;
    }

    private void sendTimerMessage() {
        HdmiCecMessage message;
        int i = this.mSourceType;
        if (i == 1) {
            message = HdmiCecMessageBuilder.buildSetDigitalTimer(getSourceAddress(), this.mRecorderAddress, this.mRecordSource);
        } else if (i == 2) {
            message = HdmiCecMessageBuilder.buildSetAnalogueTimer(getSourceAddress(), this.mRecorderAddress, this.mRecordSource);
        } else if (i != 3) {
            tv().announceTimerRecordingResult(this.mRecorderAddress, 2);
            finish();
            return;
        } else {
            message = HdmiCecMessageBuilder.buildSetExternalTimer(getSourceAddress(), this.mRecorderAddress, this.mRecordSource);
        }
        sendCommand(message, new HdmiControlService.SendMessageCallback() {
            /* class com.android.server.hdmi.TimerRecordingAction.AnonymousClass1 */

            @Override // com.android.server.hdmi.HdmiControlService.SendMessageCallback
            public void onSendCompleted(int error) {
                if (error != 0) {
                    TimerRecordingAction.this.tv().announceTimerRecordingResult(TimerRecordingAction.this.mRecorderAddress, 1);
                    TimerRecordingAction.this.finish();
                    return;
                }
                TimerRecordingAction timerRecordingAction = TimerRecordingAction.this;
                timerRecordingAction.mState = 1;
                timerRecordingAction.addTimer(timerRecordingAction.mState, TimerRecordingAction.TIMER_STATUS_TIMEOUT_MS);
            }
        });
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public boolean processCommand(HdmiCecMessage cmd) {
        if (this.mState != 1 || cmd.getSource() != this.mRecorderAddress) {
            return false;
        }
        int opcode = cmd.getOpcode();
        if (opcode == 0) {
            return handleFeatureAbort(cmd);
        }
        if (opcode != 53) {
            return false;
        }
        return handleTimerStatus(cmd);
    }

    private boolean handleTimerStatus(HdmiCecMessage cmd) {
        byte[] timerStatusData = cmd.getParams();
        if (timerStatusData.length == 1 || timerStatusData.length == 3) {
            tv().announceTimerRecordingResult(this.mRecorderAddress, bytesToInt(timerStatusData));
            Slog.i(TAG, "Received [Timer Status Data]:" + Arrays.toString(timerStatusData));
        } else {
            Slog.w(TAG, "Invalid [Timer Status Data]:" + Arrays.toString(timerStatusData));
        }
        finish();
        return true;
    }

    private boolean handleFeatureAbort(HdmiCecMessage cmd) {
        byte[] params = cmd.getParams();
        int messageType = params[0] & 255;
        if (messageType != 52 && messageType != 151 && messageType != 162) {
            return false;
        }
        Slog.i(TAG, "[Feature Abort] for " + messageType + " reason:" + (params[1] & 255));
        tv().announceTimerRecordingResult(this.mRecorderAddress, 1);
        finish();
        return true;
    }

    private static int bytesToInt(byte[] data) {
        if (data.length <= 4) {
            int result = 0;
            for (int i = 0; i < data.length; i++) {
                result |= (data[i] & 255) << ((3 - i) * 8);
            }
            return result;
        }
        throw new IllegalArgumentException("Invalid data size:" + Arrays.toString(data));
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public void handleTimerEvent(int state) {
        if (this.mState != state) {
            Slog.w(TAG, "Timeout in invalid state:[Expected:" + this.mState + ", Actual:" + state + "]");
            return;
        }
        tv().announceTimerRecordingResult(this.mRecorderAddress, 1);
        finish();
    }
}
