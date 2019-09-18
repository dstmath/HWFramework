package com.android.internal.telephony.dataconnection;

import android.app.PendingIntent;
import android.net.NetworkCapabilities;
import android.net.NetworkConfig;
import android.net.NetworkRequest;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.SparseIntArray;
import com.android.internal.telephony.DctConstants;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.RetryManager;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ApnContext {
    protected static final boolean DBG = true;
    private static final String SLOG_TAG = "ApnContext";
    public final String LOG_TAG;
    private ApnSetting mApnSetting;
    private final String mApnType;
    private boolean mConcurrentVoiceAndDataAllowed;
    private final AtomicInteger mConnectionGeneration = new AtomicInteger(0);
    AtomicBoolean mDataEnabled;
    DcAsyncChannel mDcAc;
    private final DcTracker mDcTracker;
    AtomicBoolean mDependencyMet;
    private final ArrayList<LocalLog> mLocalLogs = new ArrayList<>();
    private final ArrayList<NetworkRequest> mNetworkRequests = new ArrayList<>();
    private final Phone mPhone;
    String mReason;
    PendingIntent mReconnectAlarmIntent;
    private final Object mRefCountLock = new Object();
    private final SparseIntArray mRetriesLeftPerErrorCode = new SparseIntArray();
    private final RetryManager mRetryManager;
    private DctConstants.State mState;
    private final LocalLog mStateLocalLog = new LocalLog(50);
    DcFailCause pdpFailCause = DcFailCause.NONE;
    public final int priority;

    public ApnContext(Phone phone, String apnType, String logTag, NetworkConfig config, DcTracker tracker) {
        this.mPhone = phone;
        this.mApnType = apnType;
        this.mState = DctConstants.State.IDLE;
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
        setState(DctConstants.State.IDLE);
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
        return this.mRetryManager.getDelayForNextApn(failFastEnabled || isFastRetryReason());
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

    public synchronized void setState(DctConstants.State s) {
        log("setState: " + s + ", previous state:" + this.mState);
        if (this.mState != s) {
            LocalLog localLog = this.mStateLocalLog;
            localLog.log("State changed from " + this.mState + " to " + s);
            this.mState = s;
        }
        if (this.mState == DctConstants.State.FAILED && this.mRetryManager.getWaitingApns() != null) {
            this.mRetryManager.getWaitingApns().clear();
        }
    }

    public synchronized DctConstants.State getState() {
        return this.mState;
    }

    public boolean isDisconnected() {
        DctConstants.State currentState = getState();
        return currentState == DctConstants.State.IDLE || currentState == DctConstants.State.FAILED;
    }

    public synchronized void setReason(String reason) {
        log("set reason as " + reason + ",current state " + this.mState);
        this.mReason = reason;
    }

    public synchronized String getReason() {
        return this.mReason;
    }

    public boolean isReady() {
        return this.mDataEnabled.get() && this.mDependencyMet.get();
    }

    public synchronized void setPdpFailCause(DcFailCause pdpFailReason) {
        log("pdpFailCause is " + pdpFailReason);
        this.pdpFailCause = pdpFailReason;
    }

    public synchronized DcFailCause getPdpFailCause() {
        return this.pdpFailCause;
    }

    public boolean isConnectable() {
        return isReady() && (this.mState == DctConstants.State.IDLE || this.mState == DctConstants.State.SCANNING || this.mState == DctConstants.State.RETRYING || this.mState == DctConstants.State.FAILED);
    }

    private boolean isFastRetryReason() {
        return PhoneInternalInterface.REASON_NW_TYPE_CHANGED.equals(this.mReason) || PhoneInternalInterface.REASON_APN_CHANGED.equals(this.mReason);
    }

    public boolean isConnectedOrConnecting() {
        return isReady() && (this.mState == DctConstants.State.CONNECTED || this.mState == DctConstants.State.CONNECTING || this.mState == DctConstants.State.SCANNING || this.mState == DctConstants.State.RETRYING);
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
        String provisioningApn = this.mPhone.getContext().getResources().getString(17040534);
        if (TextUtils.isEmpty(provisioningApn) || this.mApnSetting == null || this.mApnSetting.apn == null) {
            return false;
        }
        return this.mApnSetting.apn.equals(provisioningApn);
    }

    public void requestLog(String str) {
        synchronized (this.mRefCountLock) {
            Iterator<LocalLog> it = this.mLocalLogs.iterator();
            while (it.hasNext()) {
                it.next().log(str);
            }
        }
    }

    public void requestNetwork(NetworkRequest networkRequest, LocalLog log) {
        synchronized (this.mRefCountLock) {
            if (!this.mLocalLogs.contains(log)) {
                if (!this.mNetworkRequests.contains(networkRequest)) {
                    this.mLocalLogs.add(log);
                    this.mNetworkRequests.add(networkRequest);
                    log("add new request: " + networkRequest);
                    this.mDcTracker.setEnabled(apnIdForApnName(this.mApnType), true);
                }
            }
            log.log("ApnContext.requestNetwork has duplicate add - " + this.mNetworkRequests.size());
        }
    }

    public void releaseNetwork(NetworkRequest networkRequest, LocalLog log) {
        synchronized (this.mRefCountLock) {
            if (!this.mLocalLogs.contains(log)) {
                log.log("ApnContext.releaseNetwork can't find this log");
            } else {
                this.mLocalLogs.remove(log);
            }
            if (!this.mNetworkRequests.contains(networkRequest)) {
                log.log("ApnContext.releaseNetwork can't find this request (" + networkRequest + ")");
            } else {
                this.mNetworkRequests.remove(networkRequest);
                log.log("ApnContext.releaseNetwork left with " + this.mNetworkRequests.size() + " requests.");
                StringBuilder sb = new StringBuilder();
                sb.append("release request: ");
                sb.append(networkRequest);
                log(sb.toString());
                if (this.mNetworkRequests.size() > 0) {
                    log("releaseNetwork left with " + this.mNetworkRequests.size() + " requests, first is " + this.mNetworkRequests.get(0).toString());
                }
                if (this.mNetworkRequests.size() == 0) {
                    this.mDcTracker.setEnabled(apnIdForApnName(this.mApnType), false);
                }
            }
        }
    }

    public List<NetworkRequest> getNetworkRequests() {
        ArrayList arrayList;
        synchronized (this.mRefCountLock) {
            arrayList = new ArrayList(this.mNetworkRequests);
        }
        return arrayList;
    }

    public boolean hasNoRestrictedRequests(boolean excludeDun) {
        synchronized (this.mRefCountLock) {
            Iterator<NetworkRequest> it = this.mNetworkRequests.iterator();
            while (it.hasNext()) {
                NetworkRequest nr = it.next();
                if (!excludeDun || !nr.networkCapabilities.hasCapability(2)) {
                    if (!nr.networkCapabilities.hasCapability(13)) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    public void resetErrorCodeRetries() {
        requestLog("ApnContext.resetErrorCodeRetries");
        log("ApnContext.resetErrorCodeRetries");
        String[] config = this.mPhone.getContext().getResources().getStringArray(17235995);
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

    public boolean restartOnError(int errorCode) {
        boolean result = false;
        synchronized (this.mRetriesLeftPerErrorCode) {
            switch (this.mRetriesLeftPerErrorCode.get(errorCode)) {
                case 0:
                    break;
                case 1:
                    resetErrorCodeRetries();
                    result = true;
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

    /* access modifiers changed from: package-private */
    public long getRetryAfterDisconnectDelay() {
        return this.mRetryManager.getRetryAfterDisconnectDelay();
    }

    public static int apnIdForType(int networkType) {
        if (networkType == 0) {
            return 0;
        }
        if (networkType == 48) {
            return 20;
        }
        switch (networkType) {
            case 2:
                return 1;
            case 3:
                return 2;
            case 4:
                return 3;
            default:
                switch (networkType) {
                    case 10:
                        return 6;
                    case 11:
                        return 5;
                    case 12:
                        return 7;
                    default:
                        switch (networkType) {
                            case 14:
                                return 8;
                            case 15:
                                return 9;
                            default:
                                switch (networkType) {
                                    case 38:
                                        return 12;
                                    case 39:
                                        return 13;
                                    case 40:
                                        return 14;
                                    case 41:
                                        return 15;
                                    case 42:
                                        return 16;
                                    case 43:
                                        return 17;
                                    case 44:
                                        return 18;
                                    case 45:
                                        return 19;
                                    default:
                                        return -1;
                                }
                        }
                }
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
                error = true;
            }
            apnId = 1;
        }
        if (nc.hasCapability(1)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 2;
        }
        if (nc.hasCapability(2)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 3;
        }
        if (nc.hasCapability(3)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 6;
        }
        if (nc.hasCapability(4)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 5;
        }
        if (nc.hasCapability(5)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 7;
        }
        if (nc.hasCapability(7)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 8;
        }
        if (nc.hasCapability(8)) {
            if (apnId != -1) {
                error = true;
            }
            Rlog.d(SLOG_TAG, "RCS APN type not yet supported");
        }
        if (nc.hasCapability(9)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 19;
        }
        if (nc.hasCapability(10)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 9;
        }
        if (nc.hasCapability(23)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 12;
        }
        if (nc.hasCapability(24)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 13;
        }
        if (nc.hasCapability(25)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 14;
        }
        if (nc.hasCapability(26)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 15;
        }
        if (nc.hasCapability(27)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 16;
        }
        if (nc.hasCapability(28)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 17;
        }
        if (nc.hasCapability(29)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 18;
        }
        if (nc.hasCapability(30)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 20;
        }
        if (error) {
            Rlog.d(SLOG_TAG, "Multiple apn types specified in request - result is unspecified!");
        }
        if (apnId == -1) {
            Rlog.d(SLOG_TAG, "Unsupported NetworkRequest in Telephony: nr=" + nr);
        }
        return apnId;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    public static int apnIdForApnName(String type) {
        char c;
        String str = type;
        int hashCode = type.hashCode();
        switch (hashCode) {
            case 3023943:
                if (str.equals("bip0")) {
                    c = 11;
                    break;
                }
            case 3023944:
                if (str.equals("bip1")) {
                    c = 12;
                    break;
                }
            case 3023945:
                if (str.equals("bip2")) {
                    c = 13;
                    break;
                }
            case 3023946:
                if (str.equals("bip3")) {
                    c = 14;
                    break;
                }
            case 3023947:
                if (str.equals("bip4")) {
                    c = 15;
                    break;
                }
            case 3023948:
                if (str.equals("bip5")) {
                    c = 16;
                    break;
                }
            case 3023949:
                if (str.equals("bip6")) {
                    c = 17;
                    break;
                }
            default:
                switch (hashCode) {
                    case -1490587420:
                        if (str.equals("internaldefault")) {
                            c = 18;
                            break;
                        }
                    case 3352:
                        if (str.equals("ia")) {
                            c = 8;
                            break;
                        }
                    case 98292:
                        if (str.equals("cbs")) {
                            c = 7;
                            break;
                        }
                    case 99837:
                        if (str.equals("dun")) {
                            c = 3;
                            break;
                        }
                    case 104399:
                        if (str.equals("ims")) {
                            c = 5;
                            break;
                        }
                    case 108243:
                        if (str.equals("mms")) {
                            c = 1;
                            break;
                        }
                    case 3149046:
                        if (str.equals("fota")) {
                            c = 6;
                            break;
                        }
                    case 3541982:
                        if (str.equals("supl")) {
                            c = 2;
                            break;
                        }
                    case 3673178:
                        if (str.equals("xcap")) {
                            c = 10;
                            break;
                        }
                    case 99285510:
                        if (str.equals("hipri")) {
                            c = 4;
                            break;
                        }
                    case 1544803905:
                        if (str.equals("default")) {
                            c = 0;
                            break;
                        }
                    case 1629013393:
                        if (str.equals("emergency")) {
                            c = 9;
                            break;
                        }
                }
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            case 6:
                return 6;
            case 7:
                return 7;
            case 8:
                return 8;
            case 9:
                return 9;
            case 10:
                return 19;
            case 11:
                return 12;
            case 12:
                return 13;
            case 13:
                return 14;
            case 14:
                return 15;
            case 15:
                return 16;
            case 16:
                return 17;
            case 17:
                return 18;
            case 18:
                return 20;
            default:
                return -1;
        }
    }

    private static String apnNameForApnId(int id) {
        switch (id) {
            case 0:
                return "default";
            case 1:
                return "mms";
            case 2:
                return "supl";
            case 3:
                return "dun";
            case 4:
                return "hipri";
            case 5:
                return "ims";
            case 6:
                return "fota";
            case 7:
                return "cbs";
            case 8:
                return "ia";
            case 9:
                return "emergency";
            case 12:
                return "bip0";
            case 13:
                return "bip1";
            case 14:
                return "bip2";
            case 15:
                return "bip3";
            case 16:
                return "bip4";
            case 17:
                return "bip5";
            case 18:
                return "bip6";
            case 19:
                return "xcap";
            case 20:
                return "internaldefault";
            default:
                Rlog.d(SLOG_TAG, "Unknown id (" + id + ") in apnIdToType");
                return "default";
        }
    }

    public synchronized String toString() {
        return "{mApnType=" + this.mApnType + " mState=" + getState() + " mWaitingApns={" + this.mRetryManager.getWaitingApns() + "} mApnSetting={" + this.mApnSetting + "} mReason=" + this.mReason + " mDataEnabled=" + this.mDataEnabled + " mDependencyMet=" + this.mDependencyMet + "}";
    }

    private void log(String s) {
        String str = this.LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(this.mPhone != null ? this.mPhone.getPhoneId() : -1);
        sb.append("][ApnContext:");
        sb.append(this.mApnType);
        sb.append("] ");
        sb.append(s);
        Rlog.d(str, sb.toString());
    }

    public void dump(FileDescriptor fd, PrintWriter printWriter, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(printWriter, "  ");
        synchronized (this.mRefCountLock) {
            pw.println(toString());
            if (this.mNetworkRequests.size() > 0) {
                pw.println("NetworkRequests:");
                pw.increaseIndent();
                Iterator<NetworkRequest> it = this.mNetworkRequests.iterator();
                while (it.hasNext()) {
                    pw.println(it.next());
                }
                pw.decreaseIndent();
            }
            pw.increaseIndent();
            Iterator<LocalLog> it2 = this.mLocalLogs.iterator();
            while (it2.hasNext()) {
                it2.next().dump(fd, pw, args);
                pw.println("-----");
            }
            pw.decreaseIndent();
            pw.println("Historical APN state:");
            pw.increaseIndent();
            this.mStateLocalLog.dump(fd, pw, args);
            pw.decreaseIndent();
            pw.println(this.mRetryManager);
            pw.println("--------------------------");
        }
    }

    public boolean isLastApnSetting() {
        return this.mRetryManager.isLastApnSetting();
    }

    public void resetRetryCount() {
        this.mRetryManager.resetRetryCount();
    }
}
