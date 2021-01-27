package ohos.media.image.common;

public enum AllocatorType {
    DEFAULT(0),
    HEAP(1),
    SHARED_MEMORY(2);
    
    private final int typeValue;

    private AllocatorType(int i) {
        this.typeValue = i;
    }

    public int getValue() {
        return this.typeValue;
    }
}
