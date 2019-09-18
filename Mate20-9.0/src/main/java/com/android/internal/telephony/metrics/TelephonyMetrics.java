package com.android.internal.telephony.metrics;

import android.hardware.radio.V1_0.SetupDataCallResult;
import android.os.Build;
import android.os.SystemClock;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.TelephonyHistogram;
import android.telephony.data.DataCallResponse;
import android.telephony.ims.ImsCallSession;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.feature.MmTelFeature;
import android.text.TextUtils;
import android.util.Base64;
import android.util.SparseArray;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.GsmCdmaConnection;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.RIL;
import com.android.internal.telephony.SmsResponse;
import com.android.internal.telephony.UUSInfo;
import com.android.internal.telephony.nano.TelephonyProto;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public class TelephonyMetrics {
    private static final boolean DBG = true;
    private static final int MAX_COMPLETED_CALL_SESSIONS = 50;
    private static final int MAX_COMPLETED_SMS_SESSIONS = 500;
    private static final int MAX_TELEPHONY_EVENTS = 1000;
    private static final int SESSION_START_PRECISION_MINUTES = 5;
    private static final String TAG = TelephonyMetrics.class.getSimpleName();
    private static final boolean VDBG = false;
    private static TelephonyMetrics sInstance;
    private final Deque<TelephonyProto.TelephonyCallSession> mCompletedCallSessions = new ArrayDeque();
    private final Deque<TelephonyProto.SmsSession> mCompletedSmsSessions = new ArrayDeque();
    private final SparseArray<InProgressCallSession> mInProgressCallSessions = new SparseArray<>();
    private final SparseArray<InProgressSmsSession> mInProgressSmsSessions = new SparseArray<>();
    private final SparseArray<TelephonyProto.ImsCapabilities> mLastImsCapabilities = new SparseArray<>();
    private final SparseArray<TelephonyProto.ImsConnectionState> mLastImsConnectionState = new SparseArray<>();
    private final SparseArray<TelephonyProto.TelephonyServiceState> mLastServiceState = new SparseArray<>();
    private final SparseArray<TelephonyProto.TelephonySettings> mLastSettings = new SparseArray<>();
    private long mStartElapsedTimeMs;
    private long mStartSystemTimeMs;
    private final Deque<TelephonyProto.TelephonyEvent> mTelephonyEvents = new ArrayDeque();
    private boolean mTelephonyEventsDropped = false;

    /* renamed from: com.android.internal.telephony.metrics.TelephonyMetrics$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$PhoneConstants$State = new int[PhoneConstants.State.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$PhoneConstants$State[PhoneConstants.State.IDLE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$PhoneConstants$State[PhoneConstants.State.RINGING.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$PhoneConstants$State[PhoneConstants.State.OFFHOOK.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            $SwitchMap$com$android$internal$telephony$Call$State = new int[Call.State.values().length];
            try {
                $SwitchMap$com$android$internal$telephony$Call$State[Call.State.IDLE.ordinal()] = 1;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$Call$State[Call.State.ACTIVE.ordinal()] = 2;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$Call$State[Call.State.HOLDING.ordinal()] = 3;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$Call$State[Call.State.DIALING.ordinal()] = 4;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$Call$State[Call.State.ALERTING.ordinal()] = 5;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$Call$State[Call.State.INCOMING.ordinal()] = 6;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$Call$State[Call.State.WAITING.ordinal()] = 7;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$Call$State[Call.State.DISCONNECTED.ordinal()] = 8;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$Call$State[Call.State.DISCONNECTING.ordinal()] = 9;
            } catch (NoSuchFieldError e12) {
            }
        }
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

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0028, code lost:
        if (r1.equals("--metrics") != false) goto L_0x002c;
     */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0030  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x003f  */
    /* JADX WARNING: Removed duplicated region for block: B:20:? A[RETURN, SYNTHETIC] */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (args != null && args.length > 0) {
            boolean z = false;
            String str = args[0];
            int hashCode = str.hashCode();
            if (hashCode != -1953159389) {
                if (hashCode == 950313125 && str.equals("--metricsproto")) {
                    z = true;
                    switch (z) {
                        case false:
                            printAllMetrics(pw);
                            return;
                        case true:
                            pw.println(convertProtoToBase64String(buildProto()));
                            reset();
                            return;
                        default:
                            return;
                    }
                }
            }
            z = true;
            switch (z) {
                case false:
                    break;
                case true:
                    break;
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
            case 13:
                return "CARRIER_ID_MATCHING";
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
        synchronized (this) {
            IndentingPrintWriter pw = new IndentingPrintWriter(rawWriter, "  ");
            pw.println("Telephony metrics proto:");
            pw.println("------------------------------------------");
            pw.println("Telephony events:");
            pw.increaseIndent();
            Iterator<TelephonyProto.TelephonyEvent> it = this.mTelephonyEvents.iterator();
            while (true) {
                i = 2;
                if (!it.hasNext()) {
                    break;
                }
                TelephonyProto.TelephonyEvent event = it.next();
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
            for (TelephonyProto.TelephonyCallSession callSession : this.mCompletedCallSessions) {
                pw.println("Start time in minutes: " + callSession.startTimeMinutes);
                pw.println("Events dropped: " + callSession.eventsDropped);
                pw.println("Events: ");
                pw.increaseIndent();
                TelephonyProto.TelephonyCallSession.Event[] eventArr = callSession.events;
                int length = eventArr.length;
                int i2 = 0;
                while (i2 < length) {
                    TelephonyProto.TelephonyCallSession.Event event2 = eventArr[i2];
                    pw.print(event2.delay);
                    pw.print(" T=");
                    if (event2.type == i) {
                        pw.println(callSessionEventToString(event2.type) + "(" + event2.serviceState.dataRat + ")");
                    } else if (event2.type == 10) {
                        pw.println(callSessionEventToString(event2.type));
                        pw.increaseIndent();
                        for (TelephonyProto.TelephonyCallSession.Event.RilCall call : event2.calls) {
                            pw.println(call.index + ". Type = " + call.type + " State = " + call.state + " End Reason " + call.callEndReason + " isMultiparty = " + call.isMultiparty);
                        }
                        pw.decreaseIndent();
                    } else {
                        pw.println(callSessionEventToString(event2.type));
                    }
                    i2++;
                    i = 2;
                }
                pw.decreaseIndent();
                i = 2;
            }
            pw.decreaseIndent();
            pw.println("Sms sessions:");
            pw.increaseIndent();
            int count = 0;
            for (TelephonyProto.SmsSession smsSession : this.mCompletedSmsSessions) {
                count++;
                pw.print("[" + count + "] Start time in minutes: " + smsSession.startTimeMinutes);
                if (smsSession.eventsDropped) {
                    pw.println(", events dropped: " + smsSession.eventsDropped);
                }
                pw.println("Events: ");
                pw.increaseIndent();
                for (TelephonyProto.SmsSession.Event event3 : smsSession.events) {
                    pw.print(event3.delay);
                    pw.print(" T=");
                    pw.println(smsSessionEventToString(event3.type));
                }
                pw.decreaseIndent();
            }
            pw.decreaseIndent();
            pw.println("Modem power stats:");
            pw.increaseIndent();
            TelephonyProto.ModemPowerStats s = new ModemPowerMetrics().buildProto();
            pw.println("Power log duration (battery time) (ms): " + s.loggingDurationMs);
            pw.println("Energy consumed by modem (mAh): " + s.energyConsumedMah);
            pw.println("Number of packets sent (tx): " + s.numPacketsTx);
            pw.println("Amount of time kernel is active because of cellular data (ms): " + s.cellularKernelActiveTimeMs);
            pw.println("Amount of time spent in very poor rx signal level (ms): " + s.timeInVeryPoorRxSignalLevelMs);
            pw.println("Amount of time modem is in sleep (ms): " + s.sleepTimeMs);
            pw.println("Amount of time modem is in idle (ms): " + s.idleTimeMs);
            pw.println("Amount of time modem is in rx (ms): " + s.rxTimeMs);
            pw.println("Amount of time modem is in tx (ms): " + Arrays.toString(s.txTimeMs));
            pw.decreaseIndent();
        }
    }

    private static String convertProtoToBase64String(TelephonyProto.TelephonyLog proto) {
        return Base64.encodeToString(TelephonyProto.TelephonyLog.toByteArray(proto), 0);
    }

    private synchronized void reset() {
        this.mTelephonyEvents.clear();
        this.mCompletedCallSessions.clear();
        this.mCompletedSmsSessions.clear();
        this.mTelephonyEventsDropped = false;
        this.mStartSystemTimeMs = System.currentTimeMillis();
        this.mStartElapsedTimeMs = SystemClock.elapsedRealtime();
        for (int i = 0; i < this.mLastServiceState.size(); i++) {
            int key = this.mLastServiceState.keyAt(i);
            addTelephonyEvent(new TelephonyEventBuilder(this.mStartElapsedTimeMs, key).setServiceState(this.mLastServiceState.get(key)).build());
        }
        for (int i2 = 0; i2 < this.mLastImsCapabilities.size(); i2++) {
            int key2 = this.mLastImsCapabilities.keyAt(i2);
            addTelephonyEvent(new TelephonyEventBuilder(this.mStartElapsedTimeMs, key2).setImsCapabilities(this.mLastImsCapabilities.get(key2)).build());
        }
        for (int i3 = 0; i3 < this.mLastImsConnectionState.size(); i3++) {
            int key3 = this.mLastImsConnectionState.keyAt(i3);
            addTelephonyEvent(new TelephonyEventBuilder(this.mStartElapsedTimeMs, key3).setImsConnectionState(this.mLastImsConnectionState.get(key3)).build());
        }
    }

    private synchronized TelephonyProto.TelephonyLog buildProto() {
        TelephonyProto.TelephonyLog log;
        log = new TelephonyProto.TelephonyLog();
        log.events = new TelephonyProto.TelephonyEvent[this.mTelephonyEvents.size()];
        this.mTelephonyEvents.toArray(log.events);
        log.eventsDropped = this.mTelephonyEventsDropped;
        log.callSessions = new TelephonyProto.TelephonyCallSession[this.mCompletedCallSessions.size()];
        this.mCompletedCallSessions.toArray(log.callSessions);
        log.smsSessions = new TelephonyProto.SmsSession[this.mCompletedSmsSessions.size()];
        this.mCompletedSmsSessions.toArray(log.smsSessions);
        List<TelephonyHistogram> rilHistograms = RIL.getTelephonyRILTimingHistograms();
        log.histograms = new TelephonyProto.TelephonyHistogram[rilHistograms.size()];
        for (int i = 0; i < rilHistograms.size(); i++) {
            log.histograms[i] = new TelephonyProto.TelephonyHistogram();
            TelephonyHistogram rilHistogram = rilHistograms.get(i);
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
        log.modemPowerStats = new ModemPowerMetrics().buildProto();
        log.startTime = new TelephonyProto.Time();
        log.startTime.systemTimestampMillis = this.mStartSystemTimeMs;
        log.startTime.elapsedTimestampMillis = this.mStartElapsedTimeMs;
        log.endTime = new TelephonyProto.Time();
        log.endTime.systemTimestampMillis = System.currentTimeMillis();
        log.endTime.elapsedTimestampMillis = SystemClock.elapsedRealtime();
        return log;
    }

    static int roundSessionStart(long timestamp) {
        return (int) ((timestamp / 300000) * 5);
    }

    public void writeCarrierKeyEvent(int phoneId, int keyType, boolean isDownloadSuccessful) {
        TelephonyProto.TelephonyEvent.CarrierKeyChange carrierKeyChange = new TelephonyProto.TelephonyEvent.CarrierKeyChange();
        carrierKeyChange.keyType = keyType;
        carrierKeyChange.isDownloadSuccessful = isDownloadSuccessful;
        addTelephonyEvent(new TelephonyEventBuilder(phoneId).setCarrierKeyChange(carrierKeyChange).build());
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

    private TelephonyProto.TelephonyServiceState toServiceStateProto(ServiceState serviceState) {
        TelephonyProto.TelephonyServiceState ssProto = new TelephonyProto.TelephonyServiceState();
        ssProto.voiceRoamingType = serviceState.getVoiceRoamingType();
        ssProto.dataRoamingType = serviceState.getDataRoamingType();
        ssProto.voiceOperator = new TelephonyProto.TelephonyServiceState.TelephonyOperator();
        if (serviceState.getVoiceOperatorAlphaLong() != null) {
            ssProto.voiceOperator.alphaLong = serviceState.getVoiceOperatorAlphaLong();
        }
        if (serviceState.getVoiceOperatorAlphaShort() != null) {
            ssProto.voiceOperator.alphaShort = serviceState.getVoiceOperatorAlphaShort();
        }
        if (serviceState.getVoiceOperatorNumeric() != null) {
            ssProto.voiceOperator.numeric = serviceState.getVoiceOperatorNumeric();
        }
        ssProto.dataOperator = new TelephonyProto.TelephonyServiceState.TelephonyOperator();
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
        InProgressCallSession callSession = this.mInProgressCallSessions.get(phoneId);
        if (callSession != null) {
            callSession.addEvent(timestamp, eventBuilder);
        }
    }

    private synchronized void annotateInProgressSmsSession(long timestamp, int phoneId, SmsSessionEventBuilder eventBuilder) {
        InProgressSmsSession smsSession = this.mInProgressSmsSessions.get(phoneId);
        if (smsSession != null) {
            smsSession.addEvent(timestamp, eventBuilder);
        }
    }

    private synchronized InProgressCallSession startNewCallSessionIfNeeded(int phoneId) {
        InProgressCallSession callSession;
        callSession = this.mInProgressCallSessions.get(phoneId);
        if (callSession == null) {
            callSession = new InProgressCallSession(phoneId);
            this.mInProgressCallSessions.append(phoneId, callSession);
            TelephonyProto.TelephonyServiceState serviceState = this.mLastServiceState.get(phoneId);
            if (serviceState != null) {
                callSession.addEvent(callSession.startElapsedTimeMs, new CallSessionEventBuilder(2).setServiceState(serviceState));
            }
            TelephonyProto.ImsCapabilities imsCapabilities = this.mLastImsCapabilities.get(phoneId);
            if (imsCapabilities != null) {
                callSession.addEvent(callSession.startElapsedTimeMs, new CallSessionEventBuilder(4).setImsCapabilities(imsCapabilities));
            }
            TelephonyProto.ImsConnectionState imsConnectionState = this.mLastImsConnectionState.get(phoneId);
            if (imsConnectionState != null) {
                callSession.addEvent(callSession.startElapsedTimeMs, new CallSessionEventBuilder(3).setImsConnectionState(imsConnectionState));
            }
        }
        return callSession;
    }

    private synchronized InProgressSmsSession startNewSmsSessionIfNeeded(int phoneId) {
        InProgressSmsSession smsSession;
        smsSession = this.mInProgressSmsSessions.get(phoneId);
        if (smsSession == null) {
            smsSession = new InProgressSmsSession(phoneId);
            this.mInProgressSmsSessions.append(phoneId, smsSession);
            TelephonyProto.TelephonyServiceState serviceState = this.mLastServiceState.get(phoneId);
            if (serviceState != null) {
                smsSession.addEvent(smsSession.startElapsedTimeMs, new SmsSessionEventBuilder(2).setServiceState(serviceState));
            }
            TelephonyProto.ImsCapabilities imsCapabilities = this.mLastImsCapabilities.get(phoneId);
            if (imsCapabilities != null) {
                smsSession.addEvent(smsSession.startElapsedTimeMs, new SmsSessionEventBuilder(4).setImsCapabilities(imsCapabilities));
            }
            TelephonyProto.ImsConnectionState imsConnectionState = this.mLastImsConnectionState.get(phoneId);
            if (imsConnectionState != null) {
                smsSession.addEvent(smsSession.startElapsedTimeMs, new SmsSessionEventBuilder(3).setImsConnectionState(imsConnectionState));
            }
        }
        return smsSession;
    }

    private synchronized void finishCallSession(InProgressCallSession inProgressCallSession) {
        TelephonyProto.TelephonyCallSession callSession = new TelephonyProto.TelephonyCallSession();
        callSession.events = new TelephonyProto.TelephonyCallSession.Event[inProgressCallSession.events.size()];
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
            TelephonyProto.SmsSession smsSession = new TelephonyProto.SmsSession();
            smsSession.events = new TelephonyProto.SmsSession.Event[inProgressSmsSession.events.size()];
            inProgressSmsSession.events.toArray(smsSession.events);
            smsSession.startTimeMinutes = inProgressSmsSession.startSystemTimeMin;
            smsSession.phoneId = inProgressSmsSession.phoneId;
            smsSession.eventsDropped = inProgressSmsSession.isEventsDropped();
            if (this.mCompletedSmsSessions.size() >= 500) {
                this.mCompletedSmsSessions.removeFirst();
            }
            this.mCompletedSmsSessions.add(smsSession);
            this.mInProgressSmsSessions.remove(inProgressSmsSession.phoneId);
        }
    }

    private synchronized void addTelephonyEvent(TelephonyProto.TelephonyEvent event) {
        if (this.mTelephonyEvents.size() >= 1000) {
            this.mTelephonyEvents.removeFirst();
            this.mTelephonyEventsDropped = true;
        }
        this.mTelephonyEvents.add(event);
    }

    public synchronized void writeServiceStateChanged(int phoneId, ServiceState serviceState) {
        TelephonyProto.TelephonyEvent event = new TelephonyEventBuilder(phoneId).setServiceState(toServiceStateProto(serviceState)).build();
        if (this.mLastServiceState.get(phoneId) == null || !Arrays.equals(TelephonyProto.TelephonyServiceState.toByteArray(this.mLastServiceState.get(phoneId)), TelephonyProto.TelephonyServiceState.toByteArray(event.serviceState))) {
            this.mLastServiceState.put(phoneId, event.serviceState);
            addTelephonyEvent(event);
            annotateInProgressCallSession(event.timestampMillis, phoneId, new CallSessionEventBuilder(2).setServiceState(event.serviceState));
            annotateInProgressSmsSession(event.timestampMillis, phoneId, new SmsSessionEventBuilder(2).setServiceState(event.serviceState));
        }
    }

    public void writeDataStallEvent(int phoneId, int recoveryAction) {
        addTelephonyEvent(new TelephonyEventBuilder(phoneId).setDataStallRecoveryAction(recoveryAction).build());
    }

    public void writeImsSetFeatureValue(int phoneId, int feature, int network, int value) {
        TelephonyProto.TelephonySettings s = new TelephonyProto.TelephonySettings();
        boolean z = false;
        if (network != 0) {
            if (network == 1) {
                switch (feature) {
                    case 1:
                        if (value != 0) {
                            z = true;
                        }
                        s.isWifiCallingEnabled = z;
                        break;
                    case 2:
                        if (value != 0) {
                            z = true;
                        }
                        s.isVtOverWifiEnabled = z;
                        break;
                }
            }
        } else {
            switch (feature) {
                case 1:
                    if (value != 0) {
                        z = true;
                    }
                    s.isEnhanced4GLteModeEnabled = z;
                    break;
                case 2:
                    if (value != 0) {
                        z = true;
                    }
                    s.isVtOverLteEnabled = z;
                    break;
            }
        }
        if (this.mLastSettings.get(phoneId) == null || !Arrays.equals(TelephonyProto.TelephonySettings.toByteArray(this.mLastSettings.get(phoneId)), TelephonyProto.TelephonySettings.toByteArray(s))) {
            this.mLastSettings.put(phoneId, s);
            TelephonyProto.TelephonyEvent event = new TelephonyEventBuilder(phoneId).setSettings(s).build();
            addTelephonyEvent(event);
            annotateInProgressCallSession(event.timestampMillis, phoneId, new CallSessionEventBuilder(1).setSettings(s));
            annotateInProgressSmsSession(event.timestampMillis, phoneId, new SmsSessionEventBuilder(1).setSettings(s));
        }
    }

    public void writeSetPreferredNetworkType(int phoneId, int networkType) {
        TelephonyProto.TelephonySettings s = new TelephonyProto.TelephonySettings();
        s.preferredNetworkMode = networkType + 1;
        if (this.mLastSettings.get(phoneId) == null || !Arrays.equals(TelephonyProto.TelephonySettings.toByteArray(this.mLastSettings.get(phoneId)), TelephonyProto.TelephonySettings.toByteArray(s))) {
            this.mLastSettings.put(phoneId, s);
            addTelephonyEvent(new TelephonyEventBuilder(phoneId).setSettings(s).build());
        }
    }

    public synchronized void writeOnImsConnectionState(int phoneId, int state, ImsReasonInfo reasonInfo) {
        TelephonyProto.ImsConnectionState imsState = new TelephonyProto.ImsConnectionState();
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
        if (this.mLastImsConnectionState.get(phoneId) == null || !Arrays.equals(TelephonyProto.ImsConnectionState.toByteArray(this.mLastImsConnectionState.get(phoneId)), TelephonyProto.ImsConnectionState.toByteArray(imsState))) {
            this.mLastImsConnectionState.put(phoneId, imsState);
            TelephonyProto.TelephonyEvent event = new TelephonyEventBuilder(phoneId).setImsConnectionState(imsState).build();
            addTelephonyEvent(event);
            annotateInProgressCallSession(event.timestampMillis, phoneId, new CallSessionEventBuilder(3).setImsConnectionState(event.imsConnectionState));
            annotateInProgressSmsSession(event.timestampMillis, phoneId, new SmsSessionEventBuilder(3).setImsConnectionState(event.imsConnectionState));
        }
    }

    public synchronized void writeOnImsCapabilities(int phoneId, int radioTech, MmTelFeature.MmTelCapabilities capabilities) {
        TelephonyProto.ImsCapabilities cap = new TelephonyProto.ImsCapabilities();
        if (radioTech == 0) {
            cap.voiceOverLte = capabilities.isCapable(1);
            cap.videoOverLte = capabilities.isCapable(2);
            cap.utOverLte = capabilities.isCapable(4);
        } else if (radioTech == 1) {
            cap.voiceOverWifi = capabilities.isCapable(1);
            cap.videoOverWifi = capabilities.isCapable(2);
            cap.utOverWifi = capabilities.isCapable(4);
        }
        TelephonyProto.TelephonyEvent event = new TelephonyEventBuilder(phoneId).setImsCapabilities(cap).build();
        if (this.mLastImsCapabilities.get(phoneId) == null || !Arrays.equals(TelephonyProto.ImsCapabilities.toByteArray(this.mLastImsCapabilities.get(phoneId)), TelephonyProto.ImsCapabilities.toByteArray(cap))) {
            this.mLastImsCapabilities.put(phoneId, cap);
            addTelephonyEvent(event);
            annotateInProgressCallSession(event.timestampMillis, phoneId, new CallSessionEventBuilder(4).setImsCapabilities(event.imsCapabilities));
            annotateInProgressSmsSession(event.timestampMillis, phoneId, new SmsSessionEventBuilder(4).setImsCapabilities(event.imsCapabilities));
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0048  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x005f  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0061 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0062 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0063 A[RETURN] */
    private int toPdpType(String type) {
        char c;
        int hashCode = type.hashCode();
        if (hashCode == -2128542875) {
            if (type.equals("IPV4V6")) {
                c = 2;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                }
            }
        } else if (hashCode == 2343) {
            if (type.equals("IP")) {
                c = 0;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                }
            }
        } else if (hashCode == 79440) {
            if (type.equals("PPP")) {
                c = 3;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                }
            }
        } else if (hashCode == 2254343 && type.equals("IPV6")) {
            c = 1;
            switch (c) {
                case 0:
                    return 1;
                case 1:
                    return 2;
                case 2:
                    return 3;
                case 3:
                    return 4;
                default:
                    Rlog.e(TAG, "Unknown type: " + type);
                    return 0;
            }
        }
        c = 65535;
        switch (c) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
        }
    }

    public void writeSetupDataCall(int phoneId, int radioTechnology, int profileId, String apn, String protocol) {
        TelephonyProto.TelephonyEvent.RilSetupDataCall setupDataCall = new TelephonyProto.TelephonyEvent.RilSetupDataCall();
        setupDataCall.rat = radioTechnology;
        setupDataCall.dataProfile = profileId + 1;
        if (apn != null) {
            setupDataCall.apn = apn;
        }
        if (protocol != null) {
            setupDataCall.type = toPdpType(protocol);
        }
        addTelephonyEvent(new TelephonyEventBuilder(phoneId).setSetupDataCall(setupDataCall).build());
    }

    public void writeRilDeactivateDataCall(int phoneId, int rilSerial, int cid, int reason) {
        TelephonyProto.TelephonyEvent.RilDeactivateDataCall deactivateDataCall = new TelephonyProto.TelephonyEvent.RilDeactivateDataCall();
        deactivateDataCall.cid = cid;
        switch (reason) {
            case 1:
                deactivateDataCall.reason = 1;
                break;
            case 2:
                deactivateDataCall.reason = 2;
                break;
            case 3:
                deactivateDataCall.reason = 4;
                break;
            default:
                deactivateDataCall.reason = 0;
                break;
        }
        addTelephonyEvent(new TelephonyEventBuilder(phoneId).setDeactivateDataCall(deactivateDataCall).build());
    }

    public void writeRilDataCallList(int phoneId, ArrayList<DataCallResponse> dcsList) {
        TelephonyProto.RilDataCall[] dataCalls = new TelephonyProto.RilDataCall[dcsList.size()];
        for (int i = 0; i < dcsList.size(); i++) {
            dataCalls[i] = new TelephonyProto.RilDataCall();
            dataCalls[i].cid = dcsList.get(i).getCallId();
            if (!TextUtils.isEmpty(dcsList.get(i).getIfname())) {
                dataCalls[i].iframe = dcsList.get(i).getIfname();
            }
            if (!TextUtils.isEmpty(dcsList.get(i).getType())) {
                dataCalls[i].type = toPdpType(dcsList.get(i).getType());
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
        TelephonyProto.TelephonyCallSession.Event.RilCall[] calls = convertConnectionsToRilCalls(connections);
        callSession.addEvent(new CallSessionEventBuilder(10).setRilCalls(calls));
        if (callSession.isPhoneIdle() && disconnectReasonsKnown(calls)) {
            finishCallSession(callSession);
        }
    }

    private boolean disconnectReasonsKnown(TelephonyProto.TelephonyCallSession.Event.RilCall[] calls) {
        for (TelephonyProto.TelephonyCallSession.Event.RilCall call : calls) {
            if (call.callEndReason == 0) {
                return false;
            }
        }
        return true;
    }

    private TelephonyProto.TelephonyCallSession.Event.RilCall[] convertConnectionsToRilCalls(ArrayList<GsmCdmaConnection> mConnections) {
        TelephonyProto.TelephonyCallSession.Event.RilCall[] calls = new TelephonyProto.TelephonyCallSession.Event.RilCall[mConnections.size()];
        for (int i = 0; i < mConnections.size(); i++) {
            calls[i] = new TelephonyProto.TelephonyCallSession.Event.RilCall();
            calls[i].index = i;
            convertConnectionToRilCall(mConnections.get(i), calls[i]);
        }
        return calls;
    }

    private void convertConnectionToRilCall(GsmCdmaConnection conn, TelephonyProto.TelephonyCallSession.Event.RilCall call) {
        if (conn.isIncoming()) {
            call.type = 2;
        } else {
            call.type = 1;
        }
        switch (conn.getState()) {
            case IDLE:
                call.state = 1;
                break;
            case ACTIVE:
                call.state = 2;
                break;
            case HOLDING:
                call.state = 3;
                break;
            case DIALING:
                call.state = 4;
                break;
            case ALERTING:
                call.state = 5;
                break;
            case INCOMING:
                call.state = 6;
                break;
            case WAITING:
                call.state = 7;
                break;
            case DISCONNECTED:
                call.state = 8;
                break;
            case DISCONNECTING:
                call.state = 9;
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
        TelephonyProto.TelephonyCallSession.Event.RilCall[] calls = {new TelephonyProto.TelephonyCallSession.Event.RilCall()};
        calls[0].index = -1;
        convertConnectionToRilCall(conn, calls[0]);
        callSession.addEvent(callSession.startElapsedTimeMs, new CallSessionEventBuilder(6).setRilRequest(1).setRilCalls(calls));
    }

    public void writeRilCallRing(int phoneId, char[] response) {
        InProgressCallSession callSession = startNewCallSessionIfNeeded(phoneId);
        callSession.addEvent(callSession.startElapsedTimeMs, new CallSessionEventBuilder(8));
    }

    public void writeRilHangup(int phoneId, GsmCdmaConnection conn, int callId) {
        InProgressCallSession callSession = this.mInProgressCallSessions.get(phoneId);
        if (callSession == null) {
            Rlog.e(TAG, "writeRilHangup: Call session is missing");
            return;
        }
        TelephonyProto.TelephonyCallSession.Event.RilCall[] calls = {new TelephonyProto.TelephonyCallSession.Event.RilCall()};
        calls[0].index = callId;
        convertConnectionToRilCall(conn, calls[0]);
        callSession.addEvent(new CallSessionEventBuilder(6).setRilRequest(3).setRilCalls(calls));
    }

    public void writeRilAnswer(int phoneId, int rilSerial) {
        InProgressCallSession callSession = this.mInProgressCallSessions.get(phoneId);
        if (callSession == null) {
            Rlog.e(TAG, "writeRilAnswer: Call session is missing");
        } else {
            callSession.addEvent(new CallSessionEventBuilder(6).setRilRequest(2).setRilRequestId(rilSerial));
        }
    }

    public void writeRilSrvcc(int phoneId, int rilSrvccState) {
        InProgressCallSession callSession = this.mInProgressCallSessions.get(phoneId);
        if (callSession == null) {
            Rlog.e(TAG, "writeRilSrvcc: Call session is missing");
        } else {
            callSession.addEvent(new CallSessionEventBuilder(9).setSrvccState(rilSrvccState + 1));
        }
    }

    private int toCallSessionRilRequest(int r) {
        if (r == 10) {
            return 1;
        }
        if (r == 36) {
            return 4;
        }
        if (r == 40) {
            return 2;
        }
        if (r == 84) {
            return 6;
        }
        switch (r) {
            case 12:
            case 13:
            case 14:
                return 3;
            case 15:
                return 5;
            case 16:
                return 7;
            default:
                String str = TAG;
                Rlog.e(str, "Unknown RIL request: " + r);
                return 0;
        }
    }

    private void writeOnSetupDataCallResponse(int phoneId, int rilSerial, int rilError, int rilRequest, SetupDataCallResult result) {
        TelephonyProto.TelephonyEvent.RilSetupDataCallResponse setupDataCallResponse = new TelephonyProto.TelephonyEvent.RilSetupDataCallResponse();
        TelephonyProto.RilDataCall dataCall = new TelephonyProto.RilDataCall();
        if (result != null) {
            setupDataCallResponse.status = result.status == 0 ? 1 : result.status;
            setupDataCallResponse.suggestedRetryTimeMillis = result.suggestedRetryTime;
            dataCall.cid = result.cid;
            if (!TextUtils.isEmpty(result.type)) {
                dataCall.type = toPdpType(result.type);
            }
            if (!TextUtils.isEmpty(result.ifname)) {
                dataCall.iframe = result.ifname;
            }
        }
        setupDataCallResponse.call = dataCall;
        addTelephonyEvent(new TelephonyEventBuilder(phoneId).setSetupDataCallResponse(setupDataCallResponse).build());
    }

    private void writeOnCallSolicitedResponse(int phoneId, int rilSerial, int rilError, int rilRequest) {
        InProgressCallSession callSession = this.mInProgressCallSessions.get(phoneId);
        if (callSession == null) {
            Rlog.e(TAG, "writeOnCallSolicitedResponse: Call session is missing");
        } else {
            callSession.addEvent(new CallSessionEventBuilder(7).setRilRequest(toCallSessionRilRequest(rilRequest)).setRilRequestId(rilSerial).setRilError(rilError + 1));
        }
    }

    private synchronized void writeOnSmsSolicitedResponse(int phoneId, int rilSerial, int rilError, SmsResponse response) {
        InProgressSmsSession smsSession = this.mInProgressSmsSessions.get(phoneId);
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
                writeOnSetupDataCallResponse(phoneId, rilSerial, rilError, rilRequest, (SetupDataCallResult) ret);
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
        switch (AnonymousClass1.$SwitchMap$com$android$internal$telephony$PhoneConstants$State[phoneState.ordinal()]) {
            case 1:
                state = 1;
                break;
            case 2:
                state = 2;
                break;
            case 3:
                state = 3;
                break;
            default:
                state = 0;
                break;
        }
        InProgressCallSession callSession = this.mInProgressCallSessions.get(phoneId);
        if (callSession == null) {
            Rlog.e(TAG, "writePhoneState: Call session is missing");
            return;
        }
        callSession.setLastKnownPhoneState(state);
        if (state == 1 && !callSession.containsCsCalls()) {
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

    public void writeImsCallState(int phoneId, ImsCallSession session, Call.State callState) {
        int state;
        switch (callState) {
            case IDLE:
                state = 1;
                break;
            case ACTIVE:
                state = 2;
                break;
            case HOLDING:
                state = 3;
                break;
            case DIALING:
                state = 4;
                break;
            case ALERTING:
                state = 5;
                break;
            case INCOMING:
                state = 6;
                break;
            case WAITING:
                state = 7;
                break;
            case DISCONNECTED:
                state = 8;
                break;
            case DISCONNECTING:
                state = 9;
                break;
            default:
                state = 0;
                break;
        }
        InProgressCallSession callSession = this.mInProgressCallSessions.get(phoneId);
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
        InProgressCallSession callSession = this.mInProgressCallSessions.get(phoneId);
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
        InProgressCallSession callSession = this.mInProgressCallSessions.get(phoneId);
        if (callSession == null) {
            Rlog.e(TAG, "Call session is missing");
        } else {
            callSession.addEvent(new CallSessionEventBuilder(17).setCallIndex(getCallId(session)).setImsReasonInfo(toImsReasonInfoProto(reasonInfo)));
        }
    }

    public void writeOnImsCallHandoverEvent(int phoneId, int eventType, ImsCallSession session, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
        InProgressCallSession callSession = this.mInProgressCallSessions.get(phoneId);
        if (callSession == null) {
            Rlog.e(TAG, "Call session is missing");
        } else {
            callSession.addEvent(new CallSessionEventBuilder(eventType).setCallIndex(getCallId(session)).setSrcAccessTech(srcAccessTech).setTargetAccessTech(targetAccessTech).setImsReasonInfo(toImsReasonInfoProto(reasonInfo)));
        }
    }

    public synchronized void writeRilSendSms(int phoneId, int rilSerial, int tech, int format) {
        InProgressSmsSession smsSession = startNewSmsSessionIfNeeded(phoneId);
        smsSession.addEvent(new SmsSessionEventBuilder(6).setTech(tech).setRilRequestId(rilSerial).setFormat(format));
        smsSession.increaseExpectedResponse();
    }

    public synchronized void writeRilNewSms(int phoneId, int tech, int format) {
        InProgressSmsSession smsSession = startNewSmsSessionIfNeeded(phoneId);
        smsSession.addEvent(new SmsSessionEventBuilder(8).setTech(tech).setFormat(format));
        finishSmsSessionIfNeeded(smsSession);
    }

    public synchronized void writeNewCBSms(int phoneId, int format, int priority, boolean isCMAS, boolean isETWS, int serviceCategory) {
        int type;
        InProgressSmsSession smsSession = startNewSmsSessionIfNeeded(phoneId);
        if (isCMAS) {
            type = 2;
        } else if (isETWS) {
            type = 1;
        } else {
            type = 3;
            TelephonyProto.SmsSession.Event.CBMessage cbm = new TelephonyProto.SmsSession.Event.CBMessage();
            cbm.msgFormat = format;
            cbm.msgPriority = priority + 1;
            cbm.msgType = type;
            cbm.serviceCategory = serviceCategory;
            smsSession.addEvent(new SmsSessionEventBuilder(9).setCellBroadcastMessage(cbm));
            finishSmsSessionIfNeeded(smsSession);
        }
        TelephonyProto.SmsSession.Event.CBMessage cbm2 = new TelephonyProto.SmsSession.Event.CBMessage();
        cbm2.msgFormat = format;
        cbm2.msgPriority = priority + 1;
        cbm2.msgType = type;
        cbm2.serviceCategory = serviceCategory;
        smsSession.addEvent(new SmsSessionEventBuilder(9).setCellBroadcastMessage(cbm2));
        finishSmsSessionIfNeeded(smsSession);
    }

    public void writeNITZEvent(int phoneId, long timestamp) {
        TelephonyProto.TelephonyEvent event = new TelephonyEventBuilder(phoneId).setNITZ(timestamp).build();
        addTelephonyEvent(event);
        annotateInProgressCallSession(event.timestampMillis, phoneId, new CallSessionEventBuilder(21).setNITZ(timestamp));
    }

    public void writeModemRestartEvent(int phoneId, String reason) {
        TelephonyProto.TelephonyEvent.ModemRestart modemRestart = new TelephonyProto.TelephonyEvent.ModemRestart();
        String basebandVersion = Build.getRadioVersion();
        if (basebandVersion != null) {
            modemRestart.basebandVersion = basebandVersion;
        }
        if (reason != null) {
            modemRestart.reason = reason;
        }
        addTelephonyEvent(new TelephonyEventBuilder(phoneId).setModemRestart(modemRestart).build());
    }

    public void writeCarrierIdMatchingEvent(int phoneId, int version, int cid, String mccmnc, String gid1) {
        TelephonyProto.TelephonyEvent.CarrierIdMatching carrierIdMatching = new TelephonyProto.TelephonyEvent.CarrierIdMatching();
        TelephonyProto.TelephonyEvent.CarrierIdMatchingResult carrierIdMatchingResult = new TelephonyProto.TelephonyEvent.CarrierIdMatchingResult();
        if (cid != -1) {
            carrierIdMatchingResult.carrierId = cid;
            if (gid1 != null) {
                carrierIdMatchingResult.mccmnc = mccmnc;
                carrierIdMatchingResult.gid1 = gid1;
            }
        } else if (mccmnc != null) {
            carrierIdMatchingResult.mccmnc = mccmnc;
        }
        carrierIdMatching.cidTableVersion = version;
        carrierIdMatching.result = carrierIdMatchingResult;
        addTelephonyEvent(new TelephonyEventBuilder(phoneId).setCarrierIdMatching(carrierIdMatching).build());
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
