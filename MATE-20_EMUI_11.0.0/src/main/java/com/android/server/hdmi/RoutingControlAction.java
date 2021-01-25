package com.android.server.hdmi;

import android.hardware.hdmi.HdmiDeviceInfo;
import android.hardware.hdmi.IHdmiControlCallback;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.hdmi.HdmiControlService;

/* access modifiers changed from: package-private */
public final class RoutingControlAction extends HdmiCecFeatureAction {
    private static final int STATE_WAIT_FOR_REPORT_POWER_STATUS = 2;
    private static final int STATE_WAIT_FOR_ROUTING_INFORMATION = 1;
    private static final String TAG = "RoutingControlAction";
    private static final int TIMEOUT_REPORT_POWER_STATUS_MS = 1000;
    private static final int TIMEOUT_ROUTING_INFORMATION_MS = 1000;
    private final IHdmiControlCallback mCallback;
    private int mCurrentRoutingPath;
    private final boolean mNotifyInputChange;
    private final boolean mQueryDevicePowerStatus;

    RoutingControlAction(HdmiCecLocalDevice localDevice, int path, boolean queryDevicePowerStatus, IHdmiControlCallback callback) {
        super(localDevice);
        this.mCallback = callback;
        this.mCurrentRoutingPath = path;
        this.mQueryDevicePowerStatus = queryDevicePowerStatus;
        this.mNotifyInputChange = callback == null;
    }

    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public boolean start() {
        this.mState = 1;
        addTimer(this.mState, 1000);
        return true;
    }

    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public boolean processCommand(HdmiCecMessage cmd) {
        int opcode = cmd.getOpcode();
        byte[] params = cmd.getParams();
        if (this.mState == 1 && opcode == 129) {
            int routingPath = HdmiUtils.twoBytesToInt(params);
            if (!HdmiUtils.isInActiveRoutingPath(this.mCurrentRoutingPath, routingPath)) {
                return true;
            }
            this.mCurrentRoutingPath = routingPath;
            removeActionExcept(RoutingControlAction.class, this);
            addTimer(this.mState, 1000);
            return true;
        } else if (this.mState != 2 || opcode != 144) {
            return false;
        } else {
            handleReportPowerStatus(cmd.getParams()[0]);
            return true;
        }
    }

    private void handleReportPowerStatus(int devicePowerStatus) {
        if (isPowerOnOrTransient(getTvPowerStatus())) {
            updateActiveInput();
            if (isPowerOnOrTransient(devicePowerStatus)) {
                sendSetStreamPath();
            }
        }
        finishWithCallback(0);
    }

    private void updateActiveInput() {
        HdmiCecLocalDeviceTv tv = tv();
        tv.setPrevPortId(tv.getActivePortId());
        tv.updateActiveInput(this.mCurrentRoutingPath, this.mNotifyInputChange);
    }

    private int getTvPowerStatus() {
        return tv().getPowerStatus();
    }

    private static boolean isPowerOnOrTransient(int status) {
        return status == 0 || status == 2;
    }

    private void sendSetStreamPath() {
        sendCommand(HdmiCecMessageBuilder.buildSetStreamPath(getSourceAddress(), this.mCurrentRoutingPath));
    }

    private void finishWithCallback(int result) {
        invokeCallback(result);
        finish();
    }

    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public void handleTimerEvent(int timeoutState) {
        if (this.mState != timeoutState || this.mState == 0) {
            Slog.w("CEC", "Timer in a wrong state. Ignored.");
        } else if (timeoutState == 1) {
            HdmiDeviceInfo device = tv().getDeviceInfoByPath(this.mCurrentRoutingPath);
            if (device == null || !this.mQueryDevicePowerStatus) {
                updateActiveInput();
                finishWithCallback(0);
                return;
            }
            queryDevicePowerStatus(device.getLogicalAddress(), new HdmiControlService.SendMessageCallback() {
                /* class com.android.server.hdmi.RoutingControlAction.AnonymousClass1 */

                @Override // com.android.server.hdmi.HdmiControlService.SendMessageCallback
                public void onSendCompleted(int error) {
                    RoutingControlAction.this.handlDevicePowerStatusAckResult(error == 0);
                }
            });
        } else if (timeoutState == 2) {
            if (isPowerOnOrTransient(getTvPowerStatus())) {
                updateActiveInput();
                sendSetStreamPath();
            }
            finishWithCallback(0);
        }
    }

    private void queryDevicePowerStatus(int address, HdmiControlService.SendMessageCallback callback) {
        sendCommand(HdmiCecMessageBuilder.buildGiveDevicePowerStatus(getSourceAddress(), address), callback);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlDevicePowerStatusAckResult(boolean acked) {
        if (acked) {
            this.mState = 2;
            addTimer(this.mState, 1000);
            return;
        }
        updateActiveInput();
        sendSetStreamPath();
        finishWithCallback(0);
    }

    private void invokeCallback(int result) {
        IHdmiControlCallback iHdmiControlCallback = this.mCallback;
        if (iHdmiControlCallback != null) {
            try {
                iHdmiControlCallback.onComplete(result);
            } catch (RemoteException e) {
            }
        }
    }
}
