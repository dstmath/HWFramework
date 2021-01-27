package ohos.data.distributed.device;

public enum DeviceFilterStrategy {
    FILTER(0),
    NO_FILTER(1);
    
    private int strategy;

    private DeviceFilterStrategy(int i) {
        this.strategy = i;
    }

    public int getStrategy() {
        return this.strategy;
    }
}
