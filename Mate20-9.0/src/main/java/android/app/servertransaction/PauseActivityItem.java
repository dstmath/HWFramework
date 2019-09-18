package android.app.servertransaction;

import android.app.ActivityManager;
import android.app.ClientTransactionHandler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.Trace;

public class PauseActivityItem extends ActivityLifecycleItem {
    public static final Parcelable.Creator<PauseActivityItem> CREATOR = new Parcelable.Creator<PauseActivityItem>() {
        public PauseActivityItem createFromParcel(Parcel in) {
            return new PauseActivityItem(in);
        }

        public PauseActivityItem[] newArray(int size) {
            return new PauseActivityItem[size];
        }
    };
    private static final String TAG = "PauseActivityItem";
    private int mConfigChanges;
    private boolean mDontReport;
    private boolean mFinished;
    private boolean mUserLeaving;

    public void execute(ClientTransactionHandler client, IBinder token, PendingTransactionActions pendingActions) {
        Trace.traceBegin(64, "activityPause");
        client.handlePauseActivity(token, this.mFinished, this.mUserLeaving, this.mConfigChanges, pendingActions, "PAUSE_ACTIVITY_ITEM");
        Trace.traceEnd(64);
    }

    public int getTargetState() {
        return 4;
    }

    public void postExecute(ClientTransactionHandler client, IBinder token, PendingTransactionActions pendingActions) {
        if (!this.mDontReport) {
            try {
                ActivityManager.getService().activityPaused(token);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        }
    }

    private PauseActivityItem() {
    }

    public static PauseActivityItem obtain(boolean finished, boolean userLeaving, int configChanges, boolean dontReport) {
        PauseActivityItem instance = (PauseActivityItem) ObjectPool.obtain(PauseActivityItem.class);
        if (instance == null) {
            instance = new PauseActivityItem();
        }
        instance.mFinished = finished;
        instance.mUserLeaving = userLeaving;
        instance.mConfigChanges = configChanges;
        instance.mDontReport = dontReport;
        return instance;
    }

    public static PauseActivityItem obtain() {
        PauseActivityItem instance = (PauseActivityItem) ObjectPool.obtain(PauseActivityItem.class);
        if (instance == null) {
            instance = new PauseActivityItem();
        }
        instance.mFinished = false;
        instance.mUserLeaving = false;
        instance.mConfigChanges = 0;
        instance.mDontReport = true;
        return instance;
    }

    public void recycle() {
        super.recycle();
        this.mFinished = false;
        this.mUserLeaving = false;
        this.mConfigChanges = 0;
        this.mDontReport = false;
        ObjectPool.recycle(this);
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBoolean(this.mFinished);
        dest.writeBoolean(this.mUserLeaving);
        dest.writeInt(this.mConfigChanges);
        dest.writeBoolean(this.mDontReport);
    }

    private PauseActivityItem(Parcel in) {
        this.mFinished = in.readBoolean();
        this.mUserLeaving = in.readBoolean();
        this.mConfigChanges = in.readInt();
        this.mDontReport = in.readBoolean();
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PauseActivityItem other = (PauseActivityItem) o;
        if (!(this.mFinished == other.mFinished && this.mUserLeaving == other.mUserLeaving && this.mConfigChanges == other.mConfigChanges && this.mDontReport == other.mDontReport)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (31 * ((31 * ((31 * ((31 * 17) + (this.mFinished ? 1 : 0))) + (this.mUserLeaving ? 1 : 0))) + this.mConfigChanges)) + (this.mDontReport ? 1 : 0);
    }

    public String toString() {
        return "PauseActivityItem{finished=" + this.mFinished + ",userLeaving=" + this.mUserLeaving + ",configChanges=" + this.mConfigChanges + ",dontReport=" + this.mDontReport + "}";
    }
}
