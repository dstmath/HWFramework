package com.android.internal.telephony.test;

import android.net.KeepalivePacketData;
import android.net.LinkProperties;
import android.os.Handler;
import android.os.Message;
import android.telephony.ImsiEncryptionInfo;
import android.telephony.NetworkScanRequest;
import android.telephony.data.DataProfile;
import android.telephony.emergency.EmergencyNumber;
import com.android.internal.telephony.AbstractBaseCommands;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.RadioCapability;
import com.android.internal.telephony.UUSInfo;
import com.android.internal.telephony.cdma.CdmaSmsBroadcastConfigInfo;
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;

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

    @Override // com.android.internal.telephony.CommandsInterface
    public int getRadioState() {
        return 2;
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getImsRegistrationState(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForRadioStateChanged(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForRadioStateChanged(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForVoiceRadioTechChanged(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForVoiceRadioTechChanged(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForImsNetworkStateChanged(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForImsNetworkStateChanged(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForOn(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForOn(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForAvailable(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForAvailable(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForNotAvailable(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForNotAvailable(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForOffOrNotAvailable(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForOffOrNotAvailable(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForIccStatusChanged(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForIccStatusChanged(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForIccSlotStatusChanged(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForIccSlotStatusChanged(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForCallStateChanged(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForCallStateChanged(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForNetworkStateChanged(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForNetworkStateChanged(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForDataCallListChanged(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForDataCallListChanged(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForInCallVoicePrivacyOn(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForInCallVoicePrivacyOn(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForInCallVoicePrivacyOff(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForInCallVoicePrivacyOff(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForSrvccStateChanged(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForSrvccStateChanged(Handler h) {
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void registerForRSrvccStateChanged(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void unregisterForRSrvccStateChanged(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForSubscriptionStatusChanged(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForSubscriptionStatusChanged(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForHardwareConfigChanged(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForHardwareConfigChanged(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnNewGsmSms(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnNewGsmSms(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnNewCdmaSms(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnNewCdmaSms(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnNewGsmBroadcastSms(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnNewGsmBroadcastSms(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnSmsOnSim(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnSmsOnSim(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnSmsStatus(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnSmsStatus(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnNITZTime(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnNITZTime(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnUSSD(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnUSSD(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnSignalStrengthUpdate(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnSignalStrengthUpdate(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnIccSmsFull(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnIccSmsFull(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForIccRefresh(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForIccRefresh(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnIccRefresh(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unsetOnIccRefresh(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnCallRing(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnCallRing(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnRestrictedStateChanged(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnRestrictedStateChanged(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnSuppServiceNotification(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnSuppServiceNotification(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnCatSessionEnd(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnCatSessionEnd(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnCatProactiveCmd(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnCatProactiveCmd(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnCatEvent(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnCatEvent(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnCatCallSetUp(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnCatCallSetUp(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setSuppServiceNotifications(boolean enable, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnCatCcAlphaNotify(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnCatCcAlphaNotify(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnSs(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnSs(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForDisplayInfo(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForDisplayInfo(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForCallWaitingInfo(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForCallWaitingInfo(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForSignalInfo(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForSignalInfo(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForNumberInfo(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForNumberInfo(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForRedirectedNumberInfo(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForRedirectedNumberInfo(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForLineControlInfo(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForLineControlInfo(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerFoT53ClirlInfo(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForT53ClirInfo(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForT53AudioControlInfo(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForT53AudioControlInfo(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setEmergencyCallbackMode(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForCdmaOtaProvision(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForCdmaOtaProvision(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForRingbackTone(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForRingbackTone(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForResendIncallMute(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForResendIncallMute(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForCdmaSubscriptionChanged(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForCdmaSubscriptionChanged(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForCdmaPrlChanged(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForCdmaPrlChanged(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForExitEmergencyCallbackMode(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForExitEmergencyCallbackMode(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForRilConnected(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForRilConnected(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void supplyIccPin(String pin, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void supplyIccPinForApp(String pin, String aid, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void supplyIccPuk(String puk, String newPin, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void supplyIccPukForApp(String puk, String newPin, String aid, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void supplyIccPin2(String pin2, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void supplyIccPin2ForApp(String pin2, String aid, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void supplyIccPuk2(String puk2, String newPin2, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void supplyIccPuk2ForApp(String puk2, String newPin2, String aid, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void changeIccPin(String oldPin, String newPin, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void changeIccPinForApp(String oldPin, String newPin, String aidPtr, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void changeIccPin2(String oldPin2, String newPin2, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void changeIccPin2ForApp(String oldPin2, String newPin2, String aidPtr, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void changeBarringPassword(String facility, String oldPwd, String newPwd, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void supplyNetworkDepersonalization(String netpin, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getCurrentCalls(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getPDPContextList(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getDataCallList(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void dial(String address, boolean isEmergencyCall, EmergencyNumber emergencyNumberInfo, boolean hasKnownUserIntentEmergency, int clirMode, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void dial(String address, boolean isEmergencyCall, EmergencyNumber emergencyNumberInfo, boolean hasKnownUserIntentEmergency, int clirMode, UUSInfo uusInfo, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getIMSI(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getIMSIForApp(String aid, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getIMEI(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getIMEISV(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void hangupConnection(int gsmIndex, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void hangupWaitingOrBackground(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void hangupForegroundResumeBackground(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void switchWaitingOrHoldingAndActive(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void conference(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setPreferredVoicePrivacy(boolean enable, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getPreferredVoicePrivacy(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void separateConnection(int gsmIndex, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void acceptCall(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void rejectCall(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void explicitCallTransfer(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getLastCallFailCause(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getLastPdpFailCause(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getLastDataCallFailCause(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setMute(boolean enableMute, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getMute(Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getSignalStrength(Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getVoiceRegistrationState(Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getDataRegistrationState(Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getOperator(Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendDtmf(char c, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void startDtmf(char c, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void stopDtmf(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendBurstDtmf(String dtmfString, int on, int off, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendSMS(String smscPDU, String pdu, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendSMSExpectMore(String smscPDU, String pdu, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendCdmaSms(byte[] pdu, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendImsGsmSms(String smscPDU, String pdu, int retry, int messageRef, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendImsCdmaSms(byte[] pdu, int retry, int messageRef, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void deleteSmsOnSim(int index, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void deleteSmsOnRuim(int index, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void writeSmsToSim(int status, String smsc, String pdu, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void writeSmsToRuim(int status, String pdu, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setRadioPower(boolean on, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void acknowledgeLastIncomingGsmSms(boolean success, int cause, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void acknowledgeLastIncomingCdmaSms(boolean success, int cause, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void acknowledgeIncomingGsmSmsWithPdu(boolean success, String ackPdu, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void iccIO(int command, int fileid, String path, int p1, int p2, int p3, String data, String pin2, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void iccIOForApp(int command, int fileid, String path, int p1, int p2, int p3, String data, String pin2, String aid, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void queryCLIP(Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getCLIR(Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setCLIR(int clirMode, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void queryCallWaiting(int serviceClass, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setCallWaiting(boolean enable, int serviceClass, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setCallForward(int action, int cfReason, int serviceClass, String number, int timeSeconds, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void queryCallForwardStatus(int cfReason, int serviceClass, String number, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setNetworkSelectionModeAutomatic(Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setNetworkSelectionModeManual(String operatorNumeric, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getNetworkSelectionMode(Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getAvailableNetworks(Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void startNetworkScan(NetworkScanRequest nsr, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void stopNetworkScan(Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getBasebandVersion(Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void queryFacilityLock(String facility, String password, int serviceClass, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void queryFacilityLockForApp(String facility, String password, int serviceClass, String appId, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setFacilityLock(String facility, boolean lockState, String password, int serviceClass, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setFacilityLockForApp(String facility, boolean lockState, String password, int serviceClass, String appId, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendUSSD(String ussdString, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void cancelPendingUssd(Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void resetRadio(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setBandMode(int bandMode, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void queryAvailableBandMode(Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setPreferredNetworkType(int networkType, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getPreferredNetworkType(Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setLocationUpdates(boolean enable, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getSmscAddress(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setSmscAddress(String address, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void reportSmsMemoryStatus(boolean available, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void reportStkServiceIsRunning(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void invokeOemRilRequestRaw(byte[] data, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void invokeOemRilRequestStrings(String[] strings, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnUnsolOemHookRaw(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnUnsolOemHookRaw(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendTerminalResponse(String contents, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendEnvelope(String contents, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendEnvelopeWithStatus(String contents, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void handleCallSetupRequestFromSim(boolean accept, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setGsmBroadcastActivation(boolean activate, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setGsmBroadcastConfig(SmsBroadcastConfigInfo[] config, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getGsmBroadcastConfig(Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getDeviceIdentity(Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getCDMASubscription(Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendCDMAFeatureCode(String FeatureCode, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setPhoneType(int phoneType) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void queryCdmaRoamingPreference(Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setCdmaRoamingPreference(int cdmaRoamingType, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setCdmaSubscriptionSource(int cdmaSubscriptionType, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getCdmaSubscriptionSource(Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setTTYMode(int ttyMode, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void queryTTYMode(Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setupDataCall(int accessNetworkType, DataProfile dataProfile, boolean isRoaming, boolean allowRoaming, int reason, LinkProperties linkProperties, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void deactivateDataCall(int cid, int reason, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setCdmaBroadcastActivation(boolean activate, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setCdmaBroadcastConfig(CdmaSmsBroadcastConfigInfo[] configs, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getCdmaBroadcastConfig(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void exitEmergencyCallbackMode(Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getIccCardStatus(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getIccSlotsStatus(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setLogicalToPhysicalSlotMapping(int[] physicalSlots, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public int getLteOnCdmaMode() {
        return 0;
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void requestIccSimAuthentication(int authContext, String data, String aid, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getVoiceRadioTechnology(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForCellInfoList(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForCellInfoList(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForPhysicalChannelConfiguration(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForPhysicalChannelConfiguration(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setInitialAttachApn(DataProfile dataProfile, boolean isRoaming, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setDataProfile(DataProfile[] dps, boolean isRoaming, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void testingEmergencyCall() {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void iccOpenLogicalChannel(String AID, int p2, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void iccCloseLogicalChannel(int channel, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void iccTransmitApduLogicalChannel(int channel, int cla, int instruction, int p1, int p2, int p3, String data, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void iccTransmitApduBasicChannel(int cla, int instruction, int p1, int p2, int p3, String data, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void nvWriteCdmaPrl(byte[] preferredRoamingList, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void nvResetConfig(int resetType, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getHardwareConfig(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public int getRilVersion() {
        return 0;
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setUiccSubscription(int slotId, int appIndex, int subId, int subStatus, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setDataAllowed(boolean allowed, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void requestShutdown(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setRadioCapability(RadioCapability rc, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getRadioCapability(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForRadioCapabilityChanged(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForRadioCapabilityChanged(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void startLceService(int reportIntervalMs, boolean pullMode, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void stopLceService(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void pullLceData(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForLceInfo(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForLceInfo(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setCarrierInfoForImsiEncryption(ImsiEncryptionInfo imsiEncryptionInfo, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForPcoData(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForPcoData(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForModemReset(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForModemReset(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendDeviceState(int stateType, boolean state, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setUnsolResponseFilter(int filter, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setSignalStrengthReportingCriteria(int hysteresisMs, int hysteresisDb, int[] thresholdsDbm, int ran, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setLinkCapacityReportingCriteria(int hysteresisMs, int hysteresisDlKbps, int hysteresisUlKbps, int[] thresholdsDlKbps, int[] thresholdsUlKbps, int ran, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForCarrierInfoForImsiEncryption(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForNetworkScanResult(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForNetworkScanResult(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForCarrierInfoForImsiEncryption(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForNattKeepaliveStatus(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForNattKeepaliveStatus(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForEmergencyNumberList(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForEmergencyNumberList(Handler h) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void startNattKeepalive(int contextId, KeepalivePacketData packetData, int intervalMillis, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void stopNattKeepalive(int sessionHandle, Message result) {
    }
}
