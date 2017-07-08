package com.android.internal.telephony.cdma;

public class HwCustSidMccMnc {
    private static final boolean DBG = true;
    private static final String LOG_TAG = "HwCustSidMccMnc";
    public int mccMnc;
    public int sid;

    public HwCustSidMccMnc() {
        this.sid = -1;
        this.mccMnc = -1;
    }

    public HwCustSidMccMnc(int mSid, int mMccMnc) {
        this.sid = mSid;
        this.mccMnc = mMccMnc;
    }

    public HwCustSidMccMnc(HwCustSidMccMnc t) {
        copyFrom(t);
    }

    protected void copyFrom(HwCustSidMccMnc t) {
        this.sid = t.sid;
        this.mccMnc = t.mccMnc;
    }

    public int getSid() {
        return this.sid;
    }

    public int getMccMnc() {
        return this.mccMnc;
    }

    public String toString() {
        return "sid =" + this.sid + ", mccMnc = " + this.mccMnc;
    }
}
