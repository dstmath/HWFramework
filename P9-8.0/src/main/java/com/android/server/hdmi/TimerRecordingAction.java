package com.android.server.hdmi;

import android.util.Slog;
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

    boolean start() {
        sendTimerMessage();
        return true;
    }

    private void sendTimerMessage() {
        HdmiCecMessage message;
        switch (this.mSourceType) {
            case 1:
                message = HdmiCecMessageBuilder.buildSetDigitalTimer(getSourceAddress(), this.mRecorderAddress, this.mRecordSource);
                break;
            case 2:
                message = HdmiCecMessageBuilder.buildSetAnalogueTimer(getSourceAddress(), this.mRecorderAddress, this.mRecordSource);
                break;
            case 3:
                message = HdmiCecMessageBuilder.buildSetExternalTimer(getSourceAddress(), this.mRecorderAddress, this.mRecordSource);
                break;
            default:
                tv().announceTimerRecordingResult(this.mRecorderAddress, 2);
                finish();
                return;
        }
        sendCommand(message, new SendMessageCallback() {
            public void onSendCompleted(int error) {
                if (error != 0) {
                    TimerRecordingAction.this.tv().announceTimerRecordingResult(TimerRecordingAction.this.mRecorderAddress, 1);
                    TimerRecordingAction.this.finish();
                    return;
                }
                TimerRecordingAction.this.mState = 1;
                TimerRecordingAction.this.addTimer(TimerRecordingAction.this.mState, TimerRecordingAction.TIMER_STATUS_TIMEOUT_MS);
            }
        });
    }

    boolean processCommand(HdmiCecMessage cmd) {
        if (this.mState != 1 || cmd.getSource() != this.mRecorderAddress) {
            return false;
        }
        switch (cmd.getOpcode()) {
            case 0:
                return handleFeatureAbort(cmd);
            case 53:
                return handleTimerStatus(cmd);
            default:
                return false;
        }
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
        switch (messageType) {
            case 52:
            case 151:
            case 162:
                Slog.i(TAG, "[Feature Abort] for " + messageType + " reason:" + (params[1] & 255));
                tv().announceTimerRecordingResult(this.mRecorderAddress, 1);
                finish();
                return true;
            default:
                return false;
        }
    }

    private static int bytesToInt(byte[] data) {
        if (data.length > 4) {
            throw new IllegalArgumentException("Invalid data size:" + Arrays.toString(data));
        }
        int result = 0;
        for (int i = 0; i < data.length; i++) {
            result |= (data[i] & 255) << ((3 - i) * 8);
        }
        return result;
    }

    void handleTimerEvent(int state) {
        if (this.mState != state) {
            Slog.w(TAG, "Timeout in invalid state:[Expected:" + this.mState + ", Actual:" + state + "]");
            return;
        }
        tv().announceTimerRecordingResult(this.mRecorderAddress, 1);
        finish();
    }
}
