package com.android.internal.telephony.imsphone;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.service.carrier.CarrierIdentifier;
import com.android.internal.telephony.BaseCommands;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.RadioCapability;
import com.android.internal.telephony.UUSInfo;
import com.android.internal.telephony.cdma.CdmaSmsBroadcastConfigInfo;
import com.android.internal.telephony.dataconnection.DataProfile;
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
import java.util.List;

class ImsPhoneCommandInterface extends BaseCommands implements CommandsInterface {
    ImsPhoneCommandInterface(Context context) {
        super(context);
    }

    public void setOnNITZTime(Handler h, int what, Object obj) {
    }

    public void getIccCardStatus(Message result) {
    }

    public void supplyIccPin(String pin, Message result) {
    }

    public void supplyIccPuk(String puk, String newPin, Message result) {
    }

    public void supplyIccPin2(String pin, Message result) {
    }

    public void supplyIccPuk2(String puk, String newPin2, Message result) {
    }

    public void changeIccPin(String oldPin, String newPin, Message result) {
    }

    public void changeIccPin2(String oldPin2, String newPin2, Message result) {
    }

    public void changeBarringPassword(String facility, String oldPwd, String newPwd, Message result) {
    }

    public void supplyNetworkDepersonalization(String netpin, Message result) {
    }

    public void getCurrentCalls(Message result) {
    }

    @Deprecated
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

    @Deprecated
    public void getLastPdpFailCause(Message result) {
    }

    public void getLastDataCallFailCause(Message result) {
    }

    public void setMute(boolean enableMute, Message response) {
    }

    public void getMute(Message response) {
    }

    public void getSignalStrength(Message result) {
    }

    public void getVoiceRegistrationState(Message result) {
    }

    public void getDataRegistrationState(Message result) {
    }

    public void getOperator(Message result) {
    }

    public void sendDtmf(char c, Message result) {
    }

    public void startDtmf(char c, Message result) {
    }

    public void stopDtmf(Message result) {
    }

    public void sendBurstDtmf(String dtmfString, int on, int off, Message result) {
    }

    public void sendSMS(String smscPDU, String pdu, Message result) {
    }

    public void sendSMSExpectMore(String smscPDU, String pdu, Message result) {
    }

    public void sendCdmaSms(byte[] pdu, Message result) {
    }

    public void sendImsGsmSms(String smscPDU, String pdu, int retry, int messageRef, Message response) {
    }

    public void sendImsCdmaSms(byte[] pdu, int retry, int messageRef, Message response) {
    }

    public void getImsRegistrationState(Message result) {
    }

    public void deleteSmsOnSim(int index, Message response) {
    }

    public void deleteSmsOnRuim(int index, Message response) {
    }

    public void writeSmsToSim(int status, String smsc, String pdu, Message response) {
    }

    public void writeSmsToRuim(int status, String pdu, Message response) {
    }

    public void setupDataCall(int radioTechnology, DataProfile dataProfile, boolean isRoaming, boolean allowRoaming, Message result) {
    }

    public void deactivateDataCall(int cid, int reason, Message result) {
    }

    public void setRadioPower(boolean on, Message result) {
    }

    public void setSuppServiceNotifications(boolean enable, Message result) {
    }

    public void acknowledgeLastIncomingGsmSms(boolean success, int cause, Message result) {
    }

    public void acknowledgeLastIncomingCdmaSms(boolean success, int cause, Message result) {
    }

    public void acknowledgeIncomingGsmSmsWithPdu(boolean success, String ackPdu, Message result) {
    }

    public void iccIO(int command, int fileid, String path, int p1, int p2, int p3, String data, String pin2, Message result) {
    }

    public void iccIOForApp(int command, int fileid, String path, int p1, int p2, int p3, String data, String pin2, String aid, Message result) {
    }

    public void getCLIR(Message result) {
    }

    public void setCLIR(int clirMode, Message result) {
    }

    public void queryCallWaiting(int serviceClass, Message response) {
    }

    public void setCallWaiting(boolean enable, int serviceClass, Message response) {
    }

    public void setNetworkSelectionModeAutomatic(Message response) {
    }

    public void setNetworkSelectionModeManual(String operatorNumeric, Message response) {
    }

    public void getNetworkSelectionMode(Message response) {
    }

    public void getAvailableNetworks(Message response) {
    }

    public void setCallForward(int action, int cfReason, int serviceClass, String number, int timeSeconds, Message response) {
    }

    public void queryCallForwardStatus(int cfReason, int serviceClass, String number, Message response) {
    }

    public void queryCLIP(Message response) {
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

    public void invokeOemRilRequestRaw(byte[] data, Message response) {
    }

    public void invokeOemRilRequestStrings(String[] strings, Message response) {
    }

    public void setBandMode(int bandMode, Message response) {
    }

    public void queryAvailableBandMode(Message response) {
    }

    public void sendTerminalResponse(String contents, Message response) {
    }

    public void sendEnvelope(String contents, Message response) {
    }

    public void sendEnvelopeWithStatus(String contents, Message response) {
    }

    public void handleCallSetupRequestFromSim(boolean accept, Message response) {
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

    public void getCdmaSubscriptionSource(Message response) {
    }

    public void getGsmBroadcastConfig(Message response) {
    }

    public void setGsmBroadcastConfig(SmsBroadcastConfigInfo[] config, Message response) {
    }

    public void setGsmBroadcastActivation(boolean activate, Message response) {
    }

    public void getDeviceIdentity(Message response) {
    }

    public void getCDMASubscription(Message response) {
    }

    public void setPhoneType(int phoneType) {
    }

    public void queryCdmaRoamingPreference(Message response) {
    }

    public void setCdmaRoamingPreference(int cdmaRoamingType, Message response) {
    }

    public void setCdmaSubscriptionSource(int cdmaSubscription, Message response) {
    }

    public void queryTTYMode(Message response) {
    }

    public void setTTYMode(int ttyMode, Message response) {
    }

    public void sendCDMAFeatureCode(String FeatureCode, Message response) {
    }

    public void getCdmaBroadcastConfig(Message response) {
    }

    public void setCdmaBroadcastConfig(CdmaSmsBroadcastConfigInfo[] configs, Message response) {
    }

    public void setCdmaBroadcastActivation(boolean activate, Message response) {
    }

    public void exitEmergencyCallbackMode(Message response) {
    }

    public void supplyIccPinForApp(String pin, String aid, Message response) {
    }

    public void supplyIccPukForApp(String puk, String newPin, String aid, Message response) {
    }

    public void supplyIccPin2ForApp(String pin2, String aid, Message response) {
    }

    public void supplyIccPuk2ForApp(String puk2, String newPin2, String aid, Message response) {
    }

    public void changeIccPinForApp(String oldPin, String newPin, String aidPtr, Message response) {
    }

    public void changeIccPin2ForApp(String oldPin2, String newPin2, String aidPtr, Message response) {
    }

    public void requestIsimAuthentication(String nonce, Message response) {
    }

    public void requestIccSimAuthentication(int authContext, String data, String aid, Message response) {
    }

    public void getVoiceRadioTechnology(Message result) {
    }

    public void setInitialAttachApn(DataProfile dataProfile, boolean isRoaming, Message result) {
    }

    public void setDataProfile(DataProfile[] dps, boolean isRoaming, Message result) {
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

    public void requestShutdown(Message result) {
    }

    public void setRadioCapability(RadioCapability rc, Message response) {
    }

    public void getRadioCapability(Message response) {
    }

    public void startLceService(int reportIntervalMs, boolean pullMode, Message result) {
    }

    public void stopLceService(Message result) {
    }

    public void pullLceData(Message result) {
    }

    public void getModemActivityInfo(Message result) {
    }

    public void setAllowedCarriers(List<CarrierIdentifier> list, Message result) {
    }

    public void getAllowedCarriers(Message result) {
    }

    public void sendDeviceState(int stateType, boolean state, Message result) {
    }

    public void setUnsolResponseFilter(int filter, Message result) {
    }

    public void setSimCardPower(boolean powerUp, Message result) {
    }
}
