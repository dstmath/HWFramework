package com.android.internal.telephony.separated.metrics;

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
import com.android.internal.telephony.metrics.CallQualityMetrics;
import com.android.internal.telephony.nano.TelephonyProto;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class DefaultTelephonyMetrics {
    private static final String TAG = "DefaultTelephonyMetrics";
    private static DefaultTelephonyMetrics sInstance;

    public static synchronized DefaultTelephonyMetrics getInstance() {
        DefaultTelephonyMetrics defaultTelephonyMetrics;
        synchronized (DefaultTelephonyMetrics.class) {
            if (sInstance == null) {
                sInstance = new DefaultTelephonyMetrics();
                Rlog.i(TAG, "getInstance: " + sInstance.getClass().getCanonicalName());
            }
            defaultTelephonyMetrics = sInstance;
        }
        return defaultTelephonyMetrics;
    }

    public void updateSimState(int phoneId, int simState) {
    }

    public void writeOnImsCommand(int phoneId, ImsCallSession session, int command) {
    }

    public void writeDroppedIncomingMultipartSms(int phoneId, String format, int receivedCount, int totalCount) {
    }

    public void writeRilSrvcc(int phoneId, int rilSrvccState) {
    }

    public void writeRilCallRing(int phoneId, char[] response) {
    }

    public void writeModemRestartEvent(int phoneId, String reason) {
    }

    public void writeOnRilTimeoutResponse(int phoneId, int rilSerial, int rilRequest) {
    }

    public void writeOnRilSolicitedResponse(int phoneId, int rilSerial, int rilError, int rilRequest, Object ret) {
    }

    public synchronized void writeRilSendSms(int phoneId, int rilSerial, int tech, int format) {
    }

    public void writeSetPreferredNetworkType(int phoneId, int networkType) {
    }

    public void writeRilDeactivateDataCall(int phoneId, int rilSerial, int cid, int reason) {
    }

    public void writeRilAnswer(int phoneId, int rilSerial) {
    }

    public void writeOnDemandDataSwitch(TelephonyProto.TelephonyEvent.OnDemandDataSwitch onDemandDataSwitch) {
    }

    public void writeNITZEvent(int phoneId, long timestamp) {
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
    }

    public void writeDataSwitch(int subId, TelephonyProto.TelephonyEvent.DataSwitch dataSwitch) {
    }

    public void writeNetworkValidate(int networkValidationState) {
    }

    public void writeDataStallEvent(int phoneId, int recoveryAction) {
    }

    public TelephonyProto.TelephonyCallSession.Event.CallQuality toCallQualityProto(CallQuality callQuality) {
        return new TelephonyProto.TelephonyCallSession.Event.CallQuality();
    }

    public void writeAudioCodecIms(int phoneId, ImsCallSession session) {
    }

    public void writeAudioCodecGsmCdma(int phoneId, int audioQuality) {
    }

    public void writeOnImsCallProgressing(int phoneId, ImsCallSession session) {
    }

    public void writeOnImsCallStarted(int phoneId, ImsCallSession session) {
    }

    public void writeOnImsCallStartFailed(int phoneId, ImsCallSession session, ImsReasonInfo reasonInfo) {
    }

    public void writeOnImsCallHeld(int phoneId, ImsCallSession session) {
    }

    public void writeOnImsCallHoldReceived(int phoneId, ImsCallSession session) {
    }

    public void writeOnImsCallHoldFailed(int phoneId, ImsCallSession session, ImsReasonInfo reasonInfo) {
    }

    public void writeOnImsCallResumed(int phoneId, ImsCallSession session) {
    }

    public void writeOnImsCallResumeReceived(int phoneId, ImsCallSession session) {
    }

    public void writeOnImsCallResumeFailed(int phoneId, ImsCallSession session, ImsReasonInfo reasonInfo) {
    }

    public synchronized void writeOnImsCapabilities(int phoneId, int radioTech, MmTelFeature.MmTelCapabilities capabilities) {
    }

    public void writeImsSetFeatureValue(int phoneId, int feature, int network, int value) {
    }

    public synchronized void writeOnImsConnectionState(int phoneId, int state, ImsReasonInfo reasonInfo) {
    }

    public void writeOnImsCallHandoverEvent(int phoneId, int eventType, ImsCallSession session, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
    }

    public void writeOnImsCallTerminated(int phoneId, ImsCallSession session, ImsReasonInfo reasonInfo, CallQualityMetrics cqm, EmergencyNumber emergencyNumber, String countryIso) {
    }

    public void writeImsCallState(int phoneId, ImsCallSession session, Call.State callState) {
    }

    public void writePhoneState(int phoneId, PhoneConstants.State phoneState) {
    }

    public void writeOnImsCallStart(int phoneId, ImsCallSession session) {
    }

    public void writeOnImsCallReceive(int phoneId, ImsCallSession session) {
    }

    public void writeRilHangup(int phoneId, GsmCdmaConnection conn, int callId, String countryIso) {
    }

    public void writeRilCallList(int phoneId, ArrayList<GsmCdmaConnection> arrayList, String countryIso) {
    }

    public void writeRilDial(int phoneId, GsmCdmaConnection conn, int clirMode, UUSInfo uusInfo) {
    }

    public void writeRilDataCallEvent(int phoneId, int cid, int apnTypeBitmask, int state) {
    }

    public void writeCarrierIdMatchingEvent(int phoneId, int version, int cid, String unknownMcmnc, String unknownGid1, CarrierResolver.CarrierMatchingRule simInfo) {
    }

    public void writeCarrierKeyEvent(int phoneId, int keyType, boolean isDownloadSuccessful) {
    }

    public void writeSetupDataCall(int phoneId, int radioTechnology, int profileId, String apn, int protocol) {
    }

    public synchronized void updateActiveSubscriptionInfoList(List<SubscriptionInfo> list) {
    }

    public synchronized void writeServiceStateChanged(int phoneId, ServiceState serviceState) {
    }

    public void writeEmergencyNumberUpdateEvent(int phoneId, EmergencyNumber emergencyNumber) {
    }

    public synchronized void writeOnImsServiceSmsSolicitedResponse(int phoneId, int resultCode, int errorReason) {
    }

    public synchronized void writeImsServiceSendSms(int phoneId, String format, int resultCode) {
    }

    public void writeIncomingSmsError(int phoneId, boolean isSmsOverIms, int result) {
    }

    public void writeIncomingSmsSession(int phoneId, boolean isSmsOverIms, String format, long[] timestamps, boolean isBlocked) {
    }

    public void writeIncomingWapPush(int phoneId, boolean isSmsOverIms, String format, long[] timestamps, boolean isSuccess) {
    }

    public void writeIncomingSmsTypeZero(int phoneId, String format) {
    }

    public void writeIncomingVoiceMailSms(int phoneId, String format) {
    }

    public void writeIncomingSMSPP(int phoneId, String format, boolean isSuccess) {
    }

    public synchronized void writeNewCBSms(int phoneId, int format, int priority, boolean isCmas, boolean isEtws, int serviceCategory, int serialNumber, long deliveredTimestamp) {
    }

    public void updateEnabledModemBitmap(int enabledModemBitmap) {
    }
}
