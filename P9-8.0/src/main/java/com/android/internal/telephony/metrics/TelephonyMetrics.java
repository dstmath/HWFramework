package com.android.internal.telephony.metrics;

import android.os.Build;
import android.os.SystemClock;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.TelephonyHistogram;
import android.util.Base64;
import android.util.SparseArray;
import com.android.ims.ImsReasonInfo;
import com.android.ims.internal.ImsCallSession;
import com.android.internal.telephony.Call.State;
import com.android.internal.telephony.GsmCdmaConnection;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.RIL;
import com.android.internal.telephony.SmsResponse;
import com.android.internal.telephony.UUSInfo;
import com.android.internal.telephony.dataconnection.DataCallResponse;
import com.android.internal.telephony.nano.TelephonyProto;
import com.android.internal.telephony.nano.TelephonyProto.ImsCapabilities;
import com.android.internal.telephony.nano.TelephonyProto.ImsConnectionState;
import com.android.internal.telephony.nano.TelephonyProto.RilDataCall;
import com.android.internal.telephony.nano.TelephonyProto.SmsSession;
import com.android.internal.telephony.nano.TelephonyProto.TelephonyCallSession;
import com.android.internal.telephony.nano.TelephonyProto.TelephonyCallSession.Event;
import com.android.internal.telephony.nano.TelephonyProto.TelephonyCallSession.Event.RilCall;
import com.android.internal.telephony.nano.TelephonyProto.TelephonyEvent;
import com.android.internal.telephony.nano.TelephonyProto.TelephonyEvent.ModemRestart;
import com.android.internal.telephony.nano.TelephonyProto.TelephonyEvent.RilDeactivateDataCall;
import com.android.internal.telephony.nano.TelephonyProto.TelephonyEvent.RilSetupDataCall;
import com.android.internal.telephony.nano.TelephonyProto.TelephonyEvent.RilSetupDataCallResponse;
import com.android.internal.telephony.nano.TelephonyProto.TelephonyLog;
import com.android.internal.telephony.nano.TelephonyProto.TelephonyServiceState;
import com.android.internal.telephony.nano.TelephonyProto.TelephonyServiceState.TelephonyOperator;
import com.android.internal.telephony.nano.TelephonyProto.TelephonySettings;
import com.android.internal.telephony.nano.TelephonyProto.Time;
import com.android.internal.telephony.protobuf.nano.MessageNano;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

public class TelephonyMetrics {
    private static final /* synthetic */ int[] -com-android-internal-telephony-Call$StateSwitchesValues = null;
    private static final /* synthetic */ int[] -com-android-internal-telephony-PhoneConstants$StateSwitchesValues = null;
    private static final boolean DBG = true;
    private static final int MAX_COMPLETED_CALL_SESSIONS = 50;
    private static final int MAX_COMPLETED_SMS_SESSIONS = 500;
    private static final int MAX_TELEPHONY_EVENTS = 1000;
    private static final int SESSION_START_PRECISION_MINUTES = 5;
    private static final String TAG = TelephonyMetrics.class.getSimpleName();
    private static final boolean VDBG = false;
    private static TelephonyMetrics sInstance;
    private final Deque<TelephonyCallSession> mCompletedCallSessions = new ArrayDeque();
    private final Deque<SmsSession> mCompletedSmsSessions = new ArrayDeque();
    private final SparseArray<InProgressCallSession> mInProgressCallSessions = new SparseArray();
    private final SparseArray<InProgressSmsSession> mInProgressSmsSessions = new SparseArray();
    private final SparseArray<ImsCapabilities> mLastImsCapabilities = new SparseArray();
    private final SparseArray<ImsConnectionState> mLastImsConnectionState = new SparseArray();
    private final SparseArray<TelephonyServiceState> mLastServiceState = new SparseArray();
    private final SparseArray<TelephonySettings> mLastSettings = new SparseArray();
    private long mStartElapsedTimeMs;
    private long mStartSystemTimeMs;
    private final Deque<TelephonyEvent> mTelephonyEvents = new ArrayDeque();
    private boolean mTelephonyEventsDropped = false;

    private static /* synthetic */ int[] -getcom-android-internal-telephony-Call$StateSwitchesValues() {
        if (-com-android-internal-telephony-Call$StateSwitchesValues != null) {
            return -com-android-internal-telephony-Call$StateSwitchesValues;
        }
        int[] iArr = new int[State.values().length];
        try {
            iArr[State.ACTIVE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.ALERTING.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.DIALING.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[State.DISCONNECTED.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[State.DISCONNECTING.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[State.HOLDING.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[State.IDLE.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[State.INCOMING.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[State.WAITING.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        -com-android-internal-telephony-Call$StateSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getcom-android-internal-telephony-PhoneConstants$StateSwitchesValues() {
        if (-com-android-internal-telephony-PhoneConstants$StateSwitchesValues != null) {
            return -com-android-internal-telephony-PhoneConstants$StateSwitchesValues;
        }
        int[] iArr = new int[PhoneConstants.State.values().length];
        try {
            iArr[PhoneConstants.State.IDLE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[PhoneConstants.State.OFFHOOK.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[PhoneConstants.State.RINGING.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        -com-android-internal-telephony-PhoneConstants$StateSwitchesValues = iArr;
        return iArr;
    }

    public TelephonyMetrics() {
        reset();
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

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (args != null && args.length > 0) {
            String str = args[0];
            if (str.equals("--metrics")) {
                printAllMetrics(pw);
            } else if (str.equals("--metricsproto")) {
                pw.println(convertProtoToBase64String(buildProto()));
                reset();
            }
        }
    }

    private static String telephonyEventToString(int event) {
        switch (event) {
            case 0:
                return "UNKNOWN";
            case 1:
                return "SETTINGS_CHANGED";
            case 2:
                return "RIL_SERVICE_STATE_CHANGED";
            case 3:
                return "IMS_CONNECTION_STATE_CHANGED";
            case 4:
                return "IMS_CAPABILITIES_CHANGED";
            case 5:
                return "DATA_CALL_SETUP";
            case 6:
                return "DATA_CALL_SETUP_RESPONSE";
            case 7:
                return "DATA_CALL_LIST_CHANGED";
            case 8:
                return "DATA_CALL_DEACTIVATE";
            case 9:
                return "DATA_CALL_DEACTIVATE_RESPONSE";
            case 10:
                return "DATA_STALL_ACTION";
            case 11:
                return "MODEM_RESTART";
            default:
                return Integer.toString(event);
        }
    }

    private static String callSessionEventToString(int event) {
        switch (event) {
            case 0:
                return "EVENT_UNKNOWN";
            case 1:
                return "SETTINGS_CHANGED";
            case 2:
                return "RIL_SERVICE_STATE_CHANGED";
            case 3:
                return "IMS_CONNECTION_STATE_CHANGED";
            case 4:
                return "IMS_CAPABILITIES_CHANGED";
            case 5:
                return "DATA_CALL_LIST_CHANGED";
            case 6:
                return "RIL_REQUEST";
            case 7:
                return "RIL_RESPONSE";
            case 8:
                return "RIL_CALL_RING";
            case 9:
                return "RIL_CALL_SRVCC";
            case 10:
                return "RIL_CALL_LIST_CHANGED";
            case 11:
                return "IMS_COMMAND";
            case 12:
                return "IMS_COMMAND_RECEIVED";
            case 13:
                return "IMS_COMMAND_FAILED";
            case 14:
                return "IMS_COMMAND_COMPLETE";
            case 15:
                return "IMS_CALL_RECEIVE";
            case 16:
                return "IMS_CALL_STATE_CHANGED";
            case 17:
                return "IMS_CALL_TERMINATED";
            case 18:
                return "IMS_CALL_HANDOVER";
            case 19:
                return "IMS_CALL_HANDOVER_FAILED";
            case 20:
                return "PHONE_STATE_CHANGED";
            case 21:
                return "NITZ_TIME";
            default:
                return Integer.toString(event);
        }
    }

    private static String smsSessionEventToString(int event) {
        switch (event) {
            case 0:
                return "EVENT_UNKNOWN";
            case 1:
                return "SETTINGS_CHANGED";
            case 2:
                return "RIL_SERVICE_STATE_CHANGED";
            case 3:
                return "IMS_CONNECTION_STATE_CHANGED";
            case 4:
                return "IMS_CAPABILITIES_CHANGED";
            case 5:
                return "DATA_CALL_LIST_CHANGED";
            case 6:
                return "SMS_SEND";
            case 7:
                return "SMS_SEND_RESULT";
            case 8:
                return "SMS_RECEIVED";
            default:
                return Integer.toString(event);
        }
    }

    private synchronized void printAllMetrics(PrintWriter rawWriter) {
        int i;
        IndentingPrintWriter pw = new IndentingPrintWriter(rawWriter, "  ");
        pw.println("Telephony metrics proto:");
        pw.println("------------------------------------------");
        pw.println("Telephony events:");
        pw.increaseIndent();
        for (TelephonyEvent event : this.mTelephonyEvents) {
            pw.print(event.timestampMillis);
            pw.print(" [");
            pw.print(event.phoneId);
            pw.print("] ");
            pw.print("T=");
            if (event.type == 2) {
                pw.print(telephonyEventToString(event.type) + "(" + event.serviceState.dataRat + ")");
            } else {
                pw.print(telephonyEventToString(event.type));
            }
            pw.println("");
        }
        pw.decreaseIndent();
        pw.println("Call sessions:");
        pw.increaseIndent();
        for (TelephonyCallSession callSession : this.mCompletedCallSessions) {
            pw.println("Start time in minutes: " + callSession.startTimeMinutes);
            pw.println("Events dropped: " + callSession.eventsDropped);
            pw.println("Events: ");
            pw.increaseIndent();
            Event[] eventArr = callSession.events;
            i = 0;
            int length = eventArr.length;
            while (true) {
                int i2 = i;
                if (i2 >= length) {
                    break;
                }
                Event event2 = eventArr[i2];
                pw.print(event2.delay);
                pw.print(" T=");
                if (event2.type == 2) {
                    pw.println(callSessionEventToString(event2.type) + "(" + event2.serviceState.dataRat + ")");
                } else if (event2.type == 10) {
                    pw.println(callSessionEventToString(event2.type));
                    pw.increaseIndent();
                    for (RilCall call : event2.calls) {
                        pw.println(call.index + ". Type = " + call.type + " State = " + call.state + " End Reason " + call.callEndReason + " isMultiparty = " + call.isMultiparty);
                    }
                    pw.decreaseIndent();
                } else {
                    pw.println(callSessionEventToString(event2.type));
                }
                i = i2 + 1;
            }
            pw.decreaseIndent();
        }
        pw.decreaseIndent();
        pw.println("Sms sessions:");
        pw.increaseIndent();
        int count = 0;
        for (SmsSession smsSession : this.mCompletedSmsSessions) {
            count++;
            pw.print("[" + count + "] Start time in minutes: " + smsSession.startTimeMinutes);
            if (smsSession.eventsDropped) {
                pw.println(", events dropped: " + smsSession.eventsDropped);
            }
            pw.println("Events: ");
            pw.increaseIndent();
            for (SmsSession.Event event3 : smsSession.events) {
                pw.print(event3.delay);
                pw.print(" T=");
                pw.println(smsSessionEventToString(event3.type));
            }
            pw.decreaseIndent();
        }
        pw.decreaseIndent();
    }

    private static String convertProtoToBase64String(TelephonyLog proto) {
        return Base64.encodeToString(MessageNano.toByteArray(proto), 0);
    }

    private synchronized void reset() {
        int i;
        int key;
        this.mTelephonyEvents.clear();
        this.mCompletedCallSessions.clear();
        this.mCompletedSmsSessions.clear();
        this.mTelephonyEventsDropped = false;
        this.mStartSystemTimeMs = System.currentTimeMillis();
        this.mStartElapsedTimeMs = SystemClock.elapsedRealtime();
        for (i = 0; i < this.mLastServiceState.size(); i++) {
            key = this.mLastServiceState.keyAt(i);
            addTelephonyEvent(new TelephonyEventBuilder(this.mStartElapsedTimeMs, key).setServiceState((TelephonyServiceState) this.mLastServiceState.get(key)).build());
        }
        for (i = 0; i < this.mLastImsCapabilities.size(); i++) {
            key = this.mLastImsCapabilities.keyAt(i);
            addTelephonyEvent(new TelephonyEventBuilder(this.mStartElapsedTimeMs, key).setImsCapabilities((ImsCapabilities) this.mLastImsCapabilities.get(key)).build());
        }
        for (i = 0; i < this.mLastImsConnectionState.size(); i++) {
            key = this.mLastImsConnectionState.keyAt(i);
            addTelephonyEvent(new TelephonyEventBuilder(this.mStartElapsedTimeMs, key).setImsConnectionState((ImsConnectionState) this.mLastImsConnectionState.get(key)).build());
        }
    }

    private synchronized TelephonyLog buildProto() {
        TelephonyLog log;
        log = new TelephonyLog();
        log.events = new TelephonyEvent[this.mTelephonyEvents.size()];
        this.mTelephonyEvents.toArray(log.events);
        log.eventsDropped = this.mTelephonyEventsDropped;
        log.callSessions = new TelephonyCallSession[this.mCompletedCallSessions.size()];
        this.mCompletedCallSessions.toArray(log.callSessions);
        log.smsSessions = new SmsSession[this.mCompletedSmsSessions.size()];
        this.mCompletedSmsSessions.toArray(log.smsSessions);
        List<TelephonyHistogram> rilHistograms = RIL.getTelephonyRILTimingHistograms();
        log.histograms = new TelephonyProto.TelephonyHistogram[rilHistograms.size()];
        for (int i = 0; i < rilHistograms.size(); i++) {
            log.histograms[i] = new TelephonyProto.TelephonyHistogram();
            TelephonyHistogram rilHistogram = (TelephonyHistogram) rilHistograms.get(i);
            TelephonyProto.TelephonyHistogram histogramProto = log.histograms[i];
            histogramProto.category = rilHistogram.getCategory();
            histogramProto.id = rilHistogram.getId();
            histogramProto.minTimeMillis = rilHistogram.getMinTime();
            histogramProto.maxTimeMillis = rilHistogram.getMaxTime();
            histogramProto.avgTimeMillis = rilHistogram.getAverageTime();
            histogramProto.count = rilHistogram.getSampleCount();
            histogramProto.bucketCount = rilHistogram.getBucketCount();
            histogramProto.bucketEndPoints = rilHistogram.getBucketEndPoints();
            histogramProto.bucketCounters = rilHistogram.getBucketCounters();
        }
        log.startTime = new Time();
        log.startTime.systemTimestampMillis = this.mStartSystemTimeMs;
        log.startTime.elapsedTimestampMillis = this.mStartElapsedTimeMs;
        log.endTime = new Time();
        log.endTime.systemTimestampMillis = System.currentTimeMillis();
        log.endTime.elapsedTimestampMillis = SystemClock.elapsedRealtime();
        return log;
    }

    static int roundSessionStart(long timestamp) {
        return (int) ((timestamp / 300000) * 5);
    }

    static int toPrivacyFuzzedTimeInterval(long previousTimestamp, long currentTimestamp) {
        long diff = currentTimestamp - previousTimestamp;
        if (diff < 0) {
            return 0;
        }
        if (diff <= 10) {
            return 1;
        }
        if (diff <= 20) {
            return 2;
        }
        if (diff <= 50) {
            return 3;
        }
        if (diff <= 100) {
            return 4;
        }
        if (diff <= 200) {
            return 5;
        }
        if (diff <= 500) {
            return 6;
        }
        if (diff <= 1000) {
            return 7;
        }
        if (diff <= 2000) {
            return 8;
        }
        if (diff <= 5000) {
            return 9;
        }
        if (diff <= 10000) {
            return 10;
        }
        if (diff <= 30000) {
            return 11;
        }
        if (diff <= 60000) {
            return 12;
        }
        if (diff <= 180000) {
            return 13;
        }
        if (diff <= 600000) {
            return 14;
        }
        if (diff <= 1800000) {
            return 15;
        }
        if (diff <= 3600000) {
            return 16;
        }
        if (diff <= 7200000) {
            return 17;
        }
        if (diff <= 14400000) {
            return 18;
        }
        return 19;
    }

    private TelephonyServiceState toServiceStateProto(ServiceState serviceState) {
        TelephonyServiceState ssProto = new TelephonyServiceState();
        ssProto.voiceRoamingType = serviceState.getVoiceRoamingType();
        ssProto.dataRoamingType = serviceState.getDataRoamingType();
        ssProto.voiceOperator = new TelephonyOperator();
        if (serviceState.getVoiceOperatorAlphaLong() != null) {
            ssProto.voiceOperator.alphaLong = serviceState.getVoiceOperatorAlphaLong();
        }
        if (serviceState.getVoiceOperatorAlphaShort() != null) {
            ssProto.voiceOperator.alphaShort = serviceState.getVoiceOperatorAlphaShort();
        }
        if (serviceState.getVoiceOperatorNumeric() != null) {
            ssProto.voiceOperator.numeric = serviceState.getVoiceOperatorNumeric();
        }
        ssProto.dataOperator = new TelephonyOperator();
        if (serviceState.getDataOperatorAlphaLong() != null) {
            ssProto.dataOperator.alphaLong = serviceState.getDataOperatorAlphaLong();
        }
        if (serviceState.getDataOperatorAlphaShort() != null) {
            ssProto.dataOperator.alphaShort = serviceState.getDataOperatorAlphaShort();
        }
        if (serviceState.getDataOperatorNumeric() != null) {
            ssProto.dataOperator.numeric = serviceState.getDataOperatorNumeric();
        }
        ssProto.voiceRat = serviceState.getRilVoiceRadioTechnology();
        ssProto.dataRat = serviceState.getRilDataRadioTechnology();
        return ssProto;
    }

    private synchronized void annotateInProgressCallSession(long timestamp, int phoneId, CallSessionEventBuilder eventBuilder) {
        InProgressCallSession callSession = (InProgressCallSession) this.mInProgressCallSessions.get(phoneId);
        if (callSession != null) {
            callSession.addEvent(timestamp, eventBuilder);
        }
    }

    private synchronized void annotateInProgressSmsSession(long timestamp, int phoneId, SmsSessionEventBuilder eventBuilder) {
        InProgressSmsSession smsSession = (InProgressSmsSession) this.mInProgressSmsSessions.get(phoneId);
        if (smsSession != null) {
            smsSession.addEvent(timestamp, eventBuilder);
        }
    }

    private synchronized InProgressCallSession startNewCallSessionIfNeeded(int phoneId) {
        InProgressCallSession callSession;
        callSession = (InProgressCallSession) this.mInProgressCallSessions.get(phoneId);
        if (callSession == null) {
            callSession = new InProgressCallSession(phoneId);
            this.mInProgressCallSessions.append(phoneId, callSession);
            TelephonyServiceState serviceState = (TelephonyServiceState) this.mLastServiceState.get(phoneId);
            if (serviceState != null) {
                callSession.addEvent(callSession.startElapsedTimeMs, new CallSessionEventBuilder(2).setServiceState(serviceState));
            }
            ImsCapabilities imsCapabilities = (ImsCapabilities) this.mLastImsCapabilities.get(phoneId);
            if (imsCapabilities != null) {
                callSession.addEvent(callSession.startElapsedTimeMs, new CallSessionEventBuilder(4).setImsCapabilities(imsCapabilities));
            }
            ImsConnectionState imsConnectionState = (ImsConnectionState) this.mLastImsConnectionState.get(phoneId);
            if (imsConnectionState != null) {
                callSession.addEvent(callSession.startElapsedTimeMs, new CallSessionEventBuilder(3).setImsConnectionState(imsConnectionState));
            }
        }
        return callSession;
    }

    private synchronized InProgressSmsSession startNewSmsSessionIfNeeded(int phoneId) {
        InProgressSmsSession smsSession;
        smsSession = (InProgressSmsSession) this.mInProgressSmsSessions.get(phoneId);
        if (smsSession == null) {
            smsSession = new InProgressSmsSession(phoneId);
            this.mInProgressSmsSessions.append(phoneId, smsSession);
            TelephonyServiceState serviceState = (TelephonyServiceState) this.mLastServiceState.get(phoneId);
            if (serviceState != null) {
                smsSession.addEvent(smsSession.startElapsedTimeMs, new SmsSessionEventBuilder(2).setServiceState(serviceState));
            }
            ImsCapabilities imsCapabilities = (ImsCapabilities) this.mLastImsCapabilities.get(phoneId);
            if (imsCapabilities != null) {
                smsSession.addEvent(smsSession.startElapsedTimeMs, new SmsSessionEventBuilder(4).setImsCapabilities(imsCapabilities));
            }
            ImsConnectionState imsConnectionState = (ImsConnectionState) this.mLastImsConnectionState.get(phoneId);
            if (imsConnectionState != null) {
                smsSession.addEvent(smsSession.startElapsedTimeMs, new SmsSessionEventBuilder(3).setImsConnectionState(imsConnectionState));
            }
        }
        return smsSession;
    }

    private synchronized void finishCallSession(InProgressCallSession inProgressCallSession) {
        TelephonyCallSession callSession = new TelephonyCallSession();
        callSession.events = new Event[inProgressCallSession.events.size()];
        inProgressCallSession.events.toArray(callSession.events);
        callSession.startTimeMinutes = inProgressCallSession.startSystemTimeMin;
        callSession.phoneId = inProgressCallSession.phoneId;
        callSession.eventsDropped = inProgressCallSession.isEventsDropped();
        if (this.mCompletedCallSessions.size() >= 50) {
            this.mCompletedCallSessions.removeFirst();
        }
        this.mCompletedCallSessions.add(callSession);
        this.mInProgressCallSessions.remove(inProgressCallSession.phoneId);
    }

    private synchronized void finishSmsSessionIfNeeded(InProgressSmsSession inProgressSmsSession) {
        if (inProgressSmsSession.getNumExpectedResponses() == 0) {
            SmsSession smsSession = new SmsSession();
            smsSession.events = new SmsSession.Event[inProgressSmsSession.events.size()];
            inProgressSmsSession.events.toArray(smsSession.events);
            smsSession.startTimeMinutes = inProgressSmsSession.startSystemTimeMin;
            smsSession.phoneId = inProgressSmsSession.phoneId;
            smsSession.eventsDropped = inProgressSmsSession.isEventsDropped();
            if (this.mCompletedSmsSessions.size() >= MAX_COMPLETED_SMS_SESSIONS) {
                this.mCompletedSmsSessions.removeFirst();
            }
            this.mCompletedSmsSessions.add(smsSession);
            this.mInProgressSmsSessions.remove(inProgressSmsSession.phoneId);
        }
    }

    private synchronized void addTelephonyEvent(TelephonyEvent event) {
        if (this.mTelephonyEvents.size() >= 1000) {
            this.mTelephonyEvents.removeFirst();
            this.mTelephonyEventsDropped = true;
        }
        this.mTelephonyEvents.add(event);
    }

    public synchronized void writeServiceStateChanged(int phoneId, ServiceState serviceState) {
        TelephonyEvent event = new TelephonyEventBuilder(phoneId).setServiceState(toServiceStateProto(serviceState)).build();
        if (this.mLastServiceState.get(phoneId) == null || !Arrays.equals(MessageNano.toByteArray((MessageNano) this.mLastServiceState.get(phoneId)), MessageNano.toByteArray(event.serviceState))) {
            this.mLastServiceState.put(phoneId, event.serviceState);
            addTelephonyEvent(event);
            annotateInProgressCallSession(event.timestampMillis, phoneId, new CallSessionEventBuilder(2).setServiceState(event.serviceState));
            annotateInProgressSmsSession(event.timestampMillis, phoneId, new SmsSessionEventBuilder(2).setServiceState(event.serviceState));
        }
    }

    public void writeDataStallEvent(int phoneId, int recoveryAction) {
        addTelephonyEvent(new TelephonyEventBuilder(phoneId).setDataStallRecoveryAction(recoveryAction).build());
    }

    public void writeImsSetFeatureValue(int phoneId, int feature, int network, int value, int status) {
        boolean z = false;
        TelephonySettings s = new TelephonySettings();
        switch (feature) {
            case 0:
                if (value != 0) {
                    z = true;
                }
                s.isEnhanced4GLteModeEnabled = z;
                break;
            case 1:
                if (value != 0) {
                    z = true;
                }
                s.isVtOverLteEnabled = z;
                break;
            case 2:
                if (value != 0) {
                    z = true;
                }
                s.isWifiCallingEnabled = z;
                break;
            case 3:
                if (value != 0) {
                    z = true;
                }
                s.isVtOverWifiEnabled = z;
                break;
        }
        if (this.mLastSettings.get(phoneId) == null || !Arrays.equals(MessageNano.toByteArray((MessageNano) this.mLastSettings.get(phoneId)), MessageNano.toByteArray(s))) {
            this.mLastSettings.put(phoneId, s);
            TelephonyEvent event = new TelephonyEventBuilder(phoneId).setSettings(s).build();
            addTelephonyEvent(event);
            annotateInProgressCallSession(event.timestampMillis, phoneId, new CallSessionEventBuilder(1).setSettings(s));
            annotateInProgressSmsSession(event.timestampMillis, phoneId, new SmsSessionEventBuilder(1).setSettings(s));
        }
    }

    public void writeSetPreferredNetworkType(int phoneId, int networkType) {
        TelephonySettings s = new TelephonySettings();
        s.preferredNetworkMode = networkType + 1;
        if (this.mLastSettings.get(phoneId) == null || !Arrays.equals(MessageNano.toByteArray((MessageNano) this.mLastSettings.get(phoneId)), MessageNano.toByteArray(s))) {
            this.mLastSettings.put(phoneId, s);
            addTelephonyEvent(new TelephonyEventBuilder(phoneId).setSettings(s).build());
        }
    }

    public synchronized void writeOnImsConnectionState(int phoneId, int state, ImsReasonInfo reasonInfo) {
        ImsConnectionState imsState = new ImsConnectionState();
        imsState.state = state;
        if (reasonInfo != null) {
            TelephonyProto.ImsReasonInfo ri = new TelephonyProto.ImsReasonInfo();
            ri.reasonCode = reasonInfo.getCode();
            ri.extraCode = reasonInfo.getExtraCode();
            String extraMessage = reasonInfo.getExtraMessage();
            if (extraMessage != null) {
                ri.extraMessage = extraMessage;
            }
            imsState.reasonInfo = ri;
        }
        if (this.mLastImsConnectionState.get(phoneId) == null || !Arrays.equals(MessageNano.toByteArray((MessageNano) this.mLastImsConnectionState.get(phoneId)), MessageNano.toByteArray(imsState))) {
            this.mLastImsConnectionState.put(phoneId, imsState);
            TelephonyEvent event = new TelephonyEventBuilder(phoneId).setImsConnectionState(imsState).build();
            addTelephonyEvent(event);
            annotateInProgressCallSession(event.timestampMillis, phoneId, new CallSessionEventBuilder(3).setImsConnectionState(event.imsConnectionState));
            annotateInProgressSmsSession(event.timestampMillis, phoneId, new SmsSessionEventBuilder(3).setImsConnectionState(event.imsConnectionState));
        }
    }

    public synchronized void writeOnImsCapabilities(int phoneId, boolean[] capabilities) {
        ImsCapabilities cap = new ImsCapabilities();
        cap.voiceOverLte = capabilities[0];
        cap.videoOverLte = capabilities[1];
        cap.voiceOverWifi = capabilities[2];
        cap.videoOverWifi = capabilities[3];
        cap.utOverLte = capabilities[4];
        cap.utOverWifi = capabilities[5];
        TelephonyEvent event = new TelephonyEventBuilder(phoneId).setImsCapabilities(cap).build();
        if (this.mLastImsCapabilities.get(phoneId) == null || !Arrays.equals(MessageNano.toByteArray((MessageNano) this.mLastImsCapabilities.get(phoneId)), MessageNano.toByteArray(cap))) {
            this.mLastImsCapabilities.put(phoneId, cap);
            addTelephonyEvent(event);
            annotateInProgressCallSession(event.timestampMillis, phoneId, new CallSessionEventBuilder(4).setImsCapabilities(event.imsCapabilities));
            annotateInProgressSmsSession(event.timestampMillis, phoneId, new SmsSessionEventBuilder(4).setImsCapabilities(event.imsCapabilities));
        }
    }

    private int toPdpType(String type) {
        if (type.equals("IP")) {
            return 1;
        }
        if (type.equals("IPV6")) {
            return 2;
        }
        if (type.equals("IPV4V6")) {
            return 3;
        }
        if (type.equals("PPP")) {
            return 4;
        }
        Rlog.e(TAG, "Unknown type: " + type);
        return 0;
    }

    public void writeRilSetupDataCall(int phoneId, int rilSerial, int radioTechnology, int profile, String apn, int authType, String protocol) {
        RilSetupDataCall setupDataCall = new RilSetupDataCall();
        setupDataCall.rat = radioTechnology;
        setupDataCall.dataProfile = profile + 1;
        if (apn != null) {
            setupDataCall.apn = apn;
        }
        if (protocol != null) {
            setupDataCall.type = toPdpType(protocol);
        }
        addTelephonyEvent(new TelephonyEventBuilder(phoneId).setSetupDataCall(setupDataCall).build());
    }

    public void writeRilDeactivateDataCall(int phoneId, int rilSerial, int cid, int reason) {
        RilDeactivateDataCall deactivateDataCall = new RilDeactivateDataCall();
        deactivateDataCall.cid = cid;
        deactivateDataCall.reason = reason + 1;
        addTelephonyEvent(new TelephonyEventBuilder(phoneId).setDeactivateDataCall(deactivateDataCall).build());
    }

    public void writeRilDataCallList(int phoneId, ArrayList<DataCallResponse> dcsList) {
        RilDataCall[] dataCalls = new RilDataCall[dcsList.size()];
        for (int i = 0; i < dcsList.size(); i++) {
            dataCalls[i] = new RilDataCall();
            dataCalls[i].cid = ((DataCallResponse) dcsList.get(i)).cid;
            if (((DataCallResponse) dcsList.get(i)).ifname != null) {
                dataCalls[i].iframe = ((DataCallResponse) dcsList.get(i)).ifname;
            }
            if (((DataCallResponse) dcsList.get(i)).type != null) {
                dataCalls[i].type = toPdpType(((DataCallResponse) dcsList.get(i)).type);
            }
        }
        addTelephonyEvent(new TelephonyEventBuilder(phoneId).setDataCalls(dataCalls).build());
    }

    public void writeRilCallList(int phoneId, ArrayList<GsmCdmaConnection> connections) {
        InProgressCallSession callSession = startNewCallSessionIfNeeded(phoneId);
        if (callSession == null) {
            Rlog.e(TAG, "writeRilCallList: Call session is missing");
            return;
        }
        RilCall[] calls = convertConnectionsToRilCalls(connections);
        callSession.addEvent(new CallSessionEventBuilder(10).setRilCalls(calls));
        if (callSession.isPhoneIdle() && disconnectReasonsKnown(calls)) {
            finishCallSession(callSession);
        }
    }

    private boolean disconnectReasonsKnown(RilCall[] calls) {
        for (RilCall call : calls) {
            if (call.callEndReason == 0) {
                return false;
            }
        }
        return true;
    }

    private RilCall[] convertConnectionsToRilCalls(ArrayList<GsmCdmaConnection> mConnections) {
        RilCall[] calls = new RilCall[mConnections.size()];
        for (int i = 0; i < mConnections.size(); i++) {
            calls[i] = new RilCall();
            calls[i].index = i;
            convertConnectionToRilCall((GsmCdmaConnection) mConnections.get(i), calls[i]);
        }
        return calls;
    }

    private void convertConnectionToRilCall(GsmCdmaConnection conn, RilCall call) {
        if (conn.isIncoming()) {
            call.type = 2;
        } else {
            call.type = 1;
        }
        switch (-getcom-android-internal-telephony-Call$StateSwitchesValues()[conn.getState().ordinal()]) {
            case 1:
                call.state = 2;
                break;
            case 2:
                call.state = 5;
                break;
            case 3:
                call.state = 4;
                break;
            case 4:
                call.state = 8;
                break;
            case 5:
                call.state = 9;
                break;
            case 6:
                call.state = 3;
                break;
            case 7:
                call.state = 1;
                break;
            case 8:
                call.state = 6;
                break;
            case 9:
                call.state = 7;
                break;
            default:
                call.state = 0;
                break;
        }
        call.callEndReason = conn.getDisconnectCause();
        call.isMultiparty = conn.isMultiparty();
    }

    public void writeRilDial(int phoneId, GsmCdmaConnection conn, int clirMode, UUSInfo uusInfo) {
        InProgressCallSession callSession = startNewCallSessionIfNeeded(phoneId);
        if (callSession == null) {
            Rlog.e(TAG, "writeRilDial: Call session is missing");
            return;
        }
        RilCall[] calls = new RilCall[]{new RilCall()};
        calls[0].index = -1;
        convertConnectionToRilCall(conn, calls[0]);
        callSession.addEvent(callSession.startElapsedTimeMs, new CallSessionEventBuilder(6).setRilRequest(1).setRilCalls(calls));
    }

    public void writeRilCallRing(int phoneId, char[] response) {
        InProgressCallSession callSession = startNewCallSessionIfNeeded(phoneId);
        callSession.addEvent(callSession.startElapsedTimeMs, new CallSessionEventBuilder(8));
    }

    public void writeRilHangup(int phoneId, GsmCdmaConnection conn, int callId) {
        InProgressCallSession callSession = (InProgressCallSession) this.mInProgressCallSessions.get(phoneId);
        if (callSession == null) {
            Rlog.e(TAG, "writeRilHangup: Call session is missing");
            return;
        }
        RilCall[] calls = new RilCall[]{new RilCall()};
        calls[0].index = callId;
        convertConnectionToRilCall(conn, calls[0]);
        callSession.addEvent(new CallSessionEventBuilder(6).setRilRequest(3).setRilCalls(calls));
    }

    public void writeRilAnswer(int phoneId, int rilSerial) {
        InProgressCallSession callSession = (InProgressCallSession) this.mInProgressCallSessions.get(phoneId);
        if (callSession == null) {
            Rlog.e(TAG, "writeRilAnswer: Call session is missing");
        } else {
            callSession.addEvent(new CallSessionEventBuilder(6).setRilRequest(2).setRilRequestId(rilSerial));
        }
    }

    public void writeRilSrvcc(int phoneId, int rilSrvccState) {
        InProgressCallSession callSession = (InProgressCallSession) this.mInProgressCallSessions.get(phoneId);
        if (callSession == null) {
            Rlog.e(TAG, "writeRilSrvcc: Call session is missing");
        } else {
            callSession.addEvent(new CallSessionEventBuilder(9).setSrvccState(rilSrvccState + 1));
        }
    }

    private int toCallSessionRilRequest(int r) {
        switch (r) {
            case 10:
                return 1;
            case 12:
            case 13:
            case 14:
                return 3;
            case 15:
                return 5;
            case 16:
                return 7;
            case 36:
                return 4;
            case 40:
                return 2;
            case 84:
                return 6;
            default:
                Rlog.e(TAG, "Unknown RIL request: " + r);
                return 0;
        }
    }

    private void writeOnSetupDataCallResponse(int phoneId, int rilSerial, int rilError, int rilRequest, DataCallResponse response) {
        RilSetupDataCallResponse setupDataCallResponse = new RilSetupDataCallResponse();
        RilDataCall dataCall = new RilDataCall();
        if (response != null) {
            setupDataCallResponse.status = response.status == 0 ? 1 : response.status;
            setupDataCallResponse.suggestedRetryTimeMillis = response.suggestedRetryTime;
            dataCall.cid = response.cid;
            if (response.type != null) {
                dataCall.type = toPdpType(response.type);
            }
            if (response.ifname != null) {
                dataCall.iframe = response.ifname;
            }
        }
        setupDataCallResponse.call = dataCall;
        addTelephonyEvent(new TelephonyEventBuilder(phoneId).setSetupDataCallResponse(setupDataCallResponse).build());
    }

    private void writeOnCallSolicitedResponse(int phoneId, int rilSerial, int rilError, int rilRequest) {
        InProgressCallSession callSession = (InProgressCallSession) this.mInProgressCallSessions.get(phoneId);
        if (callSession == null) {
            Rlog.e(TAG, "writeOnCallSolicitedResponse: Call session is missing");
        } else {
            callSession.addEvent(new CallSessionEventBuilder(7).setRilRequest(toCallSessionRilRequest(rilRequest)).setRilRequestId(rilSerial).setRilError(rilError + 1));
        }
    }

    private synchronized void writeOnSmsSolicitedResponse(int phoneId, int rilSerial, int rilError, SmsResponse response) {
        InProgressSmsSession smsSession = (InProgressSmsSession) this.mInProgressSmsSessions.get(phoneId);
        if (smsSession == null) {
            Rlog.e(TAG, "SMS session is missing");
        } else {
            int errorCode = 0;
            if (response != null) {
                errorCode = response.mErrorCode;
            }
            smsSession.addEvent(new SmsSessionEventBuilder(7).setErrorCode(errorCode).setRilErrno(rilError + 1).setRilRequestId(rilSerial));
            smsSession.decreaseExpectedResponse();
            finishSmsSessionIfNeeded(smsSession);
        }
    }

    private void writeOnDeactivateDataCallResponse(int phoneId, int rilError) {
        addTelephonyEvent(new TelephonyEventBuilder(phoneId).setDeactivateDataCallResponse(rilError + 1).build());
    }

    public void writeOnRilSolicitedResponse(int phoneId, int rilSerial, int rilError, int rilRequest, Object ret) {
        switch (rilRequest) {
            case 10:
            case 12:
            case 13:
            case 14:
            case 40:
                writeOnCallSolicitedResponse(phoneId, rilSerial, rilError, rilRequest);
                return;
            case 25:
            case 26:
            case 87:
            case 113:
                writeOnSmsSolicitedResponse(phoneId, rilSerial, rilError, (SmsResponse) ret);
                return;
            case 27:
                writeOnSetupDataCallResponse(phoneId, rilSerial, rilError, rilRequest, (DataCallResponse) ret);
                return;
            case 41:
                writeOnDeactivateDataCallResponse(phoneId, rilError);
                return;
            default:
                return;
        }
    }

    public void writePhoneState(int phoneId, PhoneConstants.State phoneState) {
        int state;
        switch (-getcom-android-internal-telephony-PhoneConstants$StateSwitchesValues()[phoneState.ordinal()]) {
            case 1:
                state = 1;
                break;
            case 2:
                state = 3;
                break;
            case 3:
                state = 2;
                break;
            default:
                state = 0;
                break;
        }
        InProgressCallSession callSession = (InProgressCallSession) this.mInProgressCallSessions.get(phoneId);
        if (callSession == null) {
            Rlog.e(TAG, "writePhoneState: Call session is missing");
            return;
        }
        callSession.setLastKnownPhoneState(state);
        if (state == 1 && (callSession.containsCsCalls() ^ 1) != 0) {
            finishCallSession(callSession);
        }
        callSession.addEvent(new CallSessionEventBuilder(20).setPhoneState(state));
    }

    private int getCallId(ImsCallSession session) {
        if (session == null) {
            return -1;
        }
        try {
            return Integer.parseInt(session.getCallId());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public void writeImsCallState(int phoneId, ImsCallSession session, State callState) {
        int state;
        switch (-getcom-android-internal-telephony-Call$StateSwitchesValues()[callState.ordinal()]) {
            case 1:
                state = 2;
                break;
            case 2:
                state = 5;
                break;
            case 3:
                state = 4;
                break;
            case 4:
                state = 8;
                break;
            case 5:
                state = 9;
                break;
            case 6:
                state = 3;
                break;
            case 7:
                state = 1;
                break;
            case 8:
                state = 6;
                break;
            case 9:
                state = 7;
                break;
            default:
                state = 0;
                break;
        }
        InProgressCallSession callSession = (InProgressCallSession) this.mInProgressCallSessions.get(phoneId);
        if (callSession == null) {
            Rlog.e(TAG, "Call session is missing");
        } else {
            callSession.addEvent(new CallSessionEventBuilder(16).setCallIndex(getCallId(session)).setCallState(state));
        }
    }

    public void writeOnImsCallStart(int phoneId, ImsCallSession session) {
        startNewCallSessionIfNeeded(phoneId).addEvent(new CallSessionEventBuilder(11).setCallIndex(getCallId(session)).setImsCommand(1));
    }

    public void writeOnImsCallReceive(int phoneId, ImsCallSession session) {
        startNewCallSessionIfNeeded(phoneId).addEvent(new CallSessionEventBuilder(15).setCallIndex(getCallId(session)));
    }

    public void writeOnImsCommand(int phoneId, ImsCallSession session, int command) {
        InProgressCallSession callSession = (InProgressCallSession) this.mInProgressCallSessions.get(phoneId);
        if (callSession == null) {
            Rlog.e(TAG, "Call session is missing");
        } else {
            callSession.addEvent(new CallSessionEventBuilder(11).setCallIndex(getCallId(session)).setImsCommand(command));
        }
    }

    private TelephonyProto.ImsReasonInfo toImsReasonInfoProto(ImsReasonInfo reasonInfo) {
        TelephonyProto.ImsReasonInfo ri = new TelephonyProto.ImsReasonInfo();
        if (reasonInfo != null) {
            ri.reasonCode = reasonInfo.getCode();
            ri.extraCode = reasonInfo.getExtraCode();
            String extraMessage = reasonInfo.getExtraMessage();
            if (extraMessage != null) {
                ri.extraMessage = extraMessage;
            }
        }
        return ri;
    }

    public void writeOnImsCallTerminated(int phoneId, ImsCallSession session, ImsReasonInfo reasonInfo) {
        InProgressCallSession callSession = (InProgressCallSession) this.mInProgressCallSessions.get(phoneId);
        if (callSession == null) {
            Rlog.e(TAG, "Call session is missing");
        } else {
            callSession.addEvent(new CallSessionEventBuilder(17).setCallIndex(getCallId(session)).setImsReasonInfo(toImsReasonInfoProto(reasonInfo)));
        }
    }

    public void writeOnImsCallHandoverEvent(int phoneId, int eventType, ImsCallSession session, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
        InProgressCallSession callSession = (InProgressCallSession) this.mInProgressCallSessions.get(phoneId);
        if (callSession == null) {
            Rlog.e(TAG, "Call session is missing");
        } else {
            callSession.addEvent(new CallSessionEventBuilder(eventType).setCallIndex(getCallId(session)).setSrcAccessTech(srcAccessTech).setTargetAccessTech(targetAccessTech).setImsReasonInfo(toImsReasonInfoProto(reasonInfo)));
        }
    }

    public void writeRilSendSms(int phoneId, int rilSerial, int tech, int format) {
        InProgressSmsSession smsSession = startNewSmsSessionIfNeeded(phoneId);
        smsSession.addEvent(new SmsSessionEventBuilder(6).setTech(tech).setRilRequestId(rilSerial).setFormat(format));
        smsSession.increaseExpectedResponse();
    }

    public void writeRilNewSms(int phoneId, int tech, int format) {
        InProgressSmsSession smsSession = startNewSmsSessionIfNeeded(phoneId);
        smsSession.addEvent(new SmsSessionEventBuilder(8).setTech(tech).setFormat(format));
        finishSmsSessionIfNeeded(smsSession);
    }

    public void writeNITZEvent(int phoneId, long timestamp) {
        TelephonyEvent event = new TelephonyEventBuilder(phoneId).setNITZ(timestamp).build();
        addTelephonyEvent(event);
        annotateInProgressCallSession(event.timestampMillis, phoneId, new CallSessionEventBuilder(21).setNITZ(timestamp));
    }

    public void writeModemRestartEvent(int phoneId, String reason) {
        ModemRestart modemRestart = new ModemRestart();
        String basebandVersion = Build.getRadioVersion();
        if (basebandVersion != null) {
            modemRestart.basebandVersion = basebandVersion;
        }
        if (reason != null) {
            modemRestart.reason = reason;
        }
        addTelephonyEvent(new TelephonyEventBuilder(phoneId).setModemRestart(modemRestart).build());
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

    public void writeOnRilTimeoutResponse(int phoneId, int rilSerial, int rilRequest) {
    }
}
