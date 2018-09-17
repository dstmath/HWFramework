package com.huawei.hwextdevice.devices;

public interface IHWExtDevice {
    IHWExtDevice cloneDeep();

    int[] getHWExtDeviceAttributes();

    int getHWExtDeviceAttributesCount();

    String getHWExtDeviceName();

    int getHWExtDeviceSubType();

    int getHWExtDeviceType();

    int getMaxLenValueArray();

    void setHWExtDeviceAttribute(int i);

    void setHWExtDeviceSubType(int i);
}
