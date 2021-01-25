package com.android.server.hdmi;

import android.hardware.hdmi.HdmiDeviceInfo;
import android.hardware.hdmi.IHdmiControlCallback;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.hdmi.HdmiControlService;
import com.android.server.power.IHwShutdownThread;

/* access modifiers changed from: package-private */
public final class DeviceSelectAction extends HdmiCecFeatureAction {
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

    /* access modifiers changed from: package-private */
    public int getTargetAddress() {
        return this.mTarget.getLogicalAddress();
    }

    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public boolean start() {
        queryDevicePowerStatus();
        return true;
    }

    private void queryDevicePowerStatus() {
        sendCommand(this.mGivePowerStatus, new HdmiControlService.SendMessageCallback() {
            /* class com.android.server.hdmi.DeviceSelectAction.AnonymousClass1 */

            @Override // com.android.server.hdmi.HdmiControlService.SendMessageCallback
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

    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public boolean processCommand(HdmiCecMessage cmd) {
        if (cmd.getSource() != getTargetAddress()) {
            return false;
        }
        int opcode = cmd.getOpcode();
        byte[] params = cmd.getParams();
        if (this.mState == 1 && opcode == 144) {
            return handleReportPowerStatus(params[0]);
        }
        return false;
    }

    private boolean handleReportPowerStatus(int powerStatus) {
        if (powerStatus == 0) {
            sendSetStreamPath();
            return true;
        } else if (powerStatus == 1) {
            if (this.mPowerStatusCounter == 0) {
                turnOnDevice();
            } else {
                sendSetStreamPath();
            }
            return true;
        } else if (powerStatus == 2) {
            if (this.mPowerStatusCounter < 20) {
                this.mState = 3;
                addTimer(this.mState, 5000);
            } else {
                sendSetStreamPath();
            }
            return true;
        } else if (powerStatus != 3) {
            return false;
        } else {
            if (this.mPowerStatusCounter < 4) {
                this.mState = 2;
                addTimer(this.mState, 5000);
            } else {
                sendSetStreamPath();
            }
            return true;
        }
    }

    private void turnOnDevice() {
        sendUserControlPressedAndReleased(this.mTarget.getLogicalAddress(), 64);
        sendUserControlPressedAndReleased(this.mTarget.getLogicalAddress(), 109);
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

    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public void handleTimerEvent(int timeoutState) {
        if (this.mState != timeoutState) {
            Slog.w(TAG, "Timer in a wrong state. Ignored.");
            return;
        }
        int i = this.mState;
        if (i != 1) {
            if (i == 2 || i == 3) {
                this.mPowerStatusCounter++;
                queryDevicePowerStatus();
            }
        } else if (tv().isPowerStandbyOrTransient()) {
            invokeCallback(6);
            finish();
        } else {
            sendSetStreamPath();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void invokeCallback(int result) {
        IHdmiControlCallback iHdmiControlCallback = this.mCallback;
        if (iHdmiControlCallback != null) {
            try {
                iHdmiControlCallback.onComplete(result);
            } catch (RemoteException e) {
                Slog.e(TAG, "Callback failed:" + e);
            }
        }
    }
}
