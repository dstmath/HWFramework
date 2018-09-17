package com.android.server.hdmi;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Pair;
import android.util.Slog;
import java.util.ArrayList;
import java.util.List;

abstract class HdmiCecFeatureAction {
    protected static final int MSG_TIMEOUT = 100;
    protected static final int STATE_NONE = 0;
    private static final String TAG = "HdmiCecFeatureAction";
    protected ActionTimer mActionTimer;
    private ArrayList<Pair<HdmiCecFeatureAction, Runnable>> mOnFinishedCallbacks;
    private final HdmiControlService mService;
    private final HdmiCecLocalDevice mSource;
    protected int mState = 0;

    interface ActionTimer {
        void clearTimerMessage();

        void sendTimerMessage(int i, long j);
    }

    private class ActionTimerHandler extends Handler implements ActionTimer {
        public ActionTimerHandler(Looper looper) {
            super(looper);
        }

        public void sendTimerMessage(int state, long delayMillis) {
            sendMessageDelayed(obtainMessage(100, state, 0), delayMillis);
        }

        public void clearTimerMessage() {
            removeMessages(100);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    HdmiCecFeatureAction.this.handleTimerEvent(msg.arg1);
                    return;
                default:
                    Slog.w(HdmiCecFeatureAction.TAG, "Unsupported message:" + msg.what);
                    return;
            }
        }
    }

    abstract void handleTimerEvent(int i);

    abstract boolean processCommand(HdmiCecMessage hdmiCecMessage);

    abstract boolean start();

    HdmiCecFeatureAction(HdmiCecLocalDevice source) {
        this.mSource = source;
        this.mService = this.mSource.getService();
        this.mActionTimer = createActionTimer(this.mService.getServiceLooper());
    }

    void setActionTimer(ActionTimer actionTimer) {
        this.mActionTimer = actionTimer;
    }

    private ActionTimer createActionTimer(Looper looper) {
        return new ActionTimerHandler(looper);
    }

    protected void addTimer(int state, int delayMillis) {
        this.mActionTimer.sendTimerMessage(state, (long) delayMillis);
    }

    boolean started() {
        return this.mState != 0;
    }

    protected final void sendCommand(HdmiCecMessage cmd) {
        this.mService.sendCecCommand(cmd);
    }

    protected final void sendCommand(HdmiCecMessage cmd, SendMessageCallback callback) {
        this.mService.sendCecCommand(cmd, callback);
    }

    protected final void addAndStartAction(HdmiCecFeatureAction action) {
        this.mSource.addAndStartAction(action);
    }

    protected final <T extends HdmiCecFeatureAction> List<T> getActions(Class<T> clazz) {
        return this.mSource.getActions(clazz);
    }

    protected final HdmiCecMessageCache getCecMessageCache() {
        return this.mSource.getCecMessageCache();
    }

    protected final void removeAction(HdmiCecFeatureAction action) {
        this.mSource.removeAction(action);
    }

    protected final <T extends HdmiCecFeatureAction> void removeAction(Class<T> clazz) {
        this.mSource.removeActionExcept(clazz, null);
    }

    protected final <T extends HdmiCecFeatureAction> void removeActionExcept(Class<T> clazz, HdmiCecFeatureAction exception) {
        this.mSource.removeActionExcept(clazz, exception);
    }

    protected final void pollDevices(DevicePollingCallback callback, int pickStrategy, int retryCount) {
        this.mService.pollDevices(callback, getSourceAddress(), pickStrategy, retryCount);
    }

    void clear() {
        this.mState = 0;
        this.mActionTimer.clearTimerMessage();
    }

    protected void finish() {
        finish(true);
    }

    void finish(boolean removeSelf) {
        clear();
        if (removeSelf) {
            removeAction(this);
        }
        if (this.mOnFinishedCallbacks != null) {
            for (Pair<HdmiCecFeatureAction, Runnable> actionCallbackPair : this.mOnFinishedCallbacks) {
                if (((HdmiCecFeatureAction) actionCallbackPair.first).mState != 0) {
                    ((Runnable) actionCallbackPair.second).run();
                }
            }
            this.mOnFinishedCallbacks = null;
        }
    }

    protected final HdmiCecLocalDevice localDevice() {
        return this.mSource;
    }

    protected final HdmiCecLocalDevicePlayback playback() {
        return (HdmiCecLocalDevicePlayback) this.mSource;
    }

    protected final HdmiCecLocalDeviceTv tv() {
        return (HdmiCecLocalDeviceTv) this.mSource;
    }

    protected final int getSourceAddress() {
        return this.mSource.getDeviceInfo().getLogicalAddress();
    }

    protected final int getSourcePath() {
        return this.mSource.getDeviceInfo().getPhysicalAddress();
    }

    protected final void sendUserControlPressedAndReleased(int targetAddress, int uiCommand) {
        this.mSource.sendUserControlPressedAndReleased(targetAddress, uiCommand);
    }

    protected final void addOnFinishedCallback(HdmiCecFeatureAction action, Runnable runnable) {
        if (this.mOnFinishedCallbacks == null) {
            this.mOnFinishedCallbacks = new ArrayList();
        }
        this.mOnFinishedCallbacks.add(Pair.create(action, runnable));
    }
}
