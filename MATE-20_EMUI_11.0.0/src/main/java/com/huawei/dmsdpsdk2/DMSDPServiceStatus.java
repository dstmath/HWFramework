package com.huawei.dmsdpsdk2;

public enum DMSDPServiceStatus {
    PLUGIN(1),
    UNPLUG(2),
    AVAILABLE(3),
    UNAVAILABLE(4);
    
    private int value;

    private DMSDPServiceStatus(int value2) {
        this.value = value2;
    }

    public int getValue() {
        return this.value;
    }

    public static DMSDPServiceStatus fromValue(int value2) {
        DMSDPServiceStatus[] values = values();
        for (DMSDPServiceStatus status : values) {
            if (status.value == value2) {
                return status;
            }
        }
        return null;
    }
}
