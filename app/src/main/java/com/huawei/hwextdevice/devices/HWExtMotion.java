package com.huawei.hwextdevice.devices;

import android.util.Log;

public class HWExtMotion implements IHWExtDevice {
    private static int ATTRIBUTES_MAX_COUNT = 0;
    public static int ATTRIBUTE_WAKE_UP = 0;
    private static final String DEVICE_NAME = "MotionDetectionDevice";
    private static int DEVICE_TYPE = 0;
    private static final int VALUES_MAX_LEN = 500;
    private int[] mHWExtDeviceAttributes;
    private int mHWExtDeviceAttributesCount;
    private int mHWExtSubDeviceType;

    public static class MotionType {
        public static final int TYPE_ACTIVITY = 900;
        public static final int TYPE_COUNT = 11;
        public static final int TYPE_FLIP = 200;
        public static final int TYPE_HW_STEP_COUNTER = 1100;
        public static final int TYPE_PICKUP = 100;
        public static final int TYPE_POCKET = 800;
        public static final int TYPE_PROXIMITY_EAR = 300;
        public static final int TYPE_ROTATION = 700;
        public static final int TYPE_SHAKE = 400;
        public static final int TYPE_TAKE_OFF = 1000;
        public static final int TYPE_TAP_BACK = 500;
        public static final int TYPE_TILT_LEFTRIGHT = 600;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.hwextdevice.devices.HWExtMotion.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.hwextdevice.devices.HWExtMotion.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.hwextdevice.devices.HWExtMotion.<clinit>():void");
    }

    public HWExtMotion() {
        this.mHWExtSubDeviceType = 0;
        this.mHWExtDeviceAttributes = new int[ATTRIBUTES_MAX_COUNT];
        this.mHWExtDeviceAttributesCount = 0;
        this.mHWExtSubDeviceType = DEVICE_TYPE;
    }

    public HWExtMotion(int motionType) {
        this.mHWExtSubDeviceType = 0;
        this.mHWExtDeviceAttributes = new int[ATTRIBUTES_MAX_COUNT];
        this.mHWExtDeviceAttributesCount = 0;
        this.mHWExtSubDeviceType = motionType;
    }

    public int getHWExtDeviceType() {
        return DEVICE_TYPE;
    }

    public void setHWExtDeviceSubType(int deviceSubType) {
        this.mHWExtSubDeviceType = deviceSubType;
    }

    public int getHWExtDeviceSubType() {
        return this.mHWExtSubDeviceType;
    }

    public String getHWExtDeviceName() {
        return DEVICE_NAME;
    }

    public void setHWExtDeviceAttribute(int deviceAttribute) {
        if (isAttributeSupported(deviceAttribute) && !isAttributeContanis(deviceAttribute) && this.mHWExtDeviceAttributesCount < ATTRIBUTES_MAX_COUNT) {
            this.mHWExtDeviceAttributes[this.mHWExtDeviceAttributesCount] = deviceAttribute;
            this.mHWExtDeviceAttributesCount++;
        }
    }

    public int[] getHWExtDeviceAttributes() {
        return (int[]) this.mHWExtDeviceAttributes.clone();
    }

    public int getHWExtDeviceAttributesCount() {
        return this.mHWExtDeviceAttributesCount;
    }

    private boolean isAttributeSupported(int deviceAttribute) {
        if (deviceAttribute <= ATTRIBUTES_MAX_COUNT) {
            return true;
        }
        return false;
    }

    private boolean isAttributeContanis(int deviceAttribute) {
        for (int i = 0; i < this.mHWExtDeviceAttributesCount; i++) {
            if (this.mHWExtDeviceAttributes[i] == deviceAttribute) {
                return true;
            }
        }
        return false;
    }

    public int getMaxLenValueArray() {
        return VALUES_MAX_LEN;
    }

    public IHWExtDevice cloneDeep() {
        return (IHWExtDevice) clone();
    }

    protected Object clone() {
        Object object = null;
        try {
            object = super.clone();
        } catch (CloneNotSupportedException e) {
            Log.e("HWExtMotion", "clone not support");
        }
        return object;
    }
}
