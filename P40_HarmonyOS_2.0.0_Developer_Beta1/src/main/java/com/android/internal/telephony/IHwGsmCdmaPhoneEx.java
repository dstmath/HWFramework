package com.android.internal.telephony;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.huawei.internal.telephony.ImsExceptionExt;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.uicc.UiccCardApplicationEx;

public interface IHwGsmCdmaPhoneEx {
    public static final int NO_VALUES = -1;

    default void setCallForwardingOption(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int serviceClass, int timerSeconds, Message onComplete) {
    }

    default boolean dialInternalForCdmaLte(String newDialString) {
        return false;
    }

    default void autoExitEmergencyCallbackMode() {
    }

    default void restoreSavedRadioTech() {
    }

    default boolean isDialCsFallback(Exception exception) {
        return true;
    }

    default Message getCustAvailableNetworksMessage(Message response) {
        return response;
    }

    default String getDefaultVoiceMailAlphaTagText(Context mContext, String ret) {
        return ret;
    }

    default void updateReduceSARState() {
    }

    default void resetReduceSARPowerGrade() {
    }

    default String getHwCdmaPrlVersion() {
        return ProxyController.MODEM_0;
    }

    default String getHwCdmaEsn() {
        return ProxyController.MODEM_0;
    }

    default String getVMNumberWhenIMSIChange() {
        return PhoneConfigurationManager.SSSS;
    }

    default void registerForCsgRecordsLoadedEvent() {
    }

    default void unregisterForCsgRecordsLoadedEvent() {
    }

    default boolean isUssdOkForRelease() {
        return false;
    }

    default void riseCdmaCutoffFreq(boolean on) {
    }

    default String getCdmaMlplVersion(String mlplVersion) {
        return mlplVersion;
    }

    default String getCdmaMsplVersion(String msplVersion) {
        return msplVersion;
    }

    default void getCallbarringOption(String facility, String serviceClass, Message response) {
    }

    default void setCallbarringOption(String facility, String password, boolean isActivate, String serviceClass, Message response) {
    }

    default void getCallbarringOption(String facility, int serviceClass, Message response) {
    }

    default void setCallbarringOption(String facility, String password, boolean isActivate, int serviceClass, Message response) {
    }

    default void changeBarringPassword(String oldPassword, String newPassword, Message response) {
    }

    default boolean isSupportCFT() {
        return false;
    }

    default void setCallForwardingUncondTimerOption(int startHour, int startMinute, int endHour, int endMinute, int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, Message onComplete) {
    }

    default boolean getImsSwitch() {
        return false;
    }

    default void setImsSwitch(boolean on) {
    }

    default void cleanDeviceId() {
    }

    default String processPlusSymbol(String dialNumber, String imsi) {
        return dialNumber;
    }

    default boolean beforeHandleMessage(Message msg) {
        return msg != null && msg.what >= 100;
    }

    default void processEccNumber(IServiceStateTrackerInner sST) {
    }

    default void logForImei(String phoneType, String imei) {
    }

    default void afterHandleMessage(Message msg) {
    }

    default void globalEccCustom(String operatorNumeric) {
    }

    default void dispose() {
    }

    default void setNetworkSelectionModeAutomatic(Message response) {
    }

    default boolean isChinaTelecom(int slotId) {
        return false;
    }

    default void selectNetworkManually(Message response) {
    }

    default boolean isCTSimCard(int slotId) {
        return false;
    }

    default void updateWfcMode(Context context, boolean roaming, int subId) throws ImsExceptionExt {
        throw new ImsExceptionExt();
    }

    default boolean isDualImsAvailable() {
        return false;
    }

    default void judgeToLaunchCsgPeriodicSearchTimer() {
    }

    default void logForTest(String operationName, String content) {
    }

    default String getPesn() {
        return null;
    }

    default void closeRrc() {
    }

    default void switchVoiceCallBackgroundState(int state) {
    }

    default void getPOLCapabilty(Message response) {
    }

    default void getPreferedOperatorList(Message response) {
    }

    default void setPOLEntry(int index, String numeric, int nAct, Message response) {
    }

    default void setLTEReleaseVersion(int state, Message response) {
    }

    default int getLteReleaseVersion() {
        return -1;
    }

    default boolean setISMCOEX(String setISMCoex) {
        return false;
    }

    default void setImsDomainConfig(int domainType) {
    }

    default void getImsDomain(Message response) {
    }

    default void handleUiccAuth(int authType, byte[] rand, byte[] auth, Message response) {
    }

    default void handleMapconImsaReq(byte[] msg) {
    }

    default void registerForHWBuffer(Handler h, int what, Object obj) {
    }

    default void unregisterForHWBuffer(Handler h) {
    }

    default void sendHWSolicited(Message reqMsg, int event, byte[] reqData) {
    }

    default boolean cmdForECInfo(int event, int action, byte[] buf) {
        return false;
    }

    default void registerForCallAltSrv(Handler h, int what, Object obj) {
    }

    default void unregisterForCallAltSrv(Handler h) {
    }

    default void notifyCellularCommParaReady(int paratype, int pathtype, Message response) {
    }

    default boolean isMmiCode(String dialString, UiccCardApplicationEx app) {
        return false;
    }

    default void registerForLineControlInfo(Handler h, int what, Object obj) {
    }

    default void unregisterForLineControlInfo(Handler h) {
    }

    default String getMeid() {
        return "Meid";
    }

    default void selectCsgNetworkManually(Message response) {
    }

    default String getCdmaVoiceMailNumberHwCust(Context context, String line1Number, int phoneId) {
        return null;
    }

    default void initHwTimeZoneUpdater(Context context) {
    }

    default int removeUssdCust(PhoneExt phone) {
        return -1;
    }

    default boolean isShortCodeHw(String dialString, PhoneExt phone) {
        return false;
    }

    default void startUploadAvailableNetworks(Object obj) {
    }

    default void stopUploadAvailableNetworks() {
    }
}
