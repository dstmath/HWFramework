package ohos.media.image.common;

public enum MemoryUsagePreference {
    DEFAULT(0),
    LOW_RAM(1);
    
    private final int memoryUsagePreference;

    private MemoryUsagePreference(int i) {
        this.memoryUsagePreference = i;
    }

    public int getValue() {
        return this.memoryUsagePreference;
    }
}
