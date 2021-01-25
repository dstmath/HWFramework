package com.android.server.hdmi;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Pair;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.hdmi.HdmiControlService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* access modifiers changed from: package-private */
public abstract class HdmiCecFeatureAction {
    protected static final int MSG_TIMEOUT = 100;
    protected static final int STATE_NONE = 0;
    private static final String TAG = "HdmiCecFeatureAction";
    protected ActionTimer mActionTimer;
    private ArrayList<Pair<HdmiCecFeatureAction, Runnable>> mOnFinishedCallbacks;
    private final HdmiControlService mService;
    private final HdmiCecLocalDevice mSource;
    protected int mState = 0;

    /* access modifiers changed from: package-private */
    public interface ActionTimer {
        void clearTimerMessage();

        void sendTimerMessage(int i, long j);
    }

    /* access modifiers changed from: package-private */
    public abstract void handleTimerEvent(int i);

    /* access modifiers changed from: package-private */
    public abstract boolean processCommand(HdmiCecMessage hdmiCecMessage);

    /* access modifiers changed from: package-private */
    public abstract boolean start();

    HdmiCecFeatureAction(HdmiCecLocalDevice source) {
        this.mSource = source;
        this.mService = this.mSource.getService();
        this.mActionTimer = createActionTimer(this.mService.getServiceLooper());
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setActionTimer(ActionTimer actionTimer) {
        this.mActionTimer = actionTimer;
    }

    /* access modifiers changed from: private */
    public class ActionTimerHandler extends Handler implements ActionTimer {
        public ActionTimerHandler(Looper looper) {
            super(looper);
        }

        @Override // com.android.server.hdmi.HdmiCecFeatureAction.ActionTimer
        public void sendTimerMessage(int state, long delayMillis) {
            sendMessageDelayed(obtainMessage(100, state, 0), delayMillis);
        }

        @Override // com.android.server.hdmi.HdmiCecFeatureAction.ActionTimer
        public void clearTimerMessage() {
            removeMessages(100);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what != 100) {
                Slog.w(HdmiCecFeatureAction.TAG, "Unsupported message:" + msg.what);
                return;
            }
            HdmiCecFeatureAction.this.handleTimerEvent(msg.arg1);
        }
    }

    private ActionTimer createActionTimer(Looper looper) {
        return new ActionTimerHandler(looper);
    }

    /* access modifiers changed from: protected */
    public void addTimer(int state, int delayMillis) {
        this.mActionTimer.sendTimerMessage(state, (long) delayMillis);
    }

    /* access modifiers changed from: package-private */
    public boolean started() {
        return this.mState != 0;
    }

    /* access modifiers changed from: protected */
    public final void sendCommand(HdmiCecMessage cmd) {
        this.mService.sendCecCommand(cmd);
    }

    /* access modifiers changed from: protected */
    public final void sendCommand(HdmiCecMessage cmd, HdmiControlService.SendMessageCallback callback) {
        this.mService.sendCecCommand(cmd, callback);
    }

    /* access modifiers changed from: protected */
    public final void addAndStartAction(HdmiCecFeatureAction action) {
        this.mSource.addAndStartAction(action);
    }

    /* access modifiers changed from: protected */
    public final <T extends HdmiCecFeatureAction> List<T> getActions(Class<T> clazz) {
        return this.mSource.getActions(clazz);
    }

    /* access modifiers changed from: protected */
    public final HdmiCecMessageCache getCecMessageCache() {
        return this.mSource.getCecMessageCache();
    }

    /* access modifiers changed from: protected */
    public final void removeAction(HdmiCecFeatureAction action) {
        this.mSource.removeAction(action);
    }

    /* access modifiers changed from: protected */
    public final <T extends HdmiCecFeatureAction> void removeAction(Class<T> clazz) {
        this.mSource.removeActionExcept(clazz, null);
    }

    /* access modifiers changed from: protected */
    public final <T extends HdmiCecFeatureAction> void removeActionExcept(Class<T> clazz, HdmiCecFeatureAction exception) {
        this.mSource.removeActionExcept(clazz, exception);
    }

    /* access modifiers changed from: protected */
    public final void pollDevices(HdmiControlService.DevicePollingCallback callback, int pickStrategy, int retryCount) {
        this.mService.pollDevices(callback, getSourceAddress(), pickStrategy, retryCount);
    }

    /* access modifiers changed from: package-private */
    public void clear() {
        this.mState = 0;
        this.mActionTimer.clearTimerMessage();
    }

    /* access modifiers changed from: protected */
    public void finish() {
        finish(true);
    }

    /* access modifiers changed from: package-private */
    public void finish(boolean removeSelf) {
        clear();
        if (removeSelf) {
            removeAction(this);
        }
        ArrayList<Pair<HdmiCecFeatureAction, Runnable>> arrayList = this.mOnFinishedCallbacks;
        if (arrayList != null) {
            Iterator<Pair<HdmiCecFeatureAction, Runnable>> it = arrayList.iterator();
            while (it.hasNext()) {
                Pair<HdmiCecFeatureAction, Runnable> actionCallbackPair = it.next();
                if (((HdmiCecFeatureAction) actionCallbackPair.first).mState != 0) {
                    ((Runnable) actionCallbackPair.second).run();
                }
            }
            this.mOnFinishedCallbacks = null;
        }
    }

    /* access modifiers changed from: protected */
    public final HdmiCecLocalDevice localDevice() {
        return this.mSource;
    }

    /* access modifiers changed from: protected */
    public final HdmiCecLocalDevicePlayback playback() {
        return (HdmiCecLocalDevicePlayback) this.mSource;
    }

    /* access modifiers changed from: protected */
    public final HdmiCecLocalDeviceSource source() {
        return (HdmiCecLocalDeviceSource) this.mSource;
    }

    /* access modifiers changed from: protected */
    public final HdmiCecLocalDeviceTv tv() {
        return (HdmiCecLocalDeviceTv) this.mSource;
    }

    /* access modifiers changed from: protected */
    public final HdmiCecLocalDeviceAudioSystem audioSystem() {
        return (HdmiCecLocalDeviceAudioSystem) this.mSource;
    }

    /* access modifiers changed from: protected */
    public final int getSourceAddress() {
        return this.mSource.getDeviceInfo().getLogicalAddress();
    }

    /* access modifiers changed from: protected */
    public final int getSourcePath() {
        return this.mSource.getDeviceInfo().getPhysicalAddress();
    }

    /* access modifiers changed from: protected */
    public final void sendUserControlPressedAndReleased(int targetAddress, int uiCommand) {
        this.mSource.sendUserControlPressedAndReleased(targetAddress, uiCommand);
    }

    /* access modifiers changed from: protected */
    public final void addOnFinishedCallback(HdmiCecFeatureAction action, Runnable runnable) {
        if (this.mOnFinishedCallbacks == null) {
            this.mOnFinishedCallbacks = new ArrayList<>();
        }
        this.mOnFinishedCallbacks.add(Pair.create(action, runnable));
    }
}
