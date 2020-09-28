package com.android.internal.telephony.dataconnection;

import android.os.Message;
import android.telephony.PcoData;
import android.telephony.data.ApnSetting;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccController;
import java.util.ArrayList;
import java.util.List;

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

    /* access modifiers changed from: protected */
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

    public boolean isDataDisable(int slotId) {
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

    /* access modifiers changed from: protected */
    public boolean isDataAllowedForSES(String apnType) {
        return false;
    }

    public void tryRestartRadioWhenPrefApnChange(ApnSetting curPreferredApn, ApnSetting oldPreferredApn) {
    }

    public void tryClearRejCause() {
    }

    public boolean isCustCorrectApnAuthOn() {
        return false;
    }

    public void custCorrectApnAuth(List<ApnSetting> list) {
    }

    public boolean needRemovedPreferredApn() {
        return false;
    }

    public void onAllApnFirstActiveFailed() {
    }

    public void onAllApnPermActiveFailed() {
    }

    public void setFirstTimeEnableData() {
    }

    public void setMPDNByNetwork(String plmnNetwork) {
    }

    public boolean shouldDisableMultiPdps(boolean onlySingleDcAllowed) {
        return onlySingleDcAllowed;
    }

    public void setSinglePdpAllow(boolean isSinglePdpAllowed) {
    }

    public boolean isClearCodeEnabled() {
        return false;
    }

    public void startListenCellLocationChange() {
    }

    public void stopListenCellLocationChange() {
    }

    public void resetTryTimes() {
    }

    public void operateClearCodeProcess(ApnContext apnContext, int cause, int delay) {
    }

    public int getDelayTime() {
        return 0;
    }

    public void setCurFailCause(int cause) {
    }

    public boolean isPSClearCodeRplmnMatched() {
        return false;
    }

    public long updatePSClearCodeApnContext(ApnContext apnContext, long delay) {
        return delay;
    }

    public boolean isLimitPDPAct() {
        return false;
    }

    public boolean isPsAllowedByFdn() {
        return true;
    }

    public void registerForFdnRecordsLoaded(IccRecords r) {
    }

    public void unregisterForFdnRecordsLoaded(IccRecords r) {
    }

    public boolean setDataRoamingScope(int scope) {
        return true;
    }

    public int getDataRoamingScope() {
        return -1;
    }

    public boolean getDataRoamingEnabledWithNational() {
        return false;
    }

    public boolean isNeedDataRoamingExpend() {
        return false;
    }

    public ApnSetting getCustPreferredApn(List<ApnSetting> list) {
        return null;
    }

    public boolean processAttDataRoamingOn() {
        return false;
    }

    public boolean processAttDataRoamingOff() {
        return false;
    }

    public boolean clearAndResumeNetInfoForWifiLteCoexist(int apnId, int enabled, ApnContext apnContext) {
        return true;
    }

    public boolean getCustRetryConfig() {
        return false;
    }

    public boolean getEsmFlagAdaptionEnabled() {
        return false;
    }

    public int getEsmFlagFromCard() {
        return -1;
    }

    public boolean isApnTypeDisabled(String apnType) {
        return false;
    }
}
