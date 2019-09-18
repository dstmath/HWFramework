package com.android.server.hdmi;

import android.hardware.hdmi.IHdmiControlCallback;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.power.IHwShutdownThread;
import java.util.ArrayList;
import java.util.List;

final class OneTouchPlayAction extends HdmiCecFeatureAction {
    private static final int LOOP_COUNTER_MAX = 10;
    private static final int STATE_WAITING_FOR_REPORT_POWER_STATUS = 1;
    private static final String TAG = "OneTouchPlayAction";
    private final List<IHdmiControlCallback> mCallbacks = new ArrayList();
    private int mPowerStatusCounter = 0;
    private final int mTargetAddress;

    static OneTouchPlayAction create(HdmiCecLocalDevicePlayback source, int targetAddress, IHdmiControlCallback callback) {
        if (source != null && callback != null) {
            return new OneTouchPlayAction(source, targetAddress, callback);
        }
        Slog.e(TAG, "Wrong arguments");
        return null;
    }

    private OneTouchPlayAction(HdmiCecLocalDevice localDevice, int targetAddress, IHdmiControlCallback callback) {
        super(localDevice);
        this.mTargetAddress = targetAddress;
        addCallback(callback);
    }

    /* access modifiers changed from: package-private */
    public boolean start() {
        sendCommand(HdmiCecMessageBuilder.buildTextViewOn(getSourceAddress(), this.mTargetAddress));
        broadcastActiveSource();
        queryDevicePowerStatus();
        this.mState = 1;
        addTimer(this.mState, IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME);
        return true;
    }

    private void broadcastActiveSource() {
        sendCommand(HdmiCecMessageBuilder.buildActiveSource(getSourceAddress(), getSourcePath()));
        playback().setActiveSource(true);
    }

    private void queryDevicePowerStatus() {
        sendCommand(HdmiCecMessageBuilder.buildGiveDevicePowerStatus(getSourceAddress(), this.mTargetAddress));
    }

    /* access modifiers changed from: package-private */
    public boolean processCommand(HdmiCecMessage cmd) {
        if (this.mState != 1 || this.mTargetAddress != cmd.getSource() || cmd.getOpcode() != 144) {
            return false;
        }
        if (cmd.getParams()[0] == 0) {
            broadcastActiveSource();
            invokeCallback(0);
            finish();
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void handleTimerEvent(int state) {
        if (this.mState == state && state == 1) {
            int i = this.mPowerStatusCounter;
            this.mPowerStatusCounter = i + 1;
            if (i < 10) {
                queryDevicePowerStatus();
                addTimer(this.mState, IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME);
            } else {
                invokeCallback(1);
                finish();
            }
        }
    }

    public void addCallback(IHdmiControlCallback callback) {
        this.mCallbacks.add(callback);
    }

    private void invokeCallback(int result) {
        try {
            for (IHdmiControlCallback callback : this.mCallbacks) {
                callback.onComplete(result);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "Callback failed:" + e);
        }
    }
}
