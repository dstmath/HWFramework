package com.huawei.hwextdevice.devices;

import android.util.Log;

public class HWExtMotion implements IHWExtDevice {
    private static final int ATTRIBUTES_MAX_COUNT = 1;
    public static final int ATTRIBUTE_WAKE_UP = 1;
    public static final int DEFAULT_MOTION_ACTION = -1;
    private static final int DEFAULT_MOTION_DELAY = 0;
    private static final String DEVICE_NAME = "MotionDetectionDevice";
    private static int DEVICE_TYPE = 1;
    private static final int VALUES_MAX_LEN = 500;
    private int mHWExtDeviceAction;
    private int[] mHWExtDeviceAttributes;
    private int mHWExtDeviceAttributesCount;
    private int mHWExtDeviceDelay;
    private int mHWExtSubDeviceType;

    public static class MotionType {
        public static final int TYPE_ACTIVITY = 900;
        public static final int TYPE_COUNT = 11;
        public static final int TYPE_FLIP = 200;
        public static final int TYPE_HEAD_DOWN = 1300;
        public static final int TYPE_HW_STEP_COUNTER = 1100;
        public static final int TYPE_MOTOR_CONTRL = 3100;
        public static final int TYPE_MOVE = 1500;
        public static final int TYPE_PICKUP = 100;
        public static final int TYPE_POCKET = 800;
        public static final int TYPE_PROXIMITY_EAR = 300;
        public static final int TYPE_ROTATION = 700;
        public static final int TYPE_SENSORHUB_LOG = 1200;
        public static final int TYPE_SHAKE = 400;
        public static final int TYPE_TAKE_OFF = 1000;
        public static final int TYPE_TAP_BACK = 500;
        public static final int TYPE_TILT_LEFTRIGHT = 600;
    }

    public HWExtMotion() {
        this.mHWExtSubDeviceType = 0;
        this.mHWExtDeviceAction = -1;
        this.mHWExtDeviceDelay = 0;
        this.mHWExtDeviceAttributes = new int[1];
        this.mHWExtDeviceAttributesCount = 0;
        this.mHWExtSubDeviceType = DEVICE_TYPE;
    }

    public HWExtMotion(int motionType) {
        this.mHWExtSubDeviceType = 0;
        this.mHWExtDeviceAction = -1;
        this.mHWExtDeviceDelay = 0;
        this.mHWExtDeviceAttributes = new int[1];
        this.mHWExtDeviceAttributesCount = 0;
        this.mHWExtSubDeviceType = motionType;
    }

    public HWExtMotion(int motionType, int action) {
        this.mHWExtSubDeviceType = 0;
        this.mHWExtDeviceAction = -1;
        this.mHWExtDeviceDelay = 0;
        this.mHWExtDeviceAttributes = new int[1];
        this.mHWExtDeviceAttributesCount = 0;
        this.mHWExtSubDeviceType = motionType;
        this.mHWExtDeviceAction = action;
    }

    public HWExtMotion(int motionType, int action, int delay) {
        this.mHWExtSubDeviceType = 0;
        this.mHWExtDeviceAction = -1;
        this.mHWExtDeviceDelay = 0;
        this.mHWExtDeviceAttributes = new int[1];
        this.mHWExtDeviceAttributesCount = 0;
        this.mHWExtSubDeviceType = motionType;
        this.mHWExtDeviceAction = action;
        this.mHWExtDeviceDelay = delay;
    }

    @Override // com.huawei.hwextdevice.devices.IHWExtDevice
    public int getHWExtDeviceType() {
        return DEVICE_TYPE;
    }

    @Override // com.huawei.hwextdevice.devices.IHWExtDevice
    public void setHWExtDeviceSubType(int deviceSubType) {
        this.mHWExtSubDeviceType = deviceSubType;
    }

    @Override // com.huawei.hwextdevice.devices.IHWExtDevice
    public int getHWExtDeviceSubType() {
        return this.mHWExtSubDeviceType;
    }

    @Override // com.huawei.hwextdevice.devices.IHWExtDevice
    public void setHWExtDeviceAction(int action) {
        this.mHWExtDeviceAction = action;
    }

    @Override // com.huawei.hwextdevice.devices.IHWExtDevice
    public int getHWExtDeviceAction() {
        return (this.mHWExtDeviceAction << 16) + this.mHWExtDeviceDelay;
    }

    @Override // com.huawei.hwextdevice.devices.IHWExtDevice
    public void setHWExtDeviceDelay(int delay) {
        this.mHWExtDeviceDelay = delay;
    }

    @Override // com.huawei.hwextdevice.devices.IHWExtDevice
    public String getHWExtDeviceName() {
        return DEVICE_NAME;
    }

    @Override // com.huawei.hwextdevice.devices.IHWExtDevice
    public void setHWExtDeviceAttribute(int deviceAttribute) {
        int i;
        if (isAttributeSupported(deviceAttribute) && !isAttributeContanis(deviceAttribute) && (i = this.mHWExtDeviceAttributesCount) < 1) {
            this.mHWExtDeviceAttributes[i] = deviceAttribute;
            this.mHWExtDeviceAttributesCount = i + 1;
        }
    }

    @Override // com.huawei.hwextdevice.devices.IHWExtDevice
    public int[] getHWExtDeviceAttributes() {
        return (int[]) this.mHWExtDeviceAttributes.clone();
    }

    @Override // com.huawei.hwextdevice.devices.IHWExtDevice
    public int getHWExtDeviceAttributesCount() {
        return this.mHWExtDeviceAttributesCount;
    }

    private boolean isAttributeSupported(int deviceAttribute) {
        if (deviceAttribute <= 1) {
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

    @Override // com.huawei.hwextdevice.devices.IHWExtDevice
    public int getMaxLenValueArray() {
        return 500;
    }

    @Override // com.huawei.hwextdevice.devices.IHWExtDevice
    public IHWExtDevice cloneDeep() {
        return (IHWExtDevice) clone();
    }

    /* access modifiers changed from: protected */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            Log.e("HWExtMotion", "clone not support");
            return null;
        }
    }
}
