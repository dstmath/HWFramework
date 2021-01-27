package com.android.server.locksettings.recoverablekeystore;

import android.app.PendingIntent;
import android.util.ArraySet;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;

public class RecoverySnapshotListenersStorage {
    private static final String TAG = "RecoverySnapshotLstnrs";
    @GuardedBy({"this"})
    private SparseArray<PendingIntent> mAgentIntents = new SparseArray<>();
    @GuardedBy({"this"})
    private ArraySet<Integer> mAgentsWithPendingSnapshots = new ArraySet<>();

    public synchronized void setSnapshotListener(int recoveryAgentUid, PendingIntent intent) {
        Log.i(TAG, "Registered listener for agent with uid " + recoveryAgentUid);
        this.mAgentIntents.put(recoveryAgentUid, intent);
        if (this.mAgentsWithPendingSnapshots.contains(Integer.valueOf(recoveryAgentUid))) {
            Log.i(TAG, "Snapshot already created for agent. Immediately triggering intent.");
            tryToSendIntent(recoveryAgentUid, intent);
        }
    }

    public synchronized boolean hasListener(int recoveryAgentUid) {
        return this.mAgentIntents.get(recoveryAgentUid) != null;
    }

    public synchronized void recoverySnapshotAvailable(int recoveryAgentUid) {
        PendingIntent intent = this.mAgentIntents.get(recoveryAgentUid);
        if (intent == null) {
            Log.i(TAG, "Snapshot available for agent " + recoveryAgentUid + " but agent has not yet initialized. Will notify agent when it does.");
            this.mAgentsWithPendingSnapshots.add(Integer.valueOf(recoveryAgentUid));
            return;
        }
        tryToSendIntent(recoveryAgentUid, intent);
    }

    private synchronized void tryToSendIntent(int recoveryAgentUid, PendingIntent intent) {
        try {
            intent.send();
            this.mAgentsWithPendingSnapshots.remove(Integer.valueOf(recoveryAgentUid));
            Log.d(TAG, "Successfully notified listener.");
        } catch (PendingIntent.CanceledException e) {
            Log.e(TAG, "Failed to trigger PendingIntent for " + recoveryAgentUid, e);
            this.mAgentsWithPendingSnapshots.add(Integer.valueOf(recoveryAgentUid));
        }
        return;
    }
}
