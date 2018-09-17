package com.android.server.hdmi;

import android.hardware.hdmi.HdmiDeviceInfo;
import android.hardware.hdmi.IHdmiControlCallback;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.power.IHwShutdownThread;

final class DeviceSelectAction extends HdmiCecFeatureAction {
    private static final int LOOP_COUNTER_MAX = 20;
    private static final int STATE_WAIT_FOR_DEVICE_POWER_ON = 3;
    private static final int STATE_WAIT_FOR_DEVICE_TO_TRANSIT_TO_STANDBY = 2;
    private static final int STATE_WAIT_FOR_REPORT_POWER_STATUS = 1;
    private static final String TAG = "DeviceSelect";
    private static final int TIMEOUT_POWER_ON_MS = 5000;
    private static final int TIMEOUT_TRANSIT_TO_STANDBY_MS = 5000;
    private final IHdmiControlCallback mCallback;
    private final HdmiCecMessage mGivePowerStatus;
    private int mPowerStatusCounter = 0;
    private final HdmiDeviceInfo mTarget;

    public DeviceSelectAction(HdmiCecLocalDeviceTv source, HdmiDeviceInfo target, IHdmiControlCallback callback) {
        super(source);
        this.mCallback = callback;
        this.mTarget = target;
        this.mGivePowerStatus = HdmiCecMessageBuilder.buildGiveDevicePowerStatus(getSourceAddress(), getTargetAddress());
    }

    int getTargetAddress() {
        return this.mTarget.getLogicalAddress();
    }

    public boolean start() {
        queryDevicePowerStatus();
        return true;
    }

    private void queryDevicePowerStatus() {
        sendCommand(this.mGivePowerStatus, new SendMessageCallback() {
            public void onSendCompleted(int error) {
                if (error != 0) {
                    DeviceSelectAction.this.invokeCallback(7);
                    DeviceSelectAction.this.finish();
                }
            }
        });
        this.mState = 1;
        addTimer(this.mState, IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME);
    }

    public boolean processCommand(HdmiCecMessage cmd) {
        if (cmd.getSource() != getTargetAddress()) {
            return false;
        }
        int opcode = cmd.getOpcode();
        byte[] params = cmd.getParams();
        switch (this.mState) {
            case 1:
                if (opcode == 144) {
                    return handleReportPowerStatus(params[0]);
                }
                return false;
            default:
                return false;
        }
    }

    private boolean handleReportPowerStatus(int powerStatus) {
        switch (powerStatus) {
            case 0:
                sendSetStreamPath();
                return true;
            case 1:
                if (this.mPowerStatusCounter == 0) {
                    turnOnDevice();
                } else {
                    sendSetStreamPath();
                }
                return true;
            case 2:
                if (this.mPowerStatusCounter < 20) {
                    this.mState = 3;
                    addTimer(this.mState, 5000);
                } else {
                    sendSetStreamPath();
                }
                return true;
            case 3:
                if (this.mPowerStatusCounter < 4) {
                    this.mState = 2;
                    addTimer(this.mState, 5000);
                } else {
                    sendSetStreamPath();
                }
                return true;
            default:
                return false;
        }
    }

    private void turnOnDevice() {
        sendUserControlPressedAndReleased(this.mTarget.getLogicalAddress(), 64);
        sendUserControlPressedAndReleased(this.mTarget.getLogicalAddress(), HdmiCecKeycode.CEC_KEYCODE_POWER_ON_FUNCTION);
        this.mState = 3;
        addTimer(this.mState, 5000);
    }

    private void sendSetStreamPath() {
        tv().getActiveSource().invalidate();
        tv().setActivePath(this.mTarget.getPhysicalAddress());
        sendCommand(HdmiCecMessageBuilder.buildSetStreamPath(getSourceAddress(), this.mTarget.getPhysicalAddress()));
        invokeCallback(0);
        finish();
    }

    public void handleTimerEvent(int timeoutState) {
        if (this.mState != timeoutState) {
            Slog.w(TAG, "Timer in a wrong state. Ignored.");
            return;
        }
        switch (this.mState) {
            case 1:
                if (!tv().isPowerStandbyOrTransient()) {
                    sendSetStreamPath();
                    break;
                }
                invokeCallback(6);
                finish();
                return;
            case 2:
            case 3:
                this.mPowerStatusCounter++;
                queryDevicePowerStatus();
                break;
        }
    }

    private void invokeCallback(int result) {
        if (this.mCallback != null) {
            try {
                this.mCallback.onComplete(result);
            } catch (RemoteException e) {
                Slog.e(TAG, "Callback failed:" + e);
            }
        }
    }
}
