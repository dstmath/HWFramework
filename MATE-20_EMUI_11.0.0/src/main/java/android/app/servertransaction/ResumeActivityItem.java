package android.app.servertransaction;

import android.app.ActivityTaskManager;
import android.app.ClientTransactionHandler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.Trace;

public class ResumeActivityItem extends ActivityLifecycleItem {
    public static final Parcelable.Creator<ResumeActivityItem> CREATOR = new Parcelable.Creator<ResumeActivityItem>() {
        /* class android.app.servertransaction.ResumeActivityItem.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ResumeActivityItem createFromParcel(Parcel in) {
            return new ResumeActivityItem(in);
        }

        @Override // android.os.Parcelable.Creator
        public ResumeActivityItem[] newArray(int size) {
            return new ResumeActivityItem[size];
        }
    };
    private static final String TAG = "ResumeActivityItem";
    private boolean mIsForward;
    private int mProcState;
    private boolean mUpdateProcState;

    @Override // android.app.servertransaction.BaseClientRequest
    public void preExecute(ClientTransactionHandler client, IBinder token) {
        if (this.mUpdateProcState) {
            client.updateProcessState(this.mProcState, false);
        }
    }

    @Override // android.app.servertransaction.BaseClientRequest
    public void execute(ClientTransactionHandler client, IBinder token, PendingTransactionActions pendingActions) {
        Trace.traceBegin(64, "activityResume");
        client.handleResumeActivity(token, true, this.mIsForward, "RESUME_ACTIVITY");
        Trace.traceEnd(64);
    }

    @Override // android.app.servertransaction.BaseClientRequest
    public void postExecute(ClientTransactionHandler client, IBinder token, PendingTransactionActions pendingActions) {
        try {
            ActivityTaskManager.getService().activityResumed(token);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    @Override // android.app.servertransaction.ActivityLifecycleItem
    public int getTargetState() {
        return 3;
    }

    private ResumeActivityItem() {
    }

    public static ResumeActivityItem obtain(int procState, boolean isForward) {
        ResumeActivityItem instance = (ResumeActivityItem) ObjectPool.obtain(ResumeActivityItem.class);
        if (instance == null) {
            instance = new ResumeActivityItem();
        }
        instance.mProcState = procState;
        instance.mUpdateProcState = true;
        instance.mIsForward = isForward;
        return instance;
    }

    public static ResumeActivityItem obtain(boolean isForward) {
        ResumeActivityItem instance = (ResumeActivityItem) ObjectPool.obtain(ResumeActivityItem.class);
        if (instance == null) {
            instance = new ResumeActivityItem();
        }
        instance.mProcState = -1;
        instance.mUpdateProcState = false;
        instance.mIsForward = isForward;
        return instance;
    }

    @Override // android.app.servertransaction.ActivityLifecycleItem, android.app.servertransaction.ObjectPoolItem
    public void recycle() {
        super.recycle();
        this.mProcState = -1;
        this.mUpdateProcState = false;
        this.mIsForward = false;
        ObjectPool.recycle(this);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mProcState);
        dest.writeBoolean(this.mUpdateProcState);
        dest.writeBoolean(this.mIsForward);
    }

    private ResumeActivityItem(Parcel in) {
        this.mProcState = in.readInt();
        this.mUpdateProcState = in.readBoolean();
        this.mIsForward = in.readBoolean();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResumeActivityItem other = (ResumeActivityItem) o;
        if (this.mProcState == other.mProcState && this.mUpdateProcState == other.mUpdateProcState && this.mIsForward == other.mIsForward) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (((((17 * 31) + this.mProcState) * 31) + (this.mUpdateProcState ? 1 : 0)) * 31) + (this.mIsForward ? 1 : 0);
    }

    public String toString() {
        return "ResumeActivityItem{procState=" + this.mProcState + ",updateProcState=" + this.mUpdateProcState + ",isForward=" + this.mIsForward + "}";
    }
}
