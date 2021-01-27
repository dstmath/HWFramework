package com.android.internal.telephony.metrics;

import android.telephony.CallQuality;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.emergency.EmergencyNumber;
import android.telephony.ims.ImsCallSession;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.feature.MmTelFeature;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CarrierResolver;
import com.android.internal.telephony.GsmCdmaConnection;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.UUSInfo;
import com.android.internal.telephony.nano.TelephonyProto;
import com.android.internal.telephony.separated.DefaultTelephonySeparatedFactory;
import com.android.internal.telephony.separated.TelephonySeparatedFactory;
import com.android.internal.telephony.separated.metrics.DefaultTelephonyMetrics;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class TelephonyMetrics {
    private static final String TAG = "TelephonyMetrics";
    private static TelephonyMetrics sInstance;
    private static DefaultTelephonyMetrics sTelephonyMetrics;

    static {
        DefaultTelephonySeparatedFactory factory = TelephonySeparatedFactory.getTelephonyFactory().getTelephonySeparatedFactory();
        sTelephonyMetrics = factory.getTelephonyMetrics();
        Rlog.i(TAG, "factory:" + factory.getClass().getCanonicalName() + ", instance:" + sTelephonyMetrics.getClass().getCanonicalName());
    }

    private TelephonyMetrics() {
    }

    public static synchronized TelephonyMetrics getInstance() {
        TelephonyMetrics telephonyMetrics;
        synchronized (TelephonyMetrics.class) {
            if (sInstance == null) {
                sInstance = new TelephonyMetrics();
            }
            telephonyMetrics = sInstance;
        }
        return telephonyMetrics;
    }

    public void updateSimState(int phoneId, int simState) {
        sTelephonyMetrics.updateSimState(phoneId, simState);
    }

    public void writeOnImsCommand(int phoneId, ImsCallSession session, int command) {
        sTelephonyMetrics.writeOnImsCommand(phoneId, session, command);
    }

    public void writeDroppedIncomingMultipartSms(int phoneId, String format, int receivedCount, int totalCount) {
        sTelephonyMetrics.writeDroppedIncomingMultipartSms(phoneId, format, receivedCount, totalCount);
    }

    public void writeRilSrvcc(int phoneId, int rilSrvccState) {
        sTelephonyMetrics.writeRilSrvcc(phoneId, rilSrvccState);
    }

    public void writeRilCallRing(int phoneId, char[] response) {
        sTelephonyMetrics.writeRilCallRing(phoneId, response);
    }

    public void writeModemRestartEvent(int phoneId, String reason) {
        sTelephonyMetrics.writeModemRestartEvent(phoneId, reason);
    }

    public void writeOnRilTimeoutResponse(int phoneId, int rilSerial, int rilRequest) {
        sTelephonyMetrics.writeOnRilTimeoutResponse(phoneId, rilSerial, rilRequest);
    }

    public void writeOnRilSolicitedResponse(int phoneId, int rilSerial, int rilError, int rilRequest, Object ret) {
        sTelephonyMetrics.writeOnRilSolicitedResponse(phoneId, rilSerial, rilError, rilRequest, ret);
    }

    public synchronized void writeRilSendSms(int phoneId, int rilSerial, int tech, int format) {
        sTelephonyMetrics.writeRilSendSms(phoneId, rilSerial, tech, format);
    }

    public void writeSetPreferredNetworkType(int phoneId, int networkType) {
        sTelephonyMetrics.writeSetPreferredNetworkType(phoneId, networkType);
    }

    public void writeRilDeactivateDataCall(int phoneId, int rilSerial, int cid, int reason) {
        sTelephonyMetrics.writeRilDeactivateDataCall(phoneId, rilSerial, cid, reason);
    }

    public void writeRilAnswer(int phoneId, int rilSerial) {
        sTelephonyMetrics.writeRilAnswer(phoneId, rilSerial);
    }

    public void writeOnDemandDataSwitch(TelephonyProto.TelephonyEvent.OnDemandDataSwitch onDemandDataSwitch) {
        sTelephonyMetrics.writeOnDemandDataSwitch(onDemandDataSwitch);
    }

    public void writeNITZEvent(int phoneId, long timestamp) {
        sTelephonyMetrics.writeNITZEvent(phoneId, timestamp);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        sTelephonyMetrics.dump(fd, pw, args);
    }

    public void writeDataSwitch(int subId, TelephonyProto.TelephonyEvent.DataSwitch dataSwitch) {
        sTelephonyMetrics.writeDataSwitch(subId, dataSwitch);
    }

    public void writeNetworkValidate(int networkValidationState) {
        sTelephonyMetrics.writeNetworkValidate(networkValidationState);
    }

    public void writeDataStallEvent(int phoneId, int recoveryAction) {
        sTelephonyMetrics.writeDataStallEvent(phoneId, recoveryAction);
    }

    public static TelephonyProto.TelephonyCallSession.Event.CallQuality toCallQualityProto(CallQuality callQuality) {
        return sTelephonyMetrics.toCallQualityProto(callQuality);
    }

    public void writeAudioCodecIms(int phoneId, ImsCallSession session) {
        sTelephonyMetrics.writeAudioCodecIms(phoneId, session);
    }

    public void writeAudioCodecGsmCdma(int phoneId, int audioQuality) {
        sTelephonyMetrics.writeAudioCodecGsmCdma(phoneId, audioQuality);
    }

    public void writeOnImsCallProgressing(int phoneId, ImsCallSession session) {
        sTelephonyMetrics.writeOnImsCallProgressing(phoneId, session);
    }

    public void writeOnImsCallStarted(int phoneId, ImsCallSession session) {
        sTelephonyMetrics.writeOnImsCallStarted(phoneId, session);
    }

    public void writeOnImsCallStartFailed(int phoneId, ImsCallSession session, ImsReasonInfo reasonInfo) {
        sTelephonyMetrics.writeOnImsCallStartFailed(phoneId, session, reasonInfo);
    }

    public void writeOnImsCallHeld(int phoneId, ImsCallSession session) {
        sTelephonyMetrics.writeOnImsCallHeld(phoneId, session);
    }

    public void writeOnImsCallHoldReceived(int phoneId, ImsCallSession session) {
        sTelephonyMetrics.writeOnImsCallHoldReceived(phoneId, session);
    }

    public void writeOnImsCallHoldFailed(int phoneId, ImsCallSession session, ImsReasonInfo reasonInfo) {
        sTelephonyMetrics.writeOnImsCallHoldFailed(phoneId, session, reasonInfo);
    }

    public void writeOnImsCallResumed(int phoneId, ImsCallSession session) {
        sTelephonyMetrics.writeOnImsCallResumed(phoneId, session);
    }

    public void writeOnImsCallResumeReceived(int phoneId, ImsCallSession session) {
        sTelephonyMetrics.writeOnImsCallResumeReceived(phoneId, session);
    }

    public void writeOnImsCallResumeFailed(int phoneId, ImsCallSession session, ImsReasonInfo reasonInfo) {
        sTelephonyMetrics.writeOnImsCallResumeFailed(phoneId, session, reasonInfo);
    }

    public synchronized void writeOnImsCapabilities(int phoneId, int radioTech, MmTelFeature.MmTelCapabilities capabilities) {
        sTelephonyMetrics.writeOnImsCapabilities(phoneId, radioTech, capabilities);
    }

    public void writeImsSetFeatureValue(int phoneId, int feature, int network, int value) {
        sTelephonyMetrics.writeImsSetFeatureValue(phoneId, feature, network, value);
    }

    public synchronized void writeOnImsConnectionState(int phoneId, int state, ImsReasonInfo reasonInfo) {
        sTelephonyMetrics.writeOnImsConnectionState(phoneId, state, reasonInfo);
    }

    public void writeOnImsCallHandoverEvent(int phoneId, int eventType, ImsCallSession session, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
        sTelephonyMetrics.writeOnImsCallHandoverEvent(phoneId, eventType, session, srcAccessTech, targetAccessTech, reasonInfo);
    }

    public void writeOnImsCallTerminated(int phoneId, ImsCallSession session, ImsReasonInfo reasonInfo, CallQualityMetrics cqm, EmergencyNumber emergencyNumber, String countryIso) {
        sTelephonyMetrics.writeOnImsCallTerminated(phoneId, session, reasonInfo, cqm, emergencyNumber, countryIso);
    }

    public void writeImsCallState(int phoneId, ImsCallSession session, Call.State callState) {
        sTelephonyMetrics.writeImsCallState(phoneId, session, callState);
    }

    public void writePhoneState(int phoneId, PhoneConstants.State phoneState) {
        sTelephonyMetrics.writePhoneState(phoneId, phoneState);
    }

    public void writeOnImsCallStart(int phoneId, ImsCallSession session) {
        sTelephonyMetrics.writeOnImsCallStart(phoneId, session);
    }

    public void writeOnImsCallReceive(int phoneId, ImsCallSession session) {
        sTelephonyMetrics.writeOnImsCallReceive(phoneId, session);
    }

    public void writeRilHangup(int phoneId, GsmCdmaConnection conn, int callId, String countryIso) {
        sTelephonyMetrics.writeRilHangup(phoneId, conn, callId, countryIso);
    }

    public void writeRilCallList(int phoneId, ArrayList<GsmCdmaConnection> connections, String countryIso) {
        sTelephonyMetrics.writeRilCallList(phoneId, connections, countryIso);
    }

    public void writeRilDial(int phoneId, GsmCdmaConnection conn, int clirMode, UUSInfo uusInfo) {
        sTelephonyMetrics.writeRilDial(phoneId, conn, clirMode, uusInfo);
    }

    public void writeRilDataCallEvent(int phoneId, int cid, int apnTypeBitmask, int state) {
        sTelephonyMetrics.writeRilDataCallEvent(phoneId, cid, apnTypeBitmask, state);
    }

    public void writeCarrierIdMatchingEvent(int phoneId, int version, int cid, String unknownMcmnc, String unknownGid1, CarrierResolver.CarrierMatchingRule simInfo) {
        sTelephonyMetrics.writeCarrierIdMatchingEvent(phoneId, version, cid, unknownMcmnc, unknownGid1, simInfo);
    }

    public void writeCarrierKeyEvent(int phoneId, int keyType, boolean isDownloadSuccessful) {
        sTelephonyMetrics.writeCarrierKeyEvent(phoneId, keyType, isDownloadSuccessful);
    }

    public void writeSetupDataCall(int phoneId, int radioTechnology, int profileId, String apn, int protocol) {
        sTelephonyMetrics.writeSetupDataCall(phoneId, radioTechnology, profileId, apn, protocol);
    }

    public synchronized void updateActiveSubscriptionInfoList(List<SubscriptionInfo> subInfos) {
        sTelephonyMetrics.updateActiveSubscriptionInfoList(subInfos);
    }

    public synchronized void writeServiceStateChanged(int phoneId, ServiceState serviceState) {
        sTelephonyMetrics.writeServiceStateChanged(phoneId, serviceState);
    }

    public void writeEmergencyNumberUpdateEvent(int phoneId, EmergencyNumber emergencyNumber) {
        sTelephonyMetrics.writeEmergencyNumberUpdateEvent(phoneId, emergencyNumber);
    }

    public synchronized void writeOnImsServiceSmsSolicitedResponse(int phoneId, int resultCode, int errorReason) {
        sTelephonyMetrics.writeOnImsServiceSmsSolicitedResponse(phoneId, resultCode, errorReason);
    }

    public synchronized void writeImsServiceSendSms(int phoneId, String format, int resultCode) {
        sTelephonyMetrics.writeImsServiceSendSms(phoneId, format, resultCode);
    }

    public void writeIncomingSmsError(int phoneId, boolean isSmsOverIms, int result) {
        sTelephonyMetrics.writeIncomingSmsError(phoneId, isSmsOverIms, result);
    }

    public void writeIncomingSmsSession(int phoneId, boolean isSmsOverIms, String format, long[] timestamps, boolean isBlocked) {
        sTelephonyMetrics.writeIncomingSmsSession(phoneId, isSmsOverIms, format, timestamps, isBlocked);
    }

    public void writeIncomingWapPush(int phoneId, boolean isSmsOverIms, String format, long[] timestamps, boolean isSuccess) {
        sTelephonyMetrics.writeIncomingWapPush(phoneId, isSmsOverIms, format, timestamps, isSuccess);
    }

    public void writeIncomingSmsTypeZero(int phoneId, String format) {
        sTelephonyMetrics.writeIncomingSmsTypeZero(phoneId, format);
    }

    public void writeIncomingVoiceMailSms(int phoneId, String format) {
        sTelephonyMetrics.writeIncomingVoiceMailSms(phoneId, format);
    }

    public void writeIncomingSMSPP(int phoneId, String format, boolean isSuccess) {
        sTelephonyMetrics.writeIncomingSMSPP(phoneId, format, isSuccess);
    }

    public synchronized void writeNewCBSms(int phoneId, int format, int priority, boolean isCmas, boolean isEtws, int serviceCategory, int serialNumber, long deliveredTimestamp) {
        sTelephonyMetrics.writeNewCBSms(phoneId, format, priority, isCmas, isEtws, serviceCategory, serialNumber, deliveredTimestamp);
    }

    public void updateEnabledModemBitmap(int enabledModemBitmap) {
        sTelephonyMetrics.updateEnabledModemBitmap(enabledModemBitmap);
    }
}
