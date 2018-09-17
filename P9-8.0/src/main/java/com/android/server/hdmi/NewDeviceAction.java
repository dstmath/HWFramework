package com.android.server.hdmi;

import android.hardware.hdmi.HdmiDeviceInfo;
import android.net.util.NetworkConstants;
import android.util.Slog;
import com.android.server.power.IHwShutdownThread;
import com.android.server.usb.UsbAudioDevice;
import java.io.UnsupportedEncodingException;

final class NewDeviceAction extends HdmiCecFeatureAction {
    static final int STATE_WAITING_FOR_DEVICE_VENDOR_ID = 2;
    static final int STATE_WAITING_FOR_SET_OSD_NAME = 1;
    private static final String TAG = "NewDeviceAction";
    private final int mDeviceLogicalAddress;
    private final int mDevicePhysicalAddress;
    private final int mDeviceType;
    private String mDisplayName;
    private int mTimeoutRetry;
    private int mVendorId = UsbAudioDevice.kAudioDeviceClassMask;

    NewDeviceAction(HdmiCecLocalDevice source, int deviceLogicalAddress, int devicePhysicalAddress, int deviceType) {
        super(source);
        this.mDeviceLogicalAddress = deviceLogicalAddress;
        this.mDevicePhysicalAddress = devicePhysicalAddress;
        this.mDeviceType = deviceType;
    }

    public boolean start() {
        requestOsdName(true);
        return true;
    }

    private void requestOsdName(boolean firstTry) {
        if (firstTry) {
            this.mTimeoutRetry = 0;
        }
        this.mState = 1;
        if (!mayProcessCommandIfCached(this.mDeviceLogicalAddress, 71)) {
            sendCommand(HdmiCecMessageBuilder.buildGiveOsdNameCommand(getSourceAddress(), this.mDeviceLogicalAddress));
            addTimer(this.mState, IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME);
        }
    }

    public boolean processCommand(HdmiCecMessage cmd) {
        int opcode = cmd.getOpcode();
        int src = cmd.getSource();
        byte[] params = cmd.getParams();
        if (this.mDeviceLogicalAddress != src) {
            return false;
        }
        if (this.mState == 1) {
            if (opcode == 71) {
                try {
                    this.mDisplayName = new String(params, "US-ASCII");
                } catch (UnsupportedEncodingException e) {
                    Slog.e(TAG, "Failed to get OSD name: " + e.getMessage());
                }
                requestVendorId(true);
                return true;
            } else if (opcode == 0 && (params[0] & 255) == 70) {
                requestVendorId(true);
                return true;
            }
        } else if (this.mState == 2) {
            if (opcode == NetworkConstants.ICMPV6_NEIGHBOR_SOLICITATION) {
                this.mVendorId = HdmiUtils.threeBytesToInt(params);
                addDeviceInfo();
                finish();
                return true;
            } else if (opcode == 0 && (params[0] & 255) == 140) {
                addDeviceInfo();
                finish();
                return true;
            }
        }
        return false;
    }

    private boolean mayProcessCommandIfCached(int destAddress, int opcode) {
        HdmiCecMessage message = getCecMessageCache().getMessage(destAddress, opcode);
        if (message != null) {
            return processCommand(message);
        }
        return false;
    }

    private void requestVendorId(boolean firstTry) {
        if (firstTry) {
            this.mTimeoutRetry = 0;
        }
        this.mState = 2;
        if (!mayProcessCommandIfCached(this.mDeviceLogicalAddress, NetworkConstants.ICMPV6_NEIGHBOR_SOLICITATION)) {
            sendCommand(HdmiCecMessageBuilder.buildGiveDeviceVendorIdCommand(getSourceAddress(), this.mDeviceLogicalAddress));
            addTimer(this.mState, IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME);
        }
    }

    private void addDeviceInfo() {
        if (tv().isInDeviceList(this.mDeviceLogicalAddress, this.mDevicePhysicalAddress)) {
            if (this.mDisplayName == null) {
                this.mDisplayName = HdmiUtils.getDefaultDeviceName(this.mDeviceLogicalAddress);
            }
            HdmiDeviceInfo deviceInfo = new HdmiDeviceInfo(this.mDeviceLogicalAddress, this.mDevicePhysicalAddress, tv().getPortId(this.mDevicePhysicalAddress), this.mDeviceType, this.mVendorId, this.mDisplayName);
            tv().addCecDevice(deviceInfo);
            tv().processDelayedMessages(this.mDeviceLogicalAddress);
            if (HdmiUtils.getTypeFromAddress(this.mDeviceLogicalAddress) == 5) {
                tv().onNewAvrAdded(deviceInfo);
            }
            return;
        }
        Slog.w(TAG, String.format("Device not found (%02x, %04x)", new Object[]{Integer.valueOf(this.mDeviceLogicalAddress), Integer.valueOf(this.mDevicePhysicalAddress)}));
    }

    public void handleTimerEvent(int state) {
        if (this.mState != 0 && this.mState == state) {
            int i;
            if (state == 1) {
                i = this.mTimeoutRetry + 1;
                this.mTimeoutRetry = i;
                if (i < 5) {
                    requestOsdName(false);
                    return;
                }
                requestVendorId(true);
            } else if (state == 2) {
                i = this.mTimeoutRetry + 1;
                this.mTimeoutRetry = i;
                if (i < 5) {
                    requestVendorId(false);
                } else {
                    addDeviceInfo();
                    finish();
                }
            }
        }
    }

    boolean isActionOf(ActiveSource activeSource) {
        if (this.mDeviceLogicalAddress == activeSource.logicalAddress && this.mDevicePhysicalAddress == activeSource.physicalAddress) {
            return true;
        }
        return false;
    }
}
