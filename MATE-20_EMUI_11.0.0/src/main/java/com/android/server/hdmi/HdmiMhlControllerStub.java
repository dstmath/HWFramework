package com.android.server.hdmi;

import android.hardware.hdmi.HdmiPortInfo;
import android.util.SparseArray;
import com.android.internal.util.IndentingPrintWriter;

/* access modifiers changed from: package-private */
public final class HdmiMhlControllerStub {
    private static final HdmiPortInfo[] EMPTY_PORT_INFO = new HdmiPortInfo[0];
    private static final int INVALID_DEVICE_ROLES = 0;
    private static final int INVALID_MHL_VERSION = 0;
    private static final int NO_SUPPORTED_FEATURES = 0;
    private static final SparseArray<HdmiMhlLocalDeviceStub> mLocalDevices = new SparseArray<>();

    private HdmiMhlControllerStub(HdmiControlService service) {
    }

    /* access modifiers changed from: package-private */
    public boolean isReady() {
        return false;
    }

    static HdmiMhlControllerStub create(HdmiControlService service) {
        return new HdmiMhlControllerStub(service);
    }

    /* access modifiers changed from: package-private */
    public HdmiPortInfo[] getPortInfos() {
        return EMPTY_PORT_INFO;
    }

    /* access modifiers changed from: package-private */
    public HdmiMhlLocalDeviceStub getLocalDevice(int portId) {
        return null;
    }

    /* access modifiers changed from: package-private */
    public HdmiMhlLocalDeviceStub getLocalDeviceById(int deviceId) {
        return null;
    }

    /* access modifiers changed from: package-private */
    public SparseArray<HdmiMhlLocalDeviceStub> getAllLocalDevices() {
        return mLocalDevices;
    }

    /* access modifiers changed from: package-private */
    public HdmiMhlLocalDeviceStub removeLocalDevice(int portId) {
        return null;
    }

    /* access modifiers changed from: package-private */
    public HdmiMhlLocalDeviceStub addLocalDevice(HdmiMhlLocalDeviceStub device) {
        return null;
    }

    /* access modifiers changed from: package-private */
    public void clearAllLocalDevices() {
    }

    /* access modifiers changed from: package-private */
    public void sendVendorCommand(int portId, int offset, int length, byte[] data) {
    }

    /* access modifiers changed from: package-private */
    public void setOption(int flag, int value) {
    }

    /* access modifiers changed from: package-private */
    public int getMhlVersion(int portId) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int getPeerMhlVersion(int portId) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int getSupportedFeatures(int portId) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int getEcbusDeviceRoles(int portId) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void dump(IndentingPrintWriter pw) {
    }
}
