package com.huawei.internal.telephony;

public class CallDetailsEx {
    public static final int CALL_DOMAIN_AUTOMATIC = 3;
    public static final int CALL_DOMAIN_CS = 1;
    public static final int CALL_DOMAIN_NOT_SET = 4;
    public static final int CALL_DOMAIN_PS = 2;
    public static final int CALL_DOMAIN_UNKNOWN = 0;
    public static final int CALL_TYPE_UNKNOWN = 10;
    public static final int CALL_TYPE_VOICE = 0;
    public static final int CALL_TYPE_VS_RX = 2;
    public static final int CALL_TYPE_VS_TX = 1;
    public static final int CALL_TYPE_VT = 3;
    public int call_domain;
    public int call_type;
    public String[] extras;

    public CallDetailsEx() {
        this.call_type = 0;
        this.call_domain = 4;
        this.extras = null;
    }

    public CallDetailsEx(int callType, int callDomain, String[] extraparams) {
        this.call_type = callType;
        this.call_domain = callDomain;
        if (extraparams != null) {
            int size = extraparams.length;
            this.extras = new String[size];
            for (int i = 0; i < size; i++) {
                this.extras[i] = extraparams[i];
            }
        }
    }

    public CallDetailsEx(CallDetailsEx srcCall) {
        if (srcCall != null) {
            this.call_type = srcCall.call_type;
            this.call_domain = srcCall.call_domain;
            if (srcCall.extras != null) {
                int size = srcCall.extras.length;
                this.extras = new String[size];
                for (int i = 0; i < size; i++) {
                    this.extras[i] = srcCall.extras[i];
                }
            }
        }
    }

    public void setExtrasEx(String[] extraparams) {
        this.extras = extraparams;
    }

    public String toString() {
        return " " + this.call_type + " " + this.call_domain;
    }
}
