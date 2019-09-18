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
    private final List<IHdmiControlCallback> mCallbacks = new ArrayList();
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
        this.mTargetAddress = targetAddress;
        addCallback(callback);
    }

    /* access modifiers changed from: package-private */
    public boolean start() {
        queryDevicePowerStatus();
        this.mState = 1;
        addTimer(this.mState, IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME);
        return true;
    }

    private void queryDevicePowerStatus() {
        sendCommand(HdmiCecMessageBuilder.buildGiveDevicePowerStatus(getSourceAddress(), this.mTargetAddress));
    }

    /* access modifiers changed from: package-private */
    public boolean processCommand(HdmiCecMessage cmd) {
        if (this.mState != 1 || this.mTargetAddress != cmd.getSource() || cmd.getOpcode() != 144) {
            return false;
        }
        invokeCallback(cmd.getParams()[0]);
        finish();
        return true;
    }

    /* access modifiers changed from: package-private */
    public void handleTimerEvent(int state) {
        if (this.mState == state && state == 1) {
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
