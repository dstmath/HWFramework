package ohos.data.distributed.common;

public enum SubscribeType {
    SUBSCRIBE_TYPE_LOCAL(0),
    SUBSCRIBE_TYPE_REMOTE(1),
    SUBSCRIBE_TYPE_ALL(2);
    
    private int type;

    private SubscribeType(int i) {
        this.type = i;
    }

    public int getType() {
        return this.type;
    }
}
