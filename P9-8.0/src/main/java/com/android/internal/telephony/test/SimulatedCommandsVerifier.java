package com.android.internal.telephony.test;

import android.os.Handler;
import android.os.Message;
import android.service.carrier.CarrierIdentifier;
import com.android.internal.telephony.AbstractBaseCommands;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.RadioCapability;
import com.android.internal.telephony.UUSInfo;
import com.android.internal.telephony.cdma.CdmaSmsBroadcastConfigInfo;
import com.android.internal.telephony.dataconnection.DataProfile;
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
import java.util.List;

public class SimulatedCommandsVerifier extends AbstractBaseCommands implements CommandsInterface {
    private static SimulatedCommandsVerifier sInstance;

    private SimulatedCommandsVerifier() {
    }

    public static SimulatedCommandsVerifier getInstance() {
        if (sInstance == null) {
            sInstance = new SimulatedCommandsVerifier();
        }
        return sInstance;
    }

    public RadioState getRadioState() {
        return null;
    }

    public void getImsRegistrationState(Message result) {
    }

    public void registerForRadioStateChanged(Handler h, int what, Object obj) {
    }

    public void unregisterForRadioStateChanged(Handler h) {
    }

    public void registerForVoiceRadioTechChanged(Handler h, int what, Object obj) {
    }

    public void unregisterForVoiceRadioTechChanged(Handler h) {
    }

    public void registerForImsNetworkStateChanged(Handler h, int what, Object obj) {
    }

    public void unregisterForImsNetworkStateChanged(Handler h) {
    }

    public void registerForOn(Handler h, int what, Object obj) {
    }

    public void unregisterForOn(Handler h) {
    }

    public void registerForAvailable(Handler h, int what, Object obj) {
    }

    public void unregisterForAvailable(Handler h) {
    }

    public void registerForNotAvailable(Handler h, int what, Object obj) {
    }

    public void unregisterForNotAvailable(Handler h) {
    }

    public void registerForOffOrNotAvailable(Handler h, int what, Object obj) {
    }

    public void unregisterForOffOrNotAvailable(Handler h) {
    }

    public void registerForIccStatusChanged(Handler h, int what, Object obj) {
    }

    public void unregisterForIccStatusChanged(Handler h) {
    }

    public void registerForCallStateChanged(Handler h, int what, Object obj) {
    }

    public void unregisterForCallStateChanged(Handler h) {
    }

    public void registerForNetworkStateChanged(Handler h, int what, Object obj) {
    }

    public void unregisterForNetworkStateChanged(Handler h) {
    }

    public void registerForDataCallListChanged(Handler h, int what, Object obj) {
    }

    public void unregisterForDataCallListChanged(Handler h) {
    }

    public void registerForInCallVoicePrivacyOn(Handler h, int what, Object obj) {
    }

    public void unregisterForInCallVoicePrivacyOn(Handler h) {
    }

    public void registerForInCallVoicePrivacyOff(Handler h, int what, Object obj) {
    }

    public void unregisterForInCallVoicePrivacyOff(Handler h) {
    }

    public void registerForSrvccStateChanged(Handler h, int what, Object obj) {
    }

    public void unregisterForSrvccStateChanged(Handler h) {
    }

    public void registerForRSrvccStateChanged(Handler h, int what, Object obj) {
    }

    public void unregisterForRSrvccStateChanged(Handler h) {
    }

    public void registerForSubscriptionStatusChanged(Handler h, int what, Object obj) {
    }

    public void unregisterForSubscriptionStatusChanged(Handler h) {
    }

    public void registerForHardwareConfigChanged(Handler h, int what, Object obj) {
    }

    public void unregisterForHardwareConfigChanged(Handler h) {
    }

    public void setOnNewGsmSms(Handler h, int what, Object obj) {
    }

    public void unSetOnNewGsmSms(Handler h) {
    }

    public void setOnNewCdmaSms(Handler h, int what, Object obj) {
    }

    public void unSetOnNewCdmaSms(Handler h) {
    }

    public void setOnNewGsmBroadcastSms(Handler h, int what, Object obj) {
    }

    public void unSetOnNewGsmBroadcastSms(Handler h) {
    }

    public void setOnSmsOnSim(Handler h, int what, Object obj) {
    }

    public void unSetOnSmsOnSim(Handler h) {
    }

    public void setOnSmsStatus(Handler h, int what, Object obj) {
    }

    public void unSetOnSmsStatus(Handler h) {
    }

    public void setOnNITZTime(Handler h, int what, Object obj) {
    }

    public void unSetOnNITZTime(Handler h) {
    }

    public void setOnUSSD(Handler h, int what, Object obj) {
    }

    public void unSetOnUSSD(Handler h) {
    }

    public void setOnSignalStrengthUpdate(Handler h, int what, Object obj) {
    }

    public void unSetOnSignalStrengthUpdate(Handler h) {
    }

    public void setOnIccSmsFull(Handler h, int what, Object obj) {
    }

    public void unSetOnIccSmsFull(Handler h) {
    }

    public void registerForIccRefresh(Handler h, int what, Object obj) {
    }

    public void unregisterForIccRefresh(Handler h) {
    }

    public void setOnIccRefresh(Handler h, int what, Object obj) {
    }

    public void unsetOnIccRefresh(Handler h) {
    }

    public void setOnCallRing(Handler h, int what, Object obj) {
    }

    public void unSetOnCallRing(Handler h) {
    }

    public void setOnRestrictedStateChanged(Handler h, int what, Object obj) {
    }

    public void unSetOnRestrictedStateChanged(Handler h) {
    }

    public void setOnSuppServiceNotification(Handler h, int what, Object obj) {
    }

    public void unSetOnSuppServiceNotification(Handler h) {
    }

    public void setOnCatSessionEnd(Handler h, int what, Object obj) {
    }

    public void unSetOnCatSessionEnd(Handler h) {
    }

    public void setOnCatProactiveCmd(Handler h, int what, Object obj) {
    }

    public void unSetOnCatProactiveCmd(Handler h) {
    }

    public void setOnCatEvent(Handler h, int what, Object obj) {
    }

    public void unSetOnCatEvent(Handler h) {
    }

    public void setOnCatCallSetUp(Handler h, int what, Object obj) {
    }

    public void unSetOnCatCallSetUp(Handler h) {
    }

    public void setSuppServiceNotifications(boolean enable, Message result) {
    }

    public void setOnCatCcAlphaNotify(Handler h, int what, Object obj) {
    }

    public void unSetOnCatCcAlphaNotify(Handler h) {
    }

    public void setOnSs(Handler h, int what, Object obj) {
    }

    public void unSetOnSs(Handler h) {
    }

    public void registerForDisplayInfo(Handler h, int what, Object obj) {
    }

    public void unregisterForDisplayInfo(Handler h) {
    }

    public void registerForCallWaitingInfo(Handler h, int what, Object obj) {
    }

    public void unregisterForCallWaitingInfo(Handler h) {
    }

    public void registerForSignalInfo(Handler h, int what, Object obj) {
    }

    public void unregisterForSignalInfo(Handler h) {
    }

    public void registerForNumberInfo(Handler h, int what, Object obj) {
    }

    public void unregisterForNumberInfo(Handler h) {
    }

    public void registerForRedirectedNumberInfo(Handler h, int what, Object obj) {
    }

    public void unregisterForRedirectedNumberInfo(Handler h) {
    }

    public void registerForLineControlInfo(Handler h, int what, Object obj) {
    }

    public void unregisterForLineControlInfo(Handler h) {
    }

    public void registerFoT53ClirlInfo(Handler h, int what, Object obj) {
    }

    public void unregisterForT53ClirInfo(Handler h) {
    }

    public void registerForT53AudioControlInfo(Handler h, int what, Object obj) {
    }

    public void unregisterForT53AudioControlInfo(Handler h) {
    }

    public void setEmergencyCallbackMode(Handler h, int what, Object obj) {
    }

    public void registerForCdmaOtaProvision(Handler h, int what, Object obj) {
    }

    public void unregisterForCdmaOtaProvision(Handler h) {
    }

    public void registerForRingbackTone(Handler h, int what, Object obj) {
    }

    public void unregisterForRingbackTone(Handler h) {
    }

    public void registerForResendIncallMute(Handler h, int what, Object obj) {
    }

    public void unregisterForResendIncallMute(Handler h) {
    }

    public void registerForCdmaSubscriptionChanged(Handler h, int what, Object obj) {
    }

    public void unregisterForCdmaSubscriptionChanged(Handler h) {
    }

    public void registerForCdmaPrlChanged(Handler h, int what, Object obj) {
    }

    public void unregisterForCdmaPrlChanged(Handler h) {
    }

    public void registerForExitEmergencyCallbackMode(Handler h, int what, Object obj) {
    }

    public void unregisterForExitEmergencyCallbackMode(Handler h) {
    }

    public void registerForRilConnected(Handler h, int what, Object obj) {
    }

    public void unregisterForRilConnected(Handler h) {
    }

    public void supplyIccPin(String pin, Message result) {
    }

    public void supplyIccPinForApp(String pin, String aid, Message result) {
    }

    public void supplyIccPuk(String puk, String newPin, Message result) {
    }

    public void supplyIccPukForApp(String puk, String newPin, String aid, Message result) {
    }

    public void supplyIccPin2(String pin2, Message result) {
    }

    public void supplyIccPin2ForApp(String pin2, String aid, Message result) {
    }

    public void supplyIccPuk2(String puk2, String newPin2, Message result) {
    }

    public void supplyIccPuk2ForApp(String puk2, String newPin2, String aid, Message result) {
    }

    public void changeIccPin(String oldPin, String newPin, Message result) {
    }

    public void changeIccPinForApp(String oldPin, String newPin, String aidPtr, Message result) {
    }

    public void changeIccPin2(String oldPin2, String newPin2, Message result) {
    }

    public void changeIccPin2ForApp(String oldPin2, String newPin2, String aidPtr, Message result) {
    }

    public void changeBarringPassword(String facility, String oldPwd, String newPwd, Message result) {
    }

    public void supplyNetworkDepersonalization(String netpin, Message result) {
    }

    public void getCurrentCalls(Message result) {
    }

    public void getPDPContextList(Message result) {
    }

    public void getDataCallList(Message result) {
    }

    public void dial(String address, int clirMode, Message result) {
    }

    public void dial(String address, int clirMode, UUSInfo uusInfo, Message result) {
    }

    public void getIMSI(Message result) {
    }

    public void getIMSIForApp(String aid, Message result) {
    }

    public void getIMEI(Message result) {
    }

    public void getIMEISV(Message result) {
    }

    public void hangupConnection(int gsmIndex, Message result) {
    }

    public void hangupWaitingOrBackground(Message result) {
    }

    public void hangupForegroundResumeBackground(Message result) {
    }

    public void switchWaitingOrHoldingAndActive(Message result) {
    }

    public void conference(Message result) {
    }

    public void setPreferredVoicePrivacy(boolean enable, Message result) {
    }

    public void getPreferredVoicePrivacy(Message result) {
    }

    public void separateConnection(int gsmIndex, Message result) {
    }

    public void acceptCall(Message result) {
    }

    public void rejectCall(Message result) {
    }

    public void explicitCallTransfer(Message result) {
    }

    public void getLastCallFailCause(Message result) {
    }

    public void getLastPdpFailCause(Message result) {
    }

    public void getLastDataCallFailCause(Message result) {
    }

    public void setMute(boolean enableMute, Message response) {
    }

    public void getMute(Message response) {
    }

    public void getSignalStrength(Message response) {
    }

    public void getVoiceRegistrationState(Message response) {
    }

    public void getDataRegistrationState(Message response) {
    }

    public void getOperator(Message response) {
    }

    public void sendDtmf(char c, Message result) {
    }

    public void startDtmf(char c, Message result) {
    }

    public void stopDtmf(Message result) {
    }

    public void sendBurstDtmf(String dtmfString, int on, int off, Message result) {
    }

    public void sendSMS(String smscPDU, String pdu, Message response) {
    }

    public void sendSMSExpectMore(String smscPDU, String pdu, Message response) {
    }

    public void sendCdmaSms(byte[] pdu, Message response) {
    }

    public void sendImsGsmSms(String smscPDU, String pdu, int retry, int messageRef, Message response) {
    }

    public void sendImsCdmaSms(byte[] pdu, int retry, int messageRef, Message response) {
    }

    public void deleteSmsOnSim(int index, Message response) {
    }

    public void deleteSmsOnRuim(int index, Message response) {
    }

    public void writeSmsToSim(int status, String smsc, String pdu, Message response) {
    }

    public void writeSmsToRuim(int status, String pdu, Message response) {
    }

    public void setRadioPower(boolean on, Message response) {
    }

    public void acknowledgeLastIncomingGsmSms(boolean success, int cause, Message response) {
    }

    public void acknowledgeLastIncomingCdmaSms(boolean success, int cause, Message response) {
    }

    public void acknowledgeIncomingGsmSmsWithPdu(boolean success, String ackPdu, Message response) {
    }

    public void iccIO(int command, int fileid, String path, int p1, int p2, int p3, String data, String pin2, Message response) {
    }

    public void iccIOForApp(int command, int fileid, String path, int p1, int p2, int p3, String data, String pin2, String aid, Message response) {
    }

    public void queryCLIP(Message response) {
    }

    public void getCLIR(Message response) {
    }

    public void setCLIR(int clirMode, Message response) {
    }

    public void queryCallWaiting(int serviceClass, Message response) {
    }

    public void setCallWaiting(boolean enable, int serviceClass, Message response) {
    }

    public void setCallForward(int action, int cfReason, int serviceClass, String number, int timeSeconds, Message response) {
    }

    public void queryCallForwardStatus(int cfReason, int serviceClass, String number, Message response) {
    }

    public void setNetworkSelectionModeAutomatic(Message response) {
    }

    public void setNetworkSelectionModeManual(String operatorNumeric, Message response) {
    }

    public void getNetworkSelectionMode(Message response) {
    }

    public void getAvailableNetworks(Message response) {
    }

    public void getBasebandVersion(Message response) {
    }

    public void queryFacilityLock(String facility, String password, int serviceClass, Message response) {
    }

    public void queryFacilityLockForApp(String facility, String password, int serviceClass, String appId, Message response) {
    }

    public void setFacilityLock(String facility, boolean lockState, String password, int serviceClass, Message response) {
    }

    public void setFacilityLockForApp(String facility, boolean lockState, String password, int serviceClass, String appId, Message response) {
    }

    public void sendUSSD(String ussdString, Message response) {
    }

    public void cancelPendingUssd(Message response) {
    }

    public void resetRadio(Message result) {
    }

    public void setBandMode(int bandMode, Message response) {
    }

    public void queryAvailableBandMode(Message response) {
    }

    public void setPreferredNetworkType(int networkType, Message response) {
    }

    public void getPreferredNetworkType(Message response) {
    }

    public void setLocationUpdates(boolean enable, Message response) {
    }

    public void getSmscAddress(Message result) {
    }

    public void setSmscAddress(String address, Message result) {
    }

    public void reportSmsMemoryStatus(boolean available, Message result) {
    }

    public void reportStkServiceIsRunning(Message result) {
    }

    public void invokeOemRilRequestRaw(byte[] data, Message response) {
    }

    public void invokeOemRilRequestStrings(String[] strings, Message response) {
    }

    public void setOnUnsolOemHookRaw(Handler h, int what, Object obj) {
    }

    public void unSetOnUnsolOemHookRaw(Handler h) {
    }

    public void sendTerminalResponse(String contents, Message response) {
    }

    public void sendEnvelope(String contents, Message response) {
    }

    public void sendEnvelopeWithStatus(String contents, Message response) {
    }

    public void handleCallSetupRequestFromSim(boolean accept, Message response) {
    }

    public void setGsmBroadcastActivation(boolean activate, Message result) {
    }

    public void setGsmBroadcastConfig(SmsBroadcastConfigInfo[] config, Message response) {
    }

    public void getGsmBroadcastConfig(Message response) {
    }

    public void getDeviceIdentity(Message response) {
    }

    public void getCDMASubscription(Message response) {
    }

    public void sendCDMAFeatureCode(String FeatureCode, Message response) {
    }

    public void setPhoneType(int phoneType) {
    }

    public void queryCdmaRoamingPreference(Message response) {
    }

    public void setCdmaRoamingPreference(int cdmaRoamingType, Message response) {
    }

    public void setCdmaSubscriptionSource(int cdmaSubscriptionType, Message response) {
    }

    public void getCdmaSubscriptionSource(Message response) {
    }

    public void setTTYMode(int ttyMode, Message response) {
    }

    public void queryTTYMode(Message response) {
    }

    public void setupDataCall(int radioTechnology, DataProfile dataProfile, boolean isRoaming, boolean allowRoaming, Message result) {
    }

    public void deactivateDataCall(int cid, int reason, Message result) {
    }

    public void setCdmaBroadcastActivation(boolean activate, Message result) {
    }

    public void setCdmaBroadcastConfig(CdmaSmsBroadcastConfigInfo[] configs, Message response) {
    }

    public void getCdmaBroadcastConfig(Message result) {
    }

    public void exitEmergencyCallbackMode(Message response) {
    }

    public void getIccCardStatus(Message result) {
    }

    public int getLteOnCdmaMode() {
        return 0;
    }

    public void requestIsimAuthentication(String nonce, Message response) {
    }

    public void requestIccSimAuthentication(int authContext, String data, String aid, Message response) {
    }

    public void getVoiceRadioTechnology(Message result) {
    }

    public void registerForCellInfoList(Handler h, int what, Object obj) {
    }

    public void unregisterForCellInfoList(Handler h) {
    }

    public void setInitialAttachApn(DataProfile dataProfile, boolean isRoaming, Message result) {
    }

    public void setDataProfile(DataProfile[] dps, boolean isRoaming, Message result) {
    }

    public void testingEmergencyCall() {
    }

    public void iccOpenLogicalChannel(String AID, int p2, Message response) {
    }

    public void iccOpenLogicalChannel(String AID, byte p2, Message response) {
    }

    public void iccCloseLogicalChannel(int channel, Message response) {
    }

    public void iccTransmitApduLogicalChannel(int channel, int cla, int instruction, int p1, int p2, int p3, String data, Message response) {
    }

    public void iccTransmitApduBasicChannel(int cla, int instruction, int p1, int p2, int p3, String data, Message response) {
    }

    public void nvReadItem(int itemID, Message response) {
    }

    public void nvWriteItem(int itemID, String itemValue, Message response) {
    }

    public void nvWriteCdmaPrl(byte[] preferredRoamingList, Message response) {
    }

    public void nvResetConfig(int resetType, Message response) {
    }

    public void getHardwareConfig(Message result) {
    }

    public int getRilVersion() {
        return 0;
    }

    public void setUiccSubscription(int slotId, int appIndex, int subId, int subStatus, Message result) {
    }

    public void setDataAllowed(boolean allowed, Message result) {
    }

    public void requestShutdown(Message result) {
    }

    public void setRadioCapability(RadioCapability rc, Message result) {
    }

    public void getRadioCapability(Message result) {
    }

    public void registerForRadioCapabilityChanged(Handler h, int what, Object obj) {
    }

    public void unregisterForRadioCapabilityChanged(Handler h) {
    }

    public void startLceService(int reportIntervalMs, boolean pullMode, Message result) {
    }

    public void stopLceService(Message result) {
    }

    public void pullLceData(Message result) {
    }

    public void registerForLceInfo(Handler h, int what, Object obj) {
    }

    public void unregisterForLceInfo(Handler h) {
    }

    public void getModemActivityInfo(Message result) {
    }

    public void setAllowedCarriers(List<CarrierIdentifier> list, Message result) {
    }

    public void getAllowedCarriers(Message result) {
    }

    public void registerForPcoData(Handler h, int what, Object obj) {
    }

    public void unregisterForPcoData(Handler h) {
    }

    public void sendDeviceState(int stateType, boolean state, Message result) {
    }

    public void setUnsolResponseFilter(int filter, Message result) {
    }

    public void setSimCardPower(boolean powerUp, Message result) {
    }
}
