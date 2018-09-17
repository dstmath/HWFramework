package com.android.server.hdmi;

import android.util.Slog;

public class OneTouchRecordAction extends HdmiCecFeatureAction {
    private static final int RECORD_STATUS_TIMEOUT_MS = 120000;
    private static final int STATE_RECORDING_IN_PROGRESS = 2;
    private static final int STATE_WAITING_FOR_RECORD_STATUS = 1;
    private static final String TAG = "OneTouchRecordAction";
    private final byte[] mRecordSource;
    private final int mRecorderAddress;

    OneTouchRecordAction(HdmiCecLocalDevice source, int recorderAddress, byte[] recordSource) {
        super(source);
        this.mRecorderAddress = recorderAddress;
        this.mRecordSource = recordSource;
    }

    boolean start() {
        sendRecordOn();
        return true;
    }

    private void sendRecordOn() {
        sendCommand(HdmiCecMessageBuilder.buildRecordOn(getSourceAddress(), this.mRecorderAddress, this.mRecordSource), new SendMessageCallback() {
            public void onSendCompleted(int error) {
                if (error != 0) {
                    OneTouchRecordAction.this.tv().announceOneTouchRecordResult(OneTouchRecordAction.this.mRecorderAddress, 49);
                    OneTouchRecordAction.this.finish();
                }
            }
        });
        this.mState = 1;
        addTimer(this.mState, RECORD_STATUS_TIMEOUT_MS);
    }

    boolean processCommand(HdmiCecMessage cmd) {
        if (this.mState != 1 || this.mRecorderAddress != cmd.getSource()) {
            return false;
        }
        switch (cmd.getOpcode()) {
            case 10:
                return handleRecordStatus(cmd);
            default:
                return false;
        }
    }

    private boolean handleRecordStatus(HdmiCecMessage cmd) {
        if (cmd.getSource() != this.mRecorderAddress) {
            return false;
        }
        int recordStatus = cmd.getParams()[0];
        tv().announceOneTouchRecordResult(this.mRecorderAddress, recordStatus);
        Slog.i(TAG, "Got record status:" + recordStatus + " from " + cmd.getSource());
        switch (recordStatus) {
            case 1:
            case 2:
            case 3:
            case 4:
                this.mState = 2;
                this.mActionTimer.clearTimerMessage();
                break;
            default:
                finish();
                break;
        }
        return true;
    }

    void handleTimerEvent(int state) {
        if (this.mState != state) {
            Slog.w(TAG, "Timeout in invalid state:[Expected:" + this.mState + ", Actual:" + state + "]");
            return;
        }
        tv().announceOneTouchRecordResult(this.mRecorderAddress, 49);
        finish();
    }

    int getRecorderAddress() {
        return this.mRecorderAddress;
    }
}
