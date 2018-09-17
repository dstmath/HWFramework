package tmsdkobf;

import java.io.Serializable;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public final class ee implements Serializable {
    static final /* synthetic */ boolean bF = (!ee.class.desiredAssertionStatus());
    private static ee[] jD = new ee[20];
    public static final ee jE = new ee(0, 0, "ESP_NONE");
    public static final ee jF = new ee(1, SmsCheckResult.ESCT_NORMAL, "ESP_Symbian_V3");
    public static final ee jG = new ee(2, SmsCheckResult.ESCT_PAY, "ESP_Symbian_V5");
    public static final ee jH = new ee(3, 103, "ESP_Symbian_V2");
    public static final ee jI = new ee(4, 104, "ESP_Symbian_3");
    public static final ee jJ = new ee(5, SmsCheckResult.ESCT_201, "ESP_Android_General");
    public static final ee jK = new ee(6, SmsCheckResult.ESCT_202, "ESP_Android_Pad");
    public static final ee jL = new ee(7, SmsCheckResult.ESCT_203, "ESP_Android_HD");
    public static final ee jM = new ee(8, SmsCheckResult.ESCT_301, "ESP_Iphone_General");
    public static final ee jN = new ee(9, SmsCheckResult.ESCT_302, "ESP_Ipad");
    public static final ee jO = new ee(10, SmsCheckResult.ESCT_303, "ESP_Ipod");
    public static final ee jP = new ee(11, 401, "ESP_Kjava_General");
    public static final ee jQ = new ee(12, 402, "ESP_NK_Kjava_General");
    public static final ee jR = new ee(13, 501, "ESP_Server_General");
    public static final ee jS = new ee(14, 601, "ESP_WinPhone_General");
    public static final ee jT = new ee(15, 602, "ESP_WinPhone_Tablet");
    public static final ee jU = new ee(16, 701, "ESP_MTK_General");
    public static final ee jV = new ee(17, 801, "ESP_BB_General");
    public static final ee jW = new ee(18, 901, "ESP_PC_WindowsGeneral");
    public static final ee jX = new ee(19, 902, "ESP_END");
    private int iF;
    private String iG = new String();

    private ee(int i, int i2, String str) {
        this.iG = str;
        this.iF = i2;
        jD[i] = this;
    }

    public String toString() {
        return this.iG;
    }

    public int value() {
        return this.iF;
    }
}
