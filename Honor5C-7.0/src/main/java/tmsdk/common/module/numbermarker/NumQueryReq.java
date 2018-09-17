package tmsdk.common.module.numbermarker;

/* compiled from: Unknown */
public class NumQueryReq {
    public static final int TYPE_Called = 18;
    public static final int TYPE_Calling = 17;
    public static final int TYPE_Common = 16;
    private String CU;
    private int mType;

    public NumQueryReq(String str, int i) {
        this.CU = "";
        this.mType = -1;
        this.CU = str;
        this.mType = i;
    }

    public String getNumber() {
        return this.CU;
    }

    public int getType() {
        return this.mType;
    }
}
