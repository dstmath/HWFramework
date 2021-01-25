package ohos.media.image.common;

public enum AlphaType {
    UNKNOWN(0),
    OPAQUE(1),
    PREMUL(2),
    UNPREMUL(3);
    
    private final int typeValue;

    private AlphaType(int i) {
        this.typeValue = i;
    }

    public int getValue() {
        return this.typeValue;
    }
}
