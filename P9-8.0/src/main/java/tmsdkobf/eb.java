package tmsdkobf;

import java.io.Serializable;

public final class eb implements Serializable {
    static final /* synthetic */ boolean bF = (!eb.class.desiredAssertionStatus());
    private static eb[] iE = new eb[6];
    public static final eb iH = new eb(0, 0, "CT_NONE");
    public static final eb iI = new eb(1, 1, "CT_GPRS");
    public static final eb iJ = new eb(2, 2, "CT_WIFI");
    public static final eb iK = new eb(3, 3, "CT_GPRS_WAP");
    public static final eb iL = new eb(4, 4, "CT_GPRS_NET");
    public static final eb iM = new eb(5, 5, "CT_3G_NET");
    private int iF;
    private String iG = new String();

    private eb(int i, int i2, String str) {
        this.iG = str;
        this.iF = i2;
        iE[i] = this;
    }

    public String toString() {
        return this.iG;
    }

    public int value() {
        return this.iF;
    }
}
