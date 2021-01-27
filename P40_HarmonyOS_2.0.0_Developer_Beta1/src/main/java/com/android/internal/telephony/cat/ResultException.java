package com.android.internal.telephony.cat;

import android.annotation.UnsupportedAppUsage;
import com.android.internal.telephony.PhoneConfigurationManager;

public class ResultException extends CatException {
    private int mAdditionalInfo;
    private String mExplanation;
    private ResultCode mResult;

    @UnsupportedAppUsage
    public ResultException(ResultCode result) {
        switch (result) {
            case TERMINAL_CRNTLY_UNABLE_TO_PROCESS:
            case NETWORK_CRNTLY_UNABLE_TO_PROCESS:
            case LAUNCH_BROWSER_ERROR:
            case MULTI_CARDS_CMD_ERROR:
            case USIM_CALL_CONTROL_PERMANENT:
            case BIP_ERROR:
            case FRAMES_ERROR:
            case MMS_ERROR:
                throw new AssertionError("For result code, " + result + ", additional information must be given!");
            default:
                this.mResult = result;
                this.mAdditionalInfo = -1;
                this.mExplanation = PhoneConfigurationManager.SSSS;
                return;
        }
    }

    public ResultException(ResultCode result, String explanation) {
        this(result);
        this.mExplanation = explanation;
    }

    public ResultException(ResultCode result, int additionalInfo) {
        this(result);
        if (additionalInfo >= 0) {
            this.mAdditionalInfo = additionalInfo;
            return;
        }
        throw new AssertionError("Additional info must be greater than zero!");
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

    @Override // java.lang.Throwable, java.lang.Object
    public String toString() {
        return "result=" + this.mResult + " additionalInfo=" + this.mAdditionalInfo + " explantion=" + this.mExplanation;
    }
}
