package tmsdk.common.module.numbermarker;

public class NumQueryReq {
    public static final int TYPE_Called = 18;
    public static final int TYPE_Calling = 17;
    public static final int TYPE_Common = 16;
    private String AH = "";
    private int mType = -1;

    public NumQueryReq(String str, int i) {
        this.AH = str;
        this.mType = i;
    }

    public String getNumber() {
        return this.AH;
    }

    public int getType() {
        return this.mType;
    }
}
