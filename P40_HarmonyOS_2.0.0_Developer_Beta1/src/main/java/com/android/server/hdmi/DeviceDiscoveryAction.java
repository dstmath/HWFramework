package com.android.server.hdmi;

import android.hardware.hdmi.HdmiDeviceInfo;
import android.util.Slog;
import com.android.internal.util.Preconditions;
import com.android.server.hdmi.HdmiControlService;
import com.android.server.power.IHwShutdownThread;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* access modifiers changed from: package-private */
public final class DeviceDiscoveryAction extends HdmiCecFeatureAction {
    private static final int STATE_WAITING_FOR_DEVICES = 5;
    private static final int STATE_WAITING_FOR_DEVICE_POLLING = 1;
    private static final int STATE_WAITING_FOR_OSD_NAME = 3;
    private static final int STATE_WAITING_FOR_PHYSICAL_ADDRESS = 2;
    private static final int STATE_WAITING_FOR_POWER = 6;
    private static final int STATE_WAITING_FOR_VENDOR_ID = 4;
    private static final String TAG = "DeviceDiscoveryAction";
    private final DeviceDiscoveryCallback mCallback;
    private final int mDelayPeriod;
    private final ArrayList<DeviceInfo> mDevices;
    private boolean mIsTvDevice;
    private int mProcessedDeviceCount;
    private int mTimeoutRetry;

    /* access modifiers changed from: package-private */
    public interface DeviceDiscoveryCallback {
        void onDeviceDiscoveryDone(List<HdmiDeviceInfo> list);
    }

    /* access modifiers changed from: private */
    public static final class DeviceInfo {
        private int mDeviceType;
        private String mDisplayName;
        private final int mLogicalAddress;
        private int mPhysicalAddress;
        private int mPortId;
        private int mPowerStatus;
        private int mVendorId;

        private DeviceInfo(int logicalAddress) {
            this.mPhysicalAddress = 65535;
            this.mPortId = -1;
            this.mVendorId = 16777215;
            this.mPowerStatus = -1;
            this.mDisplayName = "";
            this.mDeviceType = -1;
            this.mLogicalAddress = logicalAddress;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private HdmiDeviceInfo toHdmiDeviceInfo() {
            return new HdmiDeviceInfo(this.mLogicalAddress, this.mPhysicalAddress, this.mPortId, this.mDeviceType, this.mVendorId, this.mDisplayName, this.mPowerStatus);
        }
    }

    DeviceDiscoveryAction(HdmiCecLocalDevice source, DeviceDiscoveryCallback callback, int delay) {
        super(source);
        this.mDevices = new ArrayList<>();
        this.mProcessedDeviceCount = 0;
        this.mTimeoutRetry = 0;
        this.mIsTvDevice = localDevice().mService.isTvDevice();
        this.mCallback = (DeviceDiscoveryCallback) Preconditions.checkNotNull(callback);
        this.mDelayPeriod = delay;
    }

    DeviceDiscoveryAction(HdmiCecLocalDevice source, DeviceDiscoveryCallback callback) {
        this(source, callback, 0);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public boolean start() {
        this.mDevices.clear();
        this.mState = 1;
        pollDevices(new HdmiControlService.DevicePollingCallback() {
            /* class com.android.server.hdmi.DeviceDiscoveryAction.AnonymousClass1 */

            @Override // com.android.server.hdmi.HdmiControlService.DevicePollingCallback
            public void onPollingFinished(List<Integer> ackedAddress) {
                if (ackedAddress.isEmpty()) {
                    Slog.v(DeviceDiscoveryAction.TAG, "No device is detected.");
                    DeviceDiscoveryAction.this.wrapUpAndFinish();
                    return;
                }
                Slog.v(DeviceDiscoveryAction.TAG, "Device detected: " + ackedAddress);
                DeviceDiscoveryAction.this.allocateDevices(ackedAddress);
                if (DeviceDiscoveryAction.this.mDelayPeriod > 0) {
                    DeviceDiscoveryAction.this.startToDelayAction();
                } else {
                    DeviceDiscoveryAction.this.startPhysicalAddressStage();
                }
            }
        }, 131073, 1);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void allocateDevices(List<Integer> addresses) {
        for (Integer i : addresses) {
            this.mDevices.add(new DeviceInfo(i.intValue()));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startToDelayAction() {
        Slog.v(TAG, "Waiting for connected devices to be ready");
        this.mState = 5;
        checkAndProceedStage();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startPhysicalAddressStage() {
        Slog.v(TAG, "Start [Physical Address Stage]:" + this.mDevices.size());
        this.mProcessedDeviceCount = 0;
        this.mState = 2;
        checkAndProceedStage();
    }

    private boolean verifyValidLogicalAddress(int address) {
        return address >= 0 && address < 15;
    }

    private void queryPhysicalAddress(int address) {
        if (!verifyValidLogicalAddress(address)) {
            checkAndProceedStage();
            return;
        }
        this.mActionTimer.clearTimerMessage();
        if (!mayProcessMessageIfCached(address, 132)) {
            sendCommand(HdmiCecMessageBuilder.buildGivePhysicalAddress(getSourceAddress(), address));
            addTimer(this.mState, IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME);
        }
    }

    private void delayActionWithTimePeriod(int timeDelay) {
        this.mActionTimer.clearTimerMessage();
        addTimer(this.mState, timeDelay);
    }

    private void startOsdNameStage() {
        Slog.v(TAG, "Start [Osd Name Stage]:" + this.mDevices.size());
        this.mProcessedDeviceCount = 0;
        this.mState = 3;
        checkAndProceedStage();
    }

    private void queryOsdName(int address) {
        if (!verifyValidLogicalAddress(address)) {
            checkAndProceedStage();
            return;
        }
        this.mActionTimer.clearTimerMessage();
        if (!mayProcessMessageIfCached(address, 71)) {
            sendCommand(HdmiCecMessageBuilder.buildGiveOsdNameCommand(getSourceAddress(), address));
            addTimer(this.mState, IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME);
        }
    }

    private void startVendorIdStage() {
        Slog.v(TAG, "Start [Vendor Id Stage]:" + this.mDevices.size());
        this.mProcessedDeviceCount = 0;
        this.mState = 4;
        checkAndProceedStage();
    }

    private void queryVendorId(int address) {
        if (!verifyValidLogicalAddress(address)) {
            checkAndProceedStage();
            return;
        }
        this.mActionTimer.clearTimerMessage();
        if (!mayProcessMessageIfCached(address, 135)) {
            sendCommand(HdmiCecMessageBuilder.buildGiveDeviceVendorIdCommand(getSourceAddress(), address));
            addTimer(this.mState, IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME);
        }
    }

    private void startPowerStatusStage() {
        Slog.v(TAG, "Start [Power Status Stage]:" + this.mDevices.size());
        this.mProcessedDeviceCount = 0;
        this.mState = 6;
        checkAndProceedStage();
    }

    private void queryPowerStatus(int address) {
        if (!verifyValidLogicalAddress(address)) {
            checkAndProceedStage();
            return;
        }
        this.mActionTimer.clearTimerMessage();
        if (!mayProcessMessageIfCached(address, 144)) {
            sendCommand(HdmiCecMessageBuilder.buildGiveDevicePowerStatus(getSourceAddress(), address));
            addTimer(this.mState, IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME);
        }
    }

    private boolean mayProcessMessageIfCached(int address, int opcode) {
        HdmiCecMessage message = getCecMessageCache().getMessage(address, opcode);
        if (message == null) {
            return false;
        }
        processCommand(message);
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public boolean processCommand(HdmiCecMessage cmd) {
        int i = this.mState;
        if (i != 2) {
            if (i != 3) {
                if (i != 4) {
                    if (i != 6) {
                        return false;
                    }
                    if (cmd.getOpcode() == 144) {
                        handleReportPowerStatus(cmd);
                        return true;
                    } else if (cmd.getOpcode() != 0 || (cmd.getParams()[0] & 255) != 144) {
                        return false;
                    } else {
                        handleReportPowerStatus(cmd);
                        return true;
                    }
                } else if (cmd.getOpcode() == 135) {
                    handleVendorId(cmd);
                    return true;
                } else if (cmd.getOpcode() != 0 || (cmd.getParams()[0] & 255) != 140) {
                    return false;
                } else {
                    handleVendorId(cmd);
                    return true;
                }
            } else if (cmd.getOpcode() == 71) {
                handleSetOsdName(cmd);
                return true;
            } else if (cmd.getOpcode() != 0 || (cmd.getParams()[0] & 255) != 70) {
                return false;
            } else {
                handleSetOsdName(cmd);
                return true;
            }
        } else if (cmd.getOpcode() != 132) {
            return false;
        } else {
            handleReportPhysicalAddress(cmd);
            return true;
        }
    }

    private void handleReportPhysicalAddress(HdmiCecMessage cmd) {
        Preconditions.checkState(this.mProcessedDeviceCount < this.mDevices.size());
        DeviceInfo current = this.mDevices.get(this.mProcessedDeviceCount);
        if (current.mLogicalAddress != cmd.getSource()) {
            Slog.w(TAG, "Unmatched address[expected:" + current.mLogicalAddress + ", actual:" + cmd.getSource());
            return;
        }
        byte[] params = cmd.getParams();
        current.mPhysicalAddress = HdmiUtils.twoBytesToInt(params);
        current.mPortId = getPortId(current.mPhysicalAddress);
        current.mDeviceType = params[2] & 255;
        current.mDisplayName = HdmiUtils.getDefaultDeviceName(current.mDeviceType);
        if (this.mIsTvDevice) {
            tv().updateCecSwitchInfo(current.mLogicalAddress, current.mDeviceType, current.mPhysicalAddress);
        }
        increaseProcessedDeviceCount();
        checkAndProceedStage();
    }

    private int getPortId(int physicalAddress) {
        if (this.mIsTvDevice) {
            return tv().getPortId(physicalAddress);
        }
        return source().getPortId(physicalAddress);
    }

    private void handleSetOsdName(HdmiCecMessage cmd) {
        String displayName;
        Preconditions.checkState(this.mProcessedDeviceCount < this.mDevices.size());
        DeviceInfo current = this.mDevices.get(this.mProcessedDeviceCount);
        if (current.mLogicalAddress != cmd.getSource()) {
            Slog.w(TAG, "Unmatched address[expected:" + current.mLogicalAddress + ", actual:" + cmd.getSource());
            return;
        }
        try {
            if (cmd.getOpcode() == 0) {
                displayName = HdmiUtils.getDefaultDeviceName(current.mLogicalAddress);
            } else {
                displayName = new String(cmd.getParams(), "US-ASCII");
            }
        } catch (UnsupportedEncodingException e) {
            Slog.w(TAG, "Failed to decode display name: " + cmd.toString());
            displayName = HdmiUtils.getDefaultDeviceName(current.mLogicalAddress);
        }
        current.mDisplayName = displayName;
        increaseProcessedDeviceCount();
        checkAndProceedStage();
    }

    private void handleVendorId(HdmiCecMessage cmd) {
        Preconditions.checkState(this.mProcessedDeviceCount < this.mDevices.size());
        DeviceInfo current = this.mDevices.get(this.mProcessedDeviceCount);
        if (current.mLogicalAddress != cmd.getSource()) {
            Slog.w(TAG, "Unmatched address[expected:" + current.mLogicalAddress + ", actual:" + cmd.getSource());
            return;
        }
        if (cmd.getOpcode() != 0) {
            current.mVendorId = HdmiUtils.threeBytesToInt(cmd.getParams());
        }
        increaseProcessedDeviceCount();
        checkAndProceedStage();
    }

    private void handleReportPowerStatus(HdmiCecMessage cmd) {
        Preconditions.checkState(this.mProcessedDeviceCount < this.mDevices.size());
        DeviceInfo current = this.mDevices.get(this.mProcessedDeviceCount);
        if (current.mLogicalAddress != cmd.getSource()) {
            Slog.w(TAG, "Unmatched address[expected:" + current.mLogicalAddress + ", actual:" + cmd.getSource());
            return;
        }
        if (cmd.getOpcode() != 0) {
            current.mPowerStatus = cmd.getParams()[0] & 255;
        }
        increaseProcessedDeviceCount();
        checkAndProceedStage();
    }

    private void increaseProcessedDeviceCount() {
        this.mProcessedDeviceCount++;
        this.mTimeoutRetry = 0;
    }

    private void removeDevice(int index) {
        this.mDevices.remove(index);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void wrapUpAndFinish() {
        Slog.v(TAG, "---------Wrap up Device Discovery:[" + this.mDevices.size() + "]---------");
        ArrayList<HdmiDeviceInfo> result = new ArrayList<>();
        Iterator<DeviceInfo> it = this.mDevices.iterator();
        while (it.hasNext()) {
            HdmiDeviceInfo cecDeviceInfo = it.next().toHdmiDeviceInfo();
            Slog.v(TAG, " DeviceInfo: " + cecDeviceInfo);
            result.add(cecDeviceInfo);
        }
        Slog.v(TAG, "--------------------------------------------");
        this.mCallback.onDeviceDiscoveryDone(result);
        finish();
        if (this.mIsTvDevice) {
            tv().processAllDelayedMessages();
        }
    }

    private void checkAndProceedStage() {
        if (this.mDevices.isEmpty()) {
            wrapUpAndFinish();
        } else if (this.mProcessedDeviceCount == this.mDevices.size()) {
            this.mProcessedDeviceCount = 0;
            int i = this.mState;
            if (i == 2) {
                startOsdNameStage();
            } else if (i == 3) {
                startVendorIdStage();
            } else if (i == 4) {
                startPowerStatusStage();
            } else if (i == 6) {
                wrapUpAndFinish();
            }
        } else {
            sendQueryCommand();
        }
    }

    private void sendQueryCommand() {
        int address = this.mDevices.get(this.mProcessedDeviceCount).mLogicalAddress;
        int i = this.mState;
        if (i == 2) {
            queryPhysicalAddress(address);
        } else if (i == 3) {
            queryOsdName(address);
        } else if (i == 4) {
            queryVendorId(address);
        } else if (i == 5) {
            delayActionWithTimePeriod(this.mDelayPeriod);
        } else if (i == 6) {
            queryPowerStatus(address);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public void handleTimerEvent(int state) {
        if (this.mState != 0 && this.mState == state) {
            if (this.mState == 5) {
                startPhysicalAddressStage();
                return;
            }
            int i = this.mTimeoutRetry + 1;
            this.mTimeoutRetry = i;
            if (i < 5) {
                sendQueryCommand();
                return;
            }
            this.mTimeoutRetry = 0;
            Slog.v(TAG, "Timeout[State=" + this.mState + ", Processed=" + this.mProcessedDeviceCount);
            if (this.mState == 6 || this.mState == 3) {
                increaseProcessedDeviceCount();
            } else {
                removeDevice(this.mProcessedDeviceCount);
            }
            checkAndProceedStage();
        }
    }
}
