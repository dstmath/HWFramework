package com.android.internal.telephony.dataconnection;

import android.app.PendingIntent;
import android.net.NetworkCapabilities;
import android.net.NetworkConfig;
import android.net.NetworkRequest;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.SparseIntArray;
import com.android.internal.telephony.DctConstants.State;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.RetryManager;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.cdma.SignalToneUtil;
import com.android.internal.telephony.cdma.sms.UserData;
import com.android.internal.util.IndentingPrintWriter;
import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduPersister;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ApnContext {
    protected static final boolean DBG = true;
    private static final int MAX_HISTORY_LOG_COUNT = 4;
    private static final String SLOG_TAG = "ApnContext";
    public final String LOG_TAG;
    private ApnSetting mApnSetting;
    private final String mApnType;
    private boolean mConcurrentVoiceAndDataAllowed;
    private final AtomicInteger mConnectionGeneration;
    AtomicBoolean mDataEnabled;
    DcAsyncChannel mDcAc;
    private final DcTracker mDcTracker;
    AtomicBoolean mDependencyMet;
    private final ArrayDeque<LocalLog> mHistoryLogs;
    private final ArrayList<LocalLog> mLocalLogs;
    private final Phone mPhone;
    String mReason;
    PendingIntent mReconnectAlarmIntent;
    private int mRefCount;
    private final Object mRefCountLock;
    private final SparseIntArray mRetriesLeftPerErrorCode;
    private final RetryManager mRetryManager;
    private State mState;
    DcFailCause pdpFailCause;
    public final int priority;

    public ApnContext(Phone phone, String apnType, String logTag, NetworkConfig config, DcTracker tracker) {
        this.mRefCountLock = new Object();
        this.mRefCount = 0;
        this.pdpFailCause = DcFailCause.NONE;
        this.mConnectionGeneration = new AtomicInteger(0);
        this.mLocalLogs = new ArrayList();
        this.mHistoryLogs = new ArrayDeque();
        this.mRetriesLeftPerErrorCode = new SparseIntArray();
        this.mPhone = phone;
        this.mApnType = apnType;
        this.mState = State.IDLE;
        setReason(PhoneInternalInterface.REASON_DATA_ENABLED);
        this.mDataEnabled = new AtomicBoolean(false);
        this.mDependencyMet = new AtomicBoolean(config.dependencyMet);
        this.priority = config.priority;
        this.LOG_TAG = logTag;
        this.mDcTracker = tracker;
        this.mRetryManager = new RetryManager(phone, apnType);
    }

    public String getApnType() {
        return this.mApnType;
    }

    public synchronized DcAsyncChannel getDcAc() {
        return this.mDcAc;
    }

    public synchronized void setDataConnectionAc(DcAsyncChannel dcac) {
        log("setDataConnectionAc: old dcac=" + this.mDcAc + " new dcac=" + dcac + " this=" + this);
        this.mDcAc = dcac;
    }

    public synchronized void releaseDataConnection(String reason) {
        if (this.mDcAc != null) {
            this.mDcAc.tearDown(this, reason, null);
            this.mDcAc = null;
        }
        setState(State.IDLE);
    }

    public synchronized PendingIntent getReconnectIntent() {
        return this.mReconnectAlarmIntent;
    }

    public synchronized void setReconnectIntent(PendingIntent intent) {
        this.mReconnectAlarmIntent = intent;
    }

    public synchronized ApnSetting getApnSetting() {
        log("getApnSetting: apnSetting=" + this.mApnSetting);
        return this.mApnSetting;
    }

    public synchronized void setApnSetting(ApnSetting apnSetting) {
        log("setApnSetting: apnSetting=" + apnSetting);
        this.mApnSetting = apnSetting;
    }

    public synchronized void setWaitingApns(ArrayList<ApnSetting> waitingApns) {
        this.mRetryManager.setWaitingApns(waitingApns);
    }

    public ApnSetting getNextApnSetting() {
        return this.mRetryManager.getNextApnSetting();
    }

    public void setModemSuggestedDelay(long delay) {
        this.mRetryManager.setModemSuggestedDelay(delay);
    }

    public long getDelayForNextApn(boolean failFastEnabled) {
        return this.mRetryManager.getDelayForNextApn(failFastEnabled);
    }

    public void markApnPermanentFailed(ApnSetting apn) {
        this.mRetryManager.markApnPermanentFailed(apn);
    }

    public ArrayList<ApnSetting> getWaitingApns() {
        return this.mRetryManager.getWaitingApns();
    }

    public synchronized void setConcurrentVoiceAndDataAllowed(boolean allowed) {
        this.mConcurrentVoiceAndDataAllowed = allowed;
    }

    public synchronized boolean isConcurrentVoiceAndDataAllowed() {
        return this.mConcurrentVoiceAndDataAllowed;
    }

    public synchronized void setState(State s) {
        log("setState: " + s + ", previous state:" + this.mState);
        this.mState = s;
        if (this.mState == State.FAILED && this.mRetryManager.getWaitingApns() != null) {
            this.mRetryManager.getWaitingApns().clear();
        }
    }

    public synchronized State getState() {
        return this.mState;
    }

    public boolean isDisconnected() {
        State currentState = getState();
        if (currentState == State.IDLE || currentState == State.FAILED) {
            return DBG;
        }
        return false;
    }

    public synchronized void setReason(String reason) {
        log("set reason as " + reason + ",current state " + this.mState);
        this.mReason = reason;
    }

    public synchronized String getReason() {
        return this.mReason;
    }

    public boolean isReady() {
        return this.mDataEnabled.get() ? this.mDependencyMet.get() : false;
    }

    public synchronized void setPdpFailCause(DcFailCause pdpFailReason) {
        log("pdpFailCause is " + pdpFailReason);
        this.pdpFailCause = pdpFailReason;
    }

    public synchronized DcFailCause getPdpFailCause() {
        return this.pdpFailCause;
    }

    public boolean isConnectable() {
        if (!isReady()) {
            return false;
        }
        if (this.mState == State.IDLE || this.mState == State.SCANNING || this.mState == State.RETRYING || this.mState == State.FAILED) {
            return DBG;
        }
        return false;
    }

    public boolean isConnectedOrConnecting() {
        if (!isReady()) {
            return false;
        }
        if (this.mState == State.CONNECTED || this.mState == State.CONNECTING || this.mState == State.SCANNING || this.mState == State.RETRYING) {
            return DBG;
        }
        return false;
    }

    public void setEnabled(boolean enabled) {
        log("set enabled as " + enabled + ", current state is " + this.mDataEnabled.get());
        this.mDataEnabled.set(enabled);
    }

    public boolean isEnabled() {
        return this.mDataEnabled.get();
    }

    public void setDependencyMet(boolean met) {
        log("set mDependencyMet as " + met + " current state is " + this.mDependencyMet.get());
        this.mDependencyMet.set(met);
    }

    public boolean getDependencyMet() {
        return this.mDependencyMet.get();
    }

    public boolean isProvisioningApn() {
        String provisioningApn = this.mPhone.getContext().getResources().getString(17039433);
        if (TextUtils.isEmpty(provisioningApn) || this.mApnSetting == null || this.mApnSetting.apn == null) {
            return false;
        }
        return this.mApnSetting.apn.equals(provisioningApn);
    }

    public void requestLog(String str) {
        synchronized (this.mRefCountLock) {
            for (LocalLog l : this.mLocalLogs) {
                l.log(str);
            }
        }
    }

    public void incRefCount(LocalLog log) {
        synchronized (this.mRefCountLock) {
            if (this.mLocalLogs.contains(log)) {
                log.log("ApnContext.incRefCount has duplicate add - " + this.mRefCount);
            } else {
                this.mLocalLogs.add(log);
                log.log("ApnContext.incRefCount - " + this.mRefCount);
            }
            int i = this.mRefCount;
            this.mRefCount = i + 1;
            if (i == 0) {
                this.mDcTracker.setEnabled(apnIdForApnName(this.mApnType), DBG);
            }
        }
    }

    public void decRefCount(LocalLog log) {
        synchronized (this.mRefCountLock) {
            if (this.mLocalLogs.remove(log)) {
                log.log("ApnContext.decRefCount - " + this.mRefCount);
                this.mHistoryLogs.addFirst(log);
                while (this.mHistoryLogs.size() > MAX_HISTORY_LOG_COUNT) {
                    this.mHistoryLogs.removeLast();
                }
            } else {
                log.log("ApnContext.decRefCount didn't find log - " + this.mRefCount);
            }
            int i = this.mRefCount;
            this.mRefCount = i - 1;
            if (i == 1) {
                this.mDcTracker.setEnabled(apnIdForApnName(this.mApnType), false);
            }
            if (this.mRefCount < 0) {
                log.log("ApnContext.decRefCount went to " + this.mRefCount);
                this.mRefCount = 0;
            }
        }
    }

    public void resetErrorCodeRetries() {
        requestLog("ApnContext.resetErrorCodeRetries");
        log("ApnContext.resetErrorCodeRetries");
        String[] config = this.mPhone.getContext().getResources().getStringArray(17236041);
        synchronized (this.mRetriesLeftPerErrorCode) {
            this.mRetriesLeftPerErrorCode.clear();
            for (String c : config) {
                String[] errorValue = c.split(",");
                if (errorValue == null || errorValue.length != 2) {
                    log("Exception parsing config_retries_per_error_code: " + c);
                } else {
                    try {
                        int errorCode = Integer.parseInt(errorValue[0]);
                        int count = Integer.parseInt(errorValue[1]);
                        if (count > 0 && errorCode > 0) {
                            this.mRetriesLeftPerErrorCode.put(errorCode, count);
                        }
                    } catch (NumberFormatException e) {
                        log("Exception parsing config_retries_per_error_code: " + e);
                    }
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean restartOnError(int errorCode) {
        int retriesLeft;
        boolean result = false;
        synchronized (this.mRetriesLeftPerErrorCode) {
            retriesLeft = this.mRetriesLeftPerErrorCode.get(errorCode);
            switch (retriesLeft) {
                case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
                    break;
                case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                    resetErrorCodeRetries();
                    result = DBG;
                    break;
                default:
                    this.mRetriesLeftPerErrorCode.put(errorCode, retriesLeft - 1);
                    result = false;
                    break;
            }
        }
        String str = "ApnContext.restartOnError(" + errorCode + ") found " + retriesLeft + " and returned " + result;
        log(str);
        requestLog(str);
        return result;
    }

    public int incAndGetConnectionGeneration() {
        return this.mConnectionGeneration.incrementAndGet();
    }

    public int getConnectionGeneration() {
        return this.mConnectionGeneration.get();
    }

    public long getInterApnDelay(boolean failFastEnabled) {
        return this.mRetryManager.getInterApnDelay(failFastEnabled);
    }

    public static int apnIdForType(int networkType) {
        switch (networkType) {
            case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
                return 0;
            case PduPersister.PROC_STATUS_PERMANENTLY_FAILURE /*2*/:
                return 1;
            case PduPersister.PROC_STATUS_COMPLETED /*3*/:
                return 2;
            case MAX_HISTORY_LOG_COUNT /*4*/:
                return 3;
            case CharacterSets.ISO_8859_7 /*10*/:
                return 6;
            case CharacterSets.ISO_8859_8 /*11*/:
                return 5;
            case CharacterSets.ISO_8859_9 /*12*/:
                return 7;
            case SmsHeader.ELT_ID_LARGE_ANIMATION /*14*/:
                return 8;
            case SignalToneUtil.IS95_CONST_IR_SIG_ISDN_OFF /*15*/:
                return 9;
            case 45:
                return 19;
            default:
                return -1;
        }
    }

    public static int apnIdForNetworkRequest(NetworkRequest nr) {
        NetworkCapabilities nc = nr.networkCapabilities;
        if (nc.getTransportTypes().length > 0 && !nc.hasTransport(0)) {
            return -1;
        }
        int apnId = -1;
        boolean error = false;
        if (nc.hasCapability(12)) {
            apnId = 0;
        }
        if (nc.hasCapability(0)) {
            if (apnId != -1) {
                error = DBG;
            }
            apnId = 1;
        }
        if (nc.hasCapability(1)) {
            if (apnId != -1) {
                error = DBG;
            }
            apnId = 2;
        }
        if (nc.hasCapability(2)) {
            if (apnId != -1) {
                error = DBG;
            }
            apnId = 3;
        }
        if (nc.hasCapability(3)) {
            if (apnId != -1) {
                error = DBG;
            }
            apnId = 6;
        }
        if (nc.hasCapability(MAX_HISTORY_LOG_COUNT)) {
            if (apnId != -1) {
                error = DBG;
            }
            apnId = 5;
        }
        if (nc.hasCapability(5)) {
            if (apnId != -1) {
                error = DBG;
            }
            apnId = 7;
        }
        if (nc.hasCapability(7)) {
            if (apnId != -1) {
                error = DBG;
            }
            apnId = 8;
        }
        if (nc.hasCapability(8)) {
            if (apnId != -1) {
                error = DBG;
            }
            Rlog.d(SLOG_TAG, "RCS APN type not yet supported");
        }
        if (nc.hasCapability(9)) {
            if (apnId != -1) {
                error = DBG;
            }
            apnId = 19;
        }
        if (nc.hasCapability(10)) {
            if (apnId != -1) {
                error = DBG;
            }
            apnId = 1;
        }
        if (nc.hasCapability(18)) {
            if (apnId != -1) {
                error = DBG;
            }
            apnId = 12;
        }
        if (nc.hasCapability(19)) {
            if (apnId != -1) {
                error = DBG;
            }
            apnId = 13;
        }
        if (nc.hasCapability(20)) {
            if (apnId != -1) {
                error = DBG;
            }
            apnId = 14;
        }
        if (nc.hasCapability(21)) {
            if (apnId != -1) {
                error = DBG;
            }
            apnId = 15;
        }
        if (nc.hasCapability(22)) {
            if (apnId != -1) {
                error = DBG;
            }
            apnId = 16;
        }
        if (nc.hasCapability(23)) {
            if (apnId != -1) {
                error = DBG;
            }
            apnId = 17;
        }
        if (nc.hasCapability(24)) {
            if (apnId != -1) {
                error = DBG;
            }
            apnId = 18;
        }
        if (error) {
            Rlog.d(SLOG_TAG, "Multiple apn types specified in request - result is unspecified!");
        }
        if (apnId == -1) {
            Rlog.d(SLOG_TAG, "Unsupported NetworkRequest in Telephony: nr=" + nr);
        }
        return apnId;
    }

    public static int apnIdForApnName(String type) {
        if (type.equals("default")) {
            return 0;
        }
        if (type.equals("mms")) {
            return 1;
        }
        if (type.equals("supl")) {
            return 2;
        }
        if (type.equals("dun")) {
            return 3;
        }
        if (type.equals("hipri")) {
            return MAX_HISTORY_LOG_COUNT;
        }
        if (type.equals("ims")) {
            return 5;
        }
        if (type.equals("fota")) {
            return 6;
        }
        if (type.equals("cbs")) {
            return 7;
        }
        if (type.equals("ia")) {
            return 8;
        }
        if (type.equals("emergency")) {
            return 9;
        }
        if (type.equals("xcap")) {
            return 19;
        }
        if (type.equals("bip0")) {
            return 12;
        }
        if (type.equals("bip1")) {
            return 13;
        }
        if (type.equals("bip2")) {
            return 14;
        }
        if (type.equals("bip3")) {
            return 15;
        }
        if (type.equals("bip4")) {
            return 16;
        }
        if (type.equals("bip5")) {
            return 17;
        }
        if (type.equals("bip6")) {
            return 18;
        }
        return -1;
    }

    private static String apnNameForApnId(int id) {
        switch (id) {
            case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
                return "default";
            case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                return "mms";
            case PduPersister.PROC_STATUS_PERMANENTLY_FAILURE /*2*/:
                return "supl";
            case PduPersister.PROC_STATUS_COMPLETED /*3*/:
                return "dun";
            case MAX_HISTORY_LOG_COUNT /*4*/:
                return "hipri";
            case CharacterSets.ISO_8859_2 /*5*/:
                return "ims";
            case CharacterSets.ISO_8859_3 /*6*/:
                return "fota";
            case CharacterSets.ISO_8859_4 /*7*/:
                return "cbs";
            case CharacterSets.ISO_8859_5 /*8*/:
                return "ia";
            case CharacterSets.ISO_8859_6 /*9*/:
                return "emergency";
            case CharacterSets.ISO_8859_9 /*12*/:
                return "bip0";
            case UserData.ASCII_CR_INDEX /*13*/:
                return "bip1";
            case SmsHeader.ELT_ID_LARGE_ANIMATION /*14*/:
                return "bip2";
            case SignalToneUtil.IS95_CONST_IR_SIG_ISDN_OFF /*15*/:
                return "bip3";
            case PduHeaders.MMS_VERSION_1_0 /*16*/:
                return "bip4";
            case PduHeaders.MMS_VERSION_1_1 /*17*/:
                return "bip5";
            case PduHeaders.MMS_VERSION_1_2 /*18*/:
                return "bip6";
            case PduHeaders.MMS_VERSION_1_3 /*19*/:
                return "xcap";
            default:
                Rlog.d(SLOG_TAG, "Unknown id (" + id + ") in apnIdToType");
                return "default";
        }
    }

    public synchronized String toString() {
        return "{mApnType=" + this.mApnType + " mState=" + getState() + " mWaitingApns={" + this.mRetryManager.getWaitingApns() + "}" + " mApnSetting={" + this.mApnSetting + "} mReason=" + this.mReason + " mDataEnabled=" + this.mDataEnabled + " mDependencyMet=" + this.mDependencyMet + "}";
    }

    private void log(String s) {
        Rlog.d(this.LOG_TAG, "[ApnContext:" + this.mApnType + "] " + s);
    }

    public void dump(FileDescriptor fd, PrintWriter printWriter, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(printWriter, "  ");
        synchronized (this.mRefCountLock) {
            pw.println(toString());
            pw.increaseIndent();
            for (LocalLog l : this.mLocalLogs) {
                l.dump(fd, pw, args);
            }
            if (this.mHistoryLogs.size() > 0) {
                pw.println("Historical Logs:");
            }
            for (LocalLog l2 : this.mHistoryLogs) {
                l2.dump(fd, pw, args);
            }
            pw.decreaseIndent();
            pw.println("mRetryManager={" + this.mRetryManager.toString() + "}");
        }
    }

    public boolean isLastApnSetting() {
        return this.mRetryManager.isLastApnSetting();
    }
}
