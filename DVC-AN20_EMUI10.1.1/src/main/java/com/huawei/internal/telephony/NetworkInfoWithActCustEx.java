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

    public NetworkInfoWithActCustEx(String operatorNumeric2, int nAct2, int nPriority2) {
        this.operatorNumeric = operatorNumeric2;
        this.nAct = nAct2;
        this.nPriority = nPriority2;
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
        if (act == 1) {
            return "GSM";
        }
        if (act == 2) {
            return "GSM_COMPACT";
        }
        if (act == 4) {
            return "UMTS";
        }
        if (act == 5) {
            return "Dual mode";
        }
        if (act == 8) {
            return "LTE";
        }
        return "" + act;
    }

    public void setOperatorNumeric(String operatorNumeric2) {
        this.operatorNumeric = operatorNumeric2;
    }

    public void setAccessTechnology(int nAct2) {
        this.nAct = nAct2;
    }

    public void setPriority(int nPriority2) {
        this.nPriority = nPriority2;
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
