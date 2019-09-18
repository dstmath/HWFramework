package com.android.internal.telephony;

import android.content.Context;
import android.hardware.radio.V1_0.CdmaSmsMessage;
import android.hardware.radio.V1_0.RadioResponseInfo;
import android.hardware.radio.V1_0.SetupDataCallResult;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.Map;
import vendor.huawei.hardware.hisiradio.V1_0.IHisiRadio;
import vendor.huawei.hardware.radio.V2_0.IRadio;
import vendor.huawei.hardware.radio.deprecated.V1_0.IOemHook;

public abstract class AbstractRIL extends BaseCommands {
    protected static final String HW_VOLTE_USER_SWITCH = "hw_volte_user_switch";
    protected static final int RIL_HW_BUFFER_MAX = 120;
    protected static final int RIL_HW_EVT_HOOK_SEND_BUFFER = 598043;
    private HwRILReference mReference = HwTelephonyFactory.getHwTelephonyBaseManager().createHwRILReference(this);

    public interface HisiRILCommand {
        void excute(IHisiRadio iHisiRadio, int i) throws RemoteException, RuntimeException;
    }

    public interface HwRILReference {
        public static final int MODEM_IMS_SWITCH_ON = 1;

        void clearHuaweiCommonRadioProxy();

        void clearHwOemHookProxy();

        Map<String, String> correctApnAuth(String str, int i, String str2);

        void custSetModemProperties();

        void dataConnectionAttach(int i, Message message);

        void dataConnectionDetach(int i, Message message);

        void existNetworkInfo(String str);

        void getCdmaGsmImsi(Message message);

        void getCdmaModeSide(Message message);

        void getCurrentPOLList(Message message);

        IRadio getHuaweiCommonRadioProxy(Message message);

        IOemHook getHwOemHookProxy(Message message);

        boolean getImsSwitch();

        void getPOLCapabilty(Message message);

        void getSignalStrength(Message message);

        void getSimMatchedFileFromRilCache(int i, Message message);

        void handleImsSwitch(boolean z);

        void handleRequestGetImsiMessage(RILRequest rILRequest, Object obj, Context context);

        void handleUnsolicitedRadioStateChanged(boolean z, Context context);

        void iccGetATR(Message message);

        void notifyAntOrMaxTxPowerInfo(byte[] bArr);

        void notifyBandClassInfo(byte[] bArr);

        void notifyCModemStatus(int i, Message message);

        void notifyDeviceState(String str, String str2, String str3, Message message);

        void notifyIccUimLockRegistrants();

        void processHWBufferUnsolicited(byte[] bArr);

        Object processSolicitedEx(int i, Parcel parcel);

        void queryEmergencyNumbers();

        void registerForUimLockcard(Handler handler, int i, Object obj);

        boolean registerSarRegistrant(int i, Message message);

        void requestSetEmergencyNumbers(String str, String str2);

        void resetAllConnections();

        void responseDataCallList(RadioResponseInfo radioResponseInfo, ArrayList<SetupDataCallResult> arrayList);

        void restartRild(Message message);

        void sendHWBufferSolicited(Message message, int i, byte[] bArr);

        void sendRacChangeBroadcast(byte[] bArr);

        void sendResponseToTarget(Message message, int i);

        void sendSMSSetLong(int i, Message message);

        void sendSimMatchedOperatorInfo(String str, String str2, int i, String str3, Message message);

        void setCdmaModeSide(int i, Message message);

        void setHwRILReferenceInstanceId(int i);

        void setImsSwitch(boolean z);

        void setPOLEntry(int i, String str, int i2, Message message);

        void setPowerGrade(int i, Message message);

        void setShouldReportRoamingPlusInfo(boolean z);

        void setVpMask(int i, Message message);

        void setWifiTxPowerGrade(int i, Message message);

        void supplyDepersonalization(String str, int i, Message message);

        void unregisterForUimLockcard(Handler handler);

        boolean unregisterSarRegistrant(int i, Message message);

        void writeContent(CdmaSmsMessage cdmaSmsMessage, String str);
    }

    public AbstractRIL(Context context) {
        super(context);
    }

    public HwRILReference getRILReference() {
        return this.mReference;
    }

    public void send(RILRequestReference rr) {
    }

    /* access modifiers changed from: protected */
    public Object processSolicitedEx(int rilRequest, Parcel p) {
        return this.mReference.processSolicitedEx(rilRequest, p);
    }

    /* access modifiers changed from: protected */
    public void setShouldReportRoamingPlusInfo(boolean on) {
        this.mReference.setShouldReportRoamingPlusInfo(on);
    }

    /* access modifiers changed from: protected */
    public void handleRequestGetImsiMessage(RILRequest rr, Object ret, Context context) {
        this.mReference.handleRequestGetImsiMessage(rr, ret, context);
    }

    /* access modifiers changed from: protected */
    public void handleRequestGetImsiMessage(RILRequest rr, Object ret) {
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

    /* access modifiers changed from: protected */
    public void writeContent(CdmaSmsMessage msg, String pdu) {
        this.mReference.writeContent(msg, pdu);
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

    public void notifyDeviceState(String device, String state, String extra, Message result) {
        this.mReference.notifyDeviceState(device, state, extra, result);
    }

    public void sendSimMatchedOperatorInfo(String opKey, String opName, int state, String reserveField, Message response) {
        if (this.mReference != null) {
            this.mReference.sendSimMatchedOperatorInfo(opKey, opName, state, reserveField, response);
        }
    }

    public void getSignalStrength(Message result) {
        if (this.mReference != null) {
            this.mReference.getSignalStrength(result);
        }
    }

    public IHisiRadio getHisiRadioProxy(Message result) {
        return null;
    }

    public void resetMTKRadioProxy() {
    }

    public void clearMTKRadioProxy() {
    }

    public IOemHook getHwOemHookProxy(Message response) {
        if (this.mReference != null) {
            return this.mReference.getHwOemHookProxy(response);
        }
        return null;
    }

    public void existNetworkInfo(String state) {
        if (this.mReference != null) {
            this.mReference.existNetworkInfo(state);
        }
    }

    public IRadio getHuaweiCommonRadioProxy(Message result) {
        if (this.mReference != null) {
            return this.mReference.getHuaweiCommonRadioProxy(result);
        }
        return null;
    }

    public void clearHuaweiCommonRadioProxy() {
        if (this.mReference != null) {
            this.mReference.clearHuaweiCommonRadioProxy();
        }
    }

    public void clearHwOemHookProxy() {
        if (this.mReference != null) {
            this.mReference.clearHwOemHookProxy();
        }
    }

    public void responseDataCallList(RadioResponseInfo responseInfo, ArrayList<SetupDataCallResult> dataCallResultList) {
        this.mReference.responseDataCallList(responseInfo, dataCallResultList);
    }

    public void getSimMatchedFileFromRilCache(int fileId, Message result) {
        if (this.mReference != null) {
            this.mReference.getSimMatchedFileFromRilCache(fileId, result);
        }
    }

    public void resetQcomRadioProxy() {
    }

    public void clearQcomRadioProxy() {
    }

    public void custSetModemProperties() {
        if (this.mReference != null) {
            this.mReference.custSetModemProperties();
        }
    }
}
