package ohos.data.distributed.common;

public enum SyncMode {
    PULL_ONLY(0),
    PUSH_ONLY(1),
    PUSH_PULL(2);
    
    private int value;

    private SyncMode(int i) {
        this.value = i;
    }

    public int getValue() {
        return this.value;
    }
}
