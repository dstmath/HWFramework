package com.android.internal.telephony.dataconnection;

import android.os.Message;
import android.telephony.PcoData;
import com.android.internal.telephony.uicc.UiccController;
import java.util.ArrayList;

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

    public boolean canKeepApn(String requestedApnType, ApnSetting apnSetting, boolean isPreferredApn) {
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

    public void savePcoData(PcoData pcoData) {
    }

    public boolean isRoamDisallowedByCustomization(ApnContext apnContext) {
        return false;
    }

    public boolean addSpecifiedApnSwitch() {
        return false;
    }

    public boolean addSpecifiedApnToWaitingApns(DcTracker dcTracker, ApnSetting preferredApn, ApnSetting apn) {
        return false;
    }

    public void setDataOrRoamOn(int cause) {
    }

    public boolean isDataDisableBySim2() {
        return false;
    }

    public boolean isDataDisable(int subId) {
        return false;
    }

    public void dispose() {
    }

    public String getOpKeyByActivedApn(String activedNumeric, String activedApn, String activedUser) {
        return null;
    }

    public void setApnOpkeyToSettingsDB(String activedApnOpkey) {
    }

    public ArrayList<ApnSetting> sortApnListByBearer(ArrayList<ApnSetting> list, String requestedApnType, int radioTech) {
        return list;
    }

    public boolean hasBetterApnByBearer(ApnSetting curApnSetting, ArrayList<ApnSetting> arrayList, String requestedApnType, int radioTech) {
        return false;
    }

    public void tryRestartRadioWhenPrefApnChange(ApnSetting curPreferredApn, ApnSetting oldPreferredApn) {
    }

    public void tryClearRejFlag() {
    }

    /* access modifiers changed from: protected */
    public boolean isDataAllowedForSES(String apnType) {
        return false;
    }
}
