package com.android.internal.telephony.dataconnection;

import android.os.Bundle;
import android.os.Message;
import android.telephony.data.ApnSetting;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.huawei.internal.telephony.dataconnection.ApnContextEx;
import com.huawei.internal.telephony.dataconnection.DataConnectionEx;
import com.huawei.internal.telephony.dataconnection.DcFailCauseExt;
import com.huawei.internal.telephony.uicc.IccRecordsEx;
import com.huawei.internal.telephony.uicc.UiccCardApplicationEx;
import huawei.net.NetworkRequestExt;
import huawei.telephony.data.DataProfileEx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface IHwDcTrackerEx {
    public static final int AID_DNS = 1051;
    public static final int APN_PRESET = 1;
    public static final String DB_PRESET = "visible";
    public static final int ESM_FLAG_CUSTOMIZED = 1;
    public static final int ESM_FLAG_NOT_CUSTOMIZED = 0;
    public static final int ESM_FLAG_ONE = 1;
    public static final int ESM_FLAG_ZERO = 0;
    public static final int OEM_DO_RECOVERY_CMD_EXCUTE = 1;
    public static final int OEM_DO_RECOVERY_CMD_RESET = 0;
    public static final int RETRY_ACTION_CONTINUE = 1;
    public static final int RETRY_ACTION_CYCLE = 2;
    public static final int RETRY_ACTION_DISABLE_NR = 3;
    public static final int RETRY_ACTION_IMEDIATE_REEST = 4;
    public static final int RETRY_ACTION_NO_ACTION = 0;
    public static final int TYPE_RX_PACKETS = 1;
    public static final int TYPE_TX_PACKETS = 3;
    public static final int VP_END = 0;
    public static final int VP_START = 1;

    default void beforeHandleMessage(Message msg) {
    }

    default boolean isDataAllowedByApnContext(ApnContextEx apnContext) {
        return true;
    }

    default boolean getAnyDataEnabledByApnContext(ApnContextEx apnContext, boolean enable) {
        return false;
    }

    default boolean isDataAllowedForRoaming(boolean isMms) {
        return false;
    }

    default String getDataRoamingSettingItem(String originItem) {
        return originItem;
    }

    default void init() {
    }

    default boolean isBipApnType(String type) {
        if (HuaweiTelephonyConfigs.isModemBipEnable()) {
            return false;
        }
        char c = 65535;
        switch (type.hashCode()) {
            case 3023943:
                if (type.equals("bip0")) {
                    c = 0;
                    break;
                }
                break;
            case 3023944:
                if (type.equals("bip1")) {
                    c = 1;
                    break;
                }
                break;
            case 3023945:
                if (type.equals("bip2")) {
                    c = 2;
                    break;
                }
                break;
            case 3023946:
                if (type.equals("bip3")) {
                    c = 3;
                    break;
                }
                break;
            case 3023947:
                if (type.equals("bip4")) {
                    c = 4;
                    break;
                }
                break;
            case 3023948:
                if (type.equals("bip5")) {
                    c = 5;
                    break;
                }
                break;
            case 3023949:
                if (type.equals("bip6")) {
                    c = 6;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return true;
            default:
                return false;
        }
    }

    default ApnSetting fetchBipApn(ApnSetting preferredApn, List<ApnSetting> list) {
        return null;
    }

    default void dispose() {
    }

    default boolean noNeedDoRecovery() {
        return false;
    }

    default boolean isApnPreset(ApnSetting apnSetting) {
        if (apnSetting != null) {
            return apnSetting.isPreset();
        }
        return false;
    }

    default void setupDataOnConnectableApns(String reason, String excludedApnType) {
    }

    default boolean isCTSimCard(int slotId) {
        return false;
    }

    default boolean isPingOk() {
        return false;
    }

    default void unregisterForImsiReady(IccRecordsEx r) {
    }

    default void unregisterForRecordsLoaded(IccRecordsEx r) {
    }

    default void unregisterForGetAdDone(UiccCardApplicationEx newUiccApplication) {
    }

    default void registerForImsiReady(IccRecordsEx r) {
    }

    default void registerForRecordsLoaded(IccRecordsEx r) {
    }

    default void registerForGetAdDone(UiccCardApplicationEx newUiccApplication) {
    }

    default void registerForImsi(UiccCardApplicationEx newUiccApplication, IccRecordsEx r) {
    }

    default boolean checkMvnoParams() {
        return false;
    }

    default void handleCustMessage(Message msg) {
    }

    default int getPrimarySlot() {
        return 0;
    }

    default void addIfacePhoneHashMap(DataConnectionEx dc, HashMap<String, Integer> hashMap) {
    }

    default void sendDSMipErrorBroadcast() {
    }

    default boolean enableTcpUdpSumForDataStall() {
        return false;
    }

    default String networkTypeToApnType(int networkType) {
        return PhoneConfigurationManager.SSSS;
    }

    default boolean isDataConnectivityDisabled(int slotId, String tag) {
        return false;
    }

    default boolean isRoamingPushDisabled() {
        return false;
    }

    default boolean getXcapDataRoamingEnable() {
        return false;
    }

    default boolean isWifiConnected() {
        return false;
    }

    default boolean isDataNeededWithWifiAndBt() {
        return true;
    }

    default void updateLastRadioResetTimestamp() {
    }

    default boolean needRestartRadioOnError(ApnContextEx apnContext, DcFailCauseExt cause) {
        return false;
    }

    default void updateDSUseDuration() {
    }

    default boolean getAttachedStatus(boolean attached) {
        return attached;
    }

    default void updateApnLists(String requestedApnType, int radioTech, ArrayList<ApnSetting> arrayList, String operator) {
    }

    default ApnSetting getAttachedApnSetting() {
        return null;
    }

    default void setAttachedApnSetting(ApnSetting apnSetting) {
    }

    default void startPdpResetAlarm(int delay) {
    }

    default void stopPdpResetAlarm() {
    }

    default boolean isCTLteNetwork() {
        return false;
    }

    default ApnSetting getApnForCT() {
        return null;
    }

    default void updateApnId() {
    }

    default boolean needSetCTProxy(ApnSetting apn) {
        return false;
    }

    default void setCtProxy(DataConnectionEx dc) {
    }

    default boolean isSupportLTE(ApnSetting apnSetting) {
        return false;
    }

    default ArrayList<ApnSetting> buildWaitingApnsForCTSupl(String requestedApnType, int radioTech) {
        return new ArrayList<>(0);
    }

    default void updateApnContextState() {
    }

    default UiccCardApplicationEx getUiccCardApplication(int phoneId, int appFamily) {
        UiccCardApplication app;
        UiccCardApplicationEx ex = new UiccCardApplicationEx();
        if (VSimUtilsInner.isVSimSub(phoneId)) {
            app = VSimUtilsInner.getVSimUiccCardApplication(appFamily);
        } else {
            app = UiccController.getInstance().getUiccCardApplication(phoneId, appFamily);
        }
        ex.setUiccCardApplication(app);
        return ex;
    }

    default void updateForVSim() {
    }

    default boolean isDisconnectedOrConnecting() {
        return false;
    }

    default void setupDataForSinglePdnArbitration(String reason) {
    }

    default boolean isNeedFilterVowifiMms(ApnSetting apn, String requestedApnType) {
        return false;
    }

    default boolean isBlockSetInitialAttachApn() {
        return false;
    }

    default boolean isNeedForceSetup(ApnContextEx apnContext) {
        return false;
    }

    default void clearDefaultLink() {
    }

    default void resumeDefaultLink() {
    }

    default String getCTOperator(String operator) {
        return operator;
    }

    default String getOperatorNumeric() {
        return PhoneConfigurationManager.SSSS;
    }

    default boolean isNRNetwork() {
        return false;
    }

    default void notifyGetTcpSumMsgReportToBooster(int tag) {
    }

    default long getDnsUidPackets(int uid, int type) {
        return 0;
    }

    default long getTcpRxPktSum() {
        return 0;
    }

    default long getTcpTxPktSum() {
        return 0;
    }

    default void correctApnAuthType(List<ApnSetting> list) {
    }

    default void updateCustomizedEsmFlagState(int defaultEsmFlag, int isEsmFlagCustomized) {
    }

    default void checkOnlyIpv6Cure(ApnContextEx apn) {
    }

    default boolean isNeedDataCure(int cause, ApnContextEx apn) {
        return false;
    }

    default void startAlarmForReenableNr(ApnContextEx apnContext, long delay) {
    }

    default void sendDisableNr(ApnContextEx apnContext, long delay) {
    }

    default void updateDataRetryStategy(ApnContextEx apnContext) {
    }

    default int getDataRetryAction(ApnContextEx apnContext) {
        return 0;
    }

    default long getDataRetryDelay(long orginDelay, ApnContextEx apnContext) {
        return orginDelay;
    }

    default void updateDataCureProtocol(ApnContextEx apn) {
    }

    default void setEsmFlag(int esmInfo) {
    }

    default int getDataCureEsmFlag(String operator) {
        return 0;
    }

    default void resetDataCureInfo(String reason) {
    }

    default ApnSetting getRegApnForCure(ApnSetting apnSetting) {
        return null;
    }

    default int notifyBoosterDoRecovery(int event) {
        return -1;
    }

    default void updateRecoveryPktStat() {
    }

    default void onVpEnded() {
    }

    default boolean needRetryAfterDisconnected(int cause) {
        return false;
    }

    default ApnContextEx getNrSliceApnContext(NetworkRequestExt networkRequest) {
        return null;
    }

    default void putApnContextFor5GSlice(int sliceIndex, ApnContextEx apnContext) {
    }

    default ApnContextEx getApnContextFor5GSlice(int sliceIndex) {
        return null;
    }

    default boolean hasMatchAllSlice() {
        return false;
    }

    default ApnSetting createSliceApnSetting(ApnSetting apn) {
        return null;
    }

    static default DataProfileEx createDataProfile(ApnSetting apn, int profileId, boolean isPreferred, Bundle extraData) {
        DataProfileEx dataProfileEx = new DataProfileEx();
        dataProfileEx.createDataProfile(apn, profileId, isPreferred, extraData);
        return dataProfileEx;
    }

    default boolean isApnSettingsSimilar(ApnSetting first, ApnSetting second) {
        if (first == null || second == null) {
            return false;
        }
        return first.similar(second);
    }

    default void sendRestartRadioChr(int subId, int cause) {
    }
}
