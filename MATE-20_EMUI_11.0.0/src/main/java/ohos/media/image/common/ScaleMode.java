package ohos.media.image.common;

public enum ScaleMode {
    FIT_TARGET_SIZE(0),
    CENTER_CROP(1);
    
    private final int modeValue;

    private ScaleMode(int i) {
        this.modeValue = i;
    }

    public int getValue() {
        return this.modeValue;
    }
}
