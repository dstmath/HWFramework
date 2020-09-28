package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemClock;
import android.os.WorkSource;
import android.telephony.Rlog;
import com.android.internal.telephony.dataconnection.KeepaliveStatus;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class RILRequest {
    static final String LOG_TAG = "RilRequest";
    private static final int MAX_POOL_SIZE = 4;
    static AtomicInteger sNextSerial = new AtomicInteger(0);
    private static RILRequest sPool = null;
    private static int sPoolSize = 0;
    private static Object sPoolSync = new Object();
    static Random sRandom = new Random();
    String mClientId;
    RILRequest mNext;
    @UnsupportedAppUsage
    int mRequest;
    @UnsupportedAppUsage
    Message mResult;
    @UnsupportedAppUsage
    int mSerial;
    long mStartTimeMs;
    int mWakeLockType;
    WorkSource mWorkSource;

    public int getSerial() {
        return this.mSerial;
    }

    public int getRequest() {
        return this.mRequest;
    }

    public Message getResult() {
        return this.mResult;
    }

    @UnsupportedAppUsage
    private static RILRequest obtain(int request, Message result) {
        RILRequest rr = null;
        synchronized (sPoolSync) {
            if (sPool != null) {
                rr = sPool;
                sPool = rr.mNext;
                rr.mNext = null;
                sPoolSize--;
            }
        }
        if (rr == null) {
            rr = new RILRequest();
        }
        rr.mSerial = sNextSerial.getAndUpdate($$Lambda$RILRequest$VaC9ddQXT8qxCl7rcNKtUadFQoI.INSTANCE);
        rr.mRequest = request;
        rr.mResult = result;
        rr.mWakeLockType = -1;
        rr.mWorkSource = null;
        rr.mStartTimeMs = SystemClock.elapsedRealtime();
        if (result == null || result.getTarget() != null) {
            return rr;
        }
        throw new NullPointerException("Message target must not be null");
    }

    static /* synthetic */ int lambda$obtain$0(int n) {
        return (n + 1) % KeepaliveStatus.INVALID_HANDLE;
    }

    public static RILRequest obtain(int request, Message result, WorkSource workSource) {
        RILRequest rr = obtain(request, result);
        if (workSource != null) {
            rr.mWorkSource = workSource;
            rr.mClientId = rr.getWorkSourceClientId();
        } else {
            Rlog.e(LOG_TAG, "null workSource " + request);
        }
        return rr;
    }

    public String getWorkSourceClientId() {
        WorkSource workSource = this.mWorkSource;
        if (workSource == null || workSource.isEmpty()) {
            return null;
        }
        if (this.mWorkSource.size() > 0) {
            return this.mWorkSource.get(0) + ":" + this.mWorkSource.getName(0);
        }
        ArrayList<WorkSource.WorkChain> workChains = this.mWorkSource.getWorkChains();
        if (workChains == null || workChains.isEmpty()) {
            return null;
        }
        WorkSource.WorkChain workChain = workChains.get(0);
        return workChain.getAttributionUid() + ":" + workChain.getTags()[0];
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void release() {
        synchronized (sPoolSync) {
            if (sPoolSize < 4) {
                this.mNext = sPool;
                sPool = this;
                sPoolSize++;
                this.mResult = null;
                if (this.mWakeLockType != -1 && this.mWakeLockType == 0) {
                    Rlog.e(LOG_TAG, "RILRequest releasing with held wake lock: " + serialString());
                }
            }
        }
    }

    private RILRequest() {
    }

    static void resetSerial() {
        sNextSerial.set(sRandom.nextInt(KeepaliveStatus.INVALID_HANDLE));
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public String serialString() {
        StringBuilder sb = new StringBuilder(8);
        String sn = Integer.toString(this.mSerial % 10000);
        sb.append('[');
        int s = sn.length();
        for (int i = 0; i < 4 - s; i++) {
            sb.append('0');
        }
        sb.append(sn);
        sb.append(']');
        return sb.toString();
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void onError(int error, Object ret) {
        CommandException ex = CommandException.fromRilErrno(error);
        Rlog.d(LOG_TAG, serialString() + "< " + RIL.requestToString(this.mRequest) + " error: " + ex + " ret=" + RIL.retToString(this.mRequest, ret));
        try {
            if (this.mResult != null && this.mResult.getTarget() != null) {
                AsyncResult.forMessage(this.mResult, ret, ex);
                this.mResult.sendToTarget();
            }
        } catch (IllegalStateException e) {
            Rlog.e(LOG_TAG, "RILRequest onError IllegalStateException maybe enqueueMessage has This message is already in use. ");
        } catch (Exception e2) {
            Rlog.e(LOG_TAG, "RILRequest onError Exception.");
        }
    }
}
