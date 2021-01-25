package com.android.server.hdmi;

import android.hardware.hdmi.HdmiDeviceInfo;
import android.hardware.hdmi.IHdmiControlCallback;

/* access modifiers changed from: package-private */
public final class HdmiMhlLocalDeviceStub {
    private static final HdmiDeviceInfo INFO = new HdmiDeviceInfo(65535, -1, -1, -1);
    private final int mPortId;
    private final HdmiControlService mService;

    protected HdmiMhlLocalDeviceStub(HdmiControlService service, int portId) {
        this.mService = service;
        this.mPortId = portId;
    }

    /* access modifiers changed from: package-private */
    public void onDeviceRemoved() {
    }

    /* access modifiers changed from: package-private */
    public HdmiDeviceInfo getInfo() {
        return INFO;
    }

    /* access modifiers changed from: package-private */
    public void setBusMode(int cbusmode) {
    }

    /* access modifiers changed from: package-private */
    public void onBusOvercurrentDetected(boolean on) {
    }

    /* access modifiers changed from: package-private */
    public void setDeviceStatusChange(int adopterId, int deviceId) {
    }

    /* access modifiers changed from: package-private */
    public int getPortId() {
        return this.mPortId;
    }

    /* access modifiers changed from: package-private */
    public void turnOn(IHdmiControlCallback callback) {
    }

    /* access modifiers changed from: package-private */
    public void sendKeyEvent(int keycode, boolean isPressed) {
    }

    /* access modifiers changed from: package-private */
    public void sendStandby() {
    }
}
