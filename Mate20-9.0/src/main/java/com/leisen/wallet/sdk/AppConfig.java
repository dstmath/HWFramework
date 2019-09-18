package com.leisen.wallet.sdk;

import android.os.Build;

public class AppConfig {
    public static final String APDU_GETCIN = "80CA004500";
    public static final String APDU_GETCPLC = "80CA9f7f00";
    public static final String APDU_GETIIN = "80CA004200";
    public static String CLIENTVERSION = "2.0.6";
    public static String MOBILETYPE = Build.MODEL;
    public static String SN = "deprecated";
    public static final String VERSION = "1.0";
    public int CARDREADER = -1;
    public String STREAMURL = "https://tsm.hicloud.com:9001/TSMAPKP/HwTSMServer/applicationBusiness.action";

    public AppConfig(String url, int reader) {
        this.STREAMURL = url;
        this.CARDREADER = reader;
    }
}
