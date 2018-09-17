package com.android.internal.telephony;

import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemClock;
import android.os.WorkSource;
import android.telephony.Rlog;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/* compiled from: RIL */
class RILRequest {
    static final String LOG_TAG = "RilRequest";
    private static final int MAX_POOL_SIZE = 4;
    static AtomicInteger sNextSerial = new AtomicInteger(0);
    private static RILRequest sPool = null;
    private static int sPoolSize = 0;
    private static Object sPoolSync = new Object();
    static Random sRandom = new Random();
    String mClientId;
    RILRequest mNext;
    int mRequest;
    Message mResult;
    int mSerial;
    long mStartTimeMs;
    int mWakeLockType;
    WorkSource mWorkSource;

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
        rr.mSerial = sNextSerial.getAndIncrement();
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

    static RILRequest obtain(int request, Message result, WorkSource workSource) {
        RILRequest rr = obtain(request, result);
        if (workSource != null) {
            rr.mWorkSource = workSource;
            rr.mClientId = String.valueOf(workSource.get(0)) + ":" + workSource.getName(0);
        } else {
            Rlog.e(LOG_TAG, "null workSource " + request);
        }
        return rr;
    }

    void release() {
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
        sNextSerial.set(sRandom.nextInt());
    }

    String serialString() {
        StringBuilder sb = new StringBuilder(8);
        String sn = Long.toString((((long) this.mSerial) - -2147483648L) % 10000);
        sb.append('[');
        int s = sn.length();
        for (int i = 0; i < 4 - s; i++) {
            sb.append('0');
        }
        sb.append(sn);
        sb.append(']');
        return sb.toString();
    }

    void onError(int error, Object ret) {
        CommandException ex = CommandException.fromRilErrno(error);
        Rlog.d(LOG_TAG, serialString() + "< " + RIL.requestToString(this.mRequest) + " error: " + ex + " ret=" + RIL.retToString(this.mRequest, ret));
        if (this.mResult != null) {
            AsyncResult.forMessage(this.mResult, ret, ex);
            this.mResult.sendToTarget();
        }
    }
}
