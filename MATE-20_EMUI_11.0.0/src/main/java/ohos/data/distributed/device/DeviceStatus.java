package ohos.data.distributed.device;

public enum DeviceStatus {
    ONLINE(0),
    OFFLINE(1);
    
    private int type;

    private DeviceStatus(int i) {
        this.type = i;
    }

    public int getType() {
        return this.type;
    }
}
