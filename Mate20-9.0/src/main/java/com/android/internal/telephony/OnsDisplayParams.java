package com.android.internal.telephony;

public class OnsDisplayParams {
    public String mPlmn;
    public int mRule;
    public boolean mShowPlmn;
    public boolean mShowSpn;
    public boolean mShowWifi;
    public String mSpn;
    public String mWifi;

    public OnsDisplayParams(boolean showSpn, boolean showPlmn, int rule, String plmn, String spn, boolean showWifi, String wifi) {
        this.mShowSpn = showSpn;
        this.mShowPlmn = showPlmn;
        this.mRule = rule;
        this.mPlmn = plmn;
        this.mSpn = spn;
        this.mShowWifi = showWifi;
        this.mWifi = wifi;
    }

    public OnsDisplayParams(boolean showSpn, boolean showPlmn, int rule, String plmn, String spn) {
        this.mShowSpn = showSpn;
        this.mShowPlmn = showPlmn;
        this.mRule = rule;
        this.mPlmn = plmn;
        this.mSpn = spn;
    }
}
