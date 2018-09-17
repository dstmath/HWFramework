package com.android.server.hdmi;

import android.hardware.hdmi.HdmiDeviceInfo;
import android.util.SparseIntArray;
import java.util.List;

public class PowerStatusMonitorAction extends HdmiCecFeatureAction {
    private static final int INVALID_POWER_STATUS = -2;
    private static final int MONITIROING_INTERNAL_MS = 60000;
    private static final int REPORT_POWER_STATUS_TIMEOUT_MS = 5000;
    private static final int STATE_WAIT_FOR_NEXT_MONITORING = 2;
    private static final int STATE_WAIT_FOR_REPORT_POWER_STATUS = 1;
    private static final String TAG = "PowerStatusMonitorAction";
    private final SparseIntArray mPowerStatus = new SparseIntArray();

    PowerStatusMonitorAction(HdmiCecLocalDevice source) {
        super(source);
    }

    boolean start() {
        queryPowerStatus();
        return true;
    }

    boolean processCommand(HdmiCecMessage cmd) {
        if (this.mState == 1 && cmd.getOpcode() == 144) {
            return handleReportPowerStatus(cmd);
        }
        return false;
    }

    private boolean handleReportPowerStatus(HdmiCecMessage cmd) {
        int sourceAddress = cmd.getSource();
        if (this.mPowerStatus.get(sourceAddress, -2) == -2) {
            return false;
        }
        updatePowerStatus(sourceAddress, cmd.getParams()[0] & 255, true);
        return true;
    }

    void handleTimerEvent(int state) {
        switch (this.mState) {
            case 1:
                handleTimeout();
                return;
            case 2:
                queryPowerStatus();
                return;
            default:
                return;
        }
    }

    private void handleTimeout() {
        for (int i = 0; i < this.mPowerStatus.size(); i++) {
            updatePowerStatus(this.mPowerStatus.keyAt(i), -1, false);
        }
        this.mPowerStatus.clear();
        this.mState = 2;
    }

    private void resetPowerStatus(List<HdmiDeviceInfo> deviceInfos) {
        this.mPowerStatus.clear();
        for (HdmiDeviceInfo info : deviceInfos) {
            this.mPowerStatus.append(info.getLogicalAddress(), info.getDevicePowerStatus());
        }
    }

    private void queryPowerStatus() {
        List<HdmiDeviceInfo> deviceInfos = tv().getDeviceInfoList(false);
        resetPowerStatus(deviceInfos);
        for (HdmiDeviceInfo info : deviceInfos) {
            final int logicalAddress = info.getLogicalAddress();
            sendCommand(HdmiCecMessageBuilder.buildGiveDevicePowerStatus(getSourceAddress(), logicalAddress), new SendMessageCallback() {
                public void onSendCompleted(int error) {
                    if (error != 0) {
                        PowerStatusMonitorAction.this.updatePowerStatus(logicalAddress, -1, true);
                    }
                }
            });
        }
        this.mState = 1;
        addTimer(2, MONITIROING_INTERNAL_MS);
        addTimer(1, REPORT_POWER_STATUS_TIMEOUT_MS);
    }

    private void updatePowerStatus(int logicalAddress, int newStatus, boolean remove) {
        tv().updateDevicePowerStatus(logicalAddress, newStatus);
        if (remove) {
            this.mPowerStatus.delete(logicalAddress);
        }
    }
}
