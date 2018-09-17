package tmsdk.common.module.optimus;

public enum BsFakeType {
    UNKNOW(0),
    SAFE(1),
    RIST(2),
    FAKE(3);
    
    public final int mValue;

    private BsFakeType(int i) {
        this.mValue = i;
    }
}
