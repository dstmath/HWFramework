package com.android.server.hdmi;

import android.hardware.hdmi.HdmiPortInfo;
import android.util.SparseArray;
import com.android.internal.util.IndentingPrintWriter;

final class HdmiMhlControllerStub {
    private static final HdmiPortInfo[] EMPTY_PORT_INFO = new HdmiPortInfo[0];
    private static final int INVALID_DEVICE_ROLES = 0;
    private static final int INVALID_MHL_VERSION = 0;
    private static final int NO_SUPPORTED_FEATURES = 0;
    private static final SparseArray<HdmiMhlLocalDeviceStub> mLocalDevices = new SparseArray();

    private HdmiMhlControllerStub(HdmiControlService service) {
    }

    boolean isReady() {
        return false;
    }

    static HdmiMhlControllerStub create(HdmiControlService service) {
        return new HdmiMhlControllerStub(service);
    }

    HdmiPortInfo[] getPortInfos() {
        return EMPTY_PORT_INFO;
    }

    HdmiMhlLocalDeviceStub getLocalDevice(int portId) {
        return null;
    }

    HdmiMhlLocalDeviceStub getLocalDeviceById(int deviceId) {
        return null;
    }

    SparseArray<HdmiMhlLocalDeviceStub> getAllLocalDevices() {
        return mLocalDevices;
    }

    HdmiMhlLocalDeviceStub removeLocalDevice(int portId) {
        return null;
    }

    HdmiMhlLocalDeviceStub addLocalDevice(HdmiMhlLocalDeviceStub device) {
        return null;
    }

    void clearAllLocalDevices() {
    }

    void sendVendorCommand(int portId, int offset, int length, byte[] data) {
    }

    void setOption(int flag, int value) {
    }

    int getMhlVersion(int portId) {
        return 0;
    }

    int getPeerMhlVersion(int portId) {
        return 0;
    }

    int getSupportedFeatures(int portId) {
        return 0;
    }

    int getEcbusDeviceRoles(int portId) {
        return 0;
    }

    void dump(IndentingPrintWriter pw) {
    }
}
