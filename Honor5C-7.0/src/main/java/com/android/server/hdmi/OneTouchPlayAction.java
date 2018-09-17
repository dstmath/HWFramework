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
    private final List<IHdmiControlCallback> mCallbacks;
    private int mPowerStatusCounter;
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
        this.mCallbacks = new ArrayList();
        this.mPowerStatusCounter = 0;
        this.mTargetAddress = targetAddress;
        addCallback(callback);
    }

    boolean start() {
        sendCommand(HdmiCecMessageBuilder.buildTextViewOn(getSourceAddress(), this.mTargetAddress));
        broadcastActiveSource();
        queryDevicePowerStatus();
        this.mState = STATE_WAITING_FOR_REPORT_POWER_STATUS;
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean processCommand(HdmiCecMessage cmd) {
        if (this.mState != STATE_WAITING_FOR_REPORT_POWER_STATUS || this.mTargetAddress != cmd.getSource() || cmd.getOpcode() != HdmiCecKeycode.UI_SOUND_PRESENTATION_SELECT_AUDIO_AUTO_REVERBERATION) {
            return false;
        }
        if (cmd.getParams()[0] == 0) {
            broadcastActiveSource();
            invokeCallback(0);
            finish();
        }
        return true;
    }

    void handleTimerEvent(int state) {
        if (this.mState == state && state == STATE_WAITING_FOR_REPORT_POWER_STATUS) {
            int i = this.mPowerStatusCounter;
            this.mPowerStatusCounter = i + STATE_WAITING_FOR_REPORT_POWER_STATUS;
            if (i < LOOP_COUNTER_MAX) {
                queryDevicePowerStatus();
                addTimer(this.mState, IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME);
            } else {
                invokeCallback(STATE_WAITING_FOR_REPORT_POWER_STATUS);
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
