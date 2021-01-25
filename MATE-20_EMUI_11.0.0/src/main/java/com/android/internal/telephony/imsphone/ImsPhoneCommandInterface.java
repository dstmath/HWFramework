package com.android.internal.telephony.imsphone;

import android.content.Context;
import android.net.KeepalivePacketData;
import android.net.LinkProperties;
import android.os.Handler;
import android.os.Message;
import android.telephony.ImsiEncryptionInfo;
import android.telephony.NetworkScanRequest;
import android.telephony.data.DataProfile;
import android.telephony.emergency.EmergencyNumber;
import com.android.internal.telephony.BaseCommands;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.RadioCapability;
import com.android.internal.telephony.UUSInfo;
import com.android.internal.telephony.cdma.CdmaSmsBroadcastConfigInfo;
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;

class ImsPhoneCommandInterface extends BaseCommands implements CommandsInterface {
    ImsPhoneCommandInterface(Context context) {
        super(context);
    }

    @Override // com.android.internal.telephony.BaseCommands, com.android.internal.telephony.CommandsInterface
    public void setOnNITZTime(Handler h, int what, Object obj) {
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
    public void supplyIccPin(String pin, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void supplyIccPuk(String puk, String newPin, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void supplyIccPin2(String pin, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void supplyIccPuk2(String puk, String newPin2, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void changeIccPin(String oldPin, String newPin, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void changeIccPin2(String oldPin2, String newPin2, Message result) {
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
    @Deprecated
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
    @Deprecated
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
    public void getSignalStrength(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getVoiceRegistrationState(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getDataRegistrationState(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getOperator(Message result) {
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
    public void sendSMS(String smscPDU, String pdu, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendSMSExpectMore(String smscPDU, String pdu, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendCdmaSms(byte[] pdu, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendImsGsmSms(String smscPDU, String pdu, int retry, int messageRef, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendImsCdmaSms(byte[] pdu, int retry, int messageRef, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getImsRegistrationState(Message result) {
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
    public void setupDataCall(int accessNetworkType, DataProfile dataProfile, boolean isRoaming, boolean allowRoaming, int reason, LinkProperties linkProperties, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void deactivateDataCall(int cid, int reason, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setRadioPower(boolean on, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setSuppServiceNotifications(boolean enable, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void acknowledgeLastIncomingGsmSms(boolean success, int cause, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void acknowledgeLastIncomingCdmaSms(boolean success, int cause, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void acknowledgeIncomingGsmSmsWithPdu(boolean success, String ackPdu, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void iccIO(int command, int fileid, String path, int p1, int p2, int p3, String data, String pin2, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void iccIOForApp(int command, int fileid, String path, int p1, int p2, int p3, String data, String pin2, String aid, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getCLIR(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setCLIR(int clirMode, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void queryCallWaiting(int serviceClass, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setCallWaiting(boolean enable, int serviceClass, Message response) {
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
    public void setCallForward(int action, int cfReason, int serviceClass, String number, int timeSeconds, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void queryCallForwardStatus(int cfReason, int serviceClass, String number, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void queryCLIP(Message response) {
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
    public void invokeOemRilRequestRaw(byte[] data, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void invokeOemRilRequestStrings(String[] strings, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setBandMode(int bandMode, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void queryAvailableBandMode(Message response) {
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
    public void getCdmaSubscriptionSource(Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getGsmBroadcastConfig(Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setGsmBroadcastConfig(SmsBroadcastConfigInfo[] config, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setGsmBroadcastActivation(boolean activate, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getDeviceIdentity(Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getCDMASubscription(Message response) {
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
    public void setCdmaSubscriptionSource(int cdmaSubscription, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void queryTTYMode(Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setTTYMode(int ttyMode, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendCDMAFeatureCode(String FeatureCode, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getCdmaBroadcastConfig(Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setCdmaBroadcastConfig(CdmaSmsBroadcastConfigInfo[] configs, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setCdmaBroadcastActivation(boolean activate, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void exitEmergencyCallbackMode(Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void supplyIccPinForApp(String pin, String aid, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void supplyIccPukForApp(String puk, String newPin, String aid, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void supplyIccPin2ForApp(String pin2, String aid, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void supplyIccPuk2ForApp(String puk2, String newPin2, String aid, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void changeIccPinForApp(String oldPin, String newPin, String aidPtr, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void changeIccPin2ForApp(String oldPin2, String newPin2, String aidPtr, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void requestIccSimAuthentication(int authContext, String data, String aid, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getVoiceRadioTechnology(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setInitialAttachApn(DataProfile dataProfile, boolean isRoaming, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setDataProfile(DataProfile[] dps, boolean isRoaming, Message result) {
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

    @Override // com.android.internal.telephony.BaseCommands, com.android.internal.telephony.CommandsInterface
    public void requestShutdown(Message result) {
    }

    @Override // com.android.internal.telephony.BaseCommands, com.android.internal.telephony.CommandsInterface
    public void setRadioCapability(RadioCapability rc, Message response) {
    }

    @Override // com.android.internal.telephony.BaseCommands, com.android.internal.telephony.CommandsInterface
    public void getRadioCapability(Message response) {
    }

    @Override // com.android.internal.telephony.BaseCommands, com.android.internal.telephony.CommandsInterface
    public void startLceService(int reportIntervalMs, boolean pullMode, Message result) {
    }

    @Override // com.android.internal.telephony.BaseCommands, com.android.internal.telephony.CommandsInterface
    public void stopLceService(Message result) {
    }

    @Override // com.android.internal.telephony.BaseCommands, com.android.internal.telephony.CommandsInterface
    public void pullLceData(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setCarrierInfoForImsiEncryption(ImsiEncryptionInfo imsiEncryptionInfo, Message result) {
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
    public void startNattKeepalive(int contextId, KeepalivePacketData packetData, int intervalMillis, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void stopNattKeepalive(int sessionHandle, Message result) {
    }
}
