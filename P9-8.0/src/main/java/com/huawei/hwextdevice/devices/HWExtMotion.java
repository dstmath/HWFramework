package com.huawei.hwextdevice.devices;

import android.util.Log;

public class HWExtMotion implements IHWExtDevice {
    private static int ATTRIBUTES_MAX_COUNT = 1;
    public static int ATTRIBUTE_WAKE_UP = 1;
    private static final String DEVICE_NAME = "MotionDetectionDevice";
    private static int DEVICE_TYPE = 1;
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
        public static final int TYPE_SENSORHUB_LOG = 1200;
        public static final int TYPE_SHAKE = 400;
        public static final int TYPE_TAKE_OFF = 1000;
        public static final int TYPE_TAP_BACK = 500;
        public static final int TYPE_TILT_LEFTRIGHT = 600;
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
        return 500;
    }

    public IHWExtDevice cloneDeep() {
        return (IHWExtDevice) clone();
    }

    protected Object clone() {
        Object object = null;
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            Log.e("HWExtMotion", "clone not support");
            return object;
        }
    }
}
