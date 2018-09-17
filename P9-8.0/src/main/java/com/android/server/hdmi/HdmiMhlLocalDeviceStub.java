package com.android.server.hdmi;

import android.hardware.hdmi.HdmiDeviceInfo;
import android.hardware.hdmi.IHdmiControlCallback;
import android.net.util.NetworkConstants;

final class HdmiMhlLocalDeviceStub {
    private static final HdmiDeviceInfo INFO = new HdmiDeviceInfo(NetworkConstants.ARP_HWTYPE_RESERVED_HI, -1, -1, -1);
    private final int mPortId;
    private final HdmiControlService mService;

    protected HdmiMhlLocalDeviceStub(HdmiControlService service, int portId) {
        this.mService = service;
        this.mPortId = portId;
    }

    void onDeviceRemoved() {
    }

    HdmiDeviceInfo getInfo() {
        return INFO;
    }

    void setBusMode(int cbusmode) {
    }

    void onBusOvercurrentDetected(boolean on) {
    }

    void setDeviceStatusChange(int adopterId, int deviceId) {
    }

    int getPortId() {
        return this.mPortId;
    }

    void turnOn(IHdmiControlCallback callback) {
    }

    void sendKeyEvent(int keycode, boolean isPressed) {
    }

    void sendStandby() {
    }
}
