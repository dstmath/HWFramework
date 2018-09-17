package com.android.server.hdmi;

import android.hardware.hdmi.IHdmiControlCallback;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.power.IHwShutdownThread;
import java.util.ArrayList;
import java.util.List;

final class DevicePowerStatusAction extends HdmiCecFeatureAction {
    private static final int STATE_WAITING_FOR_REPORT_POWER_STATUS = 1;
    private static final String TAG = "DevicePowerStatusAction";
    private final List<IHdmiControlCallback> mCallbacks;
    private final int mTargetAddress;

    static DevicePowerStatusAction create(HdmiCecLocalDevice source, int targetAddress, IHdmiControlCallback callback) {
        if (source != null && callback != null) {
            return new DevicePowerStatusAction(source, targetAddress, callback);
        }
        Slog.e(TAG, "Wrong arguments");
        return null;
    }

    private DevicePowerStatusAction(HdmiCecLocalDevice localDevice, int targetAddress, IHdmiControlCallback callback) {
        super(localDevice);
        this.mCallbacks = new ArrayList();
        this.mTargetAddress = targetAddress;
        addCallback(callback);
    }

    boolean start() {
        queryDevicePowerStatus();
        this.mState = STATE_WAITING_FOR_REPORT_POWER_STATUS;
        addTimer(this.mState, IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME);
        return true;
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
        invokeCallback(cmd.getParams()[0]);
        finish();
        return true;
    }

    void handleTimerEvent(int state) {
        if (this.mState == state && state == STATE_WAITING_FOR_REPORT_POWER_STATUS) {
            invokeCallback(-1);
            finish();
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
