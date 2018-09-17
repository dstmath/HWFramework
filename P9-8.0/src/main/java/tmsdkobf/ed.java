package tmsdkobf;

import java.io.Serializable;

public final class ed implements Serializable {
    static final /* synthetic */ boolean bF = (!ed.class.desiredAssertionStatus());
    private static ed[] iS = new ed[36];
    public static final ed iT = new ed(0, 0, "EP_None");
    public static final ed iU = new ed(1, 1, "EP_Secure");
    public static final ed iV = new ed(2, 2, "EP_Phonebook");
    public static final ed iW = new ed(3, 3, "EP_Pim");
    public static final ed iX = new ed(4, 4, "EP_QQPhonebook");
    public static final ed iY = new ed(5, 5, "EP_QZone");
    public static final ed iZ = new ed(6, 6, "EP_MobileQQ_Secure");
    public static final ed jA = new ed(33, 35, "EP_Secure_Mini");
    public static final ed jB = new ed(34, 46, "EP_TMSVirusSDK_Eng");
    public static final ed jC = new ed(35, 37, "EP_END");
    public static final ed ja = new ed(7, 7, "EP_QQBrowse_Secure");
    public static final ed jb = new ed(8, 8, "EP_XiaoYou");
    public static final ed jc = new ed(9, 9, "EP_Secure_Eng");
    public static final ed jd = new ed(10, 10, "EP_WBlog");
    public static final ed je = new ed(11, 11, "EP_Phonebook_Eng");
    public static final ed jf = new ed(12, 12, "EP_AppAssistant");
    public static final ed jg = new ed(13, 13, "EP_Secure_SDK");
    public static final ed jh = new ed(14, 14, "EP_KingRoot");
    public static final ed ji = new ed(15, 15, "EP_Secure_SDK_Pay");
    public static final ed jj = new ed(16, 16, "EP_Secure_Jailbreak");
    public static final ed jk = new ed(17, 17, "EP_KingUser");
    public static final ed jl = new ed(18, 18, "EP_Pim_Pro");
    public static final ed jm = new ed(19, 19, "EP_Pim_Jailbreak");
    public static final ed jn = new ed(20, 20, "EP_PhonebookPro");
    public static final ed jo = new ed(21, 21, "EP_PowerManager");
    public static final ed jp = new ed(22, 22, "EP_BenchMark");
    public static final ed jq = new ed(23, 23, "EP_SecurePro_Enhance");
    public static final ed jr = new ed(24, 24, "EP_Pim_Eng");
    public static final ed js = new ed(25, 25, "EP_SMS_Fraud_Killer");
    public static final ed jt = new ed(26, 26, "EP_King_SuperUser");
    public static final ed ju = new ed(27, 27, "EP_Secure_SDK_Ign");
    public static final ed jv = new ed(28, 28, "EP_Tracker");
    public static final ed jw = new ed(29, 29, "EP_TencentUser");
    public static final ed jx = new ed(30, 30, "EP_Album");
    public static final ed jy = new ed(31, 31, "EP_WeShare");
    public static final ed jz = new ed(32, 32, "EP_Tencent_Cleaner");
    private int iF;
    private String iG = new String();

    private ed(int i, int i2, String str) {
        this.iG = str;
        this.iF = i2;
        iS[i] = this;
    }

    public String toString() {
        return this.iG;
    }

    public int value() {
        return this.iF;
    }
}
