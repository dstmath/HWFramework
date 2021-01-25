package com.android.server.am;

import android.content.IIntentReceiver;
import android.util.Slog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HwBroadcastQueueEx implements IHwBroadcastQueueEx {
    private static final int INVALID_PID = -1;
    private static final boolean IS_DEBUG_BROADCAST = false;
    private static final String TAG = "HwBroadcastQueueEx";
    private static final String UNPROXY_TAG = "[unproxy]";
    private final BroadcastDispatcher mDispatcher;
    private final HwBroadcastQueue mHwBroadcastQueue;
    private final ArrayList<Integer> mProxyBroadcastPids = new ArrayList<>();
    private final String mQueueName;

    public HwBroadcastQueueEx(HwBroadcastQueue hwBroadcastQueue, BroadcastDispatcher dispatcher, String queueName) {
        this.mHwBroadcastQueue = hwBroadcastQueue;
        this.mDispatcher = dispatcher;
        this.mQueueName = queueName;
    }

    public ArrayList<Integer> getProxyBroadcastPidsLock() {
        return this.mProxyBroadcastPids;
    }

    private long proxyBroadcastByPid(List<Integer> pids, boolean isProxy, boolean isPending, long timeout) {
        BroadcastRecord record;
        int pid;
        if (pids == null) {
            return 0;
        }
        for (Integer pid2 : pids) {
            if (!this.mProxyBroadcastPids.contains(Integer.valueOf(pid2.intValue()))) {
                this.mProxyBroadcastPids.add(Integer.valueOf(pid2.intValue()));
            }
        }
        if (!isPending || this.mDispatcher.isEmpty() || (record = this.mDispatcher.getActiveBroadcastLocked()) == null || record.nextReceiver < 1 || (pid = this.mHwBroadcastQueue.getPid(record.receivers.get(record.nextReceiver - 1))) == -1 || !pids.contains(Integer.valueOf(pid))) {
            return 0;
        }
        return timeout;
    }

    public long proxyBroadcastByPidLock(List<Integer> pids, boolean isProxy, boolean isPending, long timeout) {
        if (isProxy) {
            return proxyBroadcastByPid(pids, isProxy, isPending, timeout);
        }
        List<Integer> pidList = pids != null ? pids : (ArrayList) this.mProxyBroadcastPids.clone();
        ArrayList<BroadcastRecord> orderedProxyBroadcasts = new ArrayList<>();
        ArrayList<BroadcastRecord> parallelProxyBroadcasts = new ArrayList<>();
        proxyBroadcastByPidInnerLocked(this.mHwBroadcastQueue.getParallelPendingBroadcastsLock(), pidList, parallelProxyBroadcasts);
        proxyBroadcastByPidInnerLocked(this.mHwBroadcastQueue.getOrderedPendingBroadcastsLock(), pidList, orderedProxyBroadcasts);
        this.mProxyBroadcastPids.removeAll(pidList);
        List<BroadcastRecord> parallelBroadcasts = this.mHwBroadcastQueue.getParallelBroadcastsLock();
        int paralleSize = parallelProxyBroadcasts.size();
        for (int i = 0; i < paralleSize; i++) {
            parallelBroadcasts.add(i, parallelProxyBroadcasts.get(i));
        }
        if (isPending) {
            movePendingBroadcastToProxyListByPids(this.mDispatcher.getActiveBroadcastLocked(), orderedProxyBroadcasts, pidList);
        }
        int orderSize = orderedProxyBroadcasts.size();
        int size = this.mDispatcher.getOrderedBroadcastsSize();
        for (int i2 = 0; i2 < orderSize; i2++) {
            if (!isPending || size <= 0) {
                this.mDispatcher.enqueueOrderedBroadcastLocked(i2, orderedProxyBroadcasts.get(i2));
            } else {
                this.mDispatcher.enqueueOrderedBroadcastLocked(i2 + 1, orderedProxyBroadcasts.get(i2));
            }
        }
        if (parallelProxyBroadcasts.size() <= 0 && orderedProxyBroadcasts.size() <= 0) {
            return 0;
        }
        Slog.v(TAG, UNPROXY_TAG + this.mQueueName + " Broadcast PID Parallel Broadcasts (" + parallelProxyBroadcasts.size() + ")(" + parallelProxyBroadcasts + ")");
        Slog.v(TAG, UNPROXY_TAG + this.mQueueName + " Broadcast PID Ordered Broadcasts (" + orderedProxyBroadcasts.size() + ")(" + orderedProxyBroadcasts + ")");
        this.mHwBroadcastQueue.scheduleBroadcastsLocked();
        return 0;
    }

    private void proxyBroadcastByPidInnerLocked(ArrayList<BroadcastRecord> pendingBroadcasts, List<Integer> unProxyPids, ArrayList<BroadcastRecord> unProxyBroadcasts) {
        if (pendingBroadcasts != null) {
            Iterator it = pendingBroadcasts.iterator();
            while (it.hasNext()) {
                BroadcastRecord br = it.next();
                Object nextReceiver = br.receivers.get(0);
                int pid = this.mHwBroadcastQueue.getPid(nextReceiver);
                if (pid != -1 && unProxyPids.contains(Integer.valueOf(pid))) {
                    it.remove();
                    if (!this.mHwBroadcastQueue.dropActionLocked(this.mHwBroadcastQueue.getPkg(nextReceiver), pid, br)) {
                        unProxyBroadcasts.add(br);
                    }
                }
            }
        }
    }

    private void movePendingBroadcastToProxyListByPids(BroadcastRecord record, ArrayList<BroadcastRecord> orderedProxyBroadcasts, List<Integer> pidList) {
        List<Integer> list = pidList;
        if (orderedProxyBroadcasts.size() == 0) {
            return;
        }
        if (record != null) {
            List<Object> needMoveReceivers = new ArrayList<>();
            List<Object> receivers = record.receivers;
            if (receivers == null) {
                return;
            }
            if (list != null) {
                int recIdx = record.nextReceiver;
                int numReceivers = receivers.size();
                int i = recIdx;
                while (i < numReceivers) {
                    Object target = receivers.get(i);
                    int pid = this.mHwBroadcastQueue.getPid(target);
                    if (pid != -1 && list.contains(Integer.valueOf(pid))) {
                        needMoveReceivers.add(target);
                        List<Object> tmpReceivers = new ArrayList<>(1);
                        tmpReceivers.add(target);
                        orderedProxyBroadcasts.add(new BroadcastRecord(record.queue, record.intent, record.callerApp, record.callerPackage, record.callingPid, record.callingUid, record.callerInstantApp, record.resolvedType, record.requiredPermissions, record.appOp, record.options, tmpReceivers, (IIntentReceiver) null, record.resultCode, record.resultData, record.resultExtras, record.ordered, record.sticky, record.initialSticky, record.userId, false, record.timeoutExempt));
                    }
                    i++;
                    list = pidList;
                }
                if (needMoveReceivers.size() > 0) {
                    receivers.removeAll(needMoveReceivers);
                    Slog.v(TAG, UNPROXY_TAG + this.mQueueName + ", moving receivers in Ordered Broadcasts (" + record + ") to proxyList, Move receivers : " + needMoveReceivers);
                }
            }
        }
    }
}
