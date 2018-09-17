package com.android.server.hdmi;

import android.hardware.hdmi.HdmiPortInfo;
import android.util.SparseArray;
import com.android.internal.util.IndentingPrintWriter;

final class HdmiMhlControllerStub {
    private static final HdmiPortInfo[] EMPTY_PORT_INFO = null;
    private static final int INVALID_DEVICE_ROLES = 0;
    private static final int INVALID_MHL_VERSION = 0;
    private static final int NO_SUPPORTED_FEATURES = 0;
    private static final SparseArray<HdmiMhlLocalDeviceStub> mLocalDevices = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.hdmi.HdmiMhlControllerStub.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.hdmi.HdmiMhlControllerStub.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.hdmi.HdmiMhlControllerStub.<clinit>():void");
    }

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
