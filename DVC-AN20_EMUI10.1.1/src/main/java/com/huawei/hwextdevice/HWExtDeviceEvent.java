package com.huawei.hwextdevice;

import android.util.Log;
import com.huawei.hwextdevice.devices.IHWExtDevice;

public class HWExtDeviceEvent implements Cloneable {
    public static final int DEFAULT_VALUES_LEN = 500;
    private static final String TAG = "HWExtDeviceEvent";
    private HWExtDeviceData mDeviceData;
    private float[] mDeviceValues;
    private int mDeviceValuesLen;
    private IHWExtDevice mHWExtDevice;
    private int mHWExtDeviceType;
    private int mHWExtSubDeviceType;

    public HWExtDeviceEvent() {
        this(500);
    }

    public HWExtDeviceEvent(IHWExtDevice hwextDevice) {
        this(500);
        this.mHWExtDevice = hwextDevice;
        IHWExtDevice iHWExtDevice = this.mHWExtDevice;
        if (iHWExtDevice != null) {
            this.mHWExtDeviceType = iHWExtDevice.getHWExtDeviceType();
        }
    }

    public HWExtDeviceEvent(int maxLenValueArray) {
        this.mHWExtDevice = null;
        this.mHWExtDeviceType = -1;
        this.mHWExtSubDeviceType = -1;
        this.mDeviceValues = null;
        this.mDeviceValuesLen = 500;
        this.mDeviceData = null;
        this.mDeviceValuesLen = maxLenValueArray;
        this.mDeviceValues = new float[this.mDeviceValuesLen];
    }

    public void setDevice(IHWExtDevice hwextDevice) {
        this.mHWExtDevice = hwextDevice;
    }

    public IHWExtDevice getDevice() {
        return this.mHWExtDevice;
    }

    public void setDeviceType(int deviceType) {
        this.mHWExtDeviceType = deviceType;
    }

    public int getDeviceType() {
        return this.mHWExtDeviceType;
    }

    public void setSubDeviceType(int subDeviceType) {
        this.mHWExtSubDeviceType = subDeviceType;
    }

    public int getSubDeviceType() {
        return this.mHWExtSubDeviceType;
    }

    public void setDeviceValues(float[] deviceValues, int valueLen) {
        if (deviceValues != null && deviceValues.length > 0) {
            float[] fArr = this.mDeviceValues;
            this.mDeviceValuesLen = valueLen > fArr.length ? fArr.length : valueLen;
            System.arraycopy(deviceValues, 0, this.mDeviceValues, 0, this.mDeviceValuesLen);
        }
    }

    public int getDeviceValuesLen() {
        return this.mDeviceValuesLen;
    }

    public float[] getDeviceValues() {
        float[] fArr = this.mDeviceValues;
        if (fArr != null) {
            return (float[]) fArr.clone();
        }
        return null;
    }

    public void setDeviceData(HWExtDeviceData deviceData) {
        if (deviceData != null) {
            this.mDeviceData = deviceData.cloneDeep();
        }
    }

    public HWExtDeviceData getDeviceData() {
        HWExtDeviceData hWExtDeviceData = this.mDeviceData;
        if (hWExtDeviceData != null) {
            return hWExtDeviceData.cloneDeep();
        }
        return null;
    }

    public void dispose() {
        this.mHWExtDevice = null;
        this.mDeviceValues = null;
        this.mHWExtDeviceType = -1;
    }

    @Override // java.lang.Object
    public Object clone() {
        HWExtDeviceEvent deviceEvent = null;
        try {
            deviceEvent = (HWExtDeviceEvent) super.clone();
            deviceEvent.mHWExtDeviceType = this.mHWExtDeviceType;
            deviceEvent.mHWExtSubDeviceType = this.mHWExtSubDeviceType;
            deviceEvent.mDeviceValuesLen = this.mDeviceValuesLen;
            if (this.mDeviceValues != null) {
                deviceEvent.mDeviceValues = (float[]) this.mDeviceValues.clone();
            }
            if (this.mDeviceData != null) {
                deviceEvent.mDeviceData = (HWExtDeviceData) this.mDeviceData.clone();
            }
        } catch (CloneNotSupportedException e) {
            Log.e(TAG, "clone not support");
        }
        return deviceEvent;
    }
}
