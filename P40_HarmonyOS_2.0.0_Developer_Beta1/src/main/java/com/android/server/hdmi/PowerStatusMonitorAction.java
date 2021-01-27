package com.android.server.hdmi;

import android.hardware.hdmi.HdmiDeviceInfo;
import android.util.SparseIntArray;
import com.android.server.hdmi.HdmiControlService;
import java.util.List;

public class PowerStatusMonitorAction extends HdmiCecFeatureAction {
    private static final int INVALID_POWER_STATUS = -2;
    private static final int MONITORING_INTERNAL_MS = 60000;
    private static final int REPORT_POWER_STATUS_TIMEOUT_MS = 5000;
    private static final int STATE_WAIT_FOR_NEXT_MONITORING = 2;
    private static final int STATE_WAIT_FOR_REPORT_POWER_STATUS = 1;
    private static final String TAG = "PowerStatusMonitorAction";
    private final SparseIntArray mPowerStatus = new SparseIntArray();

    PowerStatusMonitorAction(HdmiCecLocalDevice source) {
        super(source);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public boolean start() {
        queryPowerStatus();
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public boolean processCommand(HdmiCecMessage cmd) {
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

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public void handleTimerEvent(int state) {
        int i = this.mState;
        if (i == 1) {
            handleTimeout();
        } else if (i == 2) {
            queryPowerStatus();
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
            sendCommand(HdmiCecMessageBuilder.buildGiveDevicePowerStatus(getSourceAddress(), logicalAddress), new HdmiControlService.SendMessageCallback() {
                /* class com.android.server.hdmi.PowerStatusMonitorAction.AnonymousClass1 */

                @Override // com.android.server.hdmi.HdmiControlService.SendMessageCallback
                public void onSendCompleted(int error) {
                    if (error != 0) {
                        PowerStatusMonitorAction.this.updatePowerStatus(logicalAddress, -1, true);
                    }
                }
            });
        }
        this.mState = 1;
        addTimer(2, MONITORING_INTERNAL_MS);
        addTimer(1, REPORT_POWER_STATUS_TIMEOUT_MS);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updatePowerStatus(int logicalAddress, int newStatus, boolean remove) {
        tv().updateDevicePowerStatus(logicalAddress, newStatus);
        if (remove) {
            this.mPowerStatus.delete(logicalAddress);
        }
    }
}
