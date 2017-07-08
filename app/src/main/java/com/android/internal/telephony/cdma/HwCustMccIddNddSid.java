package com.android.internal.telephony.cdma;

public class HwCustMccIddNddSid {
    private static final boolean DBG = false;
    private static final String LOG_TAG = "HwCustMccIddNddSid";
    public String Cc;
    public String Idd;
    public int Mcc;
    public String Ndd;
    public int SidMax;
    public int SidMin;

    public HwCustMccIddNddSid() {
        this.Mcc = -1;
        this.Cc = null;
        this.SidMin = -1;
        this.SidMax = -1;
        this.Idd = null;
        this.Ndd = null;
    }

    public HwCustMccIddNddSid(int mcc, String cc, int sidmin, int sidmax, String idd, String ndd) {
        this.Mcc = mcc;
        this.Cc = cc;
        this.SidMin = sidmin;
        this.SidMax = sidmax;
        this.Idd = idd;
        this.Ndd = ndd;
    }

    public HwCustMccIddNddSid(HwCustMccIddNddSid t) {
        copyFrom(t);
    }

    protected void copyFrom(HwCustMccIddNddSid t) {
        this.Mcc = t.Mcc;
        this.Cc = t.Cc;
        this.SidMin = t.SidMin;
        this.SidMax = t.SidMax;
        this.Idd = t.Idd;
        this.Ndd = t.Ndd;
    }

    public int getMcc() {
        return this.Mcc;
    }

    public String getCc() {
        return this.Cc;
    }

    public int getSidMin() {
        return this.SidMin;
    }

    public int getSidMax() {
        return this.SidMax;
    }

    public String getIdd() {
        return this.Idd;
    }

    public String getNdd() {
        return this.Ndd;
    }

    public String toString() {
        return "Mcc =" + this.Mcc + ", Cc = " + this.Cc + ", SidMin = " + this.SidMin + ", SidMax = " + this.SidMax + ", Idd = " + this.Idd + ", Ndd = " + this.Ndd;
    }
}
