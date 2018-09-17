package tmsdkobf;

public final class ef {
    static final /* synthetic */ boolean bF = (!ef.class.desiredAssertionStatus());
    private static ef[] jY = new ef[6];
    public static final ef jZ = new ef(0, 0, "ETT_MIN");
    public static final ef ka = new ef(1, 1, "ETT_RING_ONE_SOUND");
    public static final ef kb = new ef(2, 2, "ETT_USER_CANCEL");
    public static final ef kc = new ef(3, 3, "ETT_MISS_CALL");
    public static final ef kd = new ef(4, 4, "ETT_USER_HANG_UP");
    public static final ef ke = new ef(5, 5, "ETT_MAX");
    private int iF;
    private String iG = new String();

    private ef(int i, int i2, String str) {
        this.iG = str;
        this.iF = i2;
        jY[i] = this;
    }

    public String toString() {
        return this.iG;
    }

    public int value() {
        return this.iF;
    }
}
