package com.android.server.net;

import android.net.DataUsageRequest;
import android.net.NetworkStats;
import android.net.NetworkTemplate;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.net.VpnInfo;
import com.android.internal.util.Preconditions;
import com.android.server.job.controllers.JobStatus;
import java.util.concurrent.atomic.AtomicInteger;

public class NetworkStatsObservers {
    private static final boolean LOGV = false;
    private static final long MIN_THRESHOLD_BYTES = 2097152;
    private static final int MSG_REGISTER = 1;
    private static final int MSG_UNREGISTER = 2;
    private static final int MSG_UPDATE_STATS = 3;
    private static final String TAG = "NetworkStatsObservers";
    private final SparseArray<RequestInfo> mDataUsageRequests = new SparseArray();
    private volatile Handler mHandler;
    private Callback mHandlerCallback = new Callback() {
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    NetworkStatsObservers.this.handleRegister((RequestInfo) msg.obj);
                    return true;
                case 2:
                    NetworkStatsObservers.this.handleUnregister((DataUsageRequest) msg.obj, msg.arg1);
                    return true;
                case 3:
                    NetworkStatsObservers.this.handleUpdateStats((StatsContext) msg.obj);
                    return true;
                default:
                    return false;
            }
        }
    };
    private final AtomicInteger mNextDataUsageRequestId = new AtomicInteger();

    private static abstract class RequestInfo implements DeathRecipient {
        protected final int mAccessLevel;
        private final IBinder mBinder;
        protected final int mCallingUid;
        protected NetworkStatsCollection mCollection;
        private final Messenger mMessenger;
        protected NetworkStatsRecorder mRecorder;
        protected final DataUsageRequest mRequest;
        private final NetworkStatsObservers mStatsObserver;

        protected abstract boolean checkStats();

        protected abstract void recordSample(StatsContext statsContext);

        RequestInfo(NetworkStatsObservers statsObserver, DataUsageRequest request, Messenger messenger, IBinder binder, int callingUid, int accessLevel) {
            this.mStatsObserver = statsObserver;
            this.mRequest = request;
            this.mMessenger = messenger;
            this.mBinder = binder;
            this.mCallingUid = callingUid;
            this.mAccessLevel = accessLevel;
            try {
                this.mBinder.linkToDeath(this, 0);
            } catch (RemoteException e) {
                binderDied();
            }
        }

        public void binderDied() {
            this.mStatsObserver.unregister(this.mRequest, 1000);
            callCallback(1);
        }

        public String toString() {
            return "RequestInfo from uid:" + this.mCallingUid + " for " + this.mRequest + " accessLevel:" + this.mAccessLevel;
        }

        private void unlinkDeathRecipient() {
            if (this.mBinder != null) {
                this.mBinder.unlinkToDeath(this, 0);
            }
        }

        private void updateStats(StatsContext statsContext) {
            if (this.mRecorder == null) {
                resetRecorder();
                recordSample(statsContext);
                return;
            }
            recordSample(statsContext);
            if (checkStats()) {
                resetRecorder();
                callCallback(0);
            }
        }

        private void callCallback(int callbackType) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("DataUsageRequest", this.mRequest);
            Message msg = Message.obtain();
            msg.what = callbackType;
            msg.setData(bundle);
            try {
                this.mMessenger.send(msg);
            } catch (RemoteException e) {
                Slog.w(NetworkStatsObservers.TAG, "RemoteException caught trying to send a callback msg for " + this.mRequest);
            }
        }

        private void resetRecorder() {
            this.mRecorder = new NetworkStatsRecorder();
            this.mCollection = this.mRecorder.getSinceBoot();
        }

        private String callbackTypeToName(int callbackType) {
            switch (callbackType) {
                case 0:
                    return "LIMIT_REACHED";
                case 1:
                    return "RELEASED";
                default:
                    return "UNKNOWN";
            }
        }
    }

    private static class NetworkUsageRequestInfo extends RequestInfo {
        NetworkUsageRequestInfo(NetworkStatsObservers statsObserver, DataUsageRequest request, Messenger messenger, IBinder binder, int callingUid, int accessLevel) {
            super(statsObserver, request, messenger, binder, callingUid, accessLevel);
        }

        protected boolean checkStats() {
            if (getTotalBytesForNetwork(this.mRequest.template) > this.mRequest.thresholdInBytes) {
                return true;
            }
            return false;
        }

        protected void recordSample(StatsContext statsContext) {
            this.mRecorder.recordSnapshotLocked(statsContext.mXtSnapshot, statsContext.mActiveIfaces, null, statsContext.mCurrentTime);
        }

        private long getTotalBytesForNetwork(NetworkTemplate template) {
            return this.mCollection.getSummary(template, Long.MIN_VALUE, JobStatus.NO_LATEST_RUNTIME, this.mAccessLevel, this.mCallingUid).getTotalBytes();
        }
    }

    private static class StatsContext {
        ArrayMap<String, NetworkIdentitySet> mActiveIfaces;
        ArrayMap<String, NetworkIdentitySet> mActiveUidIfaces;
        long mCurrentTime;
        NetworkStats mUidSnapshot;
        VpnInfo[] mVpnArray;
        NetworkStats mXtSnapshot;

        StatsContext(NetworkStats xtSnapshot, NetworkStats uidSnapshot, ArrayMap<String, NetworkIdentitySet> activeIfaces, ArrayMap<String, NetworkIdentitySet> activeUidIfaces, VpnInfo[] vpnArray, long currentTime) {
            this.mXtSnapshot = xtSnapshot;
            this.mUidSnapshot = uidSnapshot;
            this.mActiveIfaces = activeIfaces;
            this.mActiveUidIfaces = activeUidIfaces;
            this.mVpnArray = vpnArray;
            this.mCurrentTime = currentTime;
        }
    }

    private static class UserUsageRequestInfo extends RequestInfo {
        UserUsageRequestInfo(NetworkStatsObservers statsObserver, DataUsageRequest request, Messenger messenger, IBinder binder, int callingUid, int accessLevel) {
            super(statsObserver, request, messenger, binder, callingUid, accessLevel);
        }

        protected boolean checkStats() {
            int[] uidsToMonitor = this.mCollection.getRelevantUids(this.mAccessLevel, this.mCallingUid);
            for (int totalBytesForNetworkUid : uidsToMonitor) {
                if (getTotalBytesForNetworkUid(this.mRequest.template, totalBytesForNetworkUid) > this.mRequest.thresholdInBytes) {
                    return true;
                }
            }
            return false;
        }

        protected void recordSample(StatsContext statsContext) {
            this.mRecorder.recordSnapshotLocked(statsContext.mUidSnapshot, statsContext.mActiveUidIfaces, statsContext.mVpnArray, statsContext.mCurrentTime);
        }

        private long getTotalBytesForNetworkUid(NetworkTemplate template, int uid) {
            try {
                return this.mCollection.getHistory(template, uid, -1, 0, -1, Long.MIN_VALUE, JobStatus.NO_LATEST_RUNTIME, this.mAccessLevel, this.mCallingUid).getTotalBytes();
            } catch (SecurityException e) {
                return 0;
            }
        }
    }

    public DataUsageRequest register(DataUsageRequest inputRequest, Messenger messenger, IBinder binder, int callingUid, int accessLevel) {
        DataUsageRequest request = buildRequest(inputRequest);
        getHandler().sendMessage(this.mHandler.obtainMessage(1, buildRequestInfo(request, messenger, binder, callingUid, accessLevel)));
        return request;
    }

    public void unregister(DataUsageRequest request, int callingUid) {
        getHandler().sendMessage(this.mHandler.obtainMessage(2, callingUid, 0, request));
    }

    public void updateStats(NetworkStats xtSnapshot, NetworkStats uidSnapshot, ArrayMap<String, NetworkIdentitySet> activeIfaces, ArrayMap<String, NetworkIdentitySet> activeUidIfaces, VpnInfo[] vpnArray, long currentTime) {
        getHandler().sendMessage(this.mHandler.obtainMessage(3, new StatsContext(xtSnapshot, uidSnapshot, activeIfaces, activeUidIfaces, vpnArray, currentTime)));
    }

    private Handler getHandler() {
        if (this.mHandler == null) {
            synchronized (this) {
                if (this.mHandler == null) {
                    this.mHandler = new Handler(getHandlerLooperLocked(), this.mHandlerCallback);
                }
            }
        }
        return this.mHandler;
    }

    protected Looper getHandlerLooperLocked() {
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        return handlerThread.getLooper();
    }

    private void handleRegister(RequestInfo requestInfo) {
        this.mDataUsageRequests.put(requestInfo.mRequest.requestId, requestInfo);
    }

    private void handleUnregister(DataUsageRequest request, int callingUid) {
        RequestInfo requestInfo = (RequestInfo) this.mDataUsageRequests.get(request.requestId);
        if (requestInfo != null) {
            if (1000 == callingUid || requestInfo.mCallingUid == callingUid) {
                this.mDataUsageRequests.remove(request.requestId);
                requestInfo.unlinkDeathRecipient();
                requestInfo.callCallback(1);
                return;
            }
            Slog.w(TAG, "Caller uid " + callingUid + " is not owner of " + request);
        }
    }

    private void handleUpdateStats(StatsContext statsContext) {
        if (this.mDataUsageRequests.size() != 0) {
            for (int i = 0; i < this.mDataUsageRequests.size(); i++) {
                ((RequestInfo) this.mDataUsageRequests.valueAt(i)).updateStats(statsContext);
            }
        }
    }

    private DataUsageRequest buildRequest(DataUsageRequest request) {
        long thresholdInBytes = Math.max(MIN_THRESHOLD_BYTES, request.thresholdInBytes);
        if (thresholdInBytes < request.thresholdInBytes) {
            Slog.w(TAG, "Threshold was too low for " + request + ". Overriding to a safer default of " + thresholdInBytes + " bytes");
        }
        return new DataUsageRequest(this.mNextDataUsageRequestId.incrementAndGet(), request.template, thresholdInBytes);
    }

    private RequestInfo buildRequestInfo(DataUsageRequest request, Messenger messenger, IBinder binder, int callingUid, int accessLevel) {
        boolean z = true;
        if (accessLevel <= 1) {
            return new UserUsageRequestInfo(this, request, messenger, binder, callingUid, accessLevel);
        }
        if (accessLevel < 2) {
            z = false;
        }
        Preconditions.checkArgument(z);
        return new NetworkUsageRequestInfo(this, request, messenger, binder, callingUid, accessLevel);
    }
}
