package ohos.idn;

import ohos.annotation.SystemApi;

@SystemApi
public enum DeviceInfoType {
    UNKNOWN_INFO(0),
    BASIC_INFO(1),
    NETWORK_INFO(2),
    TRUST_INFO(3);
    
    private int mValue;

    private DeviceInfoType(int i) {
        this.mValue = i;
    }

    public int toInt() {
        return this.mValue;
    }

    public static DeviceInfoType fromInt(int i) {
        DeviceInfoType[] values = values();
        for (DeviceInfoType deviceInfoType : values) {
            if (deviceInfoType.toInt() == i) {
                return deviceInfoType;
            }
        }
        return UNKNOWN_INFO;
    }
}
