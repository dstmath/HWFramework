package com.android.internal.telephony;

import android.telephony.ClientRequestStats;
import android.telephony.Rlog;
import java.util.ArrayList;

public class ClientWakelockAccountant {
    public static final String LOG_TAG = "ClientWakelockAccountant: ";
    public ArrayList<RilWakelockInfo> mPendingRilWakelocks = new ArrayList();
    public ClientRequestStats mRequestStats = new ClientRequestStats();

    public ClientWakelockAccountant(String callingPackage) {
        this.mRequestStats.setCallingPackage(callingPackage);
    }

    public void startAttributingWakelock(int request, int token, int concurrentRequests, long time) {
        RilWakelockInfo wlInfo = new RilWakelockInfo(request, token, concurrentRequests, time);
        synchronized (this.mPendingRilWakelocks) {
            this.mPendingRilWakelocks.add(wlInfo);
        }
    }

    public void stopAttributingWakelock(int request, int token, long time) {
        RilWakelockInfo wlInfo = removePendingWakelock(request, token);
        if (wlInfo != null) {
            completeRequest(wlInfo, time);
        }
    }

    public void stopAllPendingRequests(long time) {
        synchronized (this.mPendingRilWakelocks) {
            for (RilWakelockInfo wlInfo : this.mPendingRilWakelocks) {
                completeRequest(wlInfo, time);
            }
            this.mPendingRilWakelocks.clear();
        }
    }

    public void changeConcurrentRequests(int concurrentRequests, long time) {
        synchronized (this.mPendingRilWakelocks) {
            for (RilWakelockInfo wlInfo : this.mPendingRilWakelocks) {
                wlInfo.updateConcurrentRequests(concurrentRequests, time);
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

    public int getPendingRequestCount() {
        return this.mPendingRilWakelocks.size();
    }

    public synchronized long updatePendingRequestWakelockTime(long uptime) {
        long totalPendingWakelockTime;
        totalPendingWakelockTime = 0;
        synchronized (this.mPendingRilWakelocks) {
            for (RilWakelockInfo wlInfo : this.mPendingRilWakelocks) {
                wlInfo.updateTime(uptime);
                totalPendingWakelockTime += wlInfo.getWakelockTimeAttributedToClient();
            }
        }
        synchronized (this.mRequestStats) {
            this.mRequestStats.setPendingRequestsCount((long) getPendingRequestCount());
            this.mRequestStats.setPendingRequestsWakelockTime(totalPendingWakelockTime);
        }
        return totalPendingWakelockTime;
    }

    private RilWakelockInfo removePendingWakelock(int request, int token) {
        RilWakelockInfo result = null;
        synchronized (this.mPendingRilWakelocks) {
            for (RilWakelockInfo wlInfo : this.mPendingRilWakelocks) {
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
