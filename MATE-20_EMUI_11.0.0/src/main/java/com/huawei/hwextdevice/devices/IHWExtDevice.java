package com.huawei.hwextdevice.devices;

public interface IHWExtDevice {
    IHWExtDevice cloneDeep();

    int getHWExtDeviceAction();

    int[] getHWExtDeviceAttributes();

    int getHWExtDeviceAttributesCount();

    String getHWExtDeviceName();

    int getHWExtDeviceSubType();

    int getHWExtDeviceType();

    int getMaxLenValueArray();

    void setHWExtDeviceAction(int i);

    void setHWExtDeviceAttribute(int i);

    void setHWExtDeviceDelay(int i);

    void setHWExtDeviceSubType(int i);
}
