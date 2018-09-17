package com.android.internal.telephony.cat;

public class ResultException extends CatException {
    private static final /* synthetic */ int[] -com-android-internal-telephony-cat-ResultCodeSwitchesValues = null;
    private int mAdditionalInfo;
    private String mExplanation;
    private ResultCode mResult;

    private static /* synthetic */ int[] -getcom-android-internal-telephony-cat-ResultCodeSwitchesValues() {
        if (-com-android-internal-telephony-cat-ResultCodeSwitchesValues != null) {
            return -com-android-internal-telephony-cat-ResultCodeSwitchesValues;
        }
        int[] iArr = new int[ResultCode.values().length];
        try {
            iArr[ResultCode.ACCESS_TECH_UNABLE_TO_PROCESS.ordinal()] = 9;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ResultCode.BACKWARD_MOVE_BY_USER.ordinal()] = 10;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ResultCode.BEYOND_TERMINAL_CAPABILITY.ordinal()] = 11;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ResultCode.BIP_ERROR.ordinal()] = 1;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ResultCode.CMD_DATA_NOT_UNDERSTOOD.ordinal()] = 12;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ResultCode.CMD_NUM_NOT_KNOWN.ordinal()] = 13;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ResultCode.CMD_TYPE_NOT_UNDERSTOOD.ordinal()] = 14;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ResultCode.CONTRADICTION_WITH_TIMER.ordinal()] = 15;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ResultCode.FRAMES_ERROR.ordinal()] = 2;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[ResultCode.HELP_INFO_REQUIRED.ordinal()] = 16;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[ResultCode.LAUNCH_BROWSER_ERROR.ordinal()] = 3;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[ResultCode.MMS_ERROR.ordinal()] = 4;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[ResultCode.MMS_TEMPORARY.ordinal()] = 17;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[ResultCode.MULTI_CARDS_CMD_ERROR.ordinal()] = 5;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[ResultCode.NAA_CALL_CONTROL_TEMPORARY.ordinal()] = 18;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[ResultCode.NETWORK_CRNTLY_UNABLE_TO_PROCESS.ordinal()] = 6;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[ResultCode.NO_RESPONSE_FROM_USER.ordinal()] = 19;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[ResultCode.OK.ordinal()] = 20;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[ResultCode.PRFRMD_ICON_NOT_DISPLAYED.ordinal()] = 21;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[ResultCode.PRFRMD_LIMITED_SERVICE.ordinal()] = 22;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[ResultCode.PRFRMD_MODIFIED_BY_NAA.ordinal()] = 23;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[ResultCode.PRFRMD_NAA_NOT_ACTIVE.ordinal()] = 24;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[ResultCode.PRFRMD_TONE_NOT_PLAYED.ordinal()] = 25;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[ResultCode.PRFRMD_WITH_ADDITIONAL_EFS_READ.ordinal()] = 26;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[ResultCode.PRFRMD_WITH_MISSING_INFO.ordinal()] = 27;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[ResultCode.PRFRMD_WITH_MODIFICATION.ordinal()] = 28;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[ResultCode.PRFRMD_WITH_PARTIAL_COMPREHENSION.ordinal()] = 29;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[ResultCode.REQUIRED_VALUES_MISSING.ordinal()] = 30;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[ResultCode.SMS_RP_ERROR.ordinal()] = 31;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[ResultCode.SS_RETURN_ERROR.ordinal()] = 32;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS.ordinal()] = 7;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[ResultCode.UICC_SESSION_TERM_BY_USER.ordinal()] = 33;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[ResultCode.USER_CLEAR_DOWN_CALL.ordinal()] = 34;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[ResultCode.USER_NOT_ACCEPT.ordinal()] = 35;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[ResultCode.USIM_CALL_CONTROL_PERMANENT.ordinal()] = 8;
        } catch (NoSuchFieldError e35) {
        }
        try {
            iArr[ResultCode.USSD_RETURN_ERROR.ordinal()] = 36;
        } catch (NoSuchFieldError e36) {
        }
        try {
            iArr[ResultCode.USSD_SS_SESSION_TERM_BY_USER.ordinal()] = 37;
        } catch (NoSuchFieldError e37) {
        }
        -com-android-internal-telephony-cat-ResultCodeSwitchesValues = iArr;
        return iArr;
    }

    public ResultException(ResultCode result) {
        switch (-getcom-android-internal-telephony-cat-ResultCodeSwitchesValues()[result.ordinal()]) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                throw new AssertionError("For result code, " + result + ", additional information must be given!");
            default:
                this.mResult = result;
                this.mAdditionalInfo = -1;
                this.mExplanation = "";
                return;
        }
    }

    public ResultException(ResultCode result, String explanation) {
        this(result);
        this.mExplanation = explanation;
    }

    public ResultException(ResultCode result, int additionalInfo) {
        this(result);
        if (additionalInfo < 0) {
            throw new AssertionError("Additional info must be greater than zero!");
        }
        this.mAdditionalInfo = additionalInfo;
    }

    public ResultException(ResultCode result, int additionalInfo, String explanation) {
        this(result, additionalInfo);
        this.mExplanation = explanation;
    }

    public ResultCode result() {
        return this.mResult;
    }

    public boolean hasAdditionalInfo() {
        return this.mAdditionalInfo >= 0;
    }

    public int additionalInfo() {
        return this.mAdditionalInfo;
    }

    public String explanation() {
        return this.mExplanation;
    }

    public String toString() {
        return "result=" + this.mResult + " additionalInfo=" + this.mAdditionalInfo + " explantion=" + this.mExplanation;
    }
}
