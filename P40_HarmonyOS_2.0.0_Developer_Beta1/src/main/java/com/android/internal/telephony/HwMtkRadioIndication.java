package com.android.internal.telephony;

import android.hardware.radio.V1_0.SuppSvcNotification;
import android.os.AsyncResult;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthNr;
import android.telephony.CellSignalStrengthTdscdma;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.Rlog;
import android.telephony.SignalStrength;
import com.android.internal.telephony.uicc.IccUtils;
import com.huawei.internal.telephony.vsim.ExternalSimManager;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import vendor.huawei.hardware.mtkradio.V1_0.CfuStatusNotification;
import vendor.huawei.hardware.mtkradio.V1_0.CipherNotification;
import vendor.huawei.hardware.mtkradio.V1_0.CrssNotification;
import vendor.huawei.hardware.mtkradio.V1_0.DedicateDataCall;
import vendor.huawei.hardware.mtkradio.V1_0.EtwsNotification;
import vendor.huawei.hardware.mtkradio.V1_0.IncomingCallNotification;
import vendor.huawei.hardware.mtkradio.V1_0.PcoDataAttachedInfo;
import vendor.huawei.hardware.mtkradio.V1_0.RILUnsolMsgPayload;
import vendor.huawei.hardware.mtkradio.V1_0.SignalStrengthWithWcdmaEcio;
import vendor.huawei.hardware.mtkradio.V1_0.VsimOperationEvent;
import vendor.huawei.hardware.mtkradio.V1_3.IMtkRadioExIndication;

public class HwMtkRadioIndication extends IMtkRadioExIndication.Stub {
    private static final String LOG_TAG = "HwMtkRadioIndication";
    private static final int REGISTER_TYPE_MAX_TX_POWER = 4;
    private static final int SIGNAL_STRENGTH_DATA_LEN = 15;
    private HwMtkRIL mMtkRil;

    HwMtkRadioIndication(RIL ril) {
        this.mMtkRil = (HwMtkRIL) ril;
    }

    public static SignalStrength convertHwHalSignalStrength(int[] payload, int phoneId) {
        if (payload.length < SIGNAL_STRENGTH_DATA_LEN) {
            Rlog.i(LOG_TAG, "signal strength data is illegal");
            return new SignalStrength();
        }
        SignalStrength signalStrength = new SignalStrength(new CellSignalStrengthCdma(payload[4], payload[5], payload[6], payload[7], payload[8]), new CellSignalStrengthGsm(payload[0], payload[1], Integer.MAX_VALUE), new CellSignalStrengthWcdma(Integer.MAX_VALUE, Integer.MAX_VALUE, payload[2], Integer.MAX_VALUE, payload[3]), new CellSignalStrengthTdscdma(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE), new CellSignalStrengthLte(payload[9], payload[10], payload[11], payload[12], payload[13], Integer.MAX_VALUE), new CellSignalStrengthNr());
        signalStrength.setPhoneId(phoneId);
        return signalStrength;
    }

    public void currentSignalStrengthWithWcdmaEcioInd(int indicationType, SignalStrengthWithWcdmaEcio signalStrength) {
    }

    public void cfuStatusNotify(int indicationType, CfuStatusNotification cfuStatus) {
    }

    public void incomingCallIndication(int indicationType, IncomingCallNotification inCallNotify) {
    }

    public void cipherIndication(int indicationType, CipherNotification cipherNotify) {
    }

    public void crssIndication(int indicationType, CrssNotification crssNotification) {
    }

    public void speechCodecInfoIndication(int indicationType, int info) {
    }

    public void cdmaCallAccepted(int indicationType) {
    }

    public void eccNumIndication(int indicationType, String eccListWithCard, String eccListNoCard) {
    }

    public void responseCsNetworkStateChangeInd(int indicationType, ArrayList<String> arrayList) {
    }

    public void responsePsNetworkStateChangeInd(int indicationType, ArrayList<Integer> arrayList) {
    }

    public void responseNetworkEventInd(int indicationType, ArrayList<Integer> arrayList) {
    }

    public void responseModulationInfoInd(int indicationType, ArrayList<Integer> arrayList) {
    }

    public void responseInvalidSimInd(int indicationType, ArrayList<String> arrayList) {
    }

    public void responseFemtocellInfo(int indicationType, ArrayList<String> arrayList) {
    }

    public void responseLteNetworkInfo(int indicationType, int info) {
    }

    public void onMccMncChanged(int indicationType, String mccmnc) {
    }

    public void UnsolMsg(int indicationType, int msgId, RILUnsolMsgPayload payload) {
        this.mMtkRil.unsljLog(msgId);
        if (msgId == 2019) {
            simHotplugChanged(indicationType, processInts(payload));
        } else if (msgId == 2077) {
            currentHwSignalStrengthInd(indicationType, convertHwHalSignalStrength(processInts(payload), this.mMtkRil.mPhoneId.intValue()));
        }
    }

    public void currentHwSignalStrengthInd(int indicationType, SignalStrength signalStrength) {
        this.mMtkRil.processIndication(indicationType);
        this.mMtkRil.unsljLogvRet(1125, signalStrength);
        if (this.mMtkRil.mSignalStrengthRegistrant != null) {
            this.mMtkRil.mSignalStrengthRegistrant.notifyRegistrant(new AsyncResult((Object) null, signalStrength, (Throwable) null));
        }
    }

    private void simHotplugChanged(int indicationType, int[] states) {
        this.mMtkRil.processIndication(indicationType);
        this.mMtkRil.unsljLogvRet(1520, states);
        if (this.mMtkRil.mSimHotPlugRegistrants != null) {
            this.mMtkRil.mSimHotPlugRegistrants.notifyRegistrants(new AsyncResult((Object) null, states, (Throwable) null));
        }
    }

    private int[] processInts(RILUnsolMsgPayload payload) {
        int numInts = payload.nDatas.size();
        int[] response = new int[numInts];
        for (int i = 0; i < numInts; i++) {
            response[i] = ((Integer) payload.nDatas.get(i)).intValue();
        }
        return response;
    }

    public void onVirtualSimOn(int indicationType, int simInserted) {
    }

    public void onVirtualSimOff(int indicationType, int simInserted) {
    }

    public void onImeiLock(int indicationType) {
    }

    public void onImsiRefreshDone(int indicationType) {
    }

    public void onCardDetectedInd(int indicationType) {
    }

    public void newEtwsInd(int indicationType, EtwsNotification etws) {
    }

    public void meSmsStorageFullInd(int indicationType) {
    }

    public void smsReadyInd(int indicationType) {
    }

    public void dataAllowedNotification(int indicationType, int isAllowed) {
    }

    public void onPseudoCellInfoInd(int indicationType, ArrayList<Integer> arrayList) {
    }

    public void eMBMSSessionStatusIndication(int indicationType, int status) {
    }

    public void eMBMSAtInfoIndication(int indicationType, String info) {
    }

    public void plmnChangedIndication(int indicationType, ArrayList<String> arrayList) {
    }

    public void registrationSuspendedIndication(int indicationType, ArrayList<Integer> arrayList) {
    }

    public void gmssRatChangedIndication(int indicationType, ArrayList<Integer> arrayList) {
    }

    public void worldModeChangedIndication(int indicationType, ArrayList<Integer> arrayList) {
    }

    public void resetAttachApnInd(int indicationType) {
    }

    public void mdChangedApnInd(int indicationType, int apnClassType) {
    }

    public void esnMeidChangeInd(int indicationType, String esnMeid) {
    }

    public void phbReadyNotification(int indicationType, int isPhbReady) {
    }

    public void bipProactiveCommand(int indicationType, String cmd) {
    }

    public void triggerOtaSP(int indicationType) {
    }

    public void onStkMenuReset(int indicationType) {
    }

    public void onMdDataRetryCountReset(int indicationType) {
        this.mMtkRil.processIndication(indicationType);
        this.mMtkRil.unsljLog(1524);
        if (this.mMtkRil.mModemDataRetryRegistrants != null) {
            this.mMtkRil.mModemDataRetryRegistrants.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
        }
    }

    public void onRemoveRestrictEutran(int indicationType) {
    }

    public void onPcoStatus(int indicationType, ArrayList<Integer> arrayList) {
    }

    public void onLteAccessStratumStateChanged(int indicationType, ArrayList<Integer> arrayList) {
    }

    public void onSimPlugIn(int indicationType) {
    }

    public void onSimPlugOut(int indicationType) {
    }

    public void onSimMissing(int indicationType, int simInserted) {
    }

    public void onSimRecovery(int indicationType, int simInserted) {
    }

    public void onSimTrayPlugIn(int indicationType) {
    }

    public void onSimCommonSlotNoChanged(int indicationType) {
    }

    public void networkInfoInd(int indicationType, ArrayList<String> arrayList) {
    }

    public void onSimMeLockEvent(int indicationType) {
    }

    public void pcoDataAfterAttached(int indicationType, PcoDataAttachedInfo pco) {
    }

    public void confSRVCC(int indicationType, ArrayList<Integer> arrayList) {
    }

    public void volteLteConnectionStatus(int indicationType, ArrayList<Integer> arrayList) {
    }

    public void onVsimEventIndication(int indicationType, VsimOperationEvent event) {
        this.mMtkRil.processIndication(indicationType);
        int length = event.dataLength > 0 ? (event.dataLength / 2) + 4 : 0;
        ExternalSimManager.VsimEvent indicationEvent = new ExternalSimManager.VsimEvent(event.transactionId, event.eventId, length, 1 << this.mMtkRil.mInstanceId.intValue());
        if (length > 0) {
            indicationEvent.putInt(event.dataLength / 2);
            indicationEvent.putBytes(IccUtils.hexStringToBytes(event.data));
        }
        logi("onVsimEventIndication, len=" + event.dataLength + ",event=" + indicationEvent);
        if (this.mMtkRil.mVsimIndicationRegistrants != null) {
            this.mMtkRil.mVsimIndicationRegistrants.notifyRegistrants(new AsyncResult((Object) null, indicationEvent, (Throwable) null));
        }
    }

    public void dedicatedBearerActivationInd(int indicationType, DedicateDataCall ddcResult) {
    }

    public void dedicatedBearerModificationInd(int indicationType, DedicateDataCall ddcResult) {
    }

    public void dedicatedBearerDeactivationInd(int indicationType, int ddcResult) {
    }

    public void oemHookRaw(int indicationType, ArrayList<Byte> data) {
        this.mMtkRil.processIndication(indicationType);
        byte[] response = RIL.arrayListToPrimitiveArray(data);
        Rlog.d(LOG_TAG, IccUtils.bytesToHexString(response));
        if (this.mMtkRil.mUnsolOemHookRawRegistrant != null) {
            this.mMtkRil.mUnsolOemHookRawRegistrant.notifyRegistrant(new AsyncResult((Object) null, response, (Throwable) null));
        }
    }

    public void onTxPowerStatusIndication(int indicationType, ArrayList<Integer> arrayList) {
    }

    public void csconModeIndication(int indicationType, int info) {
        this.mMtkRil.processIndication(indicationType);
        logi("csconModeIndication: result = " + info);
        if (this.mMtkRil.mCsconModeInfoRegistrant != null) {
            this.mMtkRil.mCsconModeInfoRegistrant.notifyRegistrant(new AsyncResult((Object) null, Integer.valueOf(info), (Throwable) null));
        }
    }

    public void networkRejectCauseInd(int indicationType, ArrayList<Integer> arrayList) {
    }

    public void dsbpStateChanged(int indicationType, int dsbpState) {
    }

    public void smlSlotLockInfoChangedInd(int indicationType, ArrayList<Integer> arrayList) {
    }

    public void onSimPowerChangedInd(int indicationType, ArrayList<Integer> arrayList) {
    }

    public void networkBandInfoInd(int indicationType, ArrayList<Integer> arrayList) {
    }

    public void smsInfoExtInd(int indicationType, String info) {
    }

    public void onDsdaChangedInd(int indicationType, int suppSvcNotifyExmode) {
    }

    public void qualifiedNetworkTypesChangedInd(int indicationType, ArrayList<Integer> arrayList) {
    }

    public void onCellularQualityChangedInd(int indicationType, ArrayList<Integer> arrayList) {
    }

    public void mobileDataUsageInd(int indicationType, ArrayList<Integer> arrayList) {
    }

    public void onRsuSimLockEvent(int indicationType, int eventId) {
    }

    public void callAdditionalInfoInd(int indicationType, int ciType, ArrayList<String> arrayList) {
    }

    public void suppSvcNotifyEx(int indicationType, SuppSvcNotification suppSvc) {
    }

    public void onVirtualSimStatusChanged(int indicationType, int simInserted) {
    }

    public void onSimHotSwapInd(int indicationType, int event, String info) {
    }

    public void recPseBaseStationReport(int indicationType, int rat) {
        this.mMtkRil.processIndication(indicationType);
        logi("recPseBaseStationReport: result = " + rat);
        if (this.mMtkRil.mHwAntiFakeBaseStationRegistrants != null) {
            this.mMtkRil.mHwAntiFakeBaseStationRegistrants.notifyRegistrants(new AsyncResult((Object) null, Integer.valueOf(rat), (Throwable) null));
        }
    }

    public void onTxPowerIndication(int indicationType, ArrayList<Integer> indPower) {
        this.mMtkRil.processIndication(indicationType);
        this.mMtkRil.unsljLogRet(3117, indPower);
        if (indPower == null || indPower.size() == 0) {
            Rlog.e(LOG_TAG, "onTxPowerIndication error indPower is null!!!");
            return;
        }
        int data = indPower.get(0).intValue();
        ByteBuffer byteBuffer = ByteBuffer.allocate(7);
        byteBuffer.order(ByteOrder.nativeOrder());
        byteBuffer.put((byte) 4);
        byteBuffer.putShort(4);
        byteBuffer.putInt(data);
        byteBuffer.flip();
        this.mMtkRil.notifyAntOrMaxTxPowerInfo(byteBuffer.array());
    }

    private void logi(String msg) {
        Rlog.i("HwMtkRadioIndication[" + this.mMtkRil.mPhoneId + "]", msg);
    }

    public void onNwLimitInd(int indicationType, ArrayList<Integer> arrayList) {
    }
}
