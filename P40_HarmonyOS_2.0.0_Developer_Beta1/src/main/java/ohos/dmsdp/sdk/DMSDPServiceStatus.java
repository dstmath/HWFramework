package ohos.dmsdp.sdk;

public enum DMSDPServiceStatus {
    PLUGIN(1),
    UNPLUG(2),
    AVAILABLE(3),
    UNAVAILABLE(4);
    
    private int value;

    private DMSDPServiceStatus(int i) {
        this.value = i;
    }

    public int getValue() {
        return this.value;
    }

    public static DMSDPServiceStatus fromValue(int i) {
        DMSDPServiceStatus[] values = values();
        for (DMSDPServiceStatus dMSDPServiceStatus : values) {
            if (dMSDPServiceStatus.value == i) {
                return dMSDPServiceStatus;
            }
        }
        return null;
    }
}
