package com.android.server.hdmi;

import android.hardware.hdmi.HdmiDeviceInfo;
import android.util.SparseIntArray;
import com.android.server.display.RampAnimator;
import java.util.List;

public class PowerStatusMonitorAction extends HdmiCecFeatureAction {
    private static final int INVALID_POWER_STATUS = -2;
    private static final int MONITIROING_INTERNAL_MS = 60000;
    private static final int REPORT_POWER_STATUS_TIMEOUT_MS = 5000;
    private static final int STATE_WAIT_FOR_NEXT_MONITORING = 2;
    private static final int STATE_WAIT_FOR_REPORT_POWER_STATUS = 1;
    private static final String TAG = "PowerStatusMonitorAction";
    private final SparseIntArray mPowerStatus;

    /* renamed from: com.android.server.hdmi.PowerStatusMonitorAction.1 */
    class AnonymousClass1 implements SendMessageCallback {
        final /* synthetic */ int val$logicalAddress;

        AnonymousClass1(int val$logicalAddress) {
            this.val$logicalAddress = val$logicalAddress;
        }

        public void onSendCompleted(int error) {
            if (error != 0) {
                PowerStatusMonitorAction.this.updatePowerStatus(this.val$logicalAddress, -1, true);
            }
        }
    }

    PowerStatusMonitorAction(HdmiCecLocalDevice source) {
        super(source);
        this.mPowerStatus = new SparseIntArray();
    }

    boolean start() {
        queryPowerStatus();
        return true;
    }

    boolean processCommand(HdmiCecMessage cmd) {
        if (this.mState == STATE_WAIT_FOR_REPORT_POWER_STATUS && cmd.getOpcode() == HdmiCecKeycode.UI_SOUND_PRESENTATION_SELECT_AUDIO_AUTO_REVERBERATION) {
            return handleReportPowerStatus(cmd);
        }
        return false;
    }

    private boolean handleReportPowerStatus(HdmiCecMessage cmd) {
        int sourceAddress = cmd.getSource();
        if (this.mPowerStatus.get(sourceAddress, INVALID_POWER_STATUS) == INVALID_POWER_STATUS) {
            return false;
        }
        updatePowerStatus(sourceAddress, cmd.getParams()[0] & RampAnimator.DEFAULT_MAX_BRIGHTNESS, true);
        return true;
    }

    void handleTimerEvent(int state) {
        switch (this.mState) {
            case STATE_WAIT_FOR_REPORT_POWER_STATUS /*1*/:
                handleTimeout();
            case STATE_WAIT_FOR_NEXT_MONITORING /*2*/:
                queryPowerStatus();
            default:
        }
    }

    private void handleTimeout() {
        for (int i = 0; i < this.mPowerStatus.size(); i += STATE_WAIT_FOR_REPORT_POWER_STATUS) {
            updatePowerStatus(this.mPowerStatus.keyAt(i), -1, false);
        }
        this.mPowerStatus.clear();
        this.mState = STATE_WAIT_FOR_NEXT_MONITORING;
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
            int logicalAddress = info.getLogicalAddress();
            sendCommand(HdmiCecMessageBuilder.buildGiveDevicePowerStatus(getSourceAddress(), logicalAddress), new AnonymousClass1(logicalAddress));
        }
        this.mState = STATE_WAIT_FOR_REPORT_POWER_STATUS;
        addTimer(STATE_WAIT_FOR_NEXT_MONITORING, MONITIROING_INTERNAL_MS);
        addTimer(STATE_WAIT_FOR_REPORT_POWER_STATUS, REPORT_POWER_STATUS_TIMEOUT_MS);
    }

    private void updatePowerStatus(int logicalAddress, int newStatus, boolean remove) {
        tv().updateDevicePowerStatus(logicalAddress, newStatus);
        if (remove) {
            this.mPowerStatus.delete(logicalAddress);
        }
    }
}
