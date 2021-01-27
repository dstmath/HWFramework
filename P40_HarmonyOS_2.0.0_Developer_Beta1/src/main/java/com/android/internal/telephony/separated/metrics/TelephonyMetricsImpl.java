package com.android.internal.telephony.separated.metrics;

import android.telephony.CallQuality;
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
import com.android.internal.telephony.metrics.CallQualityMetrics;
import com.android.internal.telephony.nano.TelephonyProto;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class TelephonyMetricsImpl extends DefaultTelephonyMetrics {
    private static final String TAG = "RcsMessageManagerImpl";
    private static TelephonyMetricsImpl sInstance = null;

    public static synchronized TelephonyMetricsImpl getInstance() {
        TelephonyMetricsImpl telephonyMetricsImpl;
        synchronized (TelephonyMetricsImpl.class) {
            if (sInstance == null) {
                sInstance = new TelephonyMetricsImpl();
            }
            telephonyMetricsImpl = sInstance;
        }
        return telephonyMetricsImpl;
    }

    public void updateSimState(int phoneId, int simState) {
        TelephonyMetrics.getInstance().updateSimState(phoneId, simState);
    }

    public void writeOnImsCommand(int phoneId, ImsCallSession session, int command) {
        TelephonyMetrics.getInstance().writeOnImsCommand(phoneId, session, command);
    }

    public void writeDroppedIncomingMultipartSms(int phoneId, String format, int receivedCount, int totalCount) {
        TelephonyMetrics.getInstance().writeDroppedIncomingMultipartSms(phoneId, format, receivedCount, totalCount);
    }

    public void writeRilSrvcc(int phoneId, int rilSrvccState) {
        TelephonyMetrics.getInstance().writeRilSrvcc(phoneId, rilSrvccState);
    }

    public void writeRilCallRing(int phoneId, char[] response) {
        TelephonyMetrics.getInstance().writeRilCallRing(phoneId, response);
    }

    public void writeModemRestartEvent(int phoneId, String reason) {
        TelephonyMetrics.getInstance().writeModemRestartEvent(phoneId, reason);
    }

    public void writeOnRilTimeoutResponse(int phoneId, int rilSerial, int rilRequest) {
        TelephonyMetrics.getInstance().writeOnRilTimeoutResponse(phoneId, rilSerial, rilRequest);
    }

    public void writeOnRilSolicitedResponse(int phoneId, int rilSerial, int rilError, int rilRequest, Object ret) {
        TelephonyMetrics.getInstance().writeOnRilSolicitedResponse(phoneId, rilSerial, rilError, rilRequest, ret);
    }

    public synchronized void writeRilSendSms(int phoneId, int rilSerial, int tech, int format) {
        TelephonyMetrics.getInstance().writeRilSendSms(phoneId, rilSerial, tech, format);
    }

    public void writeSetPreferredNetworkType(int phoneId, int networkType) {
        TelephonyMetrics.getInstance().writeSetPreferredNetworkType(phoneId, networkType);
    }

    public void writeRilDeactivateDataCall(int phoneId, int rilSerial, int cid, int reason) {
        TelephonyMetrics.getInstance().writeRilDeactivateDataCall(phoneId, rilSerial, cid, reason);
    }

    public void writeRilAnswer(int phoneId, int rilSerial) {
        TelephonyMetrics.getInstance().writeRilAnswer(phoneId, rilSerial);
    }

    public void writeOnDemandDataSwitch(TelephonyProto.TelephonyEvent.OnDemandDataSwitch onDemandDataSwitch) {
        TelephonyMetrics.getInstance().writeOnDemandDataSwitch(onDemandDataSwitch);
    }

    public void writeNITZEvent(int phoneId, long timestamp) {
        TelephonyMetrics.getInstance().writeNITZEvent(phoneId, timestamp);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        TelephonyMetrics.getInstance().dump(fd, pw, args);
    }

    public void writeDataSwitch(int subId, TelephonyProto.TelephonyEvent.DataSwitch dataSwitch) {
        TelephonyMetrics.getInstance().writeDataSwitch(subId, dataSwitch);
    }

    public void writeNetworkValidate(int networkValidationState) {
        TelephonyMetrics.getInstance().writeNetworkValidate(networkValidationState);
    }

    public void writeDataStallEvent(int phoneId, int recoveryAction) {
        TelephonyMetrics.getInstance().writeDataStallEvent(phoneId, recoveryAction);
    }

    public TelephonyProto.TelephonyCallSession.Event.CallQuality toCallQualityProto(CallQuality callQuality) {
        return TelephonyMetrics.getInstance().toCallQualityProto(callQuality);
    }

    public void writeAudioCodecIms(int phoneId, ImsCallSession session) {
        TelephonyMetrics.getInstance().writeAudioCodecIms(phoneId, session);
    }

    public void writeAudioCodecGsmCdma(int phoneId, int audioQuality) {
        TelephonyMetrics.getInstance().writeAudioCodecGsmCdma(phoneId, audioQuality);
    }

    public void writeOnImsCallProgressing(int phoneId, ImsCallSession session) {
        TelephonyMetrics.getInstance().writeOnImsCallProgressing(phoneId, session);
    }

    public void writeOnImsCallStarted(int phoneId, ImsCallSession session) {
        TelephonyMetrics.getInstance().writeOnImsCallStarted(phoneId, session);
    }

    public void writeOnImsCallStartFailed(int phoneId, ImsCallSession session, ImsReasonInfo reasonInfo) {
        TelephonyMetrics.getInstance().writeOnImsCallStartFailed(phoneId, session, reasonInfo);
    }

    public void writeOnImsCallHeld(int phoneId, ImsCallSession session) {
        TelephonyMetrics.getInstance().writeOnImsCallHeld(phoneId, session);
    }

    public void writeOnImsCallHoldReceived(int phoneId, ImsCallSession session) {
        TelephonyMetrics.getInstance().writeOnImsCallHoldReceived(phoneId, session);
    }

    public void writeOnImsCallHoldFailed(int phoneId, ImsCallSession session, ImsReasonInfo reasonInfo) {
        TelephonyMetrics.getInstance().writeOnImsCallHoldFailed(phoneId, session, reasonInfo);
    }

    public void writeOnImsCallResumed(int phoneId, ImsCallSession session) {
        TelephonyMetrics.getInstance().writeOnImsCallResumed(phoneId, session);
    }

    public void writeOnImsCallResumeReceived(int phoneId, ImsCallSession session) {
        TelephonyMetrics.getInstance().writeOnImsCallResumeReceived(phoneId, session);
    }

    public void writeOnImsCallResumeFailed(int phoneId, ImsCallSession session, ImsReasonInfo reasonInfo) {
        TelephonyMetrics.getInstance().writeOnImsCallResumeFailed(phoneId, session, reasonInfo);
    }

    public synchronized void writeOnImsCapabilities(int phoneId, int radioTech, MmTelFeature.MmTelCapabilities capabilities) {
        TelephonyMetrics.getInstance().writeOnImsCapabilities(phoneId, radioTech, capabilities);
    }

    public void writeImsSetFeatureValue(int phoneId, int feature, int network, int value) {
        TelephonyMetrics.getInstance().writeImsSetFeatureValue(phoneId, feature, network, value);
    }

    public synchronized void writeOnImsConnectionState(int phoneId, int state, ImsReasonInfo reasonInfo) {
        TelephonyMetrics.getInstance().writeOnImsConnectionState(phoneId, state, reasonInfo);
    }

    public void writeOnImsCallHandoverEvent(int phoneId, int eventType, ImsCallSession session, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
        TelephonyMetrics.getInstance().writeOnImsCallHandoverEvent(phoneId, eventType, session, srcAccessTech, targetAccessTech, reasonInfo);
    }

    public void writeOnImsCallTerminated(int phoneId, ImsCallSession session, ImsReasonInfo reasonInfo, CallQualityMetrics cqm, EmergencyNumber emergencyNumber, String countryIso) {
        TelephonyMetrics.getInstance().writeOnImsCallTerminated(phoneId, session, reasonInfo, cqm, emergencyNumber, countryIso);
    }

    public void writeImsCallState(int phoneId, ImsCallSession session, Call.State callState) {
        TelephonyMetrics.getInstance().writeImsCallState(phoneId, session, callState);
    }

    public void writePhoneState(int phoneId, PhoneConstants.State phoneState) {
        TelephonyMetrics.getInstance().writePhoneState(phoneId, phoneState);
    }

    public void writeOnImsCallStart(int phoneId, ImsCallSession session) {
        TelephonyMetrics.getInstance().writeOnImsCallStart(phoneId, session);
    }

    public void writeOnImsCallReceive(int phoneId, ImsCallSession session) {
        TelephonyMetrics.getInstance().writeOnImsCallReceive(phoneId, session);
    }

    public void writeRilHangup(int phoneId, GsmCdmaConnection conn, int callId, String countryIso) {
        TelephonyMetrics.getInstance().writeRilHangup(phoneId, conn, callId, countryIso);
    }

    public void writeRilCallList(int phoneId, ArrayList<GsmCdmaConnection> connections, String countryIso) {
        TelephonyMetrics.getInstance().writeRilCallList(phoneId, connections, countryIso);
    }

    public void writeRilDial(int phoneId, GsmCdmaConnection conn, int clirMode, UUSInfo uusInfo) {
        TelephonyMetrics.getInstance().writeRilDial(phoneId, conn, clirMode, uusInfo);
    }

    public void writeRilDataCallEvent(int phoneId, int cid, int apnTypeBitmask, int state) {
        TelephonyMetrics.getInstance().writeRilDataCallEvent(phoneId, cid, apnTypeBitmask, state);
    }

    public void writeCarrierIdMatchingEvent(int phoneId, int version, int cid, String unknownMcmnc, String unknownGid1, CarrierResolver.CarrierMatchingRule simInfo) {
        TelephonyMetrics.getInstance().writeCarrierIdMatchingEvent(phoneId, version, cid, unknownMcmnc, unknownGid1, simInfo);
    }

    public void writeCarrierKeyEvent(int phoneId, int keyType, boolean isDownloadSuccessful) {
        TelephonyMetrics.getInstance().writeCarrierKeyEvent(phoneId, keyType, isDownloadSuccessful);
    }

    public void writeSetupDataCall(int phoneId, int radioTechnology, int profileId, String apn, int protocol) {
        TelephonyMetrics.getInstance().writeSetupDataCall(phoneId, radioTechnology, profileId, apn, protocol);
    }

    public synchronized void updateActiveSubscriptionInfoList(List<SubscriptionInfo> subInfos) {
        TelephonyMetrics.getInstance().updateActiveSubscriptionInfoList(subInfos);
    }

    public synchronized void writeServiceStateChanged(int phoneId, ServiceState serviceState) {
        TelephonyMetrics.getInstance().writeServiceStateChanged(phoneId, serviceState);
    }

    public void writeEmergencyNumberUpdateEvent(int phoneId, EmergencyNumber emergencyNumber) {
        TelephonyMetrics.getInstance().writeEmergencyNumberUpdateEvent(phoneId, emergencyNumber);
    }

    public synchronized void writeOnImsServiceSmsSolicitedResponse(int phoneId, int resultCode, int errorReason) {
        TelephonyMetrics.getInstance().writeOnImsServiceSmsSolicitedResponse(phoneId, resultCode, errorReason);
    }

    public synchronized void writeImsServiceSendSms(int phoneId, String format, int resultCode) {
        TelephonyMetrics.getInstance().writeImsServiceSendSms(phoneId, format, resultCode);
    }

    public void writeIncomingSmsError(int phoneId, boolean isSmsOverIms, int result) {
        TelephonyMetrics.getInstance().writeIncomingSmsError(phoneId, isSmsOverIms, result);
    }

    public void writeIncomingSmsSession(int phoneId, boolean isSmsOverIms, String format, long[] timestamps, boolean isBlocked) {
        TelephonyMetrics.getInstance().writeIncomingSmsSession(phoneId, isSmsOverIms, format, timestamps, isBlocked);
    }

    public void writeIncomingWapPush(int phoneId, boolean isSmsOverIms, String format, long[] timestamps, boolean isSuccess) {
        TelephonyMetrics.getInstance().writeIncomingWapPush(phoneId, isSmsOverIms, format, timestamps, isSuccess);
    }

    public void writeIncomingSmsTypeZero(int phoneId, String format) {
        TelephonyMetrics.getInstance().writeIncomingSmsTypeZero(phoneId, format);
    }

    public void writeIncomingVoiceMailSms(int phoneId, String format) {
        TelephonyMetrics.getInstance().writeIncomingVoiceMailSms(phoneId, format);
    }

    public void writeIncomingSMSPP(int phoneId, String format, boolean isSuccess) {
        TelephonyMetrics.getInstance().writeIncomingSMSPP(phoneId, format, isSuccess);
    }

    public synchronized void writeNewCBSms(int phoneId, int format, int priority, boolean isCmas, boolean isEtws, int serviceCategory, int serialNumber, long deliveredTimestamp) {
        TelephonyMetrics.getInstance().writeNewCBSms(phoneId, format, priority, isCmas, isEtws, serviceCategory, serialNumber, deliveredTimestamp);
    }

    public void updateEnabledModemBitmap(int enabledModemBitmap) {
        TelephonyMetrics.getInstance().updateEnabledModemBitmap(enabledModemBitmap);
    }
}
