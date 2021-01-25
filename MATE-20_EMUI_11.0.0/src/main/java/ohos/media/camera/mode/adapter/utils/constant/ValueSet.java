package ohos.media.camera.mode.adapter.utils.constant;

public enum ValueSet {
    MIN(0),
    MAX(1);
    
    private final int value;

    private ValueSet(int i) {
        this.value = i;
    }

    public int getValue() {
        return this.value;
    }
}
