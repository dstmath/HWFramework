package com.android.internal.telephony;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import java.util.Map;

public abstract class AbstractRIL extends BaseCommands {
    protected static final String HW_VOLTE_USER_SWITCH = "hw_volte_user_switch";
    protected static final int RIL_HW_BUFFER_MAX = 120;
    protected static final int RIL_HW_EVT_HOOK_SEND_BUFFER = 598043;
    private HwRILReference mReference;

    public interface HwRILReference {
        Map<String, String> correctApnAuth(String str, int i, String str2);

        void dataConnectionAttach(int i, Message message);

        void dataConnectionDetach(int i, Message message);

        void getCdmaChrInfo(Message message);

        void getCdmaGsmImsi(Message message);

        void getCdmaModeSide(Message message);

        void getCurrentPOLList(Message message);

        void getHwRatCombineMode(Message message);

        boolean getImsSwitch();

        void getPOLCapabilty(Message message);

        void handleRequestGetImsiMessage(RILRequest rILRequest, Object obj, Context context);

        void handleUnsolicitedDefaultMessage(int i, Object obj, Context context);

        Object handleUnsolicitedDefaultMessagePara(int i, Parcel parcel);

        void handleUnsolicitedRadioStateChanged(boolean z, Context context);

        void iccCloseChannel(int i, Message message);

        void iccExchangeAPDU(int i, int i2, int i3, int i4, int i5, int i6, String str, Message message);

        void iccGetATR(Message message);

        void iccOpenChannel(String str, Message message);

        void notifyAntOrMaxTxPowerInfo(byte[] bArr);

        void notifyBandClassInfo(byte[] bArr);

        void notifyCModemStatus(int i, Message message);

        void notifyIccUimLockRegistrants();

        void processHWBufferUnsolicited(byte[] bArr);

        Object processSolicitedEx(int i, Parcel parcel);

        void queryEmergencyNumbers();

        void registerForUimLockcard(Handler handler, int i, Object obj);

        boolean registerSarRegistrant(int i, Message message);

        void requestSetEmergencyNumbers(String str, String str2);

        void resetAllConnections();

        void restartRild(Message message);

        void riseCdmaCutoffFreq(boolean z, Message message);

        void sendHWBufferSolicited(Message message, int i, byte[] bArr);

        void sendRacChangeBroadcast(byte[] bArr);

        void sendResponseToTarget(Message message, int i);

        void sendSMSSetLong(int i, Message message);

        void setCdmaModeSide(int i, Message message);

        void setHwRFChannelSwitch(int i, Message message);

        void setHwRILReferenceInstanceId(int i);

        void setHwRatCombineMode(int i, Message message);

        void setImsSwitch(boolean z);

        void setPOLEntry(int i, String str, int i2, Message message);

        void setPowerGrade(int i, Message message);

        void setShouldReportRoamingPlusInfo(boolean z);

        void setVpMask(int i, Message message);

        void setWifiTxPowerGrade(int i, Message message);

        void supplyDepersonalization(String str, int i, Message message);

        void testVoiceLoopBack(int i);

        void unregisterForUimLockcard(Handler handler);

        boolean unregisterSarRegistrant(int i, Message message);

        void writeContent(RILRequestReference rILRequestReference, String str);
    }

    public AbstractRIL(Context context) {
        super(context);
        this.mReference = HwTelephonyFactory.getHwTelephonyBaseManager().createHwRILReference(this);
    }

    public void send(RILRequestReference rr) {
    }

    protected Object processSolicitedEx(int rilRequest, Parcel p) {
        return this.mReference.processSolicitedEx(rilRequest, p);
    }

    protected void setShouldReportRoamingPlusInfo(boolean on) {
        this.mReference.setShouldReportRoamingPlusInfo(on);
    }

    protected void handleRequestGetImsiMessage(RILRequest rr, Object ret, Context context) {
        this.mReference.handleRequestGetImsiMessage(rr, ret, context);
    }

    protected void handleRequestGetImsiMessage(RILRequest rr, Object ret) {
    }

    protected Object handleUnsolicitedDefaultMessagePara(int response, Parcel p) {
        return this.mReference.handleUnsolicitedDefaultMessagePara(response, p);
    }

    protected void handleUnsolicitedDefaultMessage(int response, Object ret, Context context) {
        this.mReference.handleUnsolicitedDefaultMessage(response, ret, context);
    }

    public void setImsSwitch(boolean on) {
        this.mReference.setImsSwitch(on);
    }

    public boolean getImsSwitch() {
        return this.mReference.getImsSwitch();
    }

    public void handleUnsolicitedRadioStateChanged(boolean on, Context context) {
        this.mReference.handleUnsolicitedRadioStateChanged(on, context);
    }

    public void iccExchangeAPDU(int cla, int command, int channel, int p1, int p2, int p3, String data, Message result) {
        this.mReference.iccExchangeAPDU(cla, command, channel, p1, p2, p3, data, result);
    }

    public void iccOpenChannel(String AID, Message result) {
        this.mReference.iccOpenChannel(AID, result);
    }

    public void iccCloseChannel(int channel, Message result) {
        this.mReference.iccCloseChannel(channel, result);
    }

    protected void writeContent(RILRequestReference rr, String pdu) {
        this.mReference.writeContent(rr, pdu);
    }

    public void iccGetATR(Message result) {
        this.mReference.iccGetATR(result);
    }

    public void setPowerGrade(int powerGrade, Message response) {
        this.mReference.setPowerGrade(powerGrade, response);
    }

    public void setWifiTxPowerGrade(int powerGrade, Message response) {
        this.mReference.setWifiTxPowerGrade(powerGrade, response);
    }

    public void riseCdmaCutoffFreq(boolean on, Message msg) {
        this.mReference.riseCdmaCutoffFreq(on, msg);
    }

    public void getPOLCapabilty(Message response) {
        this.mReference.getPOLCapabilty(response);
    }

    public void getCurrentPOLList(Message response) {
        this.mReference.getCurrentPOLList(response);
    }

    public void setPOLEntry(int index, String numeric, int nAct, Message response) {
        this.mReference.setPOLEntry(index, numeric, nAct, response);
    }

    public void supplyDepersonalization(String netpin, int type, Message result) {
        this.mReference.supplyDepersonalization(netpin, type, result);
    }

    public void registerForUimLockcard(Handler h, int what, Object obj) {
        this.mReference.registerForUimLockcard(h, what, obj);
    }

    public void unregisterForUimLockcard(Handler h) {
        this.mReference.unregisterForUimLockcard(h);
    }

    public void notifyIccUimLockRegistrants() {
        this.mReference.notifyIccUimLockRegistrants();
    }

    public void sendSMSSetLong(int flag, Message result) {
        this.mReference.sendSMSSetLong(flag, result);
    }

    public void dataConnectionDetach(int mode, Message response) {
        this.mReference.dataConnectionDetach(mode, response);
    }

    public void dataConnectionAttach(int mode, Message response) {
        this.mReference.dataConnectionAttach(mode, response);
    }

    public void resetProfile(Message response) {
    }

    public void getCdmaChrInfo(Message result) {
        this.mReference.getCdmaChrInfo(result);
    }

    public void restartRild(Message result) {
        this.mReference.restartRild(result);
    }

    public void sendResponseToTarget(Message response, int responseCode) {
        this.mReference.sendResponseToTarget(response, responseCode);
    }

    public void requestSetEmergencyNumbers(String ecclist_withcard, String ecclist_nocard) {
        this.mReference.requestSetEmergencyNumbers(ecclist_withcard, ecclist_nocard);
    }

    public void queryEmergencyNumbers() {
        this.mReference.queryEmergencyNumbers();
    }

    public void getCdmaGsmImsi(Message result) {
        this.mReference.getCdmaGsmImsi(result);
    }

    public void testVoiceLoopBack(int mode) {
        this.mReference.testVoiceLoopBack(mode);
    }

    public void setHwRatCombineMode(int combineMode, Message result) {
        this.mReference.setHwRatCombineMode(combineMode, result);
    }

    public void getHwRatCombineMode(Message result) {
        this.mReference.getHwRatCombineMode(result);
    }

    public void setHwRFChannelSwitch(int rfChannel, Message result) {
        this.mReference.setHwRFChannelSwitch(rfChannel, result);
    }

    public void setCdmaModeSide(int modemID, Message result) {
        this.mReference.setCdmaModeSide(modemID, result);
    }

    public void getCdmaModeSide(Message result) {
        this.mReference.getCdmaModeSide(result);
    }

    public void setVpMask(int vpMask, Message result) {
        this.mReference.setVpMask(vpMask, result);
    }

    public void resetAllConnections() {
        this.mReference.resetAllConnections();
    }

    public Map<String, String> correctApnAuth(String userName, int authType, String password) {
        return this.mReference.correctApnAuth(userName, authType, password);
    }

    public void sendRacChangeBroadcast(byte[] data) {
        this.mReference.sendRacChangeBroadcast(data);
    }

    public void setHwRILReferenceInstanceId(int instanceId) {
        this.mReference.setHwRILReferenceInstanceId(instanceId);
    }

    public void notifyCModemStatus(int state, Message result) {
        this.mReference.notifyCModemStatus(state, result);
    }

    public boolean unregisterSarRegistrant(int type, Message result) {
        return this.mReference.unregisterSarRegistrant(type, result);
    }

    public boolean registerSarRegistrant(int type, Message result) {
        return this.mReference.registerSarRegistrant(type, result);
    }

    public void notifyAntOrMaxTxPowerInfo(byte[] data) {
        this.mReference.notifyAntOrMaxTxPowerInfo(data);
    }

    public void notifyBandClassInfo(byte[] resultData) {
        this.mReference.notifyBandClassInfo(resultData);
    }

    public void sendHWBufferSolicited(Message result, int event, byte[] reqData) {
        this.mReference.sendHWBufferSolicited(result, event, reqData);
    }

    public void processHWBufferUnsolicited(byte[] respData) {
        this.mReference.processHWBufferUnsolicited(respData);
    }
}
