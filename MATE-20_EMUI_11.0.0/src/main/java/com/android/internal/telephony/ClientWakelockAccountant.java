package com.android.internal.telephony;

import android.telephony.ClientRequestStats;
import android.telephony.Rlog;
import com.android.internal.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Iterator;

public class ClientWakelockAccountant {
    public static final String LOG_TAG = "ClientWakelockAccountant: ";
    @VisibleForTesting
    public ArrayList<RilWakelockInfo> mPendingRilWakelocks = new ArrayList<>();
    @VisibleForTesting
    public ClientRequestStats mRequestStats = new ClientRequestStats();

    @VisibleForTesting
    public ClientWakelockAccountant(String callingPackage) {
        this.mRequestStats.setCallingPackage(callingPackage);
    }

    @VisibleForTesting
    public void startAttributingWakelock(int request, int token, int concurrentRequests, long time) {
        RilWakelockInfo wlInfo = new RilWakelockInfo(request, token, concurrentRequests, time);
        synchronized (this.mPendingRilWakelocks) {
            this.mPendingRilWakelocks.add(wlInfo);
        }
    }

    @VisibleForTesting
    public void stopAttributingWakelock(int request, int token, long time) {
        RilWakelockInfo wlInfo = removePendingWakelock(request, token);
        if (wlInfo != null) {
            completeRequest(wlInfo, time);
        }
    }

    @VisibleForTesting
    public void stopAllPendingRequests(long time) {
        synchronized (this.mPendingRilWakelocks) {
            Iterator<RilWakelockInfo> it = this.mPendingRilWakelocks.iterator();
            while (it.hasNext()) {
                completeRequest(it.next(), time);
            }
            this.mPendingRilWakelocks.clear();
        }
    }

    @VisibleForTesting
    public void changeConcurrentRequests(int concurrentRequests, long time) {
        synchronized (this.mPendingRilWakelocks) {
            Iterator<RilWakelockInfo> it = this.mPendingRilWakelocks.iterator();
            while (it.hasNext()) {
                it.next().updateConcurrentRequests(concurrentRequests, time);
            }
        }
    }

    private void completeRequest(RilWakelockInfo wlInfo, long time) {
        wlInfo.setResponseTime(time);
        synchronized (this.mRequestStats) {
            this.mRequestStats.addCompletedWakelockTime(wlInfo.getWakelockTimeAttributedToClient());
            this.mRequestStats.incrementCompletedRequestsCount();
            this.mRequestStats.updateRequestHistograms(wlInfo.getRilRequestSent(), (int) wlInfo.getWakelockTimeAttributedToClient());
        }
    }

    @VisibleForTesting
    public int getPendingRequestCount() {
        return this.mPendingRilWakelocks.size();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x003c, code lost:
        r3 = th;
     */
    @VisibleForTesting
    public synchronized long updatePendingRequestWakelockTime(long uptime) {
        long totalPendingWakelockTime;
        Throwable th;
        totalPendingWakelockTime = 0;
        synchronized (this.mPendingRilWakelocks) {
            try {
                Iterator<RilWakelockInfo> it = this.mPendingRilWakelocks.iterator();
                while (it.hasNext()) {
                    try {
                        RilWakelockInfo wlInfo = it.next();
                        wlInfo.updateTime(uptime);
                        totalPendingWakelockTime += wlInfo.getWakelockTimeAttributedToClient();
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                }
                synchronized (this.mRequestStats) {
                    this.mRequestStats.setPendingRequestsCount((long) getPendingRequestCount());
                    this.mRequestStats.setPendingRequestsWakelockTime(totalPendingWakelockTime);
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        return totalPendingWakelockTime;
        while (true) {
        }
    }

    private RilWakelockInfo removePendingWakelock(int request, int token) {
        RilWakelockInfo result = null;
        synchronized (this.mPendingRilWakelocks) {
            Iterator<RilWakelockInfo> it = this.mPendingRilWakelocks.iterator();
            while (it.hasNext()) {
                RilWakelockInfo wlInfo = it.next();
                if (wlInfo.getTokenNumber() == token && wlInfo.getRilRequestSent() == request) {
                    result = wlInfo;
                }
            }
            if (result != null) {
                this.mPendingRilWakelocks.remove(result);
            }
        }
        if (result == null) {
            Rlog.w(LOG_TAG, "Looking for Request<" + request + "," + token + "> in " + this.mPendingRilWakelocks);
        }
        return result;
    }

    public String toString() {
        return "ClientWakelockAccountant{mRequestStats=" + this.mRequestStats + ", mPendingRilWakelocks=" + this.mPendingRilWakelocks + '}';
    }
}
