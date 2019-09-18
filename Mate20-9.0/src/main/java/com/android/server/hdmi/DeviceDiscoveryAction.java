package com.android.server.hdmi;

import android.hardware.hdmi.HdmiDeviceInfo;
import android.net.util.NetworkConstants;
import android.util.Slog;
import com.android.internal.util.Preconditions;
import com.android.server.hdmi.HdmiControlService;
import com.android.server.power.IHwShutdownThread;
import com.android.server.usb.UsbAudioDevice;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

final class DeviceDiscoveryAction extends HdmiCecFeatureAction {
    private static final int STATE_WAITING_FOR_DEVICE_POLLING = 1;
    private static final int STATE_WAITING_FOR_OSD_NAME = 3;
    private static final int STATE_WAITING_FOR_PHYSICAL_ADDRESS = 2;
    private static final int STATE_WAITING_FOR_VENDOR_ID = 4;
    private static final String TAG = "DeviceDiscoveryAction";
    private final DeviceDiscoveryCallback mCallback;
    private final ArrayList<DeviceInfo> mDevices = new ArrayList<>();
    private int mProcessedDeviceCount = 0;
    private int mTimeoutRetry = 0;

    interface DeviceDiscoveryCallback {
        void onDeviceDiscoveryDone(List<HdmiDeviceInfo> list);
    }

    private static final class DeviceInfo {
        /* access modifiers changed from: private */
        public int mDeviceType;
        /* access modifiers changed from: private */
        public String mDisplayName;
        /* access modifiers changed from: private */
        public final int mLogicalAddress;
        /* access modifiers changed from: private */
        public int mPhysicalAddress;
        /* access modifiers changed from: private */
        public int mPortId;
        /* access modifiers changed from: private */
        public int mVendorId;

        private DeviceInfo(int logicalAddress) {
            this.mPhysicalAddress = NetworkConstants.ARP_HWTYPE_RESERVED_HI;
            this.mPortId = -1;
            this.mVendorId = UsbAudioDevice.kAudioDeviceClassMask;
            this.mDisplayName = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            this.mDeviceType = -1;
            this.mLogicalAddress = logicalAddress;
        }

        /* access modifiers changed from: private */
        public HdmiDeviceInfo toHdmiDeviceInfo() {
            HdmiDeviceInfo hdmiDeviceInfo = new HdmiDeviceInfo(this.mLogicalAddress, this.mPhysicalAddress, this.mPortId, this.mDeviceType, this.mVendorId, this.mDisplayName);
            return hdmiDeviceInfo;
        }
    }

    DeviceDiscoveryAction(HdmiCecLocalDevice source, DeviceDiscoveryCallback callback) {
        super(source);
        this.mCallback = (DeviceDiscoveryCallback) Preconditions.checkNotNull(callback);
    }

    /* access modifiers changed from: package-private */
    public boolean start() {
        this.mDevices.clear();
        this.mState = 1;
        pollDevices(new HdmiControlService.DevicePollingCallback() {
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
        }, 131073, 1);
        return true;
    }

    /* access modifiers changed from: private */
    public void allocateDevices(List<Integer> addresses) {
        for (Integer i : addresses) {
            this.mDevices.add(new DeviceInfo(i.intValue()));
        }
    }

    /* access modifiers changed from: private */
    public void startPhysicalAddressStage() {
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
        if (!mayProcessMessageIfCached(address, NetworkConstants.ICMPV6_NEIGHBOR_SOLICITATION)) {
            sendCommand(HdmiCecMessageBuilder.buildGiveDeviceVendorIdCommand(getSourceAddress(), address));
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
    public boolean processCommand(HdmiCecMessage cmd) {
        switch (this.mState) {
            case 2:
                if (cmd.getOpcode() != 132) {
                    return false;
                }
                handleReportPhysicalAddress(cmd);
                return true;
            case 3:
                if (cmd.getOpcode() == 71) {
                    handleSetOsdName(cmd);
                    return true;
                } else if (cmd.getOpcode() != 0 || (cmd.getParams()[0] & 255) != 70) {
                    return false;
                } else {
                    handleSetOsdName(cmd);
                    return true;
                }
            case 4:
                if (cmd.getOpcode() == 135) {
                    handleVendorId(cmd);
                    return true;
                } else if (cmd.getOpcode() != 0 || (cmd.getParams()[0] & 255) != 140) {
                    return false;
                } else {
                    handleVendorId(cmd);
                    return true;
                }
            default:
                return false;
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
        int unused = current.mPhysicalAddress = HdmiUtils.twoBytesToInt(params);
        int unused2 = current.mPortId = getPortId(current.mPhysicalAddress);
        int unused3 = current.mDeviceType = params[2] & 255;
        tv().updateCecSwitchInfo(current.mLogicalAddress, current.mDeviceType, current.mPhysicalAddress);
        increaseProcessedDeviceCount();
        checkAndProceedStage();
    }

    private int getPortId(int physicalAddress) {
        return tv().getPortId(physicalAddress);
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
        String unused = current.mDisplayName = displayName;
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
            int unused = current.mVendorId = HdmiUtils.threeBytesToInt(cmd.getParams());
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
    public void wrapUpAndFinish() {
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
        tv().processAllDelayedMessages();
    }

    private void checkAndProceedStage() {
        if (this.mDevices.isEmpty()) {
            wrapUpAndFinish();
        } else if (this.mProcessedDeviceCount == this.mDevices.size()) {
            this.mProcessedDeviceCount = 0;
            switch (this.mState) {
                case 2:
                    startOsdNameStage();
                    return;
                case 3:
                    startVendorIdStage();
                    return;
                case 4:
                    wrapUpAndFinish();
                    return;
                default:
                    return;
            }
        } else {
            sendQueryCommand();
        }
    }

    private void sendQueryCommand() {
        int address = this.mDevices.get(this.mProcessedDeviceCount).mLogicalAddress;
        switch (this.mState) {
            case 2:
                queryPhysicalAddress(address);
                return;
            case 3:
                queryOsdName(address);
                return;
            case 4:
                queryVendorId(address);
                break;
        }
    }

    /* access modifiers changed from: package-private */
    public void handleTimerEvent(int state) {
        if (this.mState != 0 && this.mState == state) {
            int i = this.mTimeoutRetry + 1;
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
