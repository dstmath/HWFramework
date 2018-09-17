package com.android.server.hdmi;

import android.hardware.hdmi.HdmiDeviceInfo;
import android.util.Slog;
import com.android.internal.util.Preconditions;
import com.android.server.display.RampAnimator;
import com.android.server.power.IHwShutdownThread;
import com.android.server.usb.UsbAudioDevice;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

final class DeviceDiscoveryAction extends HdmiCecFeatureAction {
    private static final int STATE_WAITING_FOR_DEVICE_POLLING = 1;
    private static final int STATE_WAITING_FOR_OSD_NAME = 3;
    private static final int STATE_WAITING_FOR_PHYSICAL_ADDRESS = 2;
    private static final int STATE_WAITING_FOR_VENDOR_ID = 4;
    private static final String TAG = "DeviceDiscoveryAction";
    private final DeviceDiscoveryCallback mCallback;
    private final ArrayList<DeviceInfo> mDevices;
    private int mProcessedDeviceCount;
    private int mTimeoutRetry;

    interface DeviceDiscoveryCallback {
        void onDeviceDiscoveryDone(List<HdmiDeviceInfo> list);
    }

    private static final class DeviceInfo {
        private int mDeviceType;
        private String mDisplayName;
        private final int mLogicalAddress;
        private int mPhysicalAddress;
        private int mPortId;
        private int mVendorId;

        private DeviceInfo(int logicalAddress) {
            this.mPhysicalAddress = 65535;
            this.mPortId = -1;
            this.mVendorId = UsbAudioDevice.kAudioDeviceClassMask;
            this.mDisplayName = "";
            this.mDeviceType = -1;
            this.mLogicalAddress = logicalAddress;
        }

        private HdmiDeviceInfo toHdmiDeviceInfo() {
            return new HdmiDeviceInfo(this.mLogicalAddress, this.mPhysicalAddress, this.mPortId, this.mDeviceType, this.mVendorId, this.mDisplayName);
        }
    }

    DeviceDiscoveryAction(HdmiCecLocalDevice source, DeviceDiscoveryCallback callback) {
        super(source);
        this.mDevices = new ArrayList();
        this.mProcessedDeviceCount = 0;
        this.mTimeoutRetry = 0;
        this.mCallback = (DeviceDiscoveryCallback) Preconditions.checkNotNull(callback);
    }

    boolean start() {
        this.mDevices.clear();
        this.mState = STATE_WAITING_FOR_DEVICE_POLLING;
        pollDevices(new DevicePollingCallback() {
            public void onPollingFinished(List<Integer> ackedAddress) {
                if (ackedAddress.isEmpty()) {
                    Slog.v(DeviceDiscoveryAction.TAG, "No device is detected.");
                    DeviceDiscoveryAction.this.wrapUpAndFinish();
                    return;
                }
                Slog.v(DeviceDiscoveryAction.TAG, "Device detected: " + ackedAddress);
                DeviceDiscoveryAction.this.allocateDevices(ackedAddress);
                DeviceDiscoveryAction.this.startPhysicalAddressStage();
            }
        }, 131073, STATE_WAITING_FOR_DEVICE_POLLING);
        return true;
    }

    private void allocateDevices(List<Integer> addresses) {
        for (Integer i : addresses) {
            this.mDevices.add(new DeviceInfo(null));
        }
    }

    private void startPhysicalAddressStage() {
        Slog.v(TAG, "Start [Physical Address Stage]:" + this.mDevices.size());
        this.mProcessedDeviceCount = 0;
        this.mState = STATE_WAITING_FOR_PHYSICAL_ADDRESS;
        checkAndProceedStage();
    }

    private boolean verifyValidLogicalAddress(int address) {
        return address >= 0 && address < 15;
    }

    private void queryPhysicalAddress(int address) {
        if (verifyValidLogicalAddress(address)) {
            this.mActionTimer.clearTimerMessage();
            if (!mayProcessMessageIfCached(address, 132)) {
                sendCommand(HdmiCecMessageBuilder.buildGivePhysicalAddress(getSourceAddress(), address));
                addTimer(this.mState, IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME);
                return;
            }
            return;
        }
        checkAndProceedStage();
    }

    private void startOsdNameStage() {
        Slog.v(TAG, "Start [Osd Name Stage]:" + this.mDevices.size());
        this.mProcessedDeviceCount = 0;
        this.mState = STATE_WAITING_FOR_OSD_NAME;
        checkAndProceedStage();
    }

    private void queryOsdName(int address) {
        if (verifyValidLogicalAddress(address)) {
            this.mActionTimer.clearTimerMessage();
            if (!mayProcessMessageIfCached(address, 71)) {
                sendCommand(HdmiCecMessageBuilder.buildGiveOsdNameCommand(getSourceAddress(), address));
                addTimer(this.mState, IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME);
                return;
            }
            return;
        }
        checkAndProceedStage();
    }

    private void startVendorIdStage() {
        Slog.v(TAG, "Start [Vendor Id Stage]:" + this.mDevices.size());
        this.mProcessedDeviceCount = 0;
        this.mState = STATE_WAITING_FOR_VENDOR_ID;
        checkAndProceedStage();
    }

    private void queryVendorId(int address) {
        if (verifyValidLogicalAddress(address)) {
            this.mActionTimer.clearTimerMessage();
            if (!mayProcessMessageIfCached(address, 135)) {
                sendCommand(HdmiCecMessageBuilder.buildGiveDeviceVendorIdCommand(getSourceAddress(), address));
                addTimer(this.mState, IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME);
                return;
            }
            return;
        }
        checkAndProceedStage();
    }

    private boolean mayProcessMessageIfCached(int address, int opcode) {
        HdmiCecMessage message = getCecMessageCache().getMessage(address, opcode);
        if (message == null) {
            return false;
        }
        processCommand(message);
        return true;
    }

    boolean processCommand(HdmiCecMessage cmd) {
        switch (this.mState) {
            case STATE_WAITING_FOR_PHYSICAL_ADDRESS /*2*/:
                if (cmd.getOpcode() != 132) {
                    return false;
                }
                handleReportPhysicalAddress(cmd);
                return true;
            case STATE_WAITING_FOR_OSD_NAME /*3*/:
                if (cmd.getOpcode() != 71) {
                    return false;
                }
                handleSetOsdName(cmd);
                return true;
            case STATE_WAITING_FOR_VENDOR_ID /*4*/:
                if (cmd.getOpcode() != 135) {
                    return false;
                }
                handleVendorId(cmd);
                return true;
            default:
                return false;
        }
    }

    private void handleReportPhysicalAddress(HdmiCecMessage cmd) {
        Preconditions.checkState(this.mProcessedDeviceCount < this.mDevices.size());
        DeviceInfo current = (DeviceInfo) this.mDevices.get(this.mProcessedDeviceCount);
        if (current.mLogicalAddress != cmd.getSource()) {
            Slog.w(TAG, "Unmatched address[expected:" + current.mLogicalAddress + ", actual:" + cmd.getSource());
            return;
        }
        byte[] params = cmd.getParams();
        current.mPhysicalAddress = HdmiUtils.twoBytesToInt(params);
        current.mPortId = getPortId(current.mPhysicalAddress);
        current.mDeviceType = params[STATE_WAITING_FOR_PHYSICAL_ADDRESS] & RampAnimator.DEFAULT_MAX_BRIGHTNESS;
        tv().updateCecSwitchInfo(current.mLogicalAddress, current.mDeviceType, current.mPhysicalAddress);
        increaseProcessedDeviceCount();
        checkAndProceedStage();
    }

    private int getPortId(int physicalAddress) {
        return tv().getPortId(physicalAddress);
    }

    private void handleSetOsdName(HdmiCecMessage cmd) {
        Preconditions.checkState(this.mProcessedDeviceCount < this.mDevices.size());
        DeviceInfo current = (DeviceInfo) this.mDevices.get(this.mProcessedDeviceCount);
        if (current.mLogicalAddress != cmd.getSource()) {
            Slog.w(TAG, "Unmatched address[expected:" + current.mLogicalAddress + ", actual:" + cmd.getSource());
            return;
        }
        String displayName;
        try {
            displayName = new String(cmd.getParams(), "US-ASCII");
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
        DeviceInfo current = (DeviceInfo) this.mDevices.get(this.mProcessedDeviceCount);
        if (current.mLogicalAddress != cmd.getSource()) {
            Slog.w(TAG, "Unmatched address[expected:" + current.mLogicalAddress + ", actual:" + cmd.getSource());
            return;
        }
        current.mVendorId = HdmiUtils.threeBytesToInt(cmd.getParams());
        increaseProcessedDeviceCount();
        checkAndProceedStage();
    }

    private void increaseProcessedDeviceCount() {
        this.mProcessedDeviceCount += STATE_WAITING_FOR_DEVICE_POLLING;
        this.mTimeoutRetry = 0;
    }

    private void removeDevice(int index) {
        this.mDevices.remove(index);
    }

    private void wrapUpAndFinish() {
        Slog.v(TAG, "---------Wrap up Device Discovery:[" + this.mDevices.size() + "]---------");
        ArrayList<HdmiDeviceInfo> result = new ArrayList();
        for (DeviceInfo info : this.mDevices) {
            HdmiDeviceInfo cecDeviceInfo = info.toHdmiDeviceInfo();
            Slog.v(TAG, " DeviceInfo: " + cecDeviceInfo);
            result.add(cecDeviceInfo);
        }
        Slog.v(TAG, "--------------------------------------------");
        this.mCallback.onDeviceDiscoveryDone(result);
        finish();
        tv().processAllDelayedMessages();
    }

    private void checkAndProceedStage() {
        if (this.mDevices.isEmpty()) {
            wrapUpAndFinish();
        } else if (this.mProcessedDeviceCount == this.mDevices.size()) {
            this.mProcessedDeviceCount = 0;
            switch (this.mState) {
                case STATE_WAITING_FOR_PHYSICAL_ADDRESS /*2*/:
                    startOsdNameStage();
                case STATE_WAITING_FOR_OSD_NAME /*3*/:
                    startVendorIdStage();
                case STATE_WAITING_FOR_VENDOR_ID /*4*/:
                    wrapUpAndFinish();
                default:
            }
        } else {
            sendQueryCommand();
        }
    }

    private void sendQueryCommand() {
        int address = ((DeviceInfo) this.mDevices.get(this.mProcessedDeviceCount)).mLogicalAddress;
        switch (this.mState) {
            case STATE_WAITING_FOR_PHYSICAL_ADDRESS /*2*/:
                queryPhysicalAddress(address);
            case STATE_WAITING_FOR_OSD_NAME /*3*/:
                queryOsdName(address);
            case STATE_WAITING_FOR_VENDOR_ID /*4*/:
                queryVendorId(address);
                break;
        }
    }

    void handleTimerEvent(int state) {
        if (this.mState != 0 && this.mState == state) {
            int i = this.mTimeoutRetry + STATE_WAITING_FOR_DEVICE_POLLING;
            this.mTimeoutRetry = i;
            if (i < 5) {
                sendQueryCommand();
                return;
            }
            this.mTimeoutRetry = 0;
            Slog.v(TAG, "Timeout[State=" + this.mState + ", Processed=" + this.mProcessedDeviceCount);
            removeDevice(this.mProcessedDeviceCount);
            checkAndProceedStage();
        }
    }
}
