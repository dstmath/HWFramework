package com.android.commands.monkey;

import android.app.IActivityManager;
import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.view.IWindowManager;
import android.view.KeyEvent;

public class MonkeyKeyEvent extends MonkeyEvent {
    private int mAction;
    private int mDeviceId;
    private long mDownTime;
    private long mEventTime;
    private int mKeyCode;
    private KeyEvent mKeyEvent;
    private int mMetaState;
    private int mRepeatCount;
    private int mScanCode;

    public MonkeyKeyEvent(int action, int keyCode) {
        this(-1, -1, action, keyCode, 0, 0, -1, 0);
    }

    public MonkeyKeyEvent(long downTime, long eventTime, int action, int keyCode, int repeatCount, int metaState, int device, int scanCode) {
        super(0);
        this.mDownTime = downTime;
        this.mEventTime = eventTime;
        this.mAction = action;
        this.mKeyCode = keyCode;
        this.mRepeatCount = repeatCount;
        this.mMetaState = metaState;
        this.mDeviceId = device;
        this.mScanCode = scanCode;
    }

    public MonkeyKeyEvent(KeyEvent e) {
        super(0);
        this.mKeyEvent = e;
    }

    public int getKeyCode() {
        KeyEvent keyEvent = this.mKeyEvent;
        return keyEvent != null ? keyEvent.getKeyCode() : this.mKeyCode;
    }

    public int getAction() {
        KeyEvent keyEvent = this.mKeyEvent;
        return keyEvent != null ? keyEvent.getAction() : this.mAction;
    }

    public long getDownTime() {
        KeyEvent keyEvent = this.mKeyEvent;
        return keyEvent != null ? keyEvent.getDownTime() : this.mDownTime;
    }

    public long getEventTime() {
        KeyEvent keyEvent = this.mKeyEvent;
        return keyEvent != null ? keyEvent.getEventTime() : this.mEventTime;
    }

    public void setDownTime(long downTime) {
        if (this.mKeyEvent == null) {
            this.mDownTime = downTime;
            return;
        }
        throw new IllegalStateException("Cannot modify down time of this key event.");
    }

    public void setEventTime(long eventTime) {
        if (this.mKeyEvent == null) {
            this.mEventTime = eventTime;
            return;
        }
        throw new IllegalStateException("Cannot modify event time of this key event.");
    }

    @Override // com.android.commands.monkey.MonkeyEvent
    public boolean isThrottlable() {
        return getAction() == 1;
    }

    /* JADX INFO: Multiple debug info for r0v0 android.view.KeyEvent: [D('e' java.lang.ArrayIndexOutOfBoundsException), D('keyEvent' android.view.KeyEvent)] */
    @Override // com.android.commands.monkey.MonkeyEvent
    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        long downTime;
        String note;
        if (verbose > 1) {
            if (this.mAction == 1) {
                note = "ACTION_UP";
            } else {
                note = "ACTION_DOWN";
            }
            try {
                Logger logger = Logger.out;
                logger.println(":Sending Key (" + note + "): " + this.mKeyCode + "    // " + MonkeySourceRandom.getKeyName(this.mKeyCode));
            } catch (ArrayIndexOutOfBoundsException e) {
                Logger logger2 = Logger.out;
                logger2.println(":Sending Key (" + note + "): " + this.mKeyCode + "    // Unknown key event");
            }
        }
        KeyEvent keyEvent = this.mKeyEvent;
        if (keyEvent == null) {
            long eventTime = this.mEventTime;
            if (eventTime <= 0) {
                eventTime = SystemClock.uptimeMillis();
            }
            long downTime2 = this.mDownTime;
            if (downTime2 <= 0) {
                downTime = eventTime;
            } else {
                downTime = downTime2;
            }
            keyEvent = new KeyEvent(downTime, eventTime, this.mAction, this.mKeyCode, this.mRepeatCount, this.mMetaState, this.mDeviceId, this.mScanCode, 8, 257);
        }
        if (!InputManager.getInstance().injectInputEvent(keyEvent, 1)) {
            return 0;
        }
        return 1;
    }
}
