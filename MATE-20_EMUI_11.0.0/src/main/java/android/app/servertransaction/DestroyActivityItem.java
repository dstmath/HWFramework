package android.app.servertransaction;

import android.app.ClientTransactionHandler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Trace;

public class DestroyActivityItem extends ActivityLifecycleItem {
    public static final Parcelable.Creator<DestroyActivityItem> CREATOR = new Parcelable.Creator<DestroyActivityItem>() {
        /* class android.app.servertransaction.DestroyActivityItem.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DestroyActivityItem createFromParcel(Parcel in) {
            return new DestroyActivityItem(in);
        }

        @Override // android.os.Parcelable.Creator
        public DestroyActivityItem[] newArray(int size) {
            return new DestroyActivityItem[size];
        }
    };
    private int mConfigChanges;
    private boolean mFinished;

    @Override // android.app.servertransaction.BaseClientRequest
    public void preExecute(ClientTransactionHandler client, IBinder token) {
        client.getActivitiesToBeDestroyed().put(token, this);
    }

    @Override // android.app.servertransaction.BaseClientRequest
    public void execute(ClientTransactionHandler client, IBinder token, PendingTransactionActions pendingActions) {
        Trace.traceBegin(64, "activityDestroy");
        client.handleDestroyActivity(token, this.mFinished, this.mConfigChanges, false, "DestroyActivityItem");
        Trace.traceEnd(64);
    }

    @Override // android.app.servertransaction.ActivityLifecycleItem
    public int getTargetState() {
        return 6;
    }

    private DestroyActivityItem() {
    }

    public static DestroyActivityItem obtain(boolean finished, int configChanges) {
        DestroyActivityItem instance = (DestroyActivityItem) ObjectPool.obtain(DestroyActivityItem.class);
        if (instance == null) {
            instance = new DestroyActivityItem();
        }
        instance.mFinished = finished;
        instance.mConfigChanges = configChanges;
        return instance;
    }

    @Override // android.app.servertransaction.ActivityLifecycleItem, android.app.servertransaction.ObjectPoolItem
    public void recycle() {
        super.recycle();
        this.mFinished = false;
        this.mConfigChanges = 0;
        ObjectPool.recycle(this);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBoolean(this.mFinished);
        dest.writeInt(this.mConfigChanges);
    }

    private DestroyActivityItem(Parcel in) {
        this.mFinished = in.readBoolean();
        this.mConfigChanges = in.readInt();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DestroyActivityItem other = (DestroyActivityItem) o;
        if (this.mFinished == other.mFinished && this.mConfigChanges == other.mConfigChanges) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (((17 * 31) + (this.mFinished ? 1 : 0)) * 31) + this.mConfigChanges;
    }

    public String toString() {
        return "DestroyActivityItem{finished=" + this.mFinished + ",mConfigChanges=" + this.mConfigChanges + "}";
    }
}
