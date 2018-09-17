package tmsdkobf;

import java.io.Serializable;

public final class fa implements Serializable {
    static final /* synthetic */ boolean bF = (!fa.class.desiredAssertionStatus());
    private static fa[] lf = new fa[17];
    public static final fa lg = new fa(0, 0, "CHECK_REGULAR");
    public static final fa lh = new fa(1, 1, "CHECK_COCKHORSE");
    public static final fa li = new fa(2, 2, "CHECK_DEFAULT_CHEAT");
    public static final fa lj = new fa(3, 3, "CHECK_MONEY_CHEAT");
    public static final fa lk = new fa(4, 4, "CHECK_SP_SERVICE");
    public static final fa ll = new fa(5, 5, "CHECK_STEAL_ACCOUNT");
    public static final fa lm = new fa(6, 6, "CHECK_TIPS_CHEAT");
    public static final fa ln = new fa(7, 7, "CHECK_TIPS_DEFAULT");
    public static final fa lo = new fa(8, 8, "CHECK_GAMES_HANG");
    public static final fa lp = new fa(9, 9, "CHECK_MAKE_MONEY");
    public static final fa lq = new fa(10, 10, "CHECK_SEX");
    public static final fa lr = new fa(11, 11, "CHECK_PRIVATE_SERVER");
    public static final fa ls = new fa(12, 12, "CHECK_MSG_REACTIONARY");
    public static final fa lt = new fa(13, 13, "CHECK_MSG_WHITE");
    public static final fa lu = new fa(14, 18, "CHECK_MSG_SHADINESS");
    public static final fa lv = new fa(15, 19, "CHECK_MSG_BLOG");
    public static final fa lw = new fa(16, 20, "CHECK_MAX");
    private int iF;
    private String iG = new String();

    private fa(int i, int i2, String str) {
        this.iG = str;
        this.iF = i2;
        lf[i] = this;
    }

    public String toString() {
        return this.iG;
    }

    public int value() {
        return this.iF;
    }
}
