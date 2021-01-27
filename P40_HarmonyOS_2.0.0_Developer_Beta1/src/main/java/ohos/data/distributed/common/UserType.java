package ohos.data.distributed.common;

public enum UserType {
    SAME_USER_ID(0);
    
    private int type;

    private UserType(int i) {
        this.type = i;
    }

    public int getType() {
        return this.type;
    }
}
