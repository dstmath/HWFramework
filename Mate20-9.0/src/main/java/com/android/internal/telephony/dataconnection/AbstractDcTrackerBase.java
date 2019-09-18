package com.android.internal.telephony.dataconnection;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractDcTrackerBase extends Handler {
    public static final int VP_END = 0;
    public static final int VP_START = 1;
    DcTrackerBaseReference mReference = HwTelephonyFactory.getHwDataConnectionManager().createHwDcTrackerBaseReference(this);
    public int mVpStatus = 0;

    public interface DcTrackerBaseReference {
        void addIfacePhoneHashMap(DcAsyncChannel dcAsyncChannel, HashMap<String, Integer> hashMap);

        void beforeHandleMessage(Message message);

        ArrayList<ApnSetting> buildWaitingApnsForCTSupl(String str, int i);

        boolean checkMvnoParams();

        void clearAndResumeNetInfoForWifiLteCoexist(int i, int i2, ApnContext apnContext);

        void clearDefaultLink();

        void disableGoogleDunApn(Context context, String str, ApnSetting apnSetting);

        void dispose();

        void disposeCustDct();

        boolean enableTcpUdpSumForDataStall();

        ApnSetting fetchBipApn(ApnSetting apnSetting, ArrayList<ApnSetting> arrayList);

        int get2gSlot();

        int get4gSlot();

        boolean getAnyDataEnabledByApnContext(ApnContext apnContext, boolean z);

        ApnSetting getApnForCT();

        String getAppName(int i);

        ApnSetting getAttachedApnSetting();

        void getAttachedApnSettings();

        boolean getAttachedStatus(boolean z);

        String getCTOperator(String str);

        String getCTOperatorNumeric(String str);

        ApnSetting getCustPreferredApn(ArrayList<ApnSetting> arrayList);

        boolean getCustRetryConfig();

        boolean getDataRoamingEnabledWithNational();

        int getDataRoamingScope();

        String getDataRoamingSettingItem(String str);

        int getDefault4GSlotId();

        int getDelayTime();

        long[] getDnsPacketTxRxSum();

        boolean getEsmFlagAdaptionEnabled();

        int getEsmFlagFromCard();

        void getNetdPid();

        String getOpKeyByActivedApn(String str, String str2, String str3);

        String getOperatorNumeric();

        UiccCardApplication getUiccCardApplication(int i);

        int getVSimSubId();

        boolean getXcapDataRoamingEnable();

        void handleCustMessage(Message message);

        void init();

        boolean isActiveDataSubscription();

        boolean isApnPreset(ApnSetting apnSetting);

        boolean isApnTypeDisabled(String str);

        boolean isBipApnType(String str);

        boolean isBlockSetInitialAttachApn();

        boolean isBtConnected();

        boolean isCTDualModeCard(int i);

        boolean isCTSimCard(int i);

        boolean isChinaTelecom(int i);

        boolean isClearCodeEnabled();

        boolean isDataAllowedByApnContext(ApnContext apnContext);

        boolean isDataAllowedForRoaming(boolean z);

        boolean isDataConnectivityDisabled(int i, String str);

        boolean isDataDisableBySim2();

        boolean isDataNeededWithWifiAndBt();

        boolean isDisconnectedOrConnecting();

        boolean isFullNetworkSupported();

        boolean isLTENetwork();

        boolean isNeedDataRoamingExpend();

        boolean isNeedFilterVowifiMms(ApnSetting apnSetting, String str);

        boolean isNeedForceSetup(ApnContext apnContext);

        boolean isPSClearCodeRplmnMatched();

        boolean isPingOk();

        boolean isPsAllowedByFdn();

        boolean isRoamingPushDisabled();

        boolean isSupportLTE(ApnSetting apnSetting);

        boolean isWifiConnected();

        ApnSetting makeHwApnSetting(Cursor cursor, String[] strArr);

        boolean needRemovedPreferredApn();

        boolean needRestartRadioOnError(ApnContext apnContext, DcFailCause dcFailCause);

        boolean needRetryAfterDisconnected(DcFailCause dcFailCause);

        boolean needSetCTProxy(ApnSetting apnSetting);

        String networkTypeToApnType(int i);

        boolean noNeedDoRecovery(ConcurrentHashMap concurrentHashMap);

        void onAllApnFirstActiveFailed();

        void onAllApnPermActiveFailed();

        void onRatChange();

        void onVPEnded();

        void onVPStarted();

        void onVpStatusChanged(AsyncResult asyncResult);

        void operateClearCodeProcess(ApnContext apnContext, DcFailCause dcFailCause, int i);

        boolean processAttDataRoamingOff();

        boolean processAttDataRoamingOn();

        void registerForFdn();

        void registerForFdnRecordsLoaded(IccRecords iccRecords);

        void registerForGetAdDone(UiccCardApplication uiccCardApplication);

        void registerForImsi(UiccCardApplication uiccCardApplication, IccRecords iccRecords);

        void registerForImsiReady(IccRecords iccRecords);

        void registerForRecordsLoaded(IccRecords iccRecords);

        void registerPhoneStateListener(Context context);

        void resetTryTimes();

        void resumeDefaultLink();

        void sendDSMipErrorBroadcast();

        void sendRoamingDataStatusChangBroadcast();

        void setApnOpkeyToSettingsDB(String str);

        void setAttachedApnSetting(ApnSetting apnSetting);

        void setCtProxy(DcAsyncChannel dcAsyncChannel);

        void setCurFailCause(AsyncResult asyncResult);

        boolean setDataRoamingScope(int i);

        void setFirstTimeEnableData();

        void setMPDNByNetWork(String str);

        void setRetryAfterDisconnectedReason(DataConnection dataConnection, ArrayList<ApnContext> arrayList);

        void setupDataForSinglePdnArbitration(String str);

        void setupDataOnConnectableApns(String str, String str2);

        boolean shouldDisableMultiPdps(boolean z);

        void startListenCellLocationChange();

        void startPdpResetAlarm(int i);

        void stopListenCellLocationChange();

        void stopPdpResetAlarm();

        void unregisterForFdn();

        void unregisterForFdnRecordsLoaded(IccRecords iccRecords);

        void unregisterForGetAdDone(UiccCardApplication uiccCardApplication);

        void unregisterForImsiReady(IccRecords iccRecords);

        void unregisterForRecordsLoaded(IccRecords iccRecords);

        void updateApnContextState();

        void updateApnId();

        void updateApnLists(String str, int i, ArrayList<ApnSetting> arrayList, String str2);

        void updateDSUseDuration();

        void updateForVSim();

        void updateLastRadioResetTimestamp();

        long updatePSClearCodeApnContext(AsyncResult asyncResult, ApnContext apnContext, long j);
    }

    /* access modifiers changed from: protected */
    public void init() {
        this.mReference.init();
    }

    /* access modifiers changed from: protected */
    public void beforeHandleMessage(Message msg) {
        this.mReference.beforeHandleMessage(msg);
    }

    /* access modifiers changed from: protected */
    public boolean isDataAllowedByApnContext(ApnContext apnContext) {
        return this.mReference.isDataAllowedByApnContext(apnContext);
    }

    /* access modifiers changed from: protected */
    public boolean getAnyDataEnabledByApnContext(ApnContext apnContext, boolean enable) {
        return this.mReference.getAnyDataEnabledByApnContext(apnContext, enable);
    }

    /* access modifiers changed from: protected */
    public boolean isDataAllowedForRoaming(boolean isMms) {
        return this.mReference.isDataAllowedForRoaming(isMms);
    }

    /* access modifiers changed from: protected */
    public void onAllApnFirstActiveFailed() {
        this.mReference.onAllApnFirstActiveFailed();
    }

    /* access modifiers changed from: protected */
    public void onAllApnPermActiveFailed() {
        this.mReference.onAllApnPermActiveFailed();
    }

    /* access modifiers changed from: protected */
    public void setFirstTimeEnableData() {
        this.mReference.setFirstTimeEnableData();
    }

    public String getDataRoamingSettingItem(String originItem) {
        return this.mReference.getDataRoamingSettingItem(originItem);
    }

    public void disableGoogleDunApn(Context c, String apnData, ApnSetting dunSetting) {
        this.mReference.disableGoogleDunApn(c, apnData, dunSetting);
    }

    public boolean shouldDisableMultiPdps(boolean onlySingleDcAllowed) {
        return this.mReference.shouldDisableMultiPdps(onlySingleDcAllowed);
    }

    /* access modifiers changed from: protected */
    public boolean needRemovedPreferredApn() {
        return this.mReference.needRemovedPreferredApn();
    }

    /* access modifiers changed from: protected */
    public boolean isBipApnType(String type) {
        return this.mReference.isBipApnType(type);
    }

    /* access modifiers changed from: protected */
    public ApnSetting fetchBipApn(ApnSetting preferredApn, ArrayList<ApnSetting> allApnSettings) {
        return this.mReference.fetchBipApn(preferredApn, allApnSettings);
    }

    public void setMPDNByNetWork(String plmnNetWork) {
        this.mReference.setMPDNByNetWork(plmnNetWork);
    }

    public void dispose() {
        this.mReference.dispose();
    }

    public String getCTOperatorNumeric(String operator) {
        return this.mReference.getCTOperatorNumeric(operator);
    }

    public ApnSetting makeHwApnSetting(Cursor cursor, String[] types) {
        return this.mReference.makeHwApnSetting(cursor, types);
    }

    public boolean noNeedDoRecovery(ConcurrentHashMap<String, ApnContext> mApnContexts) {
        return this.mReference.noNeedDoRecovery(mApnContexts);
    }

    public boolean isApnPreset(ApnSetting apnSetting) {
        return this.mReference.isApnPreset(apnSetting);
    }

    public void getNetdPid() {
        this.mReference.getNetdPid();
    }

    public long[] getDnsPacketTxRxSum() {
        return this.mReference.getDnsPacketTxRxSum();
    }

    /* access modifiers changed from: protected */
    public void setEnabled(int id, boolean enable) {
    }

    public void setEnabledPublic(int id, boolean enable) {
        setEnabled(id, enable);
    }

    public void setupDataOnConnectableApns(String reason, String excludedApnType) {
        this.mReference.setupDataOnConnectableApns(reason, excludedApnType);
    }

    public boolean needRetryAfterDisconnected(DcFailCause cause) {
        return this.mReference.needRetryAfterDisconnected(cause);
    }

    public void setRetryAfterDisconnectedReason(DataConnection dc, ArrayList<ApnContext> apnsToCleanup) {
        this.mReference.setRetryAfterDisconnectedReason(dc, apnsToCleanup);
    }

    public boolean isChinaTelecom(int slotId) {
        return this.mReference.isChinaTelecom(slotId);
    }

    public boolean isFullNetworkSupported() {
        return this.mReference.isFullNetworkSupported();
    }

    public boolean isCTSimCard(int slotId) {
        return this.mReference.isCTSimCard(slotId);
    }

    public int getDefault4GSlotId() {
        return this.mReference.getDefault4GSlotId();
    }

    public boolean isCTDualModeCard(int sub) {
        return this.mReference.isCTDualModeCard(sub);
    }

    public boolean isPingOk() {
        return this.mReference.isPingOk();
    }

    public boolean isClearCodeEnabled() {
        return this.mReference.isClearCodeEnabled();
    }

    public void startListenCellLocationChange() {
        this.mReference.startListenCellLocationChange();
    }

    public void stopListenCellLocationChange() {
        this.mReference.stopListenCellLocationChange();
    }

    public void resetTryTimes() {
        this.mReference.resetTryTimes();
    }

    public void operateClearCodeProcess(ApnContext apnContext, DcFailCause cause, int delay) {
        this.mReference.operateClearCodeProcess(apnContext, cause, delay);
    }

    public void setCurFailCause(AsyncResult ar) {
        this.mReference.setCurFailCause(ar);
    }

    public int getDelayTime() {
        return this.mReference.getDelayTime();
    }

    public boolean isPSClearCodeRplmnMatched() {
        return this.mReference.isPSClearCodeRplmnMatched();
    }

    public long updatePSClearCodeApnContext(AsyncResult ar, ApnContext apnContext, long delay) {
        return this.mReference.updatePSClearCodeApnContext(ar, apnContext, delay);
    }

    public void registerForFdn() {
        this.mReference.registerForFdn();
    }

    public void unregisterForFdn() {
        this.mReference.unregisterForFdn();
    }

    public boolean isPsAllowedByFdn() {
        return this.mReference.isPsAllowedByFdn();
    }

    public void handleCustMessage(Message msg) {
        this.mReference.handleCustMessage(msg);
    }

    public void registerForFdnRecordsLoaded(IccRecords r) {
        this.mReference.registerForFdnRecordsLoaded(r);
    }

    public void unregisterForFdnRecordsLoaded(IccRecords r) {
        this.mReference.unregisterForFdnRecordsLoaded(r);
    }

    public boolean isActiveDataSubscription() {
        return this.mReference.isActiveDataSubscription();
    }

    /* access modifiers changed from: protected */
    public int get4gSlot() {
        return this.mReference.get4gSlot();
    }

    /* access modifiers changed from: protected */
    public int get2gSlot() {
        return this.mReference.get2gSlot();
    }

    public void addIfacePhoneHashMap(DcAsyncChannel dcac, HashMap<String, Integer> mIfacePhoneHashMap) {
        this.mReference.addIfacePhoneHashMap(dcac, mIfacePhoneHashMap);
    }

    /* access modifiers changed from: protected */
    public int getVSimSubId() {
        return this.mReference.getVSimSubId();
    }

    public void sendRoamingDataStatusChangBroadcast() {
        this.mReference.sendRoamingDataStatusChangBroadcast();
    }

    public void sendDSMipErrorBroadcast() {
        this.mReference.sendDSMipErrorBroadcast();
    }

    public boolean enableTcpUdpSumForDataStall() {
        return this.mReference.enableTcpUdpSumForDataStall();
    }

    public String networkTypeToApnType(int networkType) {
        return this.mReference.networkTypeToApnType(networkType);
    }

    public boolean isApnTypeDisabled(String apnType) {
        return this.mReference.isApnTypeDisabled(apnType);
    }

    public boolean setDataRoamingScope(int scope) {
        return this.mReference.setDataRoamingScope(scope);
    }

    public int getDataRoamingScope() {
        return this.mReference.getDataRoamingScope();
    }

    public boolean getDataRoamingEnabledWithNational() {
        return this.mReference.getDataRoamingEnabledWithNational();
    }

    public boolean isNeedDataRoamingExpend() {
        return this.mReference.isNeedDataRoamingExpend();
    }

    public void unregisterForImsiReady(IccRecords r) {
        this.mReference.unregisterForImsiReady(r);
    }

    public void registerForImsiReady(IccRecords r) {
        this.mReference.registerForImsiReady(r);
    }

    public void unregisterForRecordsLoaded(IccRecords r) {
        this.mReference.unregisterForRecordsLoaded(r);
    }

    public void registerForRecordsLoaded(IccRecords r) {
        this.mReference.registerForRecordsLoaded(r);
    }

    public void registerForGetAdDone(UiccCardApplication newUiccApplication) {
        this.mReference.registerForGetAdDone(newUiccApplication);
    }

    public void unregisterForGetAdDone(UiccCardApplication newUiccApplication) {
        this.mReference.unregisterForGetAdDone(newUiccApplication);
    }

    public void registerForImsi(UiccCardApplication newUiccApplication, IccRecords r) {
        this.mReference.registerForImsi(newUiccApplication, r);
    }

    public boolean checkMvnoParams() {
        return this.mReference.checkMvnoParams();
    }

    public boolean isDataConnectivityDisabled(int slotId, String tag) {
        return this.mReference.isDataConnectivityDisabled(slotId, tag);
    }

    public ApnSetting getCustPreferredApn(ArrayList<ApnSetting> apnSettings) {
        return this.mReference.getCustPreferredApn(apnSettings);
    }

    public boolean isRoamingPushDisabled() {
        return this.mReference.isRoamingPushDisabled();
    }

    public boolean processAttDataRoamingOn() {
        return this.mReference.processAttDataRoamingOn();
    }

    public boolean processAttDataRoamingOff() {
        return this.mReference.processAttDataRoamingOff();
    }

    public boolean getXcapDataRoamingEnable() {
        return this.mReference.getXcapDataRoamingEnable();
    }

    public boolean isBtConnected() {
        return this.mReference.isBtConnected();
    }

    public boolean isWifiConnected() {
        return this.mReference.isWifiConnected();
    }

    public boolean isDataNeededWithWifiAndBt() {
        return this.mReference.isDataNeededWithWifiAndBt();
    }

    public void updateDSUseDuration() {
        this.mReference.updateDSUseDuration();
    }

    /* access modifiers changed from: protected */
    public boolean getAttachedStatus(boolean attached) {
        return this.mReference.getAttachedStatus(attached);
    }

    public void updateApnLists(String requestedApnType, int radioTech, ArrayList<ApnSetting> apnList, String operator) {
        this.mReference.updateApnLists(requestedApnType, radioTech, apnList, operator);
    }

    public ApnSetting getAttachedApnSetting() {
        return this.mReference.getAttachedApnSetting();
    }

    public void setAttachedApnSetting(ApnSetting apnSetting) {
        this.mReference.setAttachedApnSetting(apnSetting);
    }

    public void getAttachedApnSettings() {
        this.mReference.getAttachedApnSettings();
    }

    public void updateLastRadioResetTimestamp() {
        this.mReference.updateLastRadioResetTimestamp();
    }

    public boolean needRestartRadioOnError(ApnContext apnContext, DcFailCause cause) {
        return this.mReference.needRestartRadioOnError(apnContext, cause);
    }

    public void startPdpResetAlarm(int delay) {
        this.mReference.startPdpResetAlarm(delay);
    }

    public void stopPdpResetAlarm() {
        this.mReference.stopPdpResetAlarm();
    }

    public void onVpStatusChanged(AsyncResult ar) {
        this.mReference.onVpStatusChanged(ar);
    }

    public void onVPStarted() {
        this.mReference.onVPStarted();
    }

    public void onVPEnded() {
        this.mReference.onVPEnded();
    }

    public boolean isLTENetwork() {
        return this.mReference.isLTENetwork();
    }

    public ApnSetting getApnForCT() {
        return this.mReference.getApnForCT();
    }

    public void updateApnId() {
        this.mReference.updateApnId();
    }

    public boolean needSetCTProxy(ApnSetting apn) {
        return this.mReference.needSetCTProxy(apn);
    }

    public void setCtProxy(DcAsyncChannel dcac) {
        this.mReference.setCtProxy(dcac);
    }

    public void onRatChange() {
        this.mReference.onRatChange();
    }

    public boolean isSupportLTE(ApnSetting apnSetting) {
        return this.mReference.isSupportLTE(apnSetting);
    }

    public ArrayList<ApnSetting> buildWaitingApnsForCTSupl(String requestedApnType, int radioTech) {
        return this.mReference.buildWaitingApnsForCTSupl(requestedApnType, radioTech);
    }

    public void clearAndResumeNetInfoForWifiLteCoexist(int apnId, int enabled, ApnContext apnContext) {
        this.mReference.clearAndResumeNetInfoForWifiLteCoexist(apnId, enabled, apnContext);
    }

    public void updateApnContextState() {
        this.mReference.updateApnContextState();
    }

    public UiccCardApplication getUiccCardApplication(int appFamily) {
        return this.mReference.getUiccCardApplication(appFamily);
    }

    public void updateForVSim() {
        this.mReference.updateForVSim();
    }

    public String getAppName(int pid) {
        return this.mReference.getAppName(pid);
    }

    public void registerPhoneStateListener(Context context) {
        this.mReference.registerPhoneStateListener(context);
    }

    public boolean isDisconnectedOrConnecting() {
        return this.mReference.isDisconnectedOrConnecting();
    }

    public void setupDataForSinglePdnArbitration(String reason) {
        this.mReference.setupDataForSinglePdnArbitration(reason);
    }

    public boolean isNeedFilterVowifiMms(ApnSetting apn, String requestedApnType) {
        return this.mReference.isNeedFilterVowifiMms(apn, requestedApnType);
    }

    public boolean isBlockSetInitialAttachApn() {
        return this.mReference.isBlockSetInitialAttachApn();
    }

    public boolean isNeedForceSetup(ApnContext apnContext) {
        return this.mReference.isNeedForceSetup(apnContext);
    }

    public boolean isDataDisableBySim2() {
        return this.mReference.isDataDisableBySim2();
    }

    public boolean getCustRetryConfig() {
        return this.mReference.getCustRetryConfig();
    }

    public boolean getEsmFlagAdaptionEnabled() {
        return this.mReference.getEsmFlagAdaptionEnabled();
    }

    public int getEsmFlagFromCard() {
        return this.mReference.getEsmFlagFromCard();
    }

    public String getOpKeyByActivedApn(String activedNumeric, String activedApn, String activedUser) {
        return this.mReference.getOpKeyByActivedApn(activedNumeric, activedApn, activedUser);
    }

    public void setApnOpkeyToSettingsDB(String activedApnOpkey) {
        this.mReference.setApnOpkeyToSettingsDB(activedApnOpkey);
    }

    public void disposeCustDct() {
        this.mReference.disposeCustDct();
    }

    public void clearDefaultLink() {
        this.mReference.clearDefaultLink();
    }

    public void resumeDefaultLink() {
        this.mReference.resumeDefaultLink();
    }

    public String getOperatorNumeric() {
        return this.mReference.getOperatorNumeric();
    }

    public String getCTOperator(String operator) {
        return this.mReference.getCTOperator(operator);
    }
}
