package com.android.internal.telephony;

import android.content.Context;
import android.hardware.radio.V1_0.CdmaSmsMessage;
import android.hardware.radio.V1_0.RadioResponseInfo;
import android.hardware.radio.V1_0.SetupDataCallResult;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.telephony.SignalStrength;
import java.util.ArrayList;
import vendor.huawei.hardware.hisiradio.V1_0.IHisiRadio;
import vendor.huawei.hardware.radio.V2_0.IRadio;
import vendor.huawei.hardware.radio.V2_1.HwSignalStrength_2_1;
import vendor.huawei.hardware.radio.deprecated.V1_0.IOemHook;

public abstract class AbstractRIL extends BaseCommands {
    protected static final String HW_VOLTE_USER_SWITCH = "hw_volte_user_switch";
    private static final int INT_SIZE = 4;
    public static final String OEM_IDENTIFIER = "QOEMHOOK";
    protected static final int RIL_HW_BUFFER_MAX = 120;
    protected static final int RIL_HW_EVT_HOOK_SEND_BUFFER = 598043;
    int mHeaderSize = (OEM_IDENTIFIER.length() + 8);
    private HwRILReference mReference = HwTelephonyFactory.getHwTelephonyBaseManager().createHwRILReference(this);

    public interface HwRILReference {
        public static final int MODEM_IMS_SWITCH_ON = 1;

        void clearHuaweiCommonRadioProxy();

        void clearHwOemHookProxy();

        SignalStrength convertHalSignalStrength_2_1(HwSignalStrength_2_1 hwSignalStrength_2_1, int i);

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

        void getNrCellSsbId(Message message);

        void getNrOptionMode(Message message);

        void getNrSaState(Message message);

        void getPOLCapabilty(Message message);

        void getRrcConnectionState(Message message);

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

        void notifyUnsolOemHookResponse(byte[] bArr);

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

        void setNrOptionMode(int i, Message message);

        void setNrSaState(int i, Message message);

        void setNrSwitch(boolean z, Message message);

        void setPOLEntry(int i, String str, int i2, Message message);

        void setPowerGrade(int i, Message message);

        void setShouldReportRoamingPlusInfo(boolean z);

        void setTemperatureControlToModem(int i, int i2, Message message);

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

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void setImsSwitch(boolean on) {
        this.mReference.setImsSwitch(on);
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public boolean getImsSwitch() {
        return this.mReference.getImsSwitch();
    }

    public void handleUnsolicitedRadioStateChanged(boolean on, Context context) {
        this.mReference.handleUnsolicitedRadioStateChanged(on, context);
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void setNrSwitch(boolean on, Message response) {
        this.mReference.setNrSwitch(on, response);
    }

    /* access modifiers changed from: protected */
    public void writeContent(CdmaSmsMessage msg, String pdu) {
        this.mReference.writeContent(msg, pdu);
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void iccGetATR(Message result) {
        this.mReference.iccGetATR(result);
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void setPowerGrade(int powerGrade, Message response) {
        this.mReference.setPowerGrade(powerGrade, response);
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void setWifiTxPowerGrade(int powerGrade, Message response) {
        this.mReference.setWifiTxPowerGrade(powerGrade, response);
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void getPOLCapabilty(Message response) {
        this.mReference.getPOLCapabilty(response);
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void getCurrentPOLList(Message response) {
        this.mReference.getCurrentPOLList(response);
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void setPOLEntry(int index, String numeric, int nAct, Message response) {
        this.mReference.setPOLEntry(index, numeric, nAct, response);
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void supplyDepersonalization(String netpin, int type, Message result) {
        this.mReference.supplyDepersonalization(netpin, type, result);
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void registerForUimLockcard(Handler h, int what, Object obj) {
        this.mReference.registerForUimLockcard(h, what, obj);
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void unregisterForUimLockcard(Handler h) {
        this.mReference.unregisterForUimLockcard(h);
    }

    public void notifyIccUimLockRegistrants() {
        this.mReference.notifyIccUimLockRegistrants();
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void sendSMSSetLong(int flag, Message result) {
        this.mReference.sendSMSSetLong(flag, result);
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void dataConnectionDetach(int mode, Message response) {
        this.mReference.dataConnectionDetach(mode, response);
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void dataConnectionAttach(int mode, Message response) {
        this.mReference.dataConnectionAttach(mode, response);
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void resetProfile(Message response) {
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void restartRild(Message result) {
        this.mReference.restartRild(result);
    }

    public void sendResponseToTarget(Message response, int responseCode) {
        this.mReference.sendResponseToTarget(response, responseCode);
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void requestSetEmergencyNumbers(String ecclist_withcard, String ecclist_nocard) {
        this.mReference.requestSetEmergencyNumbers(ecclist_withcard, ecclist_nocard);
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void queryEmergencyNumbers() {
        this.mReference.queryEmergencyNumbers();
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void getCdmaGsmImsi(Message result) {
        this.mReference.getCdmaGsmImsi(result);
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void setCdmaModeSide(int modemID, Message result) {
        this.mReference.setCdmaModeSide(modemID, result);
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void getCdmaModeSide(Message result) {
        this.mReference.getCdmaModeSide(result);
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void setVpMask(int vpMask, Message result) {
        this.mReference.setVpMask(vpMask, result);
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void resetAllConnections() {
        this.mReference.resetAllConnections();
    }

    public void sendRacChangeBroadcast(byte[] data) {
        this.mReference.sendRacChangeBroadcast(data);
    }

    public void setHwRILReferenceInstanceId(int instanceId) {
        this.mReference.setHwRILReferenceInstanceId(instanceId);
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void notifyCModemStatus(int state, Message result) {
        this.mReference.notifyCModemStatus(state, result);
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public boolean unregisterSarRegistrant(int type, Message result) {
        return this.mReference.unregisterSarRegistrant(type, result);
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public boolean registerSarRegistrant(int type, Message result) {
        return this.mReference.registerSarRegistrant(type, result);
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void notifyAntOrMaxTxPowerInfo(byte[] data) {
        this.mReference.notifyAntOrMaxTxPowerInfo(data);
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void notifyBandClassInfo(byte[] resultData) {
        this.mReference.notifyBandClassInfo(resultData);
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void sendHWBufferSolicited(Message result, int event, byte[] reqData) {
        this.mReference.sendHWBufferSolicited(result, event, reqData);
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void processHWBufferUnsolicited(byte[] respData) {
        this.mReference.processHWBufferUnsolicited(respData);
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void notifyDeviceState(String device, String state, String extra, Message result) {
        this.mReference.notifyDeviceState(device, state, extra, result);
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void sendSimMatchedOperatorInfo(String opKey, String opName, int state, String reserveField, Message response) {
        HwRILReference hwRILReference = this.mReference;
        if (hwRILReference != null) {
            hwRILReference.sendSimMatchedOperatorInfo(opKey, opName, state, reserveField, response);
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getSignalStrength(Message result) {
        HwRILReference hwRILReference = this.mReference;
        if (hwRILReference != null) {
            hwRILReference.getSignalStrength(result);
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
        HwRILReference hwRILReference = this.mReference;
        if (hwRILReference != null) {
            return hwRILReference.getHwOemHookProxy(response);
        }
        return null;
    }

    public void existNetworkInfo(String state) {
        HwRILReference hwRILReference = this.mReference;
        if (hwRILReference != null) {
            hwRILReference.existNetworkInfo(state);
        }
    }

    public IRadio getHuaweiCommonRadioProxy(Message result) {
        HwRILReference hwRILReference = this.mReference;
        if (hwRILReference != null) {
            return hwRILReference.getHuaweiCommonRadioProxy(result);
        }
        return null;
    }

    public void clearHuaweiCommonRadioProxy() {
        HwRILReference hwRILReference = this.mReference;
        if (hwRILReference != null) {
            hwRILReference.clearHuaweiCommonRadioProxy();
        }
    }

    public void clearHwOemHookProxy() {
        HwRILReference hwRILReference = this.mReference;
        if (hwRILReference != null) {
            hwRILReference.clearHwOemHookProxy();
        }
    }

    public void responseDataCallList(RadioResponseInfo responseInfo, ArrayList<SetupDataCallResult> dataCallResultList) {
        this.mReference.responseDataCallList(responseInfo, dataCallResultList);
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void getSimMatchedFileFromRilCache(int fileId, Message result) {
        HwRILReference hwRILReference = this.mReference;
        if (hwRILReference != null) {
            hwRILReference.getSimMatchedFileFromRilCache(fileId, result);
        }
    }

    public void resetQcomRadioProxy() {
    }

    public void clearQcomRadioProxy() {
    }

    public void custSetModemProperties() {
        HwRILReference hwRILReference = this.mReference;
        if (hwRILReference != null) {
            hwRILReference.custSetModemProperties();
        }
    }

    public void notifyUnsolOemHookResponse(byte[] ret) {
        this.mReference.notifyUnsolOemHookResponse(ret);
    }

    public SignalStrength convertHalSignalStrength_2_1(HwSignalStrength_2_1 signalStrength, int phoneId) {
        HwRILReference hwRILReference = this.mReference;
        if (hwRILReference != null) {
            return hwRILReference.convertHalSignalStrength_2_1(signalStrength, phoneId);
        }
        return new SignalStrength();
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void setNrSaState(int on, Message response) {
        HwRILReference hwRILReference = this.mReference;
        if (hwRILReference != null) {
            hwRILReference.setNrSaState(on, response);
        }
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void getNrSaState(Message result) {
        HwRILReference hwRILReference = this.mReference;
        if (hwRILReference != null) {
            hwRILReference.getNrSaState(result);
        }
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void getNrCellSsbId(Message result) {
        HwRILReference hwRILReference = this.mReference;
        if (hwRILReference != null) {
            hwRILReference.getNrCellSsbId(result);
        }
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void getRrcConnectionState(Message result) {
        HwRILReference hwRILReference = this.mReference;
        if (hwRILReference != null) {
            hwRILReference.getRrcConnectionState(result);
        }
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void setNrOptionMode(int mode, Message msg) {
        HwRILReference hwRILReference = this.mReference;
        if (hwRILReference != null) {
            hwRILReference.setNrOptionMode(mode, msg);
        }
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void getNrOptionMode(Message result) {
        HwRILReference hwRILReference = this.mReference;
        if (hwRILReference != null) {
            hwRILReference.getNrOptionMode(result);
        }
    }

    @Override // com.android.internal.telephony.AbstractBaseCommands, com.android.internal.telephony.AbstractCommandsInterface
    public void setTemperatureControlToModem(int level, int type, Message result) {
        HwRILReference hwRILReference = this.mReference;
        if (hwRILReference != null) {
            hwRILReference.setTemperatureControlToModem(level, type, result);
        }
    }
}
