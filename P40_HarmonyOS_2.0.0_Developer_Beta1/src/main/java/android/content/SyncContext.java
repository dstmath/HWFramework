package android.content;

import android.annotation.UnsupportedAppUsage;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;

public class SyncContext {
    private static final long HEARTBEAT_SEND_INTERVAL_IN_MS = 1000;
    private long mLastHeartbeatSendTime = 0;
    private ISyncContext mSyncContext;

    @UnsupportedAppUsage
    public SyncContext(ISyncContext syncContextInterface) {
        this.mSyncContext = syncContextInterface;
    }

    @UnsupportedAppUsage
    public void setStatusText(String message) {
        updateHeartbeat();
    }

    private void updateHeartbeat() {
        long now = SystemClock.elapsedRealtime();
        if (now >= this.mLastHeartbeatSendTime + 1000) {
            try {
                this.mLastHeartbeatSendTime = now;
                if (this.mSyncContext != null) {
                    this.mSyncContext.sendHeartbeat();
                }
            } catch (RemoteException e) {
            }
        }
    }

    public void onFinished(SyncResult result) {
        try {
            if (this.mSyncContext != null) {
                this.mSyncContext.onFinished(result);
            }
        } catch (RemoteException e) {
        }
    }

    public IBinder getSyncContextBinder() {
        ISyncContext iSyncContext = this.mSyncContext;
        if (iSyncContext == null) {
            return null;
        }
        return iSyncContext.asBinder();
    }
}
