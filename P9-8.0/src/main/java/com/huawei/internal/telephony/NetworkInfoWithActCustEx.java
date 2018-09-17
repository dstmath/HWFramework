package com.huawei.internal.telephony;

public class NetworkInfoWithActCustEx {
    public static final int ACT_DUAL_MODE = 5;
    public static final int ACT_GSM = 1;
    public static final int ACT_GSM_COMPACT = 2;
    public static final int ACT_LTE = 8;
    public static final int ACT_UTRAN = 4;
    public static final int VALUE_DEFAULTE_PRIORITY = 0;
    private int nAct;
    private int nPriority;
    private String operatorNumeric;

    public NetworkInfoWithActCustEx() {
        this.operatorNumeric = "";
        this.nAct = 0;
        this.nPriority = 0;
    }

    public NetworkInfoWithActCustEx(String operatorNumeric, int nAct, int nPriority) {
        this.operatorNumeric = operatorNumeric;
        this.nAct = nAct;
        this.nPriority = nPriority;
    }

    public String getOperatorNumeric() {
        return this.operatorNumeric;
    }

    public int getAccessTechnology() {
        return this.nAct;
    }

    public int getPriority() {
        return this.nPriority;
    }

    public static String actToStr(int act) {
        String result = "";
        switch (act) {
            case 1:
                return "GSM";
            case 2:
                return "GSM_COMPACT";
            case ACT_UTRAN /*4*/:
                return "UMTS";
            case ACT_DUAL_MODE /*5*/:
                return "Dual mode";
            case 8:
                return "LTE";
            default:
                return "" + act;
        }
    }

    public void setOperatorNumeric(String operatorNumeric) {
        this.operatorNumeric = operatorNumeric;
    }

    public void setAccessTechnology(int nAct) {
        this.nAct = nAct;
    }

    public void setPriority(int nPriority) {
        this.nPriority = nPriority;
    }

    public void copyFrom(NetworkInfoWithActCustEx niwt) {
        this.operatorNumeric = niwt.operatorNumeric;
        this.nAct = niwt.nAct;
        this.nPriority = niwt.nPriority;
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append(this.operatorNumeric);
        str.append("/");
        str.append(actToStr(this.nAct));
        str.append("/");
        str.append(this.nPriority);
        return str.toString();
    }
}
