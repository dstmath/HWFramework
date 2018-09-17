package com.android.internal.telephony.dataconnection;

import android.os.Message;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.uicc.UiccController;

public class HwCustDcTracker {
    public HwCustDcTracker(DcTracker dcTracker) {
    }

    public void handleCustMessage(Message msg) {
    }

    public boolean isPSAllowedByFdn() {
        return true;
    }

    public void registerForFdn() {
    }

    public void unregisterForFdn() {
    }

    public ApnSetting getPrefMmsApnForVoWifi(ApnContext apnContext, int radioTech, String operator, ApnSetting apnSetting) {
        return apnSetting;
    }

    public boolean usePrefApnForIwlanNetwork(String operator, String apnType, int radioTech) {
        return true;
    }

    public boolean apnRoamingAdjust(DcTracker dcTracker, ApnSetting apnSetting, Phone phone) {
        return true;
    }

    public void checkPLMN(String plmn) {
    }

    public void onOtaAttachFailed(ApnContext apnContext) {
    }

    public boolean getmIsPseudoImsi() {
        return false;
    }

    public void sendOTAAttachTimeoutMsg(ApnContext apnContext, boolean retValue) {
    }

    public void openServiceStart(UiccController uiccController) {
    }

    public String getPlmn() {
        return null;
    }

    public boolean isDocomoApn(ApnSetting preferredApn) {
        return false;
    }

    public ApnSetting getDocomoApn(ApnSetting preferredApn) {
        return preferredApn;
    }

    public boolean isCanHandleType(ApnSetting apnSetting, String requestedApnType) {
        return true;
    }

    public boolean isDocomoTetheringApn(ApnSetting apnSetting, String type) {
        return false;
    }

    public boolean hasSetCustDataFeature() {
        return false;
    }

    public void updateCustMobileDataFeature() {
    }

    public void setCustDataEnableByHplmn() {
    }

    public boolean addSpecifiedApnSwitch() {
        return false;
    }

    public boolean addSpecifiedApnToWaitingApns(DcTracker dcTracker, ApnSetting preferredApn, ApnSetting apn) {
        return false;
    }
}
