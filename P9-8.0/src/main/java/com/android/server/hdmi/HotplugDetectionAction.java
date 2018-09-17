package com.android.server.hdmi;

import android.hardware.hdmi.HdmiDeviceInfo;
import android.util.Slog;
import java.util.BitSet;
import java.util.List;

final class HotplugDetectionAction extends HdmiCecFeatureAction {
    private static final int AVR_COUNT_MAX = 3;
    private static final int NUM_OF_ADDRESS = 15;
    private static final int POLLING_INTERVAL_MS = 5000;
    private static final int STATE_WAIT_FOR_NEXT_POLLING = 1;
    private static final String TAG = "HotPlugDetectionAction";
    private static final int TIMEOUT_COUNT = 3;
    private int mAvrStatusCount = 0;
    private int mTimeoutCount = 0;

    HotplugDetectionAction(HdmiCecLocalDevice source) {
        super(source);
    }

    boolean start() {
        Slog.v(TAG, "Hot-plug dection started.");
        this.mState = 1;
        this.mTimeoutCount = 0;
        addTimer(this.mState, POLLING_INTERVAL_MS);
        return true;
    }

    boolean processCommand(HdmiCecMessage cmd) {
        return false;
    }

    void handleTimerEvent(int state) {
        if (this.mState == state && this.mState == 1) {
            this.mTimeoutCount = (this.mTimeoutCount + 1) % 3;
            pollDevices();
        }
    }

    void pollAllDevicesNow() {
        this.mActionTimer.clearTimerMessage();
        this.mTimeoutCount = 0;
        this.mState = 1;
        pollAllDevices();
        addTimer(this.mState, POLLING_INTERVAL_MS);
    }

    private void pollDevices() {
        if (this.mTimeoutCount == 0) {
            pollAllDevices();
        } else if (tv().isSystemAudioActivated()) {
            pollAudioSystem();
        }
        addTimer(this.mState, POLLING_INTERVAL_MS);
    }

    private void pollAllDevices() {
        Slog.v(TAG, "Poll all devices.");
        pollDevices(new DevicePollingCallback() {
            public void onPollingFinished(List<Integer> ackedAddress) {
                HotplugDetectionAction.this.checkHotplug(ackedAddress, false);
            }
        }, 65537, 1);
    }

    private void pollAudioSystem() {
        Slog.v(TAG, "Poll audio system.");
        pollDevices(new DevicePollingCallback() {
            public void onPollingFinished(List<Integer> ackedAddress) {
                HotplugDetectionAction.this.checkHotplug(ackedAddress, true);
            }
        }, 65538, 1);
    }

    private void checkHotplug(List<Integer> ackedAddress, boolean audioOnly) {
        BitSet currentInfos = infoListToBitSet(tv().getDeviceInfoList(false), audioOnly);
        BitSet polledResult = addressListToBitSet(ackedAddress);
        BitSet removed = complement(currentInfos, polledResult);
        int index = -1;
        while (true) {
            index = removed.nextSetBit(index + 1);
            if (index == -1) {
                break;
            }
            if (index == 5) {
                HdmiDeviceInfo avr = tv().getAvrDeviceInfo();
                if (avr != null && tv().isConnected(avr.getPortId())) {
                    this.mAvrStatusCount++;
                    Slog.w(TAG, "Ack not returned from AVR. count: " + this.mAvrStatusCount);
                    if (this.mAvrStatusCount < 3) {
                    }
                }
            }
            Slog.v(TAG, "Remove device by hot-plug detection:" + index);
            removeDevice(index);
        }
        if (!removed.get(5)) {
            this.mAvrStatusCount = 0;
        }
        BitSet added = complement(polledResult, currentInfos);
        index = -1;
        while (true) {
            index = added.nextSetBit(index + 1);
            if (index != -1) {
                Slog.v(TAG, "Add device by hot-plug detection:" + index);
                addDevice(index);
            } else {
                return;
            }
        }
    }

    private static BitSet infoListToBitSet(List<HdmiDeviceInfo> infoList, boolean audioOnly) {
        BitSet set = new BitSet(15);
        for (HdmiDeviceInfo info : infoList) {
            if (!audioOnly) {
                set.set(info.getLogicalAddress());
            } else if (info.getDeviceType() == 5) {
                set.set(info.getLogicalAddress());
            }
        }
        return set;
    }

    private static BitSet addressListToBitSet(List<Integer> list) {
        BitSet set = new BitSet(15);
        for (Integer value : list) {
            set.set(value.intValue());
        }
        return set;
    }

    private static BitSet complement(BitSet first, BitSet second) {
        BitSet clone = (BitSet) first.clone();
        clone.andNot(second);
        return clone;
    }

    private void addDevice(int addedAddress) {
        sendCommand(HdmiCecMessageBuilder.buildGivePhysicalAddress(getSourceAddress(), addedAddress));
    }

    private void removeDevice(int removedAddress) {
        mayChangeRoutingPath(removedAddress);
        mayCancelDeviceSelect(removedAddress);
        mayCancelOneTouchRecord(removedAddress);
        mayDisableSystemAudioAndARC(removedAddress);
        tv().removeCecDevice(removedAddress);
    }

    private void mayChangeRoutingPath(int address) {
        HdmiDeviceInfo info = tv().getCecDeviceInfo(address);
        if (info != null) {
            tv().handleRemoveActiveRoutingPath(info.getPhysicalAddress());
        }
    }

    private void mayCancelDeviceSelect(int address) {
        List<DeviceSelectAction> actions = getActions(DeviceSelectAction.class);
        if (!actions.isEmpty() && ((DeviceSelectAction) actions.get(0)).getTargetAddress() == address) {
            removeAction(DeviceSelectAction.class);
        }
    }

    private void mayCancelOneTouchRecord(int address) {
        for (OneTouchRecordAction action : getActions(OneTouchRecordAction.class)) {
            if (action.getRecorderAddress() == address) {
                removeAction((HdmiCecFeatureAction) action);
            }
        }
    }

    private void mayDisableSystemAudioAndARC(int address) {
        if (HdmiUtils.getTypeFromAddress(address) == 5) {
            tv().setSystemAudioMode(false);
            if (tv().isArcEstablished()) {
                tv().enableAudioReturnChannel(false);
                addAndStartAction(new RequestArcTerminationAction(localDevice(), address));
            }
        }
    }
}
