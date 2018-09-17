package com.android.internal.telephony.dataconnection;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.dataconnection.DcTracker.DataAllowFailReason;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractDcTrackerBase extends Handler {
    DcTrackerBaseReference mReference = HwTelephonyFactory.getHwDataConnectionManager().createHwDcTrackerBaseReference(this);

    public interface DcTrackerBaseReference {
        void addIfacePhoneHashMap(DcAsyncChannel dcAsyncChannel, HashMap<String, Integer> hashMap);

        void beforeHandleMessage(Message message);

        boolean checkMvnoParams();

        void disableGoogleDunApn(Context context, String str, ApnSetting apnSetting);

        void dispose();

        boolean enableTcpUdpSumForDataStall();

        ApnSetting fetchBipApn(ApnSetting apnSetting, ArrayList<ApnSetting> arrayList);

        int get2gSlot();

        int get4gSlot();

        boolean getAnyDataEnabledByApnContext(ApnContext apnContext, boolean z);

        String getCTOperatorNumeric(String str);

        ApnSetting getCustPreferredApn(ArrayList<ApnSetting> arrayList);

        boolean getDataRoamingEnabledWithNational();

        int getDataRoamingScope();

        String getDataRoamingSettingItem(String str);

        int getDefault4GSlotId();

        int getDelayTime();

        long[] getDnsPacketTxRxSum();

        void getNetdPid();

        int getVSimSubId();

        boolean getXcapDataRoamingEnable();

        void handleCustMessage(Message message);

        void init();

        boolean isActiveDataSubscription();

        boolean isApnPreset(ApnSetting apnSetting);

        boolean isApnTypeDisabled(String str);

        boolean isBipApnType(String str);

        boolean isCTDualModeCard(int i);

        boolean isCTSimCard(int i);

        boolean isChinaTelecom(int i);

        boolean isClearCodeEnabled();

        boolean isDataAllowedByApnContext(ApnContext apnContext);

        boolean isDataAllowedByApnType(DataAllowFailReason dataAllowFailReason, String str);

        boolean isDataAllowedForRoaming(boolean z);

        boolean isDataConnectivityDisabled(int i, String str);

        boolean isFullNetworkSupported();

        boolean isNeedDataRoamingExpend();

        boolean isPSClearCodeRplmnMatched();

        boolean isPingOk();

        boolean isPsAllowedByFdn();

        boolean isRoamingPushDisabled();

        ApnSetting makeHwApnSetting(Cursor cursor, String[] strArr);

        boolean needRemovedPreferredApn();

        boolean needRetryAfterDisconnected(DcFailCause dcFailCause);

        String networkTypeToApnType(int i);

        boolean noNeedDoRecovery(ConcurrentHashMap concurrentHashMap);

        void onAllApnFirstActiveFailed();

        void onAllApnPermActiveFailed();

        void operateClearCodeProcess(ApnContext apnContext, DcFailCause dcFailCause, int i);

        boolean processAttDataRoamingOff();

        boolean processAttDataRoamingOn();

        void registerForFdn();

        void registerForFdnRecordsLoaded(IccRecords iccRecords);

        void registerForGetAdDone(UiccCardApplication uiccCardApplication);

        void registerForImsi(UiccCardApplication uiccCardApplication, IccRecords iccRecords);

        void registerForImsiReady(IccRecords iccRecords);

        void registerForRecordsLoaded(IccRecords iccRecords);

        void resetTryTimes();

        void sendDSMipErrorBroadcast();

        void sendRoamingDataStatusChangBroadcast();

        void setCurFailCause(AsyncResult asyncResult);

        boolean setDataRoamingScope(int i);

        void setFirstTimeEnableData();

        void setMPDNByNetWork(String str);

        void setRetryAfterDisconnectedReason(DataConnection dataConnection, ArrayList<ApnContext> arrayList);

        void setupDataOnConnectableApns(String str, String str2);

        boolean shouldDisableMultiPdps(boolean z);

        void startListenCellLocationChange();

        void stopListenCellLocationChange();

        void unregisterForFdn();

        void unregisterForFdnRecordsLoaded(IccRecords iccRecords);

        void unregisterForGetAdDone(UiccCardApplication uiccCardApplication);

        void unregisterForImsiReady(IccRecords iccRecords);

        void unregisterForRecordsLoaded(IccRecords iccRecords);

        long updatePSClearCodeApnContext(AsyncResult asyncResult, ApnContext apnContext, long j);
    }

    protected void init() {
        this.mReference.init();
    }

    protected void beforeHandleMessage(Message msg) {
        this.mReference.beforeHandleMessage(msg);
    }

    public boolean isDataAllowed(DataAllowFailReason failureReason, boolean isMms, boolean isUserEnable) {
        return false;
    }

    public boolean isDataAllowed(DataAllowFailReason failureReason, boolean isMms) {
        return isDataAllowed(failureReason, isMms, false);
    }

    protected boolean isDataAllowed(DataAllowFailReason failureReason) {
        return isDataAllowed(failureReason, false, false);
    }

    protected boolean isDataAllowedByApnContext(ApnContext apnContext) {
        return this.mReference.isDataAllowedByApnContext(apnContext);
    }

    protected boolean isDataAllowedByApnType(DataAllowFailReason failureReason, String apnType) {
        return this.mReference.isDataAllowedByApnType(failureReason, apnType);
    }

    protected boolean getAnyDataEnabledByApnContext(ApnContext apnContext, boolean enable) {
        return this.mReference.getAnyDataEnabledByApnContext(apnContext, enable);
    }

    protected boolean isDataAllowedForRoaming(boolean isMms) {
        return this.mReference.isDataAllowedForRoaming(isMms);
    }

    protected void onAllApnFirstActiveFailed() {
        this.mReference.onAllApnFirstActiveFailed();
    }

    protected void onAllApnPermActiveFailed() {
        this.mReference.onAllApnPermActiveFailed();
    }

    protected void setFirstTimeEnableData() {
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

    protected boolean needRemovedPreferredApn() {
        return this.mReference.needRemovedPreferredApn();
    }

    protected boolean isBipApnType(String type) {
        return this.mReference.isBipApnType(type);
    }

    protected ApnSetting fetchBipApn(ApnSetting preferredApn, ArrayList<ApnSetting> allApnSettings) {
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

    protected void setEnabled(int id, boolean enable) {
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

    protected int get4gSlot() {
        return this.mReference.get4gSlot();
    }

    protected int get2gSlot() {
        return this.mReference.get2gSlot();
    }

    public void addIfacePhoneHashMap(DcAsyncChannel dcac, HashMap<String, Integer> mIfacePhoneHashMap) {
        this.mReference.addIfacePhoneHashMap(dcac, mIfacePhoneHashMap);
    }

    protected int getVSimSubId() {
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
}
