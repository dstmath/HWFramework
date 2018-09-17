package com.android.internal.telephony;

public class RadioCapability {
    private static final int RADIO_CAPABILITY_VERSION = 1;
    public static final int RC_PHASE_APPLY = 2;
    public static final int RC_PHASE_CONFIGURED = 0;
    public static final int RC_PHASE_FINISH = 4;
    public static final int RC_PHASE_START = 1;
    public static final int RC_PHASE_UNSOL_RSP = 3;
    public static final int RC_STATUS_FAIL = 2;
    public static final int RC_STATUS_NONE = 0;
    public static final int RC_STATUS_SUCCESS = 1;
    private String mLogicalModemUuid;
    private int mPhase;
    private int mPhoneId;
    private int mRadioAccessFamily;
    private int mSession;
    private int mStatus;

    public RadioCapability(int phoneId, int session, int phase, int radioAccessFamily, String logicalModemUuid, int status) {
        this.mPhoneId = phoneId;
        this.mSession = session;
        this.mPhase = phase;
        this.mRadioAccessFamily = radioAccessFamily;
        this.mLogicalModemUuid = logicalModemUuid;
        this.mStatus = status;
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public int getVersion() {
        return RC_STATUS_SUCCESS;
    }

    public int getSession() {
        return this.mSession;
    }

    public int getPhase() {
        return this.mPhase;
    }

    public int getRadioAccessFamily() {
        return this.mRadioAccessFamily;
    }

    public String getLogicalModemUuid() {
        return this.mLogicalModemUuid;
    }

    public int getStatus() {
        return this.mStatus;
    }

    public String toString() {
        return "{mPhoneId = " + this.mPhoneId + " mVersion=" + getVersion() + " mSession=" + getSession() + " mPhase=" + getPhase() + " mRadioAccessFamily=" + getRadioAccessFamily() + " mLogicModemId=" + getLogicalModemUuid() + " mStatus=" + getStatus() + "}";
    }
}
