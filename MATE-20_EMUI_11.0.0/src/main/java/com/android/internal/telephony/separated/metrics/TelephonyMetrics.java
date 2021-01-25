package com.android.internal.telephony.separated.metrics;

import android.os.Build;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.telephony.CallQuality;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyHistogram;
import android.telephony.TelephonyManager;
import android.telephony.data.DataCallResponse;
import android.telephony.emergency.EmergencyNumber;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsCallSession;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.feature.MmTelFeature;
import android.text.TextUtils;
import android.util.Base64;
import android.util.SparseArray;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CarrierResolver;
import com.android.internal.telephony.GsmCdmaConnection;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.RIL;
import com.android.internal.telephony.SmsResponse;
import com.android.internal.telephony.UUSInfo;
import com.android.internal.telephony.metrics.CallQualityMetrics;
import com.android.internal.telephony.metrics.CallSessionEventBuilder;
import com.android.internal.telephony.metrics.InProgressCallSession;
import com.android.internal.telephony.metrics.InProgressSmsSession;
import com.android.internal.telephony.metrics.ModemPowerMetrics;
import com.android.internal.telephony.metrics.SmsSessionEventBuilder;
import com.android.internal.telephony.metrics.TelephonyEventBuilder;
import com.android.internal.telephony.nano.TelephonyProto;
import com.android.internal.telephony.protobuf.nano.MessageNano;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

public class TelephonyMetrics extends DefaultTelephonyMetrics {
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
    private final SparseArray<TelephonyProto.ActiveSubscriptionInfo> mLastActiveSubscriptionInfos = new SparseArray<>();
    private final SparseArray<TelephonyProto.TelephonyEvent.CarrierIdMatching> mLastCarrierId = new SparseArray<>();
    private int mLastEnabledModemBitmap = ((1 << TelephonyManager.getDefault().getPhoneCount()) - 1);
    private final SparseArray<TelephonyProto.ImsCapabilities> mLastImsCapabilities = new SparseArray<>();
    private final SparseArray<TelephonyProto.ImsConnectionState> mLastImsConnectionState = new SparseArray<>();
    private final SparseArray<SparseArray<TelephonyProto.RilDataCall>> mLastRilDataCallEvents = new SparseArray<>();
    private final SparseArray<TelephonyProto.TelephonyServiceState> mLastServiceState = new SparseArray<>();
    private final SparseArray<TelephonyProto.TelephonySettings> mLastSettings = new SparseArray<>();
    private final SparseArray<Integer> mLastSimState = new SparseArray<>();
    private long mStartElapsedTimeMs = SystemClock.elapsedRealtime();
    private long mStartSystemTimeMs = System.currentTimeMillis();
    private final Deque<TelephonyProto.TelephonyEvent> mTelephonyEvents = new ArrayDeque();
    private boolean mTelephonyEventsDropped = VDBG;

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

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0048, code lost:
        if (r3.equals("--metrics") != false) goto L_0x004c;
     */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x004e  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0070  */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (args != null && args.length > 0) {
            boolean reset = DBG;
            if (args.length > 1 && "--keep".equals(args[1])) {
                reset = VDBG;
            }
            boolean z = VDBG;
            String str = args[0];
            int hashCode = str.hashCode();
            if (hashCode != -1953159389) {
                if (hashCode != 513805138) {
                    if (hashCode == 950313125 && str.equals("--metricsproto")) {
                        z = true;
                        if (!z) {
                            printAllMetrics(pw);
                            return;
                        } else if (z) {
                            pw.println(convertProtoToBase64String(buildProto()));
                            if (reset) {
                                reset();
                                return;
                            }
                            return;
                        } else if (z) {
                            pw.println(buildProto().toString());
                            return;
                        } else {
                            return;
                        }
                    }
                } else if (str.equals("--metricsprototext")) {
                    z = true;
                    if (!z) {
                    }
                }
            }
            z = true;
            if (!z) {
            }
        }
    }

    private void logv(String log) {
    }

    private static String telephonyEventToString(int event) {
        if (event == 21) {
            return "EMERGENCY_NUMBER_REPORT";
        }
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
            case SESSION_START_PRECISION_MINUTES /* 5 */:
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
            case 12:
                return "NITZ_TIME";
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
            case SESSION_START_PRECISION_MINUTES /* 5 */:
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
            case 22:
                return "AUDIO_CODEC";
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
            case SESSION_START_PRECISION_MINUTES /* 5 */:
                return "DATA_CALL_LIST_CHANGED";
            case 6:
                return "SMS_SEND";
            case 7:
                return "SMS_SEND_RESULT";
            case 8:
                return "SMS_RECEIVED";
            case 9:
            default:
                return Integer.toString(event);
            case 10:
                return "INCOMPLETE_SMS_RECEIVED";
        }
    }

    private synchronized void printAllMetrics(PrintWriter rawWriter) {
        IndentingPrintWriter pw = new IndentingPrintWriter(rawWriter, "  ");
        pw.println("Telephony metrics proto:");
        pw.println("------------------------------------------");
        pw.println("Telephony events:");
        pw.increaseIndent();
        for (TelephonyProto.TelephonyEvent event : this.mTelephonyEvents) {
            pw.print(event.timestampMillis);
            pw.print(" [");
            pw.print(event.phoneId);
            pw.print("] ");
            pw.print("T=");
            if (event.type == 2) {
                pw.print(telephonyEventToString(event.type) + "(Data RAT " + event.serviceState.dataRat + " Voice RAT " + event.serviceState.voiceRat + " Channel Number " + event.serviceState.channelNumber + ")");
            } else {
                pw.print(telephonyEventToString(event.type));
            }
            pw.println("");
        }
        pw.decreaseIndent();
        pw.println("Call sessions:");
        pw.increaseIndent();
        Iterator<TelephonyProto.TelephonyCallSession> it = this.mCompletedCallSessions.iterator();
        while (true) {
            int i = 10;
            if (!it.hasNext()) {
                break;
            }
            TelephonyProto.TelephonyCallSession callSession = it.next();
            pw.print("Start time in minutes: " + callSession.startTimeMinutes);
            pw.print(", phone: " + callSession.phoneId);
            if (callSession.eventsDropped) {
                pw.println(" Events dropped: " + callSession.eventsDropped);
            }
            pw.println(" Events: ");
            pw.increaseIndent();
            TelephonyProto.TelephonyCallSession.Event[] eventArr = callSession.events;
            int length = eventArr.length;
            int i2 = 0;
            while (i2 < length) {
                TelephonyProto.TelephonyCallSession.Event event2 = eventArr[i2];
                pw.print(event2.delay);
                pw.print(" T=");
                if (event2.type == 2) {
                    pw.println(callSessionEventToString(event2.type) + "(Data RAT " + event2.serviceState.dataRat + " Voice RAT " + event2.serviceState.voiceRat + " Channel Number " + event2.serviceState.channelNumber + ")");
                } else if (event2.type == i) {
                    pw.println(callSessionEventToString(event2.type));
                    pw.increaseIndent();
                    TelephonyProto.TelephonyCallSession.Event.RilCall[] rilCallArr = event2.calls;
                    for (TelephonyProto.TelephonyCallSession.Event.RilCall call : rilCallArr) {
                        pw.println(call.index + ". Type = " + call.type + " State = " + call.state + " End Reason " + call.callEndReason + " Precise Disconnect Cause " + call.preciseDisconnectCause + " isMultiparty = " + call.isMultiparty);
                    }
                    pw.decreaseIndent();
                } else if (event2.type == 22) {
                    pw.println(callSessionEventToString(event2.type) + "(" + event2.audioCodec + ")");
                } else {
                    pw.println(callSessionEventToString(event2.type));
                }
                i2++;
                i = 10;
            }
            pw.decreaseIndent();
        }
        pw.decreaseIndent();
        pw.println("Sms sessions:");
        pw.increaseIndent();
        int count = 0;
        for (TelephonyProto.SmsSession smsSession : this.mCompletedSmsSessions) {
            count++;
            pw.print("[" + count + "] Start time in minutes: " + smsSession.startTimeMinutes);
            StringBuilder sb = new StringBuilder();
            sb.append(", phone: ");
            sb.append(smsSession.phoneId);
            pw.print(sb.toString());
            if (smsSession.eventsDropped) {
                pw.println(", events dropped: " + smsSession.eventsDropped);
            } else {
                pw.println("");
            }
            pw.println("Events: ");
            pw.increaseIndent();
            TelephonyProto.SmsSession.Event[] eventArr2 = smsSession.events;
            for (TelephonyProto.SmsSession.Event event3 : eventArr2) {
                pw.print(event3.delay);
                pw.print(" T=");
                pw.println(smsSessionEventToString(event3.type));
                if (event3.type == 8) {
                    pw.increaseIndent();
                    int i3 = event3.smsType;
                    if (i3 == 1) {
                        pw.println("Type: SMS-PP");
                    } else if (i3 == 2) {
                        pw.println("Type: Voicemail indication");
                    } else if (i3 == 3) {
                        pw.println("Type: zero");
                    } else if (i3 == 4) {
                        pw.println("Type: WAP PUSH");
                    }
                    if (event3.errorCode != 0) {
                        pw.println("E=" + event3.errorCode);
                    }
                    pw.decreaseIndent();
                } else {
                    if (event3.type != 6) {
                        if (event3.type != 7) {
                            if (event3.type == 10) {
                                pw.increaseIndent();
                                pw.println("Received: " + event3.incompleteSms.receivedParts + "/" + event3.incompleteSms.totalParts);
                                pw.decreaseIndent();
                            }
                        }
                    }
                    pw.increaseIndent();
                    pw.println("ReqId=" + event3.rilRequestId);
                    pw.println("E=" + event3.errorCode);
                    pw.println("RilE=" + event3.error);
                    pw.println("ImsE=" + event3.imsError);
                    pw.decreaseIndent();
                }
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
        pw.println("Number of bytes sent (tx): " + s.numBytesTx);
        pw.println("Number of packets received (rx): " + s.numPacketsRx);
        pw.println("Number of bytes received (rx): " + s.numBytesRx);
        pw.println("Amount of time kernel is active because of cellular data (ms): " + s.cellularKernelActiveTimeMs);
        pw.println("Amount of time spent in very poor rx signal level (ms): " + s.timeInVeryPoorRxSignalLevelMs);
        pw.println("Amount of time modem is in sleep (ms): " + s.sleepTimeMs);
        pw.println("Amount of time modem is in idle (ms): " + s.idleTimeMs);
        pw.println("Amount of time modem is in rx (ms): " + s.rxTimeMs);
        pw.println("Amount of time modem is in tx (ms): " + Arrays.toString(s.txTimeMs));
        pw.println("Amount of time phone spent in various Radio Access Technologies (ms): " + Arrays.toString(s.timeInRatMs));
        pw.println("Amount of time phone spent in various cellular rx signal strength levels (ms): " + Arrays.toString(s.timeInRxSignalStrengthLevelMs));
        pw.println("Energy consumed across measured modem rails (mAh): " + new DecimalFormat("#.##").format(s.monitoredRailEnergyConsumedMah));
        pw.decreaseIndent();
        pw.println("Hardware Version: " + SystemProperties.get("ro.boot.revision", ""));
    }

    private static String convertProtoToBase64String(TelephonyProto.TelephonyLog proto) {
        return Base64.encodeToString(TelephonyProto.TelephonyLog.toByteArray(proto), 0);
    }

    private synchronized void reset() {
        this.mTelephonyEvents.clear();
        this.mCompletedCallSessions.clear();
        this.mCompletedSmsSessions.clear();
        this.mTelephonyEventsDropped = VDBG;
        this.mStartSystemTimeMs = System.currentTimeMillis();
        this.mStartElapsedTimeMs = SystemClock.elapsedRealtime();
        addTelephonyEvent(new TelephonyEventBuilder(this.mStartElapsedTimeMs, -1).setSimStateChange(this.mLastSimState).build());
        addTelephonyEvent(new TelephonyEventBuilder(this.mStartElapsedTimeMs, -1).setEnabledModemBitmap(this.mLastEnabledModemBitmap).build());
        for (int i = 0; i < this.mLastActiveSubscriptionInfos.size(); i++) {
            int key = this.mLastActiveSubscriptionInfos.keyAt(i);
            addTelephonyEvent(new TelephonyEventBuilder(this.mStartElapsedTimeMs, key).setActiveSubscriptionInfoChange(this.mLastActiveSubscriptionInfos.get(key)).build());
        }
        for (int i2 = 0; i2 < this.mLastServiceState.size(); i2++) {
            int key2 = this.mLastServiceState.keyAt(i2);
            addTelephonyEvent(new TelephonyEventBuilder(this.mStartElapsedTimeMs, key2).setServiceState(this.mLastServiceState.get(key2)).build());
        }
        for (int i3 = 0; i3 < this.mLastImsCapabilities.size(); i3++) {
            int key3 = this.mLastImsCapabilities.keyAt(i3);
            addTelephonyEvent(new TelephonyEventBuilder(this.mStartElapsedTimeMs, key3).setImsCapabilities(this.mLastImsCapabilities.get(key3)).build());
        }
        for (int i4 = 0; i4 < this.mLastImsConnectionState.size(); i4++) {
            int key4 = this.mLastImsConnectionState.keyAt(i4);
            addTelephonyEvent(new TelephonyEventBuilder(this.mStartElapsedTimeMs, key4).setImsConnectionState(this.mLastImsConnectionState.get(key4)).build());
        }
        for (int i5 = 0; i5 < this.mLastCarrierId.size(); i5++) {
            int key5 = this.mLastCarrierId.keyAt(i5);
            addTelephonyEvent(new TelephonyEventBuilder(this.mStartElapsedTimeMs, key5).setCarrierIdMatching(this.mLastCarrierId.get(key5)).build());
        }
        for (int i6 = 0; i6 < this.mLastRilDataCallEvents.size(); i6++) {
            int key6 = this.mLastRilDataCallEvents.keyAt(i6);
            for (int j = 0; j < this.mLastRilDataCallEvents.get(key6).size(); j++) {
                addTelephonyEvent(new TelephonyEventBuilder(this.mStartElapsedTimeMs, key6).setDataCalls(new TelephonyProto.RilDataCall[]{this.mLastRilDataCallEvents.get(key6).get(this.mLastRilDataCallEvents.get(key6).keyAt(j))}).build());
            }
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
        log.hardwareRevision = SystemProperties.get("ro.boot.revision", "");
        log.startTime = new TelephonyProto.Time();
        log.startTime.systemTimestampMillis = this.mStartSystemTimeMs;
        log.startTime.elapsedTimestampMillis = this.mStartElapsedTimeMs;
        log.endTime = new TelephonyProto.Time();
        log.endTime.systemTimestampMillis = System.currentTimeMillis();
        log.endTime.elapsedTimestampMillis = SystemClock.elapsedRealtime();
        int phoneCount = TelephonyManager.getDefault().getPhoneCount();
        TelephonyProto.ActiveSubscriptionInfo[] activeSubscriptionInfo = new TelephonyProto.ActiveSubscriptionInfo[phoneCount];
        for (int i2 = 0; i2 < this.mLastActiveSubscriptionInfos.size(); i2++) {
            int key = this.mLastActiveSubscriptionInfos.keyAt(i2);
            activeSubscriptionInfo[key] = this.mLastActiveSubscriptionInfos.get(key);
        }
        for (int i3 = 0; i3 < phoneCount; i3++) {
            if (activeSubscriptionInfo[i3] == null) {
                activeSubscriptionInfo[i3] = makeInvalidSubscriptionInfo(i3);
            }
        }
        log.lastActiveSubscriptionInfo = activeSubscriptionInfo;
        return log;
    }

    public void updateSimState(int phoneId, int simState) {
        int state = mapSimStateToProto(simState);
        Integer lastSimState = this.mLastSimState.get(phoneId);
        if (lastSimState == null || !lastSimState.equals(Integer.valueOf(state))) {
            this.mLastSimState.put(phoneId, Integer.valueOf(state));
            addTelephonyEvent(new TelephonyEventBuilder().setSimStateChange(this.mLastSimState).build());
        }
    }

    public synchronized void updateActiveSubscriptionInfoList(List<SubscriptionInfo> subInfos) {
        List<Integer> inActivePhoneList = new ArrayList<>();
        for (int i = 0; i < this.mLastActiveSubscriptionInfos.size(); i++) {
            inActivePhoneList.add(Integer.valueOf(this.mLastActiveSubscriptionInfos.keyAt(i)));
        }
        for (SubscriptionInfo info : subInfos) {
            int phoneId = info.getSimSlotIndex();
            inActivePhoneList.removeIf(new Predicate(phoneId) {
                /* class com.android.internal.telephony.separated.metrics.$$Lambda$TelephonyMetrics$osS65lUIU4j1qwXYbM97n9NZTA */
                private final /* synthetic */ int f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return ((Integer) obj).equals(Integer.valueOf(this.f$0));
                }
            });
            TelephonyProto.ActiveSubscriptionInfo activeSubscriptionInfo = new TelephonyProto.ActiveSubscriptionInfo();
            activeSubscriptionInfo.slotIndex = phoneId;
            activeSubscriptionInfo.isOpportunistic = info.isOpportunistic() ? 1 : 0;
            activeSubscriptionInfo.carrierId = info.getCarrierId();
            if (!MessageNano.messageNanoEquals(this.mLastActiveSubscriptionInfos.get(phoneId), activeSubscriptionInfo)) {
                addTelephonyEvent(new TelephonyEventBuilder(phoneId).setActiveSubscriptionInfoChange(activeSubscriptionInfo).build());
                this.mLastActiveSubscriptionInfos.put(phoneId, activeSubscriptionInfo);
            }
        }
        for (Integer num : inActivePhoneList) {
            int phoneId2 = num.intValue();
            this.mLastActiveSubscriptionInfos.remove(phoneId2);
            addTelephonyEvent(new TelephonyEventBuilder(phoneId2).setActiveSubscriptionInfoChange(makeInvalidSubscriptionInfo(phoneId2)).build());
        }
    }

    public void updateEnabledModemBitmap(int enabledModemBitmap) {
        if (this.mLastEnabledModemBitmap != enabledModemBitmap) {
            this.mLastEnabledModemBitmap = enabledModemBitmap;
            addTelephonyEvent(new TelephonyEventBuilder().setEnabledModemBitmap(this.mLastEnabledModemBitmap).build());
        }
    }

    private static TelephonyProto.ActiveSubscriptionInfo makeInvalidSubscriptionInfo(int phoneId) {
        TelephonyProto.ActiveSubscriptionInfo invalidSubscriptionInfo = new TelephonyProto.ActiveSubscriptionInfo();
        invalidSubscriptionInfo.slotIndex = phoneId;
        invalidSubscriptionInfo.carrierId = -1;
        invalidSubscriptionInfo.isOpportunistic = -1;
        return invalidSubscriptionInfo;
    }

    public static int roundSessionStart(long timestamp) {
        return (int) ((timestamp / 300000) * 5);
    }

    public void writeCarrierKeyEvent(int phoneId, int keyType, boolean isDownloadSuccessful) {
        TelephonyProto.TelephonyEvent.CarrierKeyChange carrierKeyChange = new TelephonyProto.TelephonyEvent.CarrierKeyChange();
        carrierKeyChange.keyType = keyType;
        carrierKeyChange.isDownloadSuccessful = isDownloadSuccessful;
        addTelephonyEvent(new TelephonyEventBuilder(phoneId).setCarrierKeyChange(carrierKeyChange).build());
    }

    public static int toPrivacyFuzzedTimeInterval(long previousTimestamp, long currentTimestamp) {
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
            return SESSION_START_PRECISION_MINUTES;
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
        ssProto.channelNumber = serviceState.getChannelNumber();
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
            logv("Starting a new call session on phone " + phoneId);
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
            logv("Starting a new sms session on phone " + phoneId);
            smsSession = startNewSmsSession(phoneId);
            this.mInProgressSmsSessions.append(phoneId, smsSession);
        }
        return smsSession;
    }

    private InProgressSmsSession startNewSmsSession(int phoneId) {
        InProgressSmsSession smsSession = new InProgressSmsSession(phoneId);
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
        return smsSession;
    }

    private synchronized void finishCallSession(InProgressCallSession inProgressCallSession) {
        TelephonyProto.TelephonyCallSession callSession = new TelephonyProto.TelephonyCallSession();
        callSession.events = new TelephonyProto.TelephonyCallSession.Event[inProgressCallSession.events.size()];
        inProgressCallSession.events.toArray(callSession.events);
        callSession.startTimeMinutes = inProgressCallSession.startSystemTimeMin;
        callSession.phoneId = inProgressCallSession.phoneId;
        callSession.eventsDropped = inProgressCallSession.isEventsDropped();
        if (this.mCompletedCallSessions.size() >= MAX_COMPLETED_CALL_SESSIONS) {
            this.mCompletedCallSessions.removeFirst();
        }
        this.mCompletedCallSessions.add(callSession);
        this.mInProgressCallSessions.remove(inProgressCallSession.phoneId);
        logv("Call session finished");
    }

    private synchronized void finishSmsSessionIfNeeded(InProgressSmsSession inProgressSmsSession) {
        if (inProgressSmsSession.getNumExpectedResponses() == 0) {
            finishSmsSession(inProgressSmsSession);
            this.mInProgressSmsSessions.remove(inProgressSmsSession.phoneId);
            logv("SMS session finished");
        }
    }

    private TelephonyProto.SmsSession finishSmsSession(InProgressSmsSession inProgressSmsSession) {
        TelephonyProto.SmsSession smsSession = new TelephonyProto.SmsSession();
        smsSession.events = new TelephonyProto.SmsSession.Event[inProgressSmsSession.events.size()];
        inProgressSmsSession.events.toArray(smsSession.events);
        smsSession.startTimeMinutes = inProgressSmsSession.startSystemTimeMin;
        smsSession.phoneId = inProgressSmsSession.phoneId;
        smsSession.eventsDropped = inProgressSmsSession.isEventsDropped();
        if (this.mCompletedSmsSessions.size() >= MAX_COMPLETED_SMS_SESSIONS) {
            this.mCompletedSmsSessions.removeFirst();
        }
        this.mCompletedSmsSessions.add(smsSession);
        return smsSession;
    }

    private synchronized void addTelephonyEvent(TelephonyProto.TelephonyEvent event) {
        if (this.mTelephonyEvents.size() >= MAX_TELEPHONY_EVENTS) {
            this.mTelephonyEvents.removeFirst();
            this.mTelephonyEventsDropped = DBG;
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
        boolean z = VDBG;
        if (network == 0) {
            if (feature == 1) {
                if (value != 0) {
                    z = true;
                }
                s.isEnhanced4GLteModeEnabled = z;
            } else if (feature == 2) {
                if (value != 0) {
                    z = true;
                }
                s.isVtOverLteEnabled = z;
            }
        } else if (network == 1) {
            if (feature == 1) {
                if (value != 0) {
                    z = true;
                }
                s.isWifiCallingEnabled = z;
            } else if (feature == 2) {
                if (value != 0) {
                    z = true;
                }
                s.isVtOverWifiEnabled = z;
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

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private int toPdpType(String type) {
        char c;
        switch (type.hashCode()) {
            case -2128542875:
                if (type.equals("IPV4V6")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -1986566073:
                if (type.equals("NON-IP")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 2343:
                if (type.equals("IP")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 79440:
                if (type.equals("PPP")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 2254343:
                if (type.equals("IPV6")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 329043114:
                if (type.equals("UNSTRUCTURED")) {
                    c = SESSION_START_PRECISION_MINUTES;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            return 1;
        }
        if (c == 1) {
            return 2;
        }
        if (c == 2) {
            return 3;
        }
        if (c == 3) {
            return 4;
        }
        if (c == 4) {
            return SESSION_START_PRECISION_MINUTES;
        }
        if (c == SESSION_START_PRECISION_MINUTES) {
            return 6;
        }
        Rlog.e(TAG, "Unknown type: " + type);
        return 0;
    }

    public void writeSetupDataCall(int phoneId, int radioTechnology, int profileId, String apn, int protocol) {
        TelephonyProto.TelephonyEvent.RilSetupDataCall setupDataCall = new TelephonyProto.TelephonyEvent.RilSetupDataCall();
        setupDataCall.rat = radioTechnology;
        setupDataCall.dataProfile = profileId + 1;
        if (apn != null) {
            setupDataCall.apn = apn;
        }
        setupDataCall.type = protocol + 1;
        addTelephonyEvent(new TelephonyEventBuilder(phoneId).setSetupDataCall(setupDataCall).build());
    }

    public void writeRilDeactivateDataCall(int phoneId, int rilSerial, int cid, int reason) {
        TelephonyProto.TelephonyEvent.RilDeactivateDataCall deactivateDataCall = new TelephonyProto.TelephonyEvent.RilDeactivateDataCall();
        deactivateDataCall.cid = cid;
        if (reason == 1) {
            deactivateDataCall.reason = 1;
        } else if (reason == 2) {
            deactivateDataCall.reason = 2;
        } else if (reason != 3) {
            deactivateDataCall.reason = 0;
        } else {
            deactivateDataCall.reason = 4;
        }
        addTelephonyEvent(new TelephonyEventBuilder(phoneId).setDeactivateDataCall(deactivateDataCall).build());
    }

    public void writeRilDataCallEvent(int phoneId, int cid, int apnTypeBitmask, int state) {
        SparseArray<TelephonyProto.RilDataCall> dataCallList;
        TelephonyProto.RilDataCall[] dataCalls = {new TelephonyProto.RilDataCall()};
        dataCalls[0].cid = cid;
        dataCalls[0].apnTypeBitmask = apnTypeBitmask;
        dataCalls[0].state = state;
        if (this.mLastRilDataCallEvents.get(phoneId) == null) {
            dataCallList = new SparseArray<>();
        } else if (this.mLastRilDataCallEvents.get(phoneId).get(cid) == null || !Arrays.equals(TelephonyProto.RilDataCall.toByteArray(this.mLastRilDataCallEvents.get(phoneId).get(cid)), TelephonyProto.RilDataCall.toByteArray(dataCalls[0]))) {
            dataCallList = this.mLastRilDataCallEvents.get(phoneId);
        } else {
            return;
        }
        dataCallList.put(cid, dataCalls[0]);
        this.mLastRilDataCallEvents.put(phoneId, dataCallList);
        addTelephonyEvent(new TelephonyEventBuilder(phoneId).setDataCalls(dataCalls).build());
    }

    public void writeRilCallList(int phoneId, ArrayList<GsmCdmaConnection> connections, String countryIso) {
        logv("Logging CallList Changed Connections Size = " + connections.size());
        InProgressCallSession callSession = startNewCallSessionIfNeeded(phoneId);
        if (callSession == null) {
            Rlog.e(TAG, "writeRilCallList: Call session is missing");
            return;
        }
        TelephonyProto.TelephonyCallSession.Event.RilCall[] calls = convertConnectionsToRilCalls(connections, countryIso);
        callSession.addEvent(new CallSessionEventBuilder(10).setRilCalls(calls));
        logv("Logged Call list changed");
        if (callSession.isPhoneIdle() && disconnectReasonsKnown(calls)) {
            finishCallSession(callSession);
        }
    }

    private boolean disconnectReasonsKnown(TelephonyProto.TelephonyCallSession.Event.RilCall[] calls) {
        for (TelephonyProto.TelephonyCallSession.Event.RilCall call : calls) {
            if (call.callEndReason == 0) {
                return VDBG;
            }
        }
        return DBG;
    }

    private TelephonyProto.TelephonyCallSession.Event.RilCall[] convertConnectionsToRilCalls(ArrayList<GsmCdmaConnection> mConnections, String countryIso) {
        TelephonyProto.TelephonyCallSession.Event.RilCall[] calls = new TelephonyProto.TelephonyCallSession.Event.RilCall[mConnections.size()];
        for (int i = 0; i < mConnections.size(); i++) {
            calls[i] = new TelephonyProto.TelephonyCallSession.Event.RilCall();
            calls[i].index = i;
            convertConnectionToRilCall(mConnections.get(i), calls[i], countryIso);
        }
        return calls;
    }

    private TelephonyProto.EmergencyNumberInfo convertEmergencyNumberToEmergencyNumberInfo(EmergencyNumber num) {
        TelephonyProto.EmergencyNumberInfo emergencyNumberInfo = new TelephonyProto.EmergencyNumberInfo();
        emergencyNumberInfo.address = num.getNumber();
        emergencyNumberInfo.countryIso = num.getCountryIso();
        emergencyNumberInfo.mnc = num.getMnc();
        emergencyNumberInfo.serviceCategoriesBitmask = num.getEmergencyServiceCategoryBitmask();
        emergencyNumberInfo.urns = (String[]) num.getEmergencyUrns().stream().toArray($$Lambda$TelephonyMetrics$5oD3YfrKHTcJsOy8lR_8c7S27g.INSTANCE);
        emergencyNumberInfo.numberSourcesBitmask = num.getEmergencyNumberSourceBitmask();
        emergencyNumberInfo.routing = num.getEmergencyCallRouting();
        return emergencyNumberInfo;
    }

    static /* synthetic */ String[] lambda$convertEmergencyNumberToEmergencyNumberInfo$1(int x$0) {
        return new String[x$0];
    }

    private void convertConnectionToRilCall(GsmCdmaConnection conn, TelephonyProto.TelephonyCallSession.Event.RilCall call, String countryIso) {
        if (conn.isIncoming()) {
            call.type = 2;
        } else {
            call.type = 1;
        }
        switch (AnonymousClass1.$SwitchMap$com$android$internal$telephony$Call$State[conn.getState().ordinal()]) {
            case 1:
                call.state = 1;
                break;
            case 2:
                call.state = 2;
                break;
            case 3:
                call.state = 3;
                break;
            case 4:
                call.state = 4;
                break;
            case SESSION_START_PRECISION_MINUTES /* 5 */:
                call.state = SESSION_START_PRECISION_MINUTES;
                break;
            case 6:
                call.state = 6;
                break;
            case 7:
                call.state = 7;
                break;
            case 8:
                call.state = 8;
                break;
            case 9:
                call.state = 9;
                break;
            default:
                call.state = 0;
                break;
        }
        call.callEndReason = conn.getDisconnectCause();
        call.isMultiparty = conn.isMultiparty();
        call.preciseDisconnectCause = conn.getPreciseDisconnectCause();
        if (conn.getDisconnectCause() != 0 && conn.isEmergencyCall() && conn.getEmergencyNumberInfo() != null && ThreadLocalRandom.current().nextDouble(0.0d, 100.0d) < getSamplePercentageForEmergencyCall(countryIso)) {
            call.isEmergencyCall = conn.isEmergencyCall();
            call.emergencyNumberInfo = convertEmergencyNumberToEmergencyNumberInfo(conn.getEmergencyNumberInfo());
        }
    }

    public void writeRilDial(int phoneId, GsmCdmaConnection conn, int clirMode, UUSInfo uusInfo) {
        InProgressCallSession callSession = startNewCallSessionIfNeeded(phoneId);
        logv("Logging Dial Connection = " + conn);
        if (callSession == null) {
            Rlog.e(TAG, "writeRilDial: Call session is missing");
            return;
        }
        TelephonyProto.TelephonyCallSession.Event.RilCall[] calls = {new TelephonyProto.TelephonyCallSession.Event.RilCall()};
        calls[0].index = -1;
        convertConnectionToRilCall(conn, calls[0], "");
        callSession.addEvent(callSession.startElapsedTimeMs, new CallSessionEventBuilder(6).setRilRequest(1).setRilCalls(calls));
        logv("Logged Dial event");
    }

    public void writeRilCallRing(int phoneId, char[] response) {
        InProgressCallSession callSession = startNewCallSessionIfNeeded(phoneId);
        callSession.addEvent(callSession.startElapsedTimeMs, new CallSessionEventBuilder(8));
    }

    public void writeRilHangup(int phoneId, GsmCdmaConnection conn, int callId, String countryIso) {
        InProgressCallSession callSession = this.mInProgressCallSessions.get(phoneId);
        if (callSession == null) {
            Rlog.e(TAG, "writeRilHangup: Call session is missing");
            return;
        }
        TelephonyProto.TelephonyCallSession.Event.RilCall[] calls = {new TelephonyProto.TelephonyCallSession.Event.RilCall()};
        calls[0].index = callId;
        convertConnectionToRilCall(conn, calls[0], countryIso);
        callSession.addEvent(new CallSessionEventBuilder(6).setRilRequest(3).setRilCalls(calls));
        logv("Logged Hangup event");
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
                return SESSION_START_PRECISION_MINUTES;
            case 16:
                return 7;
            default:
                String str = TAG;
                Rlog.e(str, "Unknown RIL request: " + r);
                return 0;
        }
    }

    private void writeOnSetupDataCallResponse(int phoneId, int rilSerial, int rilError, int rilRequest, DataCallResponse response) {
        TelephonyProto.TelephonyEvent.RilSetupDataCallResponse setupDataCallResponse = new TelephonyProto.TelephonyEvent.RilSetupDataCallResponse();
        TelephonyProto.RilDataCall dataCall = new TelephonyProto.RilDataCall();
        if (response != null) {
            setupDataCallResponse.status = response.getCause() == 0 ? 1 : response.getCause();
            setupDataCallResponse.suggestedRetryTimeMillis = response.getSuggestedRetryTime();
            dataCall.cid = response.getId();
            dataCall.type = response.getProtocolType() + 1;
            if (!TextUtils.isEmpty(response.getInterfaceName())) {
                dataCall.iframe = response.getInterfaceName();
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

    public synchronized void writeOnImsServiceSmsSolicitedResponse(int phoneId, int resultCode, int errorReason) {
        InProgressSmsSession smsSession = this.mInProgressSmsSessions.get(phoneId);
        if (smsSession == null) {
            Rlog.e(TAG, "SMS session is missing");
        } else {
            smsSession.addEvent(new SmsSessionEventBuilder(7).setImsServiceErrno(resultCode).setErrorCode(errorReason));
            smsSession.decreaseExpectedResponse();
            finishSmsSessionIfNeeded(smsSession);
        }
    }

    private void writeOnDeactivateDataCallResponse(int phoneId, int rilError) {
        addTelephonyEvent(new TelephonyEventBuilder(phoneId).setDeactivateDataCallResponse(rilError + 1).build());
    }

    public void writeOnRilSolicitedResponse(int phoneId, int rilSerial, int rilError, int rilRequest, Object ret) {
        if (rilRequest != 10) {
            if (!(rilRequest == 87 || rilRequest == 113)) {
                if (rilRequest != 40) {
                    if (rilRequest != 41) {
                        switch (rilRequest) {
                            case 12:
                            case 13:
                            case 14:
                                break;
                            default:
                                switch (rilRequest) {
                                    case 25:
                                    case 26:
                                        break;
                                    case 27:
                                        writeOnSetupDataCallResponse(phoneId, rilSerial, rilError, rilRequest, (DataCallResponse) ret);
                                        return;
                                    default:
                                        return;
                                }
                        }
                    } else {
                        writeOnDeactivateDataCallResponse(phoneId, rilError);
                        return;
                    }
                }
            }
            writeOnSmsSolicitedResponse(phoneId, rilSerial, rilError, (SmsResponse) ret);
            return;
        }
        writeOnCallSolicitedResponse(phoneId, rilSerial, rilError, rilRequest);
    }

    public void writeNetworkValidate(int networkValidationState) {
        addTelephonyEvent(new TelephonyEventBuilder().setNetworkValidate(networkValidationState).build());
    }

    public void writeDataSwitch(int subId, TelephonyProto.TelephonyEvent.DataSwitch dataSwitch) {
        addTelephonyEvent(new TelephonyEventBuilder(SubscriptionManager.getPhoneId(subId)).setDataSwitch(dataSwitch).build());
    }

    public void writeOnDemandDataSwitch(TelephonyProto.TelephonyEvent.OnDemandDataSwitch onDemandDataSwitch) {
        addTelephonyEvent(new TelephonyEventBuilder().setOnDemandDataSwitch(onDemandDataSwitch).build());
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.internal.telephony.separated.metrics.TelephonyMetrics$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$Call$State = new int[Call.State.values().length];
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
                $SwitchMap$com$android$internal$telephony$Call$State[Call.State.ALERTING.ordinal()] = TelephonyMetrics.SESSION_START_PRECISION_MINUTES;
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

    public void writePhoneState(int phoneId, PhoneConstants.State phoneState) {
        int state;
        int i = AnonymousClass1.$SwitchMap$com$android$internal$telephony$PhoneConstants$State[phoneState.ordinal()];
        if (i == 1) {
            state = 1;
        } else if (i == 2) {
            state = 2;
        } else if (i != 3) {
            state = 0;
        } else {
            state = 3;
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
        switch (AnonymousClass1.$SwitchMap$com$android$internal$telephony$Call$State[callState.ordinal()]) {
            case 1:
                state = 1;
                break;
            case 2:
                state = 2;
                break;
            case 3:
                state = 3;
                break;
            case 4:
                state = 4;
                break;
            case SESSION_START_PRECISION_MINUTES /* 5 */:
                state = SESSION_START_PRECISION_MINUTES;
                break;
            case 6:
                state = 6;
                break;
            case 7:
                state = 7;
                break;
            case 8:
                state = 8;
                break;
            case 9:
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

    public TelephonyProto.TelephonyCallSession.Event.CallQuality toCallQualityProto(CallQuality callQuality) {
        TelephonyProto.TelephonyCallSession.Event.CallQuality cq = new TelephonyProto.TelephonyCallSession.Event.CallQuality();
        if (callQuality != null) {
            cq.downlinkLevel = callQualityLevelToProtoEnum(callQuality.getDownlinkCallQualityLevel());
            cq.uplinkLevel = callQualityLevelToProtoEnum(callQuality.getUplinkCallQualityLevel());
            cq.durationInSeconds = callQuality.getCallDuration() / MAX_TELEPHONY_EVENTS;
            cq.rtpPacketsTransmitted = callQuality.getNumRtpPacketsTransmitted();
            cq.rtpPacketsReceived = callQuality.getNumRtpPacketsReceived();
            cq.rtpPacketsTransmittedLost = callQuality.getNumRtpPacketsTransmittedLost();
            cq.rtpPacketsNotReceived = callQuality.getNumRtpPacketsNotReceived();
            cq.averageRelativeJitterMillis = callQuality.getAverageRelativeJitter();
            cq.maxRelativeJitterMillis = callQuality.getMaxRelativeJitter();
            cq.codecType = convertImsCodec(callQuality.getCodecType());
        }
        return cq;
    }

    private static int callQualityLevelToProtoEnum(int level) {
        if (level == 0) {
            return 1;
        }
        if (level == 1) {
            return 2;
        }
        if (level == 2) {
            return 3;
        }
        if (level == 3) {
            return 4;
        }
        if (level == 4) {
            return SESSION_START_PRECISION_MINUTES;
        }
        if (level == SESSION_START_PRECISION_MINUTES) {
            return 6;
        }
        return 0;
    }

    public void writeOnImsCallTerminated(int phoneId, ImsCallSession session, ImsReasonInfo reasonInfo, CallQualityMetrics cqm, EmergencyNumber emergencyNumber, String countryIso) {
        InProgressCallSession callSession = this.mInProgressCallSessions.get(phoneId);
        if (callSession == null) {
            Rlog.e(TAG, "Call session is missing");
            return;
        }
        CallSessionEventBuilder callSessionEvent = new CallSessionEventBuilder(17);
        callSessionEvent.setCallIndex(getCallId(session));
        callSessionEvent.setImsReasonInfo(toImsReasonInfoProto(reasonInfo));
        if (cqm != null) {
            callSessionEvent.setCallQualitySummaryDl(cqm.getCallQualitySummaryDl()).setCallQualitySummaryUl(cqm.getCallQualitySummaryUl());
        }
        if (emergencyNumber != null && ThreadLocalRandom.current().nextDouble(0.0d, 100.0d) < getSamplePercentageForEmergencyCall(countryIso)) {
            callSessionEvent.setIsImsEmergencyCall((boolean) DBG);
            callSessionEvent.setImsEmergencyNumberInfo(convertEmergencyNumberToEmergencyNumberInfo(emergencyNumber));
        }
        callSession.addEvent(callSessionEvent);
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

    public synchronized void writeImsServiceSendSms(int phoneId, String format, int resultCode) {
        InProgressSmsSession smsSession = startNewSmsSessionIfNeeded(phoneId);
        smsSession.addEvent(new SmsSessionEventBuilder(6).setTech(3).setImsServiceErrno(resultCode).setFormat(convertSmsFormat(format)));
        smsSession.increaseExpectedResponse();
    }

    public synchronized void writeNewCBSms(int phoneId, int format, int priority, boolean isCMAS, boolean isETWS, int serviceCategory, int serialNumber, long deliveredTimestamp) {
        int type;
        InProgressSmsSession smsSession = startNewSmsSessionIfNeeded(phoneId);
        if (isCMAS) {
            type = 2;
        } else if (isETWS) {
            type = 1;
        } else {
            type = 3;
        }
        TelephonyProto.SmsSession.Event.CBMessage cbm = new TelephonyProto.SmsSession.Event.CBMessage();
        cbm.msgFormat = format;
        cbm.msgPriority = priority + 1;
        cbm.msgType = type;
        cbm.serviceCategory = serviceCategory;
        cbm.serialNumber = serialNumber;
        cbm.deliveredTimestampMillis = deliveredTimestamp;
        smsSession.addEvent(new SmsSessionEventBuilder(9).setCellBroadcastMessage(cbm));
        finishSmsSessionIfNeeded(smsSession);
    }

    public void writeDroppedIncomingMultipartSms(int phoneId, String format, int receivedCount, int totalCount) {
        logv("Logged dropped multipart SMS: received " + receivedCount + " out of " + totalCount);
        TelephonyProto.SmsSession.Event.IncompleteSms details = new TelephonyProto.SmsSession.Event.IncompleteSms();
        details.receivedParts = receivedCount;
        details.totalParts = totalCount;
        InProgressSmsSession smsSession = startNewSmsSession(phoneId);
        smsSession.addEvent(new SmsSessionEventBuilder(10).setFormat(convertSmsFormat(format)).setIncompleteSms(details));
        finishSmsSession(smsSession);
    }

    private void writeIncomingSmsWithType(int phoneId, int type, String format, boolean success) {
        int i;
        InProgressSmsSession smsSession = startNewSmsSession(phoneId);
        SmsSessionEventBuilder smsType = new SmsSessionEventBuilder(8).setFormat(convertSmsFormat(format)).setSmsType(type);
        if (success) {
            i = 0;
        } else {
            i = 1;
        }
        smsSession.addEvent(smsType.setErrorCode(i));
        finishSmsSession(smsSession);
    }

    public void writeIncomingSMSPP(int phoneId, String format, boolean success) {
        logv("Logged SMS-PP session. Result = " + success);
        writeIncomingSmsWithType(phoneId, 1, format, success);
    }

    public void writeIncomingVoiceMailSms(int phoneId, String format) {
        logv("Logged VoiceMail message.");
        writeIncomingSmsWithType(phoneId, 2, format, DBG);
    }

    public void writeIncomingSmsTypeZero(int phoneId, String format) {
        logv("Logged Type-0 SMS message.");
        writeIncomingSmsWithType(phoneId, 3, format, DBG);
    }

    private void writeIncomingSmsSessionWithType(int phoneId, int type, boolean smsOverIms, String format, long[] timestamps, boolean blocked, boolean success) {
        logv("Logged SMS session consisting of " + timestamps.length + " parts, over IMS = " + smsOverIms + " blocked = " + blocked + " type = " + type);
        InProgressSmsSession smsSession = startNewSmsSession(phoneId);
        int length = timestamps.length;
        for (int i = 0; i < length; i++) {
            long time = timestamps[i];
            int i2 = 1;
            SmsSessionEventBuilder tech = new SmsSessionEventBuilder(8).setFormat(convertSmsFormat(format)).setTech(smsOverIms ? 3 : 1);
            if (success) {
                i2 = 0;
            }
            smsSession.addEvent(time, tech.setErrorCode(i2).setSmsType(type).setBlocked(blocked));
        }
        finishSmsSession(smsSession);
    }

    public void writeIncomingWapPush(int phoneId, boolean smsOverIms, String format, long[] timestamps, boolean success) {
        writeIncomingSmsSessionWithType(phoneId, 4, smsOverIms, format, timestamps, VDBG, success);
    }

    public void writeIncomingSmsSession(int phoneId, boolean smsOverIms, String format, long[] timestamps, boolean blocked) {
        writeIncomingSmsSessionWithType(phoneId, 0, smsOverIms, format, timestamps, blocked, DBG);
    }

    public void writeIncomingSmsError(int phoneId, boolean smsOverIms, int result) {
        int smsError;
        logv("Incoming SMS error = " + result);
        int i = 1;
        if (result != 1) {
            if (result == 3) {
                smsError = 13;
            } else if (result != 4) {
                smsError = 1;
            } else {
                smsError = 24;
            }
            InProgressSmsSession smsSession = startNewSmsSession(phoneId);
            SmsSessionEventBuilder errorCode = new SmsSessionEventBuilder(8).setErrorCode(smsError);
            if (smsOverIms) {
                i = 3;
            }
            smsSession.addEvent(errorCode.setTech(i));
            finishSmsSession(smsSession);
        }
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

    public void writeCarrierIdMatchingEvent(int phoneId, int version, int cid, String unknownMcmnc, String unknownGid1, CarrierResolver.CarrierMatchingRule simInfo) {
        TelephonyProto.TelephonyEvent.CarrierIdMatching carrierIdMatching = new TelephonyProto.TelephonyEvent.CarrierIdMatching();
        TelephonyProto.TelephonyEvent.CarrierIdMatchingResult carrierIdMatchingResult = new TelephonyProto.TelephonyEvent.CarrierIdMatchingResult();
        if (cid != -1) {
            carrierIdMatchingResult.carrierId = cid;
            if (unknownGid1 != null) {
                carrierIdMatchingResult.unknownMccmnc = unknownMcmnc;
                carrierIdMatchingResult.unknownGid1 = unknownGid1;
            }
        } else if (unknownMcmnc != null) {
            carrierIdMatchingResult.unknownMccmnc = unknownMcmnc;
        }
        carrierIdMatchingResult.mccmnc = TextUtils.emptyIfNull(simInfo.mccMnc);
        carrierIdMatchingResult.spn = TextUtils.emptyIfNull(simInfo.spn);
        carrierIdMatchingResult.pnn = TextUtils.emptyIfNull(simInfo.plmn);
        carrierIdMatchingResult.gid1 = TextUtils.emptyIfNull(simInfo.gid1);
        carrierIdMatchingResult.gid2 = TextUtils.emptyIfNull(simInfo.gid2);
        carrierIdMatchingResult.imsiPrefix = TextUtils.emptyIfNull(simInfo.imsiPrefixPattern);
        carrierIdMatchingResult.iccidPrefix = TextUtils.emptyIfNull(simInfo.iccidPrefix);
        carrierIdMatchingResult.preferApn = TextUtils.emptyIfNull(simInfo.apn);
        if (simInfo.privilegeAccessRule != null) {
            carrierIdMatchingResult.privilegeAccessRule = (String[]) simInfo.privilegeAccessRule.stream().toArray($$Lambda$TelephonyMetrics$F9I8P5zeR9ipk0DcjfoG0kuwxMM.INSTANCE);
        }
        carrierIdMatching.cidTableVersion = version;
        carrierIdMatching.result = carrierIdMatchingResult;
        TelephonyProto.TelephonyEvent event = new TelephonyEventBuilder(phoneId).setCarrierIdMatching(carrierIdMatching).build();
        this.mLastCarrierId.put(phoneId, carrierIdMatching);
        addTelephonyEvent(event);
    }

    static /* synthetic */ String[] lambda$writeCarrierIdMatchingEvent$2(int x$0) {
        return new String[x$0];
    }

    public void writeEmergencyNumberUpdateEvent(int phoneId, EmergencyNumber emergencyNumber) {
        if (emergencyNumber != null) {
            addTelephonyEvent(new TelephonyEventBuilder(phoneId).setUpdatedEmergencyNumber(convertEmergencyNumberToEmergencyNumberInfo(emergencyNumber)).build());
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0028  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x002d A[ORIG_RETURN, RETURN, SYNTHETIC] */
    private int convertSmsFormat(String format) {
        char c;
        int hashCode = format.hashCode();
        if (hashCode != 1621908) {
            if (hashCode == 50279198 && format.equals("3gpp2")) {
                c = 1;
                if (c == 0) {
                    return 1;
                }
                if (c != 1) {
                    return 0;
                }
                return 2;
            }
        } else if (format.equals("3gpp")) {
            c = 0;
            if (c == 0) {
            }
        }
        c = 65535;
        if (c == 0) {
        }
    }

    private static int convertImsCodec(int c) {
        switch (c) {
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case SESSION_START_PRECISION_MINUTES /* 5 */:
                return SESSION_START_PRECISION_MINUTES;
            case 6:
                return 6;
            case 7:
                return 7;
            case 8:
                return 8;
            case 9:
                return 9;
            case 10:
                return 10;
            case 11:
                return 11;
            case 12:
                return 12;
            case 13:
                return 13;
            case 14:
                return 14;
            case 15:
                return 15;
            case 16:
                return 16;
            case 17:
                return 17;
            case 18:
                return 18;
            case 19:
                return 19;
            case 20:
                return 20;
            default:
                return 0;
        }
    }

    private int convertGsmCdmaCodec(int c) {
        switch (c) {
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 8;
            case 4:
                return 9;
            case SESSION_START_PRECISION_MINUTES /* 5 */:
                return 10;
            case 6:
                return 4;
            case 7:
                return SESSION_START_PRECISION_MINUTES;
            case 8:
                return 6;
            case 9:
                return 7;
            default:
                return 0;
        }
    }

    public void writeAudioCodecIms(int phoneId, ImsCallSession session) {
        InProgressCallSession callSession = this.mInProgressCallSessions.get(phoneId);
        if (callSession == null) {
            Rlog.e(TAG, "Call session is missing");
            return;
        }
        ImsCallProfile localCallProfile = session.getLocalCallProfile();
        if (localCallProfile != null) {
            int codec = convertImsCodec(localCallProfile.mMediaProfile.mAudioQuality);
            callSession.addEvent(new CallSessionEventBuilder(22).setCallIndex(getCallId(session)).setAudioCodec(codec));
            logv("Logged Audio Codec event. Value: " + codec);
        }
    }

    public void writeAudioCodecGsmCdma(int phoneId, int audioQuality) {
        InProgressCallSession callSession = this.mInProgressCallSessions.get(phoneId);
        if (callSession == null) {
            Rlog.e(TAG, "Call session is missing");
            return;
        }
        int codec = convertGsmCdmaCodec(audioQuality);
        callSession.addEvent(new CallSessionEventBuilder(22).setAudioCodec(codec));
        logv("Logged Audio Codec event. Value: " + codec);
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

    private double getSamplePercentageForEmergencyCall(String countryIso) {
        if ("cn,in".contains(countryIso)) {
            return 1.0d;
        }
        if ("us,id,br,pk,ng,bd,ru,mx,jp,et,ph,eg,vn,cd,tr,ir,de".contains(countryIso)) {
            return 5.0d;
        }
        if ("th,gb,fr,tz,it,za,mm,ke,kr,co,es,ug,ar,ua,dz,sd,iq".contains(countryIso)) {
            return 15.0d;
        }
        if ("pl,ca,af,ma,sa,pe,uz,ve,my,ao,mz,gh,np,ye,mg,kp,cm".contains(countryIso)) {
            return 25.0d;
        }
        if ("au,tw,ne,lk,bf,mw,ml,ro,kz,sy,cl,zm,gt,zw,nl,ec,sn".contains(countryIso)) {
            return 35.0d;
        }
        if ("kh,td,so,gn,ss,rw,bj,tn,bi,be,cu,bo,ht,gr,do,cz,pt".contains(countryIso)) {
            return 45.0d;
        }
        return 50.0d;
    }

    private static int mapSimStateToProto(int simState) {
        if (simState == 1) {
            return 1;
        }
        if (simState != 10) {
            return 0;
        }
        return 2;
    }
}
