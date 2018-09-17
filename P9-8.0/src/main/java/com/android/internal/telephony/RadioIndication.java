package com.android.internal.telephony;

import android.hardware.radio.V1_0.ApnTypes;
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
import android.hardware.radio.V1_0.RadioAccessFamily;
import android.hardware.radio.V1_0.RadioCapability;
import android.hardware.radio.V1_0.SetupDataCallResult;
import android.hardware.radio.V1_0.SignalStrength;
import android.hardware.radio.V1_0.SimRefreshResult;
import android.hardware.radio.V1_0.SsInfoData;
import android.hardware.radio.V1_0.StkCcUnsolSsResult;
import android.hardware.radio.V1_0.SuppSvcNotification;
import android.os.AsyncResult;
import android.os.SystemProperties;
import android.telephony.PcoData;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.util.Jlog;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.cdma.CdmaCallWaitingNotification;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaDisplayInfoRec;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaLineControlInfoRec;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaNumberInfoRec;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaRedirectingNumberInfoRec;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaSignalInfoRec;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaT53AudioControlInfoRec;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaT53ClirInfoRec;
import com.android.internal.telephony.cdma.SmsMessageConverter;
import com.android.internal.telephony.dataconnection.DataCallResponse;
import com.android.internal.telephony.gsm.SsData;
import com.android.internal.telephony.gsm.SuppServiceNotification;
import com.android.internal.telephony.uicc.IccRefreshResponse;
import com.android.internal.telephony.uicc.IccUtils;
import com.google.android.mms.pdu.CharacterSets;
import java.util.ArrayList;
import vendor.huawei.hardware.radio.V1_0.IRadioIndication.Stub;

public abstract class RadioIndication extends Stub {
    RIL mRil;

    RadioIndication(RIL ril) {
        this.mRil = ril;
    }

    public void radioStateChanged(int indicationType, int radioState) {
        this.mRil.processIndication(indicationType);
        RadioState newState = getRadioStateFromInt(radioState);
        this.mRil.unsljLogMore(1000, "radioStateChanged: " + newState);
        this.mRil.setRadioState(newState);
        this.mRil.handleUnsolicitedRadioStateChanged(newState.isOn(), this.mRil.getContext());
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
        this.mRil.writeMetricsNewSms(1, 1);
        SmsMessage sms = SmsMessage.newFromCMT(pduArray);
        if (this.mRil.mGsmSmsRegistrant != null) {
            Jlog.d(47, "JL_RIL_RESPONSE_NEW_SMS");
            this.mRil.mGsmSmsRegistrant.notifyRegistrant(new AsyncResult(null, sms, null));
        }
    }

    public void newSmsStatusReport(int indicationType, ArrayList<Byte> pdu) {
        this.mRil.processIndication(indicationType);
        byte[] pduArray = RIL.arrayListToPrimitiveArray(pdu);
        this.mRil.unsljLog(1004);
        if (this.mRil.mSmsStatusRegistrant != null) {
            this.mRil.mSmsStatusRegistrant.notifyRegistrant(new AsyncResult(null, pduArray, null));
        }
    }

    public void newSmsOnSim(int indicationType, int recordNumber) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(1005);
        if (this.mRil.mSmsOnSimRegistrant != null) {
            this.mRil.mSmsOnSimRegistrant.notifyRegistrant(new AsyncResult(null, Integer.valueOf(recordNumber), null));
        }
    }

    public void onUssd(int indicationType, int ussdModeType, String msg) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLogMore(1006, "" + ussdModeType);
        String[] resp = new String[]{"" + ussdModeType, msg};
        if (this.mRil.mUSSDRegistrant != null) {
            this.mRil.mUSSDRegistrant.notifyRegistrant(new AsyncResult(null, resp, null));
        }
    }

    public void nitzTimeReceived(int indicationType, String nitzTime, long receivedTime) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLogRet(1008, nitzTime);
        Object[] result = new Object[]{nitzTime, Long.valueOf(receivedTime)};
        if (SystemProperties.getBoolean("telephony.test.ignore.nitz", false)) {
            this.mRil.riljLog("ignoring UNSOL_NITZ_TIME_RECEIVED");
            return;
        }
        if (this.mRil.mNITZTimeRegistrant != null) {
            this.mRil.mNITZTimeRegistrant.notifyRegistrant(new AsyncResult(null, result, null));
        }
        this.mRil.mLastNITZTimeInfo = result;
    }

    public void currentSignalStrength(int indicationType, SignalStrength signalStrength) {
    }

    public void dataCallListChanged(int indicationType, ArrayList<SetupDataCallResult> dcList) {
        this.mRil.processIndication(indicationType);
        ArrayList<DataCallResponse> response = new ArrayList();
        for (SetupDataCallResult dcResult : dcList) {
            response.add(RIL.convertDataCallResult(dcResult));
        }
        this.mRil.unsljLogRet(1010, response);
        this.mRil.mDataCallListChangedRegistrants.notifyRegistrants(new AsyncResult(null, response, null));
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
            this.mRil.mSsnRegistrant.notifyRegistrant(new AsyncResult(null, notification, null));
        }
    }

    public void stkSessionEnd(int indicationType) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(1012);
        if (this.mRil.mCatSessionEndRegistrant != null) {
            this.mRil.mCatSessionEndRegistrant.notifyRegistrant(new AsyncResult(null, null, null));
        }
    }

    public void stkProactiveCommand(int indicationType, String cmd) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(1013);
        if (this.mRil.mCatProCmdRegistrant != null) {
            this.mRil.mCatProCmdRegistrant.notifyRegistrant(new AsyncResult(null, cmd, null));
        }
    }

    public void stkEventNotify(int indicationType, String cmd) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(1014);
        if (this.mRil.mCatEventRegistrant != null) {
            this.mRil.mCatEventRegistrant.notifyRegistrant(new AsyncResult(null, cmd, null));
        }
    }

    public void stkCallSetup(int indicationType, long timeout) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLogRet(CharacterSets.UTF_16, Long.valueOf(timeout));
        if (this.mRil.mCatCallSetUpRegistrant != null) {
            this.mRil.mCatCallSetUpRegistrant.notifyRegistrant(new AsyncResult(null, Long.valueOf(timeout), null));
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
        this.mRil.mIccRefreshRegistrants.notifyRegistrants(new AsyncResult(null, response, null));
    }

    public void callRing(int indicationType, boolean isGsm, CdmaSignalInfoRecord record) {
        this.mRil.processIndication(indicationType);
        Object response = null;
        if (!isGsm) {
            int i;
            response = new char[4];
            if (record.isPresent) {
                i = 1;
            } else {
                i = 0;
            }
            response[0] = (char) i;
            response[1] = (char) record.signalType;
            response[2] = (char) record.alertPitch;
            response[3] = (char) record.signal;
            this.mRil.writeMetricsCallRing(response);
        }
        this.mRil.unsljLogRet(1018, response);
        if (this.mRil.mRingRegistrant != null) {
            this.mRil.mRingRegistrant.notifyRegistrant(new AsyncResult(null, response, null));
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
        this.mRil.writeMetricsNewSms(2, 2);
        SmsMessage sms = SmsMessageConverter.newSmsMessageFromCdmaSmsMessage(msg);
        if (this.mRil.mCdmaSmsRegistrant != null) {
            Jlog.d(47, "JL_RIL_RESPONSE_NEW_SMS");
            this.mRil.mCdmaSmsRegistrant.notifyRegistrant(new AsyncResult(null, sms, null));
        }
    }

    public void newBroadcastSms(int indicationType, ArrayList<Byte> data) {
        this.mRil.processIndication(indicationType);
        byte[] response = RIL.arrayListToPrimitiveArray(data);
        this.mRil.unsljLogvRet(1021, IccUtils.bytesToHexString(response));
        if (this.mRil.mGsmBroadcastSmsRegistrant != null) {
            this.mRil.mGsmBroadcastSmsRegistrant.notifyRegistrant(new AsyncResult(null, response, null));
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
        this.mRil.unsljLogvRet(ApnTypes.ALL, Integer.valueOf(state));
        if (this.mRil.mRestrictedStateRegistrant != null) {
            this.mRil.mRestrictedStateRegistrant.notifyRegistrant(new AsyncResult(null, Integer.valueOf(state), null));
        }
    }

    public void enterEmergencyCallbackMode(int indicationType) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(RadioAccessFamily.HSUPA);
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
        this.mRil.mCallWaitingInfoRegistrants.notifyRegistrants(new AsyncResult(null, notification, null));
    }

    public void cdmaOtaProvisionStatus(int indicationType, int status) {
        this.mRil.processIndication(indicationType);
        int[] response = new int[]{status};
        this.mRil.unsljLogRet(1026, response);
        this.mRil.mOtaProvisionRegistrants.notifyRegistrants(new AsyncResult(null, response, null));
    }

    public void cdmaInfoRec(int indicationType, CdmaInformationRecords records) {
        this.mRil.processIndication(indicationType);
        int numberOfInfoRecs = records.infoRec.size();
        for (int i = 0; i < numberOfInfoRecs; i++) {
            com.android.internal.telephony.cdma.CdmaInformationRecords cdmaInformationRecords;
            CdmaInformationRecord record = (CdmaInformationRecord) records.infoRec.get(i);
            int id = record.name;
            switch (id) {
                case 0:
                case 7:
                    cdmaInformationRecords = new com.android.internal.telephony.cdma.CdmaInformationRecords(new CdmaDisplayInfoRec(id, ((CdmaDisplayInfoRecord) record.display.get(0)).alphaBuf));
                    break;
                case 1:
                case 2:
                case 3:
                    CdmaNumberInfoRecord numInfoRecord = (CdmaNumberInfoRecord) record.number.get(0);
                    cdmaInformationRecords = new com.android.internal.telephony.cdma.CdmaInformationRecords(new CdmaNumberInfoRec(id, numInfoRecord.number, numInfoRecord.numberType, numInfoRecord.numberPlan, numInfoRecord.pi, numInfoRecord.si));
                    break;
                case 4:
                    CdmaSignalInfoRecord signalInfoRecord = (CdmaSignalInfoRecord) record.signal.get(0);
                    cdmaInformationRecords = new com.android.internal.telephony.cdma.CdmaInformationRecords(new CdmaSignalInfoRec(signalInfoRecord.isPresent ? 1 : 0, signalInfoRecord.signalType, signalInfoRecord.alertPitch, signalInfoRecord.signal));
                    break;
                case 5:
                    CdmaRedirectingNumberInfoRecord redirectingNumberInfoRecord = (CdmaRedirectingNumberInfoRecord) record.redir.get(0);
                    cdmaInformationRecords = new com.android.internal.telephony.cdma.CdmaInformationRecords(new CdmaRedirectingNumberInfoRec(redirectingNumberInfoRecord.redirectingNumber.number, redirectingNumberInfoRecord.redirectingNumber.numberType, redirectingNumberInfoRecord.redirectingNumber.numberPlan, redirectingNumberInfoRecord.redirectingNumber.pi, redirectingNumberInfoRecord.redirectingNumber.si, redirectingNumberInfoRecord.redirectingReason));
                    break;
                case 6:
                    CdmaLineControlInfoRecord lineControlInfoRecord = (CdmaLineControlInfoRecord) record.lineCtrl.get(0);
                    cdmaInformationRecords = new com.android.internal.telephony.cdma.CdmaInformationRecords(new CdmaLineControlInfoRec(lineControlInfoRecord.lineCtrlPolarityIncluded, lineControlInfoRecord.lineCtrlToggle, lineControlInfoRecord.lineCtrlReverse, lineControlInfoRecord.lineCtrlPowerDenial));
                    break;
                case 8:
                    cdmaInformationRecords = new com.android.internal.telephony.cdma.CdmaInformationRecords(new CdmaT53ClirInfoRec(((CdmaT53ClirInfoRecord) record.clir.get(0)).cause));
                    break;
                case 10:
                    CdmaT53AudioControlInfoRecord audioControlInfoRecord = (CdmaT53AudioControlInfoRecord) record.audioCtrl.get(0);
                    cdmaInformationRecords = new com.android.internal.telephony.cdma.CdmaInformationRecords(new CdmaT53AudioControlInfoRec(audioControlInfoRecord.upLink, audioControlInfoRecord.downLink));
                    break;
                default:
                    throw new RuntimeException("RIL_UNSOL_CDMA_INFO_REC: unsupported record. Got " + com.android.internal.telephony.cdma.CdmaInformationRecords.idToString(id) + " ");
            }
            this.mRil.unsljLogRet(1027, cdmaInformationRecords);
            this.mRil.notifyRegistrantsCdmaInfoRec(cdmaInformationRecords);
        }
    }

    public void indicateRingbackTone(int indicationType, boolean start) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLogvRet(1029, Boolean.valueOf(start));
        this.mRil.mRingbackToneRegistrants.notifyRegistrants(new AsyncResult(null, Boolean.valueOf(start), null));
    }

    public void resendIncallMute(int indicationType) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(1030);
        this.mRil.mResendIncallMuteRegistrants.notifyRegistrants();
    }

    public void cdmaSubscriptionSourceChanged(int indicationType, int cdmaSource) {
        this.mRil.processIndication(indicationType);
        int[] response = new int[]{cdmaSource};
        this.mRil.unsljLogRet(1031, response);
        this.mRil.mCdmaSubscriptionChangedRegistrants.notifyRegistrants(new AsyncResult(null, response, null));
    }

    public void cdmaPrlChanged(int indicationType, int version) {
        this.mRil.processIndication(indicationType);
        int[] response = new int[]{version};
        this.mRil.unsljLogRet(1032, response);
        this.mRil.mCdmaPrlChangedRegistrants.notifyRegistrants(new AsyncResult(null, response, null));
    }

    public void exitEmergencyCallbackMode(int indicationType) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(1033);
        this.mRil.mExitEmergencyCallbackModeRegistrants.notifyRegistrants();
    }

    public void rilConnected(int indicationType) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(1034);
        this.mRil.setCellInfoListRate();
        this.mRil.notifyRegistrantsRilConnectionChanged(15);
    }

    public void voiceRadioTechChanged(int indicationType, int rat) {
        this.mRil.processIndication(indicationType);
        int[] response = new int[]{rat};
        this.mRil.mLastRadioTech = rat;
        this.mRil.unsljLogRet(1035, response);
        this.mRil.mVoiceRadioTechChangedRegistrants.notifyRegistrants(new AsyncResult(null, response, null));
    }

    public void cellInfoList(int indicationType, ArrayList<CellInfo> records) {
        this.mRil.processIndication(indicationType);
        ArrayList<android.telephony.CellInfo> response = RIL.convertHalCellInfoList(records);
        this.mRil.unsljLogRet(1036, response);
        this.mRil.mRilCellInfoListRegistrants.notifyRegistrants(new AsyncResult(null, response, null));
    }

    public void imsNetworkStateChanged(int indicationType) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(1037);
        this.mRil.mImsNetworkStateChangedRegistrants.notifyRegistrants();
    }

    public void subscriptionStatusChanged(int indicationType, boolean activate) {
        int i = 1;
        this.mRil.processIndication(indicationType);
        int[] response = new int[1];
        if (!activate) {
            i = 0;
        }
        response[0] = i;
        this.mRil.unsljLogRet(1038, response);
        this.mRil.mSubscriptionStatusRegistrants.notifyRegistrants(new AsyncResult(null, response, null));
    }

    public void srvccStateNotify(int indicationType, int state) {
        this.mRil.processIndication(indicationType);
        int[] response = new int[]{state};
        this.mRil.unsljLogRet(1039, response);
        this.mRil.writeMetricsSrvcc(state);
        this.mRil.mSrvccStateRegistrants.notifyRegistrants(new AsyncResult(null, response, null));
    }

    public void hardwareConfigChanged(int indicationType, ArrayList<HardwareConfig> configs) {
        this.mRil.processIndication(indicationType);
        ArrayList<HardwareConfig> response = RIL.convertHalHwConfigList(configs, this.mRil);
        this.mRil.unsljLogRet(1040, response);
        this.mRil.mHardwareConfigChangeRegistrants.notifyRegistrants(new AsyncResult(null, response, null));
    }

    public void radioCapabilityIndication(int indicationType, RadioCapability rc) {
        this.mRil.processIndication(indicationType);
        RadioCapability response = RIL.convertHalRadioCapability(rc, this.mRil);
        this.mRil.unsljLogRet(1042, response);
        this.mRil.mPhoneRadioCapabilityChangedRegistrants.notifyRegistrants(new AsyncResult(null, response, null));
    }

    public void onSupplementaryServiceIndication(int indicationType, StkCcUnsolSsResult ss) {
        this.mRil.processIndication(indicationType);
        SsData ssData = new SsData();
        ssData.serviceType = ssData.ServiceTypeFromRILInt(ss.serviceType);
        ssData.requestType = ssData.RequestTypeFromRILInt(ss.requestType);
        ssData.teleserviceType = ssData.TeleserviceTypeFromRILInt(ss.teleserviceType);
        ssData.serviceClass = ss.serviceClass;
        ssData.result = ss.result;
        int num;
        int i;
        if (ssData.serviceType.isTypeCF() && ssData.requestType.isTypeInterrogation()) {
            CfData cfData = (CfData) ss.cfData.get(0);
            num = cfData.cfInfo.size();
            ssData.cfInfo = new CallForwardInfo[num];
            for (i = 0; i < num; i++) {
                CallForwardInfo cfInfo = (CallForwardInfo) cfData.cfInfo.get(i);
                ssData.cfInfo[i] = new CallForwardInfo();
                ssData.cfInfo[i].status = cfInfo.status;
                ssData.cfInfo[i].reason = cfInfo.reason;
                ssData.cfInfo[i].serviceClass = cfInfo.serviceClass;
                ssData.cfInfo[i].toa = cfInfo.toa;
                ssData.cfInfo[i].number = cfInfo.number;
                ssData.cfInfo[i].timeSeconds = cfInfo.timeSeconds;
                this.mRil.riljLog("[SS Data] CF Info " + i + " : " + ssData.cfInfo[i]);
            }
        } else {
            SsInfoData ssInfo = (SsInfoData) ss.ssInfo.get(0);
            num = ssInfo.ssInfo.size();
            ssData.ssInfo = new int[num];
            for (i = 0; i < num; i++) {
                ssData.ssInfo[i] = ((Integer) ssInfo.ssInfo.get(i)).intValue();
                this.mRil.riljLog("[SS Data] SS Info " + i + " : " + ssData.ssInfo[i]);
            }
        }
        this.mRil.unsljLogRet(1043, ssData);
        if (this.mRil.mSsRegistrant != null) {
            this.mRil.mSsRegistrant.notifyRegistrant(new AsyncResult(null, ssData, null));
        }
    }

    public void stkCallControlAlphaNotify(int indicationType, String alpha) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLogRet(1044, alpha);
        if (this.mRil.mCatCcAlphaRegistrant != null) {
            this.mRil.mCatCcAlphaRegistrant.notifyRegistrant(new AsyncResult(null, alpha, null));
        }
    }

    public void lceData(int indicationType, LceDataInfo lce) {
        this.mRil.processIndication(indicationType);
        ArrayList<Integer> response = RIL.convertHalLceData(lce, this.mRil);
        this.mRil.unsljLogRet(1045, response);
        if (this.mRil.mLceInfoRegistrant != null) {
            this.mRil.mLceInfoRegistrant.notifyRegistrant(new AsyncResult(null, response, null));
        }
    }

    public void pcoData(int indicationType, PcoDataInfo pco) {
        this.mRil.processIndication(indicationType);
        PcoData response = new PcoData(pco.cid, pco.bearerProto, pco.pcoId, RIL.arrayListToPrimitiveArray(pco.contents));
        this.mRil.unsljLogRet(1046, response);
        this.mRil.mPcoDataRegistrants.notifyRegistrants(new AsyncResult(null, response, null));
    }

    public void modemReset(int indicationType, String reason) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLogRet(1047, reason);
        this.mRil.writeMetricsModemRestartEvent(reason);
    }

    private RadioState getRadioStateFromInt(int stateInt) {
        switch (convertRadioState(stateInt)) {
            case 0:
                return RadioState.RADIO_OFF;
            case 1:
                return RadioState.RADIO_UNAVAILABLE;
            case 10:
                return RadioState.RADIO_ON;
            default:
                throw new RuntimeException("Unrecognized RadioState: " + stateInt);
        }
    }

    private int convertRadioState(int stateInt) {
        if (stateInt <= 1 || stateInt >= 10) {
            return stateInt;
        }
        return 10;
    }
}
