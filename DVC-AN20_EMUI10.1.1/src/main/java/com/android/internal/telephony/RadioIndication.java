package com.android.internal.telephony;

import android.hardware.radio.V1_0.CallForwardInfo;
import android.hardware.radio.V1_0.CdmaCallWaiting;
import android.hardware.radio.V1_0.CdmaDisplayInfoRecord;
import android.hardware.radio.V1_0.CdmaInformationRecord;
import android.hardware.radio.V1_0.CdmaInformationRecords;
import android.hardware.radio.V1_0.CdmaLineControlInfoRecord;
import android.hardware.radio.V1_0.CdmaNumberInfoRecord;
import android.hardware.radio.V1_0.CdmaRedirectingNumberInfoRecord;
import android.hardware.radio.V1_0.CdmaSignalInfoRecord;
import android.hardware.radio.V1_0.CdmaSmsMessage;
import android.hardware.radio.V1_0.CdmaT53AudioControlInfoRecord;
import android.hardware.radio.V1_0.CdmaT53ClirInfoRecord;
import android.hardware.radio.V1_0.CellInfo;
import android.hardware.radio.V1_0.CfData;
import android.hardware.radio.V1_0.HardwareConfig;
import android.hardware.radio.V1_0.LceDataInfo;
import android.hardware.radio.V1_0.PcoDataInfo;
import android.hardware.radio.V1_0.RadioCapability;
import android.hardware.radio.V1_0.SetupDataCallResult;
import android.hardware.radio.V1_0.SignalStrength;
import android.hardware.radio.V1_0.SimRefreshResult;
import android.hardware.radio.V1_0.SsInfoData;
import android.hardware.radio.V1_0.StkCcUnsolSsResult;
import android.hardware.radio.V1_0.SuppSvcNotification;
import android.hardware.radio.V1_1.NetworkScanResult;
import android.hardware.radio.V1_2.LinkCapacityEstimate;
import android.hardware.radio.V1_4.EmergencyNumber;
import android.hardware.radio.V1_4.IRadioIndication;
import android.hardware.radio.V1_4.PhysicalChannelConfig;
import android.os.AsyncResult;
import android.os.SystemProperties;
import android.telephony.PcoData;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhysicalChannelConfig;
import android.telephony.ServiceState;
import android.telephony.SmsMessage;
import com.android.internal.telephony.cdma.CdmaCallWaitingNotification;
import com.android.internal.telephony.cdma.CdmaInformationRecords;
import com.android.internal.telephony.cdma.SmsMessageConverter;
import com.android.internal.telephony.dataconnection.KeepaliveStatus;
import com.android.internal.telephony.gsm.SsData;
import com.android.internal.telephony.gsm.SuppServiceNotification;
import com.android.internal.telephony.uicc.IccRefreshResponse;
import com.android.internal.telephony.uicc.IccUtils;
import com.google.android.mms.pdu.CharacterSets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RadioIndication extends IRadioIndication.Stub {
    RIL mRil;

    public RadioIndication() {
    }

    RadioIndication(RIL ril) {
        this.mRil = ril;
    }

    public void radioStateChanged(int indicationType, int radioState) {
        this.mRil.processIndication(indicationType);
        int state = getRadioStateFromInt(radioState);
        this.mRil.unsljLogMore(1000, "radioStateChanged: " + state);
        boolean z = false;
        this.mRil.setRadioState(state, false);
        RIL ril = this.mRil;
        if (state == 1) {
            z = true;
        }
        ril.handleUnsolicitedRadioStateChanged(z, this.mRil.getContext());
    }

    public void callStateChanged(int indicationType) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(1001);
        this.mRil.mCallStateRegistrants.notifyRegistrants();
    }

    public void networkStateChanged(int indicationType) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(1002);
        this.mRil.mNetworkStateRegistrants.notifyRegistrants();
    }

    public void newSms(int indicationType, ArrayList<Byte> pdu) {
        this.mRil.processIndication(indicationType);
        byte[] pduArray = RIL.arrayListToPrimitiveArray(pdu);
        this.mRil.unsljLog(1003);
        SmsMessage sms = SmsMessage.newFromCMT(pduArray);
        if (this.mRil.mGsmSmsRegistrant != null) {
            this.mRil.mGsmSmsRegistrant.notifyRegistrant(new AsyncResult((Object) null, sms, (Throwable) null));
        }
    }

    public void newSmsStatusReport(int indicationType, ArrayList<Byte> pdu) {
        this.mRil.processIndication(indicationType);
        byte[] pduArray = RIL.arrayListToPrimitiveArray(pdu);
        this.mRil.unsljLog(1004);
        if (this.mRil.mSmsStatusRegistrant != null) {
            this.mRil.mSmsStatusRegistrant.notifyRegistrant(new AsyncResult((Object) null, pduArray, (Throwable) null));
        }
    }

    public void newSmsOnSim(int indicationType, int recordNumber) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(1005);
        if (this.mRil.mSmsOnSimRegistrant != null) {
            this.mRil.mSmsOnSimRegistrant.notifyRegistrant(new AsyncResult((Object) null, Integer.valueOf(recordNumber), (Throwable) null));
        }
    }

    public void onUssd(int indicationType, int ussdModeType, String msg) {
        this.mRil.processIndication(indicationType);
        RIL ril = this.mRil;
        ril.unsljLogMore(1006, PhoneConfigurationManager.SSSS + ussdModeType);
        String[] resp = {PhoneConfigurationManager.SSSS + ussdModeType, msg};
        if (this.mRil.mUSSDRegistrant != null) {
            this.mRil.mUSSDRegistrant.notifyRegistrant(new AsyncResult((Object) null, resp, (Throwable) null));
        }
    }

    public void nitzTimeReceived(int indicationType, String nitzTime, long receivedTime) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLogRet(CallFailCause.CDMA_NOT_EMERGENCY, nitzTime);
        Object[] result = {nitzTime, Long.valueOf(receivedTime)};
        if (SystemProperties.getBoolean("telephony.test.ignore.nitz", false)) {
            this.mRil.riljLog("ignoring UNSOL_NITZ_TIME_RECEIVED");
            return;
        }
        if (this.mRil.mNITZTimeRegistrant != null) {
            this.mRil.mNITZTimeRegistrant.notifyRegistrant(new AsyncResult((Object) null, result, (Throwable) null));
        }
        this.mRil.mLastNITZTimeInfo = result;
    }

    public void currentSignalStrength(int indicationType, SignalStrength signalStrength) {
    }

    public void currentLinkCapacityEstimate(int indicationType, LinkCapacityEstimate lce) {
        this.mRil.processIndication(indicationType);
        LinkCapacityEstimate response = RIL.convertHalLceData(lce, this.mRil);
        this.mRil.unsljLogRet(1045, response);
        if (this.mRil.mLceInfoRegistrants != null) {
            this.mRil.mLceInfoRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
        }
    }

    public void currentSignalStrength_1_2(int indicationType, android.hardware.radio.V1_2.SignalStrength signalStrength) {
        this.mRil.processIndication(indicationType);
        android.telephony.SignalStrength ss = new android.telephony.SignalStrength(signalStrength);
        if (this.mRil.mSignalStrengthRegistrant != null) {
            this.mRil.mSignalStrengthRegistrant.notifyRegistrant(new AsyncResult((Object) null, ss, (Throwable) null));
        }
    }

    public void currentSignalStrength_1_4(int indicationType, android.hardware.radio.V1_4.SignalStrength signalStrength) {
    }

    public void currentPhysicalChannelConfigs_1_4(int indicationType, ArrayList<PhysicalChannelConfig> configs) {
        this.mRil.processIndication(indicationType);
        physicalChannelConfigsIndication(configs);
    }

    public void currentPhysicalChannelConfigs(int indicationType, ArrayList<android.hardware.radio.V1_2.PhysicalChannelConfig> configs) {
        this.mRil.processIndication(indicationType);
        physicalChannelConfigsIndication(configs);
    }

    public void currentEmergencyNumberList(int indicationType, ArrayList<EmergencyNumber> emergencyNumberList) {
        List<android.telephony.emergency.EmergencyNumber> response = new ArrayList<>(emergencyNumberList.size());
        Iterator<EmergencyNumber> it = emergencyNumberList.iterator();
        while (it.hasNext()) {
            EmergencyNumber emergencyNumberHal = it.next();
            response.add(new android.telephony.emergency.EmergencyNumber(emergencyNumberHal.number, MccTable.countryCodeForMcc(emergencyNumberHal.mcc), emergencyNumberHal.mnc, emergencyNumberHal.categories, emergencyNumberHal.urns, emergencyNumberHal.sources, 0));
        }
        this.mRil.unsljLogRet(1102, response);
        this.mRil.mEmergencyNumberListRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
    }

    public void dataCallListChanged(int indicationType, ArrayList<SetupDataCallResult> dcList) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLogRet(1010, dcList);
        this.mRil.mDataCallListChangedRegistrants.notifyRegistrants(new AsyncResult((Object) null, RIL.convertDataCallResultList(dcList), (Throwable) null));
    }

    public void dataCallListChanged_1_4(int indicationType, ArrayList<android.hardware.radio.V1_4.SetupDataCallResult> dcList) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLogRet(1010, null);
        this.mRil.mDataCallListChangedRegistrants.notifyRegistrants(new AsyncResult((Object) null, RIL.convertDataCallResultList(dcList), (Throwable) null));
    }

    public void suppSvcNotify(int indicationType, SuppSvcNotification suppSvcNotification) {
        this.mRil.processIndication(indicationType);
        SuppServiceNotification notification = new SuppServiceNotification();
        notification.notificationType = suppSvcNotification.isMT ? 1 : 0;
        notification.code = suppSvcNotification.code;
        notification.index = suppSvcNotification.index;
        notification.type = suppSvcNotification.type;
        notification.number = suppSvcNotification.number;
        this.mRil.unsljLogRet(1011, notification);
        if (this.mRil.mSsnRegistrant != null) {
            this.mRil.mSsnRegistrant.notifyRegistrant(new AsyncResult((Object) null, notification, (Throwable) null));
        }
    }

    public void stkSessionEnd(int indicationType) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(1012);
        if (this.mRil.mCatSessionEndRegistrant != null) {
            this.mRil.mCatSessionEndRegistrant.notifyRegistrant(new AsyncResult((Object) null, (Object) null, (Throwable) null));
        }
    }

    public void stkProactiveCommand(int indicationType, String cmd) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(1013);
        if (this.mRil.mCatProCmdRegistrant != null) {
            this.mRil.mCatProCmdRegistrant.notifyRegistrant(new AsyncResult((Object) null, cmd, (Throwable) null));
        }
    }

    public void stkEventNotify(int indicationType, String cmd) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(1014);
        if (this.mRil.mCatEventRegistrant != null) {
            this.mRil.mCatEventRegistrant.notifyRegistrant(new AsyncResult((Object) null, cmd, (Throwable) null));
        }
    }

    public void stkCallSetup(int indicationType, long timeout) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLogRet(CharacterSets.UTF_16, Long.valueOf(timeout));
        if (this.mRil.mCatCallSetUpRegistrant != null) {
            this.mRil.mCatCallSetUpRegistrant.notifyRegistrant(new AsyncResult((Object) null, Long.valueOf(timeout), (Throwable) null));
        }
    }

    public void simSmsStorageFull(int indicationType) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(1016);
        if (this.mRil.mIccSmsFullRegistrant != null) {
            this.mRil.mIccSmsFullRegistrant.notifyRegistrant();
        }
    }

    public void simRefresh(int indicationType, SimRefreshResult refreshResult) {
        this.mRil.processIndication(indicationType);
        IccRefreshResponse response = new IccRefreshResponse();
        response.refreshResult = refreshResult.type;
        response.efId = refreshResult.efId;
        response.aid = refreshResult.aid;
        this.mRil.unsljLogRet(1017, response);
        this.mRil.mIccRefreshRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
    }

    public void callRing(int indicationType, boolean isGsm, CdmaSignalInfoRecord record) {
        this.mRil.processIndication(indicationType);
        char[] response = null;
        if (!isGsm) {
            response = new char[]{record.isPresent ? (char) 1 : 0, (char) record.signalType, (char) record.alertPitch, (char) record.signal};
            this.mRil.writeMetricsCallRing(response);
        }
        this.mRil.unsljLogRet(1018, response);
        if (this.mRil.mRingRegistrant != null) {
            this.mRil.mRingRegistrant.notifyRegistrant(new AsyncResult((Object) null, response, (Throwable) null));
        }
    }

    public void simStatusChanged(int indicationType) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(1019);
        this.mRil.mIccStatusChangedRegistrants.notifyRegistrants();
    }

    public void cdmaNewSms(int indicationType, CdmaSmsMessage msg) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(1020);
        SmsMessage sms = SmsMessageConverter.newSmsMessageFromCdmaSmsMessage(msg);
        if (this.mRil.mCdmaSmsRegistrant != null) {
            this.mRil.mCdmaSmsRegistrant.notifyRegistrant(new AsyncResult((Object) null, sms, (Throwable) null));
        }
    }

    public void newBroadcastSms(int indicationType, ArrayList<Byte> data) {
        this.mRil.processIndication(indicationType);
        byte[] response = RIL.arrayListToPrimitiveArray(data);
        this.mRil.unsljLogvRet(1021, IccUtils.bytesToHexString(response));
        if (this.mRil.mGsmBroadcastSmsRegistrant != null) {
            this.mRil.mGsmBroadcastSmsRegistrant.notifyRegistrant(new AsyncResult((Object) null, response, (Throwable) null));
        }
    }

    public void cdmaRuimSmsStorageFull(int indicationType) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(1022);
        if (this.mRil.mIccSmsFullRegistrant != null) {
            this.mRil.mIccSmsFullRegistrant.notifyRegistrant();
        }
    }

    public void restrictedStateChanged(int indicationType, int state) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLogvRet(1023, Integer.valueOf(state));
        if (this.mRil.mRestrictedStateRegistrant != null) {
            this.mRil.mRestrictedStateRegistrant.notifyRegistrant(new AsyncResult((Object) null, Integer.valueOf(state), (Throwable) null));
        }
    }

    public void enterEmergencyCallbackMode(int indicationType) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(1024);
        if (this.mRil.mEmergencyCallbackModeRegistrant != null) {
            this.mRil.mEmergencyCallbackModeRegistrant.notifyRegistrant();
        }
    }

    public void cdmaCallWaiting(int indicationType, CdmaCallWaiting callWaitingRecord) {
        this.mRil.processIndication(indicationType);
        CdmaCallWaitingNotification notification = new CdmaCallWaitingNotification();
        notification.number = callWaitingRecord.number;
        notification.numberPresentation = CdmaCallWaitingNotification.presentationFromCLIP(callWaitingRecord.numberPresentation);
        notification.name = callWaitingRecord.name;
        notification.namePresentation = notification.numberPresentation;
        notification.isPresent = callWaitingRecord.signalInfoRecord.isPresent ? 1 : 0;
        notification.signalType = callWaitingRecord.signalInfoRecord.signalType;
        notification.alertPitch = callWaitingRecord.signalInfoRecord.alertPitch;
        notification.signal = callWaitingRecord.signalInfoRecord.signal;
        notification.numberType = callWaitingRecord.numberType;
        notification.numberPlan = callWaitingRecord.numberPlan;
        if (notification.numberType == 1) {
            notification.number = PhoneNumberUtils.stringFromStringAndTOA(notification.number, 145);
        }
        this.mRil.unsljLogRet(1025, notification);
        this.mRil.mCallWaitingInfoRegistrants.notifyRegistrants(new AsyncResult((Object) null, notification, (Throwable) null));
    }

    public void cdmaOtaProvisionStatus(int indicationType, int status) {
        this.mRil.processIndication(indicationType);
        int[] response = {status};
        this.mRil.unsljLogRet(1026, response);
        this.mRil.mOtaProvisionRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
    }

    public void cdmaInfoRec(int indicationType, CdmaInformationRecords records) {
        com.android.internal.telephony.cdma.CdmaInformationRecords cdmaInformationRecords;
        this.mRil.processIndication(indicationType);
        int numberOfInfoRecs = records.infoRec.size();
        for (int i = 0; i < numberOfInfoRecs; i++) {
            CdmaInformationRecord record = (CdmaInformationRecord) records.infoRec.get(i);
            int id = record.name;
            switch (id) {
                case 0:
                case 7:
                    cdmaInformationRecords = new com.android.internal.telephony.cdma.CdmaInformationRecords(new CdmaInformationRecords.CdmaDisplayInfoRec(id, ((CdmaDisplayInfoRecord) record.display.get(0)).alphaBuf));
                    break;
                case 1:
                case 2:
                case 3:
                    CdmaNumberInfoRecord numInfoRecord = (CdmaNumberInfoRecord) record.number.get(0);
                    cdmaInformationRecords = new com.android.internal.telephony.cdma.CdmaInformationRecords(new CdmaInformationRecords.CdmaNumberInfoRec(id, numInfoRecord.number, numInfoRecord.numberType, numInfoRecord.numberPlan, numInfoRecord.pi, numInfoRecord.si));
                    break;
                case 4:
                    CdmaSignalInfoRecord signalInfoRecord = (CdmaSignalInfoRecord) record.signal.get(0);
                    boolean z = signalInfoRecord.isPresent;
                    cdmaInformationRecords = new com.android.internal.telephony.cdma.CdmaInformationRecords(new CdmaInformationRecords.CdmaSignalInfoRec(z ? 1 : 0, signalInfoRecord.signalType, signalInfoRecord.alertPitch, signalInfoRecord.signal));
                    break;
                case 5:
                    CdmaRedirectingNumberInfoRecord redirectingNumberInfoRecord = (CdmaRedirectingNumberInfoRecord) record.redir.get(0);
                    cdmaInformationRecords = new com.android.internal.telephony.cdma.CdmaInformationRecords(new CdmaInformationRecords.CdmaRedirectingNumberInfoRec(redirectingNumberInfoRecord.redirectingNumber.number, redirectingNumberInfoRecord.redirectingNumber.numberType, redirectingNumberInfoRecord.redirectingNumber.numberPlan, redirectingNumberInfoRecord.redirectingNumber.pi, redirectingNumberInfoRecord.redirectingNumber.si, redirectingNumberInfoRecord.redirectingReason));
                    break;
                case 6:
                    CdmaLineControlInfoRecord lineControlInfoRecord = (CdmaLineControlInfoRecord) record.lineCtrl.get(0);
                    cdmaInformationRecords = new com.android.internal.telephony.cdma.CdmaInformationRecords(new CdmaInformationRecords.CdmaLineControlInfoRec(lineControlInfoRecord.lineCtrlPolarityIncluded, lineControlInfoRecord.lineCtrlToggle, lineControlInfoRecord.lineCtrlReverse, lineControlInfoRecord.lineCtrlPowerDenial));
                    break;
                case 8:
                    cdmaInformationRecords = new com.android.internal.telephony.cdma.CdmaInformationRecords(new CdmaInformationRecords.CdmaT53ClirInfoRec(((CdmaT53ClirInfoRecord) record.clir.get(0)).cause));
                    break;
                case 9:
                default:
                    throw new RuntimeException("RIL_UNSOL_CDMA_INFO_REC: unsupported record. Got " + com.android.internal.telephony.cdma.CdmaInformationRecords.idToString(id) + " ");
                case 10:
                    CdmaT53AudioControlInfoRecord audioControlInfoRecord = (CdmaT53AudioControlInfoRecord) record.audioCtrl.get(0);
                    cdmaInformationRecords = new com.android.internal.telephony.cdma.CdmaInformationRecords(new CdmaInformationRecords.CdmaT53AudioControlInfoRec(audioControlInfoRecord.upLink, audioControlInfoRecord.downLink));
                    break;
            }
            this.mRil.unsljLogRet(1027, cdmaInformationRecords);
            this.mRil.notifyRegistrantsCdmaInfoRec(cdmaInformationRecords);
        }
    }

    public void indicateRingbackTone(int indicationType, boolean start) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLogvRet(1029, Boolean.valueOf(start));
        this.mRil.mRingbackToneRegistrants.notifyRegistrants(new AsyncResult((Object) null, Boolean.valueOf(start), (Throwable) null));
    }

    public void resendIncallMute(int indicationType) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(1030);
        this.mRil.mResendIncallMuteRegistrants.notifyRegistrants();
    }

    public void cdmaSubscriptionSourceChanged(int indicationType, int cdmaSource) {
        this.mRil.processIndication(indicationType);
        int[] response = {cdmaSource};
        this.mRil.unsljLogRet(1031, response);
        this.mRil.mCdmaSubscriptionChangedRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
    }

    public void cdmaPrlChanged(int indicationType, int version) {
        this.mRil.processIndication(indicationType);
        int[] response = {version};
        this.mRil.unsljLogRet(1032, response);
        this.mRil.mCdmaPrlChangedRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
    }

    public void exitEmergencyCallbackMode(int indicationType) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(1033);
        this.mRil.mExitEmergencyCallbackModeRegistrants.notifyRegistrants();
    }

    public void rilConnected(int indicationType) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(1034);
        RIL ril = this.mRil;
        ril.setCellInfoListRate(KeepaliveStatus.INVALID_HANDLE, null, ril.getMRILDefaultWorkSource());
        this.mRil.notifyRegistrantsRilConnectionChanged(15);
    }

    public void voiceRadioTechChanged(int indicationType, int rat) {
        this.mRil.processIndication(indicationType);
        int[] response = {rat};
        RIL ril = this.mRil;
        ril.mLastRadioTech = rat;
        ril.unsljLogRet(1035, response);
        this.mRil.mVoiceRadioTechChangedRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
    }

    public void cellInfoList(int indicationType, ArrayList<CellInfo> records) {
        this.mRil.processIndication(indicationType);
        ArrayList<android.telephony.CellInfo> response = RIL.convertHalCellInfoList(records);
        this.mRil.unsljLogRet(1036, response);
        this.mRil.mRilCellInfoListRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
    }

    public void cellInfoList_1_2(int indicationType, ArrayList<android.hardware.radio.V1_2.CellInfo> records) {
        this.mRil.processIndication(indicationType);
        ArrayList<android.telephony.CellInfo> response = RIL.convertHalCellInfoList_1_2(records);
        this.mRil.unsljLogRet(1036, response);
        this.mRil.mRilCellInfoListRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
    }

    public void cellInfoList_1_4(int indicationType, ArrayList<android.hardware.radio.V1_4.CellInfo> records) {
        this.mRil.processIndication(indicationType);
        ArrayList<android.telephony.CellInfo> response = RIL.convertHalCellInfoList_1_4(records);
        this.mRil.unsljLogRet(1036, response);
        this.mRil.mRilCellInfoListRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
    }

    public void networkScanResult(int indicationType, NetworkScanResult result) {
        responseNetworkScan(indicationType, result);
    }

    public void networkScanResult_1_2(int indicationType, android.hardware.radio.V1_2.NetworkScanResult result) {
        responseNetworkScan_1_2(indicationType, result);
    }

    public void networkScanResult_1_4(int indicationType, android.hardware.radio.V1_4.NetworkScanResult result) {
        responseNetworkScan_1_4(indicationType, result);
    }

    public void imsNetworkStateChanged(int indicationType) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(1037);
        this.mRil.mImsNetworkStateChangedRegistrants.notifyRegistrants();
    }

    public void subscriptionStatusChanged(int indicationType, boolean activate) {
        this.mRil.processIndication(indicationType);
        int[] response = {activate ? 1 : 0};
        this.mRil.unsljLogRet(1038, response);
        this.mRil.mSubscriptionStatusRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
    }

    public void srvccStateNotify(int indicationType, int state) {
        this.mRil.processIndication(indicationType);
        int[] response = {state};
        this.mRil.unsljLogRet(1039, response);
        this.mRil.writeMetricsSrvcc(state);
        this.mRil.mSrvccStateRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
    }

    public void hardwareConfigChanged(int indicationType, ArrayList<HardwareConfig> configs) {
        this.mRil.processIndication(indicationType);
        ArrayList<HardwareConfig> response = RIL.convertHalHwConfigList(configs, this.mRil);
        this.mRil.unsljLogRet(1040, response);
        this.mRil.mHardwareConfigChangeRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
    }

    public void radioCapabilityIndication(int indicationType, RadioCapability rc) {
        this.mRil.processIndication(indicationType);
        RadioCapability response = RIL.convertHalRadioCapability(rc, this.mRil);
        this.mRil.unsljLogRet(1042, response);
        this.mRil.mPhoneRadioCapabilityChangedRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
    }

    public void onSupplementaryServiceIndication(int indicationType, StkCcUnsolSsResult ss) {
        this.mRil.processIndication(indicationType);
        SsData ssData = new SsData();
        ssData.serviceType = ssData.ServiceTypeFromRILInt(ss.serviceType);
        ssData.requestType = ssData.RequestTypeFromRILInt(ss.requestType);
        ssData.teleserviceType = ssData.TeleserviceTypeFromRILInt(ss.teleserviceType);
        ssData.serviceClass = ss.serviceClass;
        ssData.result = ss.result;
        if (!ssData.serviceType.isTypeCF() || !ssData.requestType.isTypeInterrogation()) {
            SsInfoData ssInfo = (SsInfoData) ss.ssInfo.get(0);
            int num = ssInfo.ssInfo.size();
            ssData.ssInfo = new int[num];
            for (int i = 0; i < num; i++) {
                ssData.ssInfo[i] = ((Integer) ssInfo.ssInfo.get(i)).intValue();
                RIL ril = this.mRil;
                ril.riljLog("[SS Data] SS Info " + i + " : " + ssData.ssInfo[i]);
            }
        } else {
            CfData cfData = (CfData) ss.cfData.get(0);
            int num2 = cfData.cfInfo.size();
            ssData.cfInfo = new CallForwardInfo[num2];
            for (int i2 = 0; i2 < num2; i2++) {
                CallForwardInfo cfInfo = (CallForwardInfo) cfData.cfInfo.get(i2);
                ssData.cfInfo[i2] = new CallForwardInfo();
                ssData.cfInfo[i2].status = cfInfo.status;
                ssData.cfInfo[i2].reason = cfInfo.reason;
                ssData.cfInfo[i2].serviceClass = cfInfo.serviceClass;
                ssData.cfInfo[i2].toa = cfInfo.toa;
                ssData.cfInfo[i2].number = cfInfo.number;
                ssData.cfInfo[i2].timeSeconds = cfInfo.timeSeconds;
                RIL ril2 = this.mRil;
                ril2.riljLog("[SS Data] CF Info " + i2 + " : " + ssData.cfInfo[i2]);
            }
        }
        this.mRil.unsljLogRet(1043, ssData);
        if (this.mRil.mSsRegistrant != null) {
            this.mRil.mSsRegistrant.notifyRegistrant(new AsyncResult((Object) null, ssData, (Throwable) null));
        }
    }

    public void stkCallControlAlphaNotify(int indicationType, String alpha) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLogRet(1044, alpha);
        if (this.mRil.mCatCcAlphaRegistrant != null) {
            this.mRil.mCatCcAlphaRegistrant.notifyRegistrant(new AsyncResult((Object) null, alpha, (Throwable) null));
        }
    }

    public void lceData(int indicationType, LceDataInfo lce) {
        this.mRil.processIndication(indicationType);
        LinkCapacityEstimate response = RIL.convertHalLceData(lce, this.mRil);
        this.mRil.unsljLogRet(1045, response);
        if (this.mRil.mLceInfoRegistrants != null) {
            this.mRil.mLceInfoRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
        }
    }

    public void pcoData(int indicationType, PcoDataInfo pco) {
        this.mRil.processIndication(indicationType);
        PcoData response = new PcoData(pco.cid, pco.bearerProto, pco.pcoId, RIL.arrayListToPrimitiveArray(pco.contents));
        this.mRil.unsljLogRet(1046, response);
        this.mRil.mPcoDataRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
    }

    public void modemReset(int indicationType, String reason) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLogRet(1047, reason);
        this.mRil.writeMetricsModemRestartEvent(reason);
        this.mRil.mModemResetRegistrants.notifyRegistrants(new AsyncResult((Object) null, reason, (Throwable) null));
    }

    public void carrierInfoForImsiEncryption(int indicationType) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLogRet(1048, null);
        this.mRil.mCarrierInfoForImsiEncryptionRegistrants.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
    }

    public void keepaliveStatus(int indicationType, android.hardware.radio.V1_1.KeepaliveStatus halStatus) {
        this.mRil.processIndication(indicationType);
        RIL ril = this.mRil;
        ril.unsljLogRet(1050, "handle=" + halStatus.sessionHandle + " code=" + halStatus.code);
        this.mRil.mNattKeepaliveStatusRegistrants.notifyRegistrants(new AsyncResult((Object) null, new KeepaliveStatus(halStatus.sessionHandle, halStatus.code), (Throwable) null));
    }

    private int getRadioStateFromInt(int stateInt) {
        int newState = convertRadioState(stateInt);
        if (newState == 0) {
            return 0;
        }
        if (newState == 1) {
            return 2;
        }
        if (newState == 10) {
            return 1;
        }
        throw new RuntimeException("Unrecognized RadioState: " + stateInt);
    }

    private void setFrequencyRangeOrChannelNumber(PhysicalChannelConfig.Builder builder, android.hardware.radio.V1_4.PhysicalChannelConfig config) {
        byte discriminator = config.rfInfo.getDiscriminator();
        if (discriminator == 0) {
            builder.setFrequencyRange(config.rfInfo.range());
        } else if (discriminator != 1) {
            RIL ril = this.mRil;
            ril.riljLoge("Unsupported frequency type " + ((int) config.rfInfo.getDiscriminator()));
        } else {
            builder.setChannelNumber(config.rfInfo.channelNumber());
        }
    }

    private int convertConnectionStatusFromCellConnectionStatus(int status) {
        if (status == 1) {
            return 1;
        }
        if (status == 2) {
            return 2;
        }
        RIL ril = this.mRil;
        ril.riljLoge("Unsupported CellConnectionStatus in PhysicalChannelConfig: " + status);
        return KeepaliveStatus.INVALID_HANDLE;
    }

    private void physicalChannelConfigsIndication(List<? extends Object> configs) {
        List<android.telephony.PhysicalChannelConfig> response = new ArrayList<>(configs.size());
        for (Object obj : configs) {
            if (obj instanceof android.hardware.radio.V1_2.PhysicalChannelConfig) {
                android.hardware.radio.V1_2.PhysicalChannelConfig config = (android.hardware.radio.V1_2.PhysicalChannelConfig) obj;
                response.add(new PhysicalChannelConfig.Builder().setCellConnectionStatus(convertConnectionStatusFromCellConnectionStatus(config.status)).setCellBandwidthDownlinkKhz(config.cellBandwidthDownlink).build());
            } else if (obj instanceof android.hardware.radio.V1_4.PhysicalChannelConfig) {
                android.hardware.radio.V1_4.PhysicalChannelConfig config2 = (android.hardware.radio.V1_4.PhysicalChannelConfig) obj;
                PhysicalChannelConfig.Builder builder = new PhysicalChannelConfig.Builder();
                setFrequencyRangeOrChannelNumber(builder, config2);
                response.add(builder.setCellConnectionStatus(convertConnectionStatusFromCellConnectionStatus(config2.base.status)).setCellBandwidthDownlinkKhz(config2.base.cellBandwidthDownlink).setRat(ServiceState.rilRadioTechnologyToNetworkType(config2.rat)).setPhysicalCellId(config2.physicalCellId).setContextIds(config2.contextIds.stream().mapToInt($$Lambda$RadioIndication$GND6XxOOm1d_Ro76zEUFjA9OrEA.INSTANCE).toArray()).build());
            } else {
                RIL ril = this.mRil;
                ril.riljLoge("Unsupported PhysicalChannelConfig " + obj);
            }
        }
        this.mRil.unsljLogRet(1101, response);
        this.mRil.mPhysicalChannelConfigurationRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
    }

    private void responseNetworkScan(int indicationType, NetworkScanResult result) {
        this.mRil.processIndication(indicationType);
        NetworkScanResult nsr = new NetworkScanResult(result.status, result.error, RIL.convertHalCellInfoList(result.networkInfos));
        this.mRil.unsljLogRet(1049, nsr);
        this.mRil.mRilNetworkScanResultRegistrants.notifyRegistrants(new AsyncResult((Object) null, nsr, (Throwable) null));
    }

    private int convertRadioState(int stateInt) {
        if (stateInt <= 1 || stateInt >= 10) {
            return stateInt;
        }
        return 10;
    }

    private void responseNetworkScan_1_2(int indicationType, android.hardware.radio.V1_2.NetworkScanResult result) {
        this.mRil.processIndication(indicationType);
        NetworkScanResult nsr = new NetworkScanResult(result.status, result.error, RIL.convertHalCellInfoList_1_2(result.networkInfos));
        this.mRil.unsljLogRet(1049, nsr);
        this.mRil.mRilNetworkScanResultRegistrants.notifyRegistrants(new AsyncResult((Object) null, nsr, (Throwable) null));
    }

    private void responseNetworkScan_1_4(int indicationType, android.hardware.radio.V1_4.NetworkScanResult result) {
        this.mRil.processIndication(indicationType);
        NetworkScanResult nsr = new NetworkScanResult(result.status, result.error, RIL.convertHalCellInfoList_1_4(result.networkInfos));
        this.mRil.unsljLogRet(1049, nsr);
        this.mRil.mRilNetworkScanResultRegistrants.notifyRegistrants(new AsyncResult((Object) null, nsr, (Throwable) null));
    }
}
