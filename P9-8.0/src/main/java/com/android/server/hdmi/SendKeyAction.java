package com.android.server.hdmi;

import android.util.Slog;

final class SendKeyAction extends HdmiCecFeatureAction {
    private static final int AWAIT_LONGPRESS_MS = 400;
    private static final int AWAIT_RELEASE_KEY_MS = 1000;
    private static final int STATE_CHECKING_LONGPRESS = 1;
    private static final int STATE_PROCESSING_KEYCODE = 2;
    private static final String TAG = "SendKeyAction";
    private int mLastKeycode;
    private long mLastSendKeyTime;
    private final int mTargetAddress;

    SendKeyAction(HdmiCecLocalDevice source, int targetAddress, int keycode) {
        super(source);
        this.mTargetAddress = targetAddress;
        this.mLastKeycode = keycode;
    }

    public boolean start() {
        sendKeyDown(this.mLastKeycode);
        this.mLastSendKeyTime = getCurrentTime();
        if (HdmiCecKeycode.isRepeatableKey(this.mLastKeycode)) {
            this.mState = 1;
            addTimer(this.mState, AWAIT_LONGPRESS_MS);
            return true;
        }
        sendKeyUp();
        finish();
        return true;
    }

    private long getCurrentTime() {
        return System.currentTimeMillis();
    }

    void processKeyEvent(int keycode, boolean isPressed) {
        if (this.mState == 1 || this.mState == 2) {
            if (isPressed) {
                if (keycode != this.mLastKeycode) {
                    sendKeyDown(keycode);
                    this.mLastSendKeyTime = getCurrentTime();
                    if (!HdmiCecKeycode.isRepeatableKey(keycode)) {
                        sendKeyUp();
                        finish();
                        return;
                    }
                } else if (getCurrentTime() - this.mLastSendKeyTime >= 300) {
                    sendKeyDown(keycode);
                    this.mLastSendKeyTime = getCurrentTime();
                }
                this.mActionTimer.clearTimerMessage();
                addTimer(this.mState, 1000);
                this.mLastKeycode = keycode;
            } else if (keycode == this.mLastKeycode) {
                sendKeyUp();
                finish();
            }
            return;
        }
        Slog.w(TAG, "Not in a valid state");
    }

    private void sendKeyDown(int keycode) {
        byte[] cecKeycodeAndParams = HdmiCecKeycode.androidKeyToCecKey(keycode);
        if (cecKeycodeAndParams != null) {
            sendCommand(HdmiCecMessageBuilder.buildUserControlPressed(getSourceAddress(), this.mTargetAddress, cecKeycodeAndParams));
        }
    }

    private void sendKeyUp() {
        sendCommand(HdmiCecMessageBuilder.buildUserControlReleased(getSourceAddress(), this.mTargetAddress));
    }

    public boolean processCommand(HdmiCecMessage cmd) {
        return false;
    }

    public void handleTimerEvent(int state) {
        switch (this.mState) {
            case 1:
                this.mActionTimer.clearTimerMessage();
                this.mState = 2;
                sendKeyDown(this.mLastKeycode);
                this.mLastSendKeyTime = getCurrentTime();
                addTimer(this.mState, 1000);
                return;
            case 2:
                sendKeyUp();
                finish();
                return;
            default:
                Slog.w(TAG, "Not in a valid state");
                return;
        }
    }
}
