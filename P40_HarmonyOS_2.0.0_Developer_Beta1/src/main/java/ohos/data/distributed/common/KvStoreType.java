package ohos.data.distributed.common;

public enum KvStoreType {
    DEVICE_COLLABORATION(0),
    SINGLE_VERSION(1),
    MULTI_VERSION(2);
    
    private int code;

    private KvStoreType(int i) {
        this.code = i;
    }

    public int getCode() {
        return this.code;
    }
}
